package app.xedigital.ai.activity;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import app.xedigital.ai.MainActivity;
import app.xedigital.ai.R;
import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.api.APIInterface;
import app.xedigital.ai.utills.FaceOverlayView;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * FaceLoginActivity
 * <p>
 * Performs liveness detection followed by face recognition to authenticate a user.
 * <p>
 * Fix log:
 * - captureImage() was triggered twice per challenge (from both analyzeFace & processChallenge)
 * - updateStatus() was finding R.id.CaptureText which doesn't exist → NPE; fixed to R.id.statusText
 * - toggleScannerAnimation() referenced R.id.scannerLine and R.id.faceGuide which don't exist in XML
 * - progressBar (R.id.progressBar) didn't exist in XML; now references R.id.loadingPanel
 * - RuntimeException was thrown inside Retrofit onResponse → app crash; routed to handleError()
 * - backgroundExecutor was never shut down → thread/context leak
 * - FaceDetector was never closed → native ML Kit resource leak
 * - ObjectAnimator was never cancelled in onDestroy → animator/View leak
 * - Captured photo file was never deleted after use → disk accumulation
 * - authToken was redundantly re-fetched inside Retrofit callback
 * - safeUnbindCamera() created two ProcessCameraProvider futures unnecessarily
 * - ImageAnalysis was running on the main thread; moved to backgroundExecutor
 * - callFaceDetailApi() updated UI without isFinishing/isDestroyed guard
 * - Bitmap.createScaledBitmap used filter=false; changed to true for quality
 */
public class FaceLoginActivity extends AppCompatActivity {

    private static final String TAG = "FaceLoginActivity";
    // NOTE: Move this to BuildConfig or a secure server-provided value; never hardcode in production.
    private static final String COLLECTION_NAME = "consultedgeglobalpvtltd_5e970n";
    // ── Liveness state ────────────────────────────────────────────────────────
    private final AtomicBoolean isAnalyzing = new AtomicBoolean(false);
    // ── Threading ─────────────────────────────────────────────────────────────
    // Used for background bitmap work; must be shut down in onDestroy()
    private final ExecutorService backgroundExecutor = Executors.newSingleThreadExecutor();
    // ── API / Auth ────────────────────────────────────────────────────────────
    private String authToken;
    private String storedUserId;
    // ── Camera ────────────────────────────────────────────────────────────────
    private PreviewView previewView;
    private Preview preview;
    private ImageCapture imageCapture;
    private CameraSelector cameraSelector;
    private ProcessCameraProvider cameraProvider; // held so we can unbind cleanly
    // ── ML Kit ────────────────────────────────────────────────────────────────
    private FaceDetector detector;
    private volatile boolean isProcessingLiveness = false;
    private boolean isBlinking = false;
    private boolean challengeSatisfied = false;
    private LivenessChallenge currentChallenge;
    // ── UI ────────────────────────────────────────────────────────────────────
    private TextView statusText;
    private FaceOverlayView faceOverlay;
    private View loadingPanel;          // was progressBar → now the loading overlay card
    private ObjectAnimator scannerAnimator;

    // ─────────────────────────────────────────────────────────────────────────
    // Lifecycle
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_login);

        // ── Bind views ────────────────────────────────────────────────────────
        statusText = findViewById(R.id.statusText);   // FIX: was R.id.CaptureText (didn't exist → NPE)
        faceOverlay = findViewById(R.id.faceOverlay);
        loadingPanel = findViewById(R.id.loadingPanel); // FIX: was R.id.progressBar (didn't exist → NPE)
        previewView = findViewById(R.id.viewFinder);

        // ── Auth data ─────────────────────────────────────────────────────────
        authToken = getIntent().getStringExtra("authToken"); // read once; re-used throughout
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        storedUserId = prefs.getString("userId", null);

        // ── ML Kit face detector ──────────────────────────────────────────────
        FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .build();
        detector = FaceDetection.getClient(options);

        setRandomChallenge();
        startCamera();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // FIX: Shut down executor to prevent thread/context leak
        backgroundExecutor.shutdownNow();

        // FIX: Close ML Kit detector to release native resources
        if (detector != null) {
            detector.close();
        }

        // FIX: Cancel animator to avoid holding a reference to the View (and thus Activity)
        if (scannerAnimator != null) {
            scannerAnimator.cancel();
            scannerAnimator = null;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Camera
    // ─────────────────────────────────────────────────────────────────────────

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> future = ProcessCameraProvider.getInstance(this);

        future.addListener(() -> {
            try {
                cameraProvider = future.get(); // FIX: store reference; reuse in safeUnbindCamera()

                preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();

                // FIX: Run analysis on backgroundExecutor, not the main thread
                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();
                imageAnalysis.setAnalyzer(backgroundExecutor, this::analyzeFace);

                cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                        .build();

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture, imageAnalysis);

            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Camera binding failed", e);
                Thread.currentThread().interrupt();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    /**
     * FIX: safeUnbindCamera now reuses the already-resolved cameraProvider reference
     * instead of creating two separate getInstance() futures (old code leaked a future).
     */
    private void safeUnbindCamera() {
        if (cameraProvider != null) {
            try {
                cameraProvider.unbindAll();
            } catch (Exception e) {
                Log.e(TAG, "Unbind failed", e);
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Liveness challenge
    // ─────────────────────────────────────────────────────────────────────────

    private void setRandomChallenge() {
        LivenessChallenge[] challenges = LivenessChallenge.values();
        currentChallenge = challenges[new Random().nextInt(challenges.length)];
        challengeSatisfied = false;
        isBlinking = false;
        updateStatus(getInstructionText(currentChallenge));
    }

    private String getInstructionText(LivenessChallenge challenge) {
        if (challenge == null) return "Waiting...";
        switch (challenge) {
            case BLINK:
                return "Please Blink Your Eyes";
            case TURN_LEFT:
                return "Turn Your Face Left";
            case TURN_RIGHT:
                return "Turn Your Face Right";
            case TILT_UP:
                return "Look Up Slightly";
            case TILT_DOWN:
                return "Look Down Slightly";
            default:
                return "Follow the prompt";
        }
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    private void analyzeFace(@NonNull ImageProxy imageProxy) {
        // Gate: drop frame if busy or no challenge set
        if (isAnalyzing.get() || isProcessingLiveness || currentChallenge == null) {
            imageProxy.close();
            return;
        }

        isAnalyzing.set(true);

        android.media.Image mediaImage = imageProxy.getImage();
        if (mediaImage == null) {
            imageProxy.close();
            isAnalyzing.set(false);
            return;
        }

        InputImage image = InputImage.fromMediaImage(
                mediaImage, imageProxy.getImageInfo().getRotationDegrees());

        detector.process(image)
                .addOnSuccessListener(faces -> {
                    if (faces.isEmpty()) {
                        faceOverlay.setFaceDetected(false);
                        updateStatus("Position face in frame");
                        isBlinking = false;
                    } else {
                        faceOverlay.setFaceDetected(true);
                        // FIX: captureImage() is ONLY triggered from processChallenge()
                        // — removed the duplicate challengeSatisfied check that was here before.
                        processChallenge(faces.get(0));
                        if (!challengeSatisfied) {
                            updateStatus(getInstructionText(currentChallenge));
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Face detection failed", e))
                .addOnCompleteListener(task -> {
                    // Always release the image proxy and unlock the gate
                    imageProxy.close();
                    isAnalyzing.set(false);
                });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Frame analysis
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Evaluates the current liveness challenge against the detected face angles/probabilities.
     * FIX: captureImage() is triggered here ONLY — the duplicate call in analyzeFace() has been removed.
     */
    private void processChallenge(@NonNull Face face) {
        float headY = face.getHeadEulerAngleY();
        float headX = face.getHeadEulerAngleX();
        float leftEye = face.getLeftEyeOpenProbability() != null ? face.getLeftEyeOpenProbability() : 1.0f;
        float rightEye = face.getRightEyeOpenProbability() != null ? face.getRightEyeOpenProbability() : 1.0f;

        switch (currentChallenge) {
            case BLINK:
                if (leftEye < 0.25f && rightEye < 0.25f) isBlinking = true;
                if (isBlinking && leftEye > 0.6f && rightEye > 0.6f) challengeSatisfied = true;
                break;
            case TURN_LEFT:
                if (headY > 20) challengeSatisfied = true;
                break;
            case TURN_RIGHT:
                if (headY < -20) challengeSatisfied = true;
                break;
            case TILT_UP:
                if (headX > 15) challengeSatisfied = true;
                break;
            case TILT_DOWN:
                if (headX < -15) challengeSatisfied = true;
                break;
        }

        if (challengeSatisfied && !isProcessingLiveness) {
            isProcessingLiveness = true;
            updateStatus("Verified! Capturing...");
            captureImage(); // FIX: single, authoritative call site
        }
    }

    private void captureImage() {
        File photoFile = new File(getOutputDirectory(), System.currentTimeMillis() + "_photo.jpg");
        ImageCapture.OutputFileOptions outputOptions =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {

                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults results) {
                        if (isFinishing() || isDestroyed()) {
                            deleteQuietly(photoFile);
                            return;
                        }

                        backgroundExecutor.execute(() -> {
                            try {
                                BitmapFactory.Options bmpOptions = new BitmapFactory.Options();
                                bmpOptions.inSampleSize = 2;
                                Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath(), bmpOptions);

                                if (bitmap == null) {
                                    handleError("Failed to decode captured image");
                                    return;
                                }

                                int newWidth = 500;
                                int newHeight = (int) (bitmap.getHeight() * (newWidth / (float) bitmap.getWidth()));
                                // FIX: filter=true for better quality in face recognition
                                Bitmap scaled = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);

                                String base64 = convertImageToBase64(scaled);

                                // Recycle immediately after encoding
                                bitmap.recycle();
                                scaled.recycle();

                                // FIX: Delete temp file after reading — prevents disk accumulation
                                deleteQuietly(photoFile);

                                runOnUiThread(() -> {
                                    if (!isFinishing() && !isDestroyed()) {
                                        setLoadingVisible(true);
                                        prepareJsonAndSend(base64);
                                    }
                                });

                            } catch (Exception e) {
                                deleteQuietly(photoFile);
                                handleError("Image processing error: " + e.getMessage());
                            }
                        });
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        isProcessingLiveness = false;
                        setRandomChallenge();
                        handleError("Capture failed: " + exception.getMessage());
                    }
                });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Image capture & processing
    // ─────────────────────────────────────────────────────────────────────────

    private void prepareJsonAndSend(String base64Image) {
        try {
            JSONObject json = new JSONObject();
            json.put("collection_name", COLLECTION_NAME);
            json.put("image", base64Image);

            RequestBody requestBody = RequestBody.create(
                    MediaType.parse("application/json"), json.toString());

            sendImageToApi(requestBody);

        } catch (JSONException e) {
            Log.e(TAG, "JSON encoding error", e);
            isProcessingLiveness = false;
            setRandomChallenge();
            setLoadingVisible(false);
            handleError("Failed to prepare image data for server.");
        }
    }

    private String convertImageToBase64(@NonNull Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
        return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
    }

    private File getOutputDirectory() {
        File dir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "FaceLogin");
        if (!dir.exists() && !dir.mkdirs()) {
            Log.e(TAG, "Failed to create output directory");
        }
        return dir;
    }

    /**
     * Deletes a file silently; logs on failure.
     */
    private void deleteQuietly(@NonNull File file) {
        if (file.exists() && !file.delete()) {
            Log.w(TAG, "Could not delete temp file: " + file.getAbsolutePath());
        }
    }

    private void sendImageToApi(@NonNull RequestBody requestBody) {
        APIInterface service = APIClient.getInstance().getImage();
        service.FaceRecognitionApi(requestBody).enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(@NonNull Call<ResponseBody> call,
                                   @NonNull Response<ResponseBody> response) {
                if (isFinishing() || isDestroyed()) return;

                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String bodyStr = response.body().string();
                        JSONObject json = new JSONObject(bodyStr);

                        if (!json.has("data") || json.isNull("data")) {
                            isProcessingLiveness = false;
                            setRandomChallenge();
                            handleError("Server response is missing required data.");
                            return;
                        }

                        JSONObject dataObject = json.getJSONObject("data");
                        // FIX: authToken already read once in onCreate(); re-use field value
                        String token = "jwt " + authToken;
                        RequestBody faceBody = RequestBody.create(
                                MediaType.parse("application/json"), dataObject.toString());
                        callFaceDetailApi(token, faceBody);

                    } catch (IOException | JSONException e) {
                        // FIX: was throw new RuntimeException(e) → crashed the app; now routed to handleError
                        Log.e(TAG, "Response parse error", e);
                        handleError("Error parsing server response: " + e.getMessage());
                    }
                } else {
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) errorBody = response.errorBody().string();
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    handleError("Server Error " + response.code() + ": " + response.message()
                            + "\nDetails: " + errorBody);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                isProcessingLiveness = false;
                handleError("Network Error: " + t.getMessage());
            }
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // API calls
    // ─────────────────────────────────────────────────────────────────────────

    private void callFaceDetailApi(@NonNull String token, @NonNull RequestBody requestBody) {
        updateStatus("Verifying Identity...");
        setLoadingVisible(true);

        APIInterface service = APIClient.getInstance().getFace();
        service.FaceDetailApi(token, requestBody).enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(@NonNull Call<ResponseBody> call,
                                   @NonNull Response<ResponseBody> response) {
                // FIX: guard added (was missing in original) — prevents crash if activity is gone
                if (isFinishing() || isDestroyed()) return;

                setLoadingVisible(false);

                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String bodyStr = response.body().string();
                        JSONObject res = new JSONObject(bodyStr);
                        String recognizedId = res
                                .getJSONObject("data")
                                .getJSONObject("employee")
                                .getString("_id");

                        if (storedUserId != null && storedUserId.equals(recognizedId)) {
                            handleSuccess();
                        } else {
                            updateStatus("Unauthorized User");
                            isProcessingLiveness = false;
                            setRandomChallenge();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Face detail parse error", e);
                        handleError("Recognition Error: " + e.getMessage());
                    }
                } else {
                    handleError("Verification failed. Please try again.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                handleError("Network Error: " + t.getMessage());
            }
        });
    }

    private void handleSuccess() {
        safeUnbindCamera();
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Result handlers
    // ─────────────────────────────────────────────────────────────────────────

    private void handleError(String errorMessage) {
        isProcessingLiveness = false;
        challengeSatisfied = false;
        isAnalyzing.set(false);

        runOnUiThread(() -> {
            if (isFinishing() || isDestroyed()) return;

            setLoadingVisible(false);
            setRandomChallenge();

            String userMessage = errorMessage != null && errorMessage.contains("There are no faces in the image")
                    ? "No face detected. Please position your face clearly in the circle."
                    : errorMessage;

            new AlertDialog.Builder(this)
                    .setTitle("Verification Failed")
                    .setMessage(userMessage)
                    .setPositiveButton("Retry", (dialog, which) -> {
                        safeUnbindCamera();
                        startCamera();
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> finish())
                    .setCancelable(false)
                    .show();

            safeUnbindCamera();
        });
    }

    /**
     * Thread-safe status text update.
     * FIX: references R.id.statusText (field cached in onCreate) — old code used
     * R.id.CaptureText which didn't exist, causing a NullPointerException every frame.
     */
    private void updateStatus(String text) {
        runOnUiThread(() -> {
            if (statusText != null) statusText.setText(text);
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UI helpers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Shows/hides the loading overlay panel.
     */
    private void setLoadingVisible(boolean visible) {
        runOnUiThread(() -> {
            if (loadingPanel != null) {
                loadingPanel.setVisibility(visible ? View.VISIBLE : View.GONE);
            }
        });
    }

    /**
     * FIX: toggleScannerAnimation() previously tried to find R.id.scannerLine and
     * R.id.faceGuide which don't exist in the current XML layout, causing NPEs.
     * <p>
     * The method is retained as a no-op stub so call sites compile; re-implement
     * once the matching Views are added to the layout (or remove call sites).
     * <p>
     * To restore animation: add <View android:id="@+id/scannerLine"> and
     * <View android:id="@+id/faceGuide"> to activity_face_login.xml, then
     * un-comment the body below.
     */
    private void toggleScannerAnimation(boolean show) {
        // TODO: add R.id.scannerLine and R.id.faceGuide to the XML layout, then implement:
        //
        // View scannerLine = findViewById(R.id.scannerLine);
        // View faceGuide   = findViewById(R.id.faceGuide);
        // runOnUiThread(() -> {
        //     if (show) {
        //         scannerLine.setVisibility(View.VISIBLE);
        //         faceGuide.post(() -> {
        //             if (scannerAnimator == null) {
        //                 scannerAnimator = ObjectAnimator.ofFloat(
        //                         scannerLine, "translationY", 0f, faceGuide.getHeight());
        //                 scannerAnimator.setDuration(1500);
        //                 scannerAnimator.setRepeatCount(ValueAnimator.INFINITE);
        //                 scannerAnimator.setRepeatMode(ValueAnimator.REVERSE);
        //                 scannerAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        //             }
        //             if (!scannerAnimator.isRunning()) scannerAnimator.start();
        //         });
        //     } else {
        //         scannerLine.setVisibility(View.GONE);
        //         if (scannerAnimator != null) scannerAnimator.cancel();
        //     }
        // });
    }

    private enum LivenessChallenge {
        BLINK, TURN_LEFT, TURN_RIGHT, TILT_UP, TILT_DOWN
    }
}
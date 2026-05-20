package app.xedigital.ai.activity;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import app.xedigital.ai.utills.BioMetric;
import app.xedigital.ai.utills.FaceOverlayView;
import app.xedigital.ai.utills.LocationService;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FaceLoginActivity extends AppCompatActivity implements BioMetric.BiometricAuthListener {

    private static final String TAG = "FaceLoginActivity";
    private static final String COLLECTION_NAME = "consultedgeglobalpvtltd_5e970n";

    private final AtomicBoolean isAnalyzing = new AtomicBoolean(false);
    private final ExecutorService backgroundExecutor = Executors.newSingleThreadExecutor();

    private String authToken;
    private String storedUserId;

    private PreviewView previewView;
    private Preview preview;
    private ImageCapture imageCapture;
    private CameraSelector cameraSelector;
    private ProcessCameraProvider cameraProvider;

    private FaceDetector detector;
    private volatile boolean isProcessingLiveness = false;
    private boolean isBlinking = false;
    private boolean challengeSatisfied = false;
    private LivenessChallenge currentChallenge;

    private TextView statusText;
    private FaceOverlayView faceOverlay;
    private View loadingPanel;
    private ObjectAnimator scannerAnimator;
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isFinishing() || isDestroyed()) return;
                if (isGranted) {
                    setRandomChallenge();
                    startCamera();
                } else {
                    if (!allPermissionsGranted()) {
                        showPermissionDeniedAlert();
                    }
                }
            });
    private BioMetric bioMetric;
    private int attemptCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_login);

        bioMetric = new BioMetric(this, this, this);
        attemptCount = 0;

        statusText = findViewById(R.id.statusText);
        faceOverlay = findViewById(R.id.faceOverlay);
        loadingPanel = findViewById(R.id.loadingPanel);
        previewView = findViewById(R.id.viewFinder);
        ImageButton btnInfo = findViewById(R.id.btnInfo);

        btnInfo.setOnClickListener(v -> showLivenessInstructions(true));

        authToken = getIntent().getStringExtra("authToken");

        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        storedUserId = prefs.getString("userId", null);

        FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .build();
        detector = FaceDetection.getClient(options);

        checkInstructionsAndPermissions();
    }

    private void checkInstructionsAndPermissions() {
        if (isFinishing() || isDestroyed()) return;

        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        boolean instructionsSeen = prefs.getBoolean("instructionsSeen", false);

        if (!instructionsSeen) {
            showLivenessInstructions(false);
        } else {
            verifyCameraPermission();
        }
    }

    private void verifyCameraPermission() {
        if (allPermissionsGranted()) {
            setRandomChallenge();
            if (previewView != null) {
                previewView.post(() -> {
                    if (!isFinishing() && !isDestroyed()) {
                        startCamera();
                    }
                });
            }
        } else {
            if (previewView != null) {
                previewView.post(() -> {
                    if (!isFinishing() && !isDestroyed()) {
                        requestPermissionLauncher.launch(android.Manifest.permission.CAMERA);
                    }
                });
            }
        }
    }

    private boolean allPermissionsGranted() {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void showPermissionDeniedAlert() {
        if (isFinishing() || isDestroyed()) return;
        new AlertDialog.Builder(this)
                .setTitle("Camera Access Required")
                .setMessage("To login to your account using face recognition, you must grant camera access. Please enable it in settings.")
                .setPositiveButton("OK", (dialog, which) -> safelyExitToLogin())
                .setCancelable(false)
                .show();
    }

    private void showLivenessInstructions(boolean launchedFromButton) {
        if (isFinishing() || isDestroyed()) return;

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_liveness_instructions, null);

        AlertDialog infoDialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("I'm Ready", (dialog, which) -> {
                    dialog.dismiss();

                    if (!launchedFromButton) {
                        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                        prefs.edit().putBoolean("instructionsSeen", true).apply();
                    }

                    verifyCameraPermission();
                })
                .setCancelable(launchedFromButton)
                .create();

        infoDialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        backgroundExecutor.shutdownNow();
        if (detector != null) {
            detector.close();
        }
        if (scannerAnimator != null) {
            scannerAnimator.cancel();
            scannerAnimator = null;
        }
    }

    private void startCamera() {
        if (isFinishing() || isDestroyed()) return;

        ListenableFuture<ProcessCameraProvider> future = ProcessCameraProvider.getInstance(this);
        future.addListener(() -> {
            try {
                if (isFinishing() || isDestroyed()) return;

                cameraProvider = future.get();

                preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .setTargetResolution(new android.util.Size(480, 640))
                        .build();
                imageAnalysis.setAnalyzer(backgroundExecutor, this::analyzeFace);

                cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                        .build();

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture, imageAnalysis);
                toggleScannerAnimation(true);

            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Camera binding failed", e);
                Thread.currentThread().interrupt();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void safeUnbindCamera() {
        if (cameraProvider != null) {
            try {
                cameraProvider.unbindAll();
            } catch (Exception e) {
                Log.e(TAG, "Unbind failed", e);
            }
        }
    }

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
                        isBlinking = false;
                        updateStatus("Position your face within the circle");
                    } else {
                        faceOverlay.setFaceDetected(true);
                        if (!challengeSatisfied && !isProcessingLiveness) {
                            String instruction = getInstructionText(currentChallenge);
                            if (!statusText.getText().toString().equals(instruction)) {
                                updateStatus(instruction);
                            }
                        }
                        processChallenge(faces.get(0));
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Face detection failed", e))
                .addOnCompleteListener(task -> {
                    imageProxy.close();
                    isAnalyzing.set(false);
                });
    }

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
            captureImage();
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
                                Bitmap scaled = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);

                                String base64 = convertImageToBase64(scaled);

                                bitmap.recycle();
                                scaled.recycle();
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
                        String token = "jwt " + authToken;
                        RequestBody faceBody = RequestBody.create(
                                MediaType.parse("application/json"), dataObject.toString());
                        callFaceDetailApi(token, faceBody);

                    } catch (IOException | JSONException e) {
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

    private void callFaceDetailApi(@NonNull String token, @NonNull RequestBody requestBody) {
        updateStatus("Verifying Identity...");
        setLoadingVisible(true);

        APIInterface service = APIClient.getInstance().getFace();
        service.FaceDetailApi(token, requestBody).enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(@NonNull Call<ResponseBody> call,
                                   @NonNull Response<ResponseBody> response) {
                if (isFinishing() || isDestroyed()) return;

                setLoadingVisible(false);

                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String bodyStr = response.body().string();
                        JSONObject res = new JSONObject(bodyStr);
                        JSONObject dataObject = res.optJSONObject("data");

                        if (dataObject == null) {
                            handleError("Face details not found. Please try again.");
                            return;
                        }

                        JSONObject employee = dataObject.optJSONObject("employee");
                        if (employee == null) {
                            handleError("Employee profile missing in response.");
                            return;
                        }

                        String recognizedId = employee.optString("_id");

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
        attemptCount = 0;
        safeUnbindCamera();
        toggleScannerAnimation(false);

        Intent serviceIntent = new Intent(this, LocationService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void handleError(String errorMessage) {
        isProcessingLiveness = false;
        challengeSatisfied = false;
        isBlinking = false;
        isAnalyzing.set(false);
        setLoadingVisible(false);

        runOnUiThread(() -> {
            if (isFinishing() || isDestroyed()) return;

            setLoadingVisible(false);
            safeUnbindCamera();
            toggleScannerAnimation(false);

            attemptCount++;
            Log.d(TAG, "Current Attempt: " + attemptCount);

            if (attemptCount >= 3) {
                showFallbackLoginAlert();
            } else {
                String displayMsg = (errorMessage != null) ? errorMessage : "Verification failed";
                new AlertDialog.Builder(this)
                        .setTitle("Verification Failed")
                        .setMessage(displayMsg + "\n\nAttempt " + attemptCount + " of 3")
                        .setPositiveButton("Retry", (dialog, which) -> {
                            setRandomChallenge();
                            startCamera();
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> safelyExitToLogin())
                        .setCancelable(false)
                        .show();
            }
        });
    }

    private void showFallbackLoginAlert() {
        if (isFinishing() || isDestroyed()) return;

        runOnUiThread(() -> {
            safeUnbindCamera();
            toggleScannerAnimation(false);
            isAnalyzing.set(true);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Multiple Failed Attempts");
            builder.setMessage("Face recognition failed 3 times. Please choose an alternative login method:");
            builder.setCancelable(false);

            boolean canUseBiometrics = bioMetric != null && bioMetric.isBiometricAvailable();

            if (canUseBiometrics) {
                builder.setPositiveButton("Biometric Login", (dialog, which) -> {
                    if (bioMetric != null) bioMetric.authenticate(true);
                });
                builder.setNeutralButton("Manual Login", (dialog, which) -> navigateToManualLogin());
            } else {
                builder.setPositiveButton("Manual Login", (dialog, which) -> navigateToManualLogin());
            }

            builder.setNegativeButton("Cancel", (dialog, which) -> safelyExitToLogin());
            builder.show();
        });
    }

    private void navigateToManualLogin() {
        if (isFinishing() || isDestroyed()) return;

        safeUnbindCamera();
        toggleScannerAnimation(false);

        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra("isFallback", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void safelyExitToLogin() {
        if (isFinishing() || isDestroyed()) return;

        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra("isFallback", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private void updateStatus(String text) {
        runOnUiThread(() -> {
            if (statusText != null) statusText.setText(text);
        });
    }

    private void setLoadingVisible(boolean visible) {
        runOnUiThread(() -> {
            if (loadingPanel != null) {
                loadingPanel.setVisibility(visible ? View.VISIBLE : View.GONE);
            }
        });
    }

    @Override
    public void onAuthenticationSucceeded() {
        runOnUiThread(() -> {
            if (isFinishing() || isDestroyed()) return;
            updateStatus("Biometric Verified!");
            handleSuccess();
        });
    }

    @Override
    public void onAuthenticationError(int errorCode, CharSequence errString) {
        runOnUiThread(() -> {
            if (isFinishing() || isDestroyed()) return;
            showFallbackLoginAlert();
        });
    }

    @Override
    public void onAuthenticationFailed() {
        runOnUiThread(() -> {
            if (isFinishing() || isDestroyed()) return;
            updateStatus("Biometric authentication failed.");
        });
    }

    private void toggleScannerAnimation(boolean show) {
        View scannerLine = findViewById(R.id.scannerLine);
        View faceGuide = findViewById(R.id.faceGuide);
        if (scannerLine == null || faceGuide == null) return;

        runOnUiThread(() -> {
            if (show) {
                scannerLine.setVisibility(View.VISIBLE);
                faceGuide.post(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    if (scannerAnimator == null) {
                        scannerAnimator = ObjectAnimator.ofFloat(
                                scannerLine, "translationY", 0f, faceGuide.getHeight());
                        scannerAnimator.setDuration(1500);
                        scannerAnimator.setRepeatCount(ValueAnimator.INFINITE);
                        scannerAnimator.setRepeatMode(ValueAnimator.REVERSE);
                        scannerAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
                    }
                    if (!scannerAnimator.isRunning()) scannerAnimator.start();
                });
            } else {
                scannerLine.setVisibility(View.GONE);
                if (scannerAnimator != null) scannerAnimator.cancel();
            }
        });
    }

    private enum LivenessChallenge {
        BLINK, TURN_LEFT, TURN_RIGHT, TILT_UP, TILT_DOWN
    }
}
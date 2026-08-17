package app.xedigital.ai.activity;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import app.xedigital.ai.model.user.UserModelResponse;
import app.xedigital.ai.utills.BioMetric;
import app.xedigital.ai.utills.FaceOverlayView;
import app.xedigital.ai.utills.SecurePrefManager;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FaceLoginActivity extends AppCompatActivity implements BioMetric.BiometricAuthListener {

    private static final String TAG = "FaceLoginActivity";
    private final AtomicBoolean isAnalyzing = new AtomicBoolean(false);
    private final ExecutorService backgroundExecutor = Executors.newSingleThreadExecutor();
    private String authToken;
    private String storedUserId;
    private PreviewView previewView;
    private Preview preview;
    private ImageCapture imageCapture;
    private ImageAnalysis imageAnalysis;
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
    private String COLLECTION_NAME;
    private SecurePrefManager securePrefManager;
    private BioMetric bioMetric;
    private int attemptCount;

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
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

    // ─── Lifecycle ────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_login);

        securePrefManager = SecurePrefManager.getInstance(this);
        authToken = getIntent().getStringExtra("authToken");
        storedUserId = securePrefManager.getString("userId", null);
        bioMetric = new BioMetric(this, this, this);
        attemptCount = 0;

        statusText = findViewById(R.id.statusText);
        faceOverlay = findViewById(R.id.faceOverlay);
        loadingPanel = findViewById(R.id.loadingPanel);
        previewView = findViewById(R.id.viewFinder);
        ImageButton btnInfo = findViewById(R.id.btnInfo);

        if (btnInfo != null) {
            btnInfo.setOnClickListener(v -> showLivenessInstructions(true));
        }

        FaceDetectorOptions options = new FaceDetectorOptions.Builder().setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST).setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL).setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL).build();
        detector = FaceDetection.getClient(options);
        loadCollectionAndProceed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        backgroundExecutor.shutdownNow();
        if (detector != null) detector.close();
        if (scannerAnimator != null) {
            scannerAnimator.cancel();
            scannerAnimator = null;
        }
    }

    private void loadCollectionAndProceed() {
        COLLECTION_NAME = securePrefManager.getString("collection", null);
        if (COLLECTION_NAME != null && !COLLECTION_NAME.isEmpty()) {
            checkInstructionsAndPermissions();
        } else {
            setLoadingVisible(true);
            updateStatus("Fetching user configuration...");
            fetchUserData(storedUserId, authToken);
        }
    }

    private void fetchUserData(String userId, String authToken) {
        if (userId == null || authToken == null) {
            handleError("User session credentials missing.");
            return;
        }

        String authHeaderValue = "jwt " + authToken;
        Call<UserModelResponse> userCall = APIClient.getInstance().getUser().getUserData(userId, authHeaderValue);
        userCall.enqueue(new Callback<UserModelResponse>() {
            @Override
            public void onResponse(@NonNull Call<UserModelResponse> call, @NonNull Response<UserModelResponse> response) {
                if (isFinishing() || isDestroyed()) return;
                if (!response.isSuccessful() || response.body() == null) {
                    handleError("Failed to fetch user configuration.");
                    return;
                }

                UserModelResponse userDataResponse = response.body();
                if (!userDataResponse.isSuccess() || userDataResponse.getData() == null || userDataResponse.getData().getCompany() == null) {
                    handleError("Company metadata not found.");
                    return;
                }

                COLLECTION_NAME = userDataResponse.getData().getCompany().getCollectionName();
                if (securePrefManager != null) {
                    securePrefManager.putString("collection", COLLECTION_NAME);
                }
                setLoadingVisible(false);
                checkInstructionsAndPermissions();
            }

            @Override
            public void onFailure(@NonNull Call<UserModelResponse> call, @NonNull Throwable t) {
                handleError("Network connection failed during setup.");
            }
        });
    }

    // ─── Instructions & Permissions ───────────────────────────────────────────

    private void checkInstructionsAndPermissions() {
        if (isFinishing() || isDestroyed()) return;
        if (securePrefManager == null) {
            securePrefManager = SecurePrefManager.getInstance(this);
        }

        boolean instructionsSeen = securePrefManager.getBoolean("instructionsSeen", false);
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
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void showPermissionDeniedAlert() {
        if (isFinishing() || isDestroyed()) return;
        new AlertDialog.Builder(this).setTitle("Camera Access Required").setMessage("To login to your account using face recognition, " + "you must grant camera access. Please enable it in settings.").setPositiveButton("OK", (dialog, which) -> safelyExitToLogin()).setCancelable(false).show();
    }

    private void showLivenessInstructions(boolean launchedFromButton) {
        if (isFinishing() || isDestroyed()) return;

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_liveness_instructions, null);

        AlertDialog infoDialog = new AlertDialog.Builder(this).setView(dialogView).setPositiveButton("I'm Ready", (dialog, which) -> {
            dialog.dismiss();
            if (!launchedFromButton && securePrefManager != null) {
                securePrefManager.putBoolean("instructionsSeen", true);
            }
            verifyCameraPermission();
        }).setCancelable(launchedFromButton).create();

        infoDialog.show();
    }

    // ─── Camera ───────────────────────────────────────────────────────────────

    private void startCamera() {
        if (isFinishing() || isDestroyed()) return;
        ListenableFuture<ProcessCameraProvider> future = ProcessCameraProvider.getInstance(this);
        future.addListener(() -> {
            try {
                if (isFinishing() || isDestroyed()) return;
                cameraProvider = future.get();
                if (previewView == null || previewView.getSurfaceProvider() == null) {
                    Log.e(TAG, "PreviewView surface context is null.");
                    return;
                }

                preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());
                imageCapture = new ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build();
                imageAnalysis = new ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).setTargetResolution(new android.util.Size(480, 640)).build();
                imageAnalysis.setAnalyzer(backgroundExecutor, this::analyzeFace);
                cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_FRONT).build();

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

    // ─── Liveness Challenge ───────────────────────────────────────────────────

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

    // ─── Face Analysis ────────────────────────────────────────────────────────

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

        InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());

        detector.process(image).addOnSuccessListener(faces -> {
            if (faces.isEmpty()) {
                if (faceOverlay != null) faceOverlay.setFaceDetected(false);
                isBlinking = false;
                updateStatus("Position your face within the circle");
            } else {
                if (faceOverlay != null) faceOverlay.setFaceDetected(true);
                if (!challengeSatisfied && !isProcessingLiveness) {
                    String instruction = getInstructionText(currentChallenge);
                    if (statusText != null && statusText.getText() != null && !statusText.getText().toString().equals(instruction)) {
                        updateStatus(instruction);
                    }
                }
                processChallenge(faces.get(0));
            }
        }).addOnFailureListener(e -> Log.e(TAG, "Face detection failed", e)).addOnCompleteListener(task -> {
            imageProxy.close();
            isAnalyzing.set(false);
        });
    }

    private void processChallenge(@NonNull Face face) {
        if (isProcessingLiveness || challengeSatisfied) return;
        float headY = face.getHeadEulerAngleY();
        float headX = face.getHeadEulerAngleX();

        switch (currentChallenge) {
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
            runOnUiThread(() -> {
                if (cameraProvider != null && imageAnalysis != null) {
                    try {
                        cameraProvider.unbind(imageAnalysis);
                    } catch (Exception e) {
                        Log.e(TAG, "Error unbinding analysis", e);
                    }
                }
                updateStatus("Verified! Capturing...");
                captureImage();
            });
        }
    }

    // ─── Image Capture ────────────────────────────────────────────────────────

    private void captureImage() {
        if (imageCapture == null) {
            handleError("Camera capture pipeline is uninitialized.");
            return;
        }
        File photoFile = new File(getOutputDirectory(), System.currentTimeMillis() + "_photo.jpg");
        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this), new ImageCapture.OnImageSavedCallback() {
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
                            handleError("Failed to decode captured face image.");
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
                handleError("Face capture failed: " + exception.getMessage());
            }
        });
    }

    // ─── API Calls ────────────────────────────────────────────────────────────

    private void prepareJsonAndSend(String base64Image) {
        if (COLLECTION_NAME == null) {
            handleError("Company Record missing. Please re-login.");
            return;
        }
        try {
            JSONObject json = new JSONObject();
            json.put("collection_name", COLLECTION_NAME);
            json.put("image", base64Image);
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), json.toString());
            sendImageToApi(requestBody);
        } catch (JSONException e) {
            Log.e(TAG, "JSON encoding error", e);
            handleError("Failed to prepare image data for server validation.");
        }
    }

    private void sendImageToApi(@NonNull RequestBody requestBody) {
        if (authToken == null) {
            handleError("Session expired. Please sign in again.");
            return;
        }

        APIInterface service = APIClient.getInstance().getImage();
        service.FaceRecognitionApi(requestBody).enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (isFinishing() || isDestroyed()) return;
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String bodyStr = response.body().string();
                        JSONObject json = new JSONObject(bodyStr);
                        if (!json.has("data") || json.isNull("data")) {
                            handleError("Server response is missing required registration data.");
                            return;
                        }

                        JSONObject dataObject = json.getJSONObject("data");
                        String token = "jwt " + authToken;
                        RequestBody faceBody = RequestBody.create(MediaType.parse("application/json"), dataObject.toString());
                        callFaceDetailApi(token, faceBody);

                    } catch (IOException | JSONException e) {
                        handleError("Error parsing verification response: " + e.getMessage());
                    }
                } else {
                    handleError("Verification Server Error (" + response.code() + ")");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
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
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (isFinishing() || isDestroyed()) return;
                setLoadingVisible(false);
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String bodyStr = response.body().string();
                        JSONObject res = new JSONObject(bodyStr);
                        JSONObject dataObject = res.optJSONObject("data");

                        if (dataObject == null) {
                            handleError("Face profile records not found.");
                            return;
                        }

                        JSONObject employee = dataObject.optJSONObject("employee");
                        if (employee == null) {
                            handleError("Employee records mismatch.");
                            return;
                        }
                        String recognizedId = employee.optString("_id");
                        if (storedUserId != null && storedUserId.equals(recognizedId)) {
                            handleSuccess();
                        } else {
                            handleError("Unauthorized User Profile Detected.");
                        }

                    } catch (Exception e) {
                        Log.e(TAG, "Face detail parse error", e);
                        handleError("Data Parsing Error: " + e.getMessage());
                    }
                } else {
                    handleError("Verification failed. Please try again.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                handleError("Network Timeout: " + t.getMessage());
            }
        });
    }

    // ─── Result Handlers ──────────────────────────────────────────────────────

    private void handleSuccess() {
        attemptCount = 0;
        safeUnbindCamera();
        toggleScannerAnimation(false);
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void handleError(String errorMessage) {
        isProcessingLiveness = false;
        challengeSatisfied = false;
        isBlinking = false;
        isAnalyzing.set(true);

        runOnUiThread(() -> {
            if (isFinishing() || isDestroyed()) return;

            setLoadingVisible(false);
            safeUnbindCamera();
            toggleScannerAnimation(false);

            attemptCount++;
            Log.d(TAG, "Current Attempt Registered: " + attemptCount);

            if (attemptCount >= 3) {
                showFallbackLoginAlert();
            } else {
                String displayMsg = (errorMessage != null) ? errorMessage : "Verification failed";
                new AlertDialog.Builder(this).setTitle("Verification Failed").setMessage(displayMsg + "\n\nAttempt " + attemptCount + " of 3").setPositiveButton("Retry", (dialog, which) -> {
                    isAnalyzing.set(false);
                    resetAndRetryChallenge();
                }).setNegativeButton("Cancel", (dialog, which) -> safelyExitToLogin()).setCancelable(false).show();
            }
        });
    }

    private void resetAndRetryChallenge() {
        runOnUiThread(() -> {
            if (isFinishing() || isDestroyed()) return;
            setRandomChallenge();
            if (cameraProvider != null && imageAnalysis != null && cameraSelector != null && preview != null && imageCapture != null) {
                try {
                    cameraProvider.unbindAll();
                    cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture, imageAnalysis);
                } catch (Exception e) {
                    Log.e(TAG, "Rebinding camera pipeline failed", e);
                }
            }
        });
    }

    private void showFallbackLoginAlert() {
        if (isFinishing() || isDestroyed()) return;

        runOnUiThread(() -> {
            safeUnbindCamera();
            toggleScannerAnimation(false);
            isAnalyzing.set(true);
            boolean canUseDeviceSecurity = bioMetric != null && bioMetric.isDeviceSecurityAvailable();
            if (canUseDeviceSecurity) {
                new AlertDialog.Builder(this).setTitle("Multiple Failed Attempts").setMessage("Face recognition failed 3 times. Verify your identity " + "using your device PIN/Pattern/Biometrics or proceed to manual login:").setCancelable(false).setPositiveButton("Device Lock / Biometrics", (dialog, which) -> {
                    if (bioMetric != null) bioMetric.authenticate(true);
                }).setNeutralButton("Manual Login", (dialog, which) -> navigateToManualLogin()).setNegativeButton("Cancel", (dialog, which) -> safelyExitToLogin()).show();
            } else {
                navigateToManualLogin();
            }
        });
    }

    // ─── Navigation ───────────────────────────────────────────────────────────

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
        safeUnbindCamera();
        toggleScannerAnimation(false);
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra("isFallback", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    // ─── Biometric Callbacks ──────────────────────────────────────────────────

    @Override
    public void onAuthenticationSucceeded() {
        runOnUiThread(() -> {
            if (isFinishing() || isDestroyed()) return;
            updateStatus("Authentication Verified!");
            handleSuccess();
        });
    }

    @Override
    public void onAuthenticationError(int errorCode, CharSequence errString) {
        runOnUiThread(() -> {
            if (isFinishing() || isDestroyed()) return;
            navigateToManualLogin();
        });
    }

    @Override
    public void onAuthenticationFailed() {
        runOnUiThread(() -> {
            if (isFinishing() || isDestroyed()) return;
            updateStatus("Device authentication failed.");
        });
    }

    // ─── UI Helpers ───────────────────────────────────────────────────────────

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
                        scannerAnimator = ObjectAnimator.ofFloat(scannerLine, "translationY", 0f, faceGuide.getHeight());
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

    // ─── File Helpers ─────────────────────────────────────────────────────────

    private String convertImageToBase64(@NonNull Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
        return Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP);
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

    // ─── Enums ────────────────────────────────────────────────────────────────

    private enum LivenessChallenge {
        TURN_LEFT, TURN_RIGHT, TILT_UP, TILT_DOWN
    }
}
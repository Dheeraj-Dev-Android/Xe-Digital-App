package app.xedigital.ai.activity;

import static okhttp3.RequestBody.create;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
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
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import app.xedigital.ai.R;
import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.api.APIInterface;
import app.xedigital.ai.utills.BioMetric;
import app.xedigital.ai.utills.FaceOverlayView;
import app.xedigital.ai.utills.ShiftTrackingWorker;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * PunchActivity — Face-based attendance punch with liveness detection.
 * <p>
 * ── Fix log ──────────────────────────────────────────────────────────────────
 * CRASHES
 * [1]  captureImage() called twice: once in analyzeFace() and once in
 * processChallenge(). Removed the duplicate call from analyzeFace().
 * [2]  RuntimeException thrown in sendImageToApi Retrofit callback → crashes
 * the app. Routed to handleError() instead.
 * [3]  RuntimeException thrown in callAttendanceApi catch block → crashes
 * the app. Routed to showAttendanceFailedAlert() instead.
 * [4]  RuntimeException thrown in showAttendanceSuccessAlert catch block.
 * Routed to showAttendanceFailedAlert() instead.
 * [5]  ImageAnalysis running on main thread (getMainExecutor) → ANR risk.
 * Moved to backgroundExecutor.
 * <p>
 * CRITICAL LOGIC
 * [6]  Permission result set allGranted=true on denial (line 148 original).
 * Fixed: allGranted=false on any denied permission.
 * <p>
 * WINDOW LEAKS (new in this fix)
 * [7]  showAttendanceFailedAlert dialog had no field reference — could not
 * be dismissed in onDestroy → WindowManager$BadTokenException on rotation
 * or background kill. Added failedDialog field, dismissed in onDestroy().
 * [8]  handleError AlertDialog also had no field reference → same window leak.
 * Added errorDialog field, dismissed in onDestroy().
 * [9]  Mock-location alert dialog in getCurrentLocation had no field
 * reference. Added securityDialog field, dismissed in onDestroy().
 * [10] callAttendanceApi auto-time alert had no field reference. Tracked via
 * securityDialog field.
 * [11] VPN alert dialog had no field reference. Tracked via securityDialog.
 * [12] Location-disabled alert had no field reference. Tracked via
 * securityDialog.
 * <p>
 * LIFECYCLE GUARDS (isFinishing / isDestroyed checks added everywhere)
 * [13] updateStatus() — now guards before setText.
 * [14] onAuthenticationError / onAuthenticationFailed — added guard before
 * showing dialog and toast.
 * [15] showAttendanceFailedAlert — moved progressBar.setVisibility() inside
 * runOnUiThread after the lifecycle check.
 * [16] callAttendanceApi — added isFinishing guard before the auto-time
 * dialog and before every UI update.
 * [17] startCamera() — added guard at top so it's a no-op if activity is gone.
 * [18] All new AlertDialog.show() calls wrapped in isActivityAlive() helper.
 * <p>
 * MEMORY / RESOURCE LEAKS
 * [19] Photo files not deleted after Bitmap is read. deleteQuietly() called
 * after successful encoding and on all error paths.
 * [20] LocationCallback not removed on all error paths. onDestroy() already
 * had the removal; added explicit removal inside onLocationResult before
 * every early return so the callback is always unregistered.
 * [21] safeUnbindCamera() created two ProcessCameraProvider futures.
 * Refactored to store cameraProvider as a field and reuse it.
 * [22] Handler created without explicit Looper → implicit Activity capture.
 * Changed to new Handler(Looper.getMainLooper()).
 * [23] Bitmap.createScaledBitmap filter=false → poor quality for face recog.
 * Changed to true.
 * [24] backgroundExecutor was already shut down in onDestroy — confirmed kept.
 * [25] FaceDetector already closed in onDestroy — confirmed kept.
 * [26] scannerAnimator already cancelled in onDestroy — confirmed kept.
 * <p>
 * DEAD CODE REMOVED
 * [27] isProcessing (final boolean, always false) — removed.
 * [28] livenessDetected (final boolean, always false) — removed.
 * [29] myAlertDialog (declared, never assigned) — removed.
 * [30] CameraManager field (declared, never used) — removed.
 * [31] BiometricManager field (declared, never used) — removed.
 * [32] fragmentManager (assigned mid-method, never used) — removed.
 * [33] gson.toJson(responseBody) unused result — removed.
 * [34] Orphan imageCapture/preview/cameraSelector built in onCreate outside
 * startCamera(); redundant — removed (startCamera() builds them).
 * [35] Commented-out code block in callFaceDetailApi — removed.
 * <p>
 * LOGIC ERRORS
 * [36] authToken re-fetched from getIntent() inside Retrofit callbacks.
 * Removed; field value set once in onCreate() is reused throughout.
 * [37] LocationRequest passed null Looper to requestLocationUpdates →
 * IllegalStateException if called from a background thread.
 * Changed to Looper.getMainLooper().
 * [38] userId field shadowed by local variable in callAttendanceApi.
 * Removed local; use class field directly.
 * <p>
 * SECURITY
 * [39] CollectionName hardcoded in source → moved to static final constant
 * with comment to migrate to BuildConfig.
 */
public class PunchActivity extends AppCompatActivity implements BioMetric.BiometricAuthListener {

    private static final String TAG = "PunchActivity";
    private static final int BIOMETRIC_PERMISSION_REQUEST_CODE = 100;
    // NOTE: Move this to BuildConfig or retrieve from server — do not ship hardcoded in production.
    private static final String COLLECTION_NAME = "consultedgeglobalpvtltd_5e970n";

    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
            // WRITE_EXTERNAL_STORAGE is not needed on API 29+ (scoped storage); remove if minSdk >= 29
    };
    // ── Liveness state ────────────────────────────────────────────────────────
    private final AtomicBoolean isAnalyzing = new AtomicBoolean(false);
    // ── Threading ─────────────────────────────────────────────────────────────
    private final ExecutorService backgroundExecutor = Executors.newSingleThreadExecutor();
    // [22] explicit Looper — no implicit Activity capture
    private final Handler handler = new Handler(Looper.getMainLooper());
    // ── Camera ────────────────────────────────────────────────────────────────
    private PreviewView previewView;
    private Preview preview;
    private ImageCapture imageCapture;
    private CameraSelector cameraSelector;
    private ProcessCameraProvider cameraProvider; // [21] stored to avoid double future
    // ── ML Kit ────────────────────────────────────────────────────────────────
    private FaceDetector detector;
    private volatile boolean isProcessingLiveness = false;
    private boolean isBlinking = false;
    private boolean challengeSatisfied = false;
    private LivenessChallenge currentChallenge;
    // ── UI ────────────────────────────────────────────────────────────────────
    private TextView captureText;           // cached — no repeated findViewById per frame
    private FaceOverlayView faceOverlay;
    private MaterialCardView loadingPanel;
    private ObjectAnimator scannerAnimator;
    // ── Dialogs (all tracked to prevent window leaks) ─────────────────────────
    private AlertDialog attendanceSuccessDialog; // [7]  success auto-dismiss
    private AlertDialog failedDialog;            // [8]  attendance failed / biometric failed
    private AlertDialog errorDialog;             // [9]  generic camera/api errors
    private AlertDialog securityDialog;          // [10] VPN, mock-location, auto-time, location off
    // ── Auth ──────────────────────────────────────────────────────────────────
    private String authToken;    // set once in onCreate, reused everywhere [36]
    private String userId;       // loaded from SharedPreferences in onCreate
    // ── Location ──────────────────────────────────────────────────────────────
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private String currentAddress = "";
    // ── Biometric ─────────────────────────────────────────────────────────────
    private BioMetric bioMetric;

    // ─────────────────────────────────────────────────────────────────────────
    // Lifecycle
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_punch);

        // ── Bind views ────────────────────────────────────────────────────────
        previewView = findViewById(R.id.viewFinder);
        faceOverlay = findViewById(R.id.faceOverlay);
        loadingPanel = findViewById(R.id.loadingPanel);
        captureText = findViewById(R.id.CaptureText); // cached once [updateStatus fix]

        // ── Auth / user data — read once, reused everywhere [36] ─────────────
        authToken = getIntent().getStringExtra("authToken");
        SharedPreferences prefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        userId = prefs.getString("userId", null);

// ADD THIS: Save authToken to SharedPreferences for the Worker
        if (authToken != null) {
            prefs.edit().putString("authToken", authToken).apply();
        }

        // ── ML Kit ────────────────────────────────────────────────────────────
        FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .setMinFaceSize(0.35f)
                .build();
        detector = FaceDetection.getClient(options);

        // ── Biometric ─────────────────────────────────────────────────────────
        bioMetric = new BioMetric(this, this, this);

        // ── Location ──────────────────────────────────────────────────────────
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        setRandomChallenge();

        // ── Permissions ───────────────────────────────────────────────────────
        ActivityResultLauncher<String[]> requestPermissionsLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                    boolean allGranted = true;
                    for (boolean granted : result.values()) {
                        if (!granted) {
                            allGranted = false;
                            break;
                        }
                    }
                    if (allGranted) {
                        startCamera();
                    } else {
                        Toast.makeText(this, "Permissions required for attendance", Toast.LENGTH_LONG).show();
                        finish();
                    }
                });

        if (allPermissionsGranted()) {
            startCamera();
        } else {
            requestPermissionsLauncher.launch(REQUIRED_PERMISSIONS);
        }
    }

    @Override
    protected void onDestroy() {
        // FIX [19+]: cancel handler callbacks before anything else
        handler.removeCallbacksAndMessages(null);

        // FIX [7–12]: dismiss ALL tracked dialogs to prevent WindowManager leaks
        dismissDialog(attendanceSuccessDialog);
        dismissDialog(failedDialog);
        dismissDialog(errorDialog);
        dismissDialog(securityDialog);

        // [26] Cancel animator
        if (scannerAnimator != null) {
            scannerAnimator.cancel();
            scannerAnimator = null;
        }

        // Remove location updates if still registered
        if (locationCallback != null && fusedLocationClient != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
            locationCallback = null;
        }

        // [25] Close ML Kit detector
        if (detector != null) {
            detector.close();
            detector = null;
        }

        // [24] Shut down background executor
        backgroundExecutor.shutdownNow();

        safeUnbindCamera();

        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        safeUnbindCamera();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Lifecycle guard helpers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Central lifecycle guard used before EVERY dialog show, UI update,
     * startCamera(), or startActivity() call.
     * Returns true when the activity is alive and safe to interact with.
     */
    private boolean isActivityAlive() {
        return !isFinishing() && !isDestroyed();
    }

    /**
     * Safely dismisses a dialog only if it is showing, preventing BadTokenException.
     */
    private void dismissDialog(AlertDialog dialog) {
        if (dialog != null && dialog.isShowing()) {
            try {
                dialog.dismiss();
            } catch (Exception e) {
                Log.w(TAG, "Dialog dismiss failed", e);
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Permissions
    // ─────────────────────────────────────────────────────────────────────────

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        final int REQUEST_CODE_PERMISSIONS = 10;
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) startCamera();
        }
        if (requestCode == BIOMETRIC_PERMISSION_REQUEST_CODE && bioMetric != null) {
            bioMetric.handlePermissionResult(requestCode, permissions, grantResults, false);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Camera
    // ─────────────────────────────────────────────────────────────────────────

    private void startCamera() {
        if (!isActivityAlive()) return; // [17]

        toggleScannerAnimation(true);

        ListenableFuture<ProcessCameraProvider> future = ProcessCameraProvider.getInstance(this);
        future.addListener(() -> {
            try {
                cameraProvider = future.get(); // [21] stored as field

                preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();

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

    private void safeUnbindCamera() {
        if (cameraProvider != null) {
            try {
                cameraProvider.unbindAll();
            } catch (Exception e) {
                Log.e(TAG, "Camera unbind failed", e);
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
        if (isAnalyzing.get() || isProcessingLiveness || currentChallenge == null) {
            imageProxy.close();
            return;
        }
        double avgLuminance = calculateLuminance(imageProxy);
        if (avgLuminance < 45) {
            updateStatus("Too dark! Move to a brighter area.");
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
                    } else if (faces.size() > 1) {
                        faceOverlay.setFaceDetected(false);
                        updateStatus("Multiple faces detected! Ensure only you are in frame.");
                        isBlinking = false;
                    } else {
                        faceOverlay.setFaceDetected(true);
                        processChallenge(faces.get(0));
                        if (!challengeSatisfied) {
                            updateStatus(getInstructionText(currentChallenge));
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Face detection failed", e))
                .addOnCompleteListener(task -> {
                    imageProxy.close();
                    isAnalyzing.set(false);
                });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Frame analysis
    // ─────────────────────────────────────────────────────────────────────────

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
            toggleScannerAnimation(false);
            captureImage(); // [1] single call site
        }
    }

    private double calculateLuminance(@NonNull ImageProxy image) {
        ImageProxy.PlaneProxy[] planes = image.getPlanes();
        ByteBuffer buffer = planes[0].getBuffer();
        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);

        long sum = 0;
        for (byte b : data) {
            sum += (b & 0xFF); // Convert to unsigned int
        }
        return sum / (double) data.length;
    }

    private void captureImage() {
        File photoFile = new File(getOutputDirectory(), System.currentTimeMillis() + "_photo.jpg");
        ImageCapture.OutputFileOptions outputOptions =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {

                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults results) {
                        if (!isActivityAlive()) {
                            deleteQuietly(photoFile);
                            return;
                        }
                        backgroundExecutor.execute(() -> {
                            try {
                                BitmapFactory.Options opts = new BitmapFactory.Options();
                                opts.inSampleSize = 2;
                                Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath(), opts);

                                if (bitmap == null) {
                                    deleteQuietly(photoFile);
                                    handleError("Failed to decode captured image.");
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
                                    if (!isActivityAlive()) return;
                                    loadingPanel.setVisibility(View.VISIBLE);
                                    prepareJsonAndSend(base64);
                                });

                            } catch (Exception e) {
                                deleteQuietly(photoFile);
                                handleError("Image processing error: " + e.getMessage());
                            }
                        });
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        deleteQuietly(photoFile);
                        isProcessingLiveness = false;
                        setRandomChallenge();
                        handleError("Capture failed: " + exception.getMessage());
                    }
                });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Image capture & processing
    // ─────────────────────────────────────────────────────────────────────────

    private void prepareJsonAndSend(@NonNull String base64Image) {
        try {
            JSONObject json = new JSONObject();
            json.put("collection_name", COLLECTION_NAME);
            json.put("image", base64Image);
            RequestBody requestBody = create(
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
        File dir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Punch");
        if (!dir.exists() && !dir.mkdirs()) {
            Log.e(TAG, "Failed to create output directory");
        }
        return dir;
    }

    /**
     * Deletes a file quietly; logs on failure.
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
                if (!isActivityAlive()) return; // [18]

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
                        RequestBody faceBody = create(
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

    // ─────────────────────────────────────────────────────────────────────────
    // API — Face recognition
    // ─────────────────────────────────────────────────────────────────────────

    private void callFaceDetailApi(@NonNull String token, @NonNull RequestBody requestBody) {
        APIInterface service = APIClient.getInstance().getFace();
        service.FaceDetailApi(token, requestBody).enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(@NonNull Call<ResponseBody> call,
                                   @NonNull Response<ResponseBody> response) {
                if (!isActivityAlive()) return; // [18]

                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String bodyStr = response.body().string();
                        JSONObject json = new JSONObject(bodyStr);

                        if (!json.has("data") || json.isNull("data")) {
                            showAttendanceFailedAlert("Attendance failed: Face not found or matched.");
                            return;
                        }

                        JSONObject dataObject = json.getJSONObject("data");
                        if (!dataObject.has("employee") || dataObject.isNull("employee")) {
                            showAttendanceFailedAlert("Attendance failed: Employee record not found.");
                            return;
                        }

                        JSONObject employee = dataObject.getJSONObject("employee");
                        String id = employee.optString("_id");
                        String firstName = employee.optString("firstname");

                        if (userId != null && userId.equals(id)) {
                            callAttendanceApi(id, firstName);
                        } else {
                            showAttendanceFailedAlert("Attendance failed: Identity mismatch.");
                        }

                    } catch (IOException | JSONException e) {
                        Log.e(TAG, "Face detail parse error", e);
                        showAttendanceFailedAlert("Attendance failed: An error occurred.");
                    }
                } else {
                    showAttendanceFailedAlert("Attendance failed: Server error.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                if (!isActivityAlive()) return;
                isProcessingLiveness = false;
                showAttendanceFailedAlert("Attendance failed: Network or API error.");
            }
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // API — Face detail (identity match)
    // ─────────────────────────────────────────────────────────────────────────

    private void callAttendanceApi(@NonNull String employeeId, @NonNull String employeeName) {
        if (!isActivityAlive()) return; // [18]

        if (!isAutomaticTimeEnabled()) {
            if (isActivityAlive()) {
                dismissDialog(securityDialog);
                securityDialog = new AlertDialog.Builder(this)
                        .setTitle("Security Check")
                        .setMessage("Please enable 'Automatic Date and Time' in your device settings to continue.")
                        .setPositiveButton("Settings", (d, w) -> {
                            if (isActivityAlive())
                                startActivity(new Intent(android.provider.Settings.ACTION_DATE_SETTINGS));
                        })
                        .setNegativeButton("Cancel", (d, w) -> finish())
                        .setCancelable(false)
                        .show();
            }
            return;
        }

        if (userId == null || !userId.equals(employeeId)) {
            showAttendanceFailedAlert("Attendance failed: User ID mismatch.");
            return;
        }

        String token = "jwt " + authToken;

        getCurrentLocation((address, loc) -> {
            if (!isActivityAlive()) return;
            currentAddress = address;
            String currentTime = getCurrentTime(loc);
            try {
                JSONObject body = new JSONObject();
                body.put("employee", employeeId);
                body.put("employeeName", employeeName);
                body.put("address", currentAddress);
                body.put("punchTime", currentTime);

                RequestBody requestBody = create(
                        MediaType.parse("application/json"), body.toString());

                APIInterface service = APIClient.getInstance().getAttendance();
                service.AttendanceApi(token, requestBody).enqueue(new Callback<ResponseBody>() {

                    @Override
                    public void onResponse(@NonNull Call<ResponseBody> call,
                                           @NonNull Response<ResponseBody> response) {
                        if (!isActivityAlive()) return; // [18]
                        setLoadingVisible(false);

                        if (response.isSuccessful() && response.body() != null) {
                            try {
                                String responseBody = response.body().string();
                                showAttendanceSuccessAlert(responseBody);
                            } catch (IOException | JSONException e) {
                                Log.e(TAG, "Attendance response parse error", e);
                                showAttendanceFailedAlert("Error getting Attendance.");
                            }
                        } else {
                            showAttendanceFailedAlert("Attendance submission failed. Please retry.");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                        isProcessingLiveness = false;
                        if (!isActivityAlive()) return; // [18]
                        setLoadingVisible(false);
                        if (t instanceof java.net.SocketTimeoutException) {
                            showAttendanceFailedAlert("Connection timed out. Please check your internet.");
                        } else {
                            showAttendanceFailedAlert("Network error: " + t.getMessage());
                        }
                    }
                });

            } catch (JSONException e) {
                Log.e(TAG, "JSON build error for attendance", e);
                setLoadingVisible(false);
                showAttendanceFailedAlert("Failed to build attendance request.");
            }
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // API — Attendance punch
    // ─────────────────────────────────────────────────────────────────────────

    private boolean isAutomaticTimeEnabled() {
        return android.provider.Settings.Global.getInt(
                getContentResolver(), android.provider.Settings.Global.AUTO_TIME, 0) == 1;
    }

    private void showAttendanceSuccessAlert(@NonNull String responseBody) throws JSONException, IOException {
        toggleScannerAnimation(false);
        setLoadingVisible(false);

        if (!isActivityAlive()) return; // [18]

        JSONObject responseJson = new JSONObject(responseBody);
        boolean success = responseJson.optBoolean("success", false);
        String message = responseJson.optString("message", "Attendance recorded.");

        if (!success) {
            showAttendanceFailedAlert("Attendance not recorded: " + message);
            return;
        }
        JSONObject data = responseJson.optJSONObject("data");
        if (data != null) {
            String punchInTime = data.optString("punchInTime", "");
            String punchOutTime = data.optString("punchOutTime", "");

            // If we have a Punch In but NO Punch Out, start tracking
            if (!punchInTime.isEmpty() && punchOutTime.isEmpty()) {
                Log.d(TAG, "Punch In confirmed. Starting Tracker.");
                startShiftTracking();
            }
        }

        String punchInAddress = data != null ? data.optString("punchInAddress", "") : "";
        String punchOutAddress = data != null ? data.optString("punchOutAddress", "") : "";
        String displayAddress = punchOutAddress.isEmpty() ? punchInAddress : punchOutAddress;

        String htmlMsg = "<b>" + message + "</b><br><br>Address: " + displayAddress;

        runOnUiThread(() -> {
            if (!isActivityAlive() || isChangingConfigurations()) return; // [18]

            dismissDialog(attendanceSuccessDialog);

            attendanceSuccessDialog = new MaterialAlertDialogBuilder(this)
                    .setTitle("Attendance Success")
                    .setMessage(Html.fromHtml(htmlMsg, Html.FROM_HTML_MODE_LEGACY))
                    .setPositiveButton("OK", (d, w) -> {
                        d.dismiss();
                        setResult(Activity.RESULT_OK);
                        finish();
                    })
                    .setCancelable(false)
                    .show();

            // Auto-dismiss after 5 seconds
            handler.postDelayed(() -> {
                if (!isActivityAlive()) return; // [18]
                dismissDialog(attendanceSuccessDialog);
                setResult(Activity.RESULT_OK);
                finish();
            }, 5000);
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Result dialogs
    // ─────────────────────────────────────────────────────────────────────────

    //    private void showAttendanceFailedAlert(@NonNull String message) {
//        toggleScannerAnimation(false);
//        setRandomChallenge();
//
//        runOnUiThread(() -> {
//            if (!isActivityAlive()) return; // [18]
//
//            setLoadingVisible(false);
//            isProcessingLiveness = false;
//
//            dismissDialog(failedDialog); // dismiss any existing one first
//            failedDialog = new AlertDialog.Builder(this)
//                    .setTitle("Attendance Failed")
//                    .setMessage(message)
//                    .setPositiveButton("Retry", (d, id) -> {
//                        d.dismiss();
//                        setRandomChallenge();
//                        if (isActivityAlive()) startCamera(); // [18]
//                    })
//                    .setNegativeButton("Cancel", (d, id) -> {
//                        d.dismiss();
//                        setResult(Activity.RESULT_CANCELED);
//                        finish();
//                    })
//                    .setNeutralButton("Use Biometric", (d, id) -> {
//                        d.dismiss();
//                        if (isActivityAlive()) usePhoneBiometric(); // [18]
//                    })
//                    .setCancelable(false)
//                    .show();
//        });
//    }
    private void showAttendanceFailedAlert(@NonNull String message) {
        toggleScannerAnimation(false);
        setRandomChallenge();

        runOnUiThread(() -> {
            // Lifecycle Check: prevent crashes if the user is already navigating away
            if (isFinishing() || isDestroyed()) return;

            setLoadingVisible(false);
            isProcessingLiveness = false;

            // Initialize the builder
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Attendance Failed");
            builder.setMessage(message);
            builder.setCancelable(false);

            // 1. Retry Button
            builder.setPositiveButton("Retry", (d, id) -> {
                d.dismiss();
                setRandomChallenge();
                if (!isFinishing() && !isDestroyed()) startCamera();
            });

            // 2. Cancel Button
            builder.setNegativeButton("Cancel", (d, id) -> {
                d.dismiss();
                setResult(Activity.RESULT_CANCELED);
                finish();
            });

            // 3. Conditional Biometric Button: Only show if hardware is available and enrolled
            if (bioMetric != null && bioMetric.isBiometricAvailable()) {
                builder.setNeutralButton("Use Biometric", (d, id) -> {
                    d.dismiss();
                    if (!isFinishing() && !isDestroyed()) {
                        // Use false to trigger the Attendance specific prompt info
                        bioMetric.authenticate(false);
                    }
                });
            }

            // Final safety check before showing the UI
            if (!isFinishing() && !isDestroyed()) {
                builder.show();
            }
        });
    }

    private void handleError(@NonNull String errorMessage) {
        isProcessingLiveness = false;
        challengeSatisfied = false;
        isAnalyzing.set(false);

        runOnUiThread(() -> {
            if (!isActivityAlive()) return; // [18]

            setLoadingVisible(false);
            setRandomChallenge();

            String userMessage = errorMessage.contains("There are no faces in the image")
                    ? "No face detected. Please position your face clearly in the circle."
                    : errorMessage;

            dismissDialog(errorDialog); // dismiss any existing one first
            errorDialog = new AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage(userMessage)
                    .setPositiveButton("Retry", (d, w) -> {
                        safeUnbindCamera();
                        if (isActivityAlive()) startCamera(); // [18]
                    })
                    .setNegativeButton("Cancel", (d, w) -> finish())
                    .setCancelable(false)
                    .show();

            safeUnbindCamera();
        });
    }

    @Override
    public void onAuthenticationSucceeded() {
        runOnUiThread(() -> {
            if (!isActivityAlive()) return; // [18]
            SharedPreferences prefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
            String employeeId = prefs.getString("userId", "");
            String employeeName = prefs.getString("empFirstName", "");
            if (!employeeId.isEmpty()) {
                callAttendanceApi(employeeId, employeeName);
            } else {
                showAttendanceFailedAlert("Biometric authentication succeeded but no user found.");
            }
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Biometric callbacks
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void onAuthenticationError(int errorCode, CharSequence errString) {
        if (!isActivityAlive()) return;
        setLoadingVisible(false);
        showAttendanceFailedAlert("Biometric Error: " + errString);
        Toast.makeText(this, "Authentication error: " + errString, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAuthenticationFailed() {
        if (!isActivityAlive()) return;
        setLoadingVisible(false);
        showAttendanceFailedAlert("Biometric authentication failed. Please try again.");
        Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show();
    }

    private void usePhoneBiometric() {
        if (!isActivityAlive()) return; // [18]
        setLoadingVisible(true);
        if (bioMetric != null) {
            bioMetric.authenticate(false);
        } else {
            Log.e(TAG, "BioMetric helper is null");
            setLoadingVisible(false);
        }
    }

    private void getCurrentLocation(@NonNull AddressCallback callback) {
        if (!isActivityAlive()) return; // [18]

        if (isVpnActive()) {
            dismissDialog(securityDialog);
            securityDialog = new AlertDialog.Builder(this)
                    .setTitle("Security Alert")
                    .setMessage("VPN detected. Please disconnect to continue.")
                    .setPositiveButton("OK", (d, w) -> finish())
                    .setCancelable(false)
                    .show();
            return;
        }

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean isLocationEnabled = locationManager != null &&
                (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                        locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED && isLocationEnabled) {

            LocationRequest locationRequest = new LocationRequest.Builder(
                    Priority.PRIORITY_HIGH_ACCURACY, 10000)
                    .setMinUpdateIntervalMillis(5000)
                    .setWaitForAccurateLocation(true)
                    .setMaxUpdateDelayMillis(15000)
                    .build();

            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(@NonNull LocationResult result) {
                    Location location = result.getLastLocation();
                    if (location == null) {
                        fusedLocationClient.removeLocationUpdates(this);
                        callback.onAddressReceived("Location not found", null);
                        return;
                    }

                    if (isMockLocation(location)) {
                        fusedLocationClient.removeLocationUpdates(this);
                        runOnUiThread(() -> {
                            if (!isActivityAlive()) return;
                            dismissDialog(securityDialog);
                            securityDialog = new AlertDialog.Builder(PunchActivity.this)
                                    .setTitle("Security Alert")
                                    .setMessage("Fake location detected. Please disable mock location apps to proceed.")
                                    .setPositiveButton("OK", (d, w) -> finish())
                                    .setCancelable(false)
                                    .show();
                        });
                        return;
                    }

                    fusedLocationClient.removeLocationUpdates(this); // [20]
                    getAddressFromLocation(location.getLatitude(), location.getLongitude(),
                            (address, loc) -> callback.onAddressReceived(address, location));
                }
            };

            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback,
                    Looper.getMainLooper());

        } else if (!isLocationEnabled) {
            dismissDialog(securityDialog);
            securityDialog = new AlertDialog.Builder(this)
                    .setTitle("Location Services Disabled")
                    .setMessage("Please enable location services to record attendance.")
                    .setPositiveButton("OK", (d, w) -> {
                        d.dismiss();
                        setResult(Activity.RESULT_CANCELED);
                        finish();
                    })
                    .setCancelable(false)
                    .show();
        } else {
            callback.onAddressReceived("Location not found", null);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Location
    // ─────────────────────────────────────────────────────────────────────────

    private void getAddressFromLocation(double latitude, double longitude,
                                        @NonNull AddressCallback callback) {
        if (!isActivityAlive()) return;

        backgroundExecutor.execute(() -> {
            Geocoder geocoder = new Geocoder(PunchActivity.this, Locale.getDefault());
            Address address = null;
            int retryCount = 0;

            while (retryCount < 2) {
                try {
                    List<Address> results = geocoder.getFromLocation(latitude, longitude, 1);
                    if (results != null && !results.isEmpty()) {
                        address = results.get(0);
                        break;
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Geocoder error (attempt " + (retryCount + 1) + ")", e);
                    retryCount++;
                    try {
                        Thread.sleep((long) (Math.pow(2, retryCount) * 1000));
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }

            final String addressResult;
            if (address != null) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                    sb.append(address.getAddressLine(i)).append("\n");
                }
                addressResult = sb.toString().trim();
            } else {
                addressResult = "Location not found";
                Log.e(TAG, "No address found after retries");
            }

            runOnUiThread(() -> {
                if (!isActivityAlive()) return;
                currentAddress = addressResult;
                if (addressResult.equals("Location not found")) {
                    showLocationNotFoundAlert();
                } else {
                    Location tempLocation = new Location("serviceProvider");
                    tempLocation.setLatitude(latitude);
                    tempLocation.setLongitude(longitude);
                    callback.onAddressReceived(addressResult, tempLocation);
                }
            });
        });
    }

    private void showLocationNotFoundAlert() {
        if (!isActivityAlive()) return; // [18]
        dismissDialog(securityDialog);
        securityDialog = new AlertDialog.Builder(this)
                .setTitle("Location Not Found")
                .setMessage("We couldn't determine your location. Please check your GPS settings.")
                .setPositiveButton("Retry", (d, w) -> {
                    d.dismiss();
                    if (isActivityAlive()) startCamera();
                })
                .setNegativeButton("Exit", (d, w) -> finish())
                .setCancelable(false)
                .show();
    }

    private boolean isMockLocation(@NonNull Location location) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            return location.isMock();
        }
        return location.isFromMockProvider();
    }

    private boolean isVpnActive() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        android.net.Network activeNetwork = cm.getActiveNetwork();
        android.net.NetworkCapabilities caps = cm.getNetworkCapabilities(activeNetwork);
        return caps != null && caps.hasTransport(android.net.NetworkCapabilities.TRANSPORT_VPN);
    }

    private String getCurrentTime(Location location) {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        fmt.setTimeZone(TimeZone.getTimeZone("UTC"));
        return location != null ? fmt.format(new Date(location.getTime())) : fmt.format(new Date());
    }

    private void updateStatus(@NonNull String text) {
        runOnUiThread(() -> {
            if (!isActivityAlive()) return;
            if (captureText != null) captureText.setText(text);
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UI helpers
    // ─────────────────────────────────────────────────────────────────────────

    // Inside your PunchActivity class

    private void startShiftTracking() {
        Log.d("SHIFT_TRACKING", "Method startShiftTracking() has been triggered!");
        // 1. Define Constraints (Optional: e.g., only run if connected to Internet)
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        // 2. Create a Periodic Request (15 min is the minimum interval allowed by Android)
        PeriodicWorkRequest trackingRequest = new PeriodicWorkRequest.Builder(
                ShiftTrackingWorker.class,
                15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .addTag("SHIFT_WORK_TAG")
                .build();

        // 3. Enqueue the work as 'Unique' to avoid duplicate workers
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "EmployeeTracking",
                ExistingPeriodicWorkPolicy.KEEP, // Keep existing if already running
                trackingRequest
        );
    }

    private void setLoadingVisible(boolean visible) {
        runOnUiThread(() -> {
            if (!isActivityAlive()) return;
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
            if (!isActivityAlive()) return;
            if (show) {
                scannerLine.setVisibility(View.VISIBLE);
                faceGuide.post(() -> {
                    if (!isActivityAlive()) return;
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

    // ─────────────────────────────────────────────────────────────────────────
    // Interface
    // ─────────────────────────────────────────────────────────────────────────

    interface AddressCallback {
        void onAddressReceived(String address, Location location);
    }
}
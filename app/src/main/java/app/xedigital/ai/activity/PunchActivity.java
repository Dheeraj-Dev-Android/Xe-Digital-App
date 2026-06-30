package app.xedigital.ai.activity;

import static okhttp3.RequestBody.create;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import java.util.concurrent.atomic.AtomicBoolean;

import app.xedigital.ai.R;
import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.api.APIInterface;
import app.xedigital.ai.utills.BioMetric;
import app.xedigital.ai.utills.FaceOverlayView;
import app.xedigital.ai.utills.SecurePrefManager;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PunchActivity extends AppCompatActivity implements BioMetric.BiometricAuthListener {

    private static final String TAG = "PunchActivity";
    private static final int BIOMETRIC_PERMISSION_REQUEST_CODE = 100;
    private static final String COLLECTION_NAME = "consultedgeglobalpvtltd_5e970n";

    private static final String[] REQUIRED_PERMISSIONS = {Manifest.permission.CAMERA, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
    private final AtomicBoolean isAnalyzing = new AtomicBoolean(false);
    private final ExecutorService backgroundExecutor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private volatile ActivityState currentState = ActivityState.IDLE;
    private PreviewView previewView;
    private FaceOverlayView faceOverlay;
    private MaterialCardView loadingPanel;
    private TextView captureText;
    private ImageCapture imageCapture;
    private ProcessCameraProvider cameraProvider;
    private FaceDetector detector;
    private boolean isBlinking = false;
    private boolean challengeSatisfied = false;
    private LivenessChallenge currentChallenge;
    private ObjectAnimator scannerAnimator;
    private final ActivityResultLauncher<String[]> requestPermissionsLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
        boolean isGranted = true;
        for (boolean granted : result.values()) {
            if (!granted) {
                isGranted = false;
                break;
            }
        }
        if (isGranted) {
            initiateVerificationFlow();
        } else {
            showPermissionDeniedAlert();
        }
    });
    private AlertDialog attendanceSuccessDialog;
    private AlertDialog failedDialog;
    private AlertDialog errorDialog;
    private AlertDialog securityDialog;

    private String authToken;
    private String userId;
    private String employeeFirstName;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private String currentAddress = "";
    private BioMetric bioMetric;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_punch);

        previewView = findViewById(R.id.viewFinder);
        faceOverlay = findViewById(R.id.faceOverlay);
        loadingPanel = findViewById(R.id.loadingPanel);
        captureText = findViewById(R.id.CaptureText);

        authToken = getIntent().getStringExtra("authToken");
        SecurePrefManager prefManager = SecurePrefManager.getInstance(this);
        userId = prefManager.getString("userId", null);
        employeeFirstName = prefManager.getString("empFirstName", "");

        if (authToken != null) {
            prefManager.putString("authToken", authToken);
        }

        FaceDetectorOptions options = new FaceDetectorOptions.Builder().setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST).setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL).setMinFaceSize(0.35f).build();
        detector = FaceDetection.getClient(options);

        bioMetric = new BioMetric(this, this, this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        boolean instructionsSeen = prefManager.getBoolean("instructionsSeenAttendance", false);

        if (!instructionsSeen) {
            showLivenessInstructions();
        } else {
            if (allPermissionsGranted()) {
                initiateVerificationFlow();
            } else {
                requestPermissionsLauncher.launch(REQUIRED_PERMISSIONS);
            }
        }
    }

    private void initiateVerificationFlow() {
        currentState = ActivityState.SCANNING;
        setRandomChallenge();
        startCamera();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);

        dismissDialog(attendanceSuccessDialog);
        dismissDialog(failedDialog);
        dismissDialog(errorDialog);
        dismissDialog(securityDialog);

        if (scannerAnimator != null) {
            scannerAnimator.cancel();
            scannerAnimator = null;
        }
        if (locationCallback != null && fusedLocationClient != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
            locationCallback = null;
        }
        if (detector != null) {
            detector.close();
            detector = null;
        }
        backgroundExecutor.shutdownNow();
        safeUnbindCamera();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        safeUnbindCamera();
    }

    private void showLivenessInstructions() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_liveness_instructions, null);

        AlertDialog infoDialog = new AlertDialog.Builder(this).setView(dialogView).setPositiveButton("I'm Ready", (dialog, which) -> {
            SecurePrefManager prefManager = SecurePrefManager.getInstance(this);
            prefManager.putBoolean("instructionsSeenAttendance", true);

            if (allPermissionsGranted()) {
                initiateVerificationFlow();
            } else {
                requestPermissionsLauncher.launch(REQUIRED_PERMISSIONS);
            }
        }).setCancelable(false).create();

        infoDialog.show();
    }

    private void showPermissionDeniedAlert() {
        new AlertDialog.Builder(this).setTitle("Camera Access Required").setMessage("To login to your account using face recognition, you must grant camera access. Please enable it in settings.").setPositiveButton("OK", (dialog, which) -> finish()).setCancelable(false).show();
    }

    private boolean isActivityAlive() {
        return !isFinishing() && !isDestroyed();
    }

    private void dismissDialog(AlertDialog dialog) {
        if (dialog != null && dialog.isShowing()) {
            try {
                dialog.dismiss();
            } catch (Exception e) {
                Log.w(TAG, "Dialog dismiss failed", e);
            }
        }
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == BIOMETRIC_PERMISSION_REQUEST_CODE && bioMetric != null) {
            bioMetric.handlePermissionResult(requestCode, permissions, grantResults, false);
        }
    }

    private void startCamera() {
        if (!isActivityAlive() || currentState != ActivityState.SCANNING) return;

        toggleScannerAnimation(true);

        ListenableFuture<ProcessCameraProvider> future = ProcessCameraProvider.getInstance(this);
        future.addListener(() -> {
            try {
                if (!isActivityAlive() || currentState != ActivityState.SCANNING) return;

                cameraProvider = future.get();

                Preview previewComp = new Preview.Builder().build();
                previewComp.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build();

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();
                imageAnalysis.setAnalyzer(backgroundExecutor, this::analyzeFace);

                CameraSelector cameraSelectorComp = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_FRONT).build();

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelectorComp, previewComp, imageCapture, imageAnalysis);

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
        if (isAnalyzing.get() || currentState != ActivityState.SCANNING || currentChallenge == null) {
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

        InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
        detector.process(image).addOnSuccessListener(faces -> {
            if (!isActivityAlive() || currentState != ActivityState.SCANNING) return;

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
        }).addOnFailureListener(e -> Log.e(TAG, "Face detection failed", e)).addOnCompleteListener(task -> {
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

        if (challengeSatisfied && currentState == ActivityState.SCANNING) {
            currentState = ActivityState.PROCESSING_LIVENESS;
            updateStatus("Verified! Capturing...");
            toggleScannerAnimation(false);
            captureImage();
        }
    }

    private double calculateLuminance(@NonNull ImageProxy image) {
        ImageProxy.PlaneProxy[] planes = image.getPlanes();
        ByteBuffer buffer = planes[0].getBuffer();
        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);

        long sum = 0;
        for (byte b : data) {
            sum += (b & 0xFF);
        }
        return sum / (double) data.length;
    }

    private void captureImage() {
        File photoFile = new File(getOutputDirectory(), System.currentTimeMillis() + "_photo.jpg");
        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this), new ImageCapture.OnImageSavedCallback() {
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
                            currentState = ActivityState.UPLOADING;
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
                handleError("Capture failed: " + exception.getMessage());
            }
        });
    }

    private void prepareJsonAndSend(@NonNull String base64Image) {
        try {
            JSONObject json = new JSONObject();
            json.put("collection_name", COLLECTION_NAME);
            json.put("image", base64Image);
            RequestBody requestBody = create(MediaType.parse("application/json"), json.toString());
            sendImageToApi(requestBody);
        } catch (JSONException e) {
            Log.e(TAG, "JSON encoding error", e);
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

    private void deleteQuietly(@NonNull File file) {
        if (file.exists() && !file.delete()) {
            Log.w(TAG, "Could not delete temp file: " + file.getAbsolutePath());
        }
    }

    private void sendImageToApi(@NonNull RequestBody requestBody) {
        APIInterface service = APIClient.getInstance().getImage();
        service.FaceRecognitionApi(requestBody).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (!isActivityAlive() || currentState != ActivityState.UPLOADING) return;

                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String bodyStr = response.body().string();
                        JSONObject json = new JSONObject(bodyStr);

                        if (!json.has("data") || json.isNull("data")) {
                            handleError("Server response is missing required data.");
                            return;
                        }

                        JSONObject dataObject = json.getJSONObject("data");
                        String token = "jwt " + authToken;
                        RequestBody faceBody = create(MediaType.parse("application/json"), dataObject.toString());
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
                    handleError("Server Error " + response.code() + ": " + response.message() + "\nDetails: " + errorBody);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                if (!isActivityAlive() || currentState != ActivityState.UPLOADING) return;
                handleError("Network Error: " + t.getMessage());
            }
        });
    }

    private void callFaceDetailApi(@NonNull String token, @NonNull RequestBody requestBody) {
        APIInterface service = APIClient.getInstance().getFace();
        service.FaceDetailApi(token, requestBody).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (!isActivityAlive() || currentState != ActivityState.UPLOADING) return;

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
                if (!isActivityAlive() || currentState != ActivityState.UPLOADING) return;
                showAttendanceFailedAlert("Attendance failed: Network or API error.");
            }
        });
    }

    private void callAttendanceApi(@NonNull String employeeId, @NonNull String employeeName) {
        if (!isActivityAlive()) return;

        if (!isAutomaticTimeEnabled()) {
            dismissDialog(securityDialog);
            securityDialog = new AlertDialog.Builder(this).setTitle("Security Check").setMessage("Please enable 'Automatic Date and Time' in your device settings to continue.").setPositiveButton("Settings", (d, w) -> startActivity(new Intent(android.provider.Settings.ACTION_DATE_SETTINGS))).setNegativeButton("Cancel", (d, w) -> finish()).setCancelable(false).show();
            return;
        }

        if (userId == null || !userId.equals(employeeId)) {
            showAttendanceFailedAlert("Attendance failed: User ID mismatch.");
            return;
        }

        String token = "jwt " + authToken;

        getCurrentLocation((address, loc) -> {
            if (!isActivityAlive() || currentState != ActivityState.UPLOADING) return;
            currentAddress = address;
            String currentTime = getCurrentTime(loc);
            try {
                JSONObject body = new JSONObject();
                body.put("employee", employeeId);
                body.put("employeeName", employeeName);
                body.put("address", currentAddress);
                body.put("punchTime", currentTime);

                RequestBody requestBody = create(MediaType.parse("application/json"), body.toString());

                APIInterface service = APIClient.getInstance().getAttendance();
                service.AttendanceApi(token, requestBody).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                        if (!isActivityAlive() || currentState != ActivityState.UPLOADING) return;
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
                        if (!isActivityAlive() || currentState != ActivityState.UPLOADING) return;
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

    private boolean isAutomaticTimeEnabled() {
        return android.provider.Settings.Global.getInt(getContentResolver(), android.provider.Settings.Global.AUTO_TIME, 0) == 1;
    }

    private void showAttendanceSuccessAlert(@NonNull String responseBody) throws JSONException, IOException {
        toggleScannerAnimation(false);
        setLoadingVisible(false);

        if (!isActivityAlive()) return;

        JSONObject responseJson = new JSONObject(responseBody);
        boolean success = responseJson.optBoolean("success", false);
        String message = responseJson.optString("message", "Attendance recorded.");

        if (!success) {
            showAttendanceFailedAlert("Attendance not recorded: " + message);
            return;
        }

        JSONObject data = responseJson.optJSONObject("data");
        String punchInTime = data != null ? data.optString("punchInTime", "") : "";
        String punchOutTime = data != null ? data.optString("punchOutTime", "") : "";

        String punchInAddress = data != null ? data.optString("punchInAddress", "") : "";
        String punchOutAddress = data != null ? data.optString("punchOutAddress", "") : "";
        String displayAddress = punchOutAddress.isEmpty() ? punchInAddress : punchOutAddress;

        String htmlMsg = "<b>" + message + "</b><br><br>Address: " + displayAddress;

        dismissDialog(attendanceSuccessDialog);

        attendanceSuccessDialog = new MaterialAlertDialogBuilder(this).setTitle("Attendance Success").setMessage(Html.fromHtml(htmlMsg, Html.FROM_HTML_MODE_LEGACY)).setCancelable(false).setPositiveButton("OK", (d, w) -> {
            d.dismiss();

            if (!punchInTime.isEmpty() && punchOutTime.isEmpty()) {
                boolean hasBackgroundLoc = true;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    hasBackgroundLoc = androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_BACKGROUND_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED;
                }

                if (!hasBackgroundLoc) {
                    Toast.makeText(this, "Enable 'Allow all the time' for Precise Location.", Toast.LENGTH_LONG).show();
                    try {
                        androidx.navigation.Navigation.findNavController(this, R.id.nav_host_fragment_content_main).navigate(R.id.nav_permission);
                    } catch (Exception e) {
                        Log.e(TAG, "Navigation failed: " + e.getMessage());
                    }
                    finish();
                    return;
                }
            }
            setResult(Activity.RESULT_OK);
            finish();
        }).show();

        handler.postDelayed(() -> {
            if (!isActivityAlive()) return;
            if (attendanceSuccessDialog != null && attendanceSuccessDialog.isShowing()) {
                attendanceSuccessDialog.getButton(android.content.DialogInterface.BUTTON_POSITIVE).performClick();
            }
        }, 5000);
    }

    private void showAttendanceFailedAlert(@NonNull String message) {
        toggleScannerAnimation(false);
        setLoadingVisible(false);

        if (!isActivityAlive()) return;

        dismissDialog(failedDialog);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Attendance Failed");
        builder.setMessage(message);
        builder.setCancelable(false);

        builder.setPositiveButton("Retry", (d, id) -> {
            d.dismiss();
            safeUnbindCamera();
            initiateVerificationFlow();
        });

        builder.setNegativeButton("Cancel", (d, id) -> {
            d.dismiss();
            setResult(Activity.RESULT_CANCELED);
            finish();
        });

        if (bioMetric != null && bioMetric.isBiometricAvailable()) {
            builder.setNeutralButton("Use Biometric", (d, id) -> {
                d.dismiss();
                currentState = ActivityState.BIOMETRIC_FALLBACK;
                bioMetric.authenticate(false);
            });
        }

        failedDialog = builder.create();
        failedDialog.show();
    }

    private void handleError(@NonNull String errorMessage) {
        isAnalyzing.set(false);
        setLoadingVisible(false);
        safeUnbindCamera();

        if (!isActivityAlive()) return;

        String userMessage = errorMessage.contains("There are no faces in the image") ? "No face detected. Please position your face clearly in the circle." : errorMessage;

        dismissDialog(errorDialog);
        errorDialog = new AlertDialog.Builder(this).setTitle("Error").setMessage(userMessage).setPositiveButton("Retry", (d, w) -> initiateVerificationFlow()).setNegativeButton("Cancel", (d, w) -> finish()).setCancelable(false).create();

        errorDialog.show();
    }

    @Override
    public void onAuthenticationSucceeded() {
        if (!isActivityAlive()) return;
        if (!userId.isEmpty()) {
            currentState = ActivityState.UPLOADING;
            setLoadingVisible(true);
            callAttendanceApi(userId, employeeFirstName);
        } else {
            showAttendanceFailedAlert("Biometric authentication succeeded but no user found.");
        }
    }

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

    private void getCurrentLocation(@NonNull AddressCallback callback) {
        if (!isActivityAlive()) return;

        if (isVpnActive()) {
            dismissDialog(securityDialog);
            securityDialog = new AlertDialog.Builder(this).setTitle("Security Alert").setMessage("VPN detected. Please disconnect to continue.").setPositiveButton("OK", (d, w) -> finish()).setCancelable(false).show();
            return;
        }

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean isLocationEnabled = locationManager != null && (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && isLocationEnabled) {

            LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000).setMinUpdateIntervalMillis(5000).setWaitForAccurateLocation(true).setMaxUpdateDelayMillis(15000).build();

            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(@NonNull LocationResult result) {
                    if (!isActivityAlive()) return;

                    Location location = result.getLastLocation();
                    if (location == null) {
                        fusedLocationClient.removeLocationUpdates(this);
                        callback.onAddressReceived("Location not found", null);
                        return;
                    }

                    if (isMockLocation(location)) {
                        fusedLocationClient.removeLocationUpdates(this);
                        dismissDialog(securityDialog);
                        securityDialog = new AlertDialog.Builder(PunchActivity.this).setTitle("Security Alert").setMessage("Fake location detected. Please disable mock location apps to proceed.").setPositiveButton("OK", (d, w) -> finish()).setCancelable(false).show();
                        return;
                    }

                    fusedLocationClient.removeLocationUpdates(this);
                    getAddressFromLocation(location.getLatitude(), location.getLongitude(), (address, loc) -> callback.onAddressReceived(address, location));
                }
            };

            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());

        } else if (!isLocationEnabled) {
            dismissDialog(securityDialog);
            securityDialog = new AlertDialog.Builder(this).setTitle("Location Services Disabled").setMessage("Please enable location services to record attendance.").setPositiveButton("OK", (d, w) -> {
                d.dismiss();
                setResult(Activity.RESULT_CANCELED);
                finish();
            }).setCancelable(false).show();
        } else {
            callback.onAddressReceived("Location not found", null);
        }
    }

    private void getAddressFromLocation(double latitude, double longitude, @NonNull AddressCallback callback) {
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

    //    private void showLocationNotFoundAlert() {
//        if (!isActivityAlive()) return;
//        dismissDialog(securityDialog);
//        securityDialog = new AlertDialog.Builder(this)
//                .setTitle("Location Not Found")
//                .setMessage("We couldn't determine your location. Please check your GPS settings.")
//                .setPositiveButton("Retry", (d, w) -> {
//                    d.dismiss();
//                    initiateVerificationFlow();
//                })
//                .setNegativeButton("Exit", (d, w) -> finish())
//                .setCancelable(false)
//                .show();
//    }
    private void showLocationNotFoundAlert() {

        if (!isActivityAlive()) {
            Log.w(TAG, "Activity is not alive. Skipping Location NotFound Alert.");
            return;
        }

        dismissDialog(securityDialog);

        try {
            securityDialog = new AlertDialog.Builder(this).setTitle("Location Not Found").setMessage("We couldn't determine your location. Please check your GPS settings.").setPositiveButton("Retry", (d, w) -> {
                d.dismiss();
                initiateVerificationFlow();
            }).setNegativeButton("Exit", (d, w) -> finish()).setCancelable(false).create();

            if (isActivityAlive()) {
                securityDialog.show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to show Location NotFound Dialog safely", e);
        }
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
                    if (!isActivityAlive() || currentState != ActivityState.SCANNING) return;
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

    private enum ActivityState {
        IDLE, SCANNING, PROCESSING_LIVENESS, UPLOADING, BIOMETRIC_FALLBACK
    }

    private enum LivenessChallenge {
        BLINK, TURN_LEFT, TURN_RIGHT, TILT_UP, TILT_DOWN
    }

    interface AddressCallback {
        void onAddressReceived(String address, Location location);
    }
}
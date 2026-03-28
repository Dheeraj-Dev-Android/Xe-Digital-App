package app.xedigital.ai.activity;

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
import android.hardware.camera2.CameraManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import app.xedigital.ai.R;
import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.api.APIInterface;
import app.xedigital.ai.utills.BioMetric;
import app.xedigital.ai.utills.FaceOverlayView;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PunchActivity extends AppCompatActivity implements BioMetric.BiometricAuthListener {
    private static final String TAG = "PunchActivity";
    private static final int BIOMETRIC_PERMISSION_REQUEST_CODE = 100;
    private final String[] REQUIRED_PERMISSIONS = new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION};
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final String CollectionName = "consultedgeglobalpvtltd_5e970n";
    private final boolean isProcessing = false;
    private final boolean livenessDetected = false;
    private final AtomicBoolean isAnalyzing = new AtomicBoolean(false);
    private Preview preview;
    private ImageCapture imageCapture;
    private CameraSelector cameraSelector;
    private String authToken, userId;
    private Handler handler = new Handler();
    private FusedLocationProviderClient fusedLocationClient;
    private String currentAddress = "";
    private AlertDialog attendanceSuccessDialog;
    private MaterialCardView progressBar;
    private LocationCallback locationCallback;
    private FragmentManager fragmentManager;
    private CameraManager cameraManager;
    private BiometricManager biometricManager;
    private BioMetric bioMetric;
    private FaceDetector detector;
    private boolean isProcessingLiveness = false;
    private boolean isBlinking = false;
    private ObjectAnimator scannerAnimator;
    private FaceOverlayView faceOverlay;
    private LivenessChallenge currentChallenge;
    private boolean challengeSatisfied = false;
    private AlertDialog myAlertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_punch);
        // Initialize ML Kit Detector with Classification for Blink Detection
        FaceDetectorOptions options = new FaceDetectorOptions.Builder().setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST).setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL).setMinFaceSize(0.35f) // Ensures person is close enough
                .build();
        detector = FaceDetection.getClient(options);

        progressBar = findViewById(R.id.loadingPanel);
        faceOverlay = findViewById(R.id.faceOverlay);
        bioMetric = new BioMetric(this, this, this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        setRandomChallenge();

        ActivityResultLauncher<String[]> requestPermissionsLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
            boolean allGranted = true;
            for (boolean granted : result.values()) {
                if (!granted) {
                    allGranted = true;
                    break;
                }
            }
            startCamera();
        });

        if (allPermissionsGranted()) {
            startCamera();
        } else {
            requestPermissionsLauncher.launch(REQUIRED_PERMISSIONS);
        }

        imageCapture = new ImageCapture.Builder().build();
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        preview = new Preview.Builder().build();
        cameraSelector = new CameraSelector.Builder().build();
    }

    private void setRandomChallenge() {
        LivenessChallenge[] challenges = LivenessChallenge.values();
        currentChallenge = challenges[new Random().nextInt(challenges.length)];
        challengeSatisfied = false;
        isBlinking = false;

        // Use the helper to set the initial text
        updateStatus(getInstructionText(currentChallenge));
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void toggleScannerAnimation(boolean show) {
        View scannerLine = findViewById(R.id.scannerLine);
        View faceGuide = findViewById(R.id.faceGuide);

        runOnUiThread(() -> {
            if (show) {
                scannerLine.setVisibility(View.VISIBLE);
                if (scannerAnimator == null) {
                    // Move from top of guide to bottom
                    float startY = 0f;
                    float endY = (float) faceGuide.getHeight();

                    scannerAnimator = ObjectAnimator.ofFloat(scannerLine, "translationY", startY, endY);
                    scannerAnimator.setDuration(1500);
                    scannerAnimator.setRepeatCount(ValueAnimator.INFINITE);
                    scannerAnimator.setRepeatMode(ValueAnimator.REVERSE);
                    scannerAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
                }
                if (!scannerAnimator.isRunning()) {
                    scannerAnimator.start();
                }
            } else {
                scannerLine.setVisibility(View.GONE);
                if (scannerAnimator != null) {
                    scannerAnimator.cancel();
                }
            }
        });
    }

    private void startCamera() {
        toggleScannerAnimation(true);
        PreviewView viewFinder = findViewById(R.id.viewFinder);
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                preview = new Preview.Builder().build();
                preview.setSurfaceProvider(viewFinder.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build();

                // New Image Analysis Use Case for Liveness
                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();

                imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), this::analyzeFace);

                cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_FRONT).build();

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture, imageAnalysis);

            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Use case binding failed", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @OptIn(markerClass = androidx.camera.core.ExperimentalGetImage.class)
    private void analyzeFace(@NonNull ImageProxy imageProxy) {
        // 1. Thread & Lifecycle Gate: Drop frame if busy, processing capture, or no challenge set
        if (isAnalyzing.get() || isProcessingLiveness || currentChallenge == null) {
            imageProxy.close();
            return;
        }

        isAnalyzing.set(true); // Close the gate for incoming frames

        android.media.Image mediaImage = imageProxy.getImage();
        if (mediaImage != null) {
            InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());

            detector.process(image).addOnSuccessListener(faces -> {
                if (faces.isEmpty()) {
                    faceOverlay.setFaceDetected(false);
                    updateStatus("Position face in frame");
                    isBlinking = false;
                } else {
                    faceOverlay.setFaceDetected(true);
                    Face face = faces.get(0);
                    processChallenge(face);

                    // 3. Instruction Persistence: Keep the specific text until finished
                    if (challengeSatisfied) {
                        updateStatus("Verified! Capturing...");
                        isProcessingLiveness = true; // Lock the analyzer for capture
                        toggleScannerAnimation(false);
                        captureImage();
                    } else {
                        // This ensures the instruction doesn't disappear if they turn the wrong way
                        updateStatus(getInstructionText(currentChallenge));
                    }
                }
            }).addOnFailureListener(e -> Log.e(TAG, "Face detection failed", e)).addOnCompleteListener(task -> {
                // 4. CRITICAL: Always release the image and open the gate
                imageProxy.close();
                isAnalyzing.set(false);
            });
        } else {
            imageProxy.close();
            isAnalyzing.set(false);
        }
    }

    private void processChallenge(Face face) {
        float headY = face.getHeadEulerAngleY(); // Left/Right
        float headX = face.getHeadEulerAngleX(); // Up/Down
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

        if (challengeSatisfied) {
            isProcessingLiveness = true;
            updateStatus("Verified! Capturing...");
            toggleScannerAnimation(false);
            captureImage();
        }
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

    private void updateStatus(String text) {
        runOnUiThread(() -> {
            TextView captureText = findViewById(R.id.CaptureText);
            captureText.setText(text);
        });
    }

    private void captureImage() {
        File photoFile = new File(getOutputDirectory(), System.currentTimeMillis() + "_photo.jpg");
        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                if (isFinishing() || isDestroyed()) return;

                new Thread(() -> {
                    try {
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inSampleSize = 2;
                        Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath(), options);

                        if (bitmap != null) {
                            int newWidth = 500;
                            int newHeight = (int) (bitmap.getHeight() * (newWidth / (float) bitmap.getWidth()));
                            Bitmap scaled = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, false);

                            String base64 = convertImageToBase64(scaled);

                            bitmap.recycle();
                            scaled.recycle();

                            runOnUiThread(() -> {
                                if (!isFinishing() && !isDestroyed()) {
                                    progressBar.setVisibility(View.VISIBLE);
                                    prepareJsonAndSend(base64);
                                }
                            });
                        } else {
                            handleError("Failed to process image");
                        }
                    } catch (Exception e) {
                        handleError("Error: " + e.getMessage());
                    }
                }).start();
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
            JSONObject jsonObject = new JSONObject();

            jsonObject.put("collection_name", CollectionName);
            jsonObject.put("image", base64Image);

            String requestBodyJson = jsonObject.toString();
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), requestBodyJson);

            // Initiate the API sequence
            sendImageToApi(requestBody);

        } catch (JSONException e) {
            Log.e(TAG, "JSON Encoding error: " + e.getMessage());
            isProcessingLiveness = false;
            setRandomChallenge();
            progressBar.setVisibility(View.GONE);
            handleError("Failed to prepare image data for server.");
        }
    }

    //    API TO SEND IMAGE TO DB
    private void sendImageToApi(RequestBody requestBody) {
        APIInterface imageApiService = APIClient.getInstance().getImage();
        retrofit2.Call<ResponseBody> recognize = imageApiService.FaceRecognitionApi(requestBody);

        recognize.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseBody = response.body().string();
                        String responseJson = gson.toJson(responseBody);
                        JSONObject jsonResponse = new JSONObject(responseBody);

                        // ADD THIS CHECK:
                        if (!jsonResponse.has("data") || jsonResponse.isNull("data")) {
                            isProcessingLiveness = false; // Add this
                            setRandomChallenge();
                            handleError("Error: Data field is missing or null.");
                            return;
                        }
                        JSONObject dataObject = jsonResponse.getJSONObject("data");

                        Intent intent = getIntent();
                        if (intent != null) {
                            authToken = intent.getStringExtra("authToken");
                        }
                        String token = "jwt " + authToken;
                        String requestBodyFace = dataObject.toString();
                        RequestBody requestBodyFacee = RequestBody.create(MediaType.parse("application/json"), requestBodyFace);
                        //API CALL
                        callFaceDetailApi(token, requestBodyFacee);

                    } catch (IOException | JSONException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading error body: " + e.getMessage(), e);
                    }
                    handleError("Server Error: " + response.code() + " - " + response.message() + "\nDetails: " + errorBody);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
                // Handle network or other errors
                isProcessingLiveness = false;
                handleError("Network Error: " + throwable.getMessage());
            }
        });
    }

    private void handleError(String errorMessage) {
        isProcessingLiveness = false;
        challengeSatisfied = false;
        isAnalyzing.set(false);

        runOnUiThread(() -> {
            if (isFinishing() || isDestroyed()) return;

            toggleScannerAnimation(true);
            setRandomChallenge();

            new AlertDialog.Builder(this).setTitle("Error").setMessage(errorMessage.contains("There are no faces in the image") ? "No faces detected in the image. Please try again with a clear image showing your face." : errorMessage).setPositiveButton("Retry", (dialog, which) -> startCamera()).setNegativeButton("Cancel", (dialog, which) -> finish()).setCancelable(false).show();
            safeUnbindCamera();

        });

    }

    private void safeUnbindCamera() {
        ProcessCameraProvider.getInstance(this).addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = ProcessCameraProvider.getInstance(this).get();
                cameraProvider.unbindAll();
            } catch (Exception e) {
                Log.e(TAG, "Unbind failed", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void callFaceDetailApi(String token, RequestBody requestBodyFacee) {
        APIInterface faceApiService = APIClient.getInstance().getFace();
        retrofit2.Call<ResponseBody> faceDetails = faceApiService.FaceDetailApi(token, requestBodyFacee);

        faceDetails.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseBody = response.body().string();
                        JSONObject jsonResponse = new JSONObject(responseBody);

                        // Check if "data" key exists and is not null before proceeding
                        if (!jsonResponse.has("data") || jsonResponse.isNull("data")) {
                            showAttendanceFailedAlert("Attendance failed: Face not found or matched.");
                            return;
                        }
                        if (jsonResponse.has("data") && !jsonResponse.isNull("data")) {
                            JSONObject dataObject = jsonResponse.getJSONObject("data");
                            JSONObject employeeObject = dataObject.getJSONObject("employee");
                            String id = employeeObject.getString("_id");
                            String firstName = employeeObject.getString("firstname");
                            String responseJson = gson.toJson(responseBody);
                            SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                            userId = sharedPreferences.getString("userId", null);
                            if (userId != null && userId.equals(id)) {
                                callAttendanceApi(id, firstName);
                            } else {
                                showAttendanceFailedAlert("Attendance failed: User Id Mismatch.");
                            }
                        } else {
                            showAttendanceFailedAlert("Attendance failed: Face not found or matched.");
                        }
                    } catch (IOException | JSONException e) {
                        showAttendanceFailedAlert("Attendance failed: An error occurred.");
                    }
                } else {
                    showAttendanceFailedAlert("Attendance failed: Server error.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
                isProcessingLiveness = false;
                showAttendanceFailedAlert("Attendance failed: Network or API error.");
            }
        });
    }

    private void callAttendanceApi(String employeeId, String employeeName) {
        if (!isAutomaticTimeEnabled()) {
            // Check if the activity is finishing or destroyed before showing the dialog
            if (!isFinishing() && !isDestroyed()) {
                new AlertDialog.Builder(this).setTitle("Security Check").setMessage("Please enable 'Automatic Date and Time'...").setPositiveButton("Settings", (dialog, which) -> {
                    startActivity(new Intent(android.provider.Settings.ACTION_DATE_SETTINGS));
                }).setNegativeButton("Cancel", (dialog, which) -> finish()).show();
            }
            return;
        }
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId", null);
        if (userId != null && userId.equals(employeeId)) {
            Intent intent = getIntent();
            if (intent != null) {
                authToken = intent.getStringExtra("authToken");
            }
            String token = "jwt " + authToken;

            getCurrentLocation((address, loc) -> {
                currentAddress = address;
                String currentTime = getCurrentTime(loc);
                try {
                    JSONObject requestBody = new JSONObject();
                    requestBody.put("employee", employeeId);
                    requestBody.put("employeeName", employeeName);
                    requestBody.put("address", currentAddress);
                    requestBody.put("punchTime", currentTime);

                    String requestBodyString = requestBody.toString();
                    RequestBody requestBodyAttendance = RequestBody.create(MediaType.parse("application/json"), requestBodyString);

                    APIInterface attendanceApiService = APIClient.getInstance().getAttendance();
                    Call<ResponseBody> attendance = attendanceApiService.AttendanceApi(token, requestBodyAttendance);

                    attendance.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                            ProgressBar progressBar = findViewById(R.id.progressBar);
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    String responseBody = response.body().string();
                                    JSONObject responseJson = new JSONObject(responseBody);
                                    String message = responseJson.getString("message");
//                                    showAttendanceSuccessAlert(responseBody);
                                    if (!isFinishing() && !isDestroyed()) {
                                        showAttendanceSuccessAlert(responseBody);
                                    }
                                    progressBar.setVisibility(View.GONE);

                                    String responseJsonn = gson.toJson(responseBody);
                                } catch (IOException | JSONException e) {
                                    progressBar.setVisibility(View.GONE);
                                    throw new RuntimeException(e);
                                }
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
                            isProcessingLiveness = false;
                            if (isFinishing() || isDestroyed()) return;

                            ProgressBar progressBar = findViewById(R.id.progressBar);
                            if (progressBar != null) progressBar.setVisibility(View.GONE);
                            if (throwable instanceof java.net.SocketTimeoutException) {
                                showAttendanceFailedAlert("Connection timed out. Please check your internet and try again.");
                            } else {
                                showAttendanceFailedAlert("Network error: " + throwable.getMessage());
                            }

                        }

                    });
                } catch (JSONException e) {
                    progressBar.setVisibility(View.GONE);
                    throw new RuntimeException(e);
                }
            });
        } else {
            showAttendanceFailedAlert("Attendance failed: User Id Mismatch.");
        }
    }

    private boolean isAutomaticTimeEnabled() {
        return android.provider.Settings.Global.getInt(getContentResolver(), android.provider.Settings.Global.AUTO_TIME, 0) == 1;
    }

    private void showAttendanceSuccessAlert(String responseBody) {
        toggleScannerAnimation(false);
        progressBar.setVisibility(View.GONE);
        if (isFinishing() || isDestroyed()) {
            return;
        }
        try {
            JSONObject responseJson = new JSONObject(responseBody);
            boolean success = responseJson.getBoolean("success");
            String message = responseJson.getString("message");
            if (success) {
                JSONObject data = responseJson.getJSONObject("data");
                String punchInAddress = data.getString("punchInAddress");
                String punchOutAddress = data.getString("punchOutAddress");

                String address;
                if (punchOutAddress.isEmpty()) {
                    address = punchInAddress;
                } else {
                    address = punchOutAddress;
                }

                StringBuilder formattedMessage = new StringBuilder();
                // Check if AlertDialog.Builder supports HTML formatting
                formattedMessage.append("<b>").append(message).append("</b>").append("<br><br>");
                formattedMessage.append("Address: ").append(address).append("<br>");
                fragmentManager = getSupportFragmentManager();

                // Replace your runOnUiThread block with this:
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed() || isChangingConfigurations()) {
                        return;
                    }
                    if (attendanceSuccessDialog != null && attendanceSuccessDialog.isShowing()) {
                        attendanceSuccessDialog.dismiss();
                    }
                    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(PunchActivity.this);
                    attendanceSuccessDialog = builder.setTitle("Attendance Success").setMessage(Html.fromHtml(formattedMessage.toString(), Html.FROM_HTML_MODE_LEGACY)).setPositiveButton("OK", (dialog1, which) -> {
                        dialog1.dismiss();
                        setResult(Activity.RESULT_OK);
                        finish();
                    }).show();

                    // Use a Runnable so we can remove it if the activity is destroyed
                    handler.postDelayed(() -> {
                        // Check lifecycle AGAIN inside the delay
                        if (!isFinishing() && !isDestroyed()) {
                            if (attendanceSuccessDialog != null && attendanceSuccessDialog.isShowing()) {
                                attendanceSuccessDialog.dismiss();
                            }
                            setResult(Activity.RESULT_OK);
                            finish();
                        }
                    }, 5000);
                });
            }
        } catch (JSONException e) {
            finish();
            throw new RuntimeException(e);
        }
    }

    private void showAttendanceFailedAlert(String message) {

        toggleScannerAnimation(false);
        setRandomChallenge();
        progressBar.setVisibility(View.GONE);
        runOnUiThread(() -> {
            if (isFinishing() || isDestroyed()) {
                return;
            }
            // Reset the liveness flag so they can try again after closing the dialog
            isProcessingLiveness = false;
            AlertDialog.Builder builder = new AlertDialog.Builder(PunchActivity.this);
            builder.setTitle("Attendance Failed").setMessage(message).setPositiveButton("Retry", (dialog, id) -> {
                dialog.dismiss();
                setRandomChallenge();
                startCamera();
            }).setNegativeButton("Cancel", (dialog, id) -> {
                dialog.dismiss();
                setResult(Activity.RESULT_CANCELED);
                finish();
            }).setNeutralButton("Use Biometric", (dialog, id) -> {
                dialog.dismiss();
                usePhoneBiometric();
            }).create().show();

        });
    }

    private String getCurrentTime(Location location) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        if (location != null) {
            return dateFormat.format(new Date(location.getTime()));
        }

        return dateFormat.format(new Date());
    }

    private boolean isMockLocation(Location location) {
        if (location == null) return false;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            return location.isMock();
        } else {
            return location.isFromMockProvider();
        }
    }

    private boolean isVpnActive() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            android.net.Network activeNetwork = cm.getActiveNetwork();
            android.net.NetworkCapabilities caps = cm.getNetworkCapabilities(activeNetwork);
            return caps != null && caps.hasTransport(android.net.NetworkCapabilities.TRANSPORT_VPN);
        }
        return false;
    }

    private void getCurrentLocation(AddressCallback callback) {
        // SECURITY CHECK 1: VPN Detection
        if (isVpnActive()) {
            new AlertDialog.Builder(this).setTitle("Security Alert").setMessage("VPN detected. Please disconnect from your VPN to mark attendance.").setPositiveButton("Ok", (dialog, which) -> finish()).show();
            return;
        }
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean isLocationEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && isLocationEnabled) {
            LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000).setMinUpdateIntervalMillis(5000).setWaitForAccurateLocation(true).setMaxUpdateDelayMillis(15000).build();
            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(@NonNull LocationResult locationResult) {
                    Location location = locationResult.getLastLocation();

                    if (location != null) {
                        // SECURITY CHECK 2: Mock Location Detection
                        if (isMockLocation(location)) {
                            fusedLocationClient.removeLocationUpdates(this);
                            new AlertDialog.Builder(PunchActivity.this).setTitle("Security Alert").setMessage("Fake location detected. Please disable mock location apps to proceed.").setPositiveButton("Ok", (dialog, which) -> finish()).show();
                            return;
                        }
                        // Pass the 'location' object into the address fetcher
                        getAddressFromLocation(location.getLatitude(), location.getLongitude(), (address, loc) -> {
                            callback.onAddressReceived(address, location);
                        });
                        fusedLocationClient.removeLocationUpdates(this);
                    } else {
                        callback.onAddressReceived("Location not found", null);
                    }
                }
            };
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        } else {
            // Location permission or location services not enabled
            if (!isLocationEnabled) {
                // Show alert to enable location services
                new AlertDialog.Builder(this).setTitle("Location Services Disabled").setMessage("Please enable location services to use this feature.").setPositiveButton("Ok", (dialog, which) -> {
                    dialog.dismiss();
                    setResult(Activity.RESULT_CANCELED);
                    finish();
                }).show();
            } else {
                callback.onAddressReceived("Location not found", null);
            }
        }
    }

    private void getAddressFromLocation(final double latitude, final double longitude, final AddressCallback callback) {
        new Thread(() -> {
            Geocoder geocoder = new Geocoder(PunchActivity.this, Locale.getDefault());
            Address address = null;
            int retryCount = 0;
            int maxRetries = 2;

            while (retryCount < maxRetries) {
                try {
                    List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                    if (addresses != null && !addresses.isEmpty()) {
                        address = addresses.get(0);
                        break; // Exit the loop if an address is found
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Error getting address:", e);
                    retryCount++;
                    try {
                        Thread.sleep((long) (Math.pow(2, retryCount) * 1000));
                    } catch (InterruptedException interruptedException) {
                        interruptedException.printStackTrace();
                        break; // Exit the loop if interrupted
                    }
                }
            }

            final String addressResult;
            if (address != null) {
                StringBuilder completeAddress = new StringBuilder();
                for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                    completeAddress.append(address.getAddressLine(i)).append("\n");
                }
                addressResult = completeAddress.toString();
            } else {
                addressResult = "Location not found";
                Log.e(TAG, "No address found for location or max retries reached");
            }

            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) return;
                currentAddress = addressResult;

                // Reconstruct location object for the callback
                Location tempLocation = new Location("serviceProvider");
                tempLocation.setLatitude(latitude);
                tempLocation.setLongitude(longitude);
//                tempLocation.setTime(System.currentTimeMillis());

                callback.onAddressReceived(addressResult, tempLocation);
            });
        }).start();
    }

    private String convertImageToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream);
        byte[] imageBytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    private File getOutputDirectory() {
        File mediaDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Punch");
        if (!mediaDir.exists()) {
            if (!mediaDir.mkdirs()) {
                Log.e(TAG, "Failed to create directory");
            }
        }
        return mediaDir;
    }

    private void usePhoneBiometric() {
        progressBar.setVisibility(View.VISIBLE);
        if (bioMetric != null) {
            bioMetric.authenticate(false);
        } else {
            Log.e(TAG, "BioMetric is Null!");
        }
    }

    @Override
    public void onAuthenticationSucceeded() {
        // Call your attendance API here
        runOnUiThread(() -> {
            SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
            String employeeId = sharedPreferences.getString("userId", "");
            String employeeName = sharedPreferences.getString("empFirstName", "");
            callAttendanceApi(employeeId, employeeName);
        });
    }

    @Override
    public void onAuthenticationError(int errorCode, CharSequence errString) {
        Log.d(TAG, "onAuthenticationError");
        progressBar.setVisibility(View.GONE);
        showAttendanceFailedAlert("Biometric Error:" + errString);
        Toast.makeText(this, "Authentication error: " + errString, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAuthenticationFailed() {
        Log.d(TAG, "onAuthenticationFailed");
        progressBar.setVisibility(View.GONE);
        showAttendanceFailedAlert("Biometric Failed");
        Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        int REQUEST_CODE_PERMISSIONS = 10;
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera();
            }
        }
        if (requestCode == BIOMETRIC_PERMISSION_REQUEST_CODE) {
            bioMetric.handlePermissionResult(requestCode, permissions, grantResults, false);
        }
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        // Dismiss any active dialogs immediately
        if (attendanceSuccessDialog != null && attendanceSuccessDialog.isShowing()) {
            attendanceSuccessDialog.dismiss();
        }
        // Stop the animator
        if (scannerAnimator != null) {
            scannerAnimator.cancel();
        }
        if (locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
        if (detector != null) {
            detector.close();
        }
        safeUnbindCamera();
        super.onDestroy();
    }

    private enum LivenessChallenge {
        BLINK, TURN_LEFT, TURN_RIGHT, TILT_UP, TILT_DOWN
    }

    interface AddressCallback {
        void onAddressReceived(String address, Location location);
    }
}
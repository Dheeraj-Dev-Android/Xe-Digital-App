package app.xedigital.ai.adminActivity;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Dialog;
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
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
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
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import app.xedigital.ai.R;
import app.xedigital.ai.adminApi.AdminAPIClient;
import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.api.APIInterface;
import app.xedigital.ai.model.Admin.addBucket.AddBucketRequest;
import app.xedigital.ai.model.Admin.visitorContact.VisitorContactResponse;
import app.xedigital.ai.model.Admin.visitorFace.VisitorFaceResponse;
import app.xedigital.ai.utills.CustomAlertDialog;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminPunchActivity extends AppCompatActivity {

    private static final String TAG = "AdminPunchActivity";
    private static final int REQUEST_CODE_PERMISSIONS = 10;
    private final String[] REQUIRED_PERMISSIONS = buildRequiredPermissions();
    private final Handler handler = new Handler();
    // =========================================================
    //  STATE FLAGS — prevent double-start / double-capture
    // =========================================================
    private final AtomicBoolean isCameraStarted = new AtomicBoolean(false);
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final AtomicBoolean isCaptureInProgress = new AtomicBoolean(false);
    private final AtomicBoolean isImageSaved = new AtomicBoolean(false);
    private boolean cameFromSettings = false;
    private Dialog activeDialog;
    // =========================================================
    //  VIEWS
    // =========================================================
    private PreviewView previewView;
    private View scannerLine;
    private TextView captureOverlay;
    private MaterialCardView loadingPanel;
    // =========================================================
    //  CAMERA
    // =========================================================
    private ProcessCameraProvider cameraProvider;
    private ObjectAnimator scannerAnimator;
    private ImageCapture imageCapture;
    private Camera camera;
    private ExecutorService cameraExecutor;
    // =========================================================
    //  DATA
    // =========================================================
    private String collectionName;
    private String currentAddress = "";
    private LocationCallback locationCallback;
    private String token;
    private String base64Image;
    private String faceId, imageId;
    private FusedLocationProviderClient fusedLocationClient;
    private ActivityResultLauncher<String[]> requestPermissionsLauncher;
    private CountDownTimer qrCountDownTimer;

    // =========================================================
    //  PERMISSIONS
    // =========================================================
    private static String[] buildRequiredPermissions() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            return new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            };
        } else {
            return new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            };
        }
    }

    // =========================================================
    //  LIFECYCLE
    // =========================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_punch_activity);

        SharedPreferences prefs = getSharedPreferences("AdminCred", MODE_PRIVATE);
        token = prefs.getString("authToken", "");
        collectionName = prefs.getString("collectionName", "");
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        previewView = findViewById(R.id.previewView);
        captureOverlay = findViewById(R.id.capture_overlay);
        scannerLine = findViewById(R.id.scannerLine);
        loadingPanel = findViewById(R.id.loadingPanel);
        cameraExecutor = Executors.newSingleThreadExecutor();

        registerPermissionLauncher();

        if (allPermissionsGranted()) {
            startCamera();
        } else {
            requestPermissionsLauncher.launch(REQUIRED_PERMISSIONS);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (cameFromSettings) {
            cameFromSettings = false;
            if (allPermissionsGranted()) {
                isCameraStarted.set(false);
                startCamera();
            } else {
                Toast.makeText(this,
                        "Permissions still not granted.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (activeDialog != null && activeDialog.isShowing()) {
            activeDialog.dismiss();
            activeDialog = null;
        }
        releaseCamera();
        handler.removeCallbacksAndMessages(null);
        if (cameraExecutor != null) cameraExecutor.shutdown();
        if (locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
        if (qrCountDownTimer != null) {
            qrCountDownTimer.cancel();
        }
    }

    // =========================================================
    //  PERMISSIONS
    // =========================================================

    private void registerPermissionLauncher() {
        requestPermissionsLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    boolean allGranted = true;
                    boolean permanentlyDenied = false;

                    for (String permission : result.keySet()) {
                        Boolean granted = result.get(permission);
                        if (granted == null || !granted) {
                            allGranted = false;
                            if (!ActivityCompat.shouldShowRequestPermissionRationale(
                                    this, permission)) {
                                permanentlyDenied = true;
                            }
                        }
                    }

                    if (allGranted) {
                        startCamera();
                    } else if (permanentlyDenied) {
                        showGoToSettingsDialog();
                    } else {
                        new CustomAlertDialog(this)
                                .setType(CustomAlertDialog.Type.WARNING)
                                .setTitle("Permissions Required")
                                .setMessage("Camera and Location permissions are needed to record attendance.")
                                .setCancelable(false)
                                .setPositiveButton("TRY AGAIN", () ->
                                        requestPermissionsLauncher.launch(REQUIRED_PERMISSIONS))
                                .setNegativeButton("CANCEL", () -> {
                                    setResult(Activity.RESULT_CANCELED);
                                    finish();
                                })
                                .show();
                    }
                });
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void showGoToSettingsDialog() {
        new CustomAlertDialog(this)
                .setType(CustomAlertDialog.Type.WARNING)
                .setTitle("Permissions Required")
                .setMessage("Camera and Location permissions were permanently denied. Please enable them from App Settings.")
                .setCancelable(false)
                .setPositiveButton("OPEN SETTINGS", () -> {
                    cameFromSettings = true;
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.fromParts("package", getPackageName(), null));
                    startActivity(intent);
                })
                .setNegativeButton("CANCEL", () -> {
                    setResult(Activity.RESULT_CANCELED);
                    finish();
                })
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS && allPermissionsGranted()) {
            startCamera();
        }
    }

    // =========================================================
    //  CAMERA
    // =========================================================

    private void startCamera() {
        if (!isCameraStarted.compareAndSet(false, true)) {
            Log.d(TAG, "startCamera() skipped — already running.");
            return;
        }

        handler.removeCallbacks(captureRunnable);
        isCaptureInProgress.set(false);
        isImageSaved.set(false);

        ListenableFuture<ProcessCameraProvider> future =
                ProcessCameraProvider.getInstance(this);

        future.addListener(() -> {
            try {
                cameraProvider = future.get();

                Preview preview = new Preview.Builder().build();
                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();

                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                        .build();

                preview.setSurfaceProvider(previewView.getSurfaceProvider());
                cameraProvider.unbindAll();
                camera = cameraProvider.bindToLifecycle(
                        this, cameraSelector, preview, imageCapture);

                showCapturingOverlay();
                startScannerAnimation();
                handler.postDelayed(captureRunnable, 3000);

            } catch (Exception e) {
                Log.e(TAG, "startCamera failed: " + e.getMessage(), e);
                isCameraStarted.set(false);
                runOnUiThread(this::showRetryAlert);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void releaseCamera() {
        handler.removeCallbacks(captureRunnable);
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
        camera = null;
        imageCapture = null;
        isCameraStarted.set(false);
        isCaptureInProgress.set(false);
        stopScannerAnimation();
    }

    private void showRetryAlert() {
        if (isFinishing() || isDestroyed()) return;
        new CustomAlertDialog(this)
                .setType(CustomAlertDialog.Type.WARNING)
                .setTitle("Camera Not Found")
                .setMessage("No camera found. Please check your device and try again.")
                .setCancelable(false)
                .setPositiveButton("RETRY", this::startCamera)
                .setNegativeButton("CANCEL", () -> {
                    setResult(Activity.RESULT_CANCELED);
                    finish();
                })
                .show();
    }

    // =========================================================
    //  SCANNER ANIMATION
    // =========================================================

    private void startScannerAnimation() {
        if (scannerLine == null) return;
        runOnUiThread(() -> {
            scannerLine.setVisibility(View.VISIBLE);
            scannerLine.post(() -> {
                View cameraCard = findViewById(R.id.cameraCard);
                float travel = cameraCard != null
                        ? cameraCard.getHeight() - scannerLine.getHeight()
                        : 260f;

                scannerAnimator = ObjectAnimator.ofFloat(
                        scannerLine, "translationY", 0f, travel);
                scannerAnimator.setDuration(1400);
                scannerAnimator.setRepeatMode(ValueAnimator.REVERSE);
                scannerAnimator.setRepeatCount(ValueAnimator.INFINITE);
                scannerAnimator.start();
            });
        });
    }

    private void stopScannerAnimation() {
        runOnUiThread(() -> {
            if (scannerAnimator != null) {
                scannerAnimator.cancel();
                scannerAnimator = null;
            }
            if (scannerLine != null) {
                scannerLine.setVisibility(View.GONE);
                scannerLine.setTranslationY(0f);
            }
        });
    }

    // =========================================================
    //  IMAGE CAPTURE
    // =========================================================

    private void captureImage() {
        if (!isCaptureInProgress.compareAndSet(false, true)) {
            Log.d(TAG, "captureImage() skipped — already in progress.");
            return;
        }

        if (imageCapture == null) {
            Log.e(TAG, "captureImage() — imageCapture is null.");
            isCaptureInProgress.set(false);
            handleError("Camera not ready. Please try again.");
            return;
        }

        isImageSaved.set(false);

        File photoFile = new File(
                getOutputDirectory(),
                System.currentTimeMillis() + "_photo.jpg");

        ImageCapture.OutputFileOptions options =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(
                options,
                ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {

                    @Override
                    public void onImageSaved(
                            @NonNull ImageCapture.OutputFileResults results) {

                        isImageSaved.set(true);

                        stopScannerAnimation();
                        hideCapturingOverlay();
                        loadingPanel.setVisibility(View.VISIBLE);

                        if (cameraProvider != null) {
                            cameraProvider.unbindAll();
                        }
                        camera = null;

                        processImage(photoFile);
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        if (isImageSaved.get()) {
                            Log.w(TAG, "onError fired after successful save " +
                                    "(CameraX race — safely ignored): "
                                    + exception.getMessage());
                            return;
                        }

                        Log.e(TAG, "Photo capture failed: "
                                + exception.getMessage(), exception);
                        isCaptureInProgress.set(false);
                        stopScannerAnimation();
                        handleError("Photo capture failed: "
                                + exception.getMessage());
                    }
                });
    }    private final Runnable captureRunnable = this::captureImage;

    private void processImage(File photoFile) {
        try {
            AtomicReference<Bitmap> bitmap = new AtomicReference<>(
                    BitmapFactory.decodeFile(photoFile.getAbsolutePath()));

            if (bitmap.get() == null) {
                handleError("Failed to decode captured image.");
                return;
            }

            int newWidth = 500;
            int newHeight = (int) (bitmap.get().getHeight()
                    * (newWidth / (float) bitmap.get().getWidth()));
            bitmap.set(Bitmap.createScaledBitmap(
                    bitmap.get(), newWidth, newHeight, false));

            base64Image = convertImageToBase64(bitmap.get());

            JSONObject json = new JSONObject();
            json.put("collection_name", collectionName);
            json.put("image", base64Image);

            RequestBody requestBody = RequestBody.create(
                    MediaType.parse("application/json"), json.toString());

            sendToAPI(requestBody);

        } catch (Exception e) {
            Log.e(TAG, "processImage error: " + e.getMessage(), e);
            handleError("Error processing image: " + e.getMessage());
        }
    }

    private void sendToAPI(RequestBody requestBody) {
        String authToken = "jwt " + token;
        Call<ResponseBody> call = AdminAPIClient.getInstance()
                .getBase1()
                .recognizeFace(authToken, requestBody);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call,
                                   @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JSONObject json = new JSONObject(response.body().string());

                        if (json.has("data") && !json.isNull("data")) {
                            JSONObject data = json.getJSONObject("data");

                            if (data.has("Face") && !data.isNull("Face")) {
                                JSONObject face = data.getJSONObject("Face");
                                faceId = face.optString("FaceId", "N/A");
                                imageId = face.optString("ImageId", "N/A");
                            }

                            RequestBody faceBody = RequestBody.create(
                                    MediaType.parse("application/json"),
                                    data.toString());
                            callFaceDetailApi(faceBody);
                        } else {
                            handleError("Face Details Not Found");
                        }
                    } catch (JSONException | IOException e) {
                        handleError("Error parsing response: " + e.getMessage());
                    }
                } else {
                    String err = "";
                    try {
                        if (response.errorBody() != null)
                            err = response.errorBody().string();
                    } catch (IOException ignored) {
                    }
                    handleError("Server Error: " + response.code()
                            + "\nDetails: " + err);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call,
                                  @NonNull Throwable t) {
                handleError("Network Error: " + t.getMessage());
            }
        });
    }

    // =========================================================
    //  API CHAIN
    // =========================================================

    private void callFaceDetailApi(RequestBody faceDetailsBody) {
        String authToken = "jwt " + token;
        Call<ResponseBody> call = AdminAPIClient.getInstance()
                .getBase2()
                .FaceDetails(authToken, faceDetailsBody);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call,
                                   @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JSONObject json = new JSONObject(response.body().string());

                        if (json.has("data") && json.isNull("data")) {
                            callVisitorFace(faceDetailsBody);
                            return;
                        }

                        if (json.has("data") && !json.isNull("data")) {
                            JSONObject data = json.getJSONObject("data");
                            if (data.has("employee") && !data.isNull("employee")) {
                                JSONObject emp = data.getJSONObject("employee");
                                String id = emp.optString("_id", null);
                                String name = emp.optString("firstname", null);
                                if (id != null && name != null) {
                                    callAttendanceApi(id, name, null);
                                } else {
                                    showAttendanceFailedAlert("Missing employee details.");
                                }
                            } else {
                                callVisitorFace(faceDetailsBody);
                            }
                        } else {
                            showAttendanceFailedAlert("Face not found or matched.");
                        }
                    } catch (IOException | JSONException e) {
                        showAttendanceFailedAlert("An error occurred.");
                    }
                } else {
                    showAttendanceFailedAlert("Error processing face detail");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call,
                                  @NonNull Throwable t) {
                showAttendanceFailedAlert("Error processing face detail.");
            }
        });
    }

    private void callAttendanceApi(String employeeId, String employeeName, Location location) {
        if (employeeId == null || employeeId.isEmpty()) {
            showAttendanceFailedAlert("User not found.");
            return;
        }

        getCurrentLocation((address, loc) -> {
            currentAddress = address;
            try {
                JSONObject body = new JSONObject();
                body.put("employee", employeeId);
                body.put("employeeName", employeeName);
                body.put("address", currentAddress);
                body.put("punchTime", getCurrentTime(loc));

                RequestBody reqBody = RequestBody.create(
                        MediaType.parse("application/json"), body.toString());

                String authToken = "jwt " + token;
                APIInterface api = APIClient.getInstance().getAttendance();

                api.AttendanceApi(authToken, reqBody).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(@NonNull Call<ResponseBody> call,
                                           @NonNull Response<ResponseBody> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            try {
                                showAttendanceSuccessAlert(response.body().string());
                            } catch (IOException e) {
                                loadingPanel.setVisibility(View.GONE);
                                showAttendanceFailedAlert("Error reading response.");
                            }
                        } else {
                            loadingPanel.setVisibility(View.GONE);
                            showAttendanceFailedAlert("Attendance failed: " + response.message());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ResponseBody> call,
                                          @NonNull Throwable t) {
                        loadingPanel.setVisibility(View.GONE);
                        showAttendanceFailedAlert("Network Error: " + t.getMessage());
                    }
                });
            } catch (JSONException e) {
                loadingPanel.setVisibility(View.GONE);
                showAttendanceFailedAlert("Error building request.");
            }
        });
    }

    private void callVisitorFace(RequestBody requestBodyFace) {
        String authToken = "jwt " + token;
        Call<VisitorFaceResponse> call = AdminAPIClient.getInstance()
                .getBase2()
                .FaceDetailsVisitor(authToken, requestBodyFace);

        call.enqueue(new Callback<VisitorFaceResponse>() {
            @Override
            public void onResponse(@NonNull Call<VisitorFaceResponse> call,
                                   @NonNull Response<VisitorFaceResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JSONObject json = new JSONObject(gson.toJson(response.body()));

                        if (!json.has("data") || json.isNull("data")) {
                            addBucket(base64Image);
                            return;
                        }

                        JSONObject data = json.getJSONObject("data");

                        if (data.has("visitor") && !data.isNull("visitor")) {
                            String contact = data.getJSONObject("visitor")
                                    .optString("contact", "");
                            if (!contact.isEmpty()) {
                                callGetCheckedInApi(contact);
                            } else {
                                addBucket(base64Image);
                            }
                        } else if (data.has("employee")) {
                            JSONObject emp = data.getJSONObject("employee");
                            callAttendanceApi(
                                    emp.getString("_id"),
                                    emp.getString("firstname"),
                                    null);
                        } else {
                            addBucket(base64Image);
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON Error: " + e.getMessage());
                        addBucket(base64Image);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<VisitorFaceResponse> call,
                                  @NonNull Throwable t) {
                handleError("Network Error: " + t.getMessage());
            }
        });
    }

    private void callGetCheckedInApi(String visitorContact) {
        String authToken = "jwt " + token;
        AdminAPIClient.getInstance()
                .getBase2()
                .getCheckedIn(authToken, visitorContact)
                .enqueue(new Callback<VisitorContactResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<VisitorContactResponse> call,
                                           @NonNull Response<VisitorContactResponse> response) {
                        loadingPanel.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null) {
                            VisitorContactResponse body = response.body();
                            String msg = body.getMessage();
                            if (msg != null && msg.equalsIgnoreCase("Visitor not check-in!!!")) {
                                addBucket(base64Image);
                            } else if (body.isSuccess()
                                    && body.getData() != null
                                    && body.getData().getVisitor() != null) {
                                showAlert("Active Visit", "Visitor is already checked in.");
                            } else {
                                addBucket(base64Image);
                            }
                        } else {
                            handleError("Status Check Failed.");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<VisitorContactResponse> call,
                                          @NonNull Throwable t) {
                        loadingPanel.setVisibility(View.GONE);
                        handleError("Network Error: " + t.getMessage());
                    }
                });
    }

    private void addBucket(String base64Image) {
        String authToken = "jwt " + token;
        AddBucketRequest request = new AddBucketRequest(
                base64Image, "xe-digital-bucket/visitors-profile-images", "");

        AdminAPIClient.getInstance()
                .getBase2()
                .addBucket(authToken, request)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(@NonNull Call<ResponseBody> call,
                                           @NonNull Response<ResponseBody> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            try {
                                JSONObject json = new JSONObject(response.body().string());
                                if (json.optBoolean("success", false)) {
                                    JSONObject data = json.getJSONObject("data");
                                    String imageUrl = data.optString("imageUrl");
                                    String imageKey = data.optString("imageKey");

                                    String url = "https://app.xedigital.ai/checkin/profile?"
                                            + "access_token=" + token
                                            + "&faceId=" + faceId
                                            + "&imageId=" + imageId
                                            + "&profileImagePath=" + Uri.encode(imageUrl)
                                            + "&profileImageKey=" + imageKey
                                            + "&isVisitorNew=true"
                                            + "&isGovernmentIdUpload=false"
                                            + "&isItemImageUpload=false"
                                            + "&time=" + System.currentTimeMillis();

                                    JSONObject payload = new JSONObject();
                                    payload.put("url", url);
                                    getTinyUrl(payload);
                                } else {
                                    showAlert("AddBucket Failed", json.optString("message"));
                                }
                            } catch (IOException | JSONException e) {
                                showAlert("Error", "Failed to parse response.");
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ResponseBody> call,
                                          @NonNull Throwable t) {
                        handleError("Network Error: " + t.getMessage());
                    }
                });
    }

    private void getTinyUrl(JSONObject urlPayload) {
        String authToken = "jwt " + token;
        RequestBody body = RequestBody.create(
                urlPayload.toString(), MediaType.parse("application/json"));

        AdminAPIClient.getInstance()
                .getBase2()
                .getTinyUrl(authToken, body)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(@NonNull Call<ResponseBody> call,
                                           @NonNull Response<ResponseBody> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            try {
                                JSONObject json = new JSONObject(response.body().string());
                                if (json.optBoolean("success")
                                        && json.optInt("statusCode") == 200) {
                                    String tinyUrl = json.optString("data");
                                    runOnUiThread(() -> generateQRCode(tinyUrl));
                                } else {
                                    showAlert("Error", "Invalid response format.");
                                }
                            } catch (IOException | JSONException e) {
                                showAlert("TinyURL Error", "Failed to parse response.");
                            }
                        } else {
                            showAlert("TinyURL Failed", "Response error: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ResponseBody> call,
                                          @NonNull Throwable t) {
                        showAlert("TinyURL Error", t.getMessage());
                    }
                });
    }

    private void generateQRCode(String data) {
        try {
            BarcodeEncoder encoder = new BarcodeEncoder();
            Bitmap bitmap = encoder.encodeBitmap(data, BarcodeFormat.QR_CODE, 400, 400);

            ImageView qrImage = findViewById(R.id.qrCodeImage);
            TextView qrLabel = findViewById(R.id.qrCodeLabel);
            LinearLayout qrLayout = findViewById(R.id.qrLayout);

            qrImage.setImageBitmap(bitmap);
            qrLayout.setVisibility(View.VISIBLE);
            previewView.setVisibility(View.GONE);
            loadingPanel.setVisibility(View.GONE);

            setViewsInvisible(
                    R.id.headerLayout,
                    R.id.cameraCard,
                    R.id.statusModule,
                    R.id.footerText);

            qrCountDownTimer = new CountDownTimer(30000, 1000) {
                public void onTick(long ms) {
                    qrLabel.setText("Please scan the QR Code for Touchless Check-In.\nValid for "
                            + (ms / 1000) + " seconds.");
                }

                public void onFinish() {
                    qrLayout.setVisibility(View.GONE);
                    finish();
                }
            };
            qrCountDownTimer.start();

        } catch (Exception e) {
            showAlert("QR Error", "Failed to generate QR code.");
        }
    }

    // =========================================================
    //  QR CODE
    // =========================================================

    private void setViewsInvisible(int... ids) {
        for (int id : ids) {
            View v = findViewById(id);
            if (v != null) v.setVisibility(View.INVISIBLE);
        }
    }

    private void showAlert(String title, String message) {
        runOnUiThread(() -> {
            if (isFinishing() || isDestroyed()) return;
            new CustomAlertDialog(this)
                    .setType(CustomAlertDialog.Type.INFO)
                    .setTitle(title)
                    .setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton("OK", () -> {
                    })
                    .show();
        });
    }

    // =========================================================
    //  UI HELPERS — now using CustomAlertDialog
    // =========================================================

    private void showCapturingOverlay() {
        runOnUiThread(() -> {
            captureOverlay.setText(getString(R.string.capturing_image));
            captureOverlay.setVisibility(View.VISIBLE);
            AlphaAnimation anim = new AlphaAnimation(0.2f, 1.0f);
            anim.setDuration(800);
            anim.setRepeatMode(Animation.REVERSE);
            anim.setRepeatCount(Animation.INFINITE);
            captureOverlay.startAnimation(anim);
        });
    }

    private void hideCapturingOverlay() {
        runOnUiThread(() -> {
            captureOverlay.clearAnimation();
            captureOverlay.setVisibility(View.GONE);
        });
    }

    private void showAttendanceSuccessAlert(String responseBody) {
        loadingPanel.setVisibility(View.GONE);
        try {
            JSONObject json = new JSONObject(responseBody);
            boolean success = json.getBoolean("success");
            String message = json.getString("message");

            if (!success) return;

            JSONObject data = json.getJSONObject("data");
            String punchIn = data.getString("punchInAddress");
            String punchOut = data.getString("punchOutAddress");
            String address = punchOut.isEmpty() ? punchIn : punchOut;

            String fullMessage = message + "\n\nAddress: " + address;

            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) return;

                if (activeDialog != null && activeDialog.isShowing()) {
                    activeDialog.dismiss();
                }

                CustomAlertDialog customAlert = new CustomAlertDialog(this)
                        .setType(CustomAlertDialog.Type.SUCCESS)
                        .setTitle("Attendance Success")
                        .setMessage(fullMessage)
                        .setCancelable(false)
                        .setPositiveButton("OK", () -> {
                            setResult(Activity.RESULT_OK);
                            finish();
                        });

                activeDialog = customAlert.show();

                new Handler().postDelayed(() -> {
                    if (!isFinishing() && !isDestroyed()) {
                        if (activeDialog != null && activeDialog.isShowing()) {
                            activeDialog.dismiss();
                        }
                        finish();
                    }
                }, 5000);
            });
        } catch (JSONException e) {
            showAttendanceFailedAlert("Error parsing attendance response.");
        }
    }

    private void showAttendanceFailedAlert(String message) {
        loadingPanel.setVisibility(View.GONE);
        runOnUiThread(() -> {
            if (isFinishing() || isDestroyed()) return;
            new CustomAlertDialog(this)
                    .setType(CustomAlertDialog.Type.ERROR)
                    .setTitle("Attendance Failed")
                    .setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton("RETRY", () -> {
                        isCameraStarted.set(false);
                        isCaptureInProgress.set(false);
                        isImageSaved.set(false);
                        startCamera();
                    })
                    .setNegativeButton("CANCEL", () -> {
                        setResult(Activity.RESULT_CANCELED);
                        finish();
                    })
                    .show();
        });
    }

//    private void showAttendanceSuccessAlert(String responseBody) {
//        loadingPanel.setVisibility(View.GONE);
//        try {
//            JSONObject json = new JSONObject(responseBody);
//            boolean success = json.getBoolean("success");
//            String message = json.getString("message");
//
//            if (!success) return;
//
//            JSONObject data = json.getJSONObject("data");
//            String punchIn = data.getString("punchInAddress");
//            String punchOut = data.getString("punchOutAddress");
//            String address = punchOut.isEmpty() ? punchIn : punchOut;
//
//            String fullMessage = message + "\n\nAddress: " + address;
//
//            runOnUiThread(() -> {
//                if (isFinishing() || isDestroyed()) return;
//
//                new CustomAlertDialog(this)
//                        .setType(CustomAlertDialog.Type.SUCCESS)
//                        .setTitle("Attendance Success")
//                        .setMessage(fullMessage)
//                        .setCancelable(false)
//                        .setPositiveButton("OK", () -> {
//                            setResult(Activity.RESULT_OK);
//                            finish();
//                        })
//                        .show();
//
//                new Handler().postDelayed(() -> {
//                    if (!isFinishing()) finish();
//                }, 5000);
//            });
//        } catch (JSONException e) {
//            showAttendanceFailedAlert("Error parsing attendance response.");
//        }
//    }

    private void handleError(String message) {
        runOnUiThread(() -> {
            if (isFinishing() || isDestroyed()) return;
            loadingPanel.setVisibility(View.GONE);

            String display = message.contains("There are no faces in the image")
                    ? "No faces detected. Please ensure your face is clearly visible and try again."
                    : message;

            if (cameraProvider != null) cameraProvider.unbindAll();
            camera = null;

            new CustomAlertDialog(this)
                    .setType(CustomAlertDialog.Type.ERROR)
                    .setTitle("Error")
                    .setMessage(display)
                    .setCancelable(false)
                    .setPositiveButton("RETRY", () -> {
                        isCameraStarted.set(false);
                        isCaptureInProgress.set(false);
                        isImageSaved.set(false);
                        startCamera();
                    })
                    .setNegativeButton("CANCEL", () -> {
                        setResult(Activity.RESULT_CANCELED);
                        finish();
                    })
                    .show();
        });
    }

    private void getCurrentLocation(AddressCallback callback) {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean locationEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED && locationEnabled) {

            LocationRequest req = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
                    .setMinUpdateIntervalMillis(5000)
                    .setWaitForAccurateLocation(false)
                    .setMaxUpdateDelayMillis(15000)
                    .build();

            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(@NonNull LocationResult result) {
                    Location loc = result.getLastLocation();
                    if (loc != null) {
                        getAddressFromLocation(loc.getLatitude(), loc.getLongitude(),
                                (addr, l) -> callback.onAddressReceived(addr, loc));
                        fusedLocationClient.removeLocationUpdates(this);
                    } else {
                        callback.onAddressReceived("Location not found", null);
                    }
                }
            };
            fusedLocationClient.requestLocationUpdates(req, locationCallback, null);

        } else {
            if (!locationEnabled) {
                new CustomAlertDialog(this)
                        .setType(CustomAlertDialog.Type.WARNING)
                        .setTitle("Location Services Disabled")
                        .setMessage("Please enable location services to use this feature.")
                        .setCancelable(false)
                        .setPositiveButton("OK", () -> {
                            setResult(Activity.RESULT_CANCELED);
                            finish();
                        })
                        .show();
            } else {
                callback.onAddressReceived("Location not found", null);
            }
        }
    }

    // =========================================================
    //  LOCATION
    // =========================================================

    private void getAddressFromLocation(double lat, double lng, AddressCallback callback) {
        new Thread(() -> {
            Geocoder geocoder = new Geocoder(AdminPunchActivity.this, Locale.getDefault());
            Address address = null;
            int retryCount = 0;

            while (retryCount < 2) {
                try {
                    List<Address> list = geocoder.getFromLocation(lat, lng, 1);
                    if (list != null && !list.isEmpty()) {
                        address = list.get(0);
                        break;
                    }
                } catch (IOException e) {
                    retryCount++;
                    try {
                        Thread.sleep((long) (Math.pow(2, retryCount) * 1000));
                    } catch (InterruptedException ie) {
                        ie.printStackTrace();
                        break;
                    }
                }
            }

            String result;
            if (address != null) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                    sb.append(address.getAddressLine(i)).append("\n");
                }
                result = sb.toString();
            } else {
                result = "Location not found";
                runOnUiThread(() -> Toast.makeText(AdminPunchActivity.this,
                        "No address found for location", Toast.LENGTH_SHORT).show());
            }

            final String finalResult = result;
            runOnUiThread(() -> {
                currentAddress = finalResult;
                Location tmp = new Location("serviceProvider");
                tmp.setLatitude(lat);
                tmp.setLongitude(lng);
                tmp.setTime(System.currentTimeMillis());
                callback.onAddressReceived(finalResult, tmp);
            });
        }).start();
    }

    private String getCurrentTime(Location location) {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        fmt.setTimeZone(TimeZone.getTimeZone("UTC"));
        return fmt.format(location != null ? new Date(location.getTime()) : new Date());
    }

    private String convertImageToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
        return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
    }

    private File getOutputDirectory() {
        File dir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "AdminPunch");
        if (!dir.exists() && !dir.mkdirs()) {
            Log.e(TAG, "Failed to create output directory");
        }
        return dir;
    }



    // =========================================================
    //  INTERFACE
    // =========================================================

    interface AddressCallback {
        void onAddressReceived(String address, Location location);
    }
}
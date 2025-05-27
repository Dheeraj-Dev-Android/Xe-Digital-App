package app.xedigital.ai.adminActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import app.xedigital.ai.R;
import app.xedigital.ai.adminApi.AdminAPIClient;
import app.xedigital.ai.adminApi.AdminAPIInterface;
import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.api.APIInterface;
import app.xedigital.ai.model.Admin.addBucket.AddBucketRequest;
import app.xedigital.ai.model.Admin.visitorFace.VisitorFaceResponse;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminPunchActivity extends AppCompatActivity {

    private final Handler handler = new Handler();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final String[] REQUIRED_PERMISSIONS = new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION};
    private final String CollectionName = "cloudfencedemo_wr8c2p";
    private PreviewView previewView;
    private TextView captureOverlay;
    private ProcessCameraProvider cameraProvider;
    private ImageCapture imageCapture;
    private AlertDialog attendanceSuccessDialog;
    private Camera camera;
    private String currentAddress = "";
    private FragmentManager fragmentManager;
    private LocationCallback locationCallback;
    private FusedLocationProviderClient fusedLocationClient;
    private ExecutorService cameraExecutor;
    private String token;
    private String base64Image;
    private String faceId, imageId;
    private MaterialCardView progressBar;
    private CountDownTimer qrCountDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_punch_activity);
        SharedPreferences sharedPreferences = getSharedPreferences("AdminCred", MODE_PRIVATE);
        token = sharedPreferences.getString("authToken", "");
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        previewView = findViewById(R.id.previewView);
        captureOverlay = findViewById(R.id.capture_overlay);
        progressBar = findViewById(R.id.loadingPanel);
        cameraExecutor = Executors.newSingleThreadExecutor();

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

//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
//            startCamera();
//        } else {
//            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
//        }

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
        int REQUEST_CODE_PERMISSIONS = 10;
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera();
            }
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                imageCapture = new ImageCapture.Builder().build();
                CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_FRONT).build();

                preview.setSurfaceProvider(previewView.getSurfaceProvider());
                cameraProvider.unbindAll();
                camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
//                try {
//                    camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
//                    if (camera == null) {
//                        Log.e("AdminPunchActivity", "Camera is null");
//                        showRetryAlert();
//                    } else {
//                        showCapturingOverlay();
//                        handler.postDelayed(captureRunnable, 3000);
//                    }
//
//                } catch (IllegalArgumentException e) {
//                    Log.e("AdminPunchActivity", "Error binding camera: " + e.getMessage(), e);
//                    showRetryAlert();
//                }
                showCapturingOverlay();
                handler.postDelayed(captureRunnable, 3000);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void showRetryAlert() {
        new MaterialAlertDialogBuilder(this).setTitle("Camera Not Found").setMessage("No camera found or selected. Please check your device and try again.").setPositiveButton("Retry", (dialog, which) -> {
            dialog.dismiss();
            startCamera();
        }).setNegativeButton("Cancel", (dialog, which) -> {
            dialog.dismiss();
            setResult(Activity.RESULT_CANCELED);
            finish();
        }).show();
    }


    private void captureImage() {
        File photoFile = new File(getOutputDirectory(), System.currentTimeMillis() + "_photo.jpg");
        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                String savedUri = Uri.fromFile(photoFile).toString();
                LayoutInflater inflater = getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.image_alert_dialog, null);
                ImageView imageView = dialogView.findViewById(R.id.capturedImage);

                Glide.with(AdminPunchActivity.this).load(savedUri).into(imageView);

                progressBar.setVisibility(View.VISIBLE);

                String savedImagePath = photoFile.getAbsolutePath();
                try {
                    ProcessCameraProvider cameraProvider = ProcessCameraProvider.getInstance(AdminPunchActivity.this).get();
                    cameraProvider.unbindAll();
                } catch (ExecutionException | InterruptedException e) {
                    Log.e("AdminPunchActivity", "Error unbinding camera preview: " + e.getMessage(), e);
                }

                try {
                    AtomicReference<Bitmap> bitmap;
                    bitmap = new AtomicReference<>(BitmapFactory.decodeFile(savedImagePath));

                    try {
                        int newWidth = 500;
                        int newHeight = (int) (bitmap.get().getHeight() * (newWidth / (float) bitmap.get().getWidth()));
                        bitmap.set(Bitmap.createScaledBitmap(bitmap.get(), newWidth, newHeight, false));

                        base64Image = convertImageToBase64(bitmap.get());
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("collection_name", CollectionName);
                        jsonObject.put("image", base64Image);

                        String requestBodyJson = jsonObject.toString();
                        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), requestBodyJson);
//                   API CALL
                        sendToAPI(requestBody);
                    } catch (Exception e) {
                        Log.e("AdminPunchActivity", "Error processing image: " + e.getMessage(), e);
                        handleError("Error processing image: " + e.getMessage());
                    }
                } catch (Exception e) {
                    Log.e("AdminPunchActivity", "Error during face detection: " + e.getMessage(), e);
                    handleError("Error detecting faces. Please try again.");
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Log.e("AdminPunchActivity", "Photo capture failed: " + exception.getMessage(), exception);
                handleError("Photo capture failed: " + exception.getMessage());
            }
        });
    }

    private void sendToAPI(RequestBody requestBody) {
        AdminAPIInterface apiService = AdminAPIClient.getInstance().getBase1();
        String authToken = "jwt " + token;
        // Make the API call
        Call<ResponseBody> call = apiService.recognizeFace(authToken, requestBody);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                hideCapturingOverlay();
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseBody = response.body().string();
                        String responseJson = gson.toJson(responseBody);
                        Log.d("AdminPunchActivity", "Recognize Response Body:\n " + responseJson);
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        // Check if "data" exists and is not null
                        if (jsonResponse.has("data") && !jsonResponse.isNull("data")) {
                            JSONObject dataObject = jsonResponse.getJSONObject("data");

                            // Extracting Face object from data
                            if (dataObject.has("Face") && !dataObject.isNull("Face")) {
                                JSONObject faceObject = dataObject.getJSONObject("Face");

                                String faceId = faceObject.optString("FaceId", "N/A");
                                String imageId = faceObject.optString("ImageId", "N/A");

                            } else {
                                Log.e("AdminPunchActivity", "'Face' object not found in 'data'");
                            }

                            String requestBodyFace = dataObject.toString();
                            RequestBody FaceDetails = RequestBody.create(MediaType.parse("application/json"), requestBodyFace);
                            // API CALL
                            callFaceDetailApi(FaceDetails);
                        } else {
                            Log.e("AdminPunchActivity", "Null or missing 'data' in API response: " + responseBody);
                            Toast.makeText(AdminPunchActivity.this, "No face data found in response.", Toast.LENGTH_SHORT).show();
                            handleError("No face found.");
                        }
                    } catch (JSONException | IOException e) {
                        throw new RuntimeException(e);
                    }

                } else {
                    // Get more detailed error information from the response
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (IOException e) {
                        Log.e("AdminPunchActivity", "Error reading error body: " + e.getMessage(), e);
                    }
                    Log.e("AdminPunchActivity", "Recognize Response Error: " + response.code() + " - " + response.message() + "\nError Body: " + errorBody);
                    // Handle the error based on the response code and error body
                    handleError("Server Error: " + response.code() + " - " + response.message() + "\nDetails: " + errorBody);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                hideCapturingOverlay();
                // Handle network or other errors
                handleError("Network Error: " + t.getMessage());
            }
        });

    }

    private void callFaceDetailApi(RequestBody FaceDetails) {
        Log.d("AdminPunchActivity", "Face Detail API Called");
        String authToken = "jwt " + token;
        AdminAPIInterface faceApiService = AdminAPIClient.getInstance().getBase2();
        retrofit2.Call<ResponseBody> faceDetails = faceApiService.FaceDetails(authToken, FaceDetails);

        faceDetails.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseBody = response.body().string();
                        JSONObject jsonResponse = new JSONObject(responseBody);

                        int statusCode = jsonResponse.optInt("statusCode", 0);
                        String message = jsonResponse.optString("message", "");

                        // Check for specific message and null data
                        if (jsonResponse.has("data") && jsonResponse.isNull("data")) {
                            Log.d("AdminPunchActivity", "Face not found or data missing, calling Visitor Face API...");
                            callVisitorFace(FaceDetails);
                            return;
                        }

                        // Proceed normally if data exists
                        if (jsonResponse.has("data") && !jsonResponse.isNull("data")) {
                            JSONObject dataObject = jsonResponse.getJSONObject("data");
                            JSONObject employeeObject = dataObject.getJSONObject("employee");
                            String id = employeeObject.getString("_id");
                            String firstName = employeeObject.getString("firstname");
                            if (id != null) {
                                callAttendanceApi(id, firstName);
                            } else {
                                showAttendanceFailedAlert("Attendance failed: User not Found.");
                            }
                        } else {
                            Log.e("AdminPunchActivity", "Face data not found in response.");
                            showAttendanceFailedAlert("Attendance failed: Face not found or matched.");
                        }
                    } catch (IOException | JSONException e) {
                        Log.e("AdminPunchActivity", "Error processing face detail response: " + e.getMessage(), e);
                        showAttendanceFailedAlert("Attendance failed: An error occurred.");
                    }
                } else {
                    Log.e("AdminPunchActivity", "Face Detail Response Error: " + response.code() + " - " + response.message());
                    showAttendanceFailedAlert("Error processing face detail");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
                Log.e("AdminPunchActivity", "Face Detail Error: " + throwable.getMessage(), throwable);
                showAttendanceFailedAlert("Error processing face detail.");
            }
        });
    }

    private void callAttendanceApi(String employeeId, String employeeName) {
        if (employeeId != null && !employeeId.isEmpty()) {
            Log.d("AdminPunchActivity", "Attendance API Called");

            getCurrentLocation(address -> {
                currentAddress = address;
                String currentTime = getCurrentTime();
                try {
                    JSONObject requestBody = new JSONObject();
                    requestBody.put("employee", employeeId);
                    requestBody.put("employeeName", employeeName);
                    requestBody.put("address", currentAddress);
                    requestBody.put("punchTime", currentTime);

                    String requestBodyString = requestBody.toString();
                    RequestBody requestBodyAttendance = RequestBody.create(MediaType.parse("application/json"), requestBodyString);
                    String authToken = "jwt " + token;
                    APIInterface attendanceApiService = APIClient.getInstance().getAttendance();
                    Call<ResponseBody> attendance = attendanceApiService.AttendanceApi(authToken, requestBodyAttendance);

                    attendance.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                            ProgressBar progressBar = findViewById(R.id.progressBar);
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    String responseBody = response.body().string();
                                    JSONObject responseJson = new JSONObject(responseBody);
                                    String message = responseJson.getString("message");
                                    Log.d("AdminPunchActivity", "Attendance Response Message: " + message);
                                    showAttendanceSuccessAlert(responseBody);
                                    progressBar.setVisibility(View.GONE);

                                    String responseJsonn = gson.toJson(responseBody);
                                    Log.d("AdminPunchActivity", "Attendance Response Body:\n" + responseJsonn);
                                } catch (IOException | JSONException e) {
                                    progressBar.setVisibility(View.GONE);
                                    Log.e("AdminPunchActivity", "Error reading response body: " + e.getMessage(), e);
                                    throw new RuntimeException(e);
                                }
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
                            Log.e("AdminPunchActivity", "Attendance Error: " + throwable.getMessage(), throwable);
                            progressBar.setVisibility(View.GONE);
                            throw new RuntimeException(throwable);
                        }
                    });
                } catch (JSONException e) {
                    Log.e("AdminPunchActivity", "Error creating request body: " + e.getMessage(), e);
                    progressBar.setVisibility(View.GONE);
                    throw new RuntimeException(e);
                }
            });
        } else {
            Log.e("AdminPunchActivity", "User Id Mismatch");
            showAttendanceFailedAlert("Attendance failed: User not found.");
        }
    }

    private void callVisitorFace(RequestBody requestBodyFacee) {
        Log.d("AdminPunchActivity", "Visitor Face API Called");
        String authToken = "jwt " + token;
        AdminAPIInterface faceApiService = AdminAPIClient.getInstance().getBase2();
        retrofit2.Call<VisitorFaceResponse> faceDetails = faceApiService.FaceDetailsVisitor(authToken, requestBodyFacee);

        faceDetails.enqueue(new Callback<VisitorFaceResponse>() {
            @Override
            public void onResponse(@NonNull Call<VisitorFaceResponse> call, @NonNull Response<VisitorFaceResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        VisitorFaceResponse responseBody = response.body();
//                        JSONObject jsonResponse = new JSONObject(String.valueOf(responseBody));

                        // Convert POJO to JSON string
                        String responseJson = gson.toJson(responseBody);

                        // Parse that string to a JSONObject
                        JSONObject jsonResponse = new JSONObject(responseJson);
                        // Check if "data" key exists and is not null before proceeding
                        if (!jsonResponse.has("data") || jsonResponse.isNull("data")) {
                            Log.e("AdminPunchActivity", "Face data not found in response.");
                            addBucket(base64Image);
                            return;
                        }
                        if (jsonResponse.has("data") && !jsonResponse.isNull("data")) {
                            JSONObject dataObject = jsonResponse.getJSONObject("data");

//                            String responseJson = gson.toJson(responseBody);
//                            matchVisitor();
                            Log.d("AdminPunchActivity", "Face Detail Response Body:\n" + responseJson);


                        } else {
                            Log.e("AdminPunchActivity", "Face data not found in response.");
                        }
                    } catch (JSONException e) {
                        Log.e("AdminPunchActivity", "Error processing face detail response: " + e.getMessage(), e);
                    }
                } else {
                    Log.e("AdminPunchActivity", "Face Detail Response Error: " + response.code() + " - " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<VisitorFaceResponse> call, @NonNull Throwable throwable) {
                Log.e("AdminPunchActivity", "Face Detail Error: " + throwable.getMessage(), throwable);
            }
        });

    }

    private void addBucket(String base64Image) {
        Log.d("AdminPunchActivity", "Calling Add Bucket API");

        AddBucketRequest request = new AddBucketRequest(base64Image, "visitors-profile-images", "");
        String authToken = "jwt " + token;
        AdminAPIInterface apiService = AdminAPIClient.getInstance().getBase2();
        retrofit2.Call<ResponseBody> call = apiService.addBucket(authToken, request);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseBody = response.body().string();
                        JSONObject jsonResponse = new JSONObject(responseBody);


                        if (jsonResponse.optBoolean("success", false)) {
                            JSONObject data = jsonResponse.getJSONObject("data");
                            String imageUrl = data.optString("imageUrl");
                            String imageKey = data.optString("imageKey");


                            // IMPORTANT: build URL with proper encoding & params
                            String url = "https://app.xedigital.ai/checkin/profile?" + "access_token=" + token + "&faceId=" + faceId + "&imageId=" + imageId + "&profileImagePath=" + Uri.encode(imageUrl) + "&profileImageKey=" + imageKey + "&isVisitorNew=true" + "&isGovernmentIdUpload=false" + "&isItemImageUpload=false" + "&time=" + System.currentTimeMillis();

                            JSONObject urlPayload = new JSONObject();
                            urlPayload.put("url", url);

                            getTinyUrl(urlPayload);

                        } else {
                            showAlert("AddBucket Failed", jsonResponse.optString("message"));
                        }

                        // Convert JSON to pretty printed string for better readability
                        String prettyJson = jsonResponse.toString(4); // 4 = number of spaces for indent
//                        showAlert("API Response", prettyJson);

                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                        showAlert("Error", "Failed to parse response.");
                    }

                    Log.d("AdminPunchActivity", "Add Bucket API Success");
                } else {
                    Log.e("AdminPunchActivity", "Add Bucket API Failed: " + response.code() + " - " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e("AdminPunchActivity", "Add Bucket API Error: " + t.getMessage(), t);
            }
        });

    }

    private void getTinyUrl(JSONObject urlPayload) {

        RequestBody body = RequestBody.create(urlPayload.toString(), MediaType.parse("application/json"));
        String authToken = "jwt " + token;

        AdminAPIInterface apiService = AdminAPIClient.getInstance().getBase2();
        apiService.getTinyUrl(authToken, body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String resp = response.body().string();
                        JSONObject jsonObject = new JSONObject(resp);

                        // Check if success is true and status code is 200
                        if (jsonObject.optBoolean("success") && jsonObject.optInt("statusCode") == 200) {
                            String tinyUrl = jsonObject.optString("data");

                            // Optional: Show alert with the URL
//                            showAlert("Short URL", tinyUrl);
                            Log.d("QR_DEBUG", "Tiny URL received: " + tinyUrl);

                            // Generate QR Code from the URL
//                            generateQRCode(tinyUrl);
                            runOnUiThread(() -> {
                                generateQRCode(tinyUrl);
                            });

                        } else {
                            showAlert("Error", "Invalid response format or data missing.");
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                        showAlert("Next API Error", "Failed to read response");
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    showAlert("Next API Failed", "Response error: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                showAlert("Next API Error", t.getMessage());
            }
        });
    }

    private void generateQRCode(String data) {
        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.encodeBitmap(data, BarcodeFormat.QR_CODE, 400, 400);

            ImageView qrCodeImage = findViewById(R.id.qrCodeImage);
            TextView qrCodeLabel = findViewById(R.id.qrCodeLabel);
            LinearLayout qrLayout = findViewById(R.id.qrLayout);
            PreviewView previewView = findViewById(R.id.previewView);
            MaterialCardView loadingPanel = findViewById(R.id.loadingPanel);

            qrCodeImage.setImageBitmap(bitmap);
            qrLayout.setVisibility(View.VISIBLE);
            previewView.setVisibility(View.GONE);
            loadingPanel.setVisibility(View.GONE);

            // 🔁 Start countdown from 20 seconds and update label dynamically
            qrCountDownTimer = new CountDownTimer(30000, 1000) {
                public void onTick(long millisUntilFinished) {
                    long secondsLeft = millisUntilFinished / 1000;
                    String countdownText = "Please scan the QR Code from your device for Touch less Check-In.\nThe barcode will be valid for " + secondsLeft + " seconds.";
                    qrCodeLabel.setText(countdownText);
                }

                public void onFinish() {
                    qrLayout.setVisibility(View.GONE);

                    // ✅ Return to previous activity
                    finish();
                }
            };

            qrCountDownTimer.start();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("QR Error", "Failed to generate QR code.");
        }
    }


    private void showAlert(String title, String message) {
        runOnUiThread(() -> {
            if (isFinishing() || isDestroyed()) return;

            if (cameraProvider != null) {
                cameraProvider.unbindAll();
            }

            new AlertDialog.Builder(AdminPunchActivity.this).setTitle(title).setMessage(message).setCancelable(false).setPositiveButton("OK", (dialog, which) -> {
                dialog.dismiss();
//                navigateToDashboard();
            }).show();
        });
    }

    private void showCapturingOverlay() {
        runOnUiThread(() -> {
            captureOverlay.setVisibility(TextView.VISIBLE);
            Animation fadeAnim = new AlphaAnimation(0.2f, 1.0f);
            fadeAnim.setDuration(800);
            fadeAnim.setRepeatMode(Animation.REVERSE);
            fadeAnim.setRepeatCount(Animation.INFINITE);
            captureOverlay.startAnimation(fadeAnim);
        });
    }

    private void hideCapturingOverlay() {
        runOnUiThread(() -> {
            captureOverlay.clearAnimation();
            captureOverlay.setVisibility(TextView.GONE);
        });
    }

//    private void navigateToDashboard() {
//        Intent intent = new Intent(AdminPunchActivity.this, AdminDashboardActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//        startActivity(intent);
//        finish();
//    }

    private void showAttendanceSuccessAlert(String responseBody) {
        progressBar.setVisibility(View.GONE);
        try {
            JSONObject responseJson = new JSONObject(responseBody);
            boolean success = responseJson.getBoolean("success");
            String message = responseJson.getString("message");
            if (success) {
                JSONObject data = responseJson.getJSONObject("data");
                Log.d("AdminPunchActivity", "Attendance Data: " + data);
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

                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) {
                        return;
                    }

                    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(AdminPunchActivity.this);
                    attendanceSuccessDialog = builder.setTitle("Attendance Success").setMessage(Html.fromHtml(formattedMessage.toString(), Html.FROM_HTML_MODE_LEGACY)).setPositiveButton("OK", (dialog1, which) -> {
                        dialog1.dismiss();
                        setResult(Activity.RESULT_OK);
                        finish();
                    }).show();
                    new Handler().postDelayed(() -> {
                        if (isFinishing()) {
                            return;
                        }
                        finish();

                        if (attendanceSuccessDialog != null && attendanceSuccessDialog.isShowing()) {
                            attendanceSuccessDialog.dismiss();
                        }
                    }, 5000);
                });
            }
        } catch (JSONException e) {
            Log.e("AdminPunchActivity", "Error parsing JSON response", e);
            throw new RuntimeException(e);
        }
    }

    private void showAttendanceFailedAlert(String message) {
        progressBar.setVisibility(View.GONE);
        runOnUiThread(() -> {
            if (isFinishing() || isDestroyed()) {
                return;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(AdminPunchActivity.this);
            builder.setTitle("Attendance Failed").setMessage(message).setPositiveButton("Retry", (dialog, id) -> {
                dialog.dismiss();
                startCamera();
            }).setNegativeButton("Cancel", (dialog, id) -> {
                dialog.dismiss();
                setResult(Activity.RESULT_CANCELED);
                finish();
            }).create().show();

        });
    }

    private void getCurrentLocation(AdminPunchActivity.AddressCallback callback) {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean isLocationEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && isLocationEnabled) {
            // Location permission granted and location services enabled
            LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000).setMinUpdateIntervalMillis(5000).setWaitForAccurateLocation(false).setMaxUpdateDelayMillis(15000).build();
            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(@NonNull LocationResult locationResult) {
                    Location location = locationResult.getLastLocation();
                    if (location != null) {
                        getAddressFromLocation(location.getLatitude(), location.getLongitude(), callback);
                        Log.d("AdminPunchActivity", "Location: " + location.getLatitude() + ", " + location.getLongitude());
                    } else {
                        Log.e("AdminPunchActivity", "Location is null in onLocationResult");
                        callback.onAddressReceived("Location not found");
                    }
                }
            };
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        } else {
            // Location permission or location services not enabled
            if (!isLocationEnabled) {
                // Show alert to enable location services
                new AlertDialog.Builder(this).setTitle("Location Services Disabled").setMessage("Please enable location services to use this feature.").setPositiveButton("Ok", (dialog, which) -> {
//                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
//                }).setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.dismiss();
                    setResult(Activity.RESULT_CANCELED);
                    finish();
                }).show();
            } else {
                Log.e("AdminPunchActivity", "Location permission not granted");
                callback.onAddressReceived("Location not found");
            }
        }
    }

    private void getAddressFromLocation(final double latitude, final double longitude, final AdminPunchActivity.AddressCallback callback) {
        new Thread(() -> {
            Geocoder geocoder = new Geocoder(AdminPunchActivity.this, Locale.getDefault());
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
                    Log.e("AdminPunchActivity", "Error getting address:", e);
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
                Log.e("AdminPunchActivity", "No address found for location or max retries reached");
                Toast.makeText(AdminPunchActivity.this, "No address found for location", Toast.LENGTH_SHORT).show();
            }

            runOnUiThread(() -> {
                currentAddress = addressResult;
                callback.onAddressReceived(addressResult);
            });
        }).start();
    }

    private String getCurrentTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(new Date());
    }

    private String convertImageToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] imageBytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    private File getOutputDirectory() {
        File mediaDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Punch");
        if (!mediaDir.exists()) {
            if (!mediaDir.mkdirs()) {
                Log.e("AdminPunchActivity", "Failed to create directory");
            }
        }
        return mediaDir;
    }

    private void handleError(String errorMessage) {
        runOnUiThread(() -> {
            if (isFinishing() || isDestroyed()) {
                return;
            }

            AlertDialog alertDialog = new AlertDialog.Builder(this).setTitle("Error").setMessage(errorMessage.contains("There are no faces in the image") ? "No faces detected in the image. Please try again with a clear image showing your face." : errorMessage).setPositiveButton("Retry", null).setNegativeButton("Cancel", (dialog, which) -> {
                dialog.dismiss();
                setResult(Activity.RESULT_CANCELED);
                if (!isFinishing()) {
                    try {
                        ProcessCameraProvider cameraProvider = ProcessCameraProvider.getInstance(AdminPunchActivity.this).get();
                        cameraProvider.unbindAll();
                    } catch (ExecutionException | InterruptedException e) {
                        Log.e("AdminPunchActivity", "Error unbinding camera preview: " + e.getMessage(), e);
                    }
                    finish();
                }
            }).create();

            alertDialog.setOnShowListener(dialog -> {
                Button retryButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                retryButton.setOnClickListener(view -> {
                    alertDialog.dismiss();
                    startCamera();
                });
            });

            // Unbind camera preview (if bound)
            try {
                ProcessCameraProvider cameraProvider = ProcessCameraProvider.getInstance(AdminPunchActivity.this).get();
                cameraProvider.unbindAll();
            } catch (ExecutionException | InterruptedException e) {
                Log.e("AdminPunchActivity", "Error unbinding camera preview: " + e.getMessage(), e);
            }

            alertDialog.show();
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraProvider != null) cameraProvider.unbindAll();
        handler.removeCallbacks(captureRunnable);
        if (cameraExecutor != null) cameraExecutor.shutdown();

        if (locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
        // Dismiss the dialog if it's showing
        if (attendanceSuccessDialog != null && attendanceSuccessDialog.isShowing()) {
            attendanceSuccessDialog.dismiss();
        }
        if (qrCountDownTimer != null) {
            qrCountDownTimer.cancel();
        }
    }

    interface AddressCallback {
        void onAddressReceived(String address);
    }

    private final Runnable captureRunnable = this::captureImage;


//    private void showPermissionDeniedDialog() {
//        if (isFinishing() || isDestroyed()) return;
//        new AlertDialog.Builder(this).setTitle("Camera Permission Needed").setMessage("Camera permission is required to use this feature.").setPositiveButton("OK", (dialog, which) -> finish()).setCancelable(false).show();
//    }


//    private final ActivityResultLauncher<String> cameraPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
//        if (isGranted) startCamera();
//        else showPermissionDeniedDialog();
//    });


}

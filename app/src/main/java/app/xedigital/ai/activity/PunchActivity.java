package app.xedigital.ai.activity;

import android.Manifest;
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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraInfo;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import app.xedigital.ai.R;
import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.api.APIInterface;
import app.xedigital.ai.ui.dashboard.DashboardFragment;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PunchActivity extends AppCompatActivity {
    private static final String TAG = "PunchActivity";
    private final String[] REQUIRED_PERMISSIONS = new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION};
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private Preview preview;
    private ImageCapture imageCapture;
    private CameraSelector cameraSelector;
    private String authToken, userId;
    private FusedLocationProviderClient fusedLocationClient;
    private String currentAddress = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_punch);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

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

//        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
//            new AlertDialog.Builder(this).setTitle("Camera Permission Required").setMessage("This app needs access to the camera to capture your punch.").setPositiveButton("OK", (dialog, which) -> requestPermissionsLauncher.launch(REQUIRED_PERMISSIONS)).setNegativeButton("Cancel", null).show();
//        } else {
//            requestPermissionsLauncher.launch(REQUIRED_PERMISSIONS);
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

    private void startCamera() {
        Log.d(TAG, "Camera Started");
        PreviewView viewFinder = findViewById(R.id.viewFinder);
        CameraManager cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                cameraProvider.unbindAll();
                preview.setSurfaceProvider(viewFinder.getSurfaceProvider());

                cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_FRONT).build();
                try {
                    Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
                    if (camera == null) {
                        Log.e(TAG, "Camera is null");
                        showRetryAlert();
                        return;
                    }

                    CameraInfo cameraInfo = camera.getCameraInfo();
                    Log.d(TAG, "Camera Info: " + cameraInfo);
                    Log.d(TAG, "Camera State: " + cameraInfo.getCameraState());
                    TextView captureText = findViewById(R.id.CaptureText);

                    new CountDownTimer(3000, 1000) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            captureText.setVisibility(View.VISIBLE);
                            captureText.setText("Capturing in: " + millisUntilFinished / 1000 + "seconds");
                        }

                        @Override
                        public void onFinish() {
                            Toast.makeText(PunchActivity.this, "Captured", Toast.LENGTH_SHORT).show();
                            Log.i(TAG, "Captured");
                            captureImage();
                            captureText.setEnabled(true);
                            captureText.setText("Capture");
                            captureText.setVisibility(View.GONE);
                        }
                    }.start();
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "Error binding camera: " + e.getMessage(), e);
                    showRetryAlert();
                }

            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Error starting camera: " + e.getMessage(), e);
            }
        }, ContextCompat.getMainExecutor(this));
    }


    private void showRetryAlert() {
        new MaterialAlertDialogBuilder(this).setTitle("Camera Not Found").setMessage("No camera found or selected. Please check your device and try again.").setPositiveButton("Retry", (dialog, which) -> {
            startCamera();
        }).setNegativeButton("Cancel", (dialog, which) -> {
            dialog.dismiss();
            Navigation.findNavController(this, R.id.nav_host_fragment_content_main).navigate(R.id.nav_dashboard);
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

                Glide.with(PunchActivity.this).load(savedUri).into(imageView);
                ProgressBar progressBar = findViewById(R.id.progressBar);
                progressBar.setVisibility(View.VISIBLE);

                String savedImagePath = photoFile.getAbsolutePath();
                try {
                    ProcessCameraProvider cameraProvider = ProcessCameraProvider.getInstance(PunchActivity.this).get();
                    cameraProvider.unbind(preview);
                } catch (ExecutionException | InterruptedException e) {
                    Log.e(TAG, "Error unbinding camera preview: " + e.getMessage(), e);
                }

                try {
                    Bitmap bitmap = BitmapFactory.decodeFile(savedImagePath);
                    int newWidth = 500;
                    int newHeight = (int) (bitmap.getHeight() * (newWidth / (float) bitmap.getWidth()));
                    bitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, false);

                    String base64Image = convertImageToBase64(bitmap);
                    if (base64Image.length() >= 10) {
                        Log.d(TAG, "Base64 Image: " + base64Image.substring(0, 10) + "...");
                    }
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("collection_name", "consultedgeglobalpvtltd_5e970n");
                    jsonObject.put("image", base64Image);

                    String requestBodyJson = jsonObject.toString();
                    RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), requestBodyJson);
//                   API CALL
                    sendImageToApi(requestBody);
                } catch (Exception e) {
                    Log.e(TAG, "Error processing image: " + e.getMessage(), e);
                }
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Log.e(TAG, "Photo capture failed: " + exception.getMessage(), exception);
            }
        });
    }

    //    API TO SEND IMAGE TO DB
    private void sendImageToApi(RequestBody requestBody) {
        Log.d(TAG, "Recognize API Called");
        APIInterface imageApiService = APIClient.getInstance().getImage();
        retrofit2.Call<ResponseBody> recognize = imageApiService.FaceRecognitionApi(requestBody);

        recognize.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseBody = response.body().string();
                        String responseJson = gson.toJson(responseBody);
                        Log.d(TAG, "Recognize Response Body:\n " + responseJson);
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        JSONObject dataObject = jsonResponse.getJSONObject("data");

                        Intent intent = getIntent();
                        if (intent != null) {
                            authToken = intent.getStringExtra("authToken");
                        }
                        String token = "jwt " + authToken;
                        if (token.length() >= 10) {
                            Log.d("token", "token" + token.substring(0, 10) + "...");
                        }
                        String requestBodyFace = dataObject.toString();
//                        Log.d(TAG, "Face data:\n " + requestBodyFace);
                        RequestBody requestBodyFacee = RequestBody.create(MediaType.parse("application/json"), requestBodyFace);
                        //API CALL
                        callFaceDetailApi(token, requestBodyFacee);

                    } catch (IOException | JSONException e) {
                        Log.e(TAG, "Error reading response body: " + e.getMessage(), e);
                        throw new RuntimeException(e);
                    }
                } else {
                    Log.e(TAG, "Recognize Response Error: " + response.code() + " - " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
                Log.e(TAG, "Recognize Error: " + throwable.getMessage(), throwable);
            }
        });
    }

//    private void callFaceDetailApi(String token, RequestBody requestBodyFacee) {
//        Log.d(TAG, "Face Detail API Called");
//        Log.d(TAG, "Token: " + token);
//        APIInterface faceApiService = APIClient.getInstance().getFace();
//        retrofit2.Call<ResponseBody> faceDetails = faceApiService.FaceDetailApi(token, requestBodyFacee);
//
//        faceDetails.enqueue(new Callback<ResponseBody>() {
//            @Override
//            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
//                if (response.isSuccessful() && response.body() != null) {
//                    String responseBody = null;
//                    try {
//                        responseBody = response.body().string();
////                        String responseJson = gson.toJson(responseBody);
////                        Log.d(TAG, "Face Detail Response Body:\n" + responseJson);
//                        JSONObject jsonResponse = new JSONObject(responseBody);
//                        JSONObject dataObject = jsonResponse.getJSONObject("data");
//                        Log.d(TAG, "Face data: " + dataObject);
//                        String id = dataObject.getString("_id");
//                        String firstName = dataObject.getString("firstname");
//                        Log.d(TAG, "ID: " + id + ", First Name: " + firstName);
//
//                        callAttendanceApi(id, firstName);
//                    } catch (IOException | JSONException e) {
//                        throw new RuntimeException(e);
//                    }
////                    callAttendanceApi();
//                } else {
//                    Log.e(TAG, "Face Detail Response Error: " + response.code() + " - " + response.message());
//                }
//            }
//
//            @Override
//            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
//                Log.e(TAG, "Face DetailError: " + throwable.getMessage(), throwable);
//
//            }
//        });
//    }

    private void callFaceDetailApi(String token, RequestBody requestBodyFacee) {
        Log.d(TAG, "Face Detail API Called");
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
                        if (jsonResponse.has("data") && !jsonResponse.isNull("data")) {
                            JSONObject dataObject = jsonResponse.getJSONObject("data");
                            JSONObject employeeObject = dataObject.getJSONObject("employee");
                            String id = employeeObject.getString("_id");
                            String firstName = employeeObject.getString("firstname");

                            Log.d(TAG, "ID: " + id + ", First Name: " + firstName);

                            String responseJson = gson.toJson(responseBody);
                            Log.d(TAG, "Face Detail Response Body:\n" + responseJson);

                            SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                            String userId = sharedPreferences.getString("userId", null);
                            Log.e(TAG, "User ID: " + userId + ", ID: " + id);
                            if (userId != null && userId.equals(id)) {
                                callAttendanceApi(id, firstName);
                            } else {
                                showAttendanceFailedAlert("Attendance failed: User Id Mismatch.");
                            }
                        } else {
                            Log.e(TAG, "Face data not found in response.");
                            showAttendanceFailedAlert("Attendance failed: Face not found or matched.");
                        }
                    } catch (IOException | JSONException e) {
                        Log.e(TAG, "Error processing face detail response: " + e.getMessage(), e);
                        showAttendanceFailedAlert("Attendance failed: An error occurred.");
                    }
                } else {
                    Log.e(TAG, "Face Detail Response Error: " + response.code() + " - " + response.message());
                    showAttendanceFailedAlert("Attendance failed: Server error.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
                Log.e(TAG, "Face Detail Error: " + throwable.getMessage(), throwable);
                showAttendanceFailedAlert("Attendance failed: Network or API error.");
            }
        });
    }


//    private void callAttendanceApi() {
//        Log.d(TAG, "Attendance API Called");
//        Intent intent = getIntent();
//        if (intent != null) {
//            authToken = intent.getStringExtra("authToken");
//            userId = intent.getStringExtra("userId");
//            name = intent.getStringExtra("name");
//        }
//        String token = "jwt " + authToken;
//        if (token.length() >= 10) {
//            Log.d("token", "token" + token.substring(0, 10) + "...");
//        }
////        String currentAddress = "GoBolt";
//        getCurrentLocation();
//        Log.d(TAG, "Current Address: " + currentAddress);
//        String currentTime = getCurrentTime();
//        Log.d(TAG, "Current Time: " + currentTime);
//        try {
//            JSONObject requestBody = new JSONObject();
//            requestBody.put("employee", userId);
//            requestBody.put("employeename", name);
//            requestBody.put("address", currentAddress);
//            requestBody.put("punchTime", currentTime);
//
//            String requestBodyString = requestBody.toString();
//            Log.d(TAG, "Request Body: " + requestBodyString);
//            RequestBody requestBodyAttendance = RequestBody.create(MediaType.parse("application/json"), requestBodyString);
//
//            APIInterface attendanceApiService = APIClient.getInstance().getAttendance();
//            retrofit2.Call<ResponseBody> attendance = attendanceApiService.AttendanceApi(token, requestBodyAttendance);
////API FOR ATTENDANCE PUNCH
//            attendance.enqueue(new Callback<ResponseBody>() {
//                @Override
//                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
//                    if (response.isSuccessful() && response.body() != null) {
//                        try {
//                            String responseBody = response.body().string();
//                            String responseJson = gson.toJson(responseBody);
//                            Log.d(TAG, "Attendance Response Body:\n" + responseJson);
//                        } catch (IOException e) {
//                            Log.e(TAG, "Error reading response body: " + e.getMessage(), e);
//                            throw new RuntimeException(e);
//                        }
//                    }
//
//                }
//
//                @Override
//                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
//                    Log.e(TAG, "Attendance Error: " + throwable.getMessage(), throwable);
//                    throw new RuntimeException(throwable);
//
//                }
//            });
//
//        } catch (JSONException e) {
//            Log.e(TAG, "Error creating request body: " + e.getMessage(), e);
//            throw new RuntimeException(e);
//        }
//
//    }

    private void callAttendanceApi(String employeeId, String employeeName) {
        Log.d(TAG, "Attendance API Called");
        Intent intent = getIntent();
        if (intent != null) {
            authToken = intent.getStringExtra("authToken");
        }
        String token = "jwt " + authToken;
        if (token.length() >= 10) {
            Log.d("token", "token" + token.substring(0, 10) + "...");
        }

        getCurrentLocation(address -> {
            currentAddress = address;
            Log.d(TAG, "Current Address (Received): " + currentAddress);

            String currentTime = getCurrentTime();
            try {
                JSONObject requestBody = new JSONObject();
                requestBody.put("employee", employeeId);
                requestBody.put("employeeName", employeeName);
                requestBody.put("address", currentAddress);
                requestBody.put("punchTime", currentTime);

                String requestBodyString = requestBody.toString();
                Log.d(TAG, "Request Body: " + requestBodyString);
                RequestBody requestBodyAttendance = RequestBody.create(MediaType.parse("application/json"), requestBodyString);

                APIInterface attendanceApiService = APIClient.getInstance().getAttendance();
                Call<ResponseBody> attendance = attendanceApiService.AttendanceApi(token, requestBodyAttendance);

                attendance.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                        ProgressBar progressBar = findViewById(R.id.progressBar);
                        progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null) {
                            try {
                                String responseBody = response.body().string();
                                JSONObject responseJson = new JSONObject(responseBody);
                                String message = responseJson.getString("message");
                                Log.d(TAG, "Attendance Response Message: " + message);
                                showAttendanceSuccessAlert(responseBody);

                                String responseJsonn = gson.toJson(responseBody);
                                Log.d(TAG, "Attendance Response Body:\n" + responseJsonn);
                            } catch (IOException | JSONException e) {
                                Log.e(TAG, "Error reading response body: " + e.getMessage(), e);
                                throw new RuntimeException(e);
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
                        Log.e(TAG, "Attendance Error: " + throwable.getMessage(), throwable);
                        throw new RuntimeException(throwable);
                    }
                });
            } catch (JSONException e) {
                Log.e(TAG, "Error creating request body: " + e.getMessage(), e);
                throw new RuntimeException(e);
            }
        });
    }

    private void showAttendanceSuccessAlert(String responseBody) {
        try {
            JSONObject responseJson = new JSONObject(responseBody);
            boolean success = responseJson.getBoolean("success");
            String message = responseJson.getString("message");
            if (success) {
                JSONObject data = responseJson.getJSONObject("data");
                Log.d(TAG, "Attendance Data: " + data);
//                String punchDate = data.getString("punchDate");
//                String punchIn = data.getString("punchIn");
//                String punchOut = data.getString("punchOut");
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
//                    formattedMessage.append("Punch Date: <i>").append(punchDate).append("</i>").append("<br><br>");
//                    formattedMessage.append("Punch In: ").append(punchIn).append("<br>");
//                    formattedMessage.append("Punch Out: ").append(punchOut).append("<br><br>");
                formattedMessage.append("Address: ").append(address).append("<br>");
                FragmentManager fragmentManager = getSupportFragmentManager();
//                runOnUiThread(() -> {
//                    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(PunchActivity.this);
//                    builder.setTitle("Attendance Success").setMessage(Html.fromHtml(formattedMessage.toString(), Html.FROM_HTML_MODE_LEGACY)).setPositiveButton("OK", (dialog, which) -> {
//                        dialog.dismiss();
//                        DashboardFragment dashboardFragment = new DashboardFragment();
//                        fragmentManager.beginTransaction().replace(R.id.nav_host_fragment_content_main, dashboardFragment).commit();
//                    }).show();
//                });

                runOnUiThread(() -> {
                    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(PunchActivity.this);
                    AlertDialog dialog = builder.setTitle("Attendance Success").setMessage(Html.fromHtml(formattedMessage.toString(), Html.FROM_HTML_MODE_LEGACY)).setPositiveButton("OK", (dialog1, which) -> {
                        dialog1.dismiss();
                        DashboardFragment dashboardFragment = new DashboardFragment();
                        fragmentManager.beginTransaction().replace(R.id.nav_host_fragment_content_main, dashboardFragment).commit();
                    }).show();
                    new Handler().postDelayed(() -> {
                        if (dialog.isShowing()) {
                            dialog.dismiss();
                            DashboardFragment dashboardFragment = new DashboardFragment();
                            fragmentManager.beginTransaction().replace(R.id.nav_host_fragment_content_main, dashboardFragment).commit();
                        }
                    }, 5000);
                });
            }
        } catch (JSONException e) {
            Log.e("PunchActivity", "Error parsing JSON response", e);
            throw new RuntimeException(e);
        }
    }

    private void showAttendanceFailedAlert(String message) {
        runOnUiThread(() -> new AlertDialog.Builder(PunchActivity.this).setTitle("Attendance Failed").setMessage(message).setPositiveButton("Retry", (dialog, id) -> {
            dialog.dismiss();
            startCamera();
        }).setNegativeButton("Cancel", (dialog, id) -> {
            dialog.dismiss();
            FragmentManager fragmentManager = getSupportFragmentManager();
            DashboardFragment dashboardFragment = new DashboardFragment();
            fragmentManager.beginTransaction().replace(R.id.nav_host_fragment_content_main, dashboardFragment).commit();

        }).show());
    }

    private String getCurrentTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(new Date());
    }

    private void getCurrentLocation(AddressCallback callback) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationRequest locationRequest = LocationRequest.create().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY).setInterval(10000).setFastestInterval(5000);

            LocationCallback locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(@NonNull LocationResult locationResult) {
                    Location location = locationResult.getLastLocation();
                    if (location != null) {
                        getAddressFromLocation(location.getLatitude(), location.getLongitude(), callback);
                    } else {
                        Log.e(TAG, "Location is null in onLocationResult");
                        callback.onAddressReceived("Location not found");
                    }
                }
            };
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        } else {
            Log.e(TAG, "Location permission not granted");
            callback.onAddressReceived("Location not found");
        }
    }

    private void getAddressFromLocation(double latitude, double longitude, AddressCallback callback) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                StringBuilder completeAddress = new StringBuilder();
                for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                    completeAddress.append(address.getAddressLine(i)).append("\n");
                }
                currentAddress = completeAddress.toString();
                callback.onAddressReceived(currentAddress);
            } else {
                Log.e(TAG, "No address found for location");
                currentAddress = "Location not found";
                callback.onAddressReceived(currentAddress);
            }
        } catch (IOException e) {
            Log.e(TAG, "Error getting address: " + e.getMessage());
            currentAddress = "Location not found";
            callback.onAddressReceived(currentAddress);
        }
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

    interface AddressCallback {
        void onAddressReceived(String address);
    }
}
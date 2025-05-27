package app.xedigital.ai.adminActivity;

import android.Manifest;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.SimpleDateFormat;
import android.icu.util.TimeZone;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import app.xedigital.ai.R;
import app.xedigital.ai.adminApi.AdminAPIClient;
import app.xedigital.ai.adminApi.AdminAPIInterface;
import app.xedigital.ai.model.Admin.visitorContact.VisitorContactResponse;
import app.xedigital.ai.model.Admin.visitorFace.VisitorFaceResponse;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class AdminCheckOutActivity extends AppCompatActivity {

    private final Handler handler = new Handler();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private final String CollectionName = "cloudfencedemo_wr8c2p";
    private PreviewView previewView;
    private TextView captureOverlay;
    private ProcessCameraProvider cameraProvider;
    private ImageCapture imageCapture;
    private Camera camera;
    private FragmentManager fragmentManager;
    private ExecutorService cameraExecutor;
    private String token;
    private String base64Image;
    private String faceId, imageId;
    private MaterialCardView progressBar;
    private CountDownTimer qrCountDownTimer;
    private String currentDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_checkout);
        SharedPreferences sharedPreferences = getSharedPreferences("AdminCred", MODE_PRIVATE);
        token = sharedPreferences.getString("authToken", "");
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

                try {
                    camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
                    if (camera == null) {
                        Log.e("AdminCheckOutActivity", "Camera is null");
                        showRetryAlert();
                    } else {
                        showCapturingOverlay();
                        handler.postDelayed(this::captureImage, 3000);
                    }
                } catch (IllegalArgumentException e) {
                    Log.e("AdminCheckOutActivity", "Error binding camera: " + e.getMessage(), e);
                    showRetryAlert();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
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

                Glide.with(AdminCheckOutActivity.this).load(savedUri).into(imageView);

                progressBar.setVisibility(View.VISIBLE);

                String savedImagePath = photoFile.getAbsolutePath();
                try {
                    ProcessCameraProvider cameraProvider = ProcessCameraProvider.getInstance(AdminCheckOutActivity.this).get();
                    cameraProvider.unbindAll();
                } catch (ExecutionException | InterruptedException e) {
                    Log.e("AdminCheckOutActivity", "Error unbinding camera preview: " + e.getMessage(), e);
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
                Log.e("AdminCheckOutActivity", "Photo capture failed: " + exception.getMessage(), exception);
                handleError("Photo capture failed: " + exception.getMessage());
            }
        });
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

//    private void showPermissionDeniedAlert() {
//        new MaterialAlertDialogBuilder(this).setTitle("Permissions Denied").setMessage("Camera and location permissions are required for this feature.").setPositiveButton("Grant Permissions", (dialog, which) -> {
//            dialog.dismiss();
//            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, 10);
//        }).setNegativeButton("Cancel", (dialog, which) -> {
//            dialog.dismiss();
//            setResult(Activity.RESULT_CANCELED);
//            finish();
//        }).show();
//    }

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


    private String convertImageToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.NO_WRAP);
    }

    private void sendToAPI(RequestBody requestBody) {
        AdminAPIInterface apiService = AdminAPIClient.getInstance().getBase1();
        String authToken = "jwt " + token;

        Call<ResponseBody> call = apiService.recognizeFace(authToken, requestBody);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                hideCapturingOverlay();
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseBody = response.body().string();
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        if (jsonResponse.has("data") && !jsonResponse.isNull("data")) {
                            JSONObject dataObject = jsonResponse.getJSONObject("data");

                            if (dataObject.has("Face") && !dataObject.isNull("Face")) {
                                JSONObject faceObject = dataObject.getJSONObject("Face");

                                faceId = faceObject.optString("FaceId", "N/A");
                                imageId = faceObject.optString("ImageId", "N/A");
                            } else {
                                Log.e("AdminCheckOutActivity", "'Face' object not found in 'data'");
                            }

                            String requestBodyFace = dataObject.toString();
                            RequestBody FaceDetails = RequestBody.create(MediaType.parse("application/json"), requestBodyFace);
                            callFaceDetailApi(FaceDetails);
                        } else {
                            Log.e("AdminCheckOutActivity", "Null or missing 'data' in API response: " + responseBody);
                            Toast.makeText(AdminCheckOutActivity.this, "No face data found in response.", Toast.LENGTH_SHORT).show();
                            handleError("No face found.");
                        }
                    } catch (JSONException | IOException e) {
                        throw new RuntimeException(e);
                    }

                } else {
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (IOException e) {
                        Log.e("AdminCheckOutActivity", "Error reading error body: " + e.getMessage(), e);
                    }
                    Log.e("AdminCheckOutActivity", "Recognize Response Error: " + response.code() + " - " + response.message() + "\nError Body: " + errorBody);
                    handleError("Server Error: " + response.code() + " - " + response.message() + "\nDetails: " + errorBody);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                hideCapturingOverlay();
                handleError("Network Error: " + t.getMessage());
            }
        });
    }

    private void callFaceDetailApi(RequestBody faceDetails) {
        String authToken = "jwt " + token;
        AdminAPIInterface faceApiService = AdminAPIClient.getInstance().getBase2();
        Call<VisitorFaceResponse> call = faceApiService.FaceDetailsVisitor(authToken, faceDetails);
        call.enqueue(new Callback<VisitorFaceResponse>() {
            @Override
            public void onResponse(@NonNull Call<VisitorFaceResponse> call, @NonNull Response<VisitorFaceResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    VisitorFaceResponse responseBody = response.body();

                    if (responseBody.getData() != null && responseBody.getData().getVisitor() != null) {
                        String contact = responseBody.getData().getVisitor().getContact();
                        checkOut(contact);
                    } else {
                        handleError("Face detail data is missing or invalid.");
                    }
                } else {
                    handleError("Failed to retrieve face details. Response code: " + response.code());
                }
            }


            @Override
            public void onFailure(@NonNull Call<VisitorFaceResponse> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                handleError("Network error fetching face details: " + t.getMessage());
            }
        });
    }

    private void checkOut(String contact) {
        String authToken = "jwt " + token;
        AdminAPIInterface faceApiService = AdminAPIClient.getInstance().getBase2();
        Call<VisitorContactResponse> call = faceApiService.getCheckedOut(authToken, contact);
        call.enqueue(new Callback<VisitorContactResponse>() {
            @Override
            public void onResponse(@NonNull Call<VisitorContactResponse> call, @NonNull Response<VisitorContactResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    VisitorContactResponse responseBody = response.body();
                    Log.d("AdminCheckOutActivity", "Response Body: " + responseBody);
                    String contact = responseBody.getData().getVisitor().getContact();
                    String company = responseBody.getData().getVisitor().getCompany();
                    String signOut = getCurrentUtcTimestamp();
                    signOut(contact, company, signOut);

                } else {
                    handleError("Error. Response code: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<VisitorContactResponse> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                handleError("Network error " + t.getMessage());
            }
        });
    }

    private void signOut(String contact, String company, String signOut) {
        String authToken = "jwt " + token;
        try {
            //JSON payload
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("contact", contact);
            jsonObject.put("signOut", signOut);
            jsonObject.put("company", company);

            // Step 2: Convert JSON to RequestBody
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonObject.toString());

            AdminAPIInterface apiService = AdminAPIClient.getInstance().getBase2();
            Call<ResponseBody> call = apiService.signOut(authToken, requestBody);

            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(AdminCheckOutActivity.this, "Sign out successful!", Toast.LENGTH_SHORT).show();
                        SuccessSignOut();
                    } else {
                        Toast.makeText(AdminCheckOutActivity.this, "Sign out failed: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    Toast.makeText(AdminCheckOutActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error preparing sign-out request", Toast.LENGTH_SHORT).show();
        }
    }

    private void SuccessSignOut() {
        new MaterialAlertDialogBuilder(this).setTitle("Successfully Checked-Out").setPositiveButton("OK", (dialog, which) -> {
            dialog.dismiss();
            finish();
        }).show();

    }

    private String getCurrentUtcTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(new Date());
    }


    private void handleError(String message) {
        new MaterialAlertDialogBuilder(this).setTitle("Error").setMessage(message).setPositiveButton("Retry", (dialog, which) -> {
            dialog.dismiss();
            startCamera();
        }).setNegativeButton("Cancel", (dialog, which) -> {
            dialog.dismiss();
            finish();
        }).show();
    }

    private File getOutputDirectory() {
        File mediaDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (mediaDir != null && mediaDir.exists()) return mediaDir;
        return getFilesDir();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
        if (qrCountDownTimer != null) {
            qrCountDownTimer.cancel();
        }
    }
}

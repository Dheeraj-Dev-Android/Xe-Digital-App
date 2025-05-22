package app.xedigital.ai.adminActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
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
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import app.xedigital.ai.R;
import app.xedigital.ai.activity.AdminDashboardActivity;
import app.xedigital.ai.adminApi.AdminAPIClient;
import app.xedigital.ai.adminApi.AdminAPIInterface;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminPunchActivity extends AppCompatActivity {

    private final Handler handler = new Handler();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private PreviewView previewView;
    private String CollectionName = "cloudfencedemo_wr8c2p";
    private TextView captureOverlay;
    private ProcessCameraProvider cameraProvider;
    private ImageCapture imageCapture;
    private Camera camera;
    private ExecutorService cameraExecutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_punch_activity);

        previewView = findViewById(R.id.previewView);
        captureOverlay = findViewById(R.id.capture_overlay);
        cameraExecutor = Executors.newSingleThreadExecutor();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
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

                showCapturingOverlay();
                handler.postDelayed(captureRunnable, 3000);

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

                Glide.with(AdminPunchActivity.this).load(savedUri).into(imageView);


                String savedImagePath = photoFile.getAbsolutePath();

                try {
                    AtomicReference<Bitmap> bitmap;
                    bitmap = new AtomicReference<>(BitmapFactory.decodeFile(savedImagePath));

                    try {
                        int newWidth = 500;
                        int newHeight = (int) (bitmap.get().getHeight() * (newWidth / (float) bitmap.get().getWidth()));
                        bitmap.set(Bitmap.createScaledBitmap(bitmap.get(), newWidth, newHeight, false));

                        String base64Image = convertImageToBase64(bitmap.get());
                        if (base64Image.length() >= 10) {
                            Log.e("AdminPunchActivity", "Base64 Image: " + base64Image.substring(0, 10) + "...");
                        }
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
                    Log.e("AdminPunchActivity", "Error processing image: " + e.getMessage(), e);
                    Log.e("AdminPunchActivity", "Error during face detection: " + e.getMessage(), e);
                    handleError("Error detecting faces. Please try again.");
//                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Log.e("AdminPunchActivity", "Photo capture failed: " + exception.getMessage(), exception);
                handleError("Photo capture failed: " + exception.getMessage());
            }
        });
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

    private void sendToAPI(RequestBody requestBody) {
        Toast.makeText(this, "Sending image to API...", Toast.LENGTH_SHORT).show();
        AdminAPIInterface apiService = AdminAPIClient.getInstance().getBase1();

        SharedPreferences sharedPreferences = getSharedPreferences("AdminCred", MODE_PRIVATE);
        String token = sharedPreferences.getString("authToken", "");
        String authToken = "jwt " + token;
        Log.e("AdminPunchActivity", "authToken: " + authToken);

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
                        JSONObject dataObject = jsonResponse.getJSONObject("data");

                        String requestBodyFace = dataObject.toString();
                        RequestBody requestBodyFacee = RequestBody.create(MediaType.parse("application/json"), requestBodyFace);
                        //API CALL
                        callFaceDetailApi(requestBodyFacee);
                        showAlert("Success", "Image uploaded successfully.");
                    } catch (JSONException | IOException e) {
                        throw new RuntimeException(e);
                    }

                } else {
                    showAlert("Failed", "Upload failed. Code: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                hideCapturingOverlay();
                showAlert("Error", "Upload error: " + t.getMessage());
            }
        });

    }    private final Runnable captureRunnable = this::captureImage;

    private void callFaceDetailApi(RequestBody requestBodyFacee) {
        Log.d("AdminPunchActivity", "Face Detail API Called");
        SharedPreferences sharedPreferences = getSharedPreferences("AdminCred", MODE_PRIVATE);
        String token = sharedPreferences.getString("authToken", "");
        String authToken = "jwt " + token;
        AdminAPIInterface faceApiService = AdminAPIClient.getInstance().getBase2();
        retrofit2.Call<ResponseBody> faceDetails = faceApiService.FaceDetails(authToken, requestBodyFacee);

        faceDetails.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseBody = response.body().string();
                        JSONObject jsonResponse = new JSONObject(responseBody);

                        boolean success = jsonResponse.optBoolean("success", false);
                        int statusCode = jsonResponse.optInt("statusCode", 0);
                        String message = jsonResponse.optString("message", "");

                        // Check for specific message and null data
                        if (success && statusCode == 200 && "No data found!!!".equals(message) && jsonResponse.isNull("data")) {
                            Log.d("AdminPunchActivity", "No data found, calling Visitor Face API...");
                            callVisitorFace(requestBodyFacee);
                            return;
                        }

                        // Proceed normally if data exists
                        if (jsonResponse.has("data") && !jsonResponse.isNull("data")) {
                            JSONObject dataObject = jsonResponse.getJSONObject("data");
                            JSONObject employeeObject = dataObject.getJSONObject("employee");
                            String id = employeeObject.getString("_id");
                            String firstName = employeeObject.getString("firstname");

                            Log.d("AdminPunchActivity", "ID: " + id + ", First Name: " + firstName);

                            String responseJson = gson.toJson(responseBody);
                            Log.d("AdminPunchActivity", "Face Detail Response Body:\n" + responseJson);
                        } else {
                            Log.e("AdminPunchActivity", "Face data not found in response.");
                        }
                    } catch (IOException | JSONException e) {
                        Log.e("AdminPunchActivity", "Error processing face detail response: " + e.getMessage(), e);
                    }
                } else {
                    Log.e("AdminPunchActivity", "Face Detail Response Error: " + response.code() + " - " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
                Log.e("AdminPunchActivity", "Face Detail Error: " + throwable.getMessage(), throwable);
            }
        });
    }

    private void callVisitorFace(RequestBody requestBodyFacee) {
        Log.d("AdminPunchActivity", "Visitor Face API Called");
        SharedPreferences sharedPreferences = getSharedPreferences("AdminCred", MODE_PRIVATE);
        String token = sharedPreferences.getString("authToken", "");
        String authToken = "jwt " + token;
        AdminAPIInterface faceApiService = AdminAPIClient.getInstance().getBase2();
        retrofit2.Call<ResponseBody> faceDetails = faceApiService.FaceDetailsVisitor(authToken, requestBodyFacee);

        faceDetails.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseBody = response.body().string();
                        JSONObject jsonResponse = new JSONObject(responseBody);

                        // Check if "data" key exists and is not null before proceeding
                        if (!jsonResponse.has("data") || jsonResponse.isNull("data")) {
                            Log.e("AdminPunchActivity", "Face data not found in response.");
                            return;
                        }
                        if (jsonResponse.has("data") && !jsonResponse.isNull("data")) {
                            JSONObject dataObject = jsonResponse.getJSONObject("data");

                            String responseJson = gson.toJson(responseBody);
                            Log.d("AdminPunchActivity", "Face Detail Response Body:\n" + responseJson);


                        } else {
                            Log.e("AdminPunchActivity", "Face data not found in response.");
                        }
                    } catch (IOException | JSONException e) {
                        Log.e("AdminPunchActivity", "Error processing face detail response: " + e.getMessage(), e);
                    }
                } else {
                    Log.e("AdminPunchActivity", "Face Detail Response Error: " + response.code() + " - " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
                Log.e("AdminPunchActivity", "Face Detail Error: " + throwable.getMessage(), throwable);
            }
        });

    }

    private void showAlert(String title, String message) {
        runOnUiThread(() -> {
            if (isFinishing() || isDestroyed()) return;

            if (cameraProvider != null) {
                cameraProvider.unbindAll();
            }

            new AlertDialog.Builder(AdminPunchActivity.this).setTitle(title).setMessage(message).setCancelable(false).setPositiveButton("OK", (dialog, which) -> {
                dialog.dismiss();
                navigateToDashboard();
            }).show();
        });
    }

//    private void callFaceDetailApi(RequestBody requestBodyFacee) {
//        Log.d("AdminPunchActivity", "Face Detail API Called");
//        SharedPreferences sharedPreferences = getSharedPreferences("AdminCred", MODE_PRIVATE);
//        String token = sharedPreferences.getString("authToken", "");
//        String authToken = "jwt " + token;
//        AdminAPIInterface faceApiService = AdminAPIClient.getInstance().getBase2();
//        retrofit2.Call<ResponseBody> faceDetails = faceApiService.FaceDetails(authToken , requestBodyFacee);
//
//        faceDetails.enqueue(new Callback<ResponseBody>() {
//            @Override
//            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
//                if (response.isSuccessful() && response.body() != null) {
//                    try {
//                        String responseBody = response.body().string();
//                        JSONObject jsonResponse = new JSONObject(responseBody);
//
//                        // Check if "data" key exists and is not null before proceeding
//                        if (!jsonResponse.has("data") || jsonResponse.isNull("data")) {
//                            Log.e("AdminPunchActivity", "Face data not found in response.");
//                            return;
//                        }
//                        if (jsonResponse.has("data") && !jsonResponse.isNull("data")) {
//                            JSONObject dataObject = jsonResponse.getJSONObject("data");
//                            JSONObject employeeObject = dataObject.getJSONObject("employee");
//                            String id = employeeObject.getString("_id");
//                            String firstName = employeeObject.getString("firstname");
//
//                            Log.d("AdminPunchActivity", "ID: " + id + ", First Name: " + firstName);
//
//                            String responseJson = gson.toJson(responseBody);
//                            Log.d("AdminPunchActivity", "Face Detail Response Body:\n" + responseJson);
//
//
//                        } else {
//                            callVisitorFace(requestBodyFacee);
//                            Log.e("AdminPunchActivity", "Face data not found in response.");
//                        }
//                    } catch (IOException | JSONException e) {
//                        Log.e("AdminPunchActivity", "Error processing face detail response: " + e.getMessage(), e);
//                    }
//                } else {
//                    Log.e("AdminPunchActivity", "Face Detail Response Error: " + response.code() + " - " + response.message());
//                }
//            }
//
//            @Override
//            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
//                Log.e("AdminPunchActivity", "Face Detail Error: " + throwable.getMessage(), throwable);
//            }
//        });
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

    private void showPermissionDeniedDialog() {
        if (isFinishing() || isDestroyed()) return;
        new AlertDialog.Builder(this).setTitle("Camera Permission Needed").setMessage("Camera permission is required to use this feature.").setPositiveButton("OK", (dialog, which) -> finish()).setCancelable(false).show();
    }

    private void navigateToDashboard() {
        Intent intent = new Intent(AdminPunchActivity.this, AdminDashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraProvider != null) cameraProvider.unbindAll();
        handler.removeCallbacks(captureRunnable);
        if (cameraExecutor != null) cameraExecutor.shutdown();
    }




    private final ActivityResultLauncher<String> cameraPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
        if (isGranted) startCamera();
        else showPermissionDeniedDialog();
    });


}

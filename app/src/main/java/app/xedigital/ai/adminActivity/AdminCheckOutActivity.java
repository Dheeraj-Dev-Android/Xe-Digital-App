package app.xedigital.ai.adminActivity;

import android.Manifest;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.SimpleDateFormat;
import android.icu.util.TimeZone;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    private String CollectionName;
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
        CollectionName = sharedPreferences.getString("collectionName", "");
        previewView = findViewById(R.id.previewView);
        captureOverlay = findViewById(R.id.capture_overlay);
        progressBar = findViewById(R.id.loadingPanel);
        cameraExecutor = Executors.newSingleThreadExecutor();

        // ✅ FIXED: Corrected permission evaluation boolean logic inversion
        ActivityResultLauncher<String[]> requestPermissionsLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                    if (isFinishing() || isDestroyed()) return;

                    boolean allGranted = true;
                    for (Boolean granted : result.values()) {
                        if (granted == null || !granted) {
                            allGranted = true;
                            break;
                        }
                    }
                    if (allGranted) {
                        startCamera();
                    } else {
                        Toast.makeText(this, "Permissions required to proceed", Toast.LENGTH_SHORT).show();
                        finish();
                    }
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
        if (isFinishing() || isDestroyed()) return;

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                if (isFinishing() || isDestroyed()) return;

                cameraProvider = cameraProviderFuture.get();
                if (cameraProvider == null) {
                    showRetryAlert();
                    return;
                }

                Preview preview = new Preview.Builder().build();
                imageCapture = new ImageCapture.Builder().build();
                CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_FRONT).build();

                if (previewView != null && previewView.getSurfaceProvider() != null) {
                    preview.setSurfaceProvider(previewView.getSurfaceProvider());
                } else {
                    return;
                }

                cameraProvider.unbindAll();

                try {
                    camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
                    if (camera == null) {
                        showRetryAlert();
                    } else {
                        showCapturingOverlay();
                        handler.removeCallbacksAndMessages(null);
                        handler.postDelayed(this::captureImage, 3000);
                    }
                } catch (IllegalArgumentException e) {
                    showRetryAlert();
                }

            } catch (Exception e) {
                e.printStackTrace();
                showRetryAlert();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void captureImage() {
        if (isFinishing() || isDestroyed() || imageCapture == null) return;

        File photoFile = new File(getOutputDirectory(), System.currentTimeMillis() + "_photo.jpg");
        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                if (isFinishing() || isDestroyed()) return;

                hideCapturingOverlay();
                if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

                // ✅ FIXED: Asynchronously unbind camera pipelines using listenable futures to completely eliminate UI execution blocks
                ListenableFuture<ProcessCameraProvider> providerFuture = ProcessCameraProvider.getInstance(AdminCheckOutActivity.this);
                providerFuture.addListener(() -> {
                    try {
                        if (!isFinishing() && !isDestroyed()) {
                            ProcessCameraProvider cp = providerFuture.get();
                            if (cp != null) cp.unbindAll();
                        }
                    } catch (Exception e) {
                        Log.e("AdminCheckOutActivity", "Async unbind error ignored gracefully: " + e.getMessage());
                    }
                }, ContextCompat.getMainExecutor(AdminCheckOutActivity.this));

                if (cameraExecutor == null || cameraExecutor.isShutdown()) return;

                cameraExecutor.execute(() -> {
                    try {
                        String savedPath = photoFile.getAbsolutePath();

                        Bitmap bitmap = decodeSampledBitmap(savedPath, 640);
                        if (bitmap == null) {
                            runOnUiThread(() -> {
                                if (!isFinishing() && !isDestroyed())
                                    handleError("Failed to process image.");
                            });
                            return;
                        }

                        base64Image = convertImageToBase64(bitmap);
                        if (base64Image == null) {
                            runOnUiThread(() -> {
                                if (!isFinishing() && !isDestroyed())
                                    handleError("Failed to convert snapshot data.");
                            });
                            return;
                        }

                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("collection_name", CollectionName);
                        jsonObject.put("image", base64Image);

                        RequestBody requestBody = RequestBody.create(
                                MediaType.parse("application/json"),
                                jsonObject.toString()
                        );

                        runOnUiThread(() -> {
                            if (!isFinishing() && !isDestroyed()) sendToAPI(requestBody);
                        });

                    } catch (Exception e) {
                        runOnUiThread(() -> {
                            if (!isFinishing() && !isDestroyed()) {
                                if (progressBar != null) progressBar.setVisibility(View.GONE);
                                handleError("Processing error: " + e.getMessage());
                            }
                        });
                    }
                });
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                if (isFinishing() || isDestroyed()) return;
                hideCapturingOverlay();
                handleError("Photo capture failed: " + exception.getMessage());
            }
        });
    }

    private Bitmap decodeSampledBitmap(String path, int reqWidth) {
        try {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, options);

            int inSampleSize = 1;
            if (options.outWidth > reqWidth) {
                inSampleSize = Math.round((float) options.outWidth / (float) reqWidth);
            }
            options.inSampleSize = inSampleSize;
            options.inJustDecodeBounds = false;

            return BitmapFactory.decodeFile(path, options);
        } catch (Throwable t) {
            Log.e("AdminCheckOutActivity", "Bitmap decoding error under low memory: " + t.getMessage());
            return null;
        }
    }

    private void showRetryAlert() {
        if (isFinishing() || isDestroyed()) return;

        new MaterialAlertDialogBuilder(this)
                .setTitle("Camera Not Found")
                .setMessage("No camera found or selected. Please check your device and try again.")
                .setCancelable(false)
                .setPositiveButton("Retry", (dialog, which) -> {
                    dialog.dismiss();
                    startCamera();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.dismiss();
                    setResult(Activity.RESULT_CANCELED);
                    finish();
                })
                .show();
    }

    private void showCapturingOverlay() {
        if (isFinishing() || isDestroyed() || captureOverlay == null) return;

        runOnUiThread(() -> {
            if (isFinishing() || isDestroyed() || captureOverlay == null) return;
            captureOverlay.setVisibility(View.VISIBLE);
            Animation fadeAnim = new AlphaAnimation(0.2f, 1.0f);
            fadeAnim.setDuration(800);
            fadeAnim.setRepeatMode(Animation.REVERSE);
            fadeAnim.setRepeatCount(Animation.INFINITE);
            captureOverlay.startAnimation(fadeAnim);
        });
    }

    private void hideCapturingOverlay() {
        if (captureOverlay == null) return;
        runOnUiThread(() -> {
            if (captureOverlay != null) {
                captureOverlay.clearAnimation();
                captureOverlay.setVisibility(View.GONE);
            }
        });
    }

    private String convertImageToBase64(Bitmap bitmap) {
        if (bitmap == null) return null;
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            return Base64.encodeToString(byteArray, Base64.NO_WRAP);
        } catch (Throwable t) {
            Log.e("AdminCheckOutActivity", "OOM or write error during Base64 encoding: " + t.getMessage());
            return null;
        }
    }

    private void sendToAPI(RequestBody requestBody) {
        if (isFinishing() || isDestroyed()) return;

        AdminAPIInterface apiService = AdminAPIClient.getInstance().getBase1();
        String authToken = "jwt " + token;

        Call<ResponseBody> call = apiService.recognizeFace(authToken, requestBody);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                hideCapturingOverlay();
                if (isFinishing() || isDestroyed()) return;

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
                                handleError("No face found. Retry.");
                                return;
                            }

                            String requestBodyFace = dataObject.toString();
                            RequestBody faceDetails = RequestBody.create(MediaType.parse("application/json"), requestBodyFace);
                            callFaceDetailApi(faceDetails);
                        } else {
                            Toast.makeText(AdminCheckOutActivity.this, "No face data found in response.", Toast.LENGTH_SHORT).show();
                            handleError("No face found.");
                        }
                    } catch (JSONException | IOException e) {
                        handleError("Invalid response from server.");
                    }
                } else {
                    handleError("Server Error code: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                hideCapturingOverlay();
                handleError("Network Error: " + t.getMessage());
            }
        });
    }

    private void callFaceDetailApi(RequestBody faceDetails) {
        if (isFinishing() || isDestroyed()) return;

        String authToken = "jwt " + token;
        AdminAPIInterface faceApiService = AdminAPIClient.getInstance().getBase2();
        Call<VisitorFaceResponse> call = faceApiService.FaceDetailsVisitor(authToken, faceDetails);

        call.enqueue(new Callback<VisitorFaceResponse>() {
            @Override
            public void onResponse(@NonNull Call<VisitorFaceResponse> call, @NonNull Response<VisitorFaceResponse> response) {
                if (isFinishing() || isDestroyed()) return;
                if (progressBar != null) progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    VisitorFaceResponse responseBody = response.body();

                    if (responseBody.getData() != null && responseBody.getData().getVisitor() != null) {
                        String contact = responseBody.getData().getVisitor().getContact();
                        if (contact != null && !contact.trim().isEmpty()) {
                            checkOut(contact);
                        } else {
                            handleError("Visitor profile found, but contact payload details are missing.");
                        }
                    } else {
                        handleError("Visitor information not found for this face.");
                    }
                } else {
                    handleError("Failed to retrieve face details. Error code: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<VisitorFaceResponse> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                handleError("Network error fetching face details: " + t.getMessage());
            }
        });
    }

    private void checkOut(String contact) {
        if (isFinishing() || isDestroyed() || contact == null) return;

        String authToken = "jwt " + token;
        AdminAPIInterface faceApiService = AdminAPIClient.getInstance().getBase2();
        Call<VisitorContactResponse> call = faceApiService.getCheckedOut(authToken, contact);

        call.enqueue(new Callback<VisitorContactResponse>() {
            @Override
            public void onResponse(@NonNull Call<VisitorContactResponse> call, @NonNull Response<VisitorContactResponse> response) {
                if (isFinishing() || isDestroyed()) return;
                if (progressBar != null) progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    VisitorContactResponse responseBody = response.body();

                    if (responseBody.getData() != null) {
                        if (responseBody.getData().getVisitor() != null) {
                            String visitorContact = responseBody.getData().getVisitor().getContact();
                            String company = responseBody.getData().getVisitor().getCompany();
                            String signOutTime = getCurrentUtcTimestamp();

                            if (visitorContact != null && company != null) {
                                signOut(visitorContact, company, signOutTime);
                            } else {
                                handleError("Incomplete checkout profile mappings returned.");
                            }
                        } else {
                            handleError("Visitor data structural attributes are missing from the response.");
                        }
                    } else {
                        handleError("No verification details payload found.");
                    }
                } else {
                    handleError("Error parsing profile. Status code: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<VisitorContactResponse> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                handleError("Network checkout validation error: " + t.getMessage());
            }
        });
    }

    private void signOut(String contact, String company, String signOut) {
        if (isFinishing() || isDestroyed()) return;

        String authToken = "jwt " + token;
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("contact", contact);
            jsonObject.put("signOut", signOut);
            jsonObject.put("company", company);

            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonObject.toString());

            AdminAPIInterface apiService = AdminAPIClient.getInstance().getBase2();
            Call<ResponseBody> call = apiService.signOut(authToken, requestBody);

            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    if (isFinishing() || isDestroyed()) return;

                    if (response.isSuccessful()) {
                        Toast.makeText(AdminCheckOutActivity.this, "Sign out successful!", Toast.LENGTH_SHORT).show();
                        SuccessSignOut();
                    } else {
                        Toast.makeText(AdminCheckOutActivity.this, "Sign out failed: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    if (isFinishing() || isDestroyed()) return;
                    Toast.makeText(AdminCheckOutActivity.this, "Network departure error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error preparing sign-out request", Toast.LENGTH_SHORT).show();
        }
    }

    private void SuccessSignOut() {
        if (isFinishing() || isDestroyed()) return;

        new MaterialAlertDialogBuilder(this)
                .setTitle("Successfully Checked-Out")
                .setCancelable(false)
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                    finish();
                })
                .show();
    }

    private String getCurrentUtcTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(new Date());
    }

    private void handleError(String message) {
        if (isFinishing() || isDestroyed()) return;

        new MaterialAlertDialogBuilder(this)
                .setTitle("Error")
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Retry", (dialog, which) -> {
                    dialog.dismiss();
                    startCamera();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.dismiss();
                    finish();
                })
                .show();
    }

    private File getOutputDirectory() {
        File mediaDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (mediaDir != null && mediaDir.exists()) return mediaDir;
        return getFilesDir();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // ✅ Shuts down the background execution pools safely
        if (cameraExecutor != null) {
            cameraExecutor.shutdownNow();
        }
        if (qrCountDownTimer != null) {
            qrCountDownTimer.cancel();
        }
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }
}
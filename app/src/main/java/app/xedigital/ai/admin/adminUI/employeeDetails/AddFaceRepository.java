package app.xedigital.ai.admin.adminUI.employeeDetails;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;

import app.xedigital.ai.admin.adminAPI.AdminAPIClient;
import app.xedigital.ai.admin.adminModal.EmployeeDetails.EmployeesItem;
import app.xedigital.ai.admin.adminModal.addFace.AddFaceResponse;
import app.xedigital.ai.admin.adminModal.employeeFaceAdd.EmployeeFaceAddResponse;
import app.xedigital.ai.admin.adminModal.employeeFaceAdd.EmpolyeeFaceAddRequest;
import app.xedigital.ai.admin.adminModal.employeeProfileImage.EmployeeProfileImageRequest;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddFaceRepository {

    private static final String TAG = "AddFaceRepository";
    private static final int RESIZE_WIDTH = 1280;
    private static final int JPEG_QUALITY = 92;

    public void addFaceAndLink(@NonNull File imageFile, @NonNull String collectionName, @NonNull String token, @NonNull EmployeesItem employeeItem, @NonNull ResultCallback callback) {
        String base64Image = prepareBase64(imageFile, callback);
        if (base64Image == null) return;
        String authToken = "jwt " + token;
        callFaceAddApi(authToken, collectionName, base64Image, callback, (faceId, imageId) -> callLinkFaceApi(authToken, faceId, imageId, employeeItem, callback, callback::onSuccess));
    }

    // ============================================================
    //  PUBLIC API: ADD FACE + LINK (2 APIs)
    // ============================================================

    public void addProfileImage(@NonNull File imageFile, @NonNull String token, @NonNull EmployeesItem employeeItem, @NonNull ResultCallback callback) {
        String base64Image = prepareBase64(imageFile, callback);
        if (base64Image == null) return;
        String authToken = "jwt " + token;
        callProfileImageApi(authToken, base64Image, employeeItem, callback, callback::onSuccess);
    }

    // ============================================================
    //  PUBLIC API: ADD PROFILE IMAGE (1 API)
    // ============================================================

    public void addFaceAndLinkAndProfile(@NonNull File imageFile, @NonNull String collectionName, @NonNull String token, @NonNull EmployeesItem employeeItem, @NonNull ResultCallback callback) {
        String base64Image = prepareBase64(imageFile, callback);
        if (base64Image == null) return;
        String authToken = "jwt " + token;
        callFaceAddApi(authToken, collectionName, base64Image, callback, (faceId, imageId) -> callLinkFaceApi(authToken, faceId, imageId, employeeItem, callback, () -> callProfileImageApi(authToken, base64Image, employeeItem, callback, callback::onSuccess)));
    }

    // ============================================================
    //  PUBLIC API: ADD FACE + LINK + PROFILE (3 APIs chained)
    // ============================================================

    private void callFaceAddApi(@NonNull String authToken, @NonNull String collectionName, @NonNull String base64Image, @NonNull ResultCallback callback, @NonNull FaceAddSuccessHandler onSuccess) {
        JSONObject payload = new JSONObject();
        try {
            payload.put("collection_name", collectionName);
            payload.put("image", base64Image);
        } catch (JSONException e) {
            callback.onError("Failed to prepare request.", true);
            return;
        }

        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), payload.toString());
        AdminAPIClient.getInstance().getBase1().addFace(authToken, requestBody).enqueue(new Callback<AddFaceResponse>() {
            @Override
            public void onResponse(@NonNull Call<AddFaceResponse> call, @NonNull Response<AddFaceResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null && response.body().getData().getFace() != null && response.body().getData().getFace().getFaceId() != null) {
                    String faceId = response.body().getData().getFace().getFaceId();
                    String imageId = response.body().getData().getFace().getImageId();
                    Log.d(TAG, "Step 1 (face/add) success. FaceId=" + faceId);
                    onSuccess.onSuccess(faceId, imageId);
                } else {
                    callback.onError("No face detected in the image. Please retake with better lighting.", true);
                }
            }

            @Override
            public void onFailure(@NonNull Call<AddFaceResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Step 1 (face/add) failed", t);
                callback.onError("Network error: " + t.getMessage(), true);
            }
        });
    }

    // ============================================================
    //  INTERNAL: STEP 1 — face/add
    // ============================================================

    private void callLinkFaceApi(@NonNull String authToken, @NonNull String faceId, @NonNull String imageId, @NonNull EmployeesItem employeeItem, @NonNull ResultCallback callback, @NonNull Runnable onSuccess) {
        EmpolyeeFaceAddRequest request = new EmpolyeeFaceAddRequest();
        request.setFaceId(faceId);
        request.setImageId(imageId);
        request.setEmployeeId(employeeItem.getId());
        request.setEmployee(EmployeeMapper.mapToEmployeeFaceAdd(employeeItem));
        AdminAPIClient.getInstance().getBase2().addEmployeeFace(authToken, request).enqueue(new Callback<EmployeeFaceAddResponse>() {
            @Override
            public void onResponse(@NonNull Call<EmployeeFaceAddResponse> call, @NonNull Response<EmployeeFaceAddResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Log.d(TAG, "Step 2 (employees/face/add) success");
                    onSuccess.run();
                } else {
                    String msg = response.body() != null && response.body().getMessage() != null ? response.body().getMessage() : "Failed to link face to employee";
                    callback.onError(msg, true);
                }
            }

            @Override
            public void onFailure(@NonNull Call<EmployeeFaceAddResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Step 2 (employees/face/add) failed", t);
                callback.onError("Network error while linking face: " + t.getMessage(), true);
            }
        });
    }

    private void callProfileImageApi(@NonNull String authToken, @NonNull String base64Image, @NonNull EmployeesItem employeeItem, @NonNull ResultCallback callback, @NonNull Runnable onSuccess) {
        EmployeeProfileImageRequest request = new EmployeeProfileImageRequest();
        request.setImage(base64Image);
        request.setEmployeeId(employeeItem.getId());
        request.setEmployee(EmployeeMapper.mapToEmployeeProfileImage(employeeItem));
        AdminAPIClient.getInstance().getBase2().addEmployeeProfileImage(authToken, employeeItem.getId(), request).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Step 3 (profile/image) success");
                    onSuccess.run();
                } else {
                    callback.onError("Server error while uploading profile image (" + response.code() + ")", true);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e(TAG, "Step 3 (profile/image) failed", t);
                callback.onError("Network error while uploading profile image: " + t.getMessage(), true);
            }
        });
    }

    // ============================================================
    //  INTERNAL: STEP 2 — employees/face/add
    // ============================================================

    private String prepareBase64(@NonNull File imageFile, @NonNull ResultCallback callback) {
        Bitmap bitmap;
        try {
            bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
            if (bitmap == null) {
                callback.onError("Failed to read captured image. Please retake.", true);
                return null;
            }
        } catch (Throwable t) {
            Log.e(TAG, "Bitmap decode failed", t);
            callback.onError("Failed to decode image. Please retake.", true);
            return null;
        }

        Bitmap resized;
        try {
            resized = resizeBitmap(bitmap, RESIZE_WIDTH);
        } catch (Throwable t) {
            callback.onError("Failed to process image. Please retake.", true);
            return null;
        }

        String base64 = convertImageToBase64(resized);
        if (base64 == null) {
            callback.onError("Failed to encode image. Please retake.", true);
            return null;
        }
        return base64;
    }

    // ============================================================
    //  INTERNAL: STEP 3 — employees/profile/image/{id}
    // ============================================================

    private Bitmap resizeBitmap(Bitmap bitmap, int targetWidth) {
        if (bitmap.getWidth() <= targetWidth) return bitmap;
        int newHeight = (int) (bitmap.getHeight() * (targetWidth / (float) bitmap.getWidth()));
        return Bitmap.createScaledBitmap(bitmap, targetWidth, newHeight, false);
    }

    // ============================================================
    //  SHARED HELPERS
    // ============================================================

    private String convertImageToBase64(Bitmap bitmap) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            // CHANGED: JPEG quality 92% (was 100%)
            bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, baos);
            return Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP);
        } catch (Throwable t) {
            Log.e(TAG, "Base64 conversion failed", t);
            return null;
        }
    }

    public interface ResultCallback {
        void onSuccess();

        void onError(@NonNull String message, boolean isRetryable);
    }

    private interface FaceAddSuccessHandler {
        void onSuccess(String faceId, String imageId);
    }
}
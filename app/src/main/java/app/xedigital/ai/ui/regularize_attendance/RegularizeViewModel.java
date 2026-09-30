package app.xedigital.ai.ui.regularize_attendance;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.json.JSONObject;

import java.io.IOException;

import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.api.APIInterface;
import app.xedigital.ai.model.branch.UserBranchResponse;
import app.xedigital.ai.model.regularize.RegularizeAttendanceRequest;
import app.xedigital.ai.model.user.UserModelResponse;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegularizeViewModel extends ViewModel {

    private static final String TAG = "RegularizeViewModel";
    private final APIInterface apiInterface;

    // Loading states
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isHrEmailLoadingLiveData = new MutableLiveData<>();

    // Data streams
    private final MutableLiveData<String> hrEmailLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> successMessageLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();

    public RegularizeViewModel() {
        this.apiInterface = APIClient.getInstance().RegularizeAttendance();
    }

    // ─── LiveData Getters ─────────────────────────────────────────
    public LiveData<Boolean> getIsLoadingLiveData() {
        return isLoadingLiveData;
    }

    public LiveData<Boolean> getIsHrEmailLoadingLiveData() {
        return isHrEmailLoadingLiveData;
    }

    public LiveData<String> getHrEmailLiveData() {
        return hrEmailLiveData;
    }

    public LiveData<String> getSuccessMessageLiveData() {
        return successMessageLiveData;
    }

    public LiveData<String> getErrorMessageLiveData() {
        return errorMessageLiveData;
    }

    public void resetEvents() {
        successMessageLiveData.setValue(null);
        errorMessageLiveData.setValue(null);
    }

    // ─── Fetch HR (Notification) Email ───────────────────────────
    public void fetchHrEmail(String userId, String authToken) {
        if (userId == null || userId.isEmpty() || authToken == null || authToken.isEmpty()) {
            hrEmailLiveData.setValue("");
            return;
        }

        isHrEmailLoadingLiveData.setValue(true);
        String authHeaderValue = "jwt " + authToken;

        APIClient.getInstance().getUser().getUserData(userId, authHeaderValue)
                .enqueue(new Callback<UserModelResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<UserModelResponse> call,
                                           @NonNull Response<UserModelResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            try {
                                String branchId = response.body().getData().getBranch().getId();
                                fetchBranchData(branchId, authToken);
                            } catch (Exception e) {
                                Log.e(TAG, "Failed to parse branchId from user response: " + e.getMessage());
                                isHrEmailLoadingLiveData.setValue(false);
                                hrEmailLiveData.setValue("");
                            }
                        } else {
                            isHrEmailLoadingLiveData.setValue(false);
                            hrEmailLiveData.setValue("");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<UserModelResponse> call, @NonNull Throwable t) {
                        Log.e(TAG, "User API call failed: " + t.getMessage());
                        isHrEmailLoadingLiveData.setValue(false);
                        hrEmailLiveData.setValue("");
                    }
                });
    }

    private void fetchBranchData(String branchId, String authToken) {
        String authHeaderValue = "jwt " + authToken;
        APIClient.getInstance().getBranch().getBranchData(branchId, authHeaderValue)
                .enqueue(new Callback<UserBranchResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<UserBranchResponse> call,
                                           @NonNull Response<UserBranchResponse> response) {
                        isHrEmailLoadingLiveData.setValue(false);
                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            try {
                                String notificationMail = response.body().getData().getBranch().getNotificationEmail();
                                hrEmailLiveData.setValue(notificationMail != null ? notificationMail : "");
                            } catch (Exception e) {
                                Log.e(TAG, "Failed to parse notificationMail from branch: " + e.getMessage());
                                hrEmailLiveData.setValue("");
                            }
                        } else {
                            hrEmailLiveData.setValue("");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<UserBranchResponse> call, @NonNull Throwable t) {
                        Log.e(TAG, "Branch API call failed: " + t.getMessage());
                        isHrEmailLoadingLiveData.setValue(false);
                        hrEmailLiveData.setValue("");
                    }
                });
    }

    // ─── Submit Regularization ───────────────────────────────────
    public void submitRegularize(String token, String attendanceId, RegularizeAttendanceRequest requestBody) {
        if (token == null || token.isEmpty() || attendanceId == null || attendanceId.isEmpty()) {
            errorMessageLiveData.setValue("Missing authentication or attendance reference data.");
            return;
        }

        isLoadingLiveData.setValue(true);

        Call<ResponseBody> call = apiInterface.RegularizeApi("jwt " + token, attendanceId, requestBody);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                isLoadingLiveData.setValue(false);

                try {
                    if (response.isSuccessful() && response.body() != null) {
                        String responseBodyString = response.body().string();
                        JSONObject jsonObject = new JSONObject(responseBodyString);
                        String message = jsonObject.optString("message", "Regularization submitted.");
                        successMessageLiveData.setValue(message);
                    } else {
                        errorMessageLiveData.setValue("Regularization request failed."
                                + "\nServer error code: " + response.code()
                                + "\n\nPlease try again later.");
                    }
                } catch (IOException | org.json.JSONException e) {
                    errorMessageLiveData.setValue("Could not process the server response."
                            + "\nPlease try again or contact HR.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
                isLoadingLiveData.setValue(false);
                errorMessageLiveData.setValue("Could not connect to the server."
                        + "\nPlease check your internet connection and try again.");
            }
        });
    }
}
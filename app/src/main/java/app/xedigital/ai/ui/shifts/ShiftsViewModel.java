package app.xedigital.ai.ui.shifts;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;
import java.util.Objects;

import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.api.APIInterface;
import app.xedigital.ai.model.branch.UserBranchResponse;
import app.xedigital.ai.model.shiftTime.ShiftTimeResponse;
import app.xedigital.ai.model.shiftTime.ShiftTypesItem;
import app.xedigital.ai.model.shifts.ShiftTypeResponse;
import app.xedigital.ai.model.shifts.ShiftsItem;
import app.xedigital.ai.model.user.UserModelResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShiftsViewModel extends ViewModel {
    private MutableLiveData<String> hrMailLiveData = new MutableLiveData<>();
    private MutableLiveData<List<ShiftsItem>> shiftData;
    private String authToken;
    private Gson gson;
    private String hrMail;
    private MutableLiveData<List<ShiftTypesItem>> shiftTimes;
    public ShiftsViewModel() {
        // Initialize Gson instance
        gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public LiveData<String> getHrMailLiveData() {
        return hrMailLiveData;
    }

    public LiveData<List<ShiftsItem>> getShiftTypes() {
        if (shiftData == null) {
            shiftData = new MutableLiveData<>();
            fetchShiftTypes();
        }
        return shiftData;
    }

    public LiveData<List<ShiftTypesItem>> getShiftTimes() {
        if (shiftTimes == null) {
            shiftTimes = new MutableLiveData<>();
        }
        return shiftTimes;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public void fetchShiftTypes() {
        String authHeader = "jwt " + authToken;
        APIInterface apiService = APIClient.getInstance().getShiftTypes();
        Call<ShiftTypeResponse> shiftType = apiService.getShiftTypes(authHeader);
        shiftType.enqueue(new Callback<ShiftTypeResponse>() {
            @Override
            public void onResponse(@NonNull Call<ShiftTypeResponse> call, @NonNull Response<ShiftTypeResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ShiftsItem> shifts = response.body().getData().getShifts();
                    shiftData.postValue(shifts);
                } else {
                    Log.e("API Error", "Response not successful or body is null");
                }
            }


            @Override
            public void onFailure(@NonNull Call<ShiftTypeResponse> call, @NonNull Throwable throwable) {
                Log.e("API Error", Objects.requireNonNull(throwable.getMessage()));

            }
        });
    }

    public void fetchShiftTimes(String shiftId) {
        String authHeader = "jwt " + authToken;
        APIInterface apiService = APIClient.getInstance().getShiftTypes();
        Call<ShiftTimeResponse> shiftTimeCall = apiService.getShiftTime(authHeader, shiftId);

        shiftTimeCall.enqueue(new Callback<ShiftTimeResponse>() {
            @Override
            public void onResponse(@NonNull Call<ShiftTimeResponse> call, @NonNull Response<ShiftTimeResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ShiftTypesItem> times = response.body().getData().getShiftTypes();
                    shiftTimes.postValue(times);
                } else {
                    // Handle API error
                    Log.e("API Error", "Response not successful or body is null");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ShiftTimeResponse> call, @NonNull Throwable t) {
                // Handle network or other errors
                Log.e("API Error", Objects.requireNonNull(t.getMessage()));
            }
        });
    }

    public void callUserApi(String userId, String authToken) {
        String authHeaderValue = "jwt " + authToken;

        Call<UserModelResponse> call = APIClient.getInstance().getUser().getUserData(userId, authHeaderValue);
        call.enqueue(new Callback<UserModelResponse>() {
            @Override
            public void onResponse(@NonNull Call<UserModelResponse> call, @NonNull Response<UserModelResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserModelResponse userResponse = response.body();
                    String responseJson = gson.toJson(userResponse.getData());
//                    Log.d(TAG, "Response: " + responseJson);
                    String branchId = userResponse.getData().getBranch().getId();
                    callBranchApi(branchId, authToken);
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserModelResponse> call, @NonNull Throwable throwable) {

            }
        });
    }

    public void callBranchApi(String branchId, String authToken) {
        String authHeaderValue = "jwt " + authToken;
        Call<UserBranchResponse> branchCall = APIClient.getInstance().getBranch().getBranchData(branchId, authHeaderValue);
        branchCall.enqueue(new Callback<UserBranchResponse>() {
            @Override
            public void onResponse(@NonNull Call<UserBranchResponse> call, @NonNull Response<UserBranchResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        UserBranchResponse responseBranch = response.body();
                        String responseString = gson.toJson(responseBranch);
//                        Log.d("BranchData", "Response: " + responseString);
                        hrMail = responseBranch.getData().getBranch().getNotificationEmail();
                        hrMailLiveData.postValue(hrMail);
//                        Log.d("BranchData", "Notification Email: " + hrMail);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserBranchResponse> call, @NonNull Throwable throwable) {
                Log.d("BranchData", "onFailure: " + throwable.getMessage());
            }
        });
    }

}
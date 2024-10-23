package app.xedigital.ai.ui.timesheet;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.api.APIInterface;
import app.xedigital.ai.model.branch.UserBranchResponse;
import app.xedigital.ai.model.dcrData.DcrDataResponse;
import app.xedigital.ai.model.user.UserModelResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TimesheetViewModel extends ViewModel {

    private static final String TAG = "TimeSheet";
    private final MutableLiveData<DcrDataResponse> _dcrData = new MutableLiveData<>();
    private final APIInterface apiInterface;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private String authToken;

    public TimesheetViewModel() {
        apiInterface = APIClient.getInstance().getDcrData();
    }

    public LiveData<DcrDataResponse> getDcrData() {
        return _dcrData;
    }

    public void storeLoginData(String authToken) {
        this.authToken = authToken;
    }

    public void fetchEmployeeDcr(String startDate, String endDate) {
        if (startDate == null || startDate.isEmpty() || endDate == null || endDate.isEmpty()) {
            // If start or end date is not provided, default to the last 30 days
            Calendar calendar = Calendar.getInstance();
            endDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());
            calendar.add(Calendar.DAY_OF_MONTH, -30);
            startDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());

            // Make API call for default date range
            makeApiCall(startDate, endDate);
        } else {
            // Make API call with user-provided dates
            makeApiCall(startDate, endDate);
        }
    }

    private void makeApiCall(String startDate, String endDate) {
        if (authToken != null) {
            String authHeaderValue = "jwt " + authToken;
            apiInterface.getEmployeeDcr(authHeaderValue, startDate, endDate, "", "", "", "", "", "").enqueue(new Callback<DcrDataResponse>() {
                @Override
                public void onResponse(@NonNull Call<DcrDataResponse> call, @NonNull Response<DcrDataResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        _dcrData.postValue(response.body());
                        String responsejson = gson.toJson(response.body());
                        Log.d("DcrDataResponse", "Response:\n " + responsejson);
                    } else {
                        Log.e("DcrDataResponse", "Error: " + response.code());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<DcrDataResponse> call, @NonNull Throwable t) {
                    Log.e("DcrDataResponse", "Error: " + t.getMessage());
                }
            });
        } else {
            Log.e("DcrDataResponse", "Error: Auth token is null");
        }
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
                    Log.d(TAG, "Response: " + responseJson);
                    String branchId = userResponse.getData().getBranch().getId();
                    callBranchApi(branchId, authToken);
                    Log.d(TAG, "Branch ID: " + branchId);
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserModelResponse> call, @NonNull Throwable throwable) {
                Log.d(TAG, "onFailure: " + throwable.getMessage());
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
                        Log.d("BranchData", "Response: " + responseString);
                        String notificationMail = responseBranch.getData().getBranch().getNotificationEmail();
                        Log.d("BranchData", "Notification Email: " + notificationMail);
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
package app.xedigital.ai.ui.leaves;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;

import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.api.APIInterface;
import app.xedigital.ai.model.leaveType.LeaveTypeResponse;
import app.xedigital.ai.model.leaveType.LeavetypesItem;
import app.xedigital.ai.model.leaves.LeavesResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LeavesViewModel extends ViewModel {

    private final MutableLiveData<LeavesResponse> _leavesData = new MutableLiveData<>();
    private final APIInterface apiInterface;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final MutableLiveData<LeaveTypeResponse> _leavesTypeData = new MutableLiveData<>();
    public LiveData<LeavesResponse> leavesData = _leavesData;
    public LiveData<LeaveTypeResponse> leavesTypeData = _leavesTypeData;
    private String authToken;
    private String restrictedHolidayId;

    public LeavesViewModel() {
        apiInterface = APIClient.getInstance().getLeaves();
    }

    public void setUserId(String authToken) {
        this.authToken = authToken;
    }

    public void fetchLeavesData() {
//        Log.d("LeavesViewModel", "Fetching leaves data...");

        if (authToken != null) {
            String authHeaderValue = "jwt " + authToken;
            apiInterface.getLeaves(authHeaderValue).enqueue(new Callback<LeavesResponse>() {
                @Override
                public void onResponse(@NonNull Call<LeavesResponse> call, @NonNull Response<LeavesResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        mainHandler.post(() -> _leavesData.setValue(response.body()));
                        String responseJson = gson.toJson(response.body());
//                        Log.d("LeavesViewModel", "Leave Response:\n " + responseJson);
                    } else {
                        Log.e("LeavesViewModel", "Error fetching leaves data: " + response.message());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<LeavesResponse> call, @NonNull Throwable throwable) {
                    Log.e("LeavesViewModel", "Error fetching leaves data: " + throwable.getMessage());
                }
            });
        } else {
            Log.e("LeavesViewModel", "Auth Token is null");
        }
    }

    public void fetchLeavesType() {
        if (authToken != null) {
            String authHeaderValue = "jwt " + authToken;
            Call<LeaveTypeResponse> leaveType = apiInterface.getLeaveTypes(authHeaderValue);
            leaveType.enqueue(new Callback<LeaveTypeResponse>() {
                @Override
                public void onResponse(@NonNull Call<LeaveTypeResponse> call, @NonNull Response<LeaveTypeResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        _leavesTypeData.postValue(response.body());
                        String responseJson = gson.toJson(response.body());
                        // Extract Restricted holiday ID
                        List<LeavetypesItem> leaveTypes = response.body().getData().getLeavetypes();
                        for (LeavetypesItem leaveType : leaveTypes) {
                            if ("Restricted holiday".equals(leaveType.getLeavetypeName())) {
                                restrictedHolidayId = leaveType.getId();
                                break; // Exit loop once found
                            }
                        }
//                        Log.d("fetchLeavesType", "Leave Response: " + responseJson);
                    } else {
                        Log.e("fetchLeavesType", "Error fetching leaves type: " + response.message());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<LeaveTypeResponse> call, @NonNull Throwable throwable) {
                    Log.e("fetchLeavesType", "Error fetching leaves type: " + throwable.getMessage());

                }
            });
        } else {
            Log.e("fetchLeavesType", "Auth Token is null");
        }
    }
}

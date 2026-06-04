package app.xedigital.ai.ui.TeamLeaves;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.api.APIInterface;
import app.xedigital.ai.model.TeamLeave.EmployeesItem;
import app.xedigital.ai.model.TeamLeave.TeamLeaveResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeamLeavesViewModel extends ViewModel {

    private final MutableLiveData<List<EmployeesItem>> employeesLiveData = new MutableLiveData<>();
    // LiveData to track API error messages
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    public LiveData<List<EmployeesItem>> getEmployeesLiveData() {
        return employeesLiveData;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    public void fetchTeamLeaves(String authToken, String employeeId) {
        APIInterface apiService = APIClient.getInstance().getApi();
        Call<TeamLeaveResponse> call = apiService.getTeamLeaves(authToken, employeeId);
        call.enqueue(new Callback<TeamLeaveResponse>() {
            @Override
            public void onResponse(@NonNull Call<TeamLeaveResponse> call, @NonNull Response<TeamLeaveResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    employeesLiveData.setValue(response.body().getData().getEmployees());
                    Log.e("TEAM_LEAVE_API", "Response : " + response.body().toString());
                } else {
                    String errorMsg = "Response Failed : " + response.code();
                    Log.e("TEAM_LEAVE_API", errorMsg);
                    errorLiveData.setValue(errorMsg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<TeamLeaveResponse> call, @NonNull Throwable t) {
                String errorMsg = "Error : " + t.getMessage();
                Log.e("TEAM_LEAVE_API", errorMsg);
                errorLiveData.setValue(errorMsg);
            }
        });
    }
}
package app.xedigital.ai.ui.AttendanceByManager;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.model.AttandanceByManager.AttandanceByManagerResponse;
import app.xedigital.ai.model.TeamUnderManagerResponse.TeamUnderManagerResponse;
import app.xedigital.ai.utills.SecurePrefManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AttendanceByManagerViewModel extends AndroidViewModel {

    private static final String TAG = "AttendanceByManagerVM";

    private final MutableLiveData<AttandanceByManagerResponse> attendanceData = new MutableLiveData<>();
    private final MutableLiveData<TeamUnderManagerResponse> teamMemberData = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public AttendanceByManagerViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<AttandanceByManagerResponse> getAttendanceData() {
        return attendanceData;
    }

    public LiveData<TeamUnderManagerResponse> getTeamMemberData() {
        return teamMemberData;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    private String getToken() {
        SecurePrefManager prefManager = SecurePrefManager.getInstance(getApplication());
        return "jwt " + prefManager.getString("authToken", "");
    }

    private String getManagerId() {
        SecurePrefManager prefManager = SecurePrefManager.getInstance(getApplication());
        return prefManager.getString("userId", "");
    }

    public void fetchEmployeesUnderManager() {
        APIClient.getInstance().getApi()
                .getEmployeesUnderManager(getToken(), getManagerId())
                .enqueue(new Callback<TeamUnderManagerResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<TeamUnderManagerResponse> call,
                                           @NonNull Response<TeamUnderManagerResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            teamMemberData.setValue(response.body());
                        } else {
                            errorMessage.setValue("Failed to load team members");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<TeamUnderManagerResponse> call,
                                          @NonNull Throwable t) {
                        Log.e(TAG, "fetchEmployeesUnderManager failed", t);
                        errorMessage.setValue(t.getMessage());
                    }
                });
    }

    public void fetchAttendance(String startDate, String endDate, @Nullable String employeeId) {
        APIClient.getInstance().getApi()
                .getAttendanceByManager(getToken(), getManagerId(), startDate, endDate, employeeId)
                .enqueue(new Callback<AttandanceByManagerResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<AttandanceByManagerResponse> call,
                                           @NonNull Response<AttandanceByManagerResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            attendanceData.setValue(response.body());
                        } else {
                            errorMessage.setValue("Error: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<AttandanceByManagerResponse> call,
                                          @NonNull Throwable t) {
                        Log.e(TAG, "fetchAttendance failed", t);
                        errorMessage.setValue(t.getMessage());
                    }
                });
    }
}
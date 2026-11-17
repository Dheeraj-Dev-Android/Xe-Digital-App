package app.xedigital.ai.admin.adminUI.EmployeeAttendance;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

import app.xedigital.ai.admin.adminAPI.AdminAPIClient;
import app.xedigital.ai.admin.adminAPI.AdminAPIInterface;
import app.xedigital.ai.admin.adminModal.AddAttendanceApproval.AddAttendanceApprovalResponse;
import app.xedigital.ai.admin.adminModal.AddAttendanceApproval.AddAttendanceRegularizeAppliedItem;
import app.xedigital.ai.utills.SecurePrefManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddAttendancePendingApprovalViewModel extends AndroidViewModel {

    private final MutableLiveData<List<AddAttendanceRegularizeAppliedItem>> attendanceList = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public AddAttendancePendingApprovalViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<List<AddAttendanceRegularizeAppliedItem>> getAttendanceList() {
        return attendanceList;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void fetchAddAttendancePendingApprovalList() {
        isLoading.setValue(true);
        String token = SecurePrefManager.getInstance(getApplication()).getString("authToken", "");

        if (token == null || token.isEmpty()) {
            isLoading.setValue(false);
            errorMessage.setValue("Authorization token missing.");
            return;
        }

        String authToken = "jwt " + token;
        AdminAPIInterface apiInterface = AdminAPIClient.getInstance().getBase2();

        apiInterface.getEmployeeAttendanceAddRegularizeList(authToken).enqueue(new Callback<AddAttendanceApprovalResponse>() {
            @Override
            public void onResponse(@NonNull Call<AddAttendanceApprovalResponse> call, @NonNull Response<AddAttendanceApprovalResponse> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    AddAttendanceApprovalResponse body = response.body();
                    if (body.getData() != null && body.getData().getAddAttendanceRegularizeApplied() != null) {
                        attendanceList.setValue(body.getData().getAddAttendanceRegularizeApplied());
                    } else {
                        attendanceList.setValue(new ArrayList<>());
                    }
                } else {
                    errorMessage.setValue("Failed to fetch data. Error code: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<AddAttendanceApprovalResponse> call, @NonNull Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue("Network error: " + t.getLocalizedMessage());
            }
        });
    }
}
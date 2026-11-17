package app.xedigital.ai.admin.adminUI.EmployeeAttendance;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import app.xedigital.ai.admin.adminAPI.AdminAPIClient;
import app.xedigital.ai.admin.adminAPI.AdminAPIInterface;
import app.xedigital.ai.admin.adminModal.AttendanceRegularizationApproval.AttendanceRegularizationApprovalResponse;
import app.xedigital.ai.admin.adminModal.AttendanceRegularizationApproval.AttendanceRegularizeAppliedItem;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AttendanceRegularizationApprovalViewModel extends ViewModel {

    private final MutableLiveData<List<AttendanceRegularizeAppliedItem>> regularizationData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public LiveData<List<AttendanceRegularizeAppliedItem>> getRegularizationData() {
        return regularizationData;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void fetchRegularizationRequests(String token, String start, String end, String employee, int page, int limit, String branch, String prefix, boolean rm) {
        isLoading.setValue(true);
        AdminAPIInterface apiInterface = AdminAPIClient.getInstance().getBase2();
        Call<AttendanceRegularizationApprovalResponse> call = apiInterface.getEmployeeAttendanceRegularizeApplied("jwt " + token, start, end, employee, page, limit, branch, prefix, rm);

        call.enqueue(new Callback<AttendanceRegularizationApprovalResponse>() {
            @Override
            public void onResponse(@NonNull Call<AttendanceRegularizationApprovalResponse> call, @NonNull Response<AttendanceRegularizationApprovalResponse> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    AttendanceRegularizationApprovalResponse res = response.body();
                    if (res.getData() != null && res.getData().getAttendanceRegularizeApplied() != null) {
                        regularizationData.setValue(res.getData().getAttendanceRegularizeApplied());
                    } else {
                        regularizationData.setValue(null);
                    }
                } else {
                    errorMessage.setValue("Failed to fetch regularization requests: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<AttendanceRegularizationApprovalResponse> call, @NonNull Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue("Error: " + t.getMessage());
            }
        });
    }
}
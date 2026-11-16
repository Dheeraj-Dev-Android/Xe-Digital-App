package app.xedigital.ai.admin.adminUI.employeeOnboard;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import app.xedigital.ai.admin.adminAPI.AdminAPIClient;
import app.xedigital.ai.admin.adminAPI.AdminAPIInterface;
import app.xedigital.ai.model.employeeOnboardDetails.EmployeeOnBoardDetailsItem;
import app.xedigital.ai.model.employeeOnboardDetails.EmployeeOnboardDetailsResponse;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EmployeeOnboardDetailsViewModel extends ViewModel {

    private final MutableLiveData<List<EmployeeOnBoardDetailsItem>> employeeListLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public LiveData<List<EmployeeOnBoardDetailsItem>> getEmployeeList() {
        return employeeListLiveData;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void fetchOnboardedEmployees(String authToken) {
        isLoading.setValue(true);

        AdminAPIInterface apiService = AdminAPIClient.getInstance().getBase2();
        Call<EmployeeOnboardDetailsResponse> call = apiService.getEmployeesOnBoarded(authToken);

        call.enqueue(new Callback<EmployeeOnboardDetailsResponse>() {
            @Override
            public void onResponse(@NonNull Call<EmployeeOnboardDetailsResponse> call, @NonNull Response<EmployeeOnboardDetailsResponse> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    if (response.body().getData() != null && response.body().getData().getEmployeeOnBoardDetails() != null) {
                        employeeListLiveData.setValue(response.body().getData().getEmployeeOnBoardDetails());
                    } else {
                        // Pass empty array instead of null to prevent NPE loops
                        employeeListLiveData.setValue(new ArrayList<>());
                    }
                } else {
                    errorMessage.setValue("Failed to load data. Code: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<EmployeeOnboardDetailsResponse> call, @NonNull Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue("Network error: " + t.getMessage());
            }
        });
    }

    public void requestEmployeeUpdate(String authToken, String employeeId,
                                      String remarks, UpdateRequestCallback callback) {
        RequestBody body;
        try {
            JSONObject json = new JSONObject();
            json.put("remarks", remarks);
            json.put("status", false);
            body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString());
        } catch (JSONException e) {
            callback.onError("Could not build request");
            return;
        }
        AdminAPIInterface apiService = AdminAPIClient.getInstance().getBase2();

        apiService.updateEmployeeOnboardStatusRemarks(authToken, employeeId, body)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            callback.onSuccess();
                        } else {
                            callback.onError("Request failed (" + response.code() + ")");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                        callback.onError("Network error. Please try again.");
                    }
                });
    }

    public interface UpdateRequestCallback {
        void onSuccess();

        void onError(String message);
    }
}
package app.xedigital.ai.adminUI.employeeDetails;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import app.xedigital.ai.adminApi.AdminAPIClient;
import app.xedigital.ai.adminApi.AdminAPIInterface;
import app.xedigital.ai.model.Admin.EmployeeDetails.EmployeeDetailResponse;
import app.xedigital.ai.model.Admin.EmployeeDetails.EmployeesItem;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EmployeeDetailsViewModel extends ViewModel {

    private final MutableLiveData<List<EmployeesItem>> employeeListLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public LiveData<List<EmployeesItem>> getEmployeeList() {
        return employeeListLiveData;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void fetchEmployees(String token) {
        if (token == null) {
            errorMessage.postValue("Token not found");
            return;
        }

        AdminAPIInterface apiService = AdminAPIClient.getInstance().getBase2();
        Call<EmployeeDetailResponse> call = apiService.getEmployees("jwt " + token);

        call.enqueue(new Callback<EmployeeDetailResponse>() {
            @Override
            public void onResponse(Call<EmployeeDetailResponse> call, Response<EmployeeDetailResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    employeeListLiveData.postValue(response.body().getData().getEmployees());
                } else {
                    errorMessage.postValue("API Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<EmployeeDetailResponse> call, Throwable t) {
                errorMessage.postValue("Network Error: " + t.getMessage());
            }
        });
    }
}

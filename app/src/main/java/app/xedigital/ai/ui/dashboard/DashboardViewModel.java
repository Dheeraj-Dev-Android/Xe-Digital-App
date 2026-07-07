package app.xedigital.ai.ui.dashboard;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.model.allEmployee.AllEmployeeResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardViewModel extends ViewModel {
    private final MutableLiveData<AllEmployeeResponse> employeeBirthdayData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public LiveData<AllEmployeeResponse> getEmployeeBirthdayData() {
        return employeeBirthdayData;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void fetchEmployeeBirthdays(String token) {
        String authToken = "jwt " + token;
        isLoading.setValue(true);
        APIClient.getInstance().getApi().getAllEmployees(authToken).enqueue(new Callback<AllEmployeeResponse>() {
            @Override
            public void onResponse(@NonNull Call<AllEmployeeResponse> call, @NonNull Response<AllEmployeeResponse> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isSuccess()) {
                        employeeBirthdayData.setValue(response.body());
                    } else {
                        errorMessage.setValue(response.body().getMessage());
                    }
                } else {
                    errorMessage.setValue("Failed to retrieve employee data. Code: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<AllEmployeeResponse> call, @NonNull Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue(t.getLocalizedMessage());
            }
        });
    }

}
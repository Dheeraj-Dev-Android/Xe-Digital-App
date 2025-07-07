package app.xedigital.ai.adminUI.employeeDetails.adminDashboard;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import app.xedigital.ai.adminApi.AdminAPIClient;
import app.xedigital.ai.adminApi.AdminAPIInterface;
import app.xedigital.ai.model.Admin.Dashboard.AdminDashboardResponse;
import app.xedigital.ai.model.Admin.Dashboard.CounterData;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminDashboardViewModel extends ViewModel {

    private final MutableLiveData<CounterData> dashboardData = new MutableLiveData<>();
    private final AdminAPIInterface apiInterface;

    public AdminDashboardViewModel() {
        apiInterface = AdminAPIClient.getInstance().getBase2();
    }

    public LiveData<CounterData> getDashboardData() {
        return dashboardData;
    }

    public void fetchDashboardData(String token) {
        apiInterface.getDashboard(token).enqueue(new Callback<AdminDashboardResponse>() {
            @Override
            public void onResponse(@NonNull Call<AdminDashboardResponse> call, @NonNull Response<AdminDashboardResponse> response) {
                if (response.isSuccessful() && response.body() != null &&
                        response.body().getData() != null &&
                        response.body().getData().getCounterData() != null) {

                    dashboardData.setValue(response.body().getData().getCounterData());

                } else {
                    dashboardData.setValue(null);
                }
            }

            @Override
            public void onFailure(Call<AdminDashboardResponse> call, Throwable t) {
                dashboardData.setValue(null);
            }
        });
    }
}

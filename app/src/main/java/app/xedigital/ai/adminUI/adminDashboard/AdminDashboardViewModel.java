package app.xedigital.ai.adminUI.adminDashboard;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import app.xedigital.ai.adminApi.AdminAPIClient;
import app.xedigital.ai.adminApi.AdminAPIInterface;
import app.xedigital.ai.model.Admin.Dashboard.AdminDashboardResponse;
import app.xedigital.ai.model.Admin.Dashboard.CounterData;
import app.xedigital.ai.model.Admin.EmployeeDetails.EmployeeDetailResponse;
import app.xedigital.ai.model.Admin.EmployeeDetails.EmployeesItem;
import app.xedigital.ai.model.Admin.LeaveGraph.LeaveGraphResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminDashboardViewModel extends ViewModel {

    private final MutableLiveData<CounterData> dashboardData = new MutableLiveData<>();
    private final MutableLiveData<List<EmployeesItem>> birthdayData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final AdminAPIInterface apiInterface;
    private final MutableLiveData<LeaveGraphResponse> leavesGraphData = new MutableLiveData<>();

    public AdminDashboardViewModel() {
        apiInterface = AdminAPIClient.getInstance().getBase2();
    }

    public LiveData<CounterData> getDashboardData() {
        return dashboardData;
    }

    public LiveData<List<EmployeesItem>> getBirthdayData() {
        return birthdayData;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<LeaveGraphResponse> getLeavesGraphData() {
        return leavesGraphData;
    }


    public void fetchDashboardData(String token) {
        isLoading.setValue(true);
        apiInterface.getDashboard(token).enqueue(new Callback<AdminDashboardResponse>() {
            @Override
            public void onResponse(@NonNull Call<AdminDashboardResponse> call, @NonNull Response<AdminDashboardResponse> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null && response.body().getData().getCounterData() != null) {
                    dashboardData.setValue(response.body().getData().getCounterData());
                } else {
                    dashboardData.setValue(null);
                    errorMessage.setValue("Failed to load dashboard data");
                }
            }

            @Override
            public void onFailure(Call<AdminDashboardResponse> call, Throwable t) {
                isLoading.setValue(false);
                dashboardData.setValue(null);
                errorMessage.setValue("Network error: " + t.getMessage());
            }
        });
    }

    public void fetchBirthdayData(String token) {
        isLoading.setValue(true);
        apiInterface.getEmployees(token).enqueue(new Callback<EmployeeDetailResponse>() {
            @Override
            public void onResponse(@NonNull Call<EmployeeDetailResponse> call, @NonNull Response<EmployeeDetailResponse> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null && response.body().getData().getEmployees() != null) {
                    List<EmployeesItem> employees = response.body().getData().getEmployees();
                    List<EmployeesItem> currentMonthBirthdays = filterCurrentMonthBirthdays(employees);
                    birthdayData.setValue(currentMonthBirthdays);
                    Log.d("BirthdayData", "Found " + currentMonthBirthdays.size() + " birthdays this month");
                } else {
                    birthdayData.setValue(new ArrayList<>());
                    errorMessage.setValue("Failed to load birthday data");
                    Log.e("BirthdayData", "Response not successful or body is null");
                }
            }

            @Override
            public void onFailure(@NonNull Call<EmployeeDetailResponse> call, @NonNull Throwable throwable) {
                isLoading.setValue(false);
                birthdayData.setValue(new ArrayList<>());
                errorMessage.setValue("Network error: " + throwable.getMessage());
                Log.e("BirthdayData", "API call failed: " + throwable.getMessage());
            }
        });
    }

    private List<EmployeesItem> filterCurrentMonthBirthdays(List<EmployeesItem> employees) {
        List<EmployeesItem> birthdayEmployees = new ArrayList<>();
        Calendar currentCalendar = Calendar.getInstance();
        int currentMonth = currentCalendar.get(Calendar.MONTH);
        int currentYear = currentCalendar.get(Calendar.YEAR);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat alternateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

        for (EmployeesItem employee : employees) {
            if (employee.getDateOfBirth() != null && !employee.getDateOfBirth().isEmpty()) {
                try {
                    Date birthDate = null;

                    // Try primary date format first
                    try {
                        birthDate = dateFormat.parse(employee.getDateOfBirth());
                    } catch (ParseException e) {
                        // Try alternate date format
                        try {
                            birthDate = alternateFormat.parse(employee.getDateOfBirth());
                        } catch (ParseException e2) {
                            Log.w("BirthdayData", "Could not parse date: " + employee.getDateOfBirth() + " for employee: " + employee.getFirstname());
                            continue;
                        }
                    }

                    if (birthDate != null) {
                        Calendar birthCalendar = Calendar.getInstance();
                        birthCalendar.setTime(birthDate);
                        int birthMonth = birthCalendar.get(Calendar.MONTH);

                        // Check if birthday is in current month
                        if (birthMonth == currentMonth) {
                            birthdayEmployees.add(employee);
                            Log.d("BirthdayData", "Added birthday employee: " + employee.getFirstname() + " - " + employee.getDateOfBirth());
                        }
                    }
                } catch (Exception e) {
                    Log.e("BirthdayData", "Error processing birthday for employee: " + employee.getFirstname(), e);
                }
            }
        }

        return birthdayEmployees;
    }

    public void fetchLeavesGraph(String token) {
        isLoading.setValue(true);

        apiInterface.getLeavesGraph(token).enqueue(new Callback<LeaveGraphResponse>() {
            @Override
            public void onResponse(@NonNull Call<LeaveGraphResponse> call, @NonNull Response<LeaveGraphResponse> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    leavesGraphData.setValue(response.body());
                    Log.d("LeavesGraph", "Graph data loaded successfully");
                } else {
                    leavesGraphData.setValue(null);
                    errorMessage.setValue("Failed to load leaves graph data");
                    Log.e("LeavesGraph", "Response error or null body");
                }
            }

            @Override
            public void onFailure(@NonNull Call<LeaveGraphResponse> call, @NonNull Throwable throwable) {
                isLoading.setValue(false);
                leavesGraphData.setValue(null);
                errorMessage.setValue("Network error while loading leaves graph: " + throwable.getMessage());
                Log.e("LeavesGraph", "API call failed", throwable);
            }
        });
    }

}
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
    private final MutableLiveData<LeaveGraphResponse> leavesGraphData = new MutableLiveData<>();
    private final AdminAPIInterface apiInterface;

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
                if (response.isSuccessful() && response.body() != null &&
                        response.body().getData() != null && response.body().getData().getCounterData() != null) {
                    dashboardData.setValue(response.body().getData().getCounterData());
                } else {
                    dashboardData.setValue(null);
                    errorMessage.setValue("Dashboard metadata not available");
                }
            }

            @Override
            public void onFailure(@NonNull Call<AdminDashboardResponse> call, @NonNull Throwable t) {
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
                if (response.isSuccessful() && response.body() != null &&
                        response.body().getData() != null && response.body().getData().getEmployees() != null) {

                    List<EmployeesItem> employees = response.body().getData().getEmployees();
                    birthdayData.setValue(processAndSortBirthdays(employees));
                } else {
                    birthdayData.setValue(new ArrayList<>());
                    errorMessage.setValue("Birthday records not available");
                }
            }

            @Override
            public void onFailure(@NonNull Call<EmployeeDetailResponse> call, @NonNull Throwable throwable) {
                isLoading.setValue(false);
                birthdayData.setValue(new ArrayList<>());
                errorMessage.setValue("Network connectivity failure");
            }
        });
    }

    private List<EmployeesItem> processAndSortBirthdays(List<EmployeesItem> employees) {
        List<EmployeesItem> filteredList = new ArrayList<>();
        if (employees == null) return filteredList;

        Calendar currentCalendar = Calendar.getInstance();
        int currentMonth = currentCalendar.get(Calendar.MONTH);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat alternateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

        for (EmployeesItem employee : employees) {
            if (employee == null || !employee.isActive()) continue;

            String dobString = employee.getDateOfBirth();
            if (dobString != null && !dobString.trim().isEmpty()) {
                try {
                    Date birthDate = null;
                    try {
                        birthDate = dateFormat.parse(dobString);
                    } catch (ParseException e) {
                        try {
                            birthDate = alternateFormat.parse(dobString);
                        } catch (ParseException e2) {
                            continue; // Unparseable format, skip safely to protect dataset loop integrity
                        }
                    }

                    if (birthDate != null) {
                        Calendar birthCalendar = Calendar.getInstance();
                        birthCalendar.setTime(birthDate);
                        if (birthCalendar.get(Calendar.MONTH) == currentMonth) {
                            filteredList.add(employee);
                        }
                    }
                } catch (Exception e) {
                    Log.e("BirthdayData", "Error validating birthdate data structural integrity", e);
                }
            }
        }

        // Safe sort algorithm with full internal element null handling
        filteredList.sort((e1, e2) -> {
            if (e1 == null && e2 == null) return 0;
            if (e1 == null) return 1;
            if (e2 == null) return -1;

            Date d1 = parseDateHelper(e1.getDateOfBirth(), dateFormat, alternateFormat);
            Date d2 = parseDateHelper(e2.getDateOfBirth(), dateFormat, alternateFormat);

            if (d1 == null && d2 == null) return 0;
            if (d1 == null) return 1;
            if (d2 == null) return -1;

            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            cal1.setTime(d1);
            cal2.setTime(d2);

            int m1 = cal1.get(Calendar.MONTH);
            int day1 = cal1.get(Calendar.DAY_OF_MONTH);
            int m2 = cal2.get(Calendar.MONTH);
            int day2 = cal2.get(Calendar.DAY_OF_MONTH);

            if (m1 != m2) {
                return Integer.compare(m1, m2);
            } else {
                return Integer.compare(day1, day2);
            }
        });

        return filteredList;
    }

    private Date parseDateHelper(String dateStr, SimpleDateFormat f1, SimpleDateFormat f2) {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        try {
            return f1.parse(dateStr);
        } catch (ParseException e) {
            try {
                return f2.parse(dateStr);
            } catch (ParseException e2) {
                return null;
            }
        }
    }

    public void fetchLeavesGraph(String token) {
        isLoading.setValue(true);
        apiInterface.getLeavesGraph(token).enqueue(new Callback<LeaveGraphResponse>() {
            @Override
            public void onResponse(@NonNull Call<LeaveGraphResponse> call, @NonNull Response<LeaveGraphResponse> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    leavesGraphData.setValue(response.body());
                } else {
                    leavesGraphData.setValue(null);
                    errorMessage.setValue("Graph statistics not available");
                }
            }

            @Override
            public void onFailure(@NonNull Call<LeaveGraphResponse> call, @NonNull Throwable throwable) {
                isLoading.setValue(false);
                leavesGraphData.setValue(null);
                errorMessage.setValue("Network error loading graph dashboard parameters");
            }
        });
    }
}
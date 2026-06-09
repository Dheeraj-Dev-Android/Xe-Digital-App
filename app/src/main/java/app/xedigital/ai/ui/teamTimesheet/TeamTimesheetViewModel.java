package app.xedigital.ai.ui.teamTimesheet;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.model.TeamTimesheetResponse.Employee;
import app.xedigital.ai.model.TeamTimesheetResponse.EmployeesDcrDataItem;
import app.xedigital.ai.model.TeamTimesheetResponse.TeamTimesheetResponse;
import app.xedigital.ai.model.TeamUnderManagerResponse.EmployeesItem;
import app.xedigital.ai.model.TeamUnderManagerResponse.TeamUnderManagerResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeamTimesheetViewModel extends ViewModel {
    private final MutableLiveData<TeamTimesheetResponse> dcrDataResponse = new MutableLiveData<>();
    private final MutableLiveData<List<EmployeesDcrDataItem>> filteredDcrData = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    // NEW: LiveData to hold the list of employee names under the RM
    private final MutableLiveData<List<String>> teamEmployeeNames = new MutableLiveData<>();

    private List<EmployeesDcrDataItem> originalList = new ArrayList<>();

    private String selectedEmployeeName = "All Employees";
    private String fromDateStr = "";
    private String toDateStr = "";

    public LiveData<TeamTimesheetResponse> getDcrDataResponse() {
        return dcrDataResponse;
    }

    public LiveData<List<EmployeesDcrDataItem>> getFilteredDcrData() {
        return filteredDcrData;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    // NEW: Getter for the fragment to observe
    public LiveData<List<String>> getTeamEmployeeNames() {
        return teamEmployeeNames;
    }

    public void fetchDcrDataForRM(String authToken, String userId) {
        if (authToken == null || userId == null || authToken.trim().isEmpty() || userId.trim().isEmpty()) {
            errorMessage.setValue("Session expired. Please log in again.");
            return;
        }
        isLoading.setValue(true);
        String formattedToken = authToken.startsWith("jwt ") ? authToken : "jwt " + authToken;

        APIClient.getInstance().getApi().getDcrDataForRM(formattedToken, userId).enqueue(new Callback<TeamTimesheetResponse>() {
            @Override
            public void onResponse(@NonNull Call<TeamTimesheetResponse> call, @NonNull Response<TeamTimesheetResponse> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isSuccess() && response.body().getData() != null) {
                        TeamTimesheetResponse completeResponse = response.body();
                        originalList = completeResponse.getData().getEmployeesDcrData() != null ?
                                completeResponse.getData().getEmployeesDcrData() : new ArrayList<>();
                        dcrDataResponse.setValue(completeResponse);
                        applyFilters(selectedEmployeeName, fromDateStr, toDateStr);
                    } else {
                        errorMessage.setValue(response.body().getMessage() != null ? response.body().getMessage() : "No records found.");
                    }
                } else {
                    errorMessage.setValue("Server error response: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<TeamTimesheetResponse> call, @NonNull Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue(t != null ? t.getMessage() : "Network error. Please try again.");
            }
        });
    }

    public void applyFilters(String employeeName, String fromDate, String toDate) {
        this.selectedEmployeeName = employeeName;
        this.fromDateStr = fromDate;
        this.toDateStr = toDate;

        if (originalList == null || originalList.isEmpty()) {
            filteredDcrData.setValue(new ArrayList<>());
            return;
        }

        List<EmployeesDcrDataItem> outputFilteredList = new ArrayList<>();

        LocalDate parsedFromDate = parseDateString(fromDate);
        LocalDate parsedToDate = parseDateString(toDate);

        for (EmployeesDcrDataItem item : originalList) {
            boolean matchesEmployee = false;
            boolean matchesDateRange = false;

            if (employeeName == null || employeeName.equals("All Employees")) {
                matchesEmployee = true;
            } else {
                Employee emp = item.getEmployee();
                if (emp != null) {
                    String currentFullName = (emp.getFirstname() + " " + emp.getLastname()).trim();
                    if (currentFullName.equalsIgnoreCase(employeeName.trim())) {
                        matchesEmployee = true;
                    }
                }
            }

            String dcrDateRaw = item.getDcrDateFormat();
            LocalDate targetItemDate = parseDateString(dcrDateRaw);

            if (targetItemDate == null) {
                matchesDateRange = (fromDate.isEmpty() && toDate.isEmpty());
            } else {
                boolean afterOrEqualFrom = (parsedFromDate == null || !targetItemDate.isBefore(parsedFromDate));
                boolean beforeOrEqualTo = (parsedToDate == null || !targetItemDate.isAfter(parsedToDate));
                matchesDateRange = afterOrEqualFrom && beforeOrEqualTo;
            }

            if (matchesEmployee && matchesDateRange) {
                outputFilteredList.add(item);
            }
        }

        filteredDcrData.setValue(outputFilteredList);
    }

    private LocalDate parseDateString(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        try {
            return LocalDate.parse(dateStr.trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public void fetchEmployeeUnderRM(String token, String userId) {
        if (token == null || userId == null || token.trim().isEmpty() || userId.trim().isEmpty()) {
            errorMessage.setValue("Session expired. Please log in again.");
            return;
        }
        String formattedToken = token.startsWith("jwt ") ? token : "jwt " + token;

        APIClient.getInstance().getApi().getEmployeesUnderManager(formattedToken, userId).enqueue(new Callback<TeamUnderManagerResponse>() {
            @Override
            public void onResponse(@NonNull Call<TeamUnderManagerResponse> call, @NonNull Response<TeamUnderManagerResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isSuccess() && response.body().getData() != null) {
                        List<EmployeesItem> employees = response.body().getData().getEmployees();
                        if (employees != null) {
                            List<String> employeeNames = new ArrayList<>();
                            employeeNames.add("All Employees");
                            for (EmployeesItem emp : employees) {
                                String fullName = (emp.getFirstname() + " " + emp.getLastname()).trim();
                                if (!fullName.isEmpty()) {
                                    employeeNames.add(fullName);
                                }
                            }
                            // UPDATED: Post values to the LiveData so the UI is updated dynamically
                            teamEmployeeNames.setValue(employeeNames);
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<TeamUnderManagerResponse> call, @NonNull Throwable throwable) {
                errorMessage.setValue(throwable != null ? throwable.getMessage() : "Network error. Please try again.");
            }
        });
    }
}
package app.xedigital.ai.admin.adminUI.EmployeeAttendance;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import app.xedigital.ai.admin.adminAPI.AdminAPIClient;
import app.xedigital.ai.admin.adminAPI.AdminAPIInterface;
import app.xedigital.ai.admin.adminModal.EmployeeDetails.EmployeeDetailResponse;
import app.xedigital.ai.admin.adminModal.EmployeeDetails.EmployeesItem;
import app.xedigital.ai.admin.adminModal.department.DepartmentResponse;
import app.xedigital.ai.admin.adminModal.department.DepartmentsItem;
import app.xedigital.ai.admin.adminModal.empAttendance.EmployeeAttendancePunchResponse;
import app.xedigital.ai.admin.adminModal.empAttendance.EmployeePunchDataItem;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EmployeeAttendanceViewModel extends ViewModel {

    // ── Attendance ──
    private final MutableLiveData<List<EmployeePunchDataItem>> attendanceData = new MutableLiveData<>();

    public LiveData<List<EmployeePunchDataItem>> getAttendanceData() {
        return attendanceData;
    }

    // ── Departments (for filter bottom sheet) ──
    private final MutableLiveData<List<DepartmentsItem>> departments = new MutableLiveData<>();

    public LiveData<List<DepartmentsItem>> getDepartments() {
        return departments;
    }

    // ── Employees (for filter bottom sheet) ──
    private final MutableLiveData<List<EmployeesItem>> employees = new MutableLiveData<>();

    public LiveData<List<EmployeesItem>> getEmployees() {
        return employees;
    }

    // ── Loading ──
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    // ── Error ──
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    // ══════════════════════════════════════════════
    //  FETCH ATTENDANCE
    // ══════════════════════════════════════════════

    public void fetchAttendance(String token, String start, String end, String sorting, String employee, String department, int page, int limit, String branch, String prefix) {
        isLoading.setValue(true);
        errorMessage.setValue("");

        AdminAPIInterface api = AdminAPIClient.getInstance().getBase2();
        Call<EmployeeAttendancePunchResponse> call = api.getEmployeeAttendancePunch("jwt " + token, start, end, sorting, employee, department, page, limit, branch, prefix);

        call.enqueue(new Callback<EmployeeAttendancePunchResponse>() {
            @Override
            public void onResponse(@NonNull Call<EmployeeAttendancePunchResponse> call, @NonNull Response<EmployeeAttendancePunchResponse> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<EmployeePunchDataItem> list = response.body().getData().getEmployeePunchData();
                    attendanceData.setValue(list != null ? list : new ArrayList<>());
                } else {
                    String msg = response.body() != null ? response.body().getMessage() : "Unknown error";
                    errorMessage.setValue(msg);
                    attendanceData.setValue(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(@NonNull Call<EmployeeAttendancePunchResponse> call, @NonNull Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue("Network error: " + t.getMessage());
                attendanceData.setValue(new ArrayList<>());
            }
        });
    }

    // ══════════════════════════════════════════════
    //  FETCH DEPARTMENTS
    // ══════════════════════════════════════════════

    public void fetchDepartments(String token) {
        AdminAPIInterface api = AdminAPIClient.getInstance().getBase2();
        Call<DepartmentResponse> call = api.getDepartments("jwt " + token);

        call.enqueue(new Callback<DepartmentResponse>() {
            @Override
            public void onResponse(@NonNull Call<DepartmentResponse> call, @NonNull Response<DepartmentResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<DepartmentsItem> list = response.body().getData().getDepartments();
                    departments.setValue(list != null ? list : new ArrayList<>());
                }
            }

            @Override
            public void onFailure(@NonNull Call<DepartmentResponse> call, @NonNull Throwable t) {
                errorMessage.setValue("Failed to load departments: " + t.getMessage());
            }
        });
    }

    // ══════════════════════════════════════════════
    //  FETCH EMPLOYEES
    // ══════════════════════════════════════════════

    public void fetchEmployees(String token) {
        AdminAPIInterface api = AdminAPIClient.getInstance().getBase2();
        Call<EmployeeDetailResponse> call = api.getEmployees("jwt " + token);

        call.enqueue(new Callback<EmployeeDetailResponse>() {
            @Override
            public void onResponse(@NonNull Call<EmployeeDetailResponse> call, @NonNull Response<EmployeeDetailResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<EmployeesItem> list = response.body().getData().getEmployees();
                    employees.setValue(list != null ? list : new ArrayList<>());
                }
            }

            @Override
            public void onFailure(@NonNull Call<EmployeeDetailResponse> call, @NonNull Throwable t) {
                errorMessage.setValue("Failed to load employees: " + t.getMessage());
            }
        });
    }
}
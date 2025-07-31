package app.xedigital.ai.adminUI.employeeDetails;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import app.xedigital.ai.adminApi.AdminAPIClient;
import app.xedigital.ai.adminApi.AdminAPIInterface;
import app.xedigital.ai.model.Admin.ActiveShift.ActiveShiftResponse;
import app.xedigital.ai.model.Admin.ActiveShift.ShiftsItem;
import app.xedigital.ai.model.Admin.EmployeeDetails.EmployeeDetailResponse;
import app.xedigital.ai.model.Admin.EmployeeDetails.EmployeesItem;
import app.xedigital.ai.model.Admin.department.DepartmentResponse;
import app.xedigital.ai.model.Admin.department.DepartmentsItem;
import app.xedigital.ai.model.Admin.updateEmployee.UpdateEmployeeRequest;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditEmployeeViewModel extends ViewModel {

    private final MutableLiveData<EmployeesItem> selectedEmployee = new MutableLiveData<>();
    private final MutableLiveData<List<DepartmentsItem>> departments = new MutableLiveData<>();
    private final MutableLiveData<List<ShiftsItem>> shifts = new MutableLiveData<>();
    private final MutableLiveData<List<EmployeesItem>> employees = new MutableLiveData<>();
    private DepartmentsItem selectedDepartment;
    private ShiftsItem selectedShift;
    private EmployeesItem selectedReportingManager;
    private EmployeesItem selectedCrossManager;

    public LiveData<EmployeesItem> getSelectedEmployee() {
        return selectedEmployee;
    }

    public void setSelectedEmployee(EmployeesItem employee) {
        selectedEmployee.setValue(employee);
    }

    public LiveData<List<DepartmentsItem>> getDepartments() {
        return departments;
    }

    public void setDepartments(List<DepartmentsItem> list) {
        departments.setValue(list);
    }

    public LiveData<List<ShiftsItem>> getShifts() {
        return shifts;
    }

    public void setShifts(List<ShiftsItem> list) {
        shifts.setValue(list);
    }

    public LiveData<List<EmployeesItem>> getEmployees() {
        return employees;
    }

    public void setEmployees(List<EmployeesItem> list) {
        employees.setValue(list);
    }

    public DepartmentsItem getSelectedDepartment() {
        return selectedDepartment;
    }

    public void setSelectedDepartment(DepartmentsItem item) {
        this.selectedDepartment = item;
    }

    public ShiftsItem getSelectedShift() {
        return selectedShift;
    }

    public void setSelectedShift(ShiftsItem item) {
        this.selectedShift = item;
    }

    public EmployeesItem getSelectedReportingManager() {
        return selectedReportingManager;
    }

    public void setSelectedReportingManager(EmployeesItem item) {
        this.selectedReportingManager = item;
    }

    public EmployeesItem getSelectedCrossManager() {
        return selectedCrossManager;
    }

    public void setSelectedCrossManager(EmployeesItem item) {
        this.selectedCrossManager = item;
    }

    public void fetchDepartments(String token) {
        AdminAPIInterface apiService = AdminAPIClient.getInstance().getBase2();
        Call<DepartmentResponse> call = apiService.getDepartments("jwt " + token);
        call.enqueue(new Callback<DepartmentResponse>() {
            @Override
            public void onResponse(@NonNull Call<DepartmentResponse> call, @NonNull Response<DepartmentResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    departments.setValue(response.body().getData().getDepartments());
                }
            }

            @Override
            public void onFailure(@NonNull Call<DepartmentResponse> call, @NonNull Throwable t) {
                departments.setValue(null);
            }
        });
    }

    public void fetchShifts(String token) {
        AdminAPIInterface apiService = AdminAPIClient.getInstance().getBase2();
        Call<ActiveShiftResponse> call = apiService.getShifts("jwt " + token);
        call.enqueue(new Callback<ActiveShiftResponse>() {
            @Override
            public void onResponse(@NonNull Call<ActiveShiftResponse> call, @NonNull Response<ActiveShiftResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    shifts.setValue(response.body().getData().getShifts());
                } else {
                    shifts.setValue(null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ActiveShiftResponse> call, @NonNull Throwable t) {
                shifts.setValue(null);
            }
        });
    }

    public void fetchEmployees(String token) {
        AdminAPIInterface apiService = AdminAPIClient.getInstance().getBase2();
        Call<EmployeeDetailResponse> call = apiService.getEmployees("jwt " + token);
        call.enqueue(new Callback<EmployeeDetailResponse>() {
            @Override
            public void onResponse(@NonNull Call<EmployeeDetailResponse> call, @NonNull Response<EmployeeDetailResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    employees.setValue(response.body().getData().getEmployees());
                } else {
                    employees.setValue(null);
                }
            }

            @Override
            public void onFailure(Call<EmployeeDetailResponse> call, Throwable t) {
                employees.setValue(null);
            }
        });
    }

    public void updateEmployee(String token, String employeeId, UpdateEmployeeRequest payload, Callback<ResponseBody> callback) {
        AdminAPIInterface apiService = AdminAPIClient.getInstance().getBase2();
        Call<ResponseBody> call = apiService.updateEmployee("jwt " + token, employeeId, payload);
        call.enqueue(callback);
    }

}

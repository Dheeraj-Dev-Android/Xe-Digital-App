package app.xedigital.ai.admin.adminUI.employeeDetails;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import app.xedigital.ai.admin.adminAPI.AdminAPIClient;
import app.xedigital.ai.admin.adminAPI.AdminAPIInterface;
import app.xedigital.ai.admin.adminModal.ActiveShift.ActiveShiftResponse;
import app.xedigital.ai.admin.adminModal.ActiveShift.ShiftsItem;
import app.xedigital.ai.admin.adminModal.EmployeeDetails.EmployeeDetailResponse;
import app.xedigital.ai.admin.adminModal.EmployeeDetails.EmployeesItem;
import app.xedigital.ai.admin.adminModal.businessUnit.AllBuResponse;
import app.xedigital.ai.admin.adminModal.businessUnit.BusItem;
import app.xedigital.ai.admin.adminModal.department.DepartmentResponse;
import app.xedigital.ai.admin.adminModal.department.DepartmentsItem;
import app.xedigital.ai.admin.adminModal.partners.PartnersItem;
import app.xedigital.ai.admin.adminModal.partners.PartnersResponse;
import app.xedigital.ai.admin.adminModal.updateEmployee.UpdateEmployeeRequest;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditEmployeeViewModel extends ViewModel {

    private final MutableLiveData<EmployeesItem> selectedEmployee = new MutableLiveData<>();
    private final MutableLiveData<List<DepartmentsItem>> departments = new MutableLiveData<>();
    private final MutableLiveData<List<ShiftsItem>> shifts = new MutableLiveData<>();
    private final MutableLiveData<List<EmployeesItem>> employees = new MutableLiveData<>();
    private final MutableLiveData<List<PartnersItem>> partners = new MutableLiveData<>();
    private final MutableLiveData<List<BusItem>> bus = new MutableLiveData<>();

    private DepartmentsItem selectedDepartment;
    private ShiftsItem selectedShift;
    private EmployeesItem selectedReportingManager;
    private EmployeesItem selectedCrossManager;
    private PartnersItem selectedPartner;
    private BusItem selectedBu;

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

    public LiveData<List<PartnersItem>> getPartners() {
        return partners;
    }

    public LiveData<List<BusItem>> getBus() {
        return bus;
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

    public PartnersItem getSelectedPartner() {
        return selectedPartner;
    }

    public void setSelectedPartner(PartnersItem item) {
        this.selectedPartner = item;
    }

    public BusItem getSelectedBu() {
        return selectedBu;
    }

    public void setSelectedBu(BusItem item) {
        this.selectedBu = item;
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

    public void fetchPartners(String token) {
        AdminAPIInterface apiService = AdminAPIClient.getInstance().getBase2();
        Call<PartnersResponse> call = apiService.getPartners("jwt " + token);
        call.enqueue(new Callback<PartnersResponse>() {
            @Override
            public void onResponse(@NonNull Call<PartnersResponse> call, @NonNull Response<PartnersResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    partners.setValue(response.body().getData().getPartners());
                } else {
                    partners.setValue(null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<PartnersResponse> call, @NonNull Throwable t) {
                partners.setValue(null);
            }
        });
    }

    public void fetchBus(String token) {
        AdminAPIInterface apiService = AdminAPIClient.getInstance().getBase2();
        Call<AllBuResponse> call = apiService.getBus("jwt " + token);
        call.enqueue(new Callback<AllBuResponse>() {
            @Override
            public void onResponse(@NonNull Call<AllBuResponse> call, @NonNull Response<AllBuResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    bus.setValue(response.body().getData().getBus());
                } else {
                    bus.setValue(null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<AllBuResponse> call, @NonNull Throwable t) {
                bus.setValue(null);
            }
        });
    }

    public void updateEmployee(String token, String employeeId, UpdateEmployeeRequest payload, Callback<ResponseBody> callback) {
        AdminAPIInterface apiService = AdminAPIClient.getInstance().getBase2();
        Call<ResponseBody> call = apiService.updateEmployee("jwt " + token, employeeId, payload);
        call.enqueue(callback);
    }
}
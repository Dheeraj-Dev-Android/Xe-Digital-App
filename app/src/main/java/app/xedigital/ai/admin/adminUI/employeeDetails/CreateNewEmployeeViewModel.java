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
import app.xedigital.ai.admin.adminModal.addEmployee.AddEmployeeRequest;
import app.xedigital.ai.admin.adminModal.businessUnit.AllBuResponse;
import app.xedigital.ai.admin.adminModal.businessUnit.BusItem;
import app.xedigital.ai.admin.adminModal.department.DepartmentResponse;
import app.xedigital.ai.admin.adminModal.department.DepartmentsItem;
import app.xedigital.ai.admin.adminModal.partners.PartnersItem;
import app.xedigital.ai.admin.adminModal.partners.PartnersResponse;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateNewEmployeeViewModel extends ViewModel {

    // ── API data LiveData ──
    private final MutableLiveData<List<DepartmentsItem>> departments = new MutableLiveData<>();
    private final MutableLiveData<List<BusItem>> bus = new MutableLiveData<>();
    private final MutableLiveData<List<ShiftsItem>> shifts = new MutableLiveData<>();
    private final MutableLiveData<List<EmployeesItem>> employees = new MutableLiveData<>();
    private final MutableLiveData<List<PartnersItem>> partners = new MutableLiveData<>();

    // ── Error channel ──
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    // ── Loading state ──
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    // ── Selected items (for submit) ──
    private DepartmentsItem selectedDepartment;
    private BusItem selectedBu;
    private ShiftsItem selectedShift;
    private EmployeesItem selectedReportingManager;
    private EmployeesItem selectedCrossManager;
    private PartnersItem selectedPartner;

    // ════════════════════════════════════════════════════════════
    //  LIVEDATA GETTERS
    // ════════════════════════════════════════════════════════════

    public LiveData<List<DepartmentsItem>> getDepartments() {
        return departments;
    }

    public LiveData<List<BusItem>> getBus() {
        return bus;
    }

    public LiveData<List<ShiftsItem>> getShifts() {
        return shifts;
    }

    public LiveData<List<EmployeesItem>> getEmployees() {
        return employees;
    }

    public LiveData<List<PartnersItem>> getPartners() {
        return partners;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void setLoading(boolean loading) {
        isLoading.setValue(loading);
    }

    // ════════════════════════════════════════════════════════════
    //  SELECTED-ITEM ACCESSORS
    // ════════════════════════════════════════════════════════════

    public DepartmentsItem getSelectedDepartment() {
        return selectedDepartment;
    }

    public void setSelectedDepartment(DepartmentsItem item) {
        this.selectedDepartment = item;
    }

    public BusItem getSelectedBu() {
        return selectedBu;
    }

    public void setSelectedBu(BusItem item) {
        this.selectedBu = item;
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

    // ════════════════════════════════════════════════════════════
    //  FETCH METHODS
    // ════════════════════════════════════════════════════════════

    public void fetchDepartments(String token) {
        AdminAPIInterface api = AdminAPIClient.getInstance().getBase2();
        api.getDepartments("jwt " + token).enqueue(new Callback<DepartmentResponse>() {
            @Override
            public void onResponse(@NonNull Call<DepartmentResponse> call,
                                   @NonNull Response<DepartmentResponse> response) {
                if (response.isSuccessful() && response.body() != null
                        && response.body().getData() != null
                        && response.body().getData().getDepartments() != null) {
                    departments.postValue(response.body().getData().getDepartments());
                } else {
                    errorMessage.postValue("Failed to load departments");
                }
            }

            @Override
            public void onFailure(@NonNull Call<DepartmentResponse> call, @NonNull Throwable t) {
                errorMessage.postValue("Failed to load departments: " + t.getMessage());
            }
        });
    }

    public void fetchBus(String token) {
        AdminAPIInterface api = AdminAPIClient.getInstance().getBase2();
        api.getBus("jwt " + token).enqueue(new Callback<AllBuResponse>() {
            @Override
            public void onResponse(@NonNull Call<AllBuResponse> call,
                                   @NonNull Response<AllBuResponse> response) {
                if (response.isSuccessful() && response.body() != null
                        && response.body().getData() != null
                        && response.body().getData().getBus() != null) {
                    bus.postValue(response.body().getData().getBus());
                } else {
                    errorMessage.postValue("Failed to load business units");
                }
            }

            @Override
            public void onFailure(@NonNull Call<AllBuResponse> call, @NonNull Throwable t) {
                errorMessage.postValue("Failed to load business units: " + t.getMessage());
            }
        });
    }

    public void fetchShifts(String token) {
        AdminAPIInterface api = AdminAPIClient.getInstance().getBase2();
        api.getShifts("jwt " + token).enqueue(new Callback<ActiveShiftResponse>() {
            @Override
            public void onResponse(@NonNull Call<ActiveShiftResponse> call,
                                   @NonNull Response<ActiveShiftResponse> response) {
                if (response.isSuccessful() && response.body() != null
                        && response.body().getData() != null
                        && response.body().getData().getShifts() != null) {
                    shifts.postValue(response.body().getData().getShifts());
                } else {
                    errorMessage.postValue("Failed to load shifts");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ActiveShiftResponse> call, @NonNull Throwable t) {
                errorMessage.postValue("Failed to load shifts: " + t.getMessage());
            }
        });
    }

    public void fetchEmployees(String token) {
        AdminAPIInterface api = AdminAPIClient.getInstance().getBase2();
        api.getEmployees("jwt " + token).enqueue(new Callback<EmployeeDetailResponse>() {
            @Override
            public void onResponse(@NonNull Call<EmployeeDetailResponse> call,
                                   @NonNull Response<EmployeeDetailResponse> response) {
                if (response.isSuccessful() && response.body() != null
                        && response.body().getData() != null
                        && response.body().getData().getEmployees() != null) {
                    employees.postValue(response.body().getData().getEmployees());
                } else {
                    errorMessage.postValue("Failed to load employees");
                }
            }

            @Override
            public void onFailure(@NonNull Call<EmployeeDetailResponse> call, @NonNull Throwable t) {
                errorMessage.postValue("Failed to load employees: " + t.getMessage());
            }
        });
    }

    public void fetchPartners(String token) {
        AdminAPIInterface api = AdminAPIClient.getInstance().getBase2();
        api.getPartners("jwt " + token).enqueue(new Callback<PartnersResponse>() {
            @Override
            public void onResponse(@NonNull Call<PartnersResponse> call,
                                   @NonNull Response<PartnersResponse> response) {
                if (response.isSuccessful() && response.body() != null
                        && response.body().getData() != null
                        && response.body().getData().getPartners() != null) {
                    partners.postValue(response.body().getData().getPartners());
                } else {
                    errorMessage.postValue("Failed to load partners");
                }
            }

            @Override
            public void onFailure(@NonNull Call<PartnersResponse> call, @NonNull Throwable t) {
                errorMessage.postValue("Failed to load partners: " + t.getMessage());
            }
        });
    }

    // ════════════════════════════════════════════════════════════
    //  CREATE EMPLOYEE
    // ════════════════════════════════════════════════════════════

    public void createEmployee(String token, AddEmployeeRequest request, Callback<ResponseBody> callback) {
        AdminAPIInterface api = AdminAPIClient.getInstance().getBase2();
        api.addEmployee("jwt " + token, request).enqueue(callback);
    }
}
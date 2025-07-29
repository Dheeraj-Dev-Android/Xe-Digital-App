package app.xedigital.ai.adminUI.employeeDetails;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import app.xedigital.ai.model.Admin.EmployeeDetails.EmployeesItem;


public class EditEmployeeViewModel extends ViewModel {

    private final MutableLiveData<EmployeesItem> selectedEmployee = new MutableLiveData<>();

    public LiveData<EmployeesItem> getSelectedEmployee() {
        return selectedEmployee;
    }

    public void setSelectedEmployee(EmployeesItem employee) {
        selectedEmployee.setValue(employee);
    }
}

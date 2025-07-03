package app.xedigital.ai.adminActivity;

import androidx.annotation.NonNull;

import app.xedigital.ai.model.Admin.EmployeeDetails.EmployeesItem;


public class EmployeeDropdownItem {
    private final String id;
    private final String firstName;
    private final String lastName;
    private final EmployeesItem employeeItem;

    public EmployeeDropdownItem(String id, String firstName, String lastName, EmployeesItem employeeItem) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName == null ? "" : lastName;
        this.employeeItem = employeeItem;
    }

    public String getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public EmployeesItem getEmployeeItem() {
        return employeeItem;
    }

    @NonNull
    @Override
    public String toString() {
        return lastName.isEmpty()
                ? firstName
                : firstName + " " + lastName;
    }
}

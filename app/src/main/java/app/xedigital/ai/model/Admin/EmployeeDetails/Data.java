package app.xedigital.ai.model.Admin.EmployeeDetails;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Data {

    @SerializedName("employees")
    private List<EmployeesItem> employees;

    public List<EmployeesItem> getEmployees() {
        return employees;
    }
}
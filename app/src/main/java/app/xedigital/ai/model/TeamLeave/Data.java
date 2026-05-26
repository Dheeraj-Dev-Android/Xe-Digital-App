package app.xedigital.ai.model.TeamLeave;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Data {

    @SerializedName("employees")
    private List<EmployeesItem> employees;

    public List<EmployeesItem> getEmployees() {
        return employees;
    }

    public void setEmployees(List<EmployeesItem> employees) {
        this.employees = employees;
    }
}
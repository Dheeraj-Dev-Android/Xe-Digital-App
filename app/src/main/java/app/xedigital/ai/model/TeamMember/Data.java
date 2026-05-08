package app.xedigital.ai.model.TeamMember;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Data {

    @SerializedName("allEmployees")
    private List<AllEmployeesItem> allEmployees;

    @SerializedName("employees")
    private List<EmployeesItem> employees;

    public List<AllEmployeesItem> getAllEmployees() {
        return allEmployees;
    }

    public List<EmployeesItem> getEmployees() {
        return employees;
    }
}
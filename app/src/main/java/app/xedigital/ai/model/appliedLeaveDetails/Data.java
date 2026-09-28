package app.xedigital.ai.model.appliedLeaveDetails;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Data {

    @SerializedName("employeesAppliedLeaves")
    private List<EmployeesAppliedLeavesItem> employeesAppliedLeaves;

    public List<EmployeesAppliedLeavesItem> getEmployeesAppliedLeaves() {
        return employeesAppliedLeaves;
    }
}
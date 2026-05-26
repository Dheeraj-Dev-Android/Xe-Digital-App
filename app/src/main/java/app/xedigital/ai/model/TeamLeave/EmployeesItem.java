package app.xedigital.ai.model.TeamLeave;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class EmployeesItem {

    @SerializedName("employeeName")
    private String employeeName;

    @SerializedName("leaves")
    private List<LeavesItem> leaves;

    @SerializedName("employeeId")
    private String employeeId;

    @SerializedName("email")
    private String email;

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public List<LeavesItem> getLeaves() {
        return leaves;
    }

    public void setLeaves(List<LeavesItem> leaves) {
        this.leaves = leaves;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
package app.xedigital.ai.model.Admin.assignLeave;

import com.google.gson.annotations.SerializedName;

public class AssignLeaveRequest {

    @SerializedName("leavetype")
    private String leavetype;

    @SerializedName("totalLeave")
    private double totalLeave;

    @SerializedName("leaveCalculationType")
    private String leaveCalculationType;

    @SerializedName("assignedLeave")
    private double assignedLeave;

    @SerializedName("assignType")
    private String assignType;

    @SerializedName("employee")
    private String employee;

    @SerializedName("remarks")
    private String remarks;

    public void setLeavetype(String leavetype) {
        this.leavetype = leavetype;
    }

    public void setTotalLeave(double totalLeave) {
        this.totalLeave = totalLeave;
    }

    public void setLeaveCalculationType(String leaveCalculationType) {
        this.leaveCalculationType = leaveCalculationType;
    }

    public void setAssignedLeave(double assignedLeave) {
        this.assignedLeave = assignedLeave;
    }

    public void setAssignType(String assignType) {
        this.assignType = assignType;
    }

    public void setEmployee(String employee) {
        this.employee = employee;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
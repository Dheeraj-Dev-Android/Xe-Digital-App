package app.xedigital.ai.model.shiftApprovalList;

import com.google.gson.annotations.SerializedName;

public class EmployeeShiftdataItem {

    @SerializedName("approvedDate")
    private String approvedDate;

    @SerializedName("shiftType")
    private ShiftType shiftType;

    @SerializedName("reportingManager")
    private String reportingManager;

    @SerializedName("shift")
    private Shift shift;

    @SerializedName("shiftUpdate")
    private ShiftUpdate shiftUpdate;

    @SerializedName("appliedDate")
    private String appliedDate;

    @SerializedName("approvedByName")
    private String approvedByName;

    @SerializedName("_id")
    private String id;

    @SerializedName("employee")
    private Employee employee;

    @SerializedName("status")
    private String status;

    public String getApprovedDate() {
        return approvedDate;
    }

    public app.xedigital.ai.model.shiftApprove.ShiftType getShiftType() {
        return shiftType;
    }

    public String getReportingManager() {
        return reportingManager;
    }

    public app.xedigital.ai.model.shiftApprove.Shift getShift() {
        return shift;
    }

    public app.xedigital.ai.model.shiftApprove.ShiftUpdate getShiftUpdate() {
        return shiftUpdate;
    }

    public String getAppliedDate() {
        return appliedDate;
    }

    public String getApprovedByName() {
        return approvedByName;
    }

    public String getId() {
        return id;
    }

    public app.xedigital.ai.model.shiftApprove.Employee getEmployee() {
        return employee;
    }

    public String getStatus() {
        return status;
    }
}
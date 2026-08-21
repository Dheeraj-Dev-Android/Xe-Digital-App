package app.xedigital.ai.model.shiftApprovalList;

import com.google.gson.annotations.SerializedName;

public class EmployeeApproveShiftdataItem {

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

    // ---- Getters ----

    public String getApprovedDate() {
        return approvedDate;
    }

    public void setApprovedDate(String approvedDate) {
        this.approvedDate = approvedDate;
    }

    public ShiftType getShiftType() {
        return shiftType;
    }

    public String getReportingManager() {
        return reportingManager;
    }

    public Shift getShift() {
        return shift;
    }

    public ShiftUpdate getShiftUpdate() {
        return shiftUpdate;
    }

    public String getAppliedDate() {
        return appliedDate;
    }

    public String getApprovedByName() {
        return approvedByName;
    }

    public void setApprovedByName(String approvedByName) {
        this.approvedByName = approvedByName;
    }

    public String getId() {
        return id;
    }

    // ---- Setters (Added for local optimistic UI updates) ----

    public Employee getEmployee() {
        return employee;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
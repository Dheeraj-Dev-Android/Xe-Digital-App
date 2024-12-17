package app.xedigital.ai.model.shiftApprove;

import com.google.gson.annotations.SerializedName;

public class ShiftApproveRequest {

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

    @SerializedName("comment")
    private String comment;

    @SerializedName("_id")
    private String id;

    @SerializedName("employee")
    private Employee employee;

    @SerializedName("status")
    private String status;

    public String getApprovedDate() {
        return approvedDate;
    }

    public void setApprovedDate(String approvedDate) {
        this.approvedDate = approvedDate;
    }

    public ShiftType getShiftType() {
        return shiftType;
    }

    public void setShiftType(ShiftType shiftType) {
        this.shiftType = shiftType;
    }

    public String getReportingManager() {
        return reportingManager;
    }

    public void setReportingManager(String reportingManager) {
        this.reportingManager = reportingManager;
    }

    public Shift getShift() {
        return shift;
    }

    public void setShift(Shift shift) {
        this.shift = shift;
    }

    public ShiftUpdate getShiftUpdate() {
        return shiftUpdate;
    }

    public void setShiftUpdate(ShiftUpdate shiftUpdate) {
        this.shiftUpdate = shiftUpdate;
    }

    public String getAppliedDate() {
        return appliedDate;
    }

    public void setAppliedDate(String appliedDate) {
        this.appliedDate = appliedDate;
    }

    public String getApprovedByName() {
        return approvedByName;
    }

    public void setApprovedByName(String approvedByName) {
        this.approvedByName = approvedByName;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
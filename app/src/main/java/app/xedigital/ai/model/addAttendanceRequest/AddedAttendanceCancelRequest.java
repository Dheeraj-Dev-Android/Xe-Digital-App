package app.xedigital.ai.model.addAttendanceRequest;

import com.google.gson.annotations.SerializedName;

public class AddedAttendanceCancelRequest {

    @SerializedName("shift")
    private Shift shift;

    @SerializedName("appliedDate")
    private String appliedDate;

    @SerializedName("remark")
    private String remark;

    @SerializedName("employee")
    private Employee employee;

    @SerializedName("punchDate")
    private String punchDate;

    @SerializedName("punchIn")
    private String punchIn;

    @SerializedName("approvedDate")
    private String approvedDate;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("punchOut")
    private String punchOut;

    @SerializedName("punchOutAddress")
    private String punchOutAddress;

    @SerializedName("punchInAddress")
    private String punchInAddress;

    @SerializedName("approvedByName")
    private String approvedByName;

    @SerializedName("punchOutDate")
    private String punchOutDate;

    @SerializedName("_id")
    private String id;

    @SerializedName("status")
    private String status;

    public Shift getShift() {
        return shift;
    }

    public void setShift(Shift shift) {
        this.shift = shift;
    }

    public String getAppliedDate() {
        return appliedDate;
    }

    public void setAppliedDate(String appliedDate) {
        this.appliedDate = appliedDate;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public String getPunchDate() {
        return punchDate;
    }

    public void setPunchDate(String punchDate) {
        this.punchDate = punchDate;
    }

    public String getPunchIn() {
        return punchIn;
    }

    public void setPunchIn(String punchIn) {
        this.punchIn = punchIn;
    }

    public String getApprovedDate() {
        return approvedDate;
    }

    public void setApprovedDate(String approvedDate) {
        this.approvedDate = approvedDate;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getPunchOut() {
        return punchOut;
    }

    public void setPunchOut(String punchOut) {
        this.punchOut = punchOut;
    }

    public String getPunchOutAddress() {
        return punchOutAddress;
    }

    public void setPunchOutAddress(String punchOutAddress) {
        this.punchOutAddress = punchOutAddress;
    }

    public String getPunchInAddress() {
        return punchInAddress;
    }

    public void setPunchInAddress(String punchInAddress) {
        this.punchInAddress = punchInAddress;
    }

    public String getApprovedByName() {
        return approvedByName;
    }

    public void setApprovedByName(String approvedByName) {
        this.approvedByName = approvedByName;
    }

    public String getPunchOutDate() {
        return punchOutDate;
    }

    public void setPunchOutDate(String punchOutDate) {
        this.punchOutDate = punchOutDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
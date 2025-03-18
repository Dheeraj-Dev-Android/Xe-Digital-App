package app.xedigital.ai.model.regularizeUpdateStatus;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class RegularizeUpdateRequest implements Serializable {

    @SerializedName("punchInUpdated")
    private String punchInUpdated;

    @SerializedName("attendenceRegularizationRemark")
    private String attendenceRegularizationRemark;

    @SerializedName("punchOutUpdated")
    private String punchOutUpdated;

    @SerializedName("punchInAddressUpdated")
    private String punchInAddressUpdated;

    @SerializedName("shift")
    private Shift shift;

    @SerializedName("appliedDate")
    private String appliedDate;

    @SerializedName("employee")
    private Employee employee;

    @SerializedName("punchDate")
    private String punchDate;

    @SerializedName("punchIn")
    private String punchIn;

    @SerializedName("approvedDate")
    private String approvedDate;

    @SerializedName("punchOutAddressUpdated")
    private String punchOutAddressUpdated;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("punchOut")
    private String punchOut;

    @SerializedName("punchOutUpdatedDate")
    private String punchOutUpdatedDate;

    @SerializedName("punchOutAddress")
    private String punchOutAddress;

    @SerializedName("approvedByName")
    private String approvedByName;

    @SerializedName("punchOutDate")
    private String punchOutDate;

    @SerializedName("punchInAddress")
    private String punchInAddress;

    @SerializedName("_id")
    private String id;

    @SerializedName("attendance")
    private Attendance attendance;

    @SerializedName("status")
    private String status;

    public String getPunchInUpdated() {
        return punchInUpdated;
    }

    public void setPunchInUpdated(String punchInUpdated) {
        this.punchInUpdated = punchInUpdated;
    }

    public String getAttendenceRegularizationRemark() {
        return attendenceRegularizationRemark;
    }

    public void setAttendenceRegularizationRemark(String attendenceRegularizationRemark) {
        this.attendenceRegularizationRemark = attendenceRegularizationRemark;
    }

    public String getPunchOutUpdated() {
        return punchOutUpdated;
    }

    public void setPunchOutUpdated(String punchOutUpdated) {
        this.punchOutUpdated = punchOutUpdated;
    }

    public String getPunchInAddressUpdated() {
        return punchInAddressUpdated;
    }

    public void setPunchInAddressUpdated(String punchInAddressUpdated) {
        this.punchInAddressUpdated = punchInAddressUpdated;
    }

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

    public String getPunchOutAddressUpdated() {
        return punchOutAddressUpdated;
    }

    public void setPunchOutAddressUpdated(String punchOutAddressUpdated) {
        this.punchOutAddressUpdated = punchOutAddressUpdated;
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

    public String getPunchOutUpdatedDate() {
        return punchOutUpdatedDate;
    }

    public void setPunchOutUpdatedDate(String punchOutUpdatedDate) {
        this.punchOutUpdatedDate = punchOutUpdatedDate;
    }

    public String getPunchOutAddress() {
        return punchOutAddress;
    }

    public void setPunchOutAddress(String punchOutAddress) {
        this.punchOutAddress = punchOutAddress;
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

    public String getPunchInAddress() {
        return punchInAddress;
    }

    public void setPunchInAddress(String punchInAddress) {
        this.punchInAddress = punchInAddress;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Attendance getAttendance() {
        return attendance;
    }

    public void setAttendance(Attendance attendance) {
        this.attendance = attendance;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
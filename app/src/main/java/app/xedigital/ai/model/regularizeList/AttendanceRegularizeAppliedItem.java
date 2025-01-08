package app.xedigital.ai.model.regularizeList;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class AttendanceRegularizeAppliedItem implements Serializable {

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

    public String getAttendenceRegularizationRemark() {
        return attendenceRegularizationRemark;
    }

    public String getPunchOutUpdated() {
        return punchOutUpdated;
    }

    public String getPunchInAddressUpdated() {
        return punchInAddressUpdated;
    }

    public Shift getShift() {
        return shift;
    }

    public String getAppliedDate() {
        return appliedDate;
    }

    public Employee getEmployee() {
        return employee;
    }

    public String getPunchDate() {
        return punchDate;
    }

    public String getPunchIn() {
        return punchIn;
    }

    public String getApprovedDate() {
        return approvedDate;
    }

    public String getPunchOutAddressUpdated() {
        return punchOutAddressUpdated;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getPunchOut() {
        return punchOut;
    }

    public String getPunchOutUpdatedDate() {
        return punchOutUpdatedDate;
    }

    public String getPunchOutAddress() {
        return punchOutAddress;
    }

    public String getApprovedByName() {
        return approvedByName;
    }

    public String getPunchOutDate() {
        return punchOutDate;
    }

    public String getPunchInAddress() {
        return punchInAddress;
    }

    public String getId() {
        return id;
    }

    public Attendance getAttendance() {
        return attendance;
    }

    public String getStatus() {
        return status;
    }
}
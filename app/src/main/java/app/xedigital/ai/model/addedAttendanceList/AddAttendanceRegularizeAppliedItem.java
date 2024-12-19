package app.xedigital.ai.model.addedAttendanceList;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class AddAttendanceRegularizeAppliedItem implements Serializable {

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

    public String getAppliedDate() {
        return appliedDate;
    }

    public String getRemark() {
        return remark;
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

    public String getCreatedAt() {
        return createdAt;
    }

    public String getPunchOut() {
        return punchOut;
    }

    public String getPunchOutAddress() {
        return punchOutAddress;
    }

    public String getPunchInAddress() {
        return punchInAddress;
    }

    public String getApprovedByName() {
        return approvedByName;
    }

    public String getPunchOutDate() {
        return punchOutDate;
    }

    public String getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }
}
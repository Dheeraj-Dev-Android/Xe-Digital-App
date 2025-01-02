package app.xedigital.ai.model.cmLeaveApprovalPending;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class AppliedLeavesItem implements Serializable {

    @SerializedName("crossManagerEmail")
    private String crossManagerEmail;

    @SerializedName("reason")
    private String reason;

    @SerializedName("crossManager")
    private String crossManager;

    @SerializedName("empFirstName")
    private String empFirstName;

    @SerializedName("empEmail")
    private String empEmail;

    @SerializedName("approvedBy")
    private String approvedBy;

    @SerializedName("selectTypeFrom")
    private String selectTypeFrom;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("reportingManagerLastName")
    private String reportingManagerLastName;

    @SerializedName("crossManagerName")
    private String crossManagerName;

    @SerializedName("__v")
    private int v;

    @SerializedName("contactNumber")
    private String contactNumber;

    @SerializedName("approvedByName")
    private String approvedByName;

    @SerializedName("appliedBy")
    private String appliedBy;

    @SerializedName("company")
    private String company;

    @SerializedName("department")
    private String department;

    @SerializedName("updatedAt")
    private String updatedAt;

    @SerializedName("leaveName")
    private String leaveName;

    @SerializedName("leavingStation")
    private String leavingStation;

    @SerializedName("leavetype")
    private String leavetype;

    @SerializedName("reportingManager")
    private String reportingManager;

    @SerializedName("empLastName")
    private String empLastName;

    @SerializedName("toDate")
    private String toDate;

    @SerializedName("appliedDate")
    private String appliedDate;

    @SerializedName("reportingManagerName")
    private String reportingManagerName;

    @SerializedName("selectTypeTo")
    private String selectTypeTo;

    @SerializedName("fromDate")
    private String fromDate;

    @SerializedName("approvedDate")
    private String approvedDate;

    @SerializedName("vacationAddress")
    private String vacationAddress;

    @SerializedName("comment")
    private String comment;

    @SerializedName("_id")
    private String id;

    @SerializedName("hrEmail")
    private String hrEmail;

    @SerializedName("status")
    private String status;

    public String getCrossManagerEmail() {
        return crossManagerEmail;
    }

    public String getReason() {
        return reason;
    }

    public String getCrossManager() {
        return crossManager;
    }

    public String getEmpFirstName() {
        return empFirstName;
    }

    public String getEmpEmail() {
        return empEmail;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public String getSelectTypeFrom() {
        return selectTypeFrom;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getReportingManagerLastName() {
        return reportingManagerLastName;
    }

    public String getCrossManagerName() {
        return crossManagerName;
    }

    public int getV() {
        return v;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public String getApprovedByName() {
        return approvedByName;
    }

    public String getAppliedBy() {
        return appliedBy;
    }

    public String getCompany() {
        return company;
    }

    public String getDepartment() {
        return department;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public String getLeaveName() {
        return leaveName;
    }

    public String getLeavingStation() {
        return leavingStation;
    }

    public String getLeavetype() {
        return leavetype;
    }

    public String getReportingManager() {
        return reportingManager;
    }

    public String getEmpLastName() {
        return empLastName;
    }

    public String getToDate() {
        return toDate;
    }

    public String getAppliedDate() {
        return appliedDate;
    }

    public String getReportingManagerName() {
        return reportingManagerName;
    }

    public String getSelectTypeTo() {
        return selectTypeTo;
    }

    public String getFromDate() {
        return fromDate;
    }

    public String getApprovedDate() {
        return approvedDate;
    }

    public String getVacationAddress() {
        return vacationAddress;
    }

    public String getComment() {
        return comment;
    }

    public String getId() {
        return id;
    }

    public String getHrEmail() {
        return hrEmail;
    }

    public String getStatus() {
        return status;
    }
}
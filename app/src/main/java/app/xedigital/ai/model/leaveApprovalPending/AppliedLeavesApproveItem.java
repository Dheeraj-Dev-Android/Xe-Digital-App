package app.xedigital.ai.model.leaveApprovalPending;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class AppliedLeavesApproveItem implements Serializable {

    @SerializedName("reason")
    private String reason;

    @SerializedName("firstname")
    private String firstname;

    @SerializedName("approvedBy")
    private String approvedBy;

    @SerializedName("selectTypeFrom")
    private String selectTypeFrom;

    @SerializedName("employeeCode")
    private String employeeCode;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("contact")
    private String contact;

    @SerializedName("contactNumber")
    private String contactNumber;

    @SerializedName("approvedByName")
    private String approvedByName;

    @SerializedName("appliedBy")
    private String appliedBy;

    @SerializedName("email")
    private String email;

    @SerializedName("updatedAt")
    private String updatedAt;

    @SerializedName("leavingStation")
    private String leavingStation;

    @SerializedName("leavetype")
    private Leavetype leavetype;

    @SerializedName("toDate")
    private String toDate;

    @SerializedName("appliedDate")
    private String appliedDate;

    @SerializedName("selectTypeTo")
    private String selectTypeTo;

    @SerializedName("lastname")
    private String lastname;

    @SerializedName("fromDate")
    private String fromDate;

    @SerializedName("approvedDate")
    private String approvedDate;

    @SerializedName("vacationAddress")
    private String vacationAddress;

    @SerializedName("comment")
    private String comment;

    @SerializedName("designation")
    private String designation;

    @SerializedName("_id")
    private String id;

    @SerializedName("status")
    private String status;

    public String getReason() {
        return reason;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public String getSelectTypeFrom() {
        return selectTypeFrom;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getContact() {
        return contact;
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

    public String getEmail() {
        return email;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public String getLeavingStation() {
        return leavingStation;
    }

    public Leavetype getLeavetype() {
        return leavetype;
    }

    public String getToDate() {
        return toDate;
    }

    public String getAppliedDate() {
        return appliedDate;
    }

    public String getSelectTypeTo() {
        return selectTypeTo;
    }

    public String getLastname() {
        return lastname;
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

    public String getDesignation() {
        return designation;
    }

    public String getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }
}
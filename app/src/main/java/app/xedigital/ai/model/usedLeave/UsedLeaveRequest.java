package app.xedigital.ai.model.usedLeave;

import com.google.gson.annotations.SerializedName;

public class UsedLeaveRequest {

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

    @SerializedName("fUsedDays")
    private double fUsedDays;

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

    @SerializedName("tDays")
    private int tDays;

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

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }

    public void setSelectTypeFrom(String selectTypeFrom) {
        this.selectTypeFrom = selectTypeFrom;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void setFUsedDays(double fUsedDays) {
        this.fUsedDays = fUsedDays;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public void setApprovedByName(String approvedByName) {
        this.approvedByName = approvedByName;
    }

    public void setAppliedBy(String appliedBy) {
        this.appliedBy = appliedBy;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setLeavingStation(String leavingStation) {
        this.leavingStation = leavingStation;
    }

    public void setLeavetype(Leavetype leavetype) {
        this.leavetype = leavetype;
    }

    public void setToDate(String toDate) {
        this.toDate = toDate;
    }

    public void setTDays(int tDays) {
        this.tDays = tDays;
    }

    public void setAppliedDate(String appliedDate) {
        this.appliedDate = appliedDate;
    }

    public void setSelectTypeTo(String selectTypeTo) {
        this.selectTypeTo = selectTypeTo;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public void setFromDate(String fromDate) {
        this.fromDate = fromDate;
    }

    public void setApprovedDate(String approvedDate) {
        this.approvedDate = approvedDate;
    }

    public void setVacationAddress(String vacationAddress) {
        this.vacationAddress = vacationAddress;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
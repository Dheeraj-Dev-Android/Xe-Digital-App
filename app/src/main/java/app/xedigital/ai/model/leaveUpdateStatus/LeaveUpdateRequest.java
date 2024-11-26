package app.xedigital.ai.model.leaveUpdateStatus;

import com.google.gson.annotations.SerializedName;

public class LeaveUpdateRequest {

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

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }

    public String getSelectTypeFrom() {
        return selectTypeFrom;
    }

    public void setSelectTypeFrom(String selectTypeFrom) {
        this.selectTypeFrom = selectTypeFrom;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getApprovedByName() {
        return approvedByName;
    }

    public void setApprovedByName(String approvedByName) {
        this.approvedByName = approvedByName;
    }

    public String getAppliedBy() {
        return appliedBy;
    }

    public void setAppliedBy(String appliedBy) {
        this.appliedBy = appliedBy;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getLeavingStation() {
        return leavingStation;
    }

    public void setLeavingStation(String leavingStation) {
        this.leavingStation = leavingStation;
    }

    public Leavetype getLeavetype() {
        return leavetype;
    }

    public void setLeavetype(Leavetype leavetype) {
        this.leavetype = leavetype;
    }

    public String getToDate() {
        return toDate;
    }

    public void setToDate(String toDate) {
        this.toDate = toDate;
    }

    public String getAppliedDate() {
        return appliedDate;
    }

    public void setAppliedDate(String appliedDate) {
        this.appliedDate = appliedDate;
    }

    public String getSelectTypeTo() {
        return selectTypeTo;
    }

    public void setSelectTypeTo(String selectTypeTo) {
        this.selectTypeTo = selectTypeTo;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getFromDate() {
        return fromDate;
    }

    public void setFromDate(String fromDate) {
        this.fromDate = fromDate;
    }

    public String getApprovedDate() {
        return approvedDate;
    }

    public void setApprovedDate(String approvedDate) {
        this.approvedDate = approvedDate;
    }

    public String getVacationAddress() {
        return vacationAddress;
    }

    public void setVacationAddress(String vacationAddress) {
        this.vacationAddress = vacationAddress;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
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
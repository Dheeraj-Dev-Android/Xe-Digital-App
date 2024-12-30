package app.xedigital.ai.model.applyLeaves;

import com.google.gson.annotations.SerializedName;

public class ApplyLeaveRequest {

    @SerializedName("leaveName")
    private String leaveName;

    @SerializedName("crossManagerEmail")
    private String crossManagerEmail;

    @SerializedName("reason")
    private String reason;

    @SerializedName("leavingStation")
    private String leavingStation;

    @SerializedName("leavetype")
    private String leavetype;

    @SerializedName("reportingManager")
    private String reportingManager;

    @SerializedName("empLastName")
    private String empLastName;

    @SerializedName("crossManager")
    private String crossManager;

    @SerializedName("toDate")
    private String toDate;

    @SerializedName("empFirstName")
    private String empFirstName;

    @SerializedName("empEmail")
    private String empEmail;

    @SerializedName("appliedDate")
    private String appliedDate;

    @SerializedName("reportingManagerName")
    private String reportingManagerName;

    @SerializedName("selectTypeFrom")
    private String selectTypeFrom;

    @SerializedName("employee")
    private String employee;

    @SerializedName("selectTypeTo")
    private String selectTypeTo;

    @SerializedName("fromDate")
    private String fromDate;

    @SerializedName("vacationAddress")
    private String vacationAddress;

    @SerializedName("reportingManagerLastName")
    private String reportingManagerLastName;

    @SerializedName("crossManagerName")
    private String crossManagerName;

    @SerializedName("contactNumber")
    private String contactNumber;

    @SerializedName("department")
    private String department;

    @SerializedName("hrEmail")
    private String hrEmail;

    @SerializedName("status")
    private String status;

    public void setLeaveName(String leaveName) {
        this.leaveName = leaveName;
    }

    public String getLeaveName() {
        return leaveName;
    }

    public String getCrossManagerEmail() {
        return crossManagerEmail;
    }

    public void setCrossManagerEmail(String crossManagerEmail) {
        this.crossManagerEmail = crossManagerEmail;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public void setLeavingStation(String leavingStation) {
        this.leavingStation = leavingStation;
    }

    public String getLeavingStation() {
        return leavingStation;
    }

    public void setLeavetype(String leavetype) {
        this.leavetype = leavetype;
    }

    public String getLeavetype() {
        return leavetype;
    }

    public void setReportingManager(String reportingManager) {
        this.reportingManager = reportingManager;
    }

    public String getReportingManager() {
        return reportingManager;
    }

    public void setEmpLastName(String empLastName) {
        this.empLastName = empLastName;
    }

    public String getEmpLastName() {
        return empLastName;
    }

    public String getCrossManager() {
        return crossManager;
    }

    public void setCrossManager(String crossManager) {
        this.crossManager = crossManager;
    }

    public void setToDate(String toDate) {
        this.toDate = toDate;
    }

    public String getToDate() {
        return toDate;
    }

    public void setEmpFirstName(String empFirstName) {
        this.empFirstName = empFirstName;
    }

    public String getEmpFirstName() {
        return empFirstName;
    }

    public void setEmpEmail(String empEmail) {
        this.empEmail = empEmail;
    }

    public String getEmpEmail() {
        return empEmail;
    }

    public void setAppliedDate(String appliedDate) {
        this.appliedDate = appliedDate;
    }

    public String getAppliedDate() {
        return appliedDate;
    }

    public void setReportingManagerName(String reportingManagerName) {
        this.reportingManagerName = reportingManagerName;
    }

    public String getReportingManagerName() {
        return reportingManagerName;
    }

    public void setSelectTypeFrom(String selectTypeFrom) {
        this.selectTypeFrom = selectTypeFrom;
    }

    public String getSelectTypeFrom() {
        return selectTypeFrom;
    }

    public void setEmployee(String employee) {
        this.employee = employee;
    }

    public String getEmployee() {
        return employee;
    }

    public void setSelectTypeTo(String selectTypeTo) {
        this.selectTypeTo = selectTypeTo;
    }

    public String getSelectTypeTo() {
        return selectTypeTo;
    }

    public void setFromDate(String fromDate) {
        this.fromDate = fromDate;
    }

    public String getFromDate() {
        return fromDate;
    }

    public void setVacationAddress(String vacationAddress) {
        this.vacationAddress = vacationAddress;
    }

    public String getVacationAddress() {
        return vacationAddress;
    }

    public void setReportingManagerLastName(String reportingManagerLastName) {
        this.reportingManagerLastName = reportingManagerLastName;
    }

    public String getReportingManagerLastName() {
        return reportingManagerLastName;
    }

    public String getCrossManagerName() {
        return crossManagerName;
    }

    public void setCrossManagerName(String crossManagerName) {
        this.crossManagerName = crossManagerName;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getDepartment() {
        return department;
    }

    public void setHrEmail(String hrEmail) {
        this.hrEmail = hrEmail;
    }

    public String getHrEmail() {
        return hrEmail;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
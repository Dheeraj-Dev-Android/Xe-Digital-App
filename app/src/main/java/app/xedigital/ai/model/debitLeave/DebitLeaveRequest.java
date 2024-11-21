package app.xedigital.ai.model.debitLeave;

import com.google.gson.annotations.SerializedName;

public class DebitLeaveRequest {

    @SerializedName("leaveName")
    private String leaveName;

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

    @SerializedName("toDate")
    private String toDate;

    @SerializedName("empFirstName")
    private String empFirstName;

    @SerializedName("empEmail")
    private String empEmail;

    @SerializedName("tDays")
    private int tDays;

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

    @SerializedName("fUsedDays")
    private int fUsedDays;

    @SerializedName("reportingManagerLastName")
    private String reportingManagerLastName;

    @SerializedName("contactNumber")
    private String contactNumber;

    @SerializedName("department")
    private String department;

    @SerializedName("hrEmail")
    private String hrEmail;

    @SerializedName("status")
    private String status;

    public String getLeaveName() {
        return leaveName;
    }

    public void setLeaveName(String leaveName) {
        this.leaveName = leaveName;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getLeavingStation() {
        return leavingStation;
    }

    public void setLeavingStation(String leavingStation) {
        this.leavingStation = leavingStation;
    }

    public String getLeavetype() {
        return leavetype;
    }

    public void setLeavetype(String leavetype) {
        this.leavetype = leavetype;
    }

    public String getReportingManager() {
        return reportingManager;
    }

    public void setReportingManager(String reportingManager) {
        this.reportingManager = reportingManager;
    }

    public String getEmpLastName() {
        return empLastName;
    }

    public void setEmpLastName(String empLastName) {
        this.empLastName = empLastName;
    }

    public String getToDate() {
        return toDate;
    }

    public void setToDate(String toDate) {
        this.toDate = toDate;
    }

    public String getEmpFirstName() {
        return empFirstName;
    }

    public void setEmpFirstName(String empFirstName) {
        this.empFirstName = empFirstName;
    }

    public String getEmpEmail() {
        return empEmail;
    }

    public void setEmpEmail(String empEmail) {
        this.empEmail = empEmail;
    }

    public int getTDays() {
        return tDays;
    }

    public void setTDays(int tDays) {
        this.tDays = tDays;
    }

    public String getAppliedDate() {
        return appliedDate;
    }

    public void setAppliedDate(String appliedDate) {
        this.appliedDate = appliedDate;
    }

    public String getReportingManagerName() {
        return reportingManagerName;
    }

    public void setReportingManagerName(String reportingManagerName) {
        this.reportingManagerName = reportingManagerName;
    }

    public String getSelectTypeFrom() {
        return selectTypeFrom;
    }

    public void setSelectTypeFrom(String selectTypeFrom) {
        this.selectTypeFrom = selectTypeFrom;
    }

    public String getEmployee() {
        return employee;
    }

    public void setEmployee(String employee) {
        this.employee = employee;
    }

    public String getSelectTypeTo() {
        return selectTypeTo;
    }

    public void setSelectTypeTo(String selectTypeTo) {
        this.selectTypeTo = selectTypeTo;
    }

    public String getFromDate() {
        return fromDate;
    }

    public void setFromDate(String fromDate) {
        this.fromDate = fromDate;
    }

    public String getVacationAddress() {
        return vacationAddress;
    }

    public void setVacationAddress(String vacationAddress) {
        this.vacationAddress = vacationAddress;
    }

    public int getFUsedDays() {
        return fUsedDays;
    }

    public void setFUsedDays(int fUsedDays) {
        this.fUsedDays = fUsedDays;
    }

    public String getReportingManagerLastName() {
        return reportingManagerLastName;
    }

    public void setReportingManagerLastName(String reportingManagerLastName) {
        this.reportingManagerLastName = reportingManagerLastName;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getHrEmail() {
        return hrEmail;
    }

    public void setHrEmail(String hrEmail) {
        this.hrEmail = hrEmail;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
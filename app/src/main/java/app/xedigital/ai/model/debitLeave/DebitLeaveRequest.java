package app.xedigital.ai.model.debitLeave;

import com.google.gson.annotations.SerializedName;

public class DebitLeaveRequest {

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

    @SerializedName("selectTypeFrom")
    private Object selectTypeFrom;

    @SerializedName("employee")
    private String employee;

    @SerializedName("fUsedDays")
    private double fUsedDays;

    @SerializedName("reportingManagerLastName")
    private String reportingManagerLastName;

    @SerializedName("crossManagerName")
    private String crossManagerName;

    @SerializedName("contactNumber")
    private String contactNumber;

    @SerializedName("department")
    private String department;

    @SerializedName("shortLeaveEndTime")
    private String shortLeaveEndTime;

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

    @SerializedName("leavePlanned")
    private String leavePlanned;

    @SerializedName("tDays")
    private double tDays;

    @SerializedName("appliedDate")
    private String appliedDate;

    @SerializedName("reportingManagerName")
    private String reportingManagerName;

    @SerializedName("selectTypeTo")
    private Object selectTypeTo;

    @SerializedName("shortLeaveStartTime")
    private String shortLeaveStartTime;

    @SerializedName("fromDate")
    private String fromDate;

    @SerializedName("vacationAddress")
    private String vacationAddress;

    @SerializedName("shortLeaveTimingSlot")
    private String shortLeaveTimingSlot;

    @SerializedName("hrEmail")
    private String hrEmail;

    @SerializedName("status")
    private String status;

    public String getCrossManagerEmail() {
        return crossManagerEmail;
    }

    public void setCrossManagerEmail(String crossManagerEmail) {
        this.crossManagerEmail = crossManagerEmail;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getCrossManager() {
        return crossManager;
    }

    public void setCrossManager(String crossManager) {
        this.crossManager = crossManager;
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

    public Object getSelectTypeFrom() {
        return selectTypeFrom;
    }

    public void setSelectTypeFrom(Object selectTypeFrom) {
        this.selectTypeFrom = selectTypeFrom;
    }

    public String getEmployee() {
        return employee;
    }

    public void setEmployee(String employee) {
        this.employee = employee;
    }

    public double getFUsedDays() {
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

    public String getCrossManagerName() {
        return crossManagerName;
    }

    public void setCrossManagerName(String crossManagerName) {
        this.crossManagerName = crossManagerName;
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

    public String getShortLeaveEndTime() {
        return shortLeaveEndTime;
    }

    public void setShortLeaveEndTime(String shortLeaveEndTime) {
        this.shortLeaveEndTime = shortLeaveEndTime;
    }

    public String getLeaveName() {
        return leaveName;
    }

    public void setLeaveName(String leaveName) {
        this.leaveName = leaveName;
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

    public String getLeavePlanned() {
        return leavePlanned;
    }

    public void setLeavePlanned(String leavePlanned) {
        this.leavePlanned = leavePlanned;
    }

    public double getTDays() {
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

    public Object getSelectTypeTo() {
        return selectTypeTo;
    }

    public void setSelectTypeTo(Object selectTypeTo) {
        this.selectTypeTo = selectTypeTo;
    }

    public String getShortLeaveStartTime() {
        return shortLeaveStartTime;
    }

    public void setShortLeaveStartTime(String shortLeaveStartTime) {
        this.shortLeaveStartTime = shortLeaveStartTime;
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

    public String getShortLeaveTimingSlot() {
        return shortLeaveTimingSlot;
    }

    public void setShortLeaveTimingSlot(String shortLeaveTimingSlot) {
        this.shortLeaveTimingSlot = shortLeaveTimingSlot;
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
package app.xedigital.ai.model.attendance;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class EmployeePunchDataItem implements Serializable {


    private String totalTime;

    private String lateTime;
    private String overtime;
    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("punchDateFormat")
    private String punchDateFormat;

    @SerializedName("punchOut")
    private String punchOut;

    @SerializedName("shift")
    private Shift shift;

    @SerializedName("punchOutAddress")
    private String punchOutAddress;

    @SerializedName("punchOutDate")
    private String punchOutDate;

    @SerializedName("punchInAddress")
    private String punchInAddress;

    @SerializedName("_id")
    private String id;

    @SerializedName("employee")
    private Employee employee;

    @SerializedName("punchDate")
    private String punchDate;

    @SerializedName("punchIn")
    private String punchIn;

    @SerializedName("leaveName")
    private String leaveName;

    @SerializedName("appliedDate")
    private String appliedDate;

    @SerializedName("isOptional")
    private boolean isOptional;

    @SerializedName("holidayName")
    private String holidayName;

    @SerializedName("holidayDateFormat")
    private String holidayDateFormat;

    @SerializedName("holidayDate")
    private Object holidayDate;

    @SerializedName("reason")
    private String reason;

    @SerializedName("empLastName")
    private String empLastName;

    @SerializedName("toDate")
    private String toDate;

    @SerializedName("empFirstName")
    private String empFirstName;
    private String dayOfWeek;

    @SerializedName("empEmail")
    private String empEmail;

    @SerializedName("appliedLeaveDateFormat")
    private String appliedLeaveDateFormat;

    @SerializedName("selectTypeFrom")
    private String selectTypeFrom;

    @SerializedName("selectTypeTo")
    private String selectTypeTo;

    @SerializedName("fromDate")
    private String fromDate;

    @SerializedName("appliedLeavetoDateFormat")
    private String appliedLeavetoDateFormat;

    @SerializedName("status")
    private String status;

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getPunchDateFormat() {
        return punchDateFormat;
    }

    public void setPunchDateFormat(String punchDateFormat) {
        this.punchDateFormat = punchDateFormat;
    }

    public String getPunchOut() {
        return punchOut;
    }

    public void setPunchOut(String punchOut) {
        this.punchOut = punchOut;
    }

    public Shift getShift() {
        return shift;
    }

    public void setShift(Shift shift) {
        this.shift = shift;
    }

    public String getPunchOutAddress() {
        return punchOutAddress;
    }

    public void setPunchOutAddress(String punchOutAddress) {
        this.punchOutAddress = punchOutAddress;
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

    public String getLeaveName() {
        return leaveName;
    }

    public void setLeaveName(String leaveName) {
        this.leaveName = leaveName;
    }

    public String getAppliedDate() {
        return appliedDate;
    }

    public void setAppliedDate(String appliedDate) {
        this.appliedDate = appliedDate;
    }

    public boolean isIsOptional() {
        return isOptional;
    }

    public void setIsOptional(boolean isOptional) {
        this.isOptional = isOptional;
    }

    public String getHolidayName() {
        return holidayName;
    }

    public void setHolidayName(String holidayName) {
        this.holidayName = holidayName;
    }

    public String getHolidayDateFormat() {
        return holidayDateFormat;
    }

    public void setHolidayDateFormat(String holidayDateFormat) {
        this.holidayDateFormat = holidayDateFormat;
    }

    public Object getHolidayDate() {
        return holidayDate;
    }

    public void setHolidayDate(Object holidayDate) {
        this.holidayDate = holidayDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
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

    public String getAppliedLeaveDateFormat() {
        return appliedLeaveDateFormat;
    }

    public void setAppliedLeaveDateFormat(String appliedLeaveDateFormat) {
        this.appliedLeaveDateFormat = appliedLeaveDateFormat;
    }

    public String getSelectTypeFrom() {
        return selectTypeFrom;
    }

    public void setSelectTypeFrom(String selectTypeFrom) {
        this.selectTypeFrom = selectTypeFrom;
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

    public String getAppliedLeavetoDateFormat() {
        return appliedLeavetoDateFormat;
    }

    public void setAppliedLeavetoDateFormat(String appliedLeavetoDateFormat) {
        this.appliedLeavetoDateFormat = appliedLeavetoDateFormat;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(String totalTime) {
        this.totalTime = totalTime;
    }

    public String getLateTime() {
        return lateTime;
    }

    public void setLateTime(String lateTime) {
        this.lateTime = lateTime;
    }

    public String getOvertime() {
        return overtime;
    }

    public void setOvertime(String overtime) {
        this.overtime = overtime;
    }


}
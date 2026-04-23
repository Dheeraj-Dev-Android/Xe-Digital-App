package app.xedigital.ai.model.empAttandance;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class EmployeePunchDataItem {

    @SerializedName("punchDateFormat")
    private String punchDateFormat;

    @SerializedName("shift")
    private Shift shift;

    @SerializedName("employee")
    private Employee employee;

    @SerializedName("punchDate")
    private String punchDate;

    @SerializedName("holiday")
    private Object holiday;

    @SerializedName("punchIn")
    private String punchIn;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("dailyTotalWorkingHour")
    private String dailyTotalWorkingHour;

    @SerializedName("punchOut")
    private String punchOut;

    @SerializedName("appliedLeaves")
    private List<Object> appliedLeaves;

    @SerializedName("punchOutAddress")
    private String punchOutAddress;

    @SerializedName("punchOutDate")
    private String punchOutDate;

    @SerializedName("punchInAddress")
    private String punchInAddress;

    @SerializedName("_id")
    private String id;

    @SerializedName("leaveName")
    private String leaveName;

    @SerializedName("appliedDate")
    private String appliedDate;

    @SerializedName("holidayName")
    private String holidayName;

    @SerializedName("attendanceStatus")
    private String attendanceStatus;

    @SerializedName("holidayDate")
    private Object holidayDate;

    public String getPunchDateFormat() {
        return punchDateFormat;
    }

    public void setPunchDateFormat(String punchDateFormat) {
        this.punchDateFormat = punchDateFormat;
    }

    public Shift getShift() {
        return shift;
    }

    public void setShift(Shift shift) {
        this.shift = shift;
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

    public Object getHoliday() {
        return holiday;
    }

    public void setHoliday(Object holiday) {
        this.holiday = holiday;
    }

    public String getPunchIn() {
        return punchIn;
    }

    public void setPunchIn(String punchIn) {
        this.punchIn = punchIn;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getDailyTotalWorkingHour() {
        return dailyTotalWorkingHour;
    }

    public void setDailyTotalWorkingHour(String dailyTotalWorkingHour) {
        this.dailyTotalWorkingHour = dailyTotalWorkingHour;
    }

    public String getPunchOut() {
        return punchOut;
    }

    public void setPunchOut(String punchOut) {
        this.punchOut = punchOut;
    }

    public List<Object> getAppliedLeaves() {
        return appliedLeaves;
    }

    public void setAppliedLeaves(List<Object> appliedLeaves) {
        this.appliedLeaves = appliedLeaves;
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

    public String getHolidayName() {
        return holidayName;
    }

    public void setHolidayName(String holidayName) {
        this.holidayName = holidayName;
    }

    public String getAttendanceStatus() {
        return attendanceStatus;
    }

    public void setAttendanceStatus(String attendanceStatus) {
        this.attendanceStatus = attendanceStatus;
    }

    public Object getHolidayDate() {
        return holidayDate;
    }

    public void setHolidayDate(Object holidayDate) {
        this.holidayDate = holidayDate;
    }
}
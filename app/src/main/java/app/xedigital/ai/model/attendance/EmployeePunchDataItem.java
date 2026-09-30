package app.xedigital.ai.model.attendance;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class EmployeePunchDataItem implements Serializable {
    private String totalTime;

    private String lateTime;
    private String overtime;
    private String dayOfWeek;
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
    private List<AppliedLeavesItem> appliedLeaves;

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

    @SerializedName("holidayDate")
    private Object holidayDate;

    @SerializedName("attendanceStatus")
    private String attendanceStatus;

    public String getPunchDateFormat() {
        return punchDateFormat;
    }

    public Shift getShift() {
        return shift;
    }

    public Employee getEmployee() {
        return employee;
    }

    public String getPunchDate() {
        return punchDate;
    }

    public Object getHoliday() {
        return holiday;
    }

    public String getPunchIn() {
        return punchIn;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getDailyTotalWorkingHour() {
        return dailyTotalWorkingHour;
    }

    public String getPunchOut() {
        return punchOut;
    }

    public List<AppliedLeavesItem> getAppliedLeaves() {
        return appliedLeaves;
    }

    public String getPunchOutAddress() {
        return punchOutAddress;
    }

    public String getPunchOutDate() {
        return punchOutDate;
    }

    public String getPunchInAddress() {
        return punchInAddress;
    }

    public String getId() {
        return id;
    }

    public String getLeaveName() {
        return leaveName;
    }

    public String getAppliedDate() {
        return appliedDate;
    }

    public String getHolidayName() {
        return holidayName;
    }

    public Object getHolidayDate() {
        return holidayDate;
    }

    public String getAttendanceStatus() {
        return attendanceStatus;
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
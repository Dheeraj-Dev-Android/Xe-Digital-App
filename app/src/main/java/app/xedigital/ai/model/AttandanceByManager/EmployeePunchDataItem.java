package app.xedigital.ai.model.AttandanceByManager;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class EmployeePunchDataItem implements Serializable {

    @SerializedName("leaveName")
    private String leaveName;

    @SerializedName("punchDateFormat")
    private String punchDateFormat;

    @SerializedName("shift")
    private Shift shift;

    @SerializedName("appliedDate")
    private String appliedDate;

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

    @SerializedName("holidayName")
    private String holidayName;

    @SerializedName("holidayDate")
    private Object holidayDate;

    @SerializedName("attendanceStatus")
    private String attendanceStatus;

    // --- CUSTOM FIELDS FOR UI LOGIC ---
    @SerializedName("totalTime")
    private String totalTime;
    @SerializedName("overtime")
    private String overtime;
    @SerializedName("lateTime")
    private String lateTime;
    @SerializedName("isFullDayLeave")
    private boolean isFullDayLeave;

    public boolean isFullDayLeave() {
        return isFullDayLeave;
    }

    public void setFullDayLeave(boolean fullDayLeave) {
        isFullDayLeave = fullDayLeave;
    }

    public String getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(String totalTime) {
        this.totalTime = totalTime;
    }

    public String getOvertime() {
        return overtime;
    }

    public void setOvertime(String overtime) {
        this.overtime = overtime;
    }

    // --- GETTERS ---

    public String getLateTime() {
        return lateTime;
    }

    public void setLateTime(String lateTime) {
        this.lateTime = lateTime;
    }

    public String getLeaveName() {
        return leaveName;
    }

    public void setLeaveName(String leaveName) {
        this.leaveName = leaveName;
    }

    public String getPunchDateFormat() {
        return punchDateFormat;
    }

    public void setPunchDateFormat(String punchDateFormat) {
        this.punchDateFormat = punchDateFormat;
    }

    public Shift getShift() {
        return shift;
    }

    public String getAppliedDate() {
        return appliedDate;
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

    public List<Object> getAppliedLeaves() {
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

    public String getHolidayName() {
        return holidayName;
    }

    public Object getHolidayDate() {
        return holidayDate;
    }

    public String getAttendanceStatus() {
        return attendanceStatus;
    }
}
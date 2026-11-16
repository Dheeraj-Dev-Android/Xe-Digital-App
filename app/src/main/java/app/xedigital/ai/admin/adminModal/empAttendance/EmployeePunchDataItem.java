package app.xedigital.ai.admin.adminModal.empAttendance;

import java.io.Serializable;
import java.util.List;

import com.google.gson.annotations.SerializedName;

public class EmployeePunchDataItem implements Serializable {

    @SerializedName("leaveName")
    private String leaveName;

    @SerializedName("punchDateFormat")
    private String punchDateFormat;

    @SerializedName("shift")
    private Object shift;

    @SerializedName("appliedDate")
    private String appliedDate;

    @SerializedName("employee")
    private Employee employee;

    @SerializedName("punchDate")
    private String punchDate;

    @SerializedName("holiday")
    private Object holiday;

    @SerializedName("punchIn")
    private Object punchIn;

    @SerializedName("createdAt")
    private Object createdAt;

    @SerializedName("dailyTotalWorkingHour")
    private Object dailyTotalWorkingHour;

    @SerializedName("punchOut")
    private Object punchOut;

    @SerializedName("appliedLeaves")
    private List<Object> appliedLeaves;

    @SerializedName("punchOutAddress")
    private Object punchOutAddress;

    @SerializedName("punchOutDate")
    private Object punchOutDate;

    @SerializedName("punchInAddress")
    private Object punchInAddress;

    @SerializedName("_id")
    private Object id;

    @SerializedName("holidayName")
    private String holidayName;

    @SerializedName("attendanceStatus")
    private String attendanceStatus;

    @SerializedName("holidayDate")
    private Object holidayDate;

    public String getLeaveName() {
        return leaveName;
    }

    public String getPunchDateFormat() {
        return punchDateFormat;
    }

    public Object getShift() {
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

    public Object getPunchIn() {
        return punchIn;
    }

    public Object getCreatedAt() {
        return createdAt;
    }

    public Object getDailyTotalWorkingHour() {
        return dailyTotalWorkingHour;
    }

    public Object getPunchOut() {
        return punchOut;
    }

    public List<Object> getAppliedLeaves() {
        return appliedLeaves;
    }

    public Object getPunchOutAddress() {
        return punchOutAddress;
    }

    public Object getPunchOutDate() {
        return punchOutDate;
    }

    public Object getPunchInAddress() {
        return punchInAddress;
    }

    public Object getId() {
        return id;
    }

    public String getHolidayName() {
        return holidayName;
    }

    public String getAttendanceStatus() {
        return attendanceStatus;
    }

    public Object getHolidayDate() {
        return holidayDate;
    }
}
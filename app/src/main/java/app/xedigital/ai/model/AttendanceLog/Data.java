package app.xedigital.ai.model.AttendanceLog;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Data {

    @SerializedName("attendanceLogs")
    private List<AttendanceLogsItem> attendanceLogs;

    public List<AttendanceLogsItem> getAttendanceLogs() {
        return attendanceLogs;
    }
}
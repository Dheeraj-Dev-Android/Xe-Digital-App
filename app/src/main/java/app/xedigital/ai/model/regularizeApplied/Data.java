package app.xedigital.ai.model.regularizeApplied;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Data {

    @SerializedName("attendanceRegularizeApplied")
    private List<AttendanceRegularizeAppliedItem> attendanceRegularizeApplied;

    public List<AttendanceRegularizeAppliedItem> getAttendanceRegularizeApplied() {
        return attendanceRegularizeApplied;
    }
}
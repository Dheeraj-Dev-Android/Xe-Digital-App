package app.xedigital.ai.admin.adminModal.AttendanceRegularizationApproval;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Data {

    @SerializedName("attendanceRegularizeApplied")
    private List<AttendanceRegularizeAppliedItem> attendanceRegularizeApplied;

    public List<AttendanceRegularizeAppliedItem> getAttendanceRegularizeApplied() {
        return attendanceRegularizeApplied;
    }
}
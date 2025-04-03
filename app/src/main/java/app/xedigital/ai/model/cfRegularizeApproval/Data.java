package app.xedigital.ai.model.cfRegularizeApproval;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Data {

    @SerializedName("attendanceReg")
    private List<AttendanceRegItem> attendanceReg;

    public List<AttendanceRegItem> getAttendanceReg() {
        return attendanceReg;
    }

    public void setAttendanceReg(List<AttendanceRegItem> attendanceReg) {
        this.attendanceReg = attendanceReg; // Changed to match your class
    }
}
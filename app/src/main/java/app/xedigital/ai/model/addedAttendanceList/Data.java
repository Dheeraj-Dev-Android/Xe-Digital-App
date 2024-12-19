package app.xedigital.ai.model.addedAttendanceList;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Data {

    @SerializedName("addAttendanceRegularizeApplied")
    private List<AddAttendanceRegularizeAppliedItem> addAttendanceRegularizeApplied;

    public List<AddAttendanceRegularizeAppliedItem> getAddAttendanceRegularizeApplied() {
        return addAttendanceRegularizeApplied;
    }
}
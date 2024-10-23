package app.xedigital.ai.model.regularizeList;

import java.util.List;
import com.google.gson.annotations.SerializedName;

public class Data{

	@SerializedName("attendanceRegularizeApplied")
	private List<AttendanceRegularizeAppliedItem> attendanceRegularizeApplied;

	public List<AttendanceRegularizeAppliedItem> getAttendanceRegularizeApplied(){
		return attendanceRegularizeApplied;
	}
}
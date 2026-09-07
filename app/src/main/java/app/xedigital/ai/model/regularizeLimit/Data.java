package app.xedigital.ai.model.regularizeLimit;

import com.google.gson.annotations.SerializedName;

public class Data {

    @SerializedName("attendenceRegularization")
    private boolean attendenceRegularization;

    @SerializedName("attendenceRegularizationCount")
    private int attendenceRegularizationCount;

    public boolean isAttendenceRegularization() {
        return attendenceRegularization;
    }

    public int getAttendenceRegularizationCount() {
        return attendenceRegularizationCount;
    }
}
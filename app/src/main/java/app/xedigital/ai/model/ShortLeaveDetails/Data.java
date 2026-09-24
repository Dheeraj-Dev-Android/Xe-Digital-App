package app.xedigital.ai.model.ShortLeaveDetails;

import com.google.gson.annotations.SerializedName;

public class Data {

    @SerializedName("shortLeaveExemption")
    private int shortLeaveExemption;

    @SerializedName("shortLeaveCount")
    private int shortLeaveCount;

    @SerializedName("shortLeave")
    private boolean shortLeave;

    @SerializedName("shortLeaveTiming")
    private boolean shortLeaveTiming;

    public int getShortLeaveExemption() {
        return shortLeaveExemption;
    }

    public int getShortLeaveCount() {
        return shortLeaveCount;
    }

    public boolean isShortLeave() {
        return shortLeave;
    }

    public boolean isShortLeaveTiming() {
        return shortLeaveTiming;
    }
}
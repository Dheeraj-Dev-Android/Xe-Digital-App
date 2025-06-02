package app.xedigital.ai.model.Admin.VisitorManual;

import com.google.gson.annotations.SerializedName;

public class Shift {

    @SerializedName("format")
    private int format;

    @SerializedName("name")
    private String name;

    @SerializedName("active")
    private boolean active;

    @SerializedName("startTime")
    private String startTime;

    @SerializedName("endTime")
    private String endTime;

    @SerializedName("_id")
    private String id;

    @SerializedName("timeWaiver")
    private int timeWaiver;

    public void setFormat(int format) {
        this.format = format;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTimeWaiver(int timeWaiver) {
        this.timeWaiver = timeWaiver;
    }
}
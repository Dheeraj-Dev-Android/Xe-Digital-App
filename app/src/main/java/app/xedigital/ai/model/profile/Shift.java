package app.xedigital.ai.model.profile;

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

    public int getFormat() {
        return format;
    }

    public void setFormat(int format) {
        this.format = format;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getTimeWaiver() {
        return timeWaiver;
    }

    public void setTimeWaiver(int timeWaiver) {
        this.timeWaiver = timeWaiver;
    }
}
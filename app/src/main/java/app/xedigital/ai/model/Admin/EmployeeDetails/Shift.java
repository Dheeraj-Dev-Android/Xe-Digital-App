package app.xedigital.ai.model.Admin.EmployeeDetails;

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

    public String getName() {
        return name;
    }

    public boolean isActive() {
        return active;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getId() {
        return id;
    }

    public int getTimeWaiver() {
        return timeWaiver;
    }
}
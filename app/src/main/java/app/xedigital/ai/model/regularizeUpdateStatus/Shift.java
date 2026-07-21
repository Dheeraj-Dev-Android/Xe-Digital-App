package app.xedigital.ai.model.regularizeUpdateStatus;

import com.google.gson.annotations.SerializedName;

public class Shift {

    @SerializedName("fromHour")
    private int fromHour;

    @SerializedName("fromMinutes")
    private int fromMinutes;

    @SerializedName("format")
    private int format;

    @SerializedName("active")
    private boolean active;

    @SerializedName("toHour")
    private int toHour;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("toMinutes")
    private int toMinutes;

    @SerializedName("createdBy")
    private String createdBy;

    @SerializedName("__v")
    private int v;

    @SerializedName("name")
    private String name;

    @SerializedName("startTime")
    private String startTime;

    @SerializedName("company")
    private String company;

    @SerializedName("_id")
    private String id;

    @SerializedName("endTime")
    private String endTime;

    @SerializedName("timeWaiver")
    private int timeWaiver;

    @SerializedName("updatedAt")
    private String updatedAt;

    public void setFromHour(int fromHour) {
        this.fromHour = fromHour;
    }

    public void setFromMinutes(int fromMinutes) {
        this.fromMinutes = fromMinutes;
    }

    public void setFormat(int format) {
        this.format = format;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setToHour(int toHour) {
        this.toHour = toHour;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void setToMinutes(int toMinutes) {
        this.toMinutes = toMinutes;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public void setV(int v) {
        this.v = v;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public void setTimeWaiver(int timeWaiver) {
        this.timeWaiver = timeWaiver;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
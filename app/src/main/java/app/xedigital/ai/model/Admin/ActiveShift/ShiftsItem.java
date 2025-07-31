package app.xedigital.ai.model.Admin.ActiveShift;

import com.google.gson.annotations.SerializedName;

public class ShiftsItem {

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

    @SerializedName("comment")
    private String comment;

    @SerializedName("company")
    private String company;

    @SerializedName("endTime")
    private String endTime;

    @SerializedName("_id")
    private String id;

    @SerializedName("timeWaiver")
    private int timeWaiver;

    @SerializedName("updatedAt")
    private String updatedAt;

    @SerializedName("shiftType")
    private ShiftType shiftType;

    public int getFromHour() {
        return fromHour;
    }

    public int getFromMinutes() {
        return fromMinutes;
    }

    public int getFormat() {
        return format;
    }

    public boolean isActive() {
        return active;
    }

    public int getToHour() {
        return toHour;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public int getToMinutes() {
        return toMinutes;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public int getV() {
        return v;
    }

    public String getName() {
        return name;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getComment() {
        return comment;
    }

    public String getCompany() {
        return company;
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

    public String getUpdatedAt() {
        return updatedAt;
    }

    public ShiftType getShiftType() {
        return shiftType;
    }
}
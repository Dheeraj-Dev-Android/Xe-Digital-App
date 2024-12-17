package app.xedigital.ai.model.shiftApprove;

import com.google.gson.annotations.SerializedName;

public class ShiftUpdate {

    @SerializedName("shiftType")
    private String shiftType;

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

    @SerializedName("_id")
    private String id;

    @SerializedName("endTime")
    private String endTime;

    @SerializedName("timeWaiver")
    private int timeWaiver;

    @SerializedName("updatedAt")
    private String updatedAt;

    public String getShiftType() {
        return shiftType;
    }

    public void setShiftType(String shiftType) {
        this.shiftType = shiftType;
    }

    public int getFromHour() {
        return fromHour;
    }

    public void setFromHour(int fromHour) {
        this.fromHour = fromHour;
    }

    public int getFromMinutes() {
        return fromMinutes;
    }

    public void setFromMinutes(int fromMinutes) {
        this.fromMinutes = fromMinutes;
    }

    public int getFormat() {
        return format;
    }

    public void setFormat(int format) {
        this.format = format;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getToHour() {
        return toHour;
    }

    public void setToHour(int toHour) {
        this.toHour = toHour;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public int getToMinutes() {
        return toMinutes;
    }

    public void setToMinutes(int toMinutes) {
        this.toMinutes = toMinutes;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public int getV() {
        return v;
    }

    public void setV(int v) {
        this.v = v;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public int getTimeWaiver() {
        return timeWaiver;
    }

    public void setTimeWaiver(int timeWaiver) {
        this.timeWaiver = timeWaiver;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
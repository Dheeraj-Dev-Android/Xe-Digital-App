package app.xedigital.ai.model.holiday;

import com.google.gson.annotations.SerializedName;

public class HolidaysItem {

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("createdBy")
    private String createdBy;

    @SerializedName("__v")
    private int v;

    @SerializedName("active")
    private boolean active;

    @SerializedName("company")
    private String company;

    @SerializedName("_id")
    private String id;

    @SerializedName("isOptional")
    private boolean isOptional;

    @SerializedName("holidayName")
    private String holidayName;

    @SerializedName("holidayDate")
    private String holidayDate;

    @SerializedName("updatedAt")
    private String updatedAt;

    public String getCreatedAt() {
        return createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public int getV() {
        return v;
    }

    public boolean isActive() {
        return active;
    }

    public String getCompany() {
        return company;
    }

    public String getId() {
        return id;
    }

    public boolean isIsOptional() {
        return isOptional;
    }

    public String getHolidayName() {
        return holidayName;
    }

    public String getHolidayDate() {
        return holidayDate;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }
}
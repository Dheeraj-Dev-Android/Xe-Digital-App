package app.xedigital.ai.model.holiday;


import com.google.gson.annotations.SerializedName;

public class Holiday {

    @SerializedName("active")
    private boolean active;

    @SerializedName("company")
    private String company;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("createdBy")
    private String createdBy;

    @SerializedName("holidayDate")
    private String holidayDate;

    @SerializedName("holidayName")
    private String holidayName;

    @SerializedName("_id")
    private String id;

    @SerializedName("isOptional")
    private boolean isOptional;

    @SerializedName("updatedAt")
    private String updatedAt;

    @SerializedName("__v")
    private int v;

    // Getters and setters
    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getHolidayDate() {
        return holidayDate;
    }

    public void setHolidayDate(String holidayDate) {
        this.holidayDate = holidayDate;
    }

    public String getHolidayName() {
        return holidayName;
    }

    public void setHolidayName(String holidayName) {
        this.holidayName = holidayName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isOptional() {
        return isOptional;
    }

    public void setOptional(boolean optional) {
        isOptional = optional;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int getV() {
        return v;
    }

    public void setV(int v) {
        this.v = v;
    }
}

package app.xedigital.ai.model.shiftApprovalList;

import com.google.gson.annotations.SerializedName;

public class ShiftType {

    @SerializedName("shifttypeName")
    private String shifttypeName;

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

    @SerializedName("updatedAt")
    private String updatedAt;

    public String getShifttypeName() {
        return shifttypeName;
    }

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

    public String getUpdatedAt() {
        return updatedAt;
    }
}
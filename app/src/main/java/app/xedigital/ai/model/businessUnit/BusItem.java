package app.xedigital.ai.model.businessUnit;

import com.google.gson.annotations.SerializedName;

public class BusItem {

    @SerializedName("buId")
    private String buId;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("createdBy")
    private String createdBy;

    @SerializedName("contact")
    private long contact;

    @SerializedName("__v")
    private int v;

    @SerializedName("name")
    private String name;

    @SerializedName("description")
    private String description;

    @SerializedName("active")
    private String active;

    @SerializedName("_id")
    private String id;

    @SerializedName("email")
    private String email;

    @SerializedName("updatedAt")
    private String updatedAt;

    public String getBuId() {
        return buId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public long getContact() {
        return contact;
    }

    public int getV() {
        return v;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getActive() {
        return active;
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }
}
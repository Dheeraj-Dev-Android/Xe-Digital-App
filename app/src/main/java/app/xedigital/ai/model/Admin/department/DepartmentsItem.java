package app.xedigital.ai.model.Admin.department;

import com.google.gson.annotations.SerializedName;

public class DepartmentsItem {

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("default")
    private boolean jsonMemberDefault;

    @SerializedName("createdBy")
    private String createdBy;

    @SerializedName("__v")
    private int v;

    @SerializedName("name")
    private String name;

    @SerializedName("description")
    private String description;

    @SerializedName("active")
    private boolean active;

    @SerializedName("company")
    private String company;

    @SerializedName("_id")
    private String id;

    @SerializedName("updatedAt")
    private String updatedAt;

    public String getCreatedAt() {
        return createdAt;
    }

    public boolean isJsonMemberDefault() {
        return jsonMemberDefault;
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

    public String getDescription() {
        return description;
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
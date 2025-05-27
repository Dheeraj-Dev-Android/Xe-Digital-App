package app.xedigital.ai.model.Admin.UserDetails;

import com.google.gson.annotations.SerializedName;

public class PermissionsItem {

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("default")
    private boolean jsonMemberDefault;

    @SerializedName("displayName")
    private String displayName;

    @SerializedName("name")
    private String name;

    @SerializedName("description")
    private String description;

    @SerializedName("active")
    private boolean active;

    @SerializedName("_id")
    private String id;

    public String getCreatedAt() {
        return createdAt;
    }

    public boolean isJsonMemberDefault() {
        return jsonMemberDefault;
    }

    public String getDisplayName() {
        return displayName;
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

    public String getId() {
        return id;
    }
}
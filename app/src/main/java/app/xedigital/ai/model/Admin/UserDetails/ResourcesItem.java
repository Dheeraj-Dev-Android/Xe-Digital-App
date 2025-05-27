package app.xedigital.ai.model.Admin.UserDetails;

import com.google.gson.annotations.SerializedName;

public class ResourcesItem {

    @SerializedName("path")
    private String path;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("default")
    private boolean jsonMemberDefault;

    @SerializedName("displayName")
    private String displayName;

    @SerializedName("name")
    private String name;

    @SerializedName("icon")
    private String icon;

    @SerializedName("description")
    private String description;

    @SerializedName("active")
    private boolean active;

    @SerializedName("position")
    private int position;

    @SerializedName("_id")
    private String id;

    @SerializedName("class")
    private String jsonMemberClass;

    public String getPath() {
        return path;
    }

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

    public String getIcon() {
        return icon;
    }

    public String getDescription() {
        return description;
    }

    public boolean isActive() {
        return active;
    }

    public int getPosition() {
        return position;
    }

    public String getId() {
        return id;
    }

    public String getJsonMemberClass() {
        return jsonMemberClass;
    }
}
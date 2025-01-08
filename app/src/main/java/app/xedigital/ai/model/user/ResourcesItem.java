package app.xedigital.ai.model.user;

import com.google.gson.annotations.SerializedName;

public class ResourcesItem {
    private String path;
    private String createdAt;
    private boolean jsonMemberDefault;
    private String displayName;
    private String name;
    private String icon;
    private String description;
    private boolean active;
    private int position;
    @SerializedName("_id")
    private String id;
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

package app.xedigital.ai.model.profile;

import com.google.gson.annotations.SerializedName;

public class Department {

    @SerializedName("default")
    private boolean jsonMemberDefault;

    @SerializedName("name")
    private String name;

    @SerializedName("description")
    private String description;

    @SerializedName("active")
    private boolean active;

    @SerializedName("_id")
    private String id;

    public boolean isJsonMemberDefault() {
        return jsonMemberDefault;
    }

    public void setJsonMemberDefault(boolean jsonMemberDefault) {
        this.jsonMemberDefault = jsonMemberDefault;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
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

    public void setJsonMemberDefault(boolean jsonMemberDefault) {
        this.jsonMemberDefault = jsonMemberDefault;
    }

    public boolean isJsonMemberDefault() {
        return jsonMemberDefault;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
package app.xedigital.ai.model.Admin.EmployeeDetails;

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
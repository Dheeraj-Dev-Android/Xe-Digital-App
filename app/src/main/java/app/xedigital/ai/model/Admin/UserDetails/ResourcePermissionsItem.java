package app.xedigital.ai.model.Admin.UserDetails;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ResourcePermissionsItem {

    @SerializedName("resource")
    private Resource resource;

    @SerializedName("permissions")
    private List<Object> permissions;

    @SerializedName("_id")
    private String id;

    public Resource getResource() {
        return resource;
    }

    public List<Object> getPermissions() {
        return permissions;
    }

    public String getId() {
        return id;
    }
}
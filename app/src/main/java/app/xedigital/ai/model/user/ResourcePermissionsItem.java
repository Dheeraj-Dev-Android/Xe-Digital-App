package app.xedigital.ai.model.user;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ResourcePermissionsItem {
    private Resource resource;
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
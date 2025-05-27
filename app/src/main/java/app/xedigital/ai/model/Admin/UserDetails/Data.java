package app.xedigital.ai.model.Admin.UserDetails;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Data {

    @SerializedName("role")
    private Role role;

    @SerializedName("resourcePermissions")
    private List<ResourcePermissionsItem> resourcePermissions;

    @SerializedName("resources")
    private List<ResourcesItem> resources;

    @SerializedName("company")
    private Company company;

    @SerializedName("user")
    private User user;

    @SerializedName("branch")
    private Branch branch;

    public Role getRole() {
        return role;
    }

    public List<ResourcePermissionsItem> getResourcePermissions() {
        return resourcePermissions;
    }

    public List<ResourcesItem> getResources() {
        return resources;
    }

    public Company getCompany() {
        return company;
    }

    public User getUser() {
        return user;
    }

    public Branch getBranch() {
        return branch;
    }
}
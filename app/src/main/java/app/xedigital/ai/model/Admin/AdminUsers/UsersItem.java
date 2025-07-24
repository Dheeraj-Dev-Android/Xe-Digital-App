package app.xedigital.ai.model.Admin.AdminUsers;

import com.google.gson.annotations.SerializedName;

public class UsersItem {

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("firstname")
    private String firstname;

    @SerializedName("role")
    private Role role;

    @SerializedName("provider")
    private String provider;

    @SerializedName("active")
    private boolean active;

    @SerializedName("company")
    private Company company;

    @SerializedName("_id")
    private String id;

    @SerializedName("branch")
    private Branch branch;

    @SerializedName("email")
    private String email;

    @SerializedName("lastname")
    private String lastname;

    public String getCreatedAt() {
        return createdAt;
    }

    public String getFirstname() {
        return firstname;
    }

    public Role getRole() {
        return role;
    }

    public String getProvider() {
        return provider;
    }

    public boolean isActive() {
        return active;
    }

    public Company getCompany() {
        return company;
    }

    public String getId() {
        return id;
    }

    public Branch getBranch() {
        return branch;
    }

    public String getEmail() {
        return email;
    }

    public String getLastname() {
        return lastname;
    }
}
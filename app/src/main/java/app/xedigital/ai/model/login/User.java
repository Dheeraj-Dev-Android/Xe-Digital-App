package app.xedigital.ai.model.login;

import com.google.gson.annotations.SerializedName;

public class User {

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("firstname")
    private String firstname;

    @SerializedName("active")
    private boolean active;

    @SerializedName("_id")
    private String id;

    @SerializedName("email")
    private String email;

    @SerializedName("lastname")
    private String lastname;

    @SerializedName("updatedAt")
    private String updatedAt;

    public String getCreatedAt() {
        return createdAt;
    }

    public String getFirstname() {
        return firstname;
    }

    public boolean isActive() {
        return active;
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getLastname() {
        return lastname;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }
}
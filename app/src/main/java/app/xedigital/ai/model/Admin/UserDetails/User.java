package app.xedigital.ai.model.Admin.UserDetails;

import com.google.gson.annotations.SerializedName;

public class User {

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
}
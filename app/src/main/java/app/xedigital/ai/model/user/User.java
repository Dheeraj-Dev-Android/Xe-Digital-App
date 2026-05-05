package app.xedigital.ai.model.user;

import com.google.gson.annotations.SerializedName;

public class User {
    private String firstname;
    private boolean active;
    @SerializedName("_id")
    private String id;
    private String email;
    private String lastname;

    @SerializedName("firstname")
    public String getFirstname() {
        return firstname;
    }

    public boolean isActive() {
        return active;
    }

    public String getId() {
        return id;
    }

    @SerializedName("email")
    public String getEmail() {
        return email;
    }

    @SerializedName("lastname")
    public String getLastname() {
        return lastname;
    }
}

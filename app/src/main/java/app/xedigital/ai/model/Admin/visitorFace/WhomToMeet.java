package app.xedigital.ai.model.Admin.visitorFace;

import com.google.gson.annotations.SerializedName;

public class WhomToMeet {

    @SerializedName("firstname")
    private String firstname;

    @SerializedName("contact")
    private String contact;

    @SerializedName("_id")
    private String id;

    @SerializedName("email")
    private String email;

    @SerializedName("lastname")
    private String lastname;

    public String getFirstname() {
        return firstname;
    }

    public String getContact() {
        return contact;
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
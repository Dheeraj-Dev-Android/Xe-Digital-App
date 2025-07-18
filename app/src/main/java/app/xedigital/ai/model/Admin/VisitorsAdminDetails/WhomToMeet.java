package app.xedigital.ai.model.Admin.VisitorsAdminDetails;

import com.google.gson.annotations.SerializedName;

public class WhomToMeet {

    @SerializedName("firstname")
    private String firstname;

    @SerializedName("contact")
    private String contact;

    @SerializedName("_id")
    private String id;

    @SerializedName("department")
    private String department;

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

    public String getDepartment() {
        return department;
    }

    public String getEmail() {
        return email;
    }

    public String getLastname() {
        return lastname;
    }
}
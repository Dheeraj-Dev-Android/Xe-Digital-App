package app.xedigital.ai.model.Admin.EmployeeDetails;

import com.google.gson.annotations.SerializedName;

public class Crossmanager {

    @SerializedName("firstname")
    private String firstname;

    @SerializedName("_id")
    private String id;

    @SerializedName("email")
    private String email;

    @SerializedName("lastname")
    private String lastname;

    public String getFirstname() {
        return firstname;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public String getLastname() {
        return lastname;
    }
}
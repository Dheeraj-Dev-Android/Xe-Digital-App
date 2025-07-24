package app.xedigital.ai.model.Admin.AdminUsers;

import com.google.gson.annotations.SerializedName;

public class Company {

    @SerializedName("contact")
    private String contact;

    @SerializedName("name")
    private String name;

    @SerializedName("_id")
    private String id;

    @SerializedName("email")
    private String email;

    public String getContact() {
        return contact;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }
}
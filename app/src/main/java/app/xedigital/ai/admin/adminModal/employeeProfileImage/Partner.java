package app.xedigital.ai.admin.adminModal.employeeProfileImage;

import com.google.gson.annotations.SerializedName;

public class Partner {

    @SerializedName("contact")
    private String contact;

    @SerializedName("name")
    private String name;

    @SerializedName("active")
    private boolean active;

    @SerializedName("_id")
    private String id;

    @SerializedName("email")
    private String email;

    public void setContact(String contact) {
        this.contact = contact;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
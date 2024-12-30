package app.xedigital.ai.model.profile;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;

import app.xedigital.ai.utills.CrossmanagerTypeAdapter;

@JsonAdapter(CrossmanagerTypeAdapter.class)

public class Crossmanager {

    @SerializedName("firstname")
    private String firstname;

    @SerializedName("_id")
    private String id;

    @SerializedName("email")
    private String email;

    @SerializedName("lastname")
    private String lastname;


    // New constructor to handle default cross-manager
    public Crossmanager(String id, String firstname, String lastname, String email) {
        this.id = id;
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
    }

    public Crossmanager() {
        // You can initialize fields to default values here if needed
        this.id = null;
        this.firstname = null;
        this.lastname = null;
        this.email = null;
    }


    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
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

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }
}
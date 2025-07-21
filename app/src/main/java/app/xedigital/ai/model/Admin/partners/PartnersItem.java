package app.xedigital.ai.model.Admin.partners;

import com.google.gson.annotations.SerializedName;

public class PartnersItem {

    @SerializedName("zip")
    private String zip;

    @SerializedName("website")
    private String website;

    @SerializedName("address")
    private String address;

    @SerializedName("city")
    private String city;

    @SerializedName("active")
    private boolean active;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("createdBy")
    private String createdBy;

    @SerializedName("contact")
    private String contact;

    @SerializedName("__v")
    private int v;

    @SerializedName("name")
    private String name;

    @SerializedName("company")
    private String company;

    @SerializedName("state")
    private String state;

    @SerializedName("_id")
    private String id;

    @SerializedName("email")
    private String email;

    @SerializedName("updatedAt")
    private String updatedAt;

    public String getZip() {
        return zip;
    }

    public String getWebsite() {
        return website;
    }

    public String getAddress() {
        return address;
    }

    public String getCity() {
        return city;
    }

    public boolean isActive() {
        return active;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getContact() {
        return contact;
    }

    public int getV() {
        return v;
    }

    public String getName() {
        return name;
    }

    public String getCompany() {
        return company;
    }

    public String getState() {
        return state;
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }
}
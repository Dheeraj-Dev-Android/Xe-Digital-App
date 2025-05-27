package app.xedigital.ai.model.Admin.UserDetails;

import com.google.gson.annotations.SerializedName;

public class Company {

    @SerializedName("zip")
    private String zip;

    @SerializedName("emailDomain")
    private String emailDomain;

    @SerializedName("website")
    private String website;

    @SerializedName("address")
    private String address;

    @SerializedName("city")
    private String city;

    @SerializedName("active")
    private boolean active;

    @SerializedName("collectionName")
    private String collectionName;

    @SerializedName("license")
    private String license;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("contact")
    private String contact;

    @SerializedName("name")
    private String name;

    @SerializedName("logo")
    private String logo;

    @SerializedName("state")
    private String state;

    @SerializedName("_id")
    private String id;

    @SerializedName("logoKey")
    private String logoKey;

    @SerializedName("email")
    private String email;

    public String getZip() {
        return zip;
    }

    public String getEmailDomain() {
        return emailDomain;
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

    public String getCollectionName() {
        return collectionName;
    }

    public String getLicense() {
        return license;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getContact() {
        return contact;
    }

    public String getName() {
        return name;
    }

    public String getLogo() {
        return logo;
    }

    public String getState() {
        return state;
    }

    public String getId() {
        return id;
    }

    public String getLogoKey() {
        return logoKey;
    }

    public String getEmail() {
        return email;
    }
}
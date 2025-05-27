package app.xedigital.ai.model.Admin.UserDetails;

import com.google.gson.annotations.SerializedName;

public class Branch {

    @SerializedName("zip")
    private String zip;

    @SerializedName("website")
    private String website;

    @SerializedName("address")
    private String address;

    @SerializedName("city")
    private String city;

    @SerializedName("prefix")
    private String prefix;

    @SerializedName("isItemImageUpload")
    private boolean isItemImageUpload;

    @SerializedName("accountPlan")
    private String accountPlan;

    @SerializedName("active")
    private boolean active;

    @SerializedName("customPlanValue")
    private String customPlanValue;

    @SerializedName("accountExpiryDate")
    private String accountExpiryDate;

    @SerializedName("isVisitorApproval")
    private boolean isVisitorApproval;

    @SerializedName("notificationEmail")
    private String notificationEmail;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("isGovernmentIdUpload")
    private boolean isGovernmentIdUpload;

    @SerializedName("contact")
    private String contact;

    @SerializedName("name")
    private String name;

    @SerializedName("logo")
    private String logo;

    @SerializedName("isTouchless")
    private boolean isTouchless;

    @SerializedName("company")
    private String company;

    @SerializedName("state")
    private String state;

    @SerializedName("_id")
    private String id;

    @SerializedName("email")
    private String email;

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

    public String getPrefix() {
        return prefix;
    }

    public boolean isIsItemImageUpload() {
        return isItemImageUpload;
    }

    public String getAccountPlan() {
        return accountPlan;
    }

    public boolean isActive() {
        return active;
    }

    public String getCustomPlanValue() {
        return customPlanValue;
    }

    public String getAccountExpiryDate() {
        return accountExpiryDate;
    }

    public boolean isIsVisitorApproval() {
        return isVisitorApproval;
    }

    public String getNotificationEmail() {
        return notificationEmail;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public boolean isIsGovernmentIdUpload() {
        return isGovernmentIdUpload;
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

    public boolean isIsTouchless() {
        return isTouchless;
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
}
package app.xedigital.ai.model.branch;

import com.google.gson.annotations.SerializedName;

public class Branch {

    @SerializedName("city")
    private String city;

    @SerializedName("prefix")
    private String prefix;

    @SerializedName("isItemImageUpload")
    private boolean isItemImageUpload;

    @SerializedName("accountExpiryDate")
    private String accountExpiryDate;

    @SerializedName("isVisitorApproval")
    private boolean isVisitorApproval;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("contact")
    private String contact;

    @SerializedName("__v")
    private int v;

    @SerializedName("logo")
    private String logo;

    @SerializedName("company")
    private String company;

    @SerializedName("state")
    private String state;

    @SerializedName("email")
    private String email;

    @SerializedName("updatedAt")
    private String updatedAt;

    @SerializedName("zip")
    private String zip;

    @SerializedName("website")
    private String website;

    @SerializedName("address")
    private String address;

    @SerializedName("accountPlan")
    private String accountPlan;

    @SerializedName("active")
    private boolean active;

    @SerializedName("customPlanValue")
    private String customPlanValue;

    @SerializedName("notificationEmail")
    private String notificationEmail;

    @SerializedName("isGovernmentIdUpload")
    private boolean isGovernmentIdUpload;

    @SerializedName("createdBy")
    private String createdBy;

    @SerializedName("name")
    private String name;

    @SerializedName("isTouchless")
    private boolean isTouchless;

    @SerializedName("_id")
    private String id;

    public String getCity() {
        return city;
    }

    public String getPrefix() {
        return prefix;
    }

    public boolean isIsItemImageUpload() {
        return isItemImageUpload;
    }

    public String getAccountExpiryDate() {
        return accountExpiryDate;
    }

    public boolean isIsVisitorApproval() {
        return isVisitorApproval;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getContact() {
        return contact;
    }

    public int getV() {
        return v;
    }

    public String getLogo() {
        return logo;
    }

    public String getCompany() {
        return company;
    }

    public String getState() {
        return state;
    }

    public String getEmail() {
        return email;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public String getZip() {
        return zip;
    }

    public String getWebsite() {
        return website;
    }

    public String getAddress() {
        return address;
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

    public String getNotificationEmail() {
        return notificationEmail;
    }

    public boolean isIsGovernmentIdUpload() {
        return isGovernmentIdUpload;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getName() {
        return name;
    }

    public boolean isIsTouchless() {
        return isTouchless;
    }

    public String getId() {
        return id;
    }
}
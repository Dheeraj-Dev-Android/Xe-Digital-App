package app.xedigital.ai.model.visitorsDetails;

import com.google.gson.annotations.SerializedName;

public class Visitor {

    @SerializedName("whomToMeet")
    private WhomToMeet whomToMeet;

    @SerializedName("approvalDate")
    private String approvalDate;

    @SerializedName("governmentIdUploadedImagePath")
    private String governmentIdUploadedImagePath;

    @SerializedName("isPreApproved")
    private boolean isPreApproved;

    @SerializedName("meetingOverStatus")
    private String meetingOverStatus;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("contact")
    private String contact;

    @SerializedName("company")
    private String company;

    @SerializedName("department")
    private String department;

    @SerializedName("email")
    private String email;

    @SerializedName("approvalStatus")
    private String approvalStatus;

    @SerializedName("serialNumber")
    private String serialNumber;

    @SerializedName("meetingOverDate")
    private String meetingOverDate;

    @SerializedName("isVisitorVisited")
    private boolean isVisitorVisited;

    @SerializedName("signatureImagePath")
    private String signatureImagePath;

    @SerializedName("isLaptop")
    private boolean isLaptop;

    @SerializedName("signOut")
    private String signOut;

    @SerializedName("companyFrom")
    private String companyFrom;

    @SerializedName("purposeOfmeeting")
    private String purposeOfmeeting;

    @SerializedName("visitorCategory")
    private String visitorCategory;

    @SerializedName("itemImageUploadedPath")
    private String itemImageUploadedPath;

    @SerializedName("signIn")
    private String signIn;

    @SerializedName("name")
    private String name;

    @SerializedName("profileImagePath")
    private String profileImagePath;

    @SerializedName("_id")
    private String id;

    @SerializedName("preApprovedDate")
    private String preApprovedDate;

    public WhomToMeet getWhomToMeet() {
        return whomToMeet;
    }

    public String getApprovalDate() {
        return approvalDate;
    }

    public String getGovernmentIdUploadedImagePath() {
        return governmentIdUploadedImagePath;
    }

    public boolean isIsPreApproved() {
        return isPreApproved;
    }

    public String getMeetingOverStatus() {
        return meetingOverStatus;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getContact() {
        return contact;
    }

    public String getCompany() {
        return company;
    }

    public String getDepartment() {
        return department;
    }

    public String getEmail() {
        return email;
    }

    public String getApprovalStatus() {
        return approvalStatus;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public String getMeetingOverDate() {
        return meetingOverDate;
    }

    public boolean isIsVisitorVisited() {
        return isVisitorVisited;
    }

    public String getSignatureImagePath() {
        return signatureImagePath;
    }

    public boolean isIsLaptop() {
        return isLaptop;
    }

    public String getSignOut() {
        return signOut;
    }

    public String getCompanyFrom() {
        return companyFrom;
    }

    public String getPurposeOfmeeting() {
        return purposeOfmeeting;
    }

    public String getVisitorCategory() {
        return visitorCategory;
    }

    public String getItemImageUploadedPath() {
        return itemImageUploadedPath;
    }

    public String getSignIn() {
        return signIn;
    }

    public String getName() {
        return name;
    }

    public String getProfileImagePath() {
        return profileImagePath;
    }

    public String getId() {
        return id;
    }

    public String getPreApprovedDate() {
        return preApprovedDate;
    }
}
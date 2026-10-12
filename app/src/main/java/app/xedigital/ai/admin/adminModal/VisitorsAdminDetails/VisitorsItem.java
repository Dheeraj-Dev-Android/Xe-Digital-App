package app.xedigital.ai.admin.adminModal.VisitorsAdminDetails;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class VisitorsItem implements Serializable {

    @SerializedName("whomToMeet")
    private WhomToMeet whomToMeet;

    @SerializedName("approvalDate")
    private String approvalDate;

    @SerializedName("governmentIdUploadedImagePath")
    private String governmentIdUploadedImagePath;

    @SerializedName("isPreApproved")
    private boolean isPreApproved;

    @SerializedName("nameWhomToMeet")
    private String nameWhomToMeet;

    @SerializedName("meetingOverStatus")
    private String meetingOverStatus;

    @SerializedName("roomId")
    private String roomId;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("contact")
    private String contact;

    @SerializedName("__v")
    private int v;

    @SerializedName("startTime")
    private String startTime;

    @SerializedName("company")
    private String company;

    @SerializedName("department")
    private String department;

    @SerializedName("email")
    private String email;

    @SerializedName("updatedAt")
    private String updatedAt;

    @SerializedName("approvalStatus")
    private String approvalStatus;

    @SerializedName("isRequired")
    private boolean isRequired;

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

    @SerializedName("emailWhomToMeet")
    private String emailWhomToMeet;

    @SerializedName("roomName")
    private String roomName;

    @SerializedName("purposeOfmeeting")
    private String purposeOfmeeting;

    @SerializedName("visitorCategory")
    private Object visitorCategory;

    @SerializedName("itemImageUploadedPath")
    private String itemImageUploadedPath;

    @SerializedName("signIn")
    private String signIn;

    @SerializedName("name")
    private String name;

    @SerializedName("profileImagePath")
    private String profileImagePath;

    @SerializedName("endTime")
    private String endTime;

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

    public String getNameWhomToMeet() {
        return nameWhomToMeet;
    }

    public String getMeetingOverStatus() {
        return meetingOverStatus;
    }

    public String getRoomId() {
        return roomId;
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

    public String getStartTime() {
        return startTime;
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

    public String getUpdatedAt() {
        return updatedAt;
    }

    public String getApprovalStatus() {
        return approvalStatus;
    }

    public boolean isIsRequired() {
        return isRequired;
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

    public String getEmailWhomToMeet() {
        return emailWhomToMeet;
    }

    public String getRoomName() {
        return roomName;
    }

    public String getPurposeOfmeeting() {
        return purposeOfmeeting;
    }

    public Object getVisitorCategory() {
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

    public String getEndTime() {
        return endTime;
    }

    public String getId() {
        return id;
    }

    public String getPreApprovedDate() {
        return preApprovedDate;
    }

    // Setters required by VisitorsDetailsFragment
    public void setApprovalStatus(String approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    public void setApprovalDate(String approvalDate) {
        this.approvalDate = approvalDate;
    }

    public void setMeetingOverStatus(String meetingOverStatus) {
        this.meetingOverStatus = meetingOverStatus;
    }

    public void setMeetingOverDate(String meetingOverDate) {
        this.meetingOverDate = meetingOverDate;
    }

    public void setSignIn(String signIn) {
        this.signIn = signIn;
    }

    public void setSignOut(String signOut) {
        this.signOut = signOut;
    }
}
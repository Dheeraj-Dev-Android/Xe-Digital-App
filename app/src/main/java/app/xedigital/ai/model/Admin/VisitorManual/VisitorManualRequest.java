package app.xedigital.ai.model.Admin.VisitorManual;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class VisitorManualRequest {

    @SerializedName("whomToMeet")
    private List<WhomToMeetItem> whomToMeet;

    @SerializedName("governmentIdUploadedImagePath")
    private String governmentIdUploadedImagePath;

    @SerializedName("companyName")
    private String companyName;

    @SerializedName("profileImage")
    private String profileImage;

    @SerializedName("profileImageKey")
    private String profileImageKey;

    @SerializedName("empcontact")
    private Object empcontact;

    @SerializedName("emplastname")
    private Object emplastname;

    @SerializedName("contact")
    private long contact;

    @SerializedName("company")
    private String company;

    @SerializedName("email")
    private String email;

    @SerializedName("approvalStatus")
    private String approvalStatus;

    @SerializedName("empfirstname")
    private Object empfirstname;

    @SerializedName("serialNumber")
    private String serialNumber;

    @SerializedName("isLaptop")
    private String isLaptop;

    @SerializedName("governmentIdUploadedImage")
    private String governmentIdUploadedImage;

    @SerializedName("signatureImagePath")
    private String signatureImagePath;

    @SerializedName("companyFrom")
    private String companyFrom;

    @SerializedName("visitorVisit")
    private String visitorVisit;

    @SerializedName("itemUploadedImage")
    private String itemUploadedImage;

    @SerializedName("purposeOfmeeting")
    private String purposeOfmeeting;

    @SerializedName("visitorCategory")
    private String visitorCategory;

    @SerializedName("faceData")
    private FaceData faceData;

    @SerializedName("itemImageUploadedPath")
    private String itemImageUploadedPath;

    @SerializedName("signIn")
    private String signIn;

    @SerializedName("name")
    private String name;

    @SerializedName("profileImagePath")
    private String profileImagePath;

    public void setWhomToMeet(List<WhomToMeetItem> whomToMeet) {
        this.whomToMeet = whomToMeet;
    }

    public void setGovernmentIdUploadedImagePath(String governmentIdUploadedImagePath) {
        this.governmentIdUploadedImagePath = governmentIdUploadedImagePath;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public void setProfileImageKey(String profileImageKey) {
        this.profileImageKey = profileImageKey;
    }

    public void setEmpcontact(Object empcontact) {
        this.empcontact = empcontact;
    }

    public void setEmplastname(Object emplastname) {
        this.emplastname = emplastname;
    }

    public void setContact(long contact) {
        this.contact = contact;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setApprovalStatus(String approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    public void setEmpfirstname(Object empfirstname) {
        this.empfirstname = empfirstname;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public void setIsLaptop(String isLaptop) {
        this.isLaptop = isLaptop;
    }

    public void setGovernmentIdUploadedImage(String governmentIdUploadedImage) {
        this.governmentIdUploadedImage = governmentIdUploadedImage;
    }

    public void setSignatureImagePath(String signatureImagePath) {
        this.signatureImagePath = signatureImagePath;
    }

    public void setCompanyFrom(String companyFrom) {
        this.companyFrom = companyFrom;
    }

    public void setVisitorVisit(String visitorVisit) {
        this.visitorVisit = visitorVisit;
    }

    public void setItemUploadedImage(String itemUploadedImage) {
        this.itemUploadedImage = itemUploadedImage;
    }

    public void setPurposeOfmeeting(String purposeOfmeeting) {
        this.purposeOfmeeting = purposeOfmeeting;
    }

    public void setVisitorCategory(String visitorCategory) {
        this.visitorCategory = visitorCategory;
    }

    public void setFaceData(FaceData faceData) {
        this.faceData = faceData;
    }

    public void setItemImageUploadedPath(String itemImageUploadedPath) {
        this.itemImageUploadedPath = itemImageUploadedPath;
    }

    public void setSignIn(String signIn) {
        this.signIn = signIn;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setProfileImagePath(String profileImagePath) {
        this.profileImagePath = profileImagePath;
    }
}
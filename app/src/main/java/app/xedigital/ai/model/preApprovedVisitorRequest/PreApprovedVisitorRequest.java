package app.xedigital.ai.model.preApprovedVisitorRequest;

import com.google.gson.annotations.SerializedName;

public class PreApprovedVisitorRequest {

	@SerializedName("approvalStatus")
	private String approvalStatus;

	@SerializedName("whomToMeet")
	private String whomToMeet;

	@SerializedName("approvalDate")
	private String approvalDate;

	@SerializedName("governmentIdUploadedImagePath")
	private String governmentIdUploadedImagePath;

	@SerializedName("isPreApproved")
	private boolean isPreApproved;

	@SerializedName("governmentIdUploadedImage")
	private String governmentIdUploadedImage;

	@SerializedName("active")
	private boolean active;

	@SerializedName("companyFrom")
	private String companyFrom;

	@SerializedName("profileImage")
	private String profileImage;

	@SerializedName("type")
	private String type;

	@SerializedName("faceData")
	private FaceData faceData;

	@SerializedName("isProfileImageDetailFound")
	private boolean isProfileImageDetailFound;

	@SerializedName("contact")
	private String contact;

	@SerializedName("name")
	private String name;

	@SerializedName("profileImagePath")
	private String profileImagePath;

	@SerializedName("company")
	private String company;

	@SerializedName("preApprovedDate")
	private String preApprovedDate;

	@SerializedName("department")
	private String department;

	@SerializedName("email")
	private String email;

	public void setApprovalStatus(String approvalStatus) {
		this.approvalStatus = approvalStatus;
	}

	public void setWhomToMeet(String whomToMeet) {
		this.whomToMeet = whomToMeet;
	}

	public void setApprovalDate(String approvalDate) {
		this.approvalDate = approvalDate;
	}

	public void setGovernmentIdUploadedImagePath(String governmentIdUploadedImagePath) {
		this.governmentIdUploadedImagePath = governmentIdUploadedImagePath;
	}

	public void setIsPreApproved(boolean isPreApproved) {
		this.isPreApproved = isPreApproved;
	}

	public void setGovernmentIdUploadedImage(String governmentIdUploadedImage) {
		this.governmentIdUploadedImage = governmentIdUploadedImage;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public void setCompanyFrom(String companyFrom) {
		this.companyFrom = companyFrom;
	}

	public void setProfileImage(String profileImage) {
		this.profileImage = profileImage;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setFaceData(FaceData faceData) {
		this.faceData = faceData;
	}

	public void setIsProfileImageDetailFound(boolean isProfileImageDetailFound) {
		this.isProfileImageDetailFound = isProfileImageDetailFound;
	}

	public void setContact(String contact) {
		this.contact = contact;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setProfileImagePath(String profileImagePath) {
		this.profileImagePath = profileImagePath;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public void setPreApprovedDate(String preApprovedDate) {
		this.preApprovedDate = preApprovedDate;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public void setEmail(String email) {
		this.email = email;
	}
}
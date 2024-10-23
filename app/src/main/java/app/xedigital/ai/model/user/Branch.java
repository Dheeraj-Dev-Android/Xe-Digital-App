package app.xedigital.ai.model.user;

import com.google.gson.annotations.SerializedName;

public class Branch{
	private String zip;
	private String website;
	private String address;
	private String city;
	private String prefix;
	private boolean isItemImageUpload;
	private String accountPlan;
	private boolean active;
	private String customPlanValue;
	private String accountExpiryDate;
	private boolean isVisitorApproval;
	private String notificationEmail;
	private String createdAt;
	private boolean isGovernmentIdUpload;
	private String contact;
	private String name;
	private String logo;
	private boolean isTouchless;
	private String company;
	private String state;
	@SerializedName("_id")
	private String id;
	private String email;

	public String getZip(){
		return zip;
	}

	public String getWebsite(){
		return website;
	}

	public String getAddress(){
		return address;
	}

	public String getCity(){
		return city;
	}

	public String getPrefix(){
		return prefix;
	}

	public boolean isIsItemImageUpload(){
		return isItemImageUpload;
	}

	public String getAccountPlan(){
		return accountPlan;
	}

	public boolean isActive(){
		return active;
	}

	public String getCustomPlanValue(){
		return customPlanValue;
	}

	public String getAccountExpiryDate(){
		return accountExpiryDate;
	}

	public boolean isIsVisitorApproval(){
		return isVisitorApproval;
	}

	public String getNotificationEmail(){
		return notificationEmail;
	}

	public String getCreatedAt(){
		return createdAt;
	}

	public boolean isIsGovernmentIdUpload(){
		return isGovernmentIdUpload;
	}

	public String getContact(){
		return contact;
	}

	public String getName(){
		return name;
	}

	public String getLogo(){
		return logo;
	}

	public boolean isIsTouchless(){
		return isTouchless;
	}

	public String getCompany(){
		return company;
	}

	public String getState(){
		return state;
	}

	public String getId(){
		return id;
	}

	public String getEmail(){
		return email;
	}
}

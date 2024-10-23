package app.xedigital.ai.model.user;

import com.google.gson.annotations.SerializedName;

public class Company{
	private String zip;
	private String emailDomain;
	private String website;
	private String address;
	private String city;
	private boolean active;
	private String collectionName;
	private String license;
	private String createdAt;
	private String contact;
	private String name;
	private String logo;
	private String state;
	@SerializedName("_id")
	private String id;
	private String logoKey;
	private String email;

	public String getZip(){
		return zip;
	}

	public String getEmailDomain(){
		return emailDomain;
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

	public boolean isActive(){
		return active;
	}

	public String getCollectionName(){
		return collectionName;
	}

	public String getLicense(){
		return license;
	}

	public String getCreatedAt(){
		return createdAt;
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

	public String getState(){
		return state;
	}

	public String getId(){
		return id;
	}

	public String getLogoKey(){
		return logoKey;
	}

	public String getEmail(){
		return email;
	}
}

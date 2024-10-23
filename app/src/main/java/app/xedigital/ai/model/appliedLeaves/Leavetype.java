package app.xedigital.ai.model.appliedLeaves;

import com.google.gson.annotations.SerializedName;

public class Leavetype{

	@SerializedName("createdAt")
	private String createdAt;

	@SerializedName("createdBy")
	private String createdBy;

	@SerializedName("leavetypeName")
	private String leavetypeName;

	@SerializedName("__v")
	private int v;

	@SerializedName("active")
	private boolean active;

	@SerializedName("company")
	private String company;

	@SerializedName("_id")
	private String id;

	@SerializedName("updatedAt")
	private String updatedAt;

	public String getCreatedAt(){
		return createdAt;
	}

	public String getCreatedBy(){
		return createdBy;
	}

	public String getLeavetypeName(){
		return leavetypeName;
	}

	public int getV(){
		return v;
	}

	public boolean isActive(){
		return active;
	}

	public String getCompany(){
		return company;
	}

	public String getId(){
		return id;
	}

	public String getUpdatedAt(){
		return updatedAt;
	}
}
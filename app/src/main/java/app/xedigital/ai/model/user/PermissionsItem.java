package app.xedigital.ai.model.user;

import com.google.gson.annotations.SerializedName;

public class PermissionsItem{
	private String createdAt;
	private boolean jsonMemberDefault;
	private String displayName;
	private String name;
	private String description;
	private boolean active;
	@SerializedName("_id")
	private String id;

	public String getCreatedAt(){
		return createdAt;
	}

	public boolean isJsonMemberDefault(){
		return jsonMemberDefault;
	}

	public String getDisplayName(){
		return displayName;
	}

	public String getName(){
		return name;
	}

	public String getDescription(){
		return description;
	}

	public boolean isActive(){
		return active;
	}

	public String getId(){
		return id;
	}
}

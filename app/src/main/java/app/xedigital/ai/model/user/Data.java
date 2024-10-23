package app.xedigital.ai.model.user;

import java.util.List;

public class Data{
	private Role role;
	private List<ResourcePermissionsItem> resourcePermissions;
	private List<ResourcesItem> resources;
	private Company company;
	private User user;
	private Branch branch;

	public Role getRole(){
		return role;
	}

	public List<ResourcePermissionsItem> getResourcePermissions(){
		return resourcePermissions;
	}

	public List<ResourcesItem> getResources(){
		return resources;
	}

	public Company getCompany(){
		return company;
	}

	public User getUser(){
		return user;
	}

	public Branch getBranch(){
		return branch;
	}
}
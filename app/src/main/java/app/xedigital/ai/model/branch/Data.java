package app.xedigital.ai.model.branch;

import com.google.gson.annotations.SerializedName;

public class Data{

	@SerializedName("branch")
	private Branch branch;

	public Branch getBranch(){
		return branch;
	}
}
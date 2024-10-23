package app.xedigital.ai.model.appliedLeaves;

import java.util.List;
import com.google.gson.annotations.SerializedName;

public class Data{

	@SerializedName("appliedLeaves")
	private List<AppliedLeavesItem> appliedLeaves;

	public List<AppliedLeavesItem> getAppliedLeaves(){
		return appliedLeaves;
	}
}
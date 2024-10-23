package app.xedigital.ai.model.leaves;

import java.util.List;
import com.google.gson.annotations.SerializedName;

public class Data{

	@SerializedName("leaves")
	private List<LeavesItem> leaves;

	public List<LeavesItem> getLeaves(){
		return leaves;
	}
}
package app.xedigital.ai.model.leaves;

import com.google.gson.annotations.SerializedName;

public class LeavesItem{

	@SerializedName("leavetype")
	private String leavetype;

	@SerializedName("assignDate")
	private String assignDate;

	@SerializedName("creditLeave")
	private int creditLeave;

	@SerializedName("debitLeave")
	private float debitLeave;

	@SerializedName("_id")
	private String id;

	@SerializedName("usedLeave")
	private float usedLeave;

	@SerializedName("openingLeave")
	private int openingLeave;

	public String getLeavetype(){
		return leavetype;
	}

	public String getAssignDate(){
		return assignDate;
	}

	public int getCreditLeave(){
		return creditLeave;
	}

	public float getDebitLeave(){
		return debitLeave;
	}

	public String getId(){
		return id;
	}

	public float getUsedLeave(){
		return usedLeave;
	}

	public int getOpeningLeave(){
		return openingLeave;
	}
}
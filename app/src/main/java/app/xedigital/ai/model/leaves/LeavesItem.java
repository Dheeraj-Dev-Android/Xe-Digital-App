package app.xedigital.ai.model.leaves;

import com.google.gson.annotations.SerializedName;

public class LeavesItem{

	@SerializedName("leavetype")
	private String leavetype;

	@SerializedName("assignDate")
	private String assignDate;

	@SerializedName("creditLeave")
    private float creditLeave;

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

    public float getCreditLeave() {
		return creditLeave;
	}

	public float getDebitLeave(){
		return debitLeave;
	}
//	public Object getDebitLeaveAsObject() {
//		if (debitLeave % 1 == 0) {
//			return (int) debitLeave; // If it's a whole number, return as int
//		} else {
//			return debitLeave; // Otherwise, return as float
//		}
//	}

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
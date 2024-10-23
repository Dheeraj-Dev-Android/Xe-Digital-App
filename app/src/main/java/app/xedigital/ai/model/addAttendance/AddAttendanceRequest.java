package app.xedigital.ai.model.addAttendance;

import com.google.gson.annotations.SerializedName;

public class AddAttendanceRequest{

	@SerializedName("punchOut")
	private String punchOut;

	@SerializedName("punchOutAddress")
	private String punchOutAddress;

	@SerializedName("punchInAddress")
	private String punchInAddress;

	@SerializedName("remark")
	private String remark;

	@SerializedName("employee")
	private String employee;

	@SerializedName("punchDate")
	private String punchDate;

	@SerializedName("punchIn")
	private String punchIn;

	public void setPunchOut(String punchOut){
		this.punchOut = punchOut;
	}

	public void setPunchOutAddress(String punchOutAddress){
		this.punchOutAddress = punchOutAddress;
	}

	public void setPunchInAddress(String punchInAddress){
		this.punchInAddress = punchInAddress;
	}

	public void setRemark(String remark){
		this.remark = remark;
	}

	public void setEmployee(String employee){
		this.employee = employee;
	}

	public void setPunchDate(String punchDate){
		this.punchDate = punchDate;
	}

	public void setPunchIn(String punchIn){
		this.punchIn = punchIn;
	}
}
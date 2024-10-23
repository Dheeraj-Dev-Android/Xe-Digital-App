package app.xedigital.ai.model.regularizeUpdateStatus;

import com.google.gson.annotations.SerializedName;

public class Attendance{

	@SerializedName("createdAt")
	private String createdAt;

	@SerializedName("punchOut")
	private String punchOut;

	@SerializedName("__v")
	private int v;

	@SerializedName("punchOutAddress")
	private String punchOutAddress;

	@SerializedName("punchInAddress")
	private String punchInAddress;

	@SerializedName("_id")
	private String id;

	@SerializedName("employee")
	private String employee;

	@SerializedName("punchDate")
	private String punchDate;

	@SerializedName("punchIn")
	private String punchIn;

	@SerializedName("updatedAt")
	private String updatedAt;

	public void setCreatedAt(String createdAt){
		this.createdAt = createdAt;
	}

	public String getCreatedAt(){
		return createdAt;
	}

	public void setPunchOut(String punchOut){
		this.punchOut = punchOut;
	}

	public String getPunchOut(){
		return punchOut;
	}

	public void setV(int v){
		this.v = v;
	}

	public int getV(){
		return v;
	}

	public void setPunchOutAddress(String punchOutAddress){
		this.punchOutAddress = punchOutAddress;
	}

	public String getPunchOutAddress(){
		return punchOutAddress;
	}

	public void setPunchInAddress(String punchInAddress){
		this.punchInAddress = punchInAddress;
	}

	public String getPunchInAddress(){
		return punchInAddress;
	}

	public void setId(String id){
		this.id = id;
	}

	public String getId(){
		return id;
	}

	public void setEmployee(String employee){
		this.employee = employee;
	}

	public String getEmployee(){
		return employee;
	}

	public void setPunchDate(String punchDate){
		this.punchDate = punchDate;
	}

	public String getPunchDate(){
		return punchDate;
	}

	public void setPunchIn(String punchIn){
		this.punchIn = punchIn;
	}

	public String getPunchIn(){
		return punchIn;
	}

	public void setUpdatedAt(String updatedAt){
		this.updatedAt = updatedAt;
	}

	public String getUpdatedAt(){
		return updatedAt;
	}
}
package app.xedigital.ai.model.regularizeUpdateStatus;

import com.google.gson.annotations.SerializedName;

public class RegularizeUpdateRequest{

	@SerializedName("punchInUpdated")
	private String punchInUpdated;

	@SerializedName("attendenceRegularizationRemark")
	private String attendenceRegularizationRemark;

	@SerializedName("punchOutUpdated")
	private String punchOutUpdated;

	@SerializedName("punchInAddressUpdated")
	private String punchInAddressUpdated;

	@SerializedName("shift")
	private Shift shift;

	@SerializedName("appliedDate")
	private String appliedDate;

	@SerializedName("employee")
	private Employee employee;

	@SerializedName("punchDate")
	private String punchDate;

	@SerializedName("punchIn")
	private String punchIn;

	@SerializedName("approvedDate")
	private String approvedDate;

	@SerializedName("punchOutAddressUpdated")
	private String punchOutAddressUpdated;

	@SerializedName("createdAt")
	private String createdAt;

	@SerializedName("punchOut")
	private String punchOut;

	@SerializedName("punchOutUpdatedDate")
	private String punchOutUpdatedDate;

	@SerializedName("punchOutAddress")
	private String punchOutAddress;

	@SerializedName("approvedByName")
	private String approvedByName;

	@SerializedName("punchOutDate")
	private String punchOutDate;

	@SerializedName("punchInAddress")
	private String punchInAddress;

	@SerializedName("_id")
	private String id;

	@SerializedName("attendance")
	private Attendance attendance;

	@SerializedName("status")
	private String status;

	public void setPunchInUpdated(String punchInUpdated){
		this.punchInUpdated = punchInUpdated;
	}

	public String getPunchInUpdated(){
		return punchInUpdated;
	}

	public void setAttendenceRegularizationRemark(String attendenceRegularizationRemark){
		this.attendenceRegularizationRemark = attendenceRegularizationRemark;
	}

	public String getAttendenceRegularizationRemark(){
		return attendenceRegularizationRemark;
	}

	public void setPunchOutUpdated(String punchOutUpdated){
		this.punchOutUpdated = punchOutUpdated;
	}

	public String getPunchOutUpdated(){
		return punchOutUpdated;
	}

	public void setPunchInAddressUpdated(String punchInAddressUpdated){
		this.punchInAddressUpdated = punchInAddressUpdated;
	}

	public String getPunchInAddressUpdated(){
		return punchInAddressUpdated;
	}

	public void setShift(Shift shift){
		this.shift = shift;
	}

	public Shift getShift(){
		return shift;
	}

	public void setAppliedDate(String appliedDate){
		this.appliedDate = appliedDate;
	}

	public String getAppliedDate(){
		return appliedDate;
	}

	public void setEmployee(Employee employee){
		this.employee = employee;
	}

	public Employee getEmployee(){
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

	public void setApprovedDate(String approvedDate){
		this.approvedDate = approvedDate;
	}

	public String getApprovedDate(){
		return approvedDate;
	}

	public void setPunchOutAddressUpdated(String punchOutAddressUpdated){
		this.punchOutAddressUpdated = punchOutAddressUpdated;
	}

	public String getPunchOutAddressUpdated(){
		return punchOutAddressUpdated;
	}

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

	public void setPunchOutUpdatedDate(String punchOutUpdatedDate){
		this.punchOutUpdatedDate = punchOutUpdatedDate;
	}

	public String getPunchOutUpdatedDate(){
		return punchOutUpdatedDate;
	}

	public void setPunchOutAddress(String punchOutAddress){
		this.punchOutAddress = punchOutAddress;
	}

	public String getPunchOutAddress(){
		return punchOutAddress;
	}

	public void setApprovedByName(String approvedByName){
		this.approvedByName = approvedByName;
	}

	public String getApprovedByName(){
		return approvedByName;
	}

	public void setPunchOutDate(String punchOutDate){
		this.punchOutDate = punchOutDate;
	}

	public String getPunchOutDate(){
		return punchOutDate;
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

	public void setAttendance(Attendance attendance){
		this.attendance = attendance;
	}

	public Attendance getAttendance(){
		return attendance;
	}

	public void setStatus(String status){
		this.status = status;
	}

	public String getStatus(){
		return status;
	}
}
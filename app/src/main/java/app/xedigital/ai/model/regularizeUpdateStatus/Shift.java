package app.xedigital.ai.model.regularizeUpdateStatus;

import com.google.gson.annotations.SerializedName;

public class Shift{

	@SerializedName("shiftType")
	private String shiftType;

	@SerializedName("fromHour")
	private int fromHour;

	@SerializedName("fromMinutes")
	private int fromMinutes;

	@SerializedName("format")
	private int format;

	@SerializedName("active")
	private boolean active;

	@SerializedName("toHour")
	private int toHour;

	@SerializedName("createdAt")
	private String createdAt;

	@SerializedName("toMinutes")
	private int toMinutes;

	@SerializedName("createdBy")
	private String createdBy;

	@SerializedName("__v")
	private int v;

	@SerializedName("name")
	private String name;

	@SerializedName("startTime")
	private String startTime;

	@SerializedName("comment")
	private String comment;

	@SerializedName("company")
	private String company;

	@SerializedName("_id")
	private String id;

	@SerializedName("endTime")
	private String endTime;

	@SerializedName("timeWaiver")
	private int timeWaiver;

	@SerializedName("updatedAt")
	private String updatedAt;

	public void setShiftType(String shiftType){
		this.shiftType = shiftType;
	}

	public String getShiftType(){
		return shiftType;
	}

	public void setFromHour(int fromHour){
		this.fromHour = fromHour;
	}

	public int getFromHour(){
		return fromHour;
	}

	public void setFromMinutes(int fromMinutes){
		this.fromMinutes = fromMinutes;
	}

	public int getFromMinutes(){
		return fromMinutes;
	}

	public void setFormat(int format){
		this.format = format;
	}

	public int getFormat(){
		return format;
	}

	public void setActive(boolean active){
		this.active = active;
	}

	public boolean isActive(){
		return active;
	}

	public void setToHour(int toHour){
		this.toHour = toHour;
	}

	public int getToHour(){
		return toHour;
	}

	public void setCreatedAt(String createdAt){
		this.createdAt = createdAt;
	}

	public String getCreatedAt(){
		return createdAt;
	}

	public void setToMinutes(int toMinutes){
		this.toMinutes = toMinutes;
	}

	public int getToMinutes(){
		return toMinutes;
	}

	public void setCreatedBy(String createdBy){
		this.createdBy = createdBy;
	}

	public String getCreatedBy(){
		return createdBy;
	}

	public void setV(int v){
		this.v = v;
	}

	public int getV(){
		return v;
	}

	public void setName(String name){
		this.name = name;
	}

	public String getName(){
		return name;
	}

	public void setStartTime(String startTime){
		this.startTime = startTime;
	}

	public String getStartTime(){
		return startTime;
	}

	public void setComment(String comment){
		this.comment = comment;
	}

	public String getComment(){
		return comment;
	}

	public void setCompany(String company){
		this.company = company;
	}

	public String getCompany(){
		return company;
	}

	public void setId(String id){
		this.id = id;
	}

	public String getId(){
		return id;
	}

	public void setEndTime(String endTime){
		this.endTime = endTime;
	}

	public String getEndTime(){
		return endTime;
	}

	public void setTimeWaiver(int timeWaiver){
		this.timeWaiver = timeWaiver;
	}

	public int getTimeWaiver(){
		return timeWaiver;
	}

	public void setUpdatedAt(String updatedAt){
		this.updatedAt = updatedAt;
	}

	public String getUpdatedAt(){
		return updatedAt;
	}
}
package app.xedigital.ai.ui.userProfileEmail;

import com.google.gson.annotations.SerializedName;

public class Data{

	@SerializedName("employee")
	private Employee employee;

	public Employee getEmployee(){
		return employee;
	}
}
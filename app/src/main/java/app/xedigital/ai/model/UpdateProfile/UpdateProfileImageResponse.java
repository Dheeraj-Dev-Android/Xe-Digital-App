package app.xedigital.ai.model.UpdateProfile;

import com.google.gson.annotations.SerializedName;

public class UpdateProfileImageResponse {

    @SerializedName("image")
    private String image;

    @SerializedName("employeeId")
    private String employeeId;

    @SerializedName("employee")
    private Employee employee;

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }
}
package app.xedigital.ai.admin.adminModal.employeeProfileImage;

import com.google.gson.annotations.SerializedName;

public class EmployeeProfileImageRequest {

    @SerializedName("image")
    private String image;

    @SerializedName("employeeId")
    private String employeeId;

    @SerializedName("employee")
    private Employee employee;

    public void setImage(String image) {
        this.image = image;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }
}
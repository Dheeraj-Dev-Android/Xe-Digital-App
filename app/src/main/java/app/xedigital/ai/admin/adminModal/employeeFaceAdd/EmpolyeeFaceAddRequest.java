package app.xedigital.ai.admin.adminModal.employeeFaceAdd;

import com.google.gson.annotations.SerializedName;

public class EmpolyeeFaceAddRequest {

    @SerializedName("imageId")
    private String imageId;

    @SerializedName("employeeId")
    private String employeeId;

    @SerializedName("faceId")
    private String faceId;

    @SerializedName("employee")
    private Employee employee;

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public void setFaceId(String faceId) {
        this.faceId = faceId;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }
}
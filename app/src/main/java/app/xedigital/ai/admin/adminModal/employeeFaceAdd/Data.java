package app.xedigital.ai.admin.adminModal.employeeFaceAdd;

import com.google.gson.annotations.SerializedName;

public class Data {

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("imageId")
    private String imageId;

    @SerializedName("__v")
    private int v;

    @SerializedName("employeeId")
    private String employeeId;

    @SerializedName("_id")
    private String id;

    @SerializedName("faceId")
    private String faceId;

    @SerializedName("updatedAt")
    private String updatedAt;

    public String getCreatedAt() {
        return createdAt;
    }

    public String getImageId() {
        return imageId;
    }

    public int getV() {
        return v;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public String getId() {
        return id;
    }

    public String getFaceId() {
        return faceId;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }
}
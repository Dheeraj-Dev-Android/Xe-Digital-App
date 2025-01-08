package app.xedigital.ai.model.regularizeApplied;

import com.google.gson.annotations.SerializedName;

public class Attendance {

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

    public String getCreatedAt() {
        return createdAt;
    }

    public String getPunchOut() {
        return punchOut;
    }

    public int getV() {
        return v;
    }

    public String getPunchOutAddress() {
        return punchOutAddress;
    }

    public String getPunchInAddress() {
        return punchInAddress;
    }

    public String getId() {
        return id;
    }

    public String getEmployee() {
        return employee;
    }

    public String getPunchDate() {
        return punchDate;
    }

    public String getPunchIn() {
        return punchIn;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }
}
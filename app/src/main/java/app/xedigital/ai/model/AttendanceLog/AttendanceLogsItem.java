package app.xedigital.ai.model.AttendanceLog;

import com.google.gson.annotations.SerializedName;

public class AttendanceLogsItem {

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("punchTime")
    private String punchTime;

    @SerializedName("address")
    private String address;

    @SerializedName("createdBy")
    private String createdBy;

    @SerializedName("_id")
    private String id;

    public String getCreatedAt() {
        return createdAt;
    }

    public String getPunchTime() {
        return punchTime;
    }

    public String getAddress() {
        return address;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getId() {
        return id;
    }
}
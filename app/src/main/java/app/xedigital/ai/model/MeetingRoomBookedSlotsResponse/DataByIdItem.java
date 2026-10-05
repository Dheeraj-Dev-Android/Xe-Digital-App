package app.xedigital.ai.model.MeetingRoomBookedSlotsResponse;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class DataByIdItem implements Serializable {

    @SerializedName("hostName")
    private String hostName;

    @SerializedName("visitorCompany")
    private String visitorCompany;

    @SerializedName("bookingstatus")
    private String bookingstatus;

    @SerializedName("visitorCount")
    private String visitorCount;

    @SerializedName("selectDate")
    private String selectDate;

    @SerializedName("roomName")
    private RoomName roomName;

    @SerializedName("roomId")
    private String roomId;

    @SerializedName("hostContact")
    private String hostContact;

    @SerializedName("visitorContact")
    private String visitorContact;

    @SerializedName("meetingPurpose")
    private String meetingPurpose;

    @SerializedName("approvedDate")
    private String approvedDate;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("visitorName")
    private String visitorName;

    @SerializedName("__v")
    private int v;

    @SerializedName("host")
    private Host host;

    @SerializedName("hostEmail")
    private String hostEmail;

    @SerializedName("startTime")
    private String startTime;

    @SerializedName("approvedByName")
    private String approvedByName;

    @SerializedName("endTime")
    private String endTime;

    @SerializedName("_id")
    private String id;

    @SerializedName("updatedAt")
    private String updatedAt;

    public String getHostName() {
        return hostName;
    }

    public String getVisitorCompany() {
        return visitorCompany;
    }

    public String getBookingstatus() {
        return bookingstatus;
    }

    public String getVisitorCount() {
        return visitorCount;
    }

    public String getSelectDate() {
        return selectDate;
    }

    public RoomName getRoomName() {
        return roomName;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getHostContact() {
        return hostContact;
    }

    public String getVisitorContact() {
        return visitorContact;
    }

    public String getMeetingPurpose() {
        return meetingPurpose;
    }

    public String getApprovedDate() {
        return approvedDate;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getVisitorName() {
        return visitorName;
    }

    public int getV() {
        return v;
    }

    public Host getHost() {
        return host;
    }

    public String getHostEmail() {
        return hostEmail;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getApprovedByName() {
        return approvedByName;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getId() {
        return id;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }
}
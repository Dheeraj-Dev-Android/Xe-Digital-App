package app.xedigital.ai.model.meetingRoom;

import com.google.gson.annotations.SerializedName;

public class RoomDataItem {

    @SerializedName("docFileURLKey")
    private String docFileURLKey;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("__v")
    private int v;

    @SerializedName("docFileURL")
    private String docFileURL;

    @SerializedName("location")
    private String location;

    @SerializedName("_id")
    private String id;

    @SerializedName("roomCode")
    private String roomCode;

    @SerializedName("floor")
    private String floor;

    @SerializedName("seats")
    private String seats;

    @SerializedName("roomName")
    private String roomName;

    @SerializedName("updatedAt")
    private String updatedAt;

    public String getDocFileURLKey() {
        return docFileURLKey;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public int getV() {
        return v;
    }

    public String getDocFileURL() {
        return docFileURL;
    }

    public String getLocation() {
        return location;
    }

    public String getId() {
        return id;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public String getFloor() {
        return floor;
    }

    public String getSeats() {
        return seats;
    }

    public String getRoomName() {
        return roomName;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }
}
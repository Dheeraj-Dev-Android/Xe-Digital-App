package app.xedigital.ai.model.MeetingRoomBookedSlotsResponse;

import com.google.gson.annotations.SerializedName;

public class RoomName {

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
}
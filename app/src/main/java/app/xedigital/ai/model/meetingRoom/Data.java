package app.xedigital.ai.model.meetingRoom;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Data {

    @SerializedName("roomData")
    private List<RoomDataItem> roomData;

    public List<RoomDataItem> getRoomData() {
        return roomData;
    }
}
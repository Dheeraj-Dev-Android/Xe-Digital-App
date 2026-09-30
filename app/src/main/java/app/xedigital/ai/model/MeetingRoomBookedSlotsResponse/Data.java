package app.xedigital.ai.model.MeetingRoomBookedSlotsResponse;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Data {

    @SerializedName("dataById")
    private List<DataByIdItem> dataById;

    public List<DataByIdItem> getDataById() {
        return dataById;
    }
}
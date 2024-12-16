package app.xedigital.ai.model.shiftTime;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Data {

    @SerializedName("shiftTypes")
    private List<ShiftTypesItem> shiftTypes;

    public List<ShiftTypesItem> getShiftTypes() {
        return shiftTypes;
    }
}
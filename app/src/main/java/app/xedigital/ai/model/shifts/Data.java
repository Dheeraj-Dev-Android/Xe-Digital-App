package app.xedigital.ai.model.shifts;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Data {

    @SerializedName("shifts")
    private List<ShiftsItem> shifts;

    public List<ShiftsItem> getShifts() {
        return shifts;
    }
}
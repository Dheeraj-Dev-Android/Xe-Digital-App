package app.xedigital.ai.admin.adminModal.ActiveShift;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Data {

    @SerializedName("shifts")
    private List<ShiftsItem> shifts;

    public List<ShiftsItem> getShifts() {
        return shifts;
    }
}
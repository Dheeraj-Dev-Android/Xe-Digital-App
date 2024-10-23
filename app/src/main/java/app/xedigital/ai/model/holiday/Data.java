package app.xedigital.ai.model.holiday;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Data {

    @SerializedName("holidays")
    private List<HolidaysItem> holidays;

    public List<HolidaysItem> getHolidays() {
        return holidays;
    }
}
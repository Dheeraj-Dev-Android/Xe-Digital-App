package app.xedigital.ai.admin.adminModal.empAttendance;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Holiday implements Serializable {

    @SerializedName("_id")
    private String id;

    @SerializedName("isOptional")
    private boolean isOptional;

    @SerializedName("holidayName")
    private String holidayName;

    @SerializedName("holidayDateFormat")
    private String holidayDateFormat;

    @SerializedName("holidayDate")
    private String holidayDate;

    public String getId() {
        return id;
    }

    public boolean isIsOptional() {
        return isOptional;
    }

    public String getHolidayName() {
        return holidayName;
    }

    public String getHolidayDateFormat() {
        return holidayDateFormat;
    }

    public String getHolidayDate() {
        return holidayDate;
    }
}
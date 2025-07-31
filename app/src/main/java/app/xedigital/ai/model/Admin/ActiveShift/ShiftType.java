package app.xedigital.ai.model.Admin.ActiveShift;

import com.google.gson.annotations.SerializedName;

public class ShiftType {

    @SerializedName("shifttypeName")
    private String shifttypeName;

    @SerializedName("company")
    private String company;

    @SerializedName("_id")
    private String id;

    public String getShifttypeName() {
        return shifttypeName;
    }

    public String getCompany() {
        return company;
    }

    public String getId() {
        return id;
    }
}
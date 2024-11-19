package app.xedigital.ai.model.leaveType;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Data {

    @SerializedName("leavetypes")
    private List<LeavetypesItem> leavetypes;

    public List<LeavetypesItem> getLeavetypes() {
        return leavetypes;
    }
}
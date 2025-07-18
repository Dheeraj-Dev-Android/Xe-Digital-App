package app.xedigital.ai.model.Admin.LeaveGraph;

import com.google.gson.annotations.SerializedName;

public class DataItem {

    @SerializedName("total")
    private int total;

    @SerializedName("_id")
    private String id;

    public int getTotal() {
        return total;
    }

    public String getId() {
        return id;
    }
}
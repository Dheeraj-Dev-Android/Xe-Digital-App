package app.xedigital.ai.model.Admin.VisitorsAdminDetails;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Data {

    @SerializedName("visitors")
    private List<VisitorsItem> visitors;

    public List<VisitorsItem> getVisitors() {
        return visitors;
    }
}
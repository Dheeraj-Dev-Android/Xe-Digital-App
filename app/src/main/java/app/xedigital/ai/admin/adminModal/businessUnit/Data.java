package app.xedigital.ai.admin.adminModal.businessUnit;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Data {

    @SerializedName("bus")
    private List<BusItem> bus;

    public List<BusItem> getBus() {
        return bus;
    }
}
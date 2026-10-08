package app.xedigital.ai.admin.adminModal.Dashboard;

import com.google.gson.annotations.SerializedName;

public class Data {

    @SerializedName("counterData")
    private CounterData counterData;

    public CounterData getCounterData() {
        return counterData;
    }
}
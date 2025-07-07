package app.xedigital.ai.model.Admin.Dashboard;

import com.google.gson.annotations.SerializedName;

public class Data {

    @SerializedName("counterData")
    private CounterData counterData;

    public CounterData getCounterData() {
        return counterData;
    }
}
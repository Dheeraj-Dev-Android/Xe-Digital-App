package app.xedigital.ai.model.Admin.LeaveGraph;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Data {

    @SerializedName("graphBackground")
    private List<String> graphBackground;

    @SerializedName("data")
    private List<DataItem> data;

    @SerializedName("graphLabels")
    private List<String> graphLabels;

    @SerializedName("graphData")
    private List<Integer> graphData;

    public List<String> getGraphBackground() {
        return graphBackground;
    }

    public List<DataItem> getData() {
        return data;
    }

    public List<String> getGraphLabels() {
        return graphLabels;
    }

    public List<Integer> getGraphData() {
        return graphData;
    }
}
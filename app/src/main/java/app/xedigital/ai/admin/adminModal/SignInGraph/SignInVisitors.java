package app.xedigital.ai.admin.adminModal.SignInGraph;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SignInVisitors {

    @SerializedName("graphBackground")
    private List<String> graphBackground;

    @SerializedName("graphLabels")
    private List<String> graphLabels;

    @SerializedName("graphData")
    private List<Integer> graphData;

    public List<String> getGraphBackground() {
        return graphBackground;
    }

    public List<String> getGraphLabels() {
        return graphLabels;
    }

    public List<Integer> getGraphData() {
        return graphData;
    }
}
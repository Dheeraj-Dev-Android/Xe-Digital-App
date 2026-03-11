package app.xedigital.ai.model.appliedLeaves;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class Data implements Serializable {

    @SerializedName("appliedLeaves")
    private List<AppliedLeavesItem> appliedLeaves;

    public List<AppliedLeavesItem> getAppliedLeaves() {
        return appliedLeaves;
    }
}
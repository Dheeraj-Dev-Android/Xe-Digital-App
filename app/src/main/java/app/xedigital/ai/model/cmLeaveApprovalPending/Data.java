package app.xedigital.ai.model.cmLeaveApprovalPending;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Data {

    @SerializedName("appliedLeaves")
    private List<AppliedLeavesItem> appliedLeaves;

    public List<AppliedLeavesItem> getAppliedLeaves() {
        return appliedLeaves;
    }
}
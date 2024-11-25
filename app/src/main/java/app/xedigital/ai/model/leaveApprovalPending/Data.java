package app.xedigital.ai.model.leaveApprovalPending;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Data {

    @SerializedName("appliedLeaves")
    private List<AppliedLeavesApproveItem> appliedLeaves;

    public List<AppliedLeavesApproveItem> getAppliedLeaves() {
        return appliedLeaves;
    }
}
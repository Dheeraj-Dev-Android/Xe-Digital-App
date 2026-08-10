package app.xedigital.ai.model.leaveApprovalPending;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Data {

    @SerializedName("appliedLeavesApprove")
    private List<AppliedLeavesApproveItem> appliedLeavesApprove;

    public List<AppliedLeavesApproveItem> getAppliedLeaves() {
        return appliedLeavesApprove;
    }

}
package app.xedigital.ai.adapter;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import app.xedigital.ai.model.leaveApprovalPending.AppliedLeavesItem;
import app.xedigital.ai.ui.leaves.ApproveLeaveFragment;

public class LeaveApprovalAdapter extends RecyclerView.Adapter {
    public LeaveApprovalAdapter(List<AppliedLeavesItem> items, String authToken, String userId, ApproveLeaveFragment approveLeaveFragment, Context context) {
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }
}

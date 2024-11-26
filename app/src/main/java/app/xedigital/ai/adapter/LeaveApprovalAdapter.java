package app.xedigital.ai.adapter;

import static app.xedigital.ai.ui.leaves.PendingLeaveApproveFragment.ARG_LEAVE_ID;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

import app.xedigital.ai.R;
import app.xedigital.ai.model.leaveApprovalPending.AppliedLeavesApproveItem;
import app.xedigital.ai.ui.leaves.ApproveLeaveFragment;
import app.xedigital.ai.utills.DateTimeUtils;

public class LeaveApprovalAdapter extends RecyclerView.Adapter<LeaveApprovalAdapter.ViewHolder> {
    private List<AppliedLeavesApproveItem> items;
    private final String authToken;
    private final String userId;
    private final Context context;

    public LeaveApprovalAdapter(List<AppliedLeavesApproveItem> items, String authToken, String userId, ApproveLeaveFragment approveLeaveFragment, Context context) {
        this.items = items;
        this.authToken = authToken;
        this.userId = userId;
        this.context = context;
    }

    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.leave_approval_item, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppliedLeavesApproveItem item = items.get(position);

        holder.empName.setText(item.getFirstname() + " " + item.getLastname());
        String formattedFromDate = DateTimeUtils.getDayOfWeekAndDate(item.getFromDate());
        holder.fromDate.setText("From Date : " + formattedFromDate);
        String formattedToDate = DateTimeUtils.getDayOfWeekAndDate(item.getToDate());
        holder.toDate.setText("To Date : " + formattedToDate);
        String formattedAppliedDate = DateTimeUtils.getDayOfWeekAndDate(item.getAppliedDate());
        holder.appliedDate.setText("Applied Date : " + formattedAppliedDate);

        // Bind data to the Chip
        Chip statusChip = holder.itemView.findViewById(R.id.leaveStatusChip);
        statusChip.setText(item.getStatus());

        int statusColor;
        String status = item.getStatus().toLowerCase();
        if (status.equals("approved")) {
            statusColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.status_approved);
        } else if (status.equals("unapproved")) {
            statusColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.status_pending);
        } else if (status.equals("rejected")) {
            statusColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.status_rejected);
        } else if (status.equals("cancelled")) {
            statusColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.status_rejected);
        } else {
            statusColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.status_pending);
        }
        statusChip.setChipBackgroundColor(ColorStateList.valueOf(statusColor));


        holder.btnViewDetailLeave.setOnClickListener(v -> {
            if (position != RecyclerView.NO_POSITION) {
                AppliedLeavesApproveItem appliedLeavesApproveItem = items.get(position);
                String leaveId = appliedLeavesApproveItem.getId();

                if (leaveId != null && appliedLeavesApproveItem != null) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(ARG_LEAVE_ID, appliedLeavesApproveItem);
                    Navigation.findNavController(v).navigate(R.id.action_nav_approve_leaves_to_nav_approve_leave_data, bundle);
                } else {
                    Toast.makeText(context, "Leave ID is null", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, "Invalid position", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // Inside LeaveApprovalAdapter Class
    public void updateList(List<AppliedLeavesApproveItem> newLeaves) {
        this.items = newLeaves;
        notifyDataSetChanged();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView empName;
        //        TextView leaveName;
        TextView fromDate;
        TextView toDate;
        TextView appliedDate;
        Chip statusChip;
        ShapeableImageView btnViewDetailLeave;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            empName = itemView.findViewById(R.id.empName);
//            leaveName = itemView.findViewById(R.id.leaveName);
            fromDate = itemView.findViewById(R.id.fromDate);
            toDate = itemView.findViewById(R.id.toDate);
            appliedDate = itemView.findViewById(R.id.appliedDate);
            statusChip = itemView.findViewById(R.id.statusChip);
            btnViewDetailLeave = itemView.findViewById(R.id.btn_viewDetailLeave);
        }

    }
}

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
import app.xedigital.ai.model.cmLeaveApprovalPending.AppliedLeavesItem;
import app.xedigital.ai.utills.DateTimeUtils;

public class CrossManagerLeaveApprovalAdapter extends RecyclerView.Adapter<CrossManagerLeaveApprovalAdapter.ViewHolder> {

    private final Context context;
    private List<AppliedLeavesItem> leaves;

    public CrossManagerLeaveApprovalAdapter(List<AppliedLeavesItem> leaves, Context context) {
        this.leaves = leaves;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_crossmanager_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppliedLeavesItem leave = leaves.get(position);
        holder.empName.setText(leave.getEmpFirstName() + " " + leave.getEmpLastName());
        holder.fromDate.setText("From: " + DateTimeUtils.getDayOfWeekAndDate(leave.getFromDate()));
        holder.toDate.setText("To: " + DateTimeUtils.getDayOfWeekAndDate(leave.getToDate()));
        holder.appliedDate.setText("Applied: " + DateTimeUtils.getDayOfWeekAndDate(leave.getAppliedDate()));

        // Bind data to the Chip
        Chip statusChip = holder.itemView.findViewById(R.id.leaveStatusChip);
        statusChip.setText(leave.getStatus());

        int statusColor;
        String status = leave.getStatus().toLowerCase();
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
                AppliedLeavesItem appliedLeavesItem = leaves.get(position);
                String leaveId = appliedLeavesItem.getId();

                if (leaveId != null) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(ARG_LEAVE_ID, appliedLeavesItem);
                    Navigation.findNavController(v).navigate(R.id.action_nav_cross_approval_leave_to_nav_pendingCMLeaveApproval, bundle);
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
        return leaves.size();
    }

    public void updateList(List<AppliedLeavesItem> filteredList) {
        this.leaves = filteredList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView empName;
        TextView fromDate;
        TextView toDate;
        TextView appliedDate;
        Chip leaveStatusChip;
        ShapeableImageView btnViewDetailLeave;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            empName = itemView.findViewById(R.id.empName);
            fromDate = itemView.findViewById(R.id.fromDate);
            toDate = itemView.findViewById(R.id.toDate);
            appliedDate = itemView.findViewById(R.id.appliedDate);
            leaveStatusChip = itemView.findViewById(R.id.leaveStatusChip);
            btnViewDetailLeave = itemView.findViewById(R.id.btn_viewDetailLeave);
        }
    }
}
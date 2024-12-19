package app.xedigital.ai.adapter;

import static app.xedigital.ai.ui.leaves.AppliedViewFragment.ARG_APPLIED_LEAVE;

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
import app.xedigital.ai.model.appliedLeaves.AppliedLeavesItem;
import app.xedigital.ai.utills.DateTimeUtils;

public class AppliedLeaveAdapter extends RecyclerView.Adapter<AppliedLeaveAdapter.ViewHolder> {

    private List<AppliedLeavesItem> appliedLeaves;

    public AppliedLeaveAdapter(List<AppliedLeavesItem> appliedLeaves) {
        this.appliedLeaves = appliedLeaves;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.applied_leave, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppliedLeavesItem appliedLeave = appliedLeaves.get(position);
        String formattedAppliedDate = DateTimeUtils.getDayOfWeekAndDate(appliedLeave.getAppliedDate());
        holder.appliedDate.setText(formattedAppliedDate);
        holder.leaveName.setText(appliedLeave.getLeaveName());
        String formattedFromDate = DateTimeUtils.getDayOfWeekAndDate(appliedLeave.getFromDate());
        holder.fromDate.setText("From " + formattedFromDate);
        String formattedToDate = DateTimeUtils.getDayOfWeekAndDate(appliedLeave.getToDate());
        holder.toDate.setText("To " + formattedToDate);
        holder.statusChip.setText(appliedLeave.getStatus());

        String status = appliedLeave.getStatus().toLowerCase();
        int chipColor;

        if (status.equals("approved")) {
            chipColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.status_approved);
        } else if (status.equals("unapproved")) {
            chipColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.status_pending);
        } else if (status.equals("rejected")) {
            chipColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.status_rejected);
        } else if (status.equals("cancelled")) {
            chipColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.status_rejected);
        } else {
            chipColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.icon_tint);
        }

//        holder.statusChip.setChipBackgroundColorResource(chipColor);

        ColorStateList colorStateList = ColorStateList.valueOf(chipColor);
        holder.statusChip.setChipBackgroundColor(colorStateList);

        holder.btn_viewAppliedLeave.setOnClickListener(v -> {
            if (position != RecyclerView.NO_POSITION) {
                AppliedLeavesItem appliedLeaveItem = appliedLeaves.get(position);
                String leaveId = appliedLeaveItem.getId();
                if (appliedLeaveItem != null && leaveId != null) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(ARG_APPLIED_LEAVE, appliedLeaveItem);
                    Navigation.findNavController(v).navigate(R.id.action_nav_applied_leaves_to_nav_view_applied_leaves, bundle);

                } else {
                    Toast.makeText(v.getContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(v.getContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return appliedLeaves.size();
    }

    public void updateList(List<AppliedLeavesItem> filteredList) {
        this.appliedLeaves = filteredList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView leaveName;
        public TextView fromDate;
        public TextView toDate;
        public TextView appliedDate;
        public Chip statusChip;
        public ShapeableImageView btn_viewAppliedLeave;

        public ViewHolder(View itemView) {
            super(itemView);
            leaveName = itemView.findViewById(R.id.leaveName);
            fromDate = itemView.findViewById(R.id.fromDate);
            toDate = itemView.findViewById(R.id.toDate);
            appliedDate = itemView.findViewById(R.id.appliedDate);
            btn_viewAppliedLeave = itemView.findViewById(R.id.btn_viewAppliedLeave);
            statusChip = itemView.findViewById(R.id.statusChip);

        }
    }
}
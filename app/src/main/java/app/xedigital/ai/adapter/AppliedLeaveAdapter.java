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

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

import app.xedigital.ai.R;
import app.xedigital.ai.model.appliedLeaves.AppliedLeavesItem;
import app.xedigital.ai.utills.DateTimeUtils;

public class AppliedLeaveAdapter extends RecyclerView.Adapter<AppliedLeaveAdapter.ViewHolder> {

    private static final String FALLBACK = "-";
    private List<AppliedLeavesItem> appliedLeaves;

    public AppliedLeaveAdapter(List<AppliedLeavesItem> appliedLeaves) {
        this.appliedLeaves = appliedLeaves;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.applied_leave, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppliedLeavesItem appliedLeave = appliedLeaves.get(position);

        // Null-safe dates
        holder.appliedDate.setText(safeFormatDate(appliedLeave.getAppliedDate()));
        holder.fromDate.setText(safeFormatDate(appliedLeave.getFromDate()));
        holder.toDate.setText(safeFormatDate(appliedLeave.getToDate()));

        // Null-safe leave name
        String leaveName = appliedLeave.getLeaveName();
        holder.leaveName.setText(leaveName != null ? leaveName : FALLBACK);

        // TODO: Avatar should use employee name initials, not leave type initials.
        //  Update once appliedLeaves.AppliedLeavesItem model is confirmed.
        //  Example: getInitials(appliedLeave.getEmpFirstName(), appliedLeave.getEmpLastName())
        holder.empAvatar.setText(getInitials(trim(leaveName)));

        // Null-safe status chip
        String rawStatus = appliedLeave.getStatus();
        holder.statusChip.setText(rawStatus != null ? rawStatus : FALLBACK);

        String status = rawStatus != null ? rawStatus.toLowerCase() : "";
        int chipColor;

        if ("approved".equals(status)) {
            chipColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.status_approved);
        } else if ("unapproved".equals(status)) {
            chipColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.status_pending);
        } else if ("rejected".equals(status)) {
            chipColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.status_rejected);
        } else if ("cancelled".equals(status)) {
            chipColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.status_rejected);
        } else {
            chipColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.icon_tint);
        }

        holder.statusChip.setChipBackgroundColor(ColorStateList.valueOf(chipColor));

        // Click listener — uses getBindingAdapterPosition() to avoid stale position
        View.OnClickListener listener = v -> {
            int adapterPos = holder.getBindingAdapterPosition();
            if (adapterPos == RecyclerView.NO_POSITION) return;

            AppliedLeavesItem item = appliedLeaves.get(adapterPos);
            if (item.getId() != null) {
                Bundle bundle = new Bundle();
                bundle.putSerializable(ARG_APPLIED_LEAVE, item);
                Navigation.findNavController(v)
                        .navigate(R.id.action_nav_applied_leaves_to_nav_view_applied_leaves, bundle);
            } else {
                Toast.makeText(v.getContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        };

        holder.btn_viewAppliedLeave.setOnClickListener(listener);
        holder.appliedLeaveCard.setOnClickListener(listener);
    }

    @Override
    public int getItemCount() {
        return appliedLeaves != null ? appliedLeaves.size() : 0;
    }

    // ======================== HELPERS ========================

    private String safeFormatDate(String rawDate) {
        if (rawDate == null || rawDate.trim().isEmpty()) return FALLBACK;
        try {
            String formatted = DateTimeUtils.getDayOfWeekAndDate(rawDate);
            return (formatted == null || formatted.trim().isEmpty()) ? FALLBACK : formatted;
        } catch (Exception e) {
            return FALLBACK;
        }
    }

    private String getInitials(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) return "?";
        String[] parts = fullName.trim().split("\\s+");
        return (parts.length >= 2)
                ? ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase()
                : ("" + parts[0].charAt(0)).toUpperCase();
    }

    private String trim(String s) {
        return s != null ? s.trim() : "";
    }

    // ======================== PUBLIC API ========================

    public void updateList(List<AppliedLeavesItem> filteredList) {
        this.appliedLeaves = filteredList;
        notifyDataSetChanged();
    }

    // ======================== VIEW HOLDER ========================

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView leaveName;
        public TextView empAvatar;
        public TextView fromDate;
        public TextView toDate;
        public TextView appliedDate;
        public Chip statusChip;
        public ShapeableImageView btn_viewAppliedLeave;
        public MaterialCardView appliedLeaveCard;

        public ViewHolder(View itemView) {
            super(itemView);
            leaveName = itemView.findViewById(R.id.leaveName);
            empAvatar = itemView.findViewById(R.id.empAvatar);
            fromDate = itemView.findViewById(R.id.fromDate);
            toDate = itemView.findViewById(R.id.toDate);
            appliedDate = itemView.findViewById(R.id.appliedDate);
            btn_viewAppliedLeave = itemView.findViewById(R.id.btn_viewAppliedLeave);
            statusChip = itemView.findViewById(R.id.statusChip);
            appliedLeaveCard = itemView.findViewById(R.id.appliedLeaveCard);
        }
    }
}
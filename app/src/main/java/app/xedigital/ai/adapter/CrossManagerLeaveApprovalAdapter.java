package app.xedigital.ai.adapter;

import static app.xedigital.ai.ui.leaves.PendingCMLeaveApprovalFragment.ARG_LEAVE_ID;

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

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.Collections;
import java.util.List;

import app.xedigital.ai.R;
import app.xedigital.ai.model.cmLeaveApprovalPending.AppliedLeavesItem;
import app.xedigital.ai.utills.DateTimeUtils;

public class CrossManagerLeaveApprovalAdapter extends RecyclerView.Adapter<CrossManagerLeaveApprovalAdapter.ViewHolder> {

    private static final String FALLBACK = "-";
    private final Context context;
    private List<AppliedLeavesItem> leaves;

    public CrossManagerLeaveApprovalAdapter(List<AppliedLeavesItem> leaves, Context context) {
        this.leaves = leaves;
        this.context = context;
        sortLatestToOldest(this.leaves);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_crossmanager_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppliedLeavesItem leave = leaves.get(position);

        // Null-safe name
        String firstName = trim(leave.getEmpFirstName());
        String lastName = trim(leave.getEmpLastName());
        String fullName = (firstName + " " + lastName).trim();
        holder.empName.setText(fullName.isEmpty() ? FALLBACK : fullName);

        // Null-safe initials
        holder.empAvatar.setText(getInitials(firstName, lastName));

        // Null-safe dates
        holder.fromDate.setText(safeFormatDate(leave.getFromDate()));
        holder.toDate.setText(safeFormatDate(leave.getToDate()));
        holder.appliedDate.setText(safeFormatDate(leave.getAppliedDate()));

        // Null-safe status chip (use holder field, not redundant findViewById)
        String rawStatus = leave.getStatus();
        holder.leaveStatusChip.setText(rawStatus != null ? rawStatus : FALLBACK);

        String status = rawStatus != null ? rawStatus.toLowerCase() : "";
        int statusColor;
        if ("approved".equals(status)) {
            statusColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.status_approved);
        } else if ("rejected".equals(status)) {
            statusColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.status_rejected);
        } else if ("cancelled".equals(status)) {
            statusColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.status_rejected);
        } else {
            // unapproved / pending / unknown
            statusColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.status_pending);
        }
        holder.leaveStatusChip.setChipBackgroundColor(ColorStateList.valueOf(statusColor));

        // Click listeners — use getBindingAdapterPosition() to avoid stale position
        View.OnClickListener navigateListener = v -> {
            int adapterPos = holder.getBindingAdapterPosition();
            if (adapterPos == RecyclerView.NO_POSITION) {
                Toast.makeText(context, "Invalid position", Toast.LENGTH_SHORT).show();
                return;
            }
            AppliedLeavesItem appliedLeavesItem = leaves.get(adapterPos);
            String leaveId = appliedLeavesItem.getId();

            if (leaveId != null) {
                Bundle bundle = new Bundle();
                bundle.putSerializable(ARG_LEAVE_ID, appliedLeavesItem);
                Navigation.findNavController(v)
                        .navigate(R.id.action_nav_cross_approval_leave_to_nav_pendingCMLeaveApproval, bundle);
            } else {
                Toast.makeText(context, "Leave ID is null", Toast.LENGTH_SHORT).show();
            }
        };

        holder.CMLeaveApprovalCard.setOnClickListener(navigateListener);
        holder.btnViewDetailLeave.setOnClickListener(navigateListener);
    }

    @Override
    public int getItemCount() {
        return leaves != null ? leaves.size() : 0;
    }

    // ======================== SORTING (latest applied date first) ========================

    private void sortLatestToOldest(List<AppliedLeavesItem> list) {
        if (list == null || list.size() <= 1) return;

        Collections.sort(list, (a, b) -> {
            String dateA = a.getAppliedDate();
            String dateB = b.getAppliedDate();

            // Nulls go to the end
            if (dateA == null && dateB == null) return 0;
            if (dateA == null) return 1;
            if (dateB == null) return -1;

            // ISO 8601 strings sort correctly with lexicographic comparison
            // e.g. "2026-10-07T11:38:38.192Z" > "2026-08-25T07:20:34.189Z"
            // Descending: latest first
            return dateB.compareTo(dateA);
        });
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

    private String getInitials(String firstName, String lastName) {
        StringBuilder sb = new StringBuilder();
        if (firstName != null && !firstName.trim().isEmpty()) {
            sb.append(Character.toUpperCase(firstName.trim().charAt(0)));
        }
        if (lastName != null && !lastName.trim().isEmpty()) {
            sb.append(Character.toUpperCase(lastName.trim().charAt(0)));
        }
        return sb.length() == 0 ? "?" : sb.toString();
    }

    private String trim(String s) {
        return s != null ? s.trim() : "";
    }

    // ======================== PUBLIC API ========================

    public void updateList(List<AppliedLeavesItem> filteredList) {
        this.leaves = filteredList;
        sortLatestToOldest(this.leaves);
        notifyDataSetChanged();
    }

    // ======================== VIEW HOLDER ========================

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView empName;
        TextView empAvatar;
        TextView fromDate;
        TextView toDate;
        TextView appliedDate;
        Chip leaveStatusChip;
        ShapeableImageView btnViewDetailLeave;
        MaterialCardView CMLeaveApprovalCard;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            empName = itemView.findViewById(R.id.empName);
            empAvatar = itemView.findViewById(R.id.empAvatar);
            fromDate = itemView.findViewById(R.id.fromDate);
            toDate = itemView.findViewById(R.id.toDate);
            appliedDate = itemView.findViewById(R.id.appliedDate);
            leaveStatusChip = itemView.findViewById(R.id.leaveStatusChip);
            btnViewDetailLeave = itemView.findViewById(R.id.btn_viewDetailLeave);
            CMLeaveApprovalCard = itemView.findViewById(R.id.CMLeaveApprovalCard);
        }
    }
}
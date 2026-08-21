package app.xedigital.ai.adapter;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.chip.Chip;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import app.xedigital.ai.R;
import app.xedigital.ai.model.shiftApplied.EmployeeShiftdataItem;

public class ShiftAppliedAdapter extends RecyclerView.Adapter<ShiftAppliedAdapter.ShiftViewHolder> {

    private final List<EmployeeShiftdataItem> shiftList;

    public ShiftAppliedAdapter(List<EmployeeShiftdataItem> shiftList) {
        this.shiftList = shiftList;
    }

    @NonNull
    @Override
    public ShiftViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_shift_applied, parent, false);
        return new ShiftViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ShiftViewHolder holder, int position) {
        EmployeeShiftdataItem shift = shiftList.get(position);
        android.content.Context ctx = holder.itemView.getContext();

        // ---- Profile ----
        String fullName = shift.getEmployee().getFirstname() + " " + shift.getEmployee().getLastname();
        holder.tvName.setText(fullName);

        Object profileImageUrl = shift.getEmployee().getProfileImageUrl();
        if (profileImageUrl != null) {
            Glide.with(ctx).load(profileImageUrl).apply(RequestOptions.circleCropTransform().placeholder(R.mipmap.ic_default_profile).error(R.mipmap.ic_default_profile)).into(holder.ivProfile);
        } else {
            holder.ivProfile.setImageResource(R.mipmap.ic_default_profile);
        }

        holder.tvEmail.setText(shift.getEmployee().getEmail());
        holder.tvContactNumber.setText(shift.getEmployee().getContact());

        // ---- Current Shift ----
        holder.shiftTimeCurrent.setText(shift.getShift().getName() + " (" + shift.getShift().getStartTime() + " - " + shift.getShift().getEndTime() + ")");
        holder.tvShiftType.setText(shift.getShiftType().getShifttypeName());

        // ---- Requested Update ----
        holder.tvShiftUpdate.setText(shift.getShiftUpdate().getName() + " (" + shift.getShiftUpdate().getStartTime() + " - " + shift.getShiftUpdate().getEndTime() + ")");
        holder.tvRequestedDate.setText(formatDate(shift.getAppliedDate()));

        // ---- Approval Strip ----
        holder.tvApprovedBy.setText(shift.getApprovedByName());
        holder.tvApprovedDate.setText(formatDate(shift.getApprovedDate()));

        // ---- Status Chip + Approval Strip Visibility ----
        bindStatus(holder, ctx, shift.getStatus());
    }

    /**
     * Maps raw API status → readable label, chip color, and approval strip visibility.
     * API values: "unapproved" | "approved" | "cancel"
     */
    private void bindStatus(@NonNull ShiftViewHolder holder, android.content.Context ctx, String rawStatus) {
        String label;
        int chipBgColor;
        int chipTextColor = ContextCompat.getColor(ctx, R.color.white);
        boolean showApprovalStrip = false;

        if (rawStatus == null) rawStatus = "";

        switch (rawStatus.toLowerCase(Locale.getDefault())) {

            case "approved":
                label = "Approved";
                chipBgColor = ContextCompat.getColor(ctx, R.color.status_approved);
                showApprovalStrip = true;
                break;

            case "cancel":
                label = "Cancelled";
                chipBgColor = ContextCompat.getColor(ctx, R.color.status_rejected);
                // Show strip in red-tinted mode so user sees who cancelled
                showApprovalStrip = true;
                // Override text colors to red for cancelled state
                holder.tvApprovedBy.setTextColor(ContextCompat.getColor(ctx, R.color.status_rejected));
                holder.tvApprovedDate.setTextColor(ContextCompat.getColor(ctx, R.color.status_rejected));
                break;

            case "unapproved":
            default:
                label = "Pending";
                chipBgColor = ContextCompat.getColor(ctx, R.color.pending_status_color);
                // Reset text colors in case this ViewHolder was recycled
                // from a cancelled state
                holder.tvApprovedBy.setTextColor(ContextCompat.getColor(ctx, R.color.status_approved));
                holder.tvApprovedDate.setTextColor(ContextCompat.getColor(ctx, R.color.status_approved));
                break;
        }

        // Apply chip
        holder.chipStatus.setText(label);
        holder.chipStatus.setChipBackgroundColor(ColorStateList.valueOf(chipBgColor));
        holder.chipStatus.setTextColor(chipTextColor);

        // Show/hide approval strip
        holder.approvalStripContainer.setVisibility(showApprovalStrip ? View.VISIBLE : View.GONE);
    }

    /**
     * Parses ISO-8601 date string to dd-MM-yyyy.
     * Returns "N/A" on null, returns original string on parse failure.
     */
    private String formatDate(String inputDate) {
        if (inputDate == null || inputDate.isEmpty()) return "N/A";
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            Date date = inputFormat.parse(inputDate);
            return date != null ? outputFormat.format(date) : inputDate;
        } catch (ParseException e) {
            return inputDate; // graceful fallback
        }
    }

    @Override
    public int getItemCount() {
        return shiftList != null ? shiftList.size() : 0;
    }

    // ================================================================
    static class ShiftViewHolder extends RecyclerView.ViewHolder {

        TextView tvName, tvEmail, tvContactNumber;
        TextView shiftTimeCurrent, tvShiftType;
        TextView tvShiftUpdate, tvRequestedDate;
        TextView tvApprovedBy, tvApprovedDate;
        Chip chipStatus;
        ImageView ivProfile;
        LinearLayout approvalStripContainer;   // ← NEW (controls strip visibility)

        public ShiftViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvContactNumber = itemView.findViewById(R.id.tvContactNumber);
            shiftTimeCurrent = itemView.findViewById(R.id.shiftTimeCurrent);
            tvShiftType = itemView.findViewById(R.id.tvShiftType);
            tvRequestedDate = itemView.findViewById(R.id.tvRequestedDate);
            tvApprovedDate = itemView.findViewById(R.id.tvApprovedDate);
            tvShiftUpdate = itemView.findViewById(R.id.tvShiftUpdate);
            tvApprovedBy = itemView.findViewById(R.id.tvApprovedBy);
            chipStatus = itemView.findViewById(R.id.chipStatus);
            ivProfile = itemView.findViewById(R.id.ivProfile);
            approvalStripContainer = itemView.findViewById(R.id.approvalStripContainer); // ← NEW
        }
    }
}
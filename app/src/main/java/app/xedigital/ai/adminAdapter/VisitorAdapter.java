package app.xedigital.ai.adminAdapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.TooltipCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

import app.xedigital.ai.R;
import app.xedigital.ai.model.Admin.VisitorsAdminDetails.VisitorsItem;
import app.xedigital.ai.utills.DateTimeUtils;

public class VisitorAdapter extends RecyclerView.Adapter<VisitorAdapter.VisitorViewHolder> {

    private final List<VisitorsItem> visitorList;
    private final Context context;
    private final OnVisitorActionListener listener;
    ProgressBar progressLoader;

    public VisitorAdapter(Context context, List<VisitorsItem> visitorList, OnVisitorActionListener listener) {
        this.context = context;
        this.visitorList = visitorList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VisitorAdapter.VisitorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_visitors_card, parent, false);
        return new VisitorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VisitorAdapter.VisitorViewHolder holder, int position) {
        VisitorsItem visitor = visitorList.get(position);

        String fullName = visitor.getWhomToMeet().getFirstname() + " " + visitor.getWhomToMeet().getLastname();
        holder.visName.setText(visitor.getName());
        holder.visWhomToMeet.setText(visitor.getWhomToMeet() != null ? fullName : "N/A");
        holder.visContact.setText(visitor.getContact() != null ? visitor.getContact() : "N/A");
        holder.visEmail.setText(visitor.getEmail() != null ? visitor.getEmail() : "N/A");
        holder.visCheckInDate.setText(DateTimeUtils.getDayOfWeekAndDate(visitor.getSignIn()));
        //        holder.visApprovalStatus.setText(visitor.getApprovalStatus());

        SharedPreferences prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        boolean isFirstTime = prefs.getBoolean("show_visitor_hints", true);

        if (position == 0 && isFirstTime) {
            showWorkaroundTooltips(holder);
            // Save that we've shown it
            prefs.edit().putBoolean("show_visitor_hints", false).apply();
        }


        String approvalStatus = visitor.getApprovalStatus();
        String statusText;
        int chipBackgroundColorResourceId;

        if (approvalStatus == null || approvalStatus.isEmpty()) {
            statusText = "N/A";
            chipBackgroundColorResourceId = R.color.status_default;
        } else if (approvalStatus.equalsIgnoreCase("Approved")) {
            statusText = "Approved";
            chipBackgroundColorResourceId = R.color.status_approved;
        } else if (approvalStatus.equalsIgnoreCase("Pending")) {
            statusText = "Pending";
            chipBackgroundColorResourceId = R.color.status_pending;
        } else if (approvalStatus.equalsIgnoreCase("Rejected")) {
            statusText = "Rejected";
            chipBackgroundColorResourceId = R.color.status_pending;
        } else {

            statusText = approvalStatus;
            chipBackgroundColorResourceId = R.color.status_default;
        }

        // Set the text and background color based on approvalStatus
        holder.visApprovalStatus.setText(statusText);
        holder.visApprovalStatus.setChipBackgroundColorResource(chipBackgroundColorResourceId);
        // Load profile image using Glide (fallback to placeholder)
        if (visitor.getProfileImagePath() != null) {
            Glide.with(context).load(visitor.getProfileImagePath()).placeholder(R.drawable.ic_user_placeholder).into(holder.visProfile);
        } else {
            holder.visProfile.setImageResource(R.drawable.ic_user_placeholder);
        }

        holder.btnPrintVisitor.setOnClickListener(v -> Toast.makeText(context, "Available Soon :)", Toast.LENGTH_SHORT).show());

        holder.btnViewMoreVisitor.setOnClickListener(v -> {
            View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_visitor_details, null);

            // Find all dialog views
            ShapeableImageView imgProfile = dialogView.findViewById(R.id.imgProfile);
            TextView tvFullName = dialogView.findViewById(R.id.tvFullName);
            TextView tvContact = dialogView.findViewById(R.id.tvContact);
            TextView tvEmail = dialogView.findViewById(R.id.tvEmail);
            TextView tvWhomToMeet = dialogView.findViewById(R.id.tvWhomToMeet);
            TextView tvCheckInDate = dialogView.findViewById(R.id.tvCheckInDate);
            TextView tvCheckOutDate = dialogView.findViewById(R.id.tvCheckOutDate);
            TextView tvCheckInTime = dialogView.findViewById(R.id.tvCheckInTime);
            TextView tvCheckOutTime = dialogView.findViewById(R.id.tvCheckOutTime);
            TextView tvCompanyFrom = dialogView.findViewById(R.id.tvCompanyFrom);
            TextView tvLaptopSerial = dialogView.findViewById(R.id.tvLaptopSerial);

            // Set data for all fields
            tvFullName.setText(visitor.getName());
            tvContact.setText(visitor.getContact());
            tvEmail.setText(visitor.getEmail());
            tvWhomToMeet.setText((visitor.getWhomToMeet() != null ?
                    visitor.getWhomToMeet().getFirstname() + " " + visitor.getWhomToMeet().getLastname() : "N/A"));

            // Date and Time fields
            tvCheckInDate.setText(DateTimeUtils.getDayOfWeekAndDate(visitor.getSignIn()));
            tvCheckOutDate.setText(visitor.getSignOut() != null ?
                    DateTimeUtils.getDayOfWeekAndDate(visitor.getSignOut()) : "N/A");
            tvCheckInTime.setText(visitor.getSignIn() != null ?
                    DateTimeUtils.extractTime(visitor.getSignIn()) : "N/A");
            tvCheckOutTime.setText(visitor.getSignOut() != null ?
                    DateTimeUtils.extractTime(visitor.getSignOut()) : "N/A");

            // Company and laptop serial
            tvCompanyFrom.setText(visitor.getCompanyFrom() != null ? visitor.getCompanyFrom() : "N/A");
            tvLaptopSerial.setText(visitor.getSerialNumber() != null ? visitor.getSerialNumber() : "N/A");

            // Profile image
            if (visitor.getProfileImagePath() != null) {
                Glide.with(context)
                        .load(visitor.getProfileImagePath())
                        .placeholder(R.drawable.ic_user_placeholder)
                        .into(imgProfile);
            } else {
                imgProfile.setImageResource(R.drawable.ic_user_placeholder);
            }

            // Build and show dialog
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
            builder.setView(dialogView)
                    .setCancelable(true)
                    .setPositiveButton("Close", (dialog, which) -> dialog.dismiss());

            android.app.AlertDialog dialog = builder.create();
            dialog.show();
        });
        // 1. Get Statuses
        String meetingStatus = visitor.getMeetingOverStatus();

        if (approvalStatus != null && approvalStatus.equalsIgnoreCase("Approved")) {
            holder.CheckInContainer.setVisibility(View.GONE);
        } else {
            holder.CheckInContainer.setVisibility(View.VISIBLE);
        }
        if (meetingStatus != null && meetingStatus.equalsIgnoreCase("Done")) {
            holder.CheckOutContainer.setVisibility(View.GONE);
        } else {
            holder.CheckOutContainer.setVisibility(View.VISIBLE);
        }

        holder.btnCheckInVisitor.setOnClickListener(view -> {
            if (listener != null) {
                listener.onCheckInClick(visitor, position);
            }
            Toast.makeText(context, "Check In", Toast.LENGTH_SHORT).show();
        });
        holder.btnCheckOutVisitor.setOnClickListener(view -> {
            Toast.makeText(context, "Check Out", Toast.LENGTH_SHORT).show();
            if (listener != null) {
                listener.onCheckOutClick(visitor, position);
            }
        });

    }

    public void showWorkaroundTooltips(VisitorViewHolder holder) {
        // This helps the user see what the buttons do
        TooltipCompat.setTooltipText(holder.btnCheckInVisitor, "Check In");
        TooltipCompat.setTooltipText(holder.btnCheckOutVisitor, "Check Out");
        TooltipCompat.setTooltipText(holder.btnViewMoreVisitor, "View Details");
        TooltipCompat.setTooltipText(holder.btnPrintVisitor, "Print Badge");

        // To make it "pop up" automatically once:
        holder.btnCheckInVisitor.postDelayed(() -> {
            if (holder.btnCheckInVisitor.isAttachedToWindow()) {
                // Simulating a long click triggers the tooltip visually
                holder.btnCheckInVisitor.performLongClick();
            }
        }, 500);
    }
    // Inside VisitorAdapter.java

    @Override
    public int getItemCount() {
        return visitorList.size();
    }

    public interface OnVisitorActionListener {
        void onCheckInClick(VisitorsItem visitor, int position);

        void onCheckOutClick(VisitorsItem visitor, int position);
    }

    public class VisitorViewHolder extends RecyclerView.ViewHolder {
        TextView visName, visWhomToMeet, visContact, visEmail, visCheckInDate;
        Chip visApprovalStatus;
        ShapeableImageView visProfile, btnViewMoreVisitor, btnPrintVisitor, btnCheckInVisitor, btnCheckOutVisitor;
        LinearLayout CheckOutContainer, CheckInContainer;


        public VisitorViewHolder(@NonNull View itemView) {
            super(itemView);
            visName = itemView.findViewById(R.id.visName);
            visWhomToMeet = itemView.findViewById(R.id.visWhomToMeet);
            visContact = itemView.findViewById(R.id.visContact);
            visEmail = itemView.findViewById(R.id.visEmail);
            visCheckInDate = itemView.findViewById(R.id.visCheckInDate);
            visApprovalStatus = itemView.findViewById(R.id.visApprovalStatus);
            visProfile = itemView.findViewById(R.id.visProfile);
            btnViewMoreVisitor = itemView.findViewById(R.id.btnViewMoreVisitor);
            btnPrintVisitor = itemView.findViewById(R.id.btnPrintVisitor);
            btnCheckInVisitor = itemView.findViewById(R.id.btnCheckInVisitor);
            btnCheckOutVisitor = itemView.findViewById(R.id.btnCheckOutVisitor);
            progressLoader = itemView.findViewById(R.id.progressLoader);
            CheckInContainer = itemView.findViewById(R.id.checkInContainer);
            CheckOutContainer = itemView.findViewById(R.id.checkOutContainer);

        }
    }
}

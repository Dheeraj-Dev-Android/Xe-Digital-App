package app.xedigital.ai.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;

import java.util.List;

import app.xedigital.ai.R;
import app.xedigital.ai.model.vms.VisitorsItem;
import app.xedigital.ai.utills.DateTimeUtils;

public class VisitorsAdapter extends RecyclerView.Adapter<VisitorsAdapter.ViewHolder> {

    private List<VisitorsItem> visitors;

    public VisitorsAdapter(List<VisitorsItem> visitors) {
        this.visitors = visitors;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.visitor_item_layout, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        VisitorsItem visitor = visitors.get(position);

        // Bind data to views in the new layout
        holder.tvVisitorName.setText(visitor.getName());
        holder.tvSerialNumber.setText("Serial Number: " + visitor.getSerialNumber());
        holder.tvVisitorCategory.setText("Category: " + visitor.getVisitorCategory());
        holder.tvEmail.setText("Email: " + visitor.getEmail());
        holder.tvContact.setText("Contact: " + visitor.getContact());
        holder.tvCompanyFrom.setText("Company: " + visitor.getCompanyFrom());
        holder.tvWhomToMeet.setText("Meeting With: " + visitor.getWhomToMeet().getFirstname() + " " + visitor.getWhomToMeet().getLastname());
        holder.tvPurposeOfMeeting.setText("Purpose: " + visitor.getPurposeOfmeeting());
        holder.tvMeetingOverStatus.setText("Status: " + visitor.getMeetingOverStatus());
        holder.tvCheckinDateTime.setText("Check-in: " + DateTimeUtils.formatTime(visitor.getSignIn()));
        holder.tvCheckoutDateTime.setText("Check-out: " + DateTimeUtils.formatTime(visitor.getSignOut()));
        holder.tvMeetingOverDateTime.setText("Meeting Over: " + DateTimeUtils.getDayOfWeekAndDate(visitor.getMeetingOverDate()));
        holder.tvPreApproved.setText("Pre-approved: " + visitor.isIsPreApproved());
        holder.tvPreApprovedDate.setText("Pre-approval Date: " + DateTimeUtils.getDayOfWeekAndDate(visitor.getPreApprovedDate()));
        holder.tvVisitorVisited.setText("Visitor Visited: " + visitor.isIsVisitorVisited());

        Glide.with(holder.itemView.getContext())
                .load(visitor.getProfileImagePath())
                .placeholder(R.drawable.ic_profile_placeholder)
                .error(R.drawable.ic_profile_placeholder)
                .circleCrop()
                .into(holder.ivVisitorProfile);

        String status = visitor.getApprovalStatus();

        if (status == null || status.isEmpty()) {
            status = "Pending"; // Set default status to "Pending"
        }

        // Set chip text and background color based on status
        holder.chipApprovalStatus.setText(status);
        if (status.equalsIgnoreCase("Approved")) {
            holder.chipApprovalStatus.setChipBackgroundColorResource(R.color.status_approved);
        } else if (status.equalsIgnoreCase("Pending")) {
            holder.chipApprovalStatus.setChipBackgroundColorResource(R.color.pending_status_color);
        } else if (status.equalsIgnoreCase("Rejected")) {
            holder.chipApprovalStatus.setChipBackgroundColorResource(R.color.status_rejected);
        } else {
            // Handle other status values or set a default color
            holder.chipApprovalStatus.setChipBackgroundColorResource(R.color.icon_tint);
        }

    }

    @Override
    public int getItemCount() {
        return visitors.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvVisitorName;
        public TextView tvSerialNumber;
        public TextView tvVisitorCategory;
        public TextView tvEmail;
        public TextView tvContact;
        public TextView tvCompanyFrom;
        public TextView tvWhomToMeet;
        public TextView tvPurposeOfMeeting;
        public TextView tvMeetingOverStatus;
        public TextView tvCheckinDateTime;
        public TextView tvCheckoutDateTime;
        public TextView tvMeetingOverDateTime;
        public TextView tvPreApproved;
        public TextView tvPreApprovedDate;
        public TextView tvVisitorVisited;
        public Chip chipApprovalStatus;
        public ImageView ivVisitorProfile;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvVisitorName = itemView.findViewById(R.id.tvVisitorName);
            tvSerialNumber = itemView.findViewById(R.id.tvSerialNumber);
            tvVisitorCategory = itemView.findViewById(R.id.tvVisitorCategory);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvContact = itemView.findViewById(R.id.tvContact);
            tvCompanyFrom = itemView.findViewById(R.id.tvCompanyFrom);
            tvWhomToMeet = itemView.findViewById(R.id.tvWhomToMeet);
            tvPurposeOfMeeting = itemView.findViewById(R.id.tvPurposeOfMeeting);
            tvMeetingOverStatus = itemView.findViewById(R.id.tvMeetingOverStatus);
            tvCheckinDateTime = itemView.findViewById(R.id.tvCheckinDateTime);
            tvCheckoutDateTime = itemView.findViewById(R.id.tvCheckoutDateTime);
            tvMeetingOverDateTime = itemView.findViewById(R.id.tvMeetingOverDateTime);
            tvPreApproved = itemView.findViewById(R.id.tvPreApproved);
            tvPreApprovedDate = itemView.findViewById(R.id.tvPreApprovedDate);
            tvVisitorVisited = itemView.findViewById(R.id.tvVisitorVisited);
            chipApprovalStatus = itemView.findViewById(R.id.chipApprovalStatus);
            ivVisitorProfile = itemView.findViewById(R.id.ivVisitorProfile);
        }
    }
}
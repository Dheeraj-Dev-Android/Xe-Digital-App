package app.xedigital.ai.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

import app.xedigital.ai.R;
import app.xedigital.ai.model.vms.VisitorsItem;

public class VisitorsAdapter extends RecyclerView.Adapter<VisitorsAdapter.ViewHolder> {

    private final VisitorClickListener clickListener;
    private List<VisitorsItem> visitors;

    public VisitorsAdapter(List<VisitorsItem> visitors, VisitorClickListener clickListener) {
        this.visitors = visitors != null ? visitors : new ArrayList<>();
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.visitor_item_layout, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        VisitorsItem visitor = visitors.get(position);

        // Bind data to views in the new layout
        holder.tvVisitorName.setText(visitor.getName() != null ? visitor.getName() : "N/A");
        holder.tvEmail.setText((visitor.getEmail() != null ? visitor.getEmail() : "N/A"));
        holder.tvContact.setText((visitor.getContact() != null ? visitor.getContact() : "N/A"));
        holder.tvCompanyFrom.setText("Company From: " + (visitor.getCompanyFrom() != null ? visitor.getCompanyFrom() : "N/A"));
        if (visitor.getWhomToMeet() != null) {
            String firstName = visitor.getWhomToMeet().getFirstname() != null ? visitor.getWhomToMeet().getFirstname() : "";
            String lastName = visitor.getWhomToMeet().getLastname() != null ? visitor.getWhomToMeet().getLastname() : "";
            holder.tvMeetingWith.setText("Meeting With: " + firstName + " " + lastName);
        } else {
            holder.tvMeetingWith.setText("Meeting With: N/A");
        }
        String profileImagePath = visitor.getProfileImagePath();
        if (profileImagePath != null && !profileImagePath.isEmpty()) {
            Glide.with(holder.itemView.getContext()).load(profileImagePath).placeholder(R.drawable.ic_profile_placeholder).error(R.drawable.ic_profile_placeholder).circleCrop().into(holder.ivVisitorProfile);
        } else {
            Glide.with(holder.itemView.getContext()).load(R.drawable.ic_profile_placeholder).circleCrop().into(holder.ivVisitorProfile);
            Log.w("VisitorsAdapter", "Profile image path is null or empty for visitor: " + visitor.getName());
        }
        String status = visitor.getApprovalStatus();
        if (status == null || status.isEmpty()) {
            status = "Pending";
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
        // Add click listener to the card
        holder.cardViewVisitor.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onVisitorClicked(visitor);
            }
        });
    }

    @Override
    public int getItemCount() {
        return visitors != null ? visitors.size() : 0;
    }

    public void updateVisitors(List<VisitorsItem> visitorsItems) {
        this.visitors = visitorsItems != null ? visitorsItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvVisitorName;
        public TextView tvEmail;
        public TextView tvContact;
        public TextView tvCompanyFrom;
        public TextView tvWhomToMeet;
        public TextView tvPurposeOfMeeting;
        public TextView tvMeetingOverStatus;
        public TextView tvCheckinDateTime;
        public TextView tvCheckoutDateTime;
        public TextView tvMeetingOverDateTime;
        public TextView tvMeetingWith;
        public TextView tvPreApprovedDate;
        public TextView tvVisitorVisited;
        public Chip chipApprovalStatus;
        public ImageView ivVisitorProfile;
        public MaterialCardView cardViewVisitor;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvVisitorName = itemView.findViewById(R.id.tvVisitorName);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvContact = itemView.findViewById(R.id.tvContact);
            tvCompanyFrom = itemView.findViewById(R.id.tvCompanyFrom);
            tvWhomToMeet = itemView.findViewById(R.id.tvWhomToMeet);
            tvPurposeOfMeeting = itemView.findViewById(R.id.tvPurposeOfMeeting);
            tvMeetingOverStatus = itemView.findViewById(R.id.tvMeetingOverStatus);
            tvCheckinDateTime = itemView.findViewById(R.id.tvCheckinDateTime);
            tvCheckoutDateTime = itemView.findViewById(R.id.tvCheckoutDateTime);
            tvMeetingOverDateTime = itemView.findViewById(R.id.tvMeetingOverDateTime);
            tvMeetingWith = itemView.findViewById(R.id.tvMeetingWith);
            tvPreApprovedDate = itemView.findViewById(R.id.tvPreApprovedDate);
            tvVisitorVisited = itemView.findViewById(R.id.tvVisitorVisited);
            chipApprovalStatus = itemView.findViewById(R.id.chipApprovalStatus);
            ivVisitorProfile = itemView.findViewById(R.id.ivVisitorProfile);
            cardViewVisitor = itemView.findViewById(R.id.cardViewVisitor);
        }
    }
}
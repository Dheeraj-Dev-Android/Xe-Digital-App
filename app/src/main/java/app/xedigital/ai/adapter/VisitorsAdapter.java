package app.xedigital.ai.adapter;

import android.content.Context;
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

        // NPE Safety: Prevent crash if the list contains a null object
        if (visitor == null) return;

        Context context = holder.itemView.getContext();

        // Bind data safely using the helper method
        holder.tvVisitorName.setText(getDisplayValue(visitor.getName()));
        holder.tvEmail.setText(getDisplayValue(visitor.getEmail()));
        holder.tvContact.setText(getDisplayValue(visitor.getContact()));
        holder.tvCompanyFrom.setText(getDisplayValue(visitor.getCompanyFrom()));

        // Safe handling for whomToMeet to prevent solitary spaces (e.g., " ")
        if (visitor.getWhomToMeet() != null) {
            String firstName = getDisplayValue(visitor.getWhomToMeet().getFirstname());
            String lastName = getDisplayValue(visitor.getWhomToMeet().getLastname());

            if (firstName.equals("N/A") && lastName.equals("N/A")) {
                holder.tvMeetingWith.setText("N/A");
            } else if (firstName.equals("N/A")) {
                holder.tvMeetingWith.setText(lastName);
            } else if (lastName.equals("N/A")) {
                holder.tvMeetingWith.setText(firstName);
            } else {
                holder.tvMeetingWith.setText(firstName + " " + lastName);
            }
        } else {
            holder.tvMeetingWith.setText("N/A");
        }

        // Image loading: safe string trim check
        String profileImagePath = visitor.getProfileImagePath();
        if (profileImagePath != null && !profileImagePath.trim().isEmpty()) {
            Glide.with(context)
                    .load(profileImagePath)
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .error(R.drawable.ic_profile_placeholder)
                    .circleCrop()
                    .into(holder.ivVisitorProfile);
        } else {
            Glide.with(context)
                    .load(R.drawable.ic_profile_placeholder)
                    .circleCrop()
                    .into(holder.ivVisitorProfile);
            Log.w("VisitorsAdapter", "Profile image path is null or empty for visitor: " + visitor.getName());
        }

        // Status logic
        String status = visitor.getApprovalStatus();
        if (status == null || status.trim().isEmpty()) {
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
            holder.chipApprovalStatus.setChipBackgroundColorResource(R.color.icon_tint);
        }

        // Click listener
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

    /**
     * Helper method to return "N/A" if string is null or completely blank.
     */
    private String getDisplayValue(String value) {
        return (value == null || value.trim().isEmpty()) ? "N/A" : value;
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
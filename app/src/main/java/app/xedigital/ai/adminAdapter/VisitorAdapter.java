package app.xedigital.ai.adminAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
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

    public VisitorAdapter(Context context, List<VisitorsItem> visitorList) {
        this.context = context;
        this.visitorList = visitorList;
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
        holder.visContact.setText(visitor.getContact());
        holder.visEmail.setText(visitor.getEmail());
        holder.visCheckInDate.setText(DateTimeUtils.getDayOfWeekAndDate(visitor.getSignIn()));
//        holder.visApprovalStatus.setText(visitor.getApprovalStatus());


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


    }

    @Override
    public int getItemCount() {
        return visitorList.size();
    }

    public class VisitorViewHolder extends RecyclerView.ViewHolder {
        TextView visName, visWhomToMeet, visContact, visEmail, visCheckInDate;
        Chip visApprovalStatus;
        ShapeableImageView visProfile;

        public VisitorViewHolder(@NonNull View itemView) {
            super(itemView);
            visName = itemView.findViewById(R.id.visName);
            visWhomToMeet = itemView.findViewById(R.id.visWhomToMeet);
            visContact = itemView.findViewById(R.id.visContact);
            visEmail = itemView.findViewById(R.id.visEmail);
            visCheckInDate = itemView.findViewById(R.id.visCheckInDate);
            visApprovalStatus = itemView.findViewById(R.id.visApprovalStatus);
            visProfile = itemView.findViewById(R.id.visProfile);
        }
    }
}

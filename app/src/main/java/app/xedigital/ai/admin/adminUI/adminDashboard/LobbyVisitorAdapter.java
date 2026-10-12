package app.xedigital.ai.admin.adminUI.adminDashboard;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

import app.xedigital.ai.R;
import app.xedigital.ai.admin.adminModal.VisitorsAdminDetails.VisitorsItem;
import app.xedigital.ai.admin.adminModal.VisitorsAdminDetails.WhomToMeet;
import app.xedigital.ai.utills.DateTimeUtils;

public class LobbyVisitorAdapter extends RecyclerView.Adapter<LobbyVisitorAdapter.VisitorViewHolder> {

    private final List<VisitorsItem> visitors;
    private final OnVisitorClickListener clickListener;
    public LobbyVisitorAdapter(List<VisitorsItem> visitors, OnVisitorClickListener clickListener) {
        this.visitors = visitors;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public VisitorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_visitor_dashboard, parent, false);
        return new VisitorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VisitorViewHolder holder, int position) {
        VisitorsItem item = visitors.get(position);
        if (item != null) {
            holder.bind(item, clickListener);
        }
    }

    @Override
    public int getItemCount() {
        return visitors != null ? visitors.size() : 0;
    }

    public interface OnVisitorClickListener {
        void onVisitorClick(VisitorsItem visitor);
    }

    public static class VisitorViewHolder extends RecyclerView.ViewHolder {
        private final ShapeableImageView ivVisitorProfile;
        private final TextView tvProfileInitials;
        private final TextView tvVisitorName;
        private final TextView tvApprovalStatus;
        private final TextView tvTimingDatesIn;
        private final TextView tvTimingDatesOut;
        private final TextView tvWhomToMeet;
        private final TextView tvMeetingOverStatus;
        private final String fallbackText = "--";

        // Material Design colors for the circular initials fallback
        private final int[] placeholderColors = new int[]{
                Color.parseColor("#4285F4"), // Google Blue
                Color.parseColor("#0F9D58"), // Google Green
                Color.parseColor("#DB4437"), // Google Red
                Color.parseColor("#F4B400"), // Google Yellow
                Color.parseColor("#9C27B0"), // Material Purple
                Color.parseColor("#00ACC1")  // Material Teal
        };

        public VisitorViewHolder(@NonNull View itemView) {
            super(itemView);
            ivVisitorProfile = itemView.findViewById(R.id.ivVisitorProfile);
            tvProfileInitials = itemView.findViewById(R.id.tvProfileInitials);
            tvVisitorName = itemView.findViewById(R.id.tvVisitorName);
            tvApprovalStatus = itemView.findViewById(R.id.tvApprovalStatus);
            tvTimingDatesIn = itemView.findViewById(R.id.tvTimingDatesIn);
            tvTimingDatesOut = itemView.findViewById(R.id.tvTimingDatesOut);
            tvWhomToMeet = itemView.findViewById(R.id.tvWhomToMeet);
            tvMeetingOverStatus = itemView.findViewById(R.id.tvMeetingOverStatus);
        }

        public void bind(final VisitorsItem item, final OnVisitorClickListener listener) {
            // 1. Visitor Full Name
            String name = item.getName();
            String displayName;
            if (name != null && !name.trim().isEmpty()) {
                displayName = name.trim();
            } else {
                displayName = "Unknown Visitor";
            }
            tvVisitorName.setText(displayName);

            // 2. Approval Status Badge & Dynamic Chip Background Tinting
            String approvalStatus = item.getApprovalStatus();
            if (approvalStatus != null && !approvalStatus.trim().isEmpty()) {
                // Capitalize first letter for presentation (e.g. "approved" -> "Approved")
                String capitalizedStatus = approvalStatus.substring(0, 1).toUpperCase() + approvalStatus.substring(1).toLowerCase();
                tvApprovalStatus.setText(capitalizedStatus);
                tvApprovalStatus.setVisibility(View.VISIBLE);

                if (approvalStatus.equalsIgnoreCase("approved")) {
                    tvApprovalStatus.setTextColor(Color.parseColor("#2E7D32")); // Dark Green Text
                    tvApprovalStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E8F5E9"))); // Light Green Chip BG
                } else if (approvalStatus.equalsIgnoreCase("pending")) {
                    tvApprovalStatus.setTextColor(Color.parseColor("#EF6C00")); // Dark Orange Text
                    tvApprovalStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFF3E0"))); // Light Orange Chip BG
                } else {
                    tvApprovalStatus.setTextColor(Color.parseColor("#C62828")); // Dark Red Text
                    tvApprovalStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFEBEE"))); // Light Red Chip BG
                }
            } else {
                tvApprovalStatus.setText("Pending");
                tvApprovalStatus.setTextColor(Color.parseColor("#EF6C00"));
                tvApprovalStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFF3E0")));
            }

            // 3. Timings Check-in
            String signInTime = item.getSignIn();
            if (signInTime == null || signInTime.trim().isEmpty() || signInTime.equals("1900-01-01T00:00:00.000Z")) {
                signInTime = item.getStartTime();
            }
            String formattedIn = DateTimeUtils.formatTime(signInTime);
            tvTimingDatesIn.setText(formattedIn.equals("N/A") ? fallbackText : formattedIn);

            // 4. Timings Check-out
            String signOutTime = item.getSignOut();
            if (signOutTime == null || signOutTime.trim().isEmpty() || signOutTime.equals("1900-01-01T00:00:00.000Z")) {
                signOutTime = item.getEndTime();
            }
            String formattedOut = DateTimeUtils.formatTime(signOutTime);
            tvTimingDatesOut.setText(formattedOut.equals("N/A") ? fallbackText : formattedOut);

            // 5. Meeting Host (Whom to Meet)
            WhomToMeet host = item.getWhomToMeet();
            if (host != null) {
                String hostFirst = host.getFirstname();
                String hostLast = host.getLastname();
                String hostFullName;

                if (hostFirst != null && !hostFirst.trim().isEmpty() && hostLast != null && !hostLast.trim().isEmpty()) {
                    hostFullName = hostFirst.trim() + " " + hostLast.trim();
                } else if (hostFirst != null && !hostFirst.trim().isEmpty()) {
                    hostFullName = hostFirst.trim();
                } else if (hostLast != null && !hostLast.trim().isEmpty()) {
                    hostFullName = hostLast.trim();
                } else {
                    hostFullName = fallbackText;
                }
                tvWhomToMeet.setText(hostFullName);
            } else {
                String nameWhomToMeet = item.getNameWhomToMeet();
                if (nameWhomToMeet != null && !nameWhomToMeet.trim().isEmpty()) {
                    tvWhomToMeet.setText(nameWhomToMeet.trim());
                } else {
                    tvWhomToMeet.setText(fallbackText);
                }
            }

            // 6. Meeting Status (yes/no to Completed/In Progress mapping)
            String meetingStatus = item.getMeetingOverStatus();
            if (meetingStatus != null && meetingStatus.equalsIgnoreCase("yes")) {
                tvMeetingOverStatus.setText("Completed");
                tvMeetingOverStatus.setTextColor(Color.parseColor("#2E7D32")); // Green
            } else if (meetingStatus != null && meetingStatus.equalsIgnoreCase("no")) {
                tvMeetingOverStatus.setText("In Progress");
                tvMeetingOverStatus.setTextColor(Color.parseColor("#1565C0")); // Blue
            } else {
                tvMeetingOverStatus.setText(fallbackText);
                tvMeetingOverStatus.setTextColor(Color.GRAY);
            }

            // 7. Profile Picture / Initials Switch Engine
            String profileImage = item.getProfileImagePath();
            if (profileImage != null && !profileImage.trim().isEmpty()) {
                ivVisitorProfile.setVisibility(View.VISIBLE);
                if (tvProfileInitials != null) tvProfileInitials.setVisibility(View.GONE);

                Glide.with(itemView.getContext())
                        .load(profileImage)
                        .apply(new RequestOptions()
                                .placeholder(R.mipmap.ic_default_profile)
                                .error(R.mipmap.ic_default_profile)
                                .centerCrop())
                        .into(ivVisitorProfile);
            } else {
                if (tvProfileInitials != null) {
                    ivVisitorProfile.setVisibility(View.GONE);
                    tvProfileInitials.setVisibility(View.VISIBLE);

                    String initials = getInitials(displayName);
                    tvProfileInitials.setText(initials);

                    // Dynamic background color selection based on visitor name hash to ensure variety
                    int colorIndex = Math.abs(displayName.hashCode()) % placeholderColors.length;
                    tvProfileInitials.setBackgroundTintList(ColorStateList.valueOf(placeholderColors[colorIndex]));
                } else {
                    ivVisitorProfile.setVisibility(View.VISIBLE);
                    ivVisitorProfile.setImageResource(R.mipmap.ic_default_profile);
                }
            }

            // Item Click Dispatcher
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onVisitorClick(item);
                }
            });
        }

        private String getInitials(String nameStr) {
            if (nameStr == null || nameStr.trim().isEmpty()) {
                return "--";
            }
            String[] parts = nameStr.trim().split("\\s+");
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < Math.min(parts.length, 2); i++) {
                if (!parts[i].isEmpty()) {
                    builder.append(parts[i].toUpperCase().charAt(0));
                }
            }
            return builder.toString();
        }
    }
}
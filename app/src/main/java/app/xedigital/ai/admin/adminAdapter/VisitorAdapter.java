package app.xedigital.ai.admin.adminAdapter;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.TooltipCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

import app.xedigital.ai.R;
import app.xedigital.ai.admin.adminModal.VisitorsAdminDetails.VisitorsItem;
import app.xedigital.ai.utills.DateTimeUtils;
import app.xedigital.ai.utills.SecurePrefManager;

public class VisitorAdapter extends RecyclerView.Adapter<VisitorAdapter.VisitorViewHolder> {

    private static final int[] AVATAR_COLORS = {
            0xFF1E88E5,
            0xFF43A047,
            0xFFE53935,
            0xFF8E24AA,
            0xFFFB8C00,
            0xFF00ACC1,
            0xFF3949AB,
            0xFFD81B60,
            0xFF00897B,
            0xFF6D4C41
    };

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
    public VisitorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_visitors_card, parent, false);
        return new VisitorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VisitorViewHolder holder, int position) {
        VisitorsItem visitor = visitorList.get(position);

        // Safe Name Resolution
        String name = visitor.getName();
        String displayName = (name != null && !name.trim().isEmpty()) ? name.trim() : "N/A";
        holder.visName.setText(displayName);

        // Safe Whom To Meet Resolution
        String whomToMeetText = "N/A";
        if (visitor.getWhomToMeet() != null) {
            String firstName = visitor.getWhomToMeet().getFirstname();
            String lastName = visitor.getWhomToMeet().getLastname();
            StringBuilder sb = new StringBuilder();
            if (firstName != null && !firstName.trim().isEmpty()) {
                sb.append(firstName.trim());
            }
            if (lastName != null && !lastName.trim().isEmpty()) {
                if (sb.length() > 0) sb.append(" ");
                sb.append(lastName.trim());
            }
            if (sb.length() > 0) {
                whomToMeetText = sb.toString();
            }
        }
        holder.visWhomToMeet.setText(whomToMeetText);

        // Safe Contact Resolution
        String contact = visitor.getContact();
        holder.visContact.setText((contact != null && !contact.trim().isEmpty()) ? contact : "N/A");

        // Safe Email Resolution
        String email = visitor.getEmail();
        holder.visEmail.setText((email != null && !email.trim().isEmpty()) ? email : "N/A");

        // Safe Check-In Date Resolution
        String signIn = visitor.getSignIn();
        if (signIn != null && !signIn.trim().isEmpty()) {
            holder.visCheckInDate.setText(DateTimeUtils.getDayOfWeekAndDate(signIn));
        } else {
            holder.visCheckInDate.setText("N/A");
        }

        // Hint Tooltips
        SecurePrefManager prefs = SecurePrefManager.getInstance(context);
        boolean isFirstTime = prefs.getBoolean("show_visitor_hints", true);

        if (position == 0 && isFirstTime) {
            showWorkaroundTooltips(holder);
            prefs.putBoolean("show_visitor_hints", false);
        }

        // Safe Approval Status and Chip Color
        String approvalStatus = visitor.getApprovalStatus();
        String statusText;
        int chipBackgroundColorResourceId;

        if (approvalStatus == null || approvalStatus.trim().isEmpty()) {
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

        holder.visApprovalStatus.setText(statusText);
        holder.visApprovalStatus.setChipBackgroundColorResource(chipBackgroundColorResourceId);

        // Profile Image OR Name Initials Fallback
        String profileImage = visitor.getProfileImagePath();
        if (profileImage != null && !profileImage.trim().isEmpty()) {
            holder.visProfile.setVisibility(View.VISIBLE);
            holder.tvProfileInitials.setVisibility(View.GONE);
            Glide.with(context).load(profileImage).placeholder(R.drawable.ic_profile_placeholder).into(holder.visProfile);
        } else {
            holder.visProfile.setVisibility(View.GONE);
            holder.tvProfileInitials.setVisibility(View.VISIBLE);

            String initials = getInitials(displayName);
            int avatarColor = getAvatarColor(displayName);

            holder.tvProfileInitials.setText(initials);

            GradientDrawable circularBg = new GradientDrawable();
            circularBg.setShape(GradientDrawable.OVAL);
            circularBg.setColor(avatarColor);
            holder.tvProfileInitials.setBackground(circularBg);
        }

        // Print Button Listener — delegates to fragment
        holder.btnPrintVisitor.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPrintClick(visitor, position);
            }
        });

        // View Details Dialog Listener
        final String resolvedWhomToMeet = whomToMeetText;
        holder.btnViewMoreVisitor.setOnClickListener(v -> {
            View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_visitor_details, null);

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

            String dName = visitor.getName();
            String dialogDisplayName = (dName != null && !dName.trim().isEmpty()) ? dName.trim() : "N/A";
            tvFullName.setText(dialogDisplayName);

            String dContact = visitor.getContact();
            tvContact.setText((dContact != null && !dContact.trim().isEmpty()) ? dContact : "N/A");

            String dEmail = visitor.getEmail();
            tvEmail.setText((dEmail != null && !dEmail.trim().isEmpty()) ? dEmail : "N/A");

            tvWhomToMeet.setText(resolvedWhomToMeet);

            String dSignIn = visitor.getSignIn();
            if (dSignIn != null && !dSignIn.trim().isEmpty()) {
                tvCheckInDate.setText(DateTimeUtils.getDayOfWeekAndDate(dSignIn));
                tvCheckInTime.setText(DateTimeUtils.extractTime(dSignIn));
            } else {
                tvCheckInDate.setText("N/A");
                tvCheckInTime.setText("N/A");
            }

            String dSignOut = visitor.getSignOut();
            if (dSignOut != null && !dSignOut.trim().isEmpty()) {
                tvCheckOutDate.setText(DateTimeUtils.getDayOfWeekAndDate(dSignOut));
                tvCheckOutTime.setText(DateTimeUtils.extractTime(dSignOut));
            } else {
                tvCheckOutDate.setText("N/A");
                tvCheckOutTime.setText("N/A");
            }

            String companyFrom = visitor.getCompanyFrom();
            tvCompanyFrom.setText((companyFrom != null && !companyFrom.trim().isEmpty()) ? companyFrom : "N/A");

            String serialNumber = visitor.getSerialNumber();
            tvLaptopSerial.setText((serialNumber != null && !serialNumber.trim().isEmpty()) ? serialNumber : "N/A");

            if (profileImage != null && !profileImage.trim().isEmpty()) {
                Glide.with(context).load(profileImage).placeholder(R.drawable.ic_profile_placeholder).into(imgProfile);
            } else {
                String initials = getInitials(dialogDisplayName);
                int avatarColor = getAvatarColor(dialogDisplayName);
                imgProfile.setImageBitmap(createInitialsBitmap(initials, avatarColor, 200));
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setView(dialogView).setCancelable(true).setPositiveButton("Close", (dialog, which) -> dialog.dismiss());

            AlertDialog dialog = builder.create();
            dialog.show();
        });

        // Container Visibility Logic
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

        // Action Listeners
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
        TooltipCompat.setTooltipText(holder.btnCheckInVisitor, "Check In");
        TooltipCompat.setTooltipText(holder.btnCheckOutVisitor, "Check Out");
        TooltipCompat.setTooltipText(holder.btnViewMoreVisitor, "View Details");
        TooltipCompat.setTooltipText(holder.btnPrintVisitor, "Print Badge");

        holder.btnCheckInVisitor.postDelayed(() -> {
            if (holder.btnCheckInVisitor.isAttachedToWindow()) {
                holder.btnCheckInVisitor.performLongClick();
            }
        }, 500);
    }

    @Override
    public int getItemCount() {
        return visitorList != null ? visitorList.size() : 0;
    }

    private String getInitials(String name) {
        if (name == null || name.trim().isEmpty() || name.equalsIgnoreCase("N/A")) {
            return "?";
        }
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }
        return String.valueOf((parts[0].charAt(0) + parts[parts.length - 1].charAt(0)));
    }

    private int getAvatarColor(String name) {
        if (name == null || name.trim().isEmpty()) {
            return AVATAR_COLORS[0];
        }
        int hash = Math.abs(name.trim().hashCode());
        return AVATAR_COLORS[hash % AVATAR_COLORS.length];
    }

    private android.graphics.Bitmap createInitialsBitmap(String initials, int backgroundColor, int size) {
        android.graphics.Bitmap bitmap = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888);
        android.graphics.Canvas canvas = new android.graphics.Canvas(bitmap);

        android.graphics.Paint paint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
        paint.setColor(backgroundColor);
        paint.setStyle(android.graphics.Paint.Style.FILL);
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint);

        android.graphics.Paint textPaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(android.graphics.Color.WHITE);
        textPaint.setTextSize(size * 0.4f);
        textPaint.setTextAlign(android.graphics.Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);

        android.graphics.Rect bounds = new android.graphics.Rect();
        textPaint.getTextBounds(initials, 0, initials.length(), bounds);
        float yPos = (size / 2f) + (bounds.height() / 2f) - bounds.bottom;

        canvas.drawText(initials, size / 2f, yPos, textPaint);
        return bitmap;
    }

    public interface OnVisitorActionListener {
        void onCheckInClick(VisitorsItem visitor, int position);

        void onCheckOutClick(VisitorsItem visitor, int position);

        void onPrintClick(VisitorsItem visitor, int position);
    }

    public class VisitorViewHolder extends RecyclerView.ViewHolder {
        TextView visName, visWhomToMeet, visContact, visEmail, visCheckInDate, tvProfileInitials;
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
            tvProfileInitials = itemView.findViewById(R.id.tvProfileInitials);
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
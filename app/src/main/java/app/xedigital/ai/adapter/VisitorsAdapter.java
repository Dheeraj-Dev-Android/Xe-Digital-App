package app.xedigital.ai.adapter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

import app.xedigital.ai.R;
import app.xedigital.ai.model.vms.VisitorsItem;

public class VisitorsAdapter extends RecyclerView.Adapter<VisitorsAdapter.ViewHolder> {

    // Palette used for the initials-circle background color (deterministic per user).
    private static final int[] INITIALS_COLOR_PALETTE = new int[]{
            Color.rgb(51, 206, 255),
            Color.rgb(125, 206, 160),
            Color.rgb(255, 165, 0),
            Color.rgb(199, 0, 57),
            Color.rgb(161, 16, 76),
            Color.rgb(102, 0, 102),
            Color.rgb(16, 89, 178),
            Color.rgb(76, 175, 80),
            Color.rgb(255, 87, 82),
            Color.rgb(255, 183, 77),
            Color.rgb(159, 168, 218),
            Color.rgb(38, 166, 154)
    };

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

        // ---- INITIALS-BASED PROFILE IMAGE ----
        String profileImagePath = visitor.getProfileImagePath();
        String visitorName = visitor.getName() != null ? visitor.getName().trim() : "";
        Drawable initialsDrawable = getInitialsDrawable(visitorName);

        if (profileImagePath != null
                && !profileImagePath.trim().isEmpty()
                && !"null".equalsIgnoreCase(profileImagePath.trim())) {
            Glide.with(context)
                    .load(profileImagePath)
                    .placeholder(initialsDrawable)
                    .error(initialsDrawable)
                    .circleCrop()
                    .into(holder.ivVisitorProfile);
        } else {
            holder.ivVisitorProfile.setImageDrawable(initialsDrawable);
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

    /**
     * Builds a self-scaling Drawable that renders a colored circle with the
     * visitor's initials in white. The full name is split by space to extract
     * up to 2 initials (e.g. "John Doe" → "JD", "John" → "J").
     */
    private Drawable getInitialsDrawable(String fullName) {
        final String initials = getInitials(fullName);
        final int bgColor = getInitialsColor(fullName);

        return new Drawable() {
            private final Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            private final Rect textBounds = new Rect();

            {
                bgPaint.setColor(bgColor);
                bgPaint.setStyle(Paint.Style.FILL);

                textPaint.setColor(Color.WHITE);
                textPaint.setTextAlign(Paint.Align.CENTER);
                textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            }

            @Override
            public void draw(@NonNull Canvas canvas) {
                Rect b = getBounds();
                if (b.width() <= 0 || b.height() <= 0) return;

                float cx = b.exactCenterX();
                float cy = b.exactCenterY();
                float radius = Math.min(b.width(), b.height()) / 2f;

                canvas.drawCircle(cx, cy, radius, bgPaint);

                textPaint.setTextSize(radius * 0.8f);
                textPaint.getTextBounds(initials, 0, initials.length(), textBounds);
                float textY = cy - textBounds.exactCenterY();
                canvas.drawText(initials, cx, textY, textPaint);
            }

            @Override
            public void setAlpha(int alpha) {
                bgPaint.setAlpha(alpha);
                textPaint.setAlpha(alpha);
                invalidateSelf();
            }

            @Override
            public void setColorFilter(@Nullable ColorFilter colorFilter) {
                bgPaint.setColorFilter(colorFilter);
                textPaint.setColorFilter(colorFilter);
                invalidateSelf();
            }

            @Override
            public int getOpacity() {
                return PixelFormat.TRANSLUCENT;
            }
        };
    }

    // =========================================================================
    //  INITIALS AVATAR HELPERS
    // =========================================================================

    /**
     * Extracts up to 2 uppercase initials from a full name string.
     * Splits by space: "John Doe" → "JD", "John" → "J", null/empty → "?".
     */
    private String getInitials(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "?";
        }

        String[] parts = fullName.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();

        if (parts.length >= 1 && !parts[0].isEmpty()) {
            sb.append(Character.toUpperCase(parts[0].charAt(0)));
        }
        if (parts.length >= 2 && !parts[1].isEmpty()) {
            sb.append(Character.toUpperCase(parts[1].charAt(0)));
        }

        return sb.length() == 0 ? "?" : sb.toString();
    }

    /**
     * Deterministically picks a palette color for the given name so the
     * same visitor always sees the same background color.
     */
    private int getInitialsColor(String fullName) {
        String key = fullName != null ? fullName.trim() : "";
        if (key.isEmpty()) {
            return INITIALS_COLOR_PALETTE[0];
        }
        int hash = key.hashCode();
        int index = Math.abs(hash) % INITIALS_COLOR_PALETTE.length;
        return INITIALS_COLOR_PALETTE[index];
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
package app.xedigital.ai.ui.dashboard;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;

import app.xedigital.ai.R;
import app.xedigital.ai.model.allEmployee.EmployeesItem;
import app.xedigital.ai.utills.DateTimeUtils;

public class BirthdayCarouselAdapter extends RecyclerView.Adapter<BirthdayCarouselAdapter.ViewHolder> {

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

    private final List<EmployeesItem> list;

    public BirthdayCarouselAdapter(List<EmployeesItem> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_birthday_carousel, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EmployeesItem item = list.get(position);

        String firstName = item.getFirstname() != null ? item.getFirstname() : "";
        String lastName = item.getLastname() != null ? item.getLastname() : "";
        String fullName = firstName + " " + lastName;
        holder.tvEmpName.setText(fullName);

        String rawDob = DateTimeUtils.formatToReadableDate(item.getDateOfBirth());

        if (rawDob != null && !rawDob.trim().isEmpty()) {
            try {
                DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);
                LocalDate dob = LocalDate.parse(rawDob, inputFormatter);
                DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd MMM", Locale.ENGLISH);
                holder.tvEmpDOB.setText(dob.format(outputFormatter));
            } catch (DateTimeParseException e) {
                e.printStackTrace();
                holder.tvEmpDOB.setText(rawDob);
            }
        } else {
            holder.tvEmpDOB.setText("N/A");
        }

        if (item.getDepartment() != null) {
            holder.tvEmpDept.setText(item.getDesignation());
        } else {
            holder.tvEmpDept.setText("N/A");
        }

        // ---- INITIALS-BASED PROFILE IMAGE ----
        String profileImageUrl = item.getProfileImageUrl();
        Drawable initialsDrawable = getInitialsDrawable(firstName, lastName);

        if (profileImageUrl != null
                && !profileImageUrl.trim().isEmpty()
                && !"null".equalsIgnoreCase(profileImageUrl.trim())) {
            Glide.with(holder.itemView.getContext())
                    .load(profileImageUrl)
                    .placeholder(initialsDrawable)
                    .error(initialsDrawable)
                    .into(holder.ivEmpProfile);
        } else {
            holder.ivEmpProfile.setImageDrawable(initialsDrawable);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    /**
     * Builds a self-scaling Drawable that renders a colored circle with the
     * user's initials in white. Used as the fallback whenever a profile image
     * is missing OR fails to load.
     */
    private Drawable getInitialsDrawable(String firstName, String lastName) {
        final String initials = getInitials(firstName, lastName);
        final int bgColor = getInitialsColor(firstName, lastName);

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
     * Extracts up to 2 uppercase initials from the given first/last name.
     * Falls back to "?" if nothing usable is available.
     */
    private String getInitials(String firstName, String lastName) {
        String first = firstName != null ? firstName.trim() : "";
        String last = lastName != null ? lastName.trim() : "";

        StringBuilder sb = new StringBuilder();
        if (!first.isEmpty()) {
            sb.append(Character.toUpperCase(first.charAt(0)));
        }
        if (!last.isEmpty()) {
            sb.append(Character.toUpperCase(last.charAt(0)));
        }

        if (sb.length() == 0) {
            return "?";
        }
        return sb.toString();
    }

    /**
     * Deterministically picks a palette color for the given name so the
     * same user always sees the same background color.
     */
    private int getInitialsColor(String firstName, String lastName) {
        String key = ((firstName != null ? firstName : "") + "|" + (lastName != null ? lastName : "")).trim();
        if (key.isEmpty() || "|".equals(key)) {
            return INITIALS_COLOR_PALETTE[0];
        }
        int hash = key.hashCode();
        int index = Math.abs(hash) % INITIALS_COLOR_PALETTE.length;
        return INITIALS_COLOR_PALETTE[index];
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView ivEmpProfile;
        TextView tvEmpName, tvEmpDOB, tvEmpDept, tvEmpEmail;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivEmpProfile = itemView.findViewById(R.id.ivEmpProfile);
            tvEmpName = itemView.findViewById(R.id.tvEmpName);
            tvEmpDOB = itemView.findViewById(R.id.tvEmpDOB);
            tvEmpDept = itemView.findViewById(R.id.tvEmpDept);
        }
    }
}
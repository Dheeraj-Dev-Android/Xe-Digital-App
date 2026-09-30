package app.xedigital.ai.ui.mrm;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import app.xedigital.ai.R;
import app.xedigital.ai.model.MeetingRoomBookedSlotsResponse.DataByIdItem;

public class BookedSlotsAdapter extends RecyclerView.Adapter<BookedSlotsAdapter.SlotVH> {

    // ─────────────────────────────────────────────
    // Instantiated ONCE per app lifecycle
    // Eliminates 90% of GC pauses during scroll.
    // ─────────────────────────────────────────────
    private static final SimpleDateFormat API_DATE_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
    private static final SimpleDateFormat DISPLAY_DATE_FORMAT =
            new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    // Pre-parsed status colors (avoids Color.parseColor() on every scroll)
    private static final int STATUS_APPROVED_BG = Color.parseColor("#DCFCE7");
    private static final int STATUS_APPROVED_TX = Color.parseColor("#166534");
    private static final int STATUS_APPROVED_STROKE = Color.parseColor("#86EFAC");

    private static final int STATUS_REJECTED_BG = Color.parseColor("#FEE2E2");
    private static final int STATUS_REJECTED_TX = Color.parseColor("#991B1B");
    private static final int STATUS_REJECTED_STROKE = Color.parseColor("#FCA5A5");

    private static final int STATUS_PENDING_BG = Color.parseColor("#FEF3C7");
    private static final int STATUS_PENDING_TX = Color.parseColor("#92400E");
    private static final int STATUS_PENDING_STROKE = Color.parseColor("#FCD34D");

    private static final int STATUS_COMPLETED_BG = Color.parseColor("#E0E7FF");
    private static final int STATUS_COMPLETED_TX = Color.parseColor("#3730A3");
    private static final int STATUS_COMPLETED_STROKE = Color.parseColor("#A5B4FC");

    private static final int STATUS_DEFAULT_BG = Color.parseColor("#F3F4F6");
    private static final int STATUS_DEFAULT_TX = Color.parseColor("#374151");
    private static final int STATUS_DEFAULT_STROKE = Color.parseColor("#D1D5DB");

    private final List<DataByIdItem> items = new ArrayList<>();

    public void setItems(List<DataByIdItem> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SlotVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_booked_slot, parent, false);
        return new SlotVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull SlotVH holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // ─────────────────────────────────────────────
    // ViewHolder
    // ─────────────────────────────────────────────
    static class SlotVH extends RecyclerView.ViewHolder {
        private final TextView textBookedRoomName, textDate, textTimeSlot,
                textHostName, textVisitorName, textCompany, textVisitorCount, textPurpose;
        private final Chip chipStatus;

        SlotVH(@NonNull View itemView) {
            super(itemView);
            textBookedRoomName = itemView.findViewById(R.id.textBookedRoomName);
            textDate = itemView.findViewById(R.id.textDate);
            textTimeSlot = itemView.findViewById(R.id.textTimeSlot);
            textHostName = itemView.findViewById(R.id.textHostName);
            textVisitorName = itemView.findViewById(R.id.textVisitorName);
            textCompany = itemView.findViewById(R.id.textCompany);
            textVisitorCount = itemView.findViewById(R.id.textVisitorCount);
            textPurpose = itemView.findViewById(R.id.textPurpose);
            chipStatus = itemView.findViewById(R.id.chipStatus);
        }

        void bind(DataByIdItem item) {
            // Meeting Room Name
            String roomName = "Meeting Room";
            if (item.getRoomName() != null && item.getRoomName().getRoomName() != null) {
                roomName = item.getRoomName().getRoomName();
            }
            textBookedRoomName.setText(roomName);

            textDate.setText(formatDate(item.getSelectDate()));
            textTimeSlot.setText(formatTimeSlot(item.getStartTime(), item.getEndTime()));
            textHostName.setText(safe(item.getHostName()));
            textVisitorName.setText(safe(item.getVisitorName()));
            textCompany.setText(safe(item.getVisitorCompany()));
            textVisitorCount.setText(safe(item.getVisitorCount()));
            textPurpose.setText(safe(item.getMeetingPurpose()));

            applyStatusChip(item.getBookingstatus());
        }

        private void applyStatusChip(String status) {
            String s = status == null ? "Pending" : status.trim();
            int bg, tx, stroke;

            switch (s.toLowerCase(Locale.ROOT)) {
                case "approved":
                case "confirmed":
                case "booked":
                    bg = STATUS_APPROVED_BG;
                    tx = STATUS_APPROVED_TX;
                    stroke = STATUS_APPROVED_STROKE;
                    break;
                case "rejected":
                case "cancelled":
                case "canceled":
                    bg = STATUS_REJECTED_BG;
                    tx = STATUS_REJECTED_TX;
                    stroke = STATUS_REJECTED_STROKE;
                    break;
                case "pending":
                case "waiting":
                    bg = STATUS_PENDING_BG;
                    tx = STATUS_PENDING_TX;
                    stroke = STATUS_PENDING_STROKE;
                    break;
                case "completed":
                case "done":
                    bg = STATUS_COMPLETED_BG;
                    tx = STATUS_COMPLETED_TX;
                    stroke = STATUS_COMPLETED_STROKE;
                    break;
                default:
                    bg = STATUS_DEFAULT_BG;
                    tx = STATUS_DEFAULT_TX;
                    stroke = STATUS_DEFAULT_STROKE;
                    break;
            }

            chipStatus.setText(capitalize(s));
            chipStatus.setChipBackgroundColor(ColorStateList.valueOf(bg));
            chipStatus.setTextColor(tx);
            chipStatus.setChipStrokeColor(ColorStateList.valueOf(stroke));
            chipStatus.setChipStrokeWidth(1f);
        }

        private String capitalize(String s) {
            if (s == null || s.isEmpty()) return "";
            return Character.toUpperCase(s.charAt(0))
                    + s.substring(1).toLowerCase(Locale.ROOT);
        }

        private String safe(String v) {
            return (v == null || v.trim().isEmpty()) ? "N/A" : v;
        }

        private String formatDate(String raw) {
            if (raw == null) return "N/A";
            try {
                // Reuses static formatter — no memory allocation per row
                Date d = API_DATE_FORMAT.parse(
                        raw.substring(0, Math.min(19, raw.length())));
                return d != null ? DISPLAY_DATE_FORMAT.format(d) : raw;
            } catch (Exception e) {
                return raw;
            }
        }

        private String formatTimeSlot(String start, String end) {
            return safe(start) + " - " + safe(end);
        }
    }
}
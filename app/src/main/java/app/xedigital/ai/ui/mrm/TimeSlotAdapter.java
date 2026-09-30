package app.xedigital.ai.ui.mrm;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

import app.xedigital.ai.R;

public class TimeSlotAdapter extends RecyclerView.Adapter<TimeSlotAdapter.SlotVH> {

    private final List<TimeSlot> slots = new ArrayList<>();
    private final OnSlotClickListener listener;

    public TimeSlotAdapter(OnSlotClickListener listener) {
        this.listener = listener;
    }

    public void setSlots(List<TimeSlot> newSlots) {
        slots.clear();
        if (newSlots != null) slots.addAll(newSlots);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SlotVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_time_slot, parent, false);
        return new SlotVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull SlotVH holder, int position) {
        holder.bind(slots.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return slots.size();
    }

    public interface OnSlotClickListener {
        void onSlotClick(TimeSlot slot);
    }

    static class SlotVH extends RecyclerView.ViewHolder {
        private final MaterialCardView card;
        private final TextView time;
        private final TextView status;

        SlotVH(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.slotCard);
            time = itemView.findViewById(R.id.textSlotTime);
            status = itemView.findViewById(R.id.textSlotStatus);
        }

        void bind(TimeSlot slot, OnSlotClickListener listener) {
            time.setText(slot.getDisplay());

            if (slot.isBooked()) {
                // Booked → Red
                card.setCardBackgroundColor(Color.parseColor("#FEE2E2"));
                card.setStrokeColor(Color.parseColor("#FCA5A5"));
                time.setTextColor(Color.parseColor("#991B1B"));
                status.setVisibility(View.VISIBLE);
                status.setText("Booked");
                status.setTextColor(Color.parseColor("#991B1B"));
                card.setClickable(false);
                card.setAlpha(1f);
            } else if (!slot.isEnabled()) {
                // Disabled → Gray
                card.setCardBackgroundColor(Color.parseColor("#F3F4F6"));
                card.setStrokeColor(Color.parseColor("#D1D5DB"));
                time.setTextColor(Color.parseColor("#9CA3AF"));
                status.setVisibility(View.GONE);
                card.setClickable(false);
                card.setAlpha(0.6f);
            } else {
                // Available → White
                card.setCardBackgroundColor(Color.parseColor("#FFFFFF"));
                card.setStrokeColor(Color.parseColor("#E5E7EB"));
                time.setTextColor(Color.parseColor("#1A1A2E"));
                status.setVisibility(View.GONE);
                card.setClickable(true);
                card.setAlpha(1f);
                card.setOnClickListener(v -> {
                    if (listener != null) listener.onSlotClick(slot);
                });
            }
        }
    }
}
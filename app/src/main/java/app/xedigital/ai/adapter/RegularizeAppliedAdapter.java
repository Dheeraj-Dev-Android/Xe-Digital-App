package app.xedigital.ai.adapter;

import static app.xedigital.ai.ui.regularize_attendance.RegularizeViewFragment.ARG_REGULARIZE_APPLIED_ITEM;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.List;

import app.xedigital.ai.R;
import app.xedigital.ai.model.regularizeApplied.AttendanceRegularizeAppliedItem;
import app.xedigital.ai.utills.DateTimeUtils;

public class RegularizeAppliedAdapter extends RecyclerView.Adapter<RegularizeAppliedAdapter.ViewHolder> {

    private List<AttendanceRegularizeAppliedItem> items = new ArrayList<>();

    public RegularizeAppliedAdapter(List<AttendanceRegularizeAppliedItem> items) {
        if (items != null) {
            this.items = items;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.regularize_applied, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AttendanceRegularizeAppliedItem item = items.get(position);
        if (item == null) return;

        String formattedPunchDate = DateTimeUtils.getDayOfWeekAndDate(item.getPunchDate());
        holder.empPunchDate.setText("Punch Date : " + formattedPunchDate);

        String formattedAppliedDate = DateTimeUtils.getDayOfWeekAndDate(item.getAppliedDate());
        holder.appliedDate.setText("Applied Date : " + formattedAppliedDate);

        holder.statusChip.setText(item.getStatus());

        String status = item.getStatus() != null ? item.getStatus().toLowerCase() : "";
        int chipColor;

        if (status.equals("approved")) {
            chipColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.status_approved);
        } else if (status.equals("unapproved")) {
            chipColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.status_pending);
        } else if (status.equals("rejected") || status.equals("cancelled")) {
            chipColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.status_rejected);
        } else {
            chipColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.icon_tint);
        }

        ColorStateList colorStateList = ColorStateList.valueOf(chipColor);
        holder.statusChip.setChipBackgroundColor(colorStateList);

        holder.btnViewRegularize.setOnClickListener(v -> handleNavigation(v, position));
        holder.regularizeAppliedCard.setOnClickListener(v -> handleNavigation(v, position));
    }

    private void handleNavigation(View v, int position) {
        if (position != RecyclerView.NO_POSITION && position < items.size()) {
            AttendanceRegularizeAppliedItem regularizeAppliedItem = items.get(position);
            if (regularizeAppliedItem != null && regularizeAppliedItem.getId() != null) {
                Bundle bundle = new Bundle();
                bundle.putSerializable(ARG_REGULARIZE_APPLIED_ITEM, regularizeAppliedItem);
                Navigation.findNavController(v).navigate(R.id.action_nav_regularizeAppliedFragment_to_nav_regularizeViewFragment, bundle);
            } else {
                Toast.makeText(v.getContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(v.getContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void updateList(List<AttendanceRegularizeAppliedItem> filteredList) {
        if (filteredList == null) {
            this.items = new ArrayList<>();
        } else {
            this.items = filteredList;
        }
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView empPunchDate;
        public TextView appliedDate;
        public Chip statusChip;
        public ShapeableImageView btnViewRegularize;
        public MaterialCardView regularizeAppliedCard;

        public ViewHolder(View itemView) {
            super(itemView);
            empPunchDate = itemView.findViewById(R.id.empPunchDate);
            appliedDate = itemView.findViewById(R.id.appliedDate);
            btnViewRegularize = itemView.findViewById(R.id.btn_viewRegularize);
            statusChip = itemView.findViewById(R.id.statusChip);
            regularizeAppliedCard = itemView.findViewById(R.id.regularizeAppliedCard);
        }
    }
}
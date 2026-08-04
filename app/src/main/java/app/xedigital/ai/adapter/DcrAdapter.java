package app.xedigital.ai.adapter;

import static app.xedigital.ai.ui.timesheet.SelectedTimesheetFragment.ARG_SELECTED_ITEM;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import app.xedigital.ai.R;
import app.xedigital.ai.model.dcrData.EmployeesDcrDataItem;
import app.xedigital.ai.ui.timesheet.DcrFragment;
import app.xedigital.ai.utills.DateTimeUtils;

public class DcrAdapter extends RecyclerView.Adapter<DcrAdapter.DcrViewHolder> {

    private List<EmployeesDcrDataItem> dcrDataList;

    public DcrAdapter(@Nullable List<EmployeesDcrDataItem> dcrDataList, DcrFragment dcrFragment) {
        this.dcrDataList = (dcrDataList != null) ? dcrDataList : new ArrayList<>();
    }

    public void updateData(@Nullable List<EmployeesDcrDataItem> newData) {
        this.dcrDataList = (newData != null) ? newData : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DcrViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.timesheet_view_list, parent, false);
        return new DcrViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull DcrViewHolder holder, int position) {
        if (dcrDataList == null || position < 0 || position >= dcrDataList.size()) {
            return;
        }

        EmployeesDcrDataItem dcrData = dcrDataList.get(position);
        if (dcrData == null) {
            return;
        }

        // Safe binding for Date
        if (holder.dateTextView != null) {
            String rawDate = dcrData.getDcrDate();
            if (rawDate != null && !rawDate.isEmpty()) {
                String formattedDate = DateTimeUtils.getDayOfWeekAndDate(rawDate);
                holder.dateTextView.setText("Date : " + (formattedDate != null ? formattedDate : "N/A"));
            } else {
                holder.dateTextView.setText("Date : N/A");
            }
        }

        // Safe binding for In / Out times
        if (holder.inTimeTextView != null) {
            holder.inTimeTextView.setText(formatTime(dcrData.getInTime()));
        }

        if (holder.outTimeTextView != null) {
            holder.outTimeTextView.setText(formatTime(dcrData.getOutTime()));
        }

        // Click listeners with safety checks
        if (holder.cardView != null) {
            holder.cardView.setOnClickListener(v -> navigateToSelectedTimesheet(v, holder.getAdapterPosition()));
        }

        if (holder.btn_viewTimesheet != null) {
            holder.btn_viewTimesheet.setOnClickListener(v -> navigateToSelectedTimesheet(v, holder.getAdapterPosition()));
        }
    }

    private void navigateToSelectedTimesheet(@NonNull View v, int position) {
        if (position < 0 || dcrDataList == null || position >= dcrDataList.size()) {
            Toast.makeText(v.getContext(), "Invalid position", Toast.LENGTH_SHORT).show();
            return;
        }

        EmployeesDcrDataItem selectedItem = dcrDataList.get(position);
        if (selectedItem == null) {
            Toast.makeText(v.getContext(), "Selected item is null", Toast.LENGTH_SHORT).show();
            return;
        }

        String dcrId = selectedItem.getId();
        if (dcrId != null && !dcrId.trim().isEmpty()) {
            Bundle bundle = new Bundle();
            bundle.putSerializable(ARG_SELECTED_ITEM, selectedItem);
            try {
                Navigation.findNavController(v).navigate(R.id.action_nav_dcr_to_nav_selected_Timesheet, bundle);
            } catch (Exception e) {
                Log.e("DcrAdapter", "Navigation error: " + e.getMessage());
            }
        } else {
            Toast.makeText(v.getContext(), "Selected item or dcrId is null", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return (dcrDataList != null) ? dcrDataList.size() : 0;
    }

    @NonNull
    public String formatTime(@Nullable String timeString) {
        if (timeString == null || timeString.trim().isEmpty() || timeString.equals("1900-01-01T00:00:00.000Z")) {
            return "N/A";
        }

        try {
            OffsetDateTime odt = OffsetDateTime.parse(timeString);
            LocalTime localTime = odt.toLocalTime();
            if (localTime == null) {
                return "N/A";
            }
            DateTimeFormatter outputFormat = DateTimeFormatter.ofPattern("hh:mm a").withLocale(Locale.ROOT);
            return outputFormat.format(localTime);
        } catch (DateTimeParseException e) {
            Log.e("DcrAdapter", "Error parsing time: " + e.getMessage());
            return "N/A";
        }
    }

    public static class DcrViewHolder extends RecyclerView.ViewHolder {
        @Nullable
        public TextView dateTextView;
        @Nullable
        public TextView inTimeTextView;
        @Nullable
        public TextView outTimeTextView;
        @Nullable
        public MaterialButton btn_viewTimesheet;
        @Nullable
        public MaterialCardView cardView;

        public DcrViewHolder(@NonNull View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.timesheetDate);
            inTimeTextView = itemView.findViewById(R.id.punchInView);
            outTimeTextView = itemView.findViewById(R.id.punchOutView);
            btn_viewTimesheet = itemView.findViewById(R.id.btn_viewTimesheet);
            cardView = itemView.findViewById(R.id.dcrCardView);
        }
    }
}
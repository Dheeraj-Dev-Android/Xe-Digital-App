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
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;

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

    public DcrAdapter(List<EmployeesDcrDataItem> dcrDataList, DcrFragment dcrFragment) {
        this.dcrDataList = new ArrayList<>();
    }

    public void updateData(List<EmployeesDcrDataItem> newData) {
        this.dcrDataList = newData;
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
        EmployeesDcrDataItem dcrData = dcrDataList.get(position);
        holder.dateTextView.setText("Date : " + DateTimeUtils.getDayOfWeekAndDate(dcrData.getDcrDate()));

        // Format in and out times using the formatTime method
        String formattedInTime = formatTime(dcrData.getInTime());
        String formattedOutTime = formatTime(dcrData.getOutTime());

        holder.inTimeTextView.setText(formattedInTime);
        holder.outTimeTextView.setText(formattedOutTime);


        holder.btn_viewTimesheet.setOnClickListener(v -> {
            if (position != RecyclerView.NO_POSITION) {
                EmployeesDcrDataItem selectedItem = dcrDataList.get(position);
                String dcrId = selectedItem.getId();
                if (dcrId != null) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(ARG_SELECTED_ITEM, selectedItem);
                    Navigation.findNavController(v).navigate(R.id.action_nav_dcr_to_nav_selected_Timesheet, bundle);
                } else {
                    Toast.makeText(v.getContext(), "Selected item or dcrId is null", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(v.getContext(), "Invalid position", Toast.LENGTH_SHORT).show();
            }

        });
    }

    @Override
    public int getItemCount() {
        if (dcrDataList == null) {
            return 0;
        }
        return dcrDataList.size();
    }

    public String formatTime(String timeString) {
        if (timeString == null || timeString.equals("1900-01-01T00:00:00.000Z")) {
            return "N/A";
        }

        try {
            OffsetDateTime odt = OffsetDateTime.parse(timeString);
            LocalTime localTime = odt.toLocalTime();
            DateTimeFormatter outputFormat = DateTimeFormatter.ofPattern("hh:mm a").withLocale(Locale.ROOT);
            return outputFormat.format(localTime);
        } catch (DateTimeParseException e) {
            Log.e("DcrAdapter", "Error parsing time: " + e.getMessage());
            return "N/A";
        }
    }

    public static class DcrViewHolder extends RecyclerView.ViewHolder {
        public TextView dateTextView;
        public TextView inTimeTextView;
        public TextView outTimeTextView;
        public ShapeableImageView btn_viewTimesheet;

        public DcrViewHolder(@NonNull View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.timesheetDate);
            inTimeTextView = itemView.findViewById(R.id.punchInView);
            outTimeTextView = itemView.findViewById(R.id.punchOutView);
            btn_viewTimesheet = itemView.findViewById(R.id.btn_viewTimesheet);

        }
    }
}
package app.xedigital.ai.ui.teamTimesheet;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
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
import app.xedigital.ai.model.TeamTimesheetResponse.Employee;
import app.xedigital.ai.model.TeamTimesheetResponse.EmployeesDcrDataItem;
import app.xedigital.ai.utills.DateTimeUtils;

public class TimesheetAdapter extends RecyclerView.Adapter<TimesheetAdapter.TimesheetViewHolder> {

    private final OnTimesheetClickListener clickListener;
    private List<EmployeesDcrDataItem> dataset = new ArrayList<>();

    public TimesheetAdapter(OnTimesheetClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void updateData(List<EmployeesDcrDataItem> newData) {
        if (newData != null) {
            this.dataset = newData;
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public TimesheetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_timesheet_short, parent, false);
        return new TimesheetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TimesheetViewHolder holder, int position) {
        EmployeesDcrDataItem item = dataset.get(position);
        if (item == null) return;

        // 1. Employee Fullname processing
        Employee employee = item.getEmployee();
        String firstName = (employee != null && employee.getFirstname() != null) ? employee.getFirstname() : "";
        String lastName = (employee != null && employee.getLastname() != null) ? employee.getLastname() : "";
        String fullName = (firstName + " " + lastName).trim();
        holder.tvEmpName.setText(fullName.isEmpty() ? "Unknown Employee" : fullName);

        // 2. Date conversion via DateTimeUtils
        String rawDate = item.getDcrDate();
        if (rawDate != null) {
            // Converts ISO timestamp into a readable pattern like: "Tue, 09-06-2026"
            holder.tvDate.setText(DateTimeUtils.getDayOfWeekAndDate(rawDate));
        } else {
            holder.tvDate.setText("N/A");
        }

        // 3. Time conversion via DateTimeUtils
        String rawInTime = item.getInTime();
        String rawOutTime = item.getOutTime();

        String formattedIn = (rawInTime != null) ? formatTime(rawInTime) : "N/A";
        String formattedOut = (rawOutTime != null) ? formatTime(rawOutTime) : "N/A";

        holder.tvTimingSummary.setText("In: " + formattedIn + " | Out: " + formattedOut);

        holder.btnViewDetails.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onViewDetailsClick(item);
            }
        });
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

    @Override
    public int getItemCount() {
        return dataset.size();
    }

    public interface OnTimesheetClickListener {
        void onViewDetailsClick(EmployeesDcrDataItem item);
    }

    static class TimesheetViewHolder extends RecyclerView.ViewHolder {
        TextView tvEmpName, tvDate, tvTimingSummary;
        ShapeableImageView btnViewDetails;

        public TimesheetViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEmpName = itemView.findViewById(R.id.tvEmpName);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTimingSummary = itemView.findViewById(R.id.tvTimingSummary);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
        }
    }
}
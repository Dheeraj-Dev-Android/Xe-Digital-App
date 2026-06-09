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

    private List<EmployeesDcrDataItem> dataset = new ArrayList<>();
    private final OnTimesheetClickListener clickListener;

    public void setDataset(List<EmployeesDcrDataItem> newDataset) {
        this.dataset = newDataset != null ? newDataset : new ArrayList<>();
        notifyDataSetChanged();
    }

    public TimesheetAdapter(OnTimesheetClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @Override
    public void onBindViewHolder(@NonNull TimesheetViewHolder holder, int position) {
        EmployeesDcrDataItem item = dataset.get(position);

        Employee employee = item.getEmployee();
        if (employee != null) {
            holder.tvEmpName.setText(employee.getFirstname() + " " + employee.getLastname());
        } else {
            holder.tvEmpName.setText("N/A");
        }

        if (item.getDcrDate() != null) {
            holder.tvDate.setText(DateTimeUtils.getDayOfWeekAndDate(item.getDcrDate()));
        } else {
            holder.tvDate.setText("N/A");
        }

        String formattedIn = formatTime(item.getInTime());
        String formattedOut = formatTime(item.getOutTime());
        holder.tvTimingSummary.setText("Punch In: " + formattedIn + " | Punch Out: " + formattedOut);

        holder.btnViewDetails.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onViewDetailsClick(item);
            }
        });
    }

    @NonNull
    @Override
    public TimesheetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_timesheet_short, parent, false);
        return new TimesheetViewHolder(view);
    }

    public interface OnTimesheetClickListener {
        void onViewDetailsClick(EmployeesDcrDataItem item);
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
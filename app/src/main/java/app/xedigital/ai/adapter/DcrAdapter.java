package app.xedigital.ai.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import app.xedigital.ai.R;
import app.xedigital.ai.model.dcrData.EmployeesDcrDataItem;
import app.xedigital.ai.ui.timesheet.DcrFragment;
import app.xedigital.ai.utills.DateTimeUtils;

import org.jsoup.Jsoup;

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class DcrAdapter extends RecyclerView.Adapter<DcrAdapter.DcrViewHolder> {

    private List<EmployeesDcrDataItem> dcrDataList;

    public DcrAdapter(List<EmployeesDcrDataItem> dcrDataList, DcrFragment dcrFragment) {
//        this.dcrDataList = dcrDataList;
        this.dcrDataList = new ArrayList<>();
    }

    public void updateData(List<EmployeesDcrDataItem> newData) {
        this.dcrDataList = newData;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DcrViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.dcr_list, parent, false);
        return new DcrViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull DcrViewHolder holder, int position) {
        EmployeesDcrDataItem dcrData = dcrDataList.get(position);
//        holder.employeeNameTextView.setText("Name : ");
//        holder.employeeNameTextView.setText("Name : " + dcrData.getEmployee().getFirstname());

//        holder.dateTextView.setText("Date : ");
        holder.dateTextView.setText("Date : " + DateTimeUtils.getDayOfWeekAndDate(dcrData.getDcrDate()));

        // Format in and out times using the formatTime method
        String formattedInTime = formatTime(dcrData.getInTime());
        String formattedOutTime = formatTime(dcrData.getOutTime());

        String htmlToday = dcrData.getTodayReport();
        String htmlOutcome = dcrData.getOutcome();
        String htmlNextDay = dcrData.getTommarowPlan();

        String todayReport = Jsoup.parse(htmlToday).text();
        String dcrOutcome = Jsoup.parse(htmlOutcome).text();
        String nextDay = Jsoup.parse(htmlNextDay).text();

//        holder.inTimeTextView.setText("Punch In Time : ");
        holder.inTimeTextView.setText("In Time : " + formattedInTime);

//        holder.outTimeTextView.setText("Punch Out Time : ");
        holder.outTimeTextView.setText("Out Time : " + formattedOutTime);

        holder.reportTextView.setText("Today's Report : ");
        holder.reportTextValue.setText(todayReport);

        holder.reportOutcomeTextView.setText("Outcome Of The Day : ");
        holder.reportOutcomeTextValue.setText(dcrOutcome);

        holder.nexDayTextView.setText("Next Day Plan : ");
        holder.nexDayTextValue.setText(nextDay);
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
        public TextView employeeNameTextView;
        public TextView employeeNameTextValue;
        public TextView dateTextView;
        public TextView dateTextValue;
        public TextView inTimeTextView;
        public TextView inTimeTextValue;
        public TextView outTimeTextView;
        public TextView outTimeTextValue;
        public TextView reportTextView;
        public TextView reportTextValue;
        public TextView reportOutcomeTextView;
        public TextView reportOutcomeTextValue;
        public TextView nexDayTextView;
        public TextView nexDayTextValue;

        public DcrViewHolder(@NonNull View itemView) {
            super(itemView);
//            employeeNameTextView = itemView.findViewById(R.id.employeeNameTextView);
//            employeeNameTextValue = itemView.findViewById(R.id.employeeNameTextValue);
            dateTextView = itemView.findViewById(R.id.dateTextView);
//            dateTextValue = itemView.findViewById(R.id.dateTextValue);
            inTimeTextView = itemView.findViewById(R.id.inTimeTextView);
//            inTimeTextValue = itemView.findViewById(R.id.inTimeTextValue);
            outTimeTextView = itemView.findViewById(R.id.outTimeTextView);
//            outTimeTextValue = itemView.findViewById(R.id.outTimeTextValue);
            reportTextView = itemView.findViewById(R.id.reportTextView);
            reportTextValue = itemView.findViewById(R.id.reportTextValue);
            reportOutcomeTextView = itemView.findViewById(R.id.reportOutcomeTextView);
            reportOutcomeTextValue = itemView.findViewById(R.id.reportOutcomeTextValue);
            nexDayTextView = itemView.findViewById(R.id.nexDayTextView);
            nexDayTextValue = itemView.findViewById(R.id.nexDayTextValue);

        }
    }
}
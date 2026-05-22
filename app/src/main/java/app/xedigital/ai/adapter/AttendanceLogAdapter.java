package app.xedigital.ai.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import app.xedigital.ai.R;
import app.xedigital.ai.model.AttendanceLog.AttendanceLogsItem;

public class AttendanceLogAdapter extends RecyclerView.Adapter<AttendanceLogAdapter.LogViewHolder> {

    private final List<AttendanceLogsItem> logList;

    public AttendanceLogAdapter(List<AttendanceLogsItem> logList) {
        this.logList = logList;
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_attendance_log, parent, false);
        return new LogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        AttendanceLogsItem item = logList.get(position);

        // Display formatted time
        holder.tvPunchTime.setText(formatTime(item.getPunchTime()));

        // Display Address
        if (item.getAddress() != null && !item.getAddress().isEmpty()) {
            holder.tvPunchLocation.setText(item.getAddress());
        } else {
            holder.tvPunchLocation.setText("No address recorded");
        }

        // Dynamic numbering or tag layout for punch index sequence
        holder.tvPunchType.setText(String.valueOf(position + 1));
    }

    @Override
    public int getItemCount() {
        return logList != null ? logList.size() : 0;
    }

    // Time Formatter matching your Fragment logic
    private String formatTime(String timeString) {
        if (timeString == null || timeString.isEmpty()) {
            return "N/A";
        }

        SimpleDateFormat inputFormat;
        if (timeString.contains("T")) {
            inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        } else {
            inputFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        }

        SimpleDateFormat outputFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        outputFormat.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));

        try {
            Date date = inputFormat.parse(timeString);
            return outputFormat.format(date);
        } catch (Exception e) {
            return "N/A";
        }
    }

    static class LogViewHolder extends RecyclerView.ViewHolder {
        TextView tvPunchType, tvPunchTime, tvPunchLocation;

        public LogViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPunchType = itemView.findViewById(R.id.tvPunchType);
            tvPunchTime = itemView.findViewById(R.id.tvPunchTime);
            tvPunchLocation = itemView.findViewById(R.id.tvPunchLocation);
        }
    }
}

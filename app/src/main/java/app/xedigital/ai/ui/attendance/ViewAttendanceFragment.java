package app.xedigital.ai.ui.attendance;

import static app.xedigital.ai.ui.regularize_attendance.RegularizeFragment.ARG_ATTENDANCE_ITEM;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import app.xedigital.ai.R;
import app.xedigital.ai.model.attendance.EmployeePunchDataItem;

public class ViewAttendanceFragment extends Fragment {

    private EmployeePunchDataItem attendanceItem;

    public ViewAttendanceFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            attendanceItem = (EmployeePunchDataItem) getArguments().getSerializable(ARG_ATTENDANCE_ITEM);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.view_attendance, container, false);

        if (attendanceItem != null) {
            // Date
            TextView dateTextView = view.findViewById(R.id.dateTextView);
            DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            DateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            try {
                Date date = inputFormat.parse(attendanceItem.getPunchDateFormat());
                if (date != null) {
                    String formattedDate = outputFormat.format(date);
                    dateTextView.setText(formattedDate);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

            // Punch In and Out
            TextView punchInTime = view.findViewById(R.id.punchInTime);
            punchInTime.setText(formatTime(attendanceItem.getPunchIn()));

            TextView punchOutTime = view.findViewById(R.id.punchOutTime);
            punchOutTime.setText(formatTime(attendanceItem.getPunchOut()));

            // Total Time, Overtime, Late
            TextView totalTime = view.findViewById(R.id.totalTime);
            totalTime.setText(attendanceItem.getTotalTime());

            TextView overtimeTime = view.findViewById(R.id.overtimeTime);
            overtimeTime.setText(attendanceItem.getOvertime());

            TextView lateTime = view.findViewById(R.id.lateTime);
            lateTime.setText(attendanceItem.getLateTime());

            TextView shiftTime = view.findViewById(R.id.shiftDetail);
            if (attendanceItem.getShift() != null) {
                String startTime = attendanceItem.getShift().getStartTime();
                String endTime = attendanceItem.getShift().getEndTime();

                if (startTime != null && !startTime.isEmpty() && endTime != null && !endTime.isEmpty()) {
                    String shiftTimeString = startTime + " - " + endTime;
                    shiftTime.setText(formatShiftTime(shiftTimeString));
                    Log.d("ShiftTime", "Shift Time: " + shiftTimeString);
                } else {
                    shiftTime.setText("Shift time not available");
                }
            } else {
                shiftTime.setText("Shift not assigned");
            }
//            Addresses
            TextView addressDetailIn = view.findViewById(R.id.addressDetailIn);
            addressDetailIn.setText(attendanceItem.getPunchInAddress());
            TextView addressDetailOut = view.findViewById(R.id.addressDetailOut);
            addressDetailOut.setText(attendanceItem.getPunchOutAddress());

        }
        return view;
    }

    public String formatTime(String timeString) {
        if (timeString == null || timeString.isEmpty() || timeString.equals("1900-01-01T00:00:00.000Z")) {
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
        } catch (ParseException e) {
            return "N/A";
        }
    }

    private String formatShiftTime(String shiftTime) {
        if (shiftTime == null || shiftTime.isEmpty()) {
            return "";
        }
        String[] parts = shiftTime.split("-");
        if (parts.length == 2) {
            String startTime = formatTime(parts[0].trim());
            String endTime = formatTime(parts[1].trim());
            return startTime + " - " + endTime;
        }
        return "";
    }
}
package app.xedigital.ai.ui.timesheet;

import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

import app.xedigital.ai.R;
import app.xedigital.ai.model.dcrData.EmployeesDcrDataItem;
import app.xedigital.ai.utills.DateTimeUtils;

public class SelectedTimesheetFragment extends Fragment {
    public static final String ARG_SELECTED_ITEM = "selected_dcr_item";

    private EmployeesDcrDataItem selectedDcrItem;

    public SelectedTimesheetFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            selectedDcrItem = (EmployeesDcrDataItem) getArguments().getSerializable(ARG_SELECTED_ITEM);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dcr_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Find views using findViewById
        TextView dateTextView = view.findViewById(R.id.dateTextView);
        TextView inTimeTextView = view.findViewById(R.id.inTimeTextView);
        TextView outTimeTextView = view.findViewById(R.id.outTimeTextView);
        TextView reportTextValue = view.findViewById(R.id.reportTextValue);
        TextView reportOutcomeTextValue = view.findViewById(R.id.reportOutcomeTextValue);
        TextView nextDayTextValue = view.findViewById(R.id.nextDayTextValue);

        // Populate views with data from selectedDcrItem
        dateTextView.setText("Date : " + DateTimeUtils.getDayOfWeekAndDate(selectedDcrItem.getDcrDate()));
        inTimeTextView.setText(formatTime(selectedDcrItem.getInTime()));
        outTimeTextView.setText(formatTime(selectedDcrItem.getOutTime()));
//        reportTextValue.setText(selectedDcrItem.getTodayReport());
//        reportOutcomeTextValue.setText(selectedDcrItem.getOutcome());
//        nextDayTextValue.setText(selectedDcrItem.getTommarowPlan());

        reportTextValue.setText(Html.fromHtml(selectedDcrItem.getTodayReport(), Html.FROM_HTML_MODE_LEGACY));
        reportOutcomeTextValue.setText(Html.fromHtml(selectedDcrItem.getOutcome(), Html.FROM_HTML_MODE_LEGACY));
        nextDayTextValue.setText(Html.fromHtml(selectedDcrItem.getTommarowPlan(), Html.FROM_HTML_MODE_LEGACY));
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
}
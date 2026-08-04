package app.xedigital.ai.ui.timesheet;

import android.os.Bundle;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.style.BulletSpan;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
        TextView feelingOfTheday = view.findViewById(R.id.feelingOfDayTextValue);

        if (selectedDcrItem == null) {
            dateTextView.setText("N/A");
            inTimeTextView.setText("N/A");
            outTimeTextView.setText("N/A");
            reportTextValue.setText("N/A");
            reportOutcomeTextValue.setText("N/A");
            nextDayTextValue.setText("N/A");
            feelingOfTheday.setText("N/A");
            return;
        }

        // Date
        String rawDate = selectedDcrItem.getDcrDate();
        String formattedDate = (rawDate != null && !rawDate.trim().isEmpty())
                ? DateTimeUtils.getDayOfWeekAndDate(rawDate)
                : null;
        dateTextView.setText(getValidText(formattedDate));

        // Times
        inTimeTextView.setText(formatTime(selectedDcrItem.getInTime()));
        outTimeTextView.setText(formatTime(selectedDcrItem.getOutTime()));

        // HTML Fields without list bullet dots
        reportTextValue.setText(formatHtmlWithoutBullets(selectedDcrItem.getTodayReport()));
        reportOutcomeTextValue.setText(formatHtmlWithoutBullets(selectedDcrItem.getOutcome()));
        nextDayTextValue.setText(formatHtmlWithoutBullets(selectedDcrItem.getTommarowPlan()));
        feelingOfTheday.setText(formatHtmlWithoutBullets(selectedDcrItem.getTodayFeeling()));
    }

    public String formatTime(String timeString) {
        if (timeString == null || timeString.trim().isEmpty() || timeString.equals("1900-01-01T00:00:00.000Z")) {
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

    private String getValidText(String value) {
        return (value == null || value.trim().isEmpty()) ? "N/A" : value.trim();
    }

    private CharSequence formatHtmlWithoutBullets(String value) {
        String safeText = getValidText(value);
        if ("N/A".equals(safeText)) {
            return "N/A";
        }

        // Parse HTML to Spanned
        SpannableStringBuilder spannable = new SpannableStringBuilder(
                Html.fromHtml(safeText, Html.FROM_HTML_MODE_LEGACY)
        );

        // Find and remove all BulletSpans added by Android's HTML parser
        BulletSpan[] bulletSpans = spannable.getSpans(0, spannable.length(), BulletSpan.class);
        for (BulletSpan span : bulletSpans) {
            spannable.removeSpan(span);
        }

        // Trim trailing newlines often left by Html.fromHtml
        while (spannable.length() > 0 && spannable.charAt(spannable.length() - 1) == '\n') {
            spannable.delete(spannable.length() - 1, spannable.length());
        }

        return spannable;
    }
}
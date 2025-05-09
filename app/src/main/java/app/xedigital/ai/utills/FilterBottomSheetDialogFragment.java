package app.xedigital.ai.utills;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import app.xedigital.ai.R;
import app.xedigital.ai.ui.leaves.ApproveLeaveFragment;
import app.xedigital.ai.ui.leaves.FilterLeaveApprovalListener;
import app.xedigital.ai.ui.timesheet.FilterAppliedListener;
import app.xedigital.ai.ui.timesheet.TimesheetViewModel;


public class FilterBottomSheetDialogFragment extends BottomSheetDialogFragment {
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String PREFS_NAME = "MyPrefs";
    private static final String AUTH_TOKEN_KEY = "authToken";

    private TextInputLayout startDateLayout;
    private TextInputLayout endDateLayout;
    private TextInputEditText startDateEditText;
    private TextInputEditText endDateEditText;
    private MaterialButton applyFilterButton;
    private MaterialButton resetButton;
    private FilterAppliedListener filterAppliedListener;
    private FilterLeaveApprovalListener filterLeaveApprovalListener;
    private TimesheetViewModel timesheetViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.ThemeOverlay_App_BottomSheetDialog);
        timesheetViewModel = new ViewModelProvider(requireActivity()).get(TimesheetViewModel.class);
        initializeAuthToken();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_filter, container, false);
        initializeViews(view);
        setupClickListeners();
        return view;
    }

    private void initializeViews(View view) {
        startDateLayout = view.findViewById(R.id.startDateLayout);
        endDateLayout = view.findViewById(R.id.endDateLayout);
        startDateEditText = view.findViewById(R.id.startDateEditText);
        endDateEditText = view.findViewById(R.id.endDateEditText);
        applyFilterButton = view.findViewById(R.id.applyFilterButton);
        resetButton = view.findViewById(R.id.resetButton);
    }

    private void initializeAuthToken() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String authToken = sharedPreferences.getString(AUTH_TOKEN_KEY, null);
        timesheetViewModel.storeLoginData(authToken);
    }

    private void setupClickListeners() {
        startDateEditText.setOnClickListener(v -> showDatePickerDialog(startDateEditText, true));
        endDateEditText.setOnClickListener(v -> showDatePickerDialog(endDateEditText, false));
        applyFilterButton.setOnClickListener(v -> applyFilter());
        resetButton.setOnClickListener(v -> resetFilters());
    }

    private void showDatePickerDialog(final TextInputEditText editText, boolean isStartDate) {
        MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker().setTitleText(getString(isStartDate ? R.string.select_start_date : R.string.select_end_date));

        CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder().setValidator(DateValidatorPointBackward.now());
        builder.setCalendarConstraints(constraintsBuilder.build());

        MaterialDatePicker<Long> datePicker = builder.build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
            String formattedDate = sdf.format(new Date(selection));
            editText.setText(formattedDate);

            // Clear errors when date is selected
            if (isStartDate) {
                startDateLayout.setError(null);
            } else {
                endDateLayout.setError(null);
            }
        });

        datePicker.show(getParentFragmentManager(), "DATE_PICKER");
    }

    private void applyFilter() {
        String startDate = Objects.requireNonNull(startDateEditText.getText()).toString().trim();
        String endDate = Objects.requireNonNull(endDateEditText.getText()).toString().trim();

        if (!validateDates(startDate, endDate)) {
            return;
        }

        if (filterAppliedListener != null) {
            filterAppliedListener.onFilterApplied(startDate, endDate);
            dismiss();
        }
        if (filterLeaveApprovalListener != null) {
            filterLeaveApprovalListener.onFilterApplied(startDate, endDate);
            dismiss();
        }
    }

    private boolean validateDates(String startDate, String endDate) {
        // Check if dates are empty
        if (startDate.isEmpty() || endDate.isEmpty()) {
            if (startDate.isEmpty()) {
                startDateLayout.setError(getString(R.string.error_start_date_required));
            }
            if (endDate.isEmpty()) {
                endDateLayout.setError(getString(R.string.error_end_date_required));
            }
            return false;
        }

        // Validate date range
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT, Locale.getDefault());
            LocalDate startDateObj = LocalDate.parse(startDate, formatter);
            LocalDate endDateObj = LocalDate.parse(endDate, formatter);

            if (startDateObj.isAfter(endDateObj)) {
                endDateLayout.setError(getString(R.string.error_invalid_date_range));
                return false;
            }

            return true;
        } catch (DateTimeParseException e) {
            Toast.makeText(requireContext(), R.string.error_invalid_date_format, Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private void resetFilters() {
        startDateEditText.setText("");
        endDateEditText.setText("");
        startDateLayout.setError(null);
        endDateLayout.setError(null);
    }

    public void setFilterAppliedListener(FilterAppliedListener listener) {
        this.filterAppliedListener = listener;
    }

    public void setFilterLeaveApprovalListener(ApproveLeaveFragment listener) {
        this.filterLeaveApprovalListener = listener;
    }
}


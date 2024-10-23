package app.xedigital.ai.utills;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import app.xedigital.ai.R;
import app.xedigital.ai.ui.timesheet.FilterAppliedListener;
import app.xedigital.ai.ui.timesheet.TimesheetViewModel;

public class FilterBottomSheetDialogFragment extends BottomSheetDialogFragment {

    private TextInputEditText startDateEditText;
    private TextInputEditText endDateEditText;
    private FilterAppliedListener filterAppliedListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_filter, container, false);
        setStyle(STYLE_NORMAL, R.style.ThemeOverlay_App_BottomSheetDialog);

        startDateEditText = view.findViewById(R.id.startDateEditText);
        endDateEditText = view.findViewById(R.id.endDateEditText);
        Button applyFilterButton = view.findViewById(R.id.applyFilterButton);
//        Calendar calendar = Calendar.getInstance();

        startDateEditText.setOnClickListener(v -> showDatePickerDialog(startDateEditText));
        endDateEditText.setOnClickListener(v -> showDatePickerDialog(endDateEditText));

        TimesheetViewModel timesheetViewModel = new ViewModelProvider(requireActivity()).get(TimesheetViewModel.class);
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String authToken = sharedPreferences.getString("authToken", null);
        timesheetViewModel.storeLoginData(authToken);

        applyFilterButton.setOnClickListener(v -> {
            String startDate = Objects.requireNonNull(startDateEditText.getText()).toString();
            Log.d("FilterBottomSheetDialogFragment", "Start Date: " + startDate);
            String endDate = Objects.requireNonNull(endDateEditText.getText()).toString();
            Log.d("FilterBottomSheetDialogFragment", "End Date: " + endDate);

            if (startDate.isEmpty() || endDate.isEmpty()) {
                Toast.makeText(requireContext(), "Please select start and end date", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isValidDateRange(startDate, endDate)) {
                if (filterAppliedListener != null) {
                    filterAppliedListener.onFilterApplied(startDate, endDate);
                }
                dismiss();
            } else {
                Toast.makeText(requireContext(), "Invalid date range. Start date should be before end date.", Toast.LENGTH_SHORT).show();
            }
        });
        return view;
    }

    private boolean isValidDateRange(String startDate, String endDate) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault());
            LocalDate startDateObj = LocalDate.parse(startDate, formatter);
            LocalDate endDateObj = LocalDate.parse(endDate, formatter);

            return !startDateObj.isAfter(endDateObj);

        } catch (DateTimeParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void showDatePickerDialog(final TextInputEditText editText) {
        MaterialDatePicker.Builder<Long> materialDateBuilder = MaterialDatePicker.Builder.datePicker();
        materialDateBuilder.setTitleText("SELECT A DATE");
        final MaterialDatePicker<Long> materialDatePicker = materialDateBuilder.build();
        materialDatePicker.show(getParentFragmentManager(), "MATERIAL_DATE_PICKER");
        materialDatePicker.addOnPositiveButtonClickListener(selection -> {
            // Format the selected date to "yyyy-MM-dd"
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String formattedDate = sdf.format(new Date(selection));
            editText.setText(formattedDate);
        });
        materialDatePicker.addOnNegativeButtonClickListener(v -> materialDatePicker.dismiss());
        materialDatePicker.addOnCancelListener(dialogInterface -> materialDatePicker.dismiss());
    }
    public void setFilterAppliedListener(FilterAppliedListener listener) {
        this.filterAppliedListener = listener;
    }
}
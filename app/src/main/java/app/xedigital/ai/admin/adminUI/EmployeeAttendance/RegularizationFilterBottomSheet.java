package app.xedigital.ai.admin.adminUI.EmployeeAttendance;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import app.xedigital.ai.R;

public class RegularizationFilterBottomSheet extends BottomSheetDialogFragment {

    private final SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private final SimpleDateFormat apiFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private TextView tvStartDate, tvEndDate;
    private Button btnApply, btnReset;

    private FilterListener listener;
    private String selectedStartDateApi = "";
    private String selectedEndDateApi = "";

    public void setFilterListener(FilterListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottomsheet_regularization_filter, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvStartDate = view.findViewById(R.id.tvStartDate);
        tvEndDate = view.findViewById(R.id.tvEndDate);
        btnApply = view.findViewById(R.id.btnApply);
        btnReset = view.findViewById(R.id.btnReset);

        setupDatePickers();
        setupButtons();
    }

    private void setupDatePickers() {
        tvStartDate.setOnClickListener(v -> showDatePicker(true));
        tvEndDate.setOnClickListener(v -> showDatePicker(false));
    }

    private void showDatePicker(boolean isStart) {
        Calendar cal = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(requireContext(),
                (picker, year, month, day) -> {
                    cal.set(year, month, day);
                    String display = displayFormat.format(cal.getTime());
                    String api = apiFormat.format(cal.getTime());
                    if (isStart) {
                        tvStartDate.setText(display);
                        selectedStartDateApi = api;
                    } else {
                        tvEndDate.setText(display);
                        selectedEndDateApi = api;
                    }
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void setupButtons() {
        btnApply.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFilterApplied(selectedStartDateApi, selectedEndDateApi);
            }
            dismiss();
        });

        btnReset.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFilterReset();
            }
            dismiss();
        });
    }

    public interface FilterListener {
        void onFilterApplied(String startDate, String endDate);

        void onFilterReset();
    }
}
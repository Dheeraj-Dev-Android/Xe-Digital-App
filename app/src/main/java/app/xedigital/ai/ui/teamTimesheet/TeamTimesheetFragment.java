package app.xedigital.ai.ui.teamTimesheet;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import app.xedigital.ai.R;
import app.xedigital.ai.model.TeamTimesheetResponse.Employee;
import app.xedigital.ai.model.TeamTimesheetResponse.EmployeesDcrDataItem;
import app.xedigital.ai.utills.DateTimeUtils;

public class TeamTimesheetFragment extends Fragment implements TimesheetAdapter.OnTimesheetClickListener {

    private TeamTimesheetViewModel mViewModel;
    private RecyclerView timesheetRecyclerView;
    private ProgressBar loadingProgressBar;
    private LinearLayout emptyStateContainer;
    private TextView emptyStateText;
    private TimesheetAdapter adapter;

    private final List<String> uniqueEmployeeNamesList = new ArrayList<>();
    private LinearLayout layoutEmployeePicker;
    private TextView tvSelectedEmployee;
    private TextInputEditText etFromDate;
    private TextInputEditText etToDate;
    private MaterialButton btnResetFilters;
    private String activeSelectedEmployee = "All Employees";
    private String activeFromDateStr = "";
    private String activeToDateStr = "";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_team_timesheet, container, false);
        if (view != null) {
            timesheetRecyclerView = view.findViewById(R.id.timesheetRecyclerView);
            loadingProgressBar = view.findViewById(R.id.loadingProgressBar);
            emptyStateContainer = view.findViewById(R.id.emptyStateContainer);
            emptyStateText = view.findViewById(R.id.emptyStateText);

            // Custom Layout Layout bindings
            layoutEmployeePicker = view.findViewById(R.id.layoutEmployeePicker);
            tvSelectedEmployee = view.findViewById(R.id.tvSelectedEmployee);
            etFromDate = view.findViewById(R.id.etFromDate);
            etToDate = view.findViewById(R.id.etToDate);
            btnResetFilters = view.findViewById(R.id.btnResetFilters);

            if (timesheetRecyclerView != null) {
                timesheetRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                adapter = new TimesheetAdapter(this);
                timesheetRecyclerView.setAdapter(adapter);
            }
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(TeamTimesheetViewModel.class);

        setupFilterListeners();
        setupObservers();
        loadCredentialsAndFetch();
    }

    private void setupFilterListeners() {
        if (layoutEmployeePicker != null) {
            layoutEmployeePicker.setOnClickListener(v -> showEmployeeSelectionDialog());
        }
        if (etFromDate != null) {
            etFromDate.setOnClickListener(v -> showDatePickerDialog(true));
        }
        if (etToDate != null) {
            etToDate.setOnClickListener(v -> showDatePickerDialog(false));
        }
        if (btnResetFilters != null) {
            btnResetFilters.setOnClickListener(v -> resetAllFilters());
        }
    }

    private void showEmployeeSelectionDialog() {
        if (uniqueEmployeeNamesList.isEmpty()) {
            Toast.makeText(getContext(), "No employee metadata available yet.", Toast.LENGTH_SHORT).show();
            return;
        }

        CharSequence[] items = uniqueEmployeeNamesList.toArray(new CharSequence[0]);
        int checkedItemIndex = uniqueEmployeeNamesList.indexOf(activeSelectedEmployee);

        new AlertDialog.Builder(requireContext()).setTitle("Select Employee").setSingleChoiceItems(items, checkedItemIndex, (dialog, which) -> {
            activeSelectedEmployee = uniqueEmployeeNamesList.get(which);
            if (tvSelectedEmployee != null) {
                tvSelectedEmployee.setText(activeSelectedEmployee);
            }
            mViewModel.applyFilters(activeSelectedEmployee, activeFromDateStr, activeToDateStr);
            dialog.dismiss();
        }).setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss()).show();
    }

    private void showDatePickerDialog(boolean isFromDate) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), (view, selectedYear, selectedMonth, selectedDay) -> {
            String formattedDate = String.format(Locale.US, "%04d-%02d-%02d", selectedYear, (selectedMonth + 1), selectedDay);

            if (isFromDate) {
                activeFromDateStr = formattedDate;
                if (etFromDate != null) etFromDate.setText(formattedDate);
            } else {
                activeToDateStr = formattedDate;
                if (etToDate != null) etToDate.setText(formattedDate);
            }
            mViewModel.applyFilters(activeSelectedEmployee, activeFromDateStr, activeToDateStr);
        }, year, month, day);

        datePickerDialog.show();
    }

    private void resetAllFilters() {
        activeFromDateStr = "";
        activeToDateStr = "";
        activeSelectedEmployee = "All Employees";

        if (etFromDate != null) etFromDate.setText("");
        if (etToDate != null) etToDate.setText("");
        if (tvSelectedEmployee != null) tvSelectedEmployee.setText("All Employees");

        mViewModel.applyFilters(activeSelectedEmployee, activeFromDateStr, activeToDateStr);
    }

    private void setupObservers() {
        if (mViewModel == null) return;

        // NEW: Observe the dedicated employee list under Reporting Manager
        mViewModel.getTeamEmployeeNames().observe(getViewLifecycleOwner(), namesList -> {
            if (namesList != null) {
                uniqueEmployeeNamesList.clear();
                uniqueEmployeeNamesList.addAll(namesList);
            }
        });

        mViewModel.getFilteredDcrData().observe(getViewLifecycleOwner(), itemsList -> {
            if (loadingProgressBar != null) loadingProgressBar.setVisibility(View.GONE);

            if (itemsList == null || itemsList.isEmpty()) {
                adapter.setDataset(new ArrayList<>());
                showEmptyState("No matching logs found for the selected parameters.");
            } else {
                if (emptyStateContainer != null) emptyStateContainer.setVisibility(View.GONE);
                if (timesheetRecyclerView != null)
                    timesheetRecyclerView.setVisibility(View.VISIBLE);
                adapter.setDataset(itemsList);
            }
        });

        mViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMsg -> {
            if (loadingProgressBar != null) loadingProgressBar.setVisibility(View.GONE);
            if (errorMsg != null) {
                showEmptyState(errorMsg);
                Toast.makeText(getContext(), errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCredentialsAndFetch() {
        if (getContext() == null) return;
        SharedPreferences preferences = getContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        if (preferences != null) {
            String token = preferences.getString("authToken", null);
            String userId = preferences.getString("userId", null);
            if (loadingProgressBar != null) loadingProgressBar.setVisibility(View.VISIBLE);
            mViewModel.fetchDcrDataForRM(token, userId);
            mViewModel.fetchEmployeeUnderRM(token, userId);
        } else {
            showEmptyState("Failed to gather runtime environment variables.");
        }
    }

    private void showEmptyState(String message) {
        if (timesheetRecyclerView != null) timesheetRecyclerView.setVisibility(View.GONE);
        if (emptyStateContainer != null) emptyStateContainer.setVisibility(View.VISIBLE);
        if (emptyStateText != null && message != null) emptyStateText.setText(message);
    }

    @Override
    public void onViewDetailsClick(EmployeesDcrDataItem item) {
        if (getContext() == null) return;

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
        View bottomSheetView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_timesheet_detail, null);

        TextView detailTitleName = bottomSheetView.findViewById(R.id.detailTitleName);
        TextView detailDate = bottomSheetView.findViewById(R.id.detailDate);
        TextView detailPunchIn = bottomSheetView.findViewById(R.id.detailPunchIn);
        TextView detailPunchOut = bottomSheetView.findViewById(R.id.detailPunchOut);
        TextView detailTodayFeeling = bottomSheetView.findViewById(R.id.detailTodayFeeling);
        TextView detailTodayReport = bottomSheetView.findViewById(R.id.detailTodayReport);
        TextView detailTomorrowPlan = bottomSheetView.findViewById(R.id.detailTomorrowPlan);
        TextView detailOutcome = bottomSheetView.findViewById(R.id.detailOutcome);

        Employee employee = item.getEmployee();
        if (employee != null) {
            detailTitleName.setText((employee.getFirstname() + " " + employee.getLastname()).trim());
        } else {
            detailTitleName.setText("N/A");
        }

        if (item.getDcrDate() != null) {
            detailDate.setText("Date: " + DateTimeUtils.getDayOfWeekAndDate(item.getDcrDate()));
        } else {
            detailDate.setText("Date: -");
        }

        String formattedIn = formatTime(item.getInTime());
        String formattedOut = formatTime(item.getOutTime());
        detailPunchIn.setText(formattedIn);
        detailPunchOut.setText(formattedOut);

        if (item.getTodayFeeling() != null && !item.getTodayFeeling().trim().isEmpty()) {
            detailTodayFeeling.setText(item.getTodayFeeling());
        } else {
            detailTodayFeeling.setText("N/A");
        }

        if (item.getTodayReport() != null && !item.getTodayReport().trim().isEmpty()) {
            detailTodayReport.setText(HtmlCompat.fromHtml(item.getTodayReport(), HtmlCompat.FROM_HTML_MODE_LEGACY));
        } else {
            detailTodayReport.setText("N/A");
        }

        if (item.getTommarowPlan() != null && !item.getTommarowPlan().trim().isEmpty()) {
            detailTomorrowPlan.setText(HtmlCompat.fromHtml(item.getTommarowPlan(), HtmlCompat.FROM_HTML_MODE_LEGACY));
        } else {
            detailTomorrowPlan.setText("N/A");
        }

        if (item.getOutcome() != null && !item.getOutcome().trim().isEmpty()) {
            detailOutcome.setText(HtmlCompat.fromHtml(item.getOutcome(), HtmlCompat.FROM_HTML_MODE_LEGACY));
        } else {
            detailOutcome.setText("N/A");
        }

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
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
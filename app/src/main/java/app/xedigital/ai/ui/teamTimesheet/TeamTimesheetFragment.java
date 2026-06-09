package app.xedigital.ai.ui.teamTimesheet;

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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_team_timesheet, container, false);
        if (view != null) {
            timesheetRecyclerView = view.findViewById(R.id.timesheetRecyclerView);
            loadingProgressBar = view.findViewById(R.id.loadingProgressBar);
            emptyStateContainer = view.findViewById(R.id.emptyStateContainer);
            emptyStateText = view.findViewById(R.id.emptyStateText);

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
        setupObservers();
        loadCredentialsAndFetch();
    }

    private void setupObservers() {
        if (mViewModel == null) return;

        mViewModel.getDcrDataResponse().observe(getViewLifecycleOwner(), response -> {
            if (loadingProgressBar != null) loadingProgressBar.setVisibility(View.GONE);
            if (response != null && response.getData() != null && response.getData().getEmployeesDcrData() != null) {
                if (response.getData().getEmployeesDcrData().isEmpty()) {
                    showEmptyState("No active timesheet logs found.");
                } else {
                    if (emptyStateContainer != null) emptyStateContainer.setVisibility(View.GONE);
                    if (timesheetRecyclerView != null)
                        timesheetRecyclerView.setVisibility(View.VISIBLE);
                    adapter.updateData(response.getData().getEmployeesDcrData());
                }
            } else {
                showEmptyState("Data fields returned empty from network lookup.");
            }
        });

        mViewModel.getErrorMessage().observe(getViewLifecycleOwner(), err -> {
            if (loadingProgressBar != null) loadingProgressBar.setVisibility(View.GONE);
            showEmptyState(err);
        });

        mViewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
            if (loading != null && loading) {
                if (loadingProgressBar != null) loadingProgressBar.setVisibility(View.VISIBLE);
                if (emptyStateContainer != null) emptyStateContainer.setVisibility(View.GONE);
                if (timesheetRecyclerView != null) timesheetRecyclerView.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onViewDetailsClick(EmployeesDcrDataItem item) {
        if (item == null || getContext() == null) return;

        BottomSheetDialog dialog = new BottomSheetDialog(getContext());
        View sheetView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_timesheet_detail, null);

        if (sheetView != null) {
            TextView detailTitleName = sheetView.findViewById(R.id.detailTitleName);
            TextView detailDate = sheetView.findViewById(R.id.detailDate);
            TextView detailPunchTimings = sheetView.findViewById(R.id.detailPunchTimings);
            TextView detailTodayFeeling = sheetView.findViewById(R.id.detailTodayFeeling);
            TextView detailTodayReport = sheetView.findViewById(R.id.detailTodayReport);
            TextView detailTomorrowPlan = sheetView.findViewById(R.id.detailTomorrowPlan);
            TextView detailOutcome = sheetView.findViewById(R.id.detailOutcome);

            // 1. Employee Name
            Employee emp = item.getEmployee();
            String name = (emp != null) ? (emp.getFirstname() + " " + emp.getLastname()).trim() : "";
            detailTitleName.setText(name.isEmpty() ? "Unknown Employee" : name);

            // 2. Timesheet Submitted Date (Formatted)
            String rawDate = item.getDcrDate();
            detailDate.setText("Submitted Date: " + (rawDate != null ? DateTimeUtils.getDayOfWeekAndDate(rawDate) : "N/A"));

            // 3. Punch In and Punch Out Time (Formatted)
            String rawInTime = item.getInTime();
            String rawOutTime = item.getOutTime();
            String formattedIn = (rawInTime != null) ? formatTime(rawInTime) : "N/A";
            String formattedOut = (rawOutTime != null) ? formatTime(rawOutTime) : "N/A";
            detailPunchTimings.setText("In: " + formattedIn + "  |  Out: " + formattedOut);

            // 4. Feeling of the Day
            String feeling = item.getTodayFeeling();
            detailTodayFeeling.setText(feeling != null && !feeling.trim().isEmpty() ? feeling : "N/A");

            // 5. HTML Content Formatting for Today's Report
            String htmlTodayReport = item.getTodayReport();
            if (htmlTodayReport != null && !htmlTodayReport.trim().isEmpty()) {
                detailTodayReport.setText(HtmlCompat.fromHtml(htmlTodayReport, HtmlCompat.FROM_HTML_MODE_LEGACY).toString().trim());
            } else {
                detailTodayReport.setText("No work report statements provided.");
            }

            // 6. HTML Content Formatting for Tomorrow's Plan
            String htmlTomorrowPlan = item.getTommarowPlan();
            if (htmlTomorrowPlan != null && !htmlTomorrowPlan.trim().isEmpty()) {
                detailTomorrowPlan.setText(HtmlCompat.fromHtml(htmlTomorrowPlan, HtmlCompat.FROM_HTML_MODE_LEGACY).toString().trim());
            } else {
                detailTomorrowPlan.setText("No forward actions submitted.");
            }

            // 7. HTML Content Formatting for Outcome
            String htmlOutcome = item.getOutcome();
            if (htmlOutcome != null && !htmlOutcome.trim().isEmpty()) {
                detailOutcome.setText(HtmlCompat.fromHtml(htmlOutcome, HtmlCompat.FROM_HTML_MODE_LEGACY).toString().trim());
            } else {
                detailOutcome.setText("No outcome logs tracked.");
            }

            dialog.setContentView(sheetView);
            dialog.show();
        }
    }

    private void loadCredentialsAndFetch() {
        if (getContext() == null) return;
        SharedPreferences preferences = getContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        if (preferences != null) {
            String token = preferences.getString("authToken", null);
            String userId = preferences.getString("userId", null);
            mViewModel.fetchDcrDataForRM(token, userId);
        } else {
            showEmptyState("Failed to gather runtime environment variables.");
        }
    }

    private void showEmptyState(String message) {
        if (timesheetRecyclerView != null) timesheetRecyclerView.setVisibility(View.GONE);
        if (emptyStateContainer != null) emptyStateContainer.setVisibility(View.VISIBLE);
        if (emptyStateText != null && message != null) emptyStateText.setText(message);
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
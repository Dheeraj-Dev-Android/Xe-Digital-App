package app.xedigital.ai.ui.AttendanceByManager;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import app.xedigital.ai.R;
import app.xedigital.ai.adapter.AttendanceByManagerAdapter;
import app.xedigital.ai.model.AttandanceByManager.EmployeePunchDataItem;
import app.xedigital.ai.model.TeamUnderManagerResponse.EmployeesItem;
import app.xedigital.ai.utills.DateTimeUtils;

public class AttendanceByManagerFragment extends Fragment {

    private final List<String> employeeNames = new ArrayList<>();
    private final List<String> employeeIds = new ArrayList<>();
    private final Calendar calFrom = Calendar.getInstance();
    private final Calendar calTo = Calendar.getInstance();
    private final SimpleDateFormat apiFmt = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat displayFmt = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
    private AttendanceByManagerAdapter managerAdapter;
    private AttendanceByManagerViewModel mViewModel;
    private TextView tvFromDate, tvToDate, tvSelectedEmployee;
    private View layoutEmployeePicker;
    private SwipeRefreshLayout swipeRefresh;
    private View layoutEmptyState;
    private View layoutLoading;
    private String selectedEmployeeId = null;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_attendance_by_manager, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvFromDate = view.findViewById(R.id.tvFromDate);
        tvToDate = view.findViewById(R.id.tvToDate);
        tvSelectedEmployee = view.findViewById(R.id.tvSelectedEmployee);
        layoutEmployeePicker = view.findViewById(R.id.layoutEmployeePicker);
        Button btnSearch = view.findViewById(R.id.btnSearch);
//        Button btnRetry = view.findViewById(R.id.btnRetry);
        RecyclerView recyclerView = view.findViewById(R.id.rvAttendance);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        layoutEmptyState = view.findViewById(R.id.layoutEmptyState);
        layoutLoading = view.findViewById(R.id.layoutLoading);

        mViewModel = new ViewModelProvider(this).get(AttendanceByManagerViewModel.class);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        managerAdapter = new AttendanceByManagerAdapter(new ArrayList<>());
        recyclerView.setAdapter(managerAdapter);

        swipeRefresh.setColorSchemeColors(
                ContextCompat.getColor(requireContext(), android.R.color.holo_blue_bright),
                ContextCompat.getColor(requireContext(), android.R.color.holo_green_light)
        );
        swipeRefresh.setOnRefreshListener(this::triggerSearch);

        calFrom.add(Calendar.DAY_OF_MONTH, -30);
        updateDateLabels();

        view.findViewById(R.id.layoutFromDate).setOnClickListener(v -> showDatePicker(true));
        view.findViewById(R.id.layoutToDate).setOnClickListener(v -> showDatePicker(false));
        layoutEmployeePicker.setOnClickListener(v -> showEmployeePopupMenu());
        btnSearch.setOnClickListener(v -> {
            showLoading();
            triggerSearch();
        });
//        btnRetry.setOnClickListener(v -> {
//            showLoading();
//            triggerSearch();
//        });

        // Observers
        mViewModel.getTeamMemberData().observe(getViewLifecycleOwner(), response -> {
            if (response != null && response.isSuccess() && response.getData() != null) {
                List<EmployeesItem> employees = response.getData().getEmployees();
                if (employees != null && !employees.isEmpty()) {
                    populateEmployeeLists(employees);
                }
            }
        });

        mViewModel.getAttendanceData().observe(getViewLifecycleOwner(), response -> {
            swipeRefresh.setRefreshing(false);
            if (response != null && response.isSuccess() && response.getData() != null) {
                List<EmployeePunchDataItem> data = response.getData().getEmployeePunchData();
                if (data != null && !data.isEmpty()) {
                    showData(data);
                } else {
                    showEmptyState();
                }
            } else {
                showEmptyState();
            }
        });

        mViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            swipeRefresh.setRefreshing(false);
            showEmptyState();
        });

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("authToken", null);
        String managerId = sharedPreferences.getString("userId", null);

        showLoading();
        mViewModel.fetchEmployeesUnderManager(token, managerId);
        triggerSearch();
    }

    private void populateEmployeeLists(List<EmployeesItem> employees) {
        employeeNames.clear();
        employeeIds.clear();
        employeeNames.add("All Employees");
        employeeIds.add(null);

        for (EmployeesItem emp : employees) {
            if (emp == null) continue;
            String fName = emp.getFirstname() != null ? emp.getFirstname().trim() : "";
            String lName = emp.getLastname() != null ? emp.getLastname().trim() : "";
            String fullName = (fName + " " + lName).trim();
            if (fullName.isEmpty()) fullName = emp.getEmployeeCode();
            employeeNames.add(fullName);
            employeeIds.add(emp.getId());
        }
    }

    private void showData(List<EmployeePunchDataItem> data) {
        layoutLoading.setVisibility(View.GONE);
        layoutEmptyState.setVisibility(View.GONE);
        swipeRefresh.setVisibility(View.VISIBLE);

        // Formatters
        SimpleDateFormat apiFmt = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat displayFmt = new SimpleDateFormat("dd MMM yyyy, EEEE", Locale.getDefault());

        for (EmployeePunchDataItem punchData : data) {
            if (punchData == null) continue;

            boolean hasPunchIn = punchData.getPunchIn() != null && !punchData.getPunchIn().isEmpty();
            boolean hasLeave = punchData.getLeaveName() != null && !punchData.getLeaveName().isEmpty();

            try {
                // 1. Update Date to show Day Name
                Date dateObj = apiFmt.parse(punchData.getPunchDate());
                if (dateObj != null) {
                    punchData.setPunchDateFormat(displayFmt.format(dateObj));

                    // 2. Check for Weekend
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(dateObj);
                    int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
                    boolean isWeekend = (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY);

                    if (isWeekend && !hasPunchIn) {
                        punchData.setLeaveName("Week Off");
                        punchData.setFullDayLeave(true);
                    } else if (!hasPunchIn && !hasLeave) {
                        punchData.setLeaveName("❌ LOP / Absent ");
                        punchData.setFullDayLeave(true);
                    } else punchData.setFullDayLeave(!hasPunchIn && hasLeave);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // 3. Pre-calculate Times (Only for work days)
            if (!punchData.isFullDayLeave()) {
                String total = DateTimeUtils.calculateTotalTime(punchData.getPunchIn(), punchData.getPunchOut());
                punchData.setTotalTime(total);
                punchData.setOvertime(DateTimeUtils.calculateOvertime(total));
                if (punchData.getShift() != null) {
                    punchData.setLateTime(DateTimeUtils.calculateLateTime(punchData.getPunchIn(), punchData.getShift().getStartTime()));
                }
            }
        }
        managerAdapter.updateList(data);
    }

    private void showEmployeePopupMenu() {
        if (employeeNames.isEmpty()) return;
        PopupMenu popup = new PopupMenu(requireContext(), layoutEmployeePicker);
        for (int i = 0; i < employeeNames.size(); i++) {
            popup.getMenu().add(0, i, i, employeeNames.get(i));
        }
        popup.setOnMenuItemClickListener(item -> {
            int index = item.getItemId();
            selectedEmployeeId = employeeIds.get(index);
            tvSelectedEmployee.setText(employeeNames.get(index));
            return true;
        });
        popup.show();
    }

    private void triggerSearch() {
        String startDate = apiFmt.format(calFrom.getTime());
        String endDate = apiFmt.format(calTo.getTime());
        mViewModel.fetchAttendance(startDate, endDate, selectedEmployeeId);
    }

    private void updateDateLabels() {
        tvFromDate.setText(displayFmt.format(calFrom.getTime()));
        tvToDate.setText(displayFmt.format(calTo.getTime()));
    }

    private void showDatePicker(boolean isFrom) {
        Calendar target = isFrom ? calFrom : calTo;
        DatePickerDialog dialog = new DatePickerDialog(requireContext(), (dp, year, month, day) -> {
            target.set(year, month, day);
            updateDateLabels();
        }, target.get(Calendar.YEAR), target.get(Calendar.MONTH), target.get(Calendar.DAY_OF_MONTH));
        if (isFrom) dialog.getDatePicker().setMaxDate(calTo.getTimeInMillis());
        else dialog.getDatePicker().setMinDate(calFrom.getTimeInMillis());
        dialog.show();
    }

    private void showLoading() {
        layoutLoading.setVisibility(View.VISIBLE);
        layoutEmptyState.setVisibility(View.GONE);
        swipeRefresh.setVisibility(View.GONE);
    }

    private void showEmptyState() {
        layoutLoading.setVisibility(View.GONE);
        layoutEmptyState.setVisibility(View.VISIBLE);
        swipeRefresh.setVisibility(View.GONE);
    }
}
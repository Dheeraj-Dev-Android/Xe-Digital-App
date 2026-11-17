package app.xedigital.ai.admin.adminUI.EmployeeAttendance;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import app.xedigital.ai.R;
import app.xedigital.ai.admin.adminModal.EmployeeDetails.EmployeesItem;
import app.xedigital.ai.admin.adminModal.department.DepartmentsItem;
import app.xedigital.ai.admin.adminModal.empAttendance.EmployeePunchDataItem;
import app.xedigital.ai.utills.SecurePrefManager;

public class EmployeeAttendanceFragment extends Fragment implements AttendanceFilterBottomSheet.FilterListener {

    private final List<EmployeePunchDataItem> attendanceList = new ArrayList<>();
    // ── Date formatters ──
    private final SimpleDateFormat apiFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MMM/yyyy", Locale.getDefault());
    // ── Views ──
    private RecyclerView recyclerViewAttendance;
    private ProgressBar progressBar;
    private TextView tvEmpty, tvFilterSummary;
    private ImageButton btnFilter;

    // ── Chips ──
    private Chip chipAddAttendancePending;
    private Chip chipRegularizationPending;
    private Chip chipViewAttendanceLog;

    // ── ViewModel & Adapter ──
    private EmployeeAttendanceViewModel viewModel;
    private EmployeeAttendanceViewAdapter adapter;
    // ── Filter data caches ──
    private List<DepartmentsItem> departmentsList = new ArrayList<>();
    private List<EmployeesItem> employeesList = new ArrayList<>();
    // ── Active filter state ──
    private String currentStartDate = "";
    private String currentEndDate = "";
    private String currentDeptId = "";
    private String currentEmpId = "";

    // ══════════════════════════════════════════════
    //  LIFECYCLE
    // ══════════════════════════════════════════════

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_employee_attendance, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bindViews(view);
        initViewModel();
        setupRecyclerView();
        setupChips();
        observeLiveData();
        setupFilterButton();
        loadInitialData();
    }

    // ══════════════════════════════════════════════
    //  INIT
    // ══════════════════════════════════════════════

    private void bindViews(View view) {
        recyclerViewAttendance = view.findViewById(R.id.recyclerViewAttendance);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        tvFilterSummary = view.findViewById(R.id.tvFilterSummary);
        btnFilter = view.findViewById(R.id.btnFilter);

        chipAddAttendancePending = view.findViewById(R.id.chipAddAttendancePending);
        chipRegularizationPending = view.findViewById(R.id.chipRegularizationPending);
        chipViewAttendanceLog = view.findViewById(R.id.chipViewAttendanceLog);
    }

    private void initViewModel() {
        viewModel = new ViewModelProvider(this).get(EmployeeAttendanceViewModel.class);
    }

    private void setupRecyclerView() {
        adapter = new EmployeeAttendanceViewAdapter(attendanceList);
        recyclerViewAttendance.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerViewAttendance.setAdapter(adapter);
    }

    private void setupChips() {
        chipAddAttendancePending.setOnClickListener(v -> {
            // Action when "Add Attendance Pending Approval" chip is clicked
            Navigation.findNavController(v).navigate(R.id.action_nav_employee_attendance_to_nav_add_attendance_pending_approval);
        });

        chipRegularizationPending.setOnClickListener(v -> {
            // Action when "Regularization Pending Approval" chip is clicked
            Navigation.findNavController(v).navigate(R.id.action_nav_employee_attendance_to_nav_attendance_regularization_approval);


        });

        chipViewAttendanceLog.setOnClickListener(v -> {
            // Action when "View Attendance Log" chip is clicked
        });
    }

    private void setupFilterButton() {
        btnFilter.setOnClickListener(v -> openFilterBottomSheet());
    }

    // ══════════════════════════════════════════════
    //  LIVE DATA OBSERVERS
    // ══════════════════════════════════════════════

    private void observeLiveData() {

        // ── 1. Attendance Data ──
        viewModel.getAttendanceData().observe(getViewLifecycleOwner(), data -> {
            attendanceList.clear();
            if (data != null && !data.isEmpty()) {
                attendanceList.addAll(data);
                tvEmpty.setVisibility(View.GONE);
                recyclerViewAttendance.setVisibility(View.VISIBLE);
            } else {
                tvEmpty.setVisibility(View.VISIBLE);
                recyclerViewAttendance.setVisibility(View.GONE);
            }
            adapter.notifyDataSetChanged();
        });

        // ── 2. Loading State ──
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
            if (Boolean.TRUE.equals(loading)) {
                progressBar.setVisibility(View.VISIBLE);
                recyclerViewAttendance.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.GONE);
            } else {
                progressBar.setVisibility(View.GONE);
            }
        });

        // ── 3. Error Messages ──
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        // ── 4. Departments (for filter bottom sheet) ──
        viewModel.getDepartments().observe(getViewLifecycleOwner(), depts -> {
            if (depts != null) {
                departmentsList = depts;
            }
        });

        // ── 5. Employees (for filter bottom sheet) ──
        viewModel.getEmployees().observe(getViewLifecycleOwner(), emps -> {
            if (emps != null) {
                employeesList = emps;
            }
        });
    }

    // ══════════════════════════════════════════════
    //  DATA LOADING
    // ══════════════════════════════════════════════

    private void loadInitialData() {
        String token = getToken();

        // Default: 1st of current month → today (no future dates)
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        currentStartDate = apiFormat.format(cal.getTime());   // e.g. 2026-11-01

        Calendar today = Calendar.getInstance();
        currentEndDate = apiFormat.format(today.getTime());   // e.g. 2026-11-16

        updateFilterSummary();
        fetchAttendance();

        viewModel.fetchDepartments(token);
        viewModel.fetchEmployees(token);
    }

    private void fetchAttendance() {
        viewModel.fetchAttendance(getToken(), currentStartDate, currentEndDate, "", currentEmpId, currentDeptId, 1, 50, "", "");
    }

    // ══════════════════════════════════════════════
    //  FILTER BOTTOM SHEET
    // ══════════════════════════════════════════════

    private void openFilterBottomSheet() {
        AttendanceFilterBottomSheet sheet = new AttendanceFilterBottomSheet();
        sheet.setDepartments(departmentsList);
        sheet.setEmployees(employeesList);
        sheet.setFilterListener(this);
        sheet.show(getChildFragmentManager(), "AttendanceFilter");
    }

    @Override
    public void onFilterApplied(String startDate, String endDate, String departmentId, String employeeId) {
        currentStartDate = startDate;
        currentEndDate = endDate;
        currentDeptId = departmentId;
        currentEmpId = employeeId;

        updateFilterSummary();
        fetchAttendance();
    }

    @Override
    public void onFilterReset() {
        currentStartDate = "";
        currentEndDate = "";
        currentDeptId = "";
        currentEmpId = "";

        updateFilterSummary();
        fetchAttendance();
    }

    // ══════════════════════════════════════════════
    //  FILTER SUMMARY
    // ══════════════════════════════════════════════

    private void updateFilterSummary() {
        boolean hasDate = !currentStartDate.isEmpty() && !currentEndDate.isEmpty();
        boolean hasDept = !currentDeptId.isEmpty();
        boolean hasEmp = !currentEmpId.isEmpty();

        if (!hasDate && !hasDept && !hasEmp) {
            tvFilterSummary.setText("Showing all attendance");
            return;
        }

        StringBuilder sb = new StringBuilder();

        if (hasDate) {
            try {
                String s = displayFormat.format(apiFormat.parse(currentStartDate));
                String e = displayFormat.format(apiFormat.parse(currentEndDate));
                sb.append(s).append(" – ").append(e);
            } catch (Exception ex) {
                sb.append(currentStartDate).append(" – ").append(currentEndDate);
            }
        }

        if (hasDept) {
            if (sb.length() > 0) sb.append("  |  ");
            sb.append("Dept: ").append(findDeptName(currentDeptId));
        }

        if (hasEmp) {
            if (sb.length() > 0) sb.append("  |  ");
            sb.append("Emp: ").append(findEmpName(currentEmpId));
        }

        tvFilterSummary.setText(sb.toString());
    }

    private String findDeptName(String id) {
        for (DepartmentsItem d : departmentsList) {
            if (id.equals(d.getId())) return d.getName();
        }
        return id;
    }

    private String findEmpName(String id) {
        for (EmployeesItem e : employeesList) {
            if (id.equals(e.getId())) {
                String first = e.getFirstname() != null ? e.getFirstname() : "";
                String last = e.getLastname() != null ? e.getLastname() : "";
                return (first + " " + last).trim();
            }
        }
        return id;
    }

    // ══════════════════════════════════════════════
    //  UTILS
    // ══════════════════════════════════════════════

    private String getToken() {
        return SecurePrefManager.getInstance(requireContext()).getString("authToken", "");
    }
}
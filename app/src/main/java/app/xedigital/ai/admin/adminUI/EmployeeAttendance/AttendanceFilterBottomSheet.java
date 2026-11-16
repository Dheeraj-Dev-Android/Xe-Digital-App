package app.xedigital.ai.admin.adminUI.EmployeeAttendance;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import app.xedigital.ai.R;
import app.xedigital.ai.admin.adminModal.EmployeeDetails.EmployeesItem;
import app.xedigital.ai.admin.adminModal.department.DepartmentsItem;

public class AttendanceFilterBottomSheet extends BottomSheetDialogFragment {

    private TextView tvStartDate, tvEndDate;
    private Spinner spinnerDepartment, spinnerEmployee;
    private Button btnApply, btnReset;

    private List<DepartmentsItem> departments = new ArrayList<>();
    private List<EmployeesItem> employees = new ArrayList<>();
    private FilterListener listener;

    private String selectedStartDateApi = "";
    private String selectedEndDateApi = "";
    private int selectedDeptPosition = 0;
    private int selectedEmpPosition = 0;

    private final SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private final SimpleDateFormat apiFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public interface FilterListener {
        void onFilterApplied(String startDate, String endDate, String departmentId, String employeeId);
        void onFilterReset();
    }

    public void setDepartments(List<DepartmentsItem> departments) {
        this.departments = departments != null ? departments : new ArrayList<>();
    }

    public void setEmployees(List<EmployeesItem> employees) {
        this.employees = employees != null ? employees : new ArrayList<>();
    }

    public void setFilterListener(FilterListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottomsheet_attendance_filter, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvStartDate = view.findViewById(R.id.tvStartDate);
        tvEndDate = view.findViewById(R.id.tvEndDate);
        spinnerDepartment = view.findViewById(R.id.spinnerDepartment);
        spinnerEmployee = view.findViewById(R.id.spinnerEmployee);
        btnApply = view.findViewById(R.id.btnApply);
        btnReset = view.findViewById(R.id.btnReset);

        setupDatePickers();
        setupSpinners();
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

    private void setupSpinners() {
        // Department spinner
        List<String> deptNames = new ArrayList<>();
        deptNames.add("All Departments");
        if (departments != null) {
            for (DepartmentsItem dept : departments) {
                deptNames.add(dept.getName());
            }
        }
        ArrayAdapter<String> deptAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, deptNames);
        deptAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDepartment.setAdapter(deptAdapter);

        // Employee spinner
        List<String> empNames = new ArrayList<>();
        empNames.add("All Employees");
        if (employees != null) {
            for (EmployeesItem emp : employees) {
                String name = "";
                if (emp.getFirstname() != null) name += emp.getFirstname();
                if (emp.getLastname() != null) name += " " + emp.getLastname();
                if (emp.getEmployeeCode() != null) name += " (" + emp.getEmployeeCode() + ")";
                empNames.add(name.trim());
            }
        }
        ArrayAdapter<String> empAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, empNames);
        empAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEmployee.setAdapter(empAdapter);
    }

    private void setupButtons() {
        btnApply.setOnClickListener(v -> {
            if (listener != null) {
                String deptId = "";
                int deptPos = spinnerDepartment.getSelectedItemPosition();
                if (deptPos > 0 && departments != null && deptPos <= departments.size()) {
                    deptId = departments.get(deptPos - 1).getId();
                }

                String empId = "";
                int empPos = spinnerEmployee.getSelectedItemPosition();
                if (empPos > 0 && employees != null && empPos <= employees.size()) {
                    empId = employees.get(empPos - 1).getId();
                }

                listener.onFilterApplied(selectedStartDateApi, selectedEndDateApi, deptId, empId);
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
}
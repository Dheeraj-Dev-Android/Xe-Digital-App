package app.xedigital.ai.admin.adminUI.EmployeeAttendance;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Filter;
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

    private final SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private final SimpleDateFormat apiFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final List<EmployeesItem> activeEmployees = new ArrayList<>();
    private TextView tvStartDate, tvEndDate;
    private Spinner spinnerDepartment;
    private AutoCompleteTextView spinnerEmployee;
    private Button btnApply, btnReset;
    private List<DepartmentsItem> departments = new ArrayList<>();
    private FilterListener listener;
    private String selectedStartDateApi = "";
    private String selectedEndDateApi = "";
    private String selectedEmpId = "";

    public void setDepartments(List<DepartmentsItem> departments) {
        this.departments = departments != null ? departments : new ArrayList<>();
    }

    public void setEmployees(List<EmployeesItem> employees) {
        activeEmployees.clear();
        if (employees != null) {
            for (EmployeesItem emp : employees) {
                // Filter only active employees ("active": true)
                if (emp.isActive()) {
                    activeEmployees.add(emp);
                }
            }
        }
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

        spinnerEmployee = view.findViewById(R.id.spinnerEmployee);
        spinnerDepartment = view.findViewById(R.id.spinnerDepartment);
        tvStartDate = view.findViewById(R.id.tvStartDate);
        tvEndDate = view.findViewById(R.id.tvEndDate);
        btnApply = view.findViewById(R.id.btnApply);
        btnReset = view.findViewById(R.id.btnReset);

        setupDatePickers();
        setupDepartmentSpinner();
        setupEmployeeAutoComplete();
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

    private void setupDepartmentSpinner() {
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
    }

    private void setupEmployeeAutoComplete() {
        EmployeeSearchAdapter adapter = new EmployeeSearchAdapter(requireContext(), activeEmployees);
        spinnerEmployee.setAdapter(adapter);

        // Show dropdown on touch/focus
        spinnerEmployee.setOnTouchListener((v, event) -> {
            spinnerEmployee.showDropDown();
            return false;
        });

        // Capture item selection directly from adapter
        spinnerEmployee.setOnItemClickListener((parent, view, position, id) -> {
            Object selectedObject = parent.getItemAtPosition(position);
            if (selectedObject instanceof EmployeesItem) {
                EmployeesItem emp = (EmployeesItem) selectedObject;
                selectedEmpId = emp.getId() != null ? emp.getId() : "";
            } else {
                selectedEmpId = "";
            }
        });
    }

    private String getEmployeeDisplayName(EmployeesItem emp) {
        if (emp == null) return "";
        String name = "";
        if (emp.getFirstname() != null) name += emp.getFirstname();
        if (emp.getLastname() != null) name += " " + emp.getLastname();
        if (emp.getEmployeeCode() != null) name += " (" + emp.getEmployeeCode() + ")";
        return name.trim();
    }

    private void setupButtons() {
        btnApply.setOnClickListener(v -> {
            if (listener != null) {
                // Department selection
                String deptId = "";
                int deptPos = spinnerDepartment.getSelectedItemPosition();
                if (deptPos > 0 && departments != null && deptPos <= departments.size()) {
                    deptId = departments.get(deptPos - 1).getId();
                }

                // If user cleared text or typed manually without clicking an item
                String text = spinnerEmployee.getText().toString().trim();
                if (text.isEmpty()) {
                    selectedEmpId = "";
                } else {
                    boolean matched = false;
                    for (EmployeesItem emp : activeEmployees) {
                        if (getEmployeeDisplayName(emp).equalsIgnoreCase(text)) {
                            selectedEmpId = emp.getId();
                            matched = true;
                            break;
                        }
                    }
                    if (!matched && selectedEmpId.isEmpty()) {
                        selectedEmpId = "";
                    }
                }

                listener.onFilterApplied(selectedStartDateApi, selectedEndDateApi, deptId, selectedEmpId);
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
        void onFilterApplied(String startDate, String endDate, String departmentId, String employeeId);

        void onFilterReset();
    }

    // ══════════════════════════════════════════════════════════════════
    //  CUSTOM SEARCH ADAPTER FOR REAL-TIME CONTAINS MATCHING
    // ══════════════════════════════════════════════════════════════════

    private class EmployeeSearchAdapter extends ArrayAdapter<EmployeesItem> {

        private final List<EmployeesItem> allEmployees;
        private final List<EmployeesItem> filteredEmployees;

        public EmployeeSearchAdapter(@NonNull Context context, @NonNull List<EmployeesItem> employees) {
            super(context, android.R.layout.simple_dropdown_item_1line, new ArrayList<>(employees));
            this.allEmployees = new ArrayList<>(employees);
            this.filteredEmployees = new ArrayList<>(employees);
        }

        @Override
        public int getCount() {
            return filteredEmployees.size();
        }

        @Nullable
        @Override
        public EmployeesItem getItem(int position) {
            if (position >= 0 && position < filteredEmployees.size()) {
                return filteredEmployees.get(position);
            }
            return null;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            if (view instanceof TextView) {
                EmployeesItem item = getItem(position);
                if (item != null) {
                    ((TextView) view).setText(getEmployeeDisplayName(item));
                }
            }
            return view;
        }

        @NonNull
        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults results = new FilterResults();
                    List<EmployeesItem> matches = new ArrayList<>();

                    if (constraint == null || constraint.length() == 0) {
                        matches.addAll(allEmployees);
                    } else {
                        String query = constraint.toString().toLowerCase().trim();
                        for (EmployeesItem emp : allEmployees) {
                            String first = emp.getFirstname() != null ? emp.getFirstname() : "";
                            String last = emp.getLastname() != null ? emp.getLastname() : "";
                            String code = emp.getEmployeeCode() != null ? emp.getEmployeeCode() : "";
                            String fullName = (first + " " + last).toLowerCase();

                            if (fullName.contains(query) || code.toLowerCase().contains(query)) {
                                matches.add(emp);
                            }
                        }
                    }

                    results.values = matches;
                    results.count = matches.size(); // Fixed: results.count instead of results.size
                    return results;
                }

                @SuppressWarnings("unchecked")
                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    filteredEmployees.clear();
                    if (results != null && results.values != null) {
                        filteredEmployees.addAll((List<EmployeesItem>) results.values);
                    }
                    notifyDataSetChanged();
                }

                @Override
                public CharSequence convertResultToString(Object resultValue) {
                    if (resultValue instanceof EmployeesItem) {
                        return getEmployeeDisplayName((EmployeesItem) resultValue);
                    }
                    return super.convertResultToString(resultValue);
                }
            };
        }
    }
}
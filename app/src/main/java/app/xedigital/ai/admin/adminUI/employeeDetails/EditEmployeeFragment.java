package app.xedigital.ai.admin.adminUI.employeeDetails;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

import app.xedigital.ai.R;
import app.xedigital.ai.admin.adminModal.ActiveShift.ShiftsItem;
import app.xedigital.ai.admin.adminModal.EmployeeDetails.EmployeesItem;
import app.xedigital.ai.admin.adminModal.businessUnit.BusItem;
import app.xedigital.ai.admin.adminModal.department.DepartmentsItem;
import app.xedigital.ai.admin.adminModal.partners.PartnersItem;
import app.xedigital.ai.admin.adminModal.updateEmployee.UpdateEmployeeRequest;
import app.xedigital.ai.databinding.FragmentEditEmployeeBinding;
import app.xedigital.ai.utills.DateTimeUtils;
import app.xedigital.ai.utills.SecurePrefManager;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditEmployeeFragment extends Fragment {

    public static final String ARG_SELECTED_ITEM = "selected_item";

    private FragmentEditEmployeeBinding binding;
    private EditEmployeeViewModel viewModel;
    private String token;
    private EmployeesItem employee;

    private String dobIsoValue;
    private String joiningDateIsoValue;

    private List<DepartmentsItem> cachedDepartments = new ArrayList<>();
    private List<ShiftsItem> cachedShifts = new ArrayList<>();
    private List<EmployeesItem> cachedEmployees = new ArrayList<>();
    private List<PartnersItem> cachedPartners = new ArrayList<>();
    private List<BusItem> cachedBus = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEditEmployeeBinding.inflate(inflater, container, false);
        SecurePrefManager sharedPreferences = SecurePrefManager.getInstance(requireContext());
        token = sharedPreferences.getString("authToken", "");
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(EditEmployeeViewModel.class);

        setupDepartmentSpinner();
        setupShiftSpinner();
        setupReportingManagerSpinner();
        setupCrossManagerSpinner();
        setupPartnerSpinner();
        setupBusinessUnitSpinner();
        setupStaticSpinners();
        setupDatePickers();

        Bundle args = getArguments();
        if (args != null && args.containsKey(ARG_SELECTED_ITEM)) {
            employee = (EmployeesItem) args.getSerializable(ARG_SELECTED_ITEM);
            if (employee != null) {
                viewModel.setSelectedEmployee(employee);
                populateEmployeeForm(employee);
                viewModel.fetchDepartments(token);
                viewModel.fetchShifts(token);
                viewModel.fetchEmployees(token);
                viewModel.fetchPartners(token);
                viewModel.fetchBus(token);
            }
        }

        binding.btnClear.setOnClickListener(v -> clearForm());
        binding.btnSubmit.setOnClickListener(v -> handleSubmit());
    }

    // ========== STATIC SPINNERS ==========

    private void setupStaticSpinners() {
        ArrayAdapter<String> differentlyAbleAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                getResources().getStringArray(R.array.differently_able_options));
        binding.spinnerDifferentlyAbledType.setAdapter(differentlyAbleAdapter);

        ArrayAdapter<String> joiningTypeAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                getResources().getStringArray(R.array.joining_type_options));
        binding.spinnerJoiningType.setAdapter(joiningTypeAdapter);

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                getResources().getStringArray(R.array.status_options));
        binding.spinnerStatus.setAdapter(statusAdapter);

        ArrayAdapter<String> employeeTypeAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                getResources().getStringArray(R.array.employee_type_options));
        binding.spinnerEmployeeType.setAdapter(employeeTypeAdapter);

        // Toggle Partner field based on Employee Type selection
        binding.spinnerEmployeeType.setOnItemClickListener((parent, view, position, id) -> {
            String selected = (String) parent.getItemAtPosition(position);
            applyEmployeeTypeVisibility(selected);
            binding.tilEmployeeType.setError(null);
        });
    }

    private void applyEmployeeTypeVisibility(String employeeTypeDisplay) {
        if (binding == null) return;
        boolean isPartner = "Partner".equalsIgnoreCase(employeeTypeDisplay);
        if (isPartner) {
            binding.tilPartner.setVisibility(View.VISIBLE);
        } else {
            binding.tilPartner.setVisibility(View.GONE);
            binding.spinnerPartner.setText("", false);
            binding.tilPartner.setError(null);
            viewModel.setSelectedPartner(null);
        }
    }

    // ========== DATE PICKERS ==========

    private void setupDatePickers() {
        binding.etDOB.setOnClickListener(v -> showDatePickerForDOB());
        binding.etJoiningDate.setOnClickListener(v -> showDatePickerForJoining());
    }

    private void showDatePickerForDOB() {
        MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Date of Birth");

        CalendarConstraints constraints = new CalendarConstraints.Builder()
                .setValidator(DateValidatorPointBackward.now())
                .build();
        builder.setCalendarConstraints(constraints);

        if (!TextUtils.isEmpty(dobIsoValue)) {
            Long parsed = parseIsoToUtcMillis(dobIsoValue);
            if (parsed != null) builder.setSelection(parsed);
        }

        MaterialDatePicker<Long> picker = builder.build();
        picker.addOnPositiveButtonClickListener(selection -> {
            if (selection == null) return;
            dobIsoValue = utcMillisToIso(selection);
            binding.etDOB.setText(DateTimeUtils.formatToReadableDate(dobIsoValue));
            binding.tilDOB.setError(null);
        });
        picker.show(getParentFragmentManager(), "DOB_PICKER");
    }

    private void showDatePickerForJoining() {
        MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Joining Date");

        CalendarConstraints constraints = new CalendarConstraints.Builder()
                .setValidator(DateValidatorPointBackward.now())
                .build();
        builder.setCalendarConstraints(constraints);

        if (!TextUtils.isEmpty(joiningDateIsoValue)) {
            Long parsed = parseIsoToUtcMillis(joiningDateIsoValue);
            if (parsed != null) builder.setSelection(parsed);
        }

        MaterialDatePicker<Long> picker = builder.build();
        picker.addOnPositiveButtonClickListener(selection -> {
            if (selection == null) return;
            joiningDateIsoValue = utcMillisToIso(selection);
            binding.etJoiningDate.setText(DateTimeUtils.formatToReadableDate(joiningDateIsoValue));
            binding.tilJoiningDate.setError(null);
        });
        picker.show(getParentFragmentManager(), "JOIN_PICKER");
    }

    private Long parseIsoToUtcMillis(String iso) {
        if (TextUtils.isEmpty(iso)) return null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date d = sdf.parse(iso);
            return d != null ? d.getTime() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private String utcMillisToIso(long millis) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(new Date(millis));
    }

    // ========== DYNAMIC SPINNERS ==========

    private void setupDepartmentSpinner() {
        viewModel.getDepartments().observe(getViewLifecycleOwner(), departments -> {
            if (departments == null) return;
            cachedDepartments = departments;

            List<String> names = new ArrayList<>();
            for (DepartmentsItem item : departments) {
                names.add(item.getName() != null ? item.getName() : "");
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_spinner_dropdown_item, names);
            binding.spinnerDepartment.setAdapter(adapter);

            binding.spinnerDepartment.setOnItemClickListener((parent, view, position, id) -> {
                if (position >= 0 && position < cachedDepartments.size()) {
                    viewModel.setSelectedDepartment(cachedDepartments.get(position));
                }
                binding.tilDepartment.setError(null);
            });

            if (employee != null && employee.getDepartment() != null && employee.getDepartment().getName() != null) {
                binding.spinnerDepartment.setText(employee.getDepartment().getName(), false);
                viewModel.setSelectedDepartment(findDepartmentById(employee.getDepartment().getId()));
            }
        });
    }

    private DepartmentsItem findDepartmentById(String id) {
        if (id == null) return null;
        for (DepartmentsItem d : cachedDepartments) {
            if (id.equals(d.getId())) return d;
        }
        return null;
    }

    private void setupShiftSpinner() {
        viewModel.getShifts().observe(getViewLifecycleOwner(), shifts -> {
            if (shifts == null) return;
            cachedShifts = shifts;

            List<String> displayNames = new ArrayList<>();
            for (ShiftsItem item : shifts) {
                displayNames.add(buildShiftDisplay(item.getName(), item.getStartTime(), item.getEndTime()));
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_spinner_dropdown_item, displayNames);
            binding.spinnerShift.setAdapter(adapter);

            binding.spinnerShift.setOnItemClickListener((parent, view, position, id) -> {
                if (position >= 0 && position < cachedShifts.size()) {
                    viewModel.setSelectedShift(cachedShifts.get(position));
                }
                binding.tilShift.setError(null);
            });

            if (employee != null && employee.getShift() != null) {
                String preselect = buildShiftDisplay(
                        employee.getShift().getName(),
                        employee.getShift().getStartTime(),
                        employee.getShift().getEndTime());
                binding.spinnerShift.setText(preselect, false);
                viewModel.setSelectedShift(findShiftById(employee.getShift().getId()));
            }
        });
    }

    private String buildShiftDisplay(String name, String start, String end) {
        String n = (name != null) ? name.trim() : "";
        if (!TextUtils.isEmpty(start) && !TextUtils.isEmpty(end)) {
            return n + " (" + start.trim() + " - " + end.trim() + ")";
        }
        return n;
    }

    private ShiftsItem findShiftById(String id) {
        if (id == null) return null;
        for (ShiftsItem s : cachedShifts) {
            if (id.equals(s.getId())) return s;
        }
        return null;
    }

    private void setupReportingManagerSpinner() {
        viewModel.getEmployees().observe(getViewLifecycleOwner(), employees -> {
            if (employees == null) return;
            cachedEmployees = employees;

            List<String> names = new ArrayList<>();
            for (EmployeesItem item : employees) {
                names.add(buildFullName(item.getFirstname(), item.getLastname()));
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_spinner_dropdown_item, names);
            binding.spinnerReportingManager.setAdapter(adapter);

            binding.spinnerReportingManager.setOnItemClickListener((parent, view, position, id) -> {
                if (position >= 0 && position < cachedEmployees.size()) {
                    viewModel.setSelectedReportingManager(cachedEmployees.get(position));
                }
                binding.tilReportingManager.setError(null);
            });

            if (employee != null && employee.getReportingManager() != null) {
                String fullName = buildFullName(
                        safeGetField(employee.getReportingManager(), "getFirstname"),
                        safeGetField(employee.getReportingManager(), "getLastname"));
                binding.spinnerReportingManager.setText(fullName, false);
            }

            populateCrossManagerAdapter(names);
        });
    }

    private void setupCrossManagerSpinner() {
        viewModel.getEmployees().observe(getViewLifecycleOwner(), employees -> {
            if (employees == null) return;
            if (employee != null && employee.getCrossmanager() != null) {
                String fullName = buildFullName(
                        safeGetField(employee.getCrossmanager(), "getFirstname"),
                        safeGetField(employee.getCrossmanager(), "getLastname"));
                binding.spinnerCrossFunctionalManager.setText(fullName, false);
            }
        });
    }

    private void populateCrossManagerAdapter(List<String> names) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, names);
        binding.spinnerCrossFunctionalManager.setAdapter(adapter);

        binding.spinnerCrossFunctionalManager.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < cachedEmployees.size()) {
                viewModel.setSelectedCrossManager(cachedEmployees.get(position));
            }
            binding.tilCrossFunctionalManager.setError(null);
        });
    }

    private void setupPartnerSpinner() {
        viewModel.getPartners().observe(getViewLifecycleOwner(), partners -> {
            if (partners == null) return;

            // Filter to active-only
            cachedPartners = new ArrayList<>();
            for (PartnersItem p : partners) {
                if (p != null && p.isActive()) {
                    cachedPartners.add(p);
                }
            }

            List<String> names = new ArrayList<>();
            for (PartnersItem item : cachedPartners) {
                names.add(item.getName() != null ? item.getName() : "");
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_spinner_dropdown_item, names);
            binding.spinnerPartner.setAdapter(adapter);

            binding.spinnerPartner.setOnItemClickListener((parent, view, position, id) -> {
                if (position >= 0 && position < cachedPartners.size()) {
                    viewModel.setSelectedPartner(cachedPartners.get(position));
                }
                binding.tilPartner.setError(null);
            });

            // Preselect from employee.partner (Object type)
            if (employee != null && employee.getPartner() != null) {
                String partnerId = extractIdFromObject(employee.getPartner());
                if (!TextUtils.isEmpty(partnerId)) {
                    PartnersItem match = findPartnerById(partnerId);
                    if (match != null) {
                        binding.spinnerPartner.setText(match.getName() != null ? match.getName() : "", false);
                        viewModel.setSelectedPartner(match);
                    }
                }
            }
        });
    }

    private PartnersItem findPartnerById(String id) {
        if (id == null) return null;
        for (PartnersItem p : cachedPartners) {
            if (id.equals(p.getId())) return p;
        }
        return null;
    }

    private String extractIdFromObject(Object obj) {
        if (obj == null) return null;
        if (obj instanceof String) {
            String s = (String) obj;
            return s.trim().isEmpty() ? null : s.trim();
        }
        // Try common id getters
        String id = safeGetField(obj, "getId");
        if (!TextUtils.isEmpty(id)) return id;
        id = safeGetField(obj, "get_id");
        return id;
    }

    private void setupBusinessUnitSpinner() {
        viewModel.getBus().observe(getViewLifecycleOwner(), busList -> {
            if (busList == null) return;

            // Filter to active-only (BusItem.active is String)
            cachedBus = new ArrayList<>();
            for (BusItem b : busList) {
                if (b != null && isBusActive(b.getActive())) {
                    cachedBus.add(b);
                }
            }

            List<String> names = new ArrayList<>();
            for (BusItem item : cachedBus) {
                names.add(item.getName() != null ? item.getName() : "");
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_spinner_dropdown_item, names);
            binding.spinnerBusinessUnit.setAdapter(adapter);

            binding.spinnerBusinessUnit.setOnItemClickListener((parent, view, position, id) -> {
                if (position >= 0 && position < cachedBus.size()) {
                    viewModel.setSelectedBu(cachedBus.get(position));
                }
                binding.tilBusinessUnit.setError(null);
            });

            // No BU field in EmployeesItem — nothing to preselect
        });
    }

    private boolean isBusActive(String activeValue) {
        if (TextUtils.isEmpty(activeValue)) return false;
        String v = activeValue.trim().toLowerCase();
        return v.equals("true") || v.equals("active") || v.equals("1") || v.equals("yes");
    }

    private String buildFullName(String first, String last) {
        StringBuilder sb = new StringBuilder();
        if (!TextUtils.isEmpty(first)) sb.append(first.trim());
        if (!TextUtils.isEmpty(last)) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(last.trim());
        }
        return sb.toString();
    }

    private String safeGetField(Object obj, String methodName) {
        if (obj == null) return null;
        try {
            Object result = obj.getClass().getMethod(methodName).invoke(obj);
            return result != null ? result.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }

    // ========== FORM POPULATION ==========

    private void populateEmployeeForm(EmployeesItem employee) {
        binding.etFirstName.setText(safeStr(employee.getFirstname()));
        binding.etLastName.setText(safeStr(employee.getLastname()));
        binding.etFatherName.setText(safeStr(employee.getFatherName()));
        binding.etEmail.setText(safeStr(employee.getEmail()));
        binding.etContact.setText(safeStr(employee.getContact()));
        binding.etResidentialAddress.setText(safeStr(employee.getAddress()));
        binding.etState.setText(safeStr(employee.getState()));
        binding.etPinCode.setText(safeStr(employee.getPincode()));
        binding.etEmployeeCode.setText(safeStr(employee.getEmployeeCode()));
        binding.etDesignation.setText(safeStr(employee.getDesignation()));
        binding.etLevel.setText(safeStr(employee.getLevel()));
        binding.etGrade.setText(safeStr(employee.getGrade()));
        binding.etPAN.setText(safeStr(employee.getPanNo()));
        binding.etAadhaar.setText(safeStr(employee.getAdharNo()));

        if (!TextUtils.isEmpty(employee.getDateOfBirth())) {
            dobIsoValue = employee.getDateOfBirth();
            binding.etDOB.setText(DateTimeUtils.formatToReadableDate(dobIsoValue));
        }
        if (!TextUtils.isEmpty(employee.getJoiningDate())) {
            joiningDateIsoValue = employee.getJoiningDate();
            binding.etJoiningDate.setText(DateTimeUtils.formatToReadableDate(joiningDateIsoValue));
        }

        binding.spinnerStatus.setText(employee.isActive() ? "Active" : "Deactive", false);

        String empTypeDisplay = mapEmployeeTypeApiToDisplay(employee.getEmployeeType());
        binding.spinnerEmployeeType.setText(empTypeDisplay, false);
        applyEmployeeTypeVisibility(empTypeDisplay);

        if (!TextUtils.isEmpty(employee.getJoiningType())) {
            binding.spinnerJoiningType.setText(capitalize(employee.getJoiningType()), false);
        }

        if (!TextUtils.isEmpty(employee.getDifferentlyAbled())) {
            binding.spinnerDifferentlyAbledType.setText(employee.getDifferentlyAbled(), false);
        }

        binding.switchIsHROrAdmin.setChecked(employee.isIsHROrAdmin());
    }

    private String mapEmployeeTypeApiToDisplay(String apiValue) {
        if (TextUtils.isEmpty(apiValue)) return "";
        if ("employee".equalsIgnoreCase(apiValue)) return "Internal Employee";
        if ("partner".equalsIgnoreCase(apiValue)) return "Partner";
        return apiValue;
    }

    private String mapEmployeeTypeDisplayToApi(String displayValue) {
        if (TextUtils.isEmpty(displayValue)) return "";
        if ("Internal Employee".equalsIgnoreCase(displayValue)) return "employee";
        if ("Partner".equalsIgnoreCase(displayValue)) return "partner";
        return displayValue.toLowerCase();
    }

    private String capitalize(String s) {
        if (TextUtils.isEmpty(s)) return "";
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }

    private String safeStr(String s) {
        return s != null ? s : "";
    }

    // ========== SUBMIT ==========

    private void handleSubmit() {
        if (!validateForm()) {
            Toast.makeText(getContext(), "Please fix the highlighted errors", Toast.LENGTH_SHORT).show();
            return;
        }

        EmployeesItem selected = viewModel.getSelectedEmployee().getValue();
        if (selected == null) {
            Toast.makeText(getContext(), "Employee data missing", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        UpdateEmployeeRequest payload = new UpdateEmployeeRequest();
        payload.setEmployeeCode(getText(binding.etEmployeeCode));
        payload.setFirstname(getText(binding.etFirstName));
        payload.setLastname(getText(binding.etLastName));
        payload.setEmail(getText(binding.etEmail));
        payload.setContact(getText(binding.etContact));
        payload.setDateOfBirth(dobIsoValue);
        payload.setJoiningDate(joiningDateIsoValue);
        payload.setJoiningType(binding.spinnerJoiningType.getText().toString().toLowerCase());

        EmployeesItem selectedRm = viewModel.getSelectedReportingManager();
        if (selectedRm != null) {
            payload.setReportingManager(selectedRm.getId());
        } else if (selected.getReportingManager() != null) {
            String rmId = safeGetField(selected.getReportingManager(), "getId");
            payload.setReportingManager(rmId);
        }

        EmployeesItem selectedCm = viewModel.getSelectedCrossManager();
        if (selectedCm != null) {
            payload.setCrossmanager(selectedCm.getId());
        } else if (selected.getCrossmanager() != null) {
            String cmId = safeGetField(selected.getCrossmanager(), "getId");
            payload.setCrossmanager(cmId);
        }

        payload.setDesignation(getText(binding.etDesignation));
        payload.setLevel(getText(binding.etLevel));
        payload.setGrade(getText(binding.etGrade));
        payload.setIsHROrAdmin(binding.switchIsHROrAdmin.isChecked());
        payload.setCompany(selected.getCompany());

        DepartmentsItem selectedDept = viewModel.getSelectedDepartment();
        if (selectedDept != null) {
            payload.setDepartment(selectedDept.getId());
        } else if (selected.getDepartment() != null) {
            payload.setDepartment(selected.getDepartment().getId());
        }

        // Business Unit — optional, send only if selected
        BusItem selectedBu = viewModel.getSelectedBu();
        if (selectedBu != null) {
            payload.setBu(selectedBu.getId());
        }

        ShiftsItem selectedShift = viewModel.getSelectedShift();
        if (selectedShift != null) {
            payload.setShift(selectedShift.getId());
        } else if (selected.getShift() != null) {
            payload.setShift(selected.getShift().getId());
        }

        String empTypeApi = mapEmployeeTypeDisplayToApi(binding.spinnerEmployeeType.getText().toString());
        payload.setEmployeeType(empTypeApi);

        // Partner — only send id when Partner field is visible (Employee Type = partner)
        boolean partnerVisible = binding.tilPartner.getVisibility() == View.VISIBLE;
        if (partnerVisible) {
            PartnersItem selectedPartner = viewModel.getSelectedPartner();
            payload.setPartner(selectedPartner != null ? selectedPartner.getId() : "");
        } else {
            payload.setPartner("");
        }

        String status = binding.spinnerStatus.getText().toString();
        payload.setActive(status.equalsIgnoreCase("Active"));

        payload.setIsVerified(selected.isIsVerified());
        payload.setFatherName(getText(binding.etFatherName));
        payload.setPanNo(getText(binding.etPAN));
        payload.setAdharNo(getText(binding.etAadhaar));
        payload.setDifferentlyAbled(binding.spinnerDifferentlyAbledType.getText().toString());
        payload.setAddress(getText(binding.etResidentialAddress));
        payload.setState(getText(binding.etState));
        payload.setPincode(getText(binding.etPinCode));

        viewModel.updateEmployee(token, selected.getId(), payload, new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (binding == null) return;
                setLoading(false);
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Employee updated successfully!", Toast.LENGTH_SHORT).show();
                    NavController navController = Navigation.findNavController(binding.getRoot());
                    navController.navigate(R.id.nav_editEmployee_to_nav_employees);
                } else {
                    Toast.makeText(getContext(), "Failed to update employee", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                if (binding == null) return;
                setLoading(false);
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getText(com.google.android.material.textfield.TextInputEditText et) {
        return Objects.requireNonNull(et.getText()).toString().trim();
    }

    private void setLoading(boolean loading) {
        if (binding == null) return;
        binding.btnSubmit.setEnabled(!loading);
        binding.btnClear.setEnabled(!loading);
        if (loading) {
            binding.btnSubmit.setText("");
            binding.submitProgressBar.setVisibility(View.VISIBLE);
        } else {
            binding.btnSubmit.setText("Submit");
            binding.submitProgressBar.setVisibility(View.GONE);
        }
    }

    // ========== VALIDATION ==========

    private boolean validateForm() {
        boolean ok = true;

        clearErrors();

        // Only 4 fields are required: Level, Grade, Reporting Manager, Cross Manager

        if (TextUtils.isEmpty(getText(binding.etLevel))) {
            binding.tilLevel.setError("Level is required");
            ok = false;
        }

        if (TextUtils.isEmpty(getText(binding.etGrade))) {
            binding.tilGrade.setError("Grade is required");
            ok = false;
        }

        if (TextUtils.isEmpty(binding.spinnerReportingManager.getText().toString())) {
            binding.tilReportingManager.setError("Reporting manager is required");
            ok = false;
        }

        if (TextUtils.isEmpty(binding.spinnerCrossFunctionalManager.getText().toString())) {
            binding.tilCrossFunctionalManager.setError("Cross-functional manager is required");
            ok = false;
        }

        return ok;
    }

    private void clearErrors() {
        binding.tilLevel.setError(null);
        binding.tilGrade.setError(null);
        binding.tilReportingManager.setError(null);
        binding.tilCrossFunctionalManager.setError(null);
    }

    // ========== CLEAR ==========

    private void clearForm() {
        binding.etFirstName.setText("");
        binding.etLastName.setText("");
        binding.etFatherName.setText("");
        binding.etEmail.setText("");
        binding.etContact.setText("");
        binding.etResidentialAddress.setText("");
        binding.etState.setText("");
        binding.etPinCode.setText("");
        binding.etEmployeeCode.setText("");
        binding.etDesignation.setText("");
        binding.etLevel.setText("");
        binding.etGrade.setText("");
        binding.etDOB.setText("");
        binding.etJoiningDate.setText("");
        binding.etPAN.setText("");
        binding.etAadhaar.setText("");

        binding.spinnerStatus.setText("", false);
        binding.spinnerEmployeeType.setText("", false);
        binding.spinnerDepartment.setText("", false);
        binding.spinnerShift.setText("", false);
        binding.spinnerJoiningType.setText("", false);
        binding.spinnerReportingManager.setText("", false);
        binding.spinnerCrossFunctionalManager.setText("", false);
        binding.spinnerDifferentlyAbledType.setText("", false);
        binding.spinnerBusinessUnit.setText("", false);
        binding.spinnerPartner.setText("", false);

        binding.tilPartner.setVisibility(View.GONE);
        binding.switchIsHROrAdmin.setChecked(false);

        dobIsoValue = null;
        joiningDateIsoValue = null;

        viewModel.setSelectedBu(null);
        viewModel.setSelectedPartner(null);

        clearErrors();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
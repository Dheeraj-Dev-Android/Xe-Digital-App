package app.xedigital.ai.admin.adminUI.employeeDetails;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import app.xedigital.ai.R;
import app.xedigital.ai.admin.adminModal.ActiveShift.ShiftsItem;
import app.xedigital.ai.admin.adminModal.EmployeeDetails.EmployeesItem;
import app.xedigital.ai.admin.adminModal.addEmployee.AddEmployeeRequest;
import app.xedigital.ai.admin.adminModal.addEmployee.ComponentsItem;
import app.xedigital.ai.admin.adminModal.businessUnit.BusItem;
import app.xedigital.ai.admin.adminModal.department.DepartmentsItem;
import app.xedigital.ai.admin.adminModal.partners.PartnersItem;
import app.xedigital.ai.utills.SecurePrefManager;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateNewEmployeeFragment extends Fragment {

    // Date formatters
    private final SimpleDateFormat displayDateFormat;
    private final SimpleDateFormat apiDateFormat;
    private CreateNewEmployeeViewModel mViewModel;
    private String token;
    // ScrollView
    private NestedScrollView scrollViewEmployeeDetails;
    // ── Assignment ──
    private TextInputLayout tilStatus, tilEmployeeType, tilPartner, tilDepartment,
            tilBusinessUnit, tilShift, tilEmployeeCode;
    private AutoCompleteTextView spinnerStatus, spinnerEmployeeType, spinnerPartner,
            spinnerDepartment, spinnerBusinessUnit, spinnerShift;
    private TextInputEditText etEmployeeCode;
    // ── Personal Information ──
    private TextInputLayout tilFirstName, tilLastName, tilFatherName, tilEmail,
            tilContact, tilDesignation, tilLevel, tilGrade, tilDOB, tilJoiningDate, tilJoiningType;
    private TextInputEditText etFirstName, etLastName, etFatherName, etEmail,
            etContact, etDesignation, etLevel, etGrade, etDOB, etJoiningDate;
    private AutoCompleteTextView spinnerJoiningType;
    // ── Reporting ──
    private TextInputLayout tilReportingManager, tilCrossFunctionalManager;
    private AutoCompleteTextView spinnerReportingManager, spinnerCrossFunctionalManager;
    // ── Compliance & IDs ──
    private TextInputLayout tilPAN, tilAadhaar, tilDifferentlyAbledType;
    private TextInputEditText etPAN, etAadhaar;
    private AutoCompleteTextView spinnerDifferentlyAbledType;
    // ── Address ──
    private TextInputLayout tilResidentialAddress, tilState, tilPinCode;
    private TextInputEditText etResidentialAddress, etState, etPinCode;
    // ── Permissions ──
    private SwitchMaterial switchIsHROrAdmin;
    // ── Bottom Bar ──
    private MaterialButton btnClear, btnSubmit;
    private ProgressBar submitProgressBar;
    // ── Local caches (populated from LiveData observers) ──
    private List<DepartmentsItem> cachedDepartments = new ArrayList<>();
    private List<BusItem> cachedBus = new ArrayList<>();
    private List<ShiftsItem> cachedShifts = new ArrayList<>();
    private List<EmployeesItem> cachedEmployees = new ArrayList<>();
    private List<PartnersItem> cachedPartners = new ArrayList<>();

    {
        displayDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        displayDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        apiDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        apiDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public static CreateNewEmployeeFragment newInstance() {
        return new CreateNewEmployeeFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        token = SecurePrefManager.getInstance(requireContext()).getString("authToken", "");
        return inflater.inflate(R.layout.fragment_create_new_employee, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(CreateNewEmployeeViewModel.class);

        initViews(view);
        setupListeners();
        setupStaticSpinners();
        setupObservers();

        mViewModel.fetchDepartments(token);
        mViewModel.fetchBus(token);
        mViewModel.fetchShifts(token);
        mViewModel.fetchEmployees(token);
        mViewModel.fetchPartners(token);
    }

    // ════════════════════════════════════════════════════════════
    //  VIEW BINDING
    // ════════════════════════════════════════════════════════════

    private void initViews(View view) {
        scrollViewEmployeeDetails = view.findViewById(R.id.scrollViewEmployeeDetails);

        tilStatus = view.findViewById(R.id.tilStatus);
        spinnerStatus = view.findViewById(R.id.spinnerStatus);
        tilEmployeeType = view.findViewById(R.id.tilEmployeeType);
        spinnerEmployeeType = view.findViewById(R.id.spinnerEmployeeType);
        tilPartner = view.findViewById(R.id.tilPartner);
        spinnerPartner = view.findViewById(R.id.spinnerPartner);
        tilDepartment = view.findViewById(R.id.tilDepartment);
        spinnerDepartment = view.findViewById(R.id.spinnerDepartment);
        tilBusinessUnit = view.findViewById(R.id.tilBusinessUnit);
        spinnerBusinessUnit = view.findViewById(R.id.spinnerBusinessUnit);
        tilShift = view.findViewById(R.id.tilShift);
        spinnerShift = view.findViewById(R.id.spinnerShift);
        tilEmployeeCode = view.findViewById(R.id.tilEmployeeCode);
        etEmployeeCode = view.findViewById(R.id.etEmployeeCode);

        tilFirstName = view.findViewById(R.id.tilFirstName);
        etFirstName = view.findViewById(R.id.etFirstName);
        tilLastName = view.findViewById(R.id.tilLastName);
        etLastName = view.findViewById(R.id.etLastName);
        tilFatherName = view.findViewById(R.id.tilFatherName);
        etFatherName = view.findViewById(R.id.etFatherName);
        tilEmail = view.findViewById(R.id.tilEmail);
        etEmail = view.findViewById(R.id.etEmail);
        tilContact = view.findViewById(R.id.tilContact);
        etContact = view.findViewById(R.id.etContact);
        tilDesignation = view.findViewById(R.id.tilDesignation);
        etDesignation = view.findViewById(R.id.etDesignation);
        tilLevel = view.findViewById(R.id.tilLevel);
        etLevel = view.findViewById(R.id.etLevel);
        tilGrade = view.findViewById(R.id.tilGrade);
        etGrade = view.findViewById(R.id.etGrade);
        tilDOB = view.findViewById(R.id.tilDOB);
        etDOB = view.findViewById(R.id.etDOB);
        tilJoiningDate = view.findViewById(R.id.tilJoiningDate);
        etJoiningDate = view.findViewById(R.id.etJoiningDate);
        tilJoiningType = view.findViewById(R.id.tilJoiningType);
        spinnerJoiningType = view.findViewById(R.id.spinnerJoiningType);

        tilReportingManager = view.findViewById(R.id.tilReportingManager);
        spinnerReportingManager = view.findViewById(R.id.spinnerReportingManager);
        tilCrossFunctionalManager = view.findViewById(R.id.tilCrossFunctionalManager);
        spinnerCrossFunctionalManager = view.findViewById(R.id.spinnerCrossFunctionalManager);

        tilPAN = view.findViewById(R.id.tilPAN);
        etPAN = view.findViewById(R.id.etPAN);
        tilAadhaar = view.findViewById(R.id.tilAadhaar);
        etAadhaar = view.findViewById(R.id.etAadhaar);
        tilDifferentlyAbledType = view.findViewById(R.id.tilDifferentlyAbledType);
        spinnerDifferentlyAbledType = view.findViewById(R.id.spinnerDifferentlyAbledType);

        tilResidentialAddress = view.findViewById(R.id.tilResidentialAddress);
        etResidentialAddress = view.findViewById(R.id.etResidentialAddress);
        tilState = view.findViewById(R.id.tilState);
        etState = view.findViewById(R.id.etState);
        tilPinCode = view.findViewById(R.id.tilPinCode);
        etPinCode = view.findViewById(R.id.etPinCode);

        switchIsHROrAdmin = view.findViewById(R.id.switchIsHROrAdmin);

        btnClear = view.findViewById(R.id.btnClear);
        btnSubmit = view.findViewById(R.id.btnSubmit);
        submitProgressBar = view.findViewById(R.id.submitProgressBar);
    }

    // ════════════════════════════════════════════════════════════
    //  LISTENERS
    // ════════════════════════════════════════════════════════════

    private void setupListeners() {
        etDOB.setOnClickListener(v -> showDatePicker(etDOB, tilDOB));
        etJoiningDate.setOnClickListener(v -> showDatePicker(etJoiningDate, tilJoiningDate));

        clearErrorOnChange(spinnerEmployeeType, tilEmployeeType);
        clearErrorOnChange(etEmployeeCode, tilEmployeeCode);
        clearErrorOnChange(etFirstName, tilFirstName);
        clearErrorOnChange(etLastName, tilLastName);
        clearErrorOnChange(etEmail, tilEmail);
        clearErrorOnChange(etContact, tilContact);
        clearErrorOnChange(etLevel, tilLevel);
        clearErrorOnChange(etGrade, tilGrade);
        clearErrorOnChange(spinnerJoiningType, tilJoiningType);
        clearErrorOnChange(spinnerReportingManager, tilReportingManager);

        btnClear.setOnClickListener(v -> clearForm());
        btnSubmit.setOnClickListener(v -> onSubmitClicked());
    }

    // ════════════════════════════════════════════════════════════
    //  STATIC SPINNERS
    // ════════════════════════════════════════════════════════════

    private void setupStaticSpinners() {
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                getResources().getStringArray(R.array.status_options));
        spinnerStatus.setAdapter(statusAdapter);

        ArrayAdapter<String> employeeTypeAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                getResources().getStringArray(R.array.employee_type_options));
        spinnerEmployeeType.setAdapter(employeeTypeAdapter);

        spinnerEmployeeType.setOnItemClickListener((parent, view, position, id) -> {
            String selected = (String) parent.getItemAtPosition(position);
            if ("Partner".equalsIgnoreCase(selected)) {
                tilPartner.setVisibility(View.VISIBLE);
            } else {
                tilPartner.setVisibility(View.GONE);
                spinnerPartner.setText("", false);
                clearTilError(tilPartner);
                mViewModel.setSelectedPartner(null);
            }
            clearTilError(tilEmployeeType);
        });

        ArrayAdapter<String> joiningTypeAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                getResources().getStringArray(R.array.joining_type_options));
        spinnerJoiningType.setAdapter(joiningTypeAdapter);

        ArrayAdapter<String> differentlyAbledAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                getResources().getStringArray(R.array.differently_able_options));
        spinnerDifferentlyAbledType.setAdapter(differentlyAbledAdapter);
    }

    // ════════════════════════════════════════════════════════════
    //  OBSERVERS (MVVM)
    // ════════════════════════════════════════════════════════════

    private void setupObservers() {
        mViewModel.getDepartments().observe(getViewLifecycleOwner(), departments -> {
            if (departments == null) return;
            cachedDepartments = departments;

            List<String> names = new ArrayList<>();
            for (DepartmentsItem item : cachedDepartments) {
                names.add(item.getName() != null ? item.getName() : "");
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_spinner_dropdown_item, names);
            spinnerDepartment.setAdapter(adapter);

            spinnerDepartment.setOnItemClickListener((parent, view, position, id) -> {
                if (position >= 0 && position < cachedDepartments.size()) {
                    mViewModel.setSelectedDepartment(cachedDepartments.get(position));
                }
                clearTilError(tilDepartment);
            });
        });

        mViewModel.getBus().observe(getViewLifecycleOwner(), busList -> {
            if (busList == null) return;

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
            spinnerBusinessUnit.setAdapter(adapter);

            spinnerBusinessUnit.setOnItemClickListener((parent, view, position, id) -> {
                if (position >= 0 && position < cachedBus.size()) {
                    mViewModel.setSelectedBu(cachedBus.get(position));
                }
                clearTilError(tilBusinessUnit);
            });
        });

        mViewModel.getShifts().observe(getViewLifecycleOwner(), shifts -> {
            if (shifts == null) return;
            cachedShifts = shifts;

            List<String> displayNames = new ArrayList<>();
            for (ShiftsItem item : cachedShifts) {
                displayNames.add(buildShiftDisplay(item.getName(), item.getStartTime(), item.getEndTime()));
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_spinner_dropdown_item, displayNames);
            spinnerShift.setAdapter(adapter);

            spinnerShift.setOnItemClickListener((parent, view, position, id) -> {
                if (position >= 0 && position < cachedShifts.size()) {
                    mViewModel.setSelectedShift(cachedShifts.get(position));
                }
                clearTilError(tilShift);
            });
        });

        mViewModel.getEmployees().observe(getViewLifecycleOwner(), employees -> {
            if (employees == null) return;
            cachedEmployees = employees;

            List<String> names = new ArrayList<>();
            for (EmployeesItem item : cachedEmployees) {
                names.add(buildFullName(item.getFirstname(), item.getLastname()));
            }

            ArrayAdapter<String> rmAdapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_spinner_dropdown_item, names);
            spinnerReportingManager.setAdapter(rmAdapter);
            spinnerReportingManager.setOnItemClickListener((parent, view, position, id) -> {
                if (position >= 0 && position < cachedEmployees.size()) {
                    mViewModel.setSelectedReportingManager(cachedEmployees.get(position));
                }
                clearTilError(tilReportingManager);
            });

            ArrayAdapter<String> cmAdapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_spinner_dropdown_item, names);
            spinnerCrossFunctionalManager.setAdapter(cmAdapter);
            spinnerCrossFunctionalManager.setOnItemClickListener((parent, view, position, id) -> {
                if (position >= 0 && position < cachedEmployees.size()) {
                    mViewModel.setSelectedCrossManager(cachedEmployees.get(position));
                }
                clearTilError(tilCrossFunctionalManager);
            });
        });

        mViewModel.getPartners().observe(getViewLifecycleOwner(), partners -> {
            if (partners == null) return;

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
            spinnerPartner.setAdapter(adapter);

            spinnerPartner.setOnItemClickListener((parent, view, position, id) -> {
                if (position >= 0 && position < cachedPartners.size()) {
                    mViewModel.setSelectedPartner(cachedPartners.get(position));
                }
                clearTilError(tilPartner);
            });
        });

        mViewModel.getErrorMessage().observe(getViewLifecycleOwner(), msg -> {
            if (!TextUtils.isEmpty(msg) && isAdded()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });

        mViewModel.getIsLoading().observe(getViewLifecycleOwner(), this::setLoadingState);
    }

    // ════════════════════════════════════════════════════════════
    //  DATE PICKER
    // ════════════════════════════════════════════════════════════

    private void showDatePicker(TextInputEditText editText, TextInputLayout til) {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Date")
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            editText.setText(displayDateFormat.format(new Date(selection)));
            clearTilError(til);
        });

        if (getParentFragmentManager() != null) {
            datePicker.show(getParentFragmentManager(), "DATE_PICKER_" + editText.getId());
        }
    }

    // ════════════════════════════════════════════════════════════
    //  CLEAR FORM
    // ════════════════════════════════════════════════════════════

    private void clearForm() {
        spinnerStatus.setText("", false);
        spinnerEmployeeType.setText("", false);
        spinnerPartner.setText("", false);
        spinnerDepartment.setText("", false);
        spinnerBusinessUnit.setText("", false);
        spinnerShift.setText("", false);
        spinnerJoiningType.setText("", false);
        spinnerReportingManager.setText("", false);
        spinnerCrossFunctionalManager.setText("", false);
        spinnerDifferentlyAbledType.setText("", false);

        etEmployeeCode.setText("");
        etFirstName.setText("");
        etLastName.setText("");
        etFatherName.setText("");
        etEmail.setText("");
        etContact.setText("");
        etDesignation.setText("");
        etLevel.setText("");
        etGrade.setText("");
        etDOB.setText("");
        etJoiningDate.setText("");
        etPAN.setText("");
        etAadhaar.setText("");
        etResidentialAddress.setText("");
        etState.setText("");
        etPinCode.setText("");

        clearTilError(tilStatus, tilEmployeeType, tilPartner, tilDepartment,
                tilBusinessUnit, tilShift, tilEmployeeCode,
                tilFirstName, tilLastName, tilFatherName, tilEmail, tilContact,
                tilDesignation, tilLevel, tilGrade, tilDOB, tilJoiningDate, tilJoiningType,
                tilReportingManager, tilCrossFunctionalManager,
                tilPAN, tilAadhaar, tilDifferentlyAbledType,
                tilResidentialAddress, tilState, tilPinCode);

        switchIsHROrAdmin.setChecked(false);
        tilPartner.setVisibility(View.GONE);

        mViewModel.setSelectedDepartment(null);
        mViewModel.setSelectedBu(null);
        mViewModel.setSelectedShift(null);
        mViewModel.setSelectedReportingManager(null);
        mViewModel.setSelectedCrossManager(null);
        mViewModel.setSelectedPartner(null);

        scrollViewEmployeeDetails.smoothScrollTo(0, 0);
    }

    // ════════════════════════════════════════════════════════════
    //  VALIDATION (only mandatory fields)
    // ════════════════════════════════════════════════════════════

    private boolean validateForm() {
        boolean isValid = true;
        View firstErrorView = null;

        // 1. Employee Type
        if (!checkRequired(tilEmployeeType, val(spinnerEmployeeType), "Select employee type")) {
            isValid = false;
            if (firstErrorView == null) firstErrorView = tilEmployeeType;
        }

        // 2. Employee Code
        if (!checkRequired(tilEmployeeCode, val(etEmployeeCode), "Enter employee code")) {
            isValid = false;
            if (firstErrorView == null) firstErrorView = tilEmployeeCode;
        }

        // 3. First Name
        if (!checkRequired(tilFirstName, val(etFirstName), "Enter first name")) {
            isValid = false;
            if (firstErrorView == null) firstErrorView = tilFirstName;
        }

        // 4. Last Name
        if (!checkRequired(tilLastName, val(etLastName), "Enter last name")) {
            isValid = false;
            if (firstErrorView == null) firstErrorView = tilLastName;
        }

        // 5. Email
        String email = val(etEmail);
        if (!checkRequired(tilEmail, email, "Enter email")) {
            isValid = false;
            if (firstErrorView == null) firstErrorView = tilEmail;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Enter a valid email");
            tilEmail.setErrorEnabled(true);
            isValid = false;
            if (firstErrorView == null) firstErrorView = tilEmail;
        }

        // 6. Contact
        String contact = val(etContact);
        if (!checkRequired(tilContact, contact, "Enter contact number")) {
            isValid = false;
            if (firstErrorView == null) firstErrorView = tilContact;
        } else if (contact.replaceAll("[^0-9]", "").length() < 10) {
            tilContact.setError("Enter a valid 10-digit contact");
            tilContact.setErrorEnabled(true);
            isValid = false;
            if (firstErrorView == null) firstErrorView = tilContact;
        }

        // 7. Level
        if (!checkRequired(tilLevel, val(etLevel), "Enter level")) {
            isValid = false;
            if (firstErrorView == null) firstErrorView = tilLevel;
        }

        // 8. Grade
        if (!checkRequired(tilGrade, val(etGrade), "Enter grade")) {
            isValid = false;
            if (firstErrorView == null) firstErrorView = tilGrade;
        }

        // 9. Date of Birth
        if (!checkRequired(tilDOB, val(etDOB), "Select date of birth")) {
            isValid = false;
            if (firstErrorView == null) firstErrorView = tilDOB;
        }

        // 10. Joining Date
        if (!checkRequired(tilJoiningDate, val(etJoiningDate), "Select joining date")) {
            isValid = false;
            if (firstErrorView == null) firstErrorView = tilJoiningDate;
        }

        // 11. Joining Type
        if (!checkRequired(tilJoiningType, val(spinnerJoiningType), "Select joining type")) {
            isValid = false;
            if (firstErrorView == null) firstErrorView = tilJoiningType;
        }

        // 12. Reporting Manager
        if (!checkRequired(tilReportingManager, val(spinnerReportingManager), "Select reporting manager")) {
            isValid = false;
            if (firstErrorView == null) firstErrorView = tilReportingManager;
        }

        if (!isValid && firstErrorView != null) {
            scrollToView(firstErrorView);
        }

        return isValid;
    }

    // ════════════════════════════════════════════════════════════
    //  SUBMIT
    // ════════════════════════════════════════════════════════════

    private void onSubmitClicked() {
        if (!validateForm()) {
            Toast.makeText(requireContext(), "Please fill all mandatory fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Build request body
        AddEmployeeRequest request = new AddEmployeeRequest();

        // Mandatory fields
        request.setEmployeeType(mapEmployeeTypeToApi(val(spinnerEmployeeType)));
        request.setEmployeeCode(val(etEmployeeCode));
        request.setFirstname(val(etFirstName));
        request.setLastname(val(etLastName));
        request.setEmail(val(etEmail));
        request.setContact(val(etContact));
        request.setLevel(val(etLevel));
        request.setGrade(val(etGrade));
        request.setDateOfBirth(convertToApiDate(val(etDOB)));
        request.setJoiningDate(convertToApiDate(val(etJoiningDate)));
        request.setJoiningType(val(spinnerJoiningType).toLowerCase());

        EmployeesItem rm = mViewModel.getSelectedReportingManager();
        request.setReportingManager(rm != null ? rm.getId() : "");

        // Optional fields
        String status = val(spinnerStatus);
        request.setActive(status.isEmpty() || status.equalsIgnoreCase("Active"));

        request.setFatherName(val(etFatherName));
        request.setDesignation(val(etDesignation));
        request.setPanNo(val(etPAN));
        request.setAdharNo(val(etAadhaar));
        request.setDifferentlyAbled(val(spinnerDifferentlyAbledType));
        request.setAddress(val(etResidentialAddress));
        request.setState(val(etState));
        request.setPincode(val(etPinCode));

        // Cross-Functional Manager
        EmployeesItem cm = mViewModel.getSelectedCrossManager();
        request.setCrossmanager(cm != null ? cm.getId() : "");

        // Department + Company
        DepartmentsItem dept = mViewModel.getSelectedDepartment();
        if (dept != null) {
            request.setDepartment(dept.getId());
            request.setCompany(dept.getCompany() != null ? dept.getCompany() : "");
        } else {
            request.setDepartment("");
            request.setCompany("");
        }

        // Business Unit
        BusItem bu = mViewModel.getSelectedBu();
        request.setBu(bu != null ? bu.getId() : "");

        // Shift
        ShiftsItem shift = mViewModel.getSelectedShift();
        request.setShift(shift != null ? shift.getId() : "");

        // Partner
        if (tilPartner.getVisibility() == View.VISIBLE) {
            PartnersItem partner = mViewModel.getSelectedPartner();
            request.setPartner(partner != null ? partner.getId() : "");
        } else {
            request.setPartner("");
        }

        // HR/Admin (String field)
        request.setIsHROrAdmin(switchIsHROrAdmin.isChecked() ? "true" : "");

        // Fixed defaults (matching your raw payload)
        request.setIsVerified(false);
        request.setEpf(false);
        request.setEsi(false);
        request.setCtc(0);
        request.setPfAccountNo("");
        request.setUanno("");
        request.setEsiNo("");
        request.setAddpayroll(null);
        request.setTotalMonthlySalary(null);
        request.setTotalYearlySalary(null);
        request.setSalaryComponents(new ArrayList<>());

        // Default salary components
        request.setComponents(buildDefaultComponents());

        // Call API via ViewModel
        mViewModel.setLoading(true);

        mViewModel.createEmployee(token, request, new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (!isAdded()) return;
                mViewModel.setLoading(false);

                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Employee created successfully!", Toast.LENGTH_SHORT).show();
                    try {
                        NavController navController = Navigation.findNavController(requireView());
                        navController.popBackStack();
                    } catch (Exception e) {
                        // Navigation failed — stay on screen
                    }
                } else {
                    String errorMsg = "Failed to create employee";
                    try {
                        if (response.errorBody() != null) {
                            errorMsg = response.errorBody().string();
                        }
                    } catch (Exception ignored) {
                    }
                    Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                mViewModel.setLoading(false);
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ════════════════════════════════════════════════════════════
    //  DEFAULT COMPONENTS
    // ════════════════════════════════════════════════════════════

    private List<ComponentsItem> buildDefaultComponents() {
        ComponentsItem basic = new ComponentsItem();
        basic.setId("685bbebb528dc12b11868481");
        basic.setEarningname("Basic");
        basic.setCalculationType("ctcPercentage");
        basic.setComponentCalculationType("CTC");
        basic.setComponentType("earning");
        basic.setPercentage(50);
        basic.setFixedAmount(null);
        basic.setMonthlyAmount(0);
        basic.setYearlyAmount(0);

        ComponentsItem hra = new ComponentsItem();
        hra.setId("685bbec8528dc12b11868482");
        hra.setEarningname("HRA");
        hra.setCalculationType("ctcPercentage");
        hra.setComponentCalculationType("Basic");
        hra.setComponentType("earning");
        hra.setPercentage(50);
        hra.setFixedAmount(null);
        hra.setMonthlyAmount(0);
        hra.setYearlyAmount(0);

        ComponentsItem fixedAllowance = new ComponentsItem();
        fixedAllowance.setId("685bbef8528dc12b11868483");
        fixedAllowance.setEarningname("Fixed Allowance");
        fixedAllowance.setCalculationType("flatamount");
        fixedAllowance.setComponentCalculationType(null);
        fixedAllowance.setComponentType("earning");
        fixedAllowance.setPercentage(0);
        fixedAllowance.setFixedAmount(0);
        fixedAllowance.setMonthlyAmount(0);
        fixedAllowance.setYearlyAmount(0);

        ComponentsItem pf = new ComponentsItem();
        pf.setId("6863a6e14e4ea0675a318900");
        pf.setEarningname("PF");
        pf.setCalculationType("flatamount");
        pf.setComponentCalculationType(null);
        pf.setComponentType("deductions");
        pf.setPercentage(0);
        pf.setFixedAmount(1800);
        pf.setMonthlyAmount(0);
        pf.setYearlyAmount(0);

        return Arrays.asList(basic, hra, fixedAllowance, pf);
    }

    // ════════════════════════════════════════════════════════════
    //  LOADING STATE
    // ════════════════════════════════════════════════════════════

    private void setLoadingState(boolean loading) {
        submitProgressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnSubmit.setEnabled(!loading);
        btnClear.setEnabled(!loading);
        btnSubmit.setText(loading ? "" : "Submit");
    }

    // ════════════════════════════════════════════════════════════
    //  HELPERS
    // ════════════════════════════════════════════════════════════

    private String val(AutoCompleteTextView view) {
        return view.getText() != null ? view.getText().toString().trim() : "";
    }

    private String val(TextInputEditText view) {
        return view.getText() != null ? view.getText().toString().trim() : "";
    }

    private boolean checkRequired(TextInputLayout til, String value, String errorMsg) {
        if (value.isEmpty()) {
            til.setError(errorMsg);
            til.setErrorEnabled(true);
            return false;
        }
        clearTilError(til);
        return true;
    }

    private void clearTilError(TextInputLayout... layouts) {
        for (TextInputLayout til : layouts) {
            til.setError(null);
            til.setErrorEnabled(false);
        }
    }

    private void clearErrorOnChange(AutoCompleteTextView input, TextInputLayout til) {
        input.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) clearTilError(til);
            }
        });
    }

    private void clearErrorOnChange(TextInputEditText input, TextInputLayout til) {
        input.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) clearTilError(til);
            }
        });
    }

    private void scrollToView(View view) {
        scrollViewEmployeeDetails.post(() -> {
            int scrollY = 0;
            View current = view;
            while (current != null && current != scrollViewEmployeeDetails) {
                scrollY += current.getTop();
                if (current.getParent() instanceof View) {
                    current = (View) current.getParent();
                } else {
                    break;
                }
            }
            scrollViewEmployeeDetails.smoothScrollTo(0, Math.max(0, scrollY - 50));
        });
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

    private String buildShiftDisplay(String name, String start, String end) {
        String n = (name != null) ? name.trim() : "";
        if (!TextUtils.isEmpty(start) && !TextUtils.isEmpty(end)) {
            return n + " (" + start.trim() + " - " + end.trim() + ")";
        }
        return n;
    }

    private boolean isBusActive(String activeValue) {
        if (TextUtils.isEmpty(activeValue)) return false;
        String v = activeValue.trim().toLowerCase();
        return v.equals("true") || v.equals("active") || v.equals("1") || v.equals("yes");
    }

    private String mapEmployeeTypeToApi(String displayValue) {
        if ("Internal Employee".equalsIgnoreCase(displayValue)) return "employee";
        if ("Partner".equalsIgnoreCase(displayValue)) return "partner";
        return displayValue.toLowerCase();
    }

    /**
     * Converts display date "dd/MM/yyyy" to API date "yyyy-MM-dd".
     * Returns empty string if parsing fails.
     */
    private String convertToApiDate(String displayDate) {
        if (TextUtils.isEmpty(displayDate)) return "";
        try {
            Date date = displayDateFormat.parse(displayDate);
            if (date != null) return apiDateFormat.format(date);
        } catch (Exception ignored) {
        }
        return displayDate;
    }

    private abstract static class SimpleTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
    }
}
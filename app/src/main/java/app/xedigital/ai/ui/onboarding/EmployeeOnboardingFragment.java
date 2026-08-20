package app.xedigital.ai.ui.onboarding;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import app.xedigital.ai.R;
import app.xedigital.ai.model.employeeOnboarding.DocumentsItem;
import app.xedigital.ai.model.employeeOnboarding.EmployeeOnBoardDetails;
import app.xedigital.ai.model.employeeOnboarding.FamilyDetailsItem;
import app.xedigital.ai.ui.onboarding.adapter.FamilyMemberAdapter;
import app.xedigital.ai.ui.onboarding.adapter.OtherDocumentAdapter;
import app.xedigital.ai.ui.onboarding.model.FamilyMember;
import app.xedigital.ai.ui.onboarding.model.OnboardingFormModel;
import app.xedigital.ai.ui.onboarding.model.OtherDocument;
import app.xedigital.ai.utills.SecurePrefManager;

public class EmployeeOnboardingFragment extends Fragment {

    private static final int REQ_AADHAAR_FRONT = 1001;
    private static final int REQ_AADHAAR_BACK = 1002;
    private static final int REQ_PAN = 1003;
    private static final int REQ_FAMILY_BASE = 2000;
    private static final int REQ_OTHER_BASE = 3000;

    private static final long MAX_FILE_SIZE = 500 * 1024;
    private static final String[] GENDERS = {"Male", "Female", "Other"};
    private static final String[] MARITAL_STATUSES = {"Single", "Married"};
    private static final String[] EMPLOYMENT_TYPES = {"Full Time", "Part Time", "Intern", "Contract"};
    private static final String[] MEDICAL_CONDITIONS = {"No", "Yes"};

    private EmployeeOnboardingViewModel mViewModel;
    private OnboardingFormModel formModel;
    private boolean submitted = false;
    private String existingAadhaarFrontURL = "";
    private String existingAadhaarBackURL = "";
    private String existingPanURL = "";

    private ScrollView scrollContent;
    private ProgressBar progressBar;

    private TextInputLayout tilFullName, tilDob, tilGender, tilPersonalMobileLayout, tilPersonalEmail;
    private TextInputEditText etFullName, etDob, etFatherOrHusbandName, etBloodGroup, etNationality;
    private AutoCompleteTextView spGender, spMaritalStatus;
    private TextInputEditText etPersonalMobile, etAlternateMobile, etPersonalEmail, etCurrentAddress, etPermanentAddress, etCity, etState, etPincode, etCountry;
    private TextInputEditText etEmergencyName, etEmergencyRelationship, etEmergencyNumber, etEmergencyAlternateNumber, etEmergencyAddress;
    private TextInputEditText etEmployeeId, etDateOfJoining, etDesignation, etDepartment, etGrade, etReportingManager, etWorkLocation;
    private AutoCompleteTextView spEmploymentType;
    private TextInputEditText etQualification, etInstitution, etYearOfPassing, etPercentageGrade;
    private TextInputEditText etPreviousEmployer, etPreviousDesignation, etPreviousDuration, etLastDrawnCtc, etRelievingDate, etReasonForLeaving;
    private TextInputLayout tilAccountNumber, tilUanNumber, tilEsiNumber, tilPassportNumber;
    private TextInputEditText etBankName, etBranchName, etAccountNumber, etIfscCode, etAccountHolderName, etUpiId;
    private TextInputEditText etUanNumber, etEsiNumber, etPassportNumber, etPassportExpiryDate;
    private TextInputEditText etNomineeName, etNomineeRelationship, etNomineeDob, etSharePercentage;
    private AutoCompleteTextView spMedicalCondition;
    private TextInputLayout tilMedicalConditionDetails;
    private TextInputEditText etMedicalConditionDetails, etKnownAllergies;

    private RecyclerView rvFamilyDetails;
    private FamilyMemberAdapter familyAdapter;
    private List<FamilyMember> familyDetailsList;
    private Button btnAddFamilyMember;

    private TextInputLayout tilAadhaarNumber, tilPanNumber;
    private TextInputEditText etAadhaarNumber, etPanNumber;
    private Button btnAadhaarFront;
    private TextView tvAadhaarFrontFileName;
    private Button btnAadhaarBack;
    private TextView tvAadhaarBackFileName;
    private Button btnPanFile;
    private TextView tvPanFileName;
    private TextView tvAadhaarFrontError, tvAadhaarBackError, tvPanError;

    private RecyclerView rvDocuments;
    private OtherDocumentAdapter otherDocAdapter;
    private List<OtherDocument> documentsList;
    private Button btnAddDocument;
    private Button btnSave, btnReset, btnSubmit;

    private ActivityResultLauncher<Intent> filePickerLauncher;
    private int currentFileRequest = -1;

    public static EmployeeOnboardingFragment newInstance() {
        return new EmployeeOnboardingFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_employee_onboarding, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mViewModel = new ViewModelProvider(this).get(EmployeeOnboardingViewModel.class);

        formModel = new OnboardingFormModel();
        familyDetailsList = new ArrayList<>();
        documentsList = new ArrayList<>();

        familyDetailsList.add(new FamilyMember());
        documentsList.add(new OtherDocument());
        documentsList.add(new OtherDocument());

        initViews(view);
        setupDropdowns();
        setupDatePickers();
        registerFilePicker();
        setupRecyclerViews();
        setupClickListeners();
        setupMedicalConditionToggle();
        observeViewModel();

        loadCurrentOnboardingDetails();
    }

    private void initViews(View v) {
        scrollContent = v.findViewById(R.id.scroll_content);
        progressBar = v.findViewById(R.id.progress_bar);

        // Section 1 – Personal Details
        tilFullName = v.findViewById(R.id.til_full_name);
        etFullName = v.findViewById(R.id.et_full_name);
        etDob = v.findViewById(R.id.et_dob);
        spGender = v.findViewById(R.id.sp_gender);
        etFatherOrHusbandName = v.findViewById(R.id.et_father_or_husband_name);
        spMaritalStatus = v.findViewById(R.id.sp_marital_status);
        etBloodGroup = v.findViewById(R.id.et_blood_group);
        etNationality = v.findViewById(R.id.et_nationality);

        // Section 2 – Contact Details
        tilPersonalMobileLayout = v.findViewById(R.id.til_personal_mobile);
        tilPersonalEmail = v.findViewById(R.id.til_personal_email);
        etPersonalMobile = v.findViewById(R.id.et_personal_mobile);
        etAlternateMobile = v.findViewById(R.id.et_alternate_mobile);
        etPersonalEmail = v.findViewById(R.id.et_personal_email);
        etCurrentAddress = v.findViewById(R.id.et_current_address);
        etPermanentAddress = v.findViewById(R.id.et_permanent_address);
        etCity = v.findViewById(R.id.et_city);
        etState = v.findViewById(R.id.et_state);
        etPincode = v.findViewById(R.id.et_pincode);
        etCountry = v.findViewById(R.id.et_country);
        etCountry.setText("India");

        // Section 3 – Emergency
        etEmergencyName = v.findViewById(R.id.et_emergency_name);
        etEmergencyRelationship = v.findViewById(R.id.et_emergency_relationship);
        etEmergencyNumber = v.findViewById(R.id.et_emergency_number);
        etEmergencyAlternateNumber = v.findViewById(R.id.et_emergency_alternate_number);
        etEmergencyAddress = v.findViewById(R.id.et_emergency_address);

        // Section 4 – Employment
        etEmployeeId = v.findViewById(R.id.et_employee_id);
        etDateOfJoining = v.findViewById(R.id.et_date_of_joining);
        spEmploymentType = v.findViewById(R.id.sp_employment_type);
        etDesignation = v.findViewById(R.id.et_designation);
        etDepartment = v.findViewById(R.id.et_department);
        etGrade = v.findViewById(R.id.et_grade);
        etReportingManager = v.findViewById(R.id.et_reporting_manager);
        etWorkLocation = v.findViewById(R.id.et_work_location);

        // Section 5 – Education
        etQualification = v.findViewById(R.id.et_qualification);
        etInstitution = v.findViewById(R.id.et_institution);
        etYearOfPassing = v.findViewById(R.id.et_year_of_passing);
        etPercentageGrade = v.findViewById(R.id.et_percentage_grade);

        // Section 6 – Previous Employment
        etPreviousEmployer = v.findViewById(R.id.et_previous_employer);
        etPreviousDesignation = v.findViewById(R.id.et_previous_designation);
        etPreviousDuration = v.findViewById(R.id.et_previous_duration);
        etLastDrawnCtc = v.findViewById(R.id.et_last_drawn_ctc);
        etRelievingDate = v.findViewById(R.id.et_relieving_date);
        etReasonForLeaving = v.findViewById(R.id.et_reason_for_leaving);

        // Section 7 – Bank
        etBankName = v.findViewById(R.id.et_bank_name);
        etBranchName = v.findViewById(R.id.et_branch_name);
        etAccountNumber = v.findViewById(R.id.et_account_number);
        etIfscCode = v.findViewById(R.id.et_ifsc_code);
        etAccountHolderName = v.findViewById(R.id.et_account_holder_name);
        etUpiId = v.findViewById(R.id.et_upi_id);

        // Section 8 – Statutory
        etUanNumber = v.findViewById(R.id.et_uan_number);
        etEsiNumber = v.findViewById(R.id.et_esi_number);
        etPassportNumber = v.findViewById(R.id.et_passport_number);
        etPassportExpiryDate = v.findViewById(R.id.et_passport_expiry_date);

        // Section 9 – Nominee
        etNomineeName = v.findViewById(R.id.et_nominee_name);
        etNomineeRelationship = v.findViewById(R.id.et_nominee_relationship);
        etNomineeDob = v.findViewById(R.id.et_nominee_dob);
        etSharePercentage = v.findViewById(R.id.et_share_percentage);

        // Section 10 – Medical
        spMedicalCondition = v.findViewById(R.id.sp_medical_condition);
        tilMedicalConditionDetails = v.findViewById(R.id.til_medical_condition_details);
        etMedicalConditionDetails = v.findViewById(R.id.et_medical_condition_details);
        etKnownAllergies = v.findViewById(R.id.et_known_allergies);

        // Family Details
        rvFamilyDetails = v.findViewById(R.id.rv_family_details);
        btnAddFamilyMember = v.findViewById(R.id.btn_add_family_member);

        // Section 11 – Documents
        tilAadhaarNumber = v.findViewById(R.id.til_aadhaar_number);
        etAadhaarNumber = v.findViewById(R.id.et_aadhaar_number);
        btnAadhaarFront = v.findViewById(R.id.btn_aadhaar_front);
        tvAadhaarFrontFileName = v.findViewById(R.id.tv_aadhaar_front_file_name);
        tvAadhaarFrontError = v.findViewById(R.id.tv_aadhaar_front_error);
        btnAadhaarBack = v.findViewById(R.id.btn_aadhaar_back);
        tvAadhaarBackFileName = v.findViewById(R.id.tv_aadhaar_back_file_name);
        tvAadhaarBackError = v.findViewById(R.id.tv_aadhaar_back_error);
        tilPanNumber = v.findViewById(R.id.til_pan_number);
        etPanNumber = v.findViewById(R.id.et_pan_number);
        btnPanFile = v.findViewById(R.id.btn_pan_file);
        tvPanFileName = v.findViewById(R.id.tv_pan_file_name);
        tvPanError = v.findViewById(R.id.tv_pan_error);

        rvDocuments = v.findViewById(R.id.rv_documents);
        btnAddDocument = v.findViewById(R.id.btn_add_document);

        btnSave = v.findViewById(R.id.btn_save);
        btnReset = v.findViewById(R.id.btn_reset);
        btnSubmit = v.findViewById(R.id.btn_submit);
    }

    private void setupDropdowns() {
        bindDropdown(spGender, GENDERS);
        bindDropdown(spMaritalStatus, MARITAL_STATUSES);
        bindDropdown(spEmploymentType, EMPLOYMENT_TYPES);
        bindDropdown(spMedicalCondition, MEDICAL_CONDITIONS);
    }

    private void bindDropdown(AutoCompleteTextView view, String[] items) {
        view.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, items));
    }

    private void setupDatePickers() {
        makeDatePicker(etDob, true);
        makeDatePicker(etDateOfJoining, false);
        makeDatePicker(etRelievingDate, false);
        makeDatePicker(etPassportExpiryDate, false);
        makeDatePicker(etNomineeDob, true);
    }

    private void makeDatePicker(TextInputEditText field, boolean pastOnly) {
        field.setFocusable(false);
        field.setClickable(true);
        field.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            DatePickerDialog dlg = new DatePickerDialog(requireContext(), (dp, y, m, d) -> field.setText(String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d)), c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
            if (pastOnly) dlg.getDatePicker().setMaxDate(System.currentTimeMillis());
            dlg.show();
        });
    }

    private void loadCurrentOnboardingDetails() {
        SecurePrefManager prefs = SecurePrefManager.getInstance(requireContext());
        String token = prefs.getString("authToken", "");
        String userId = prefs.getString("userId", "");
        if (!token.isEmpty() && !userId.isEmpty()) {
            mViewModel.fetchOnboardingDetails("jwt " + token, userId);
        }
    }

    private void observeViewModel() {
        mViewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
            if (loading != null) {
                if (progressBar != null)
                    progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
                if (scrollContent != null) scrollContent.setAlpha(loading ? 0.35f : 1.0f);
            }
        });

        mViewModel.getFetchResponse().observe(getViewLifecycleOwner(), response -> {
            if (response != null && response.isSuccess() && response.getData() != null) {
                EmployeeOnBoardDetails details = response.getData().getEmployeeOnBoardDetails();
                if (details != null) {
                    if (details.isStatus()) {
                        // "status": true -> Locked. Show styled non-dismissible alert to view details.
                        showSubmittedAlert();
                    } else {
                        // "status": false -> Open for review/resubmission. Pre-fill form.
                        patchForm(details);
                    }
                }
            }
        });

        mViewModel.getSubmitSuccess().observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(requireContext(), "Onboarding submitted successfully!", Toast.LENGTH_LONG).show();
                navigateToViewDetails();
            }
        });

        mViewModel.getErrorMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Shows a beautifully designed, non-cancelable Material dialog
     * informing the user that their onboarding form is locked.
     */
    private void showSubmittedAlert() {
        if (getContext() == null || !isAdded()) return;

        View customLayout = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_onboarding_submitted, null);

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(customLayout)
                .setCancelable(false) // Blocks dismissing by tapping outside or back button
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        // Bind button action
        Button btnViewDetails = customLayout.findViewById(R.id.btn_dialog_view_details);
        btnViewDetails.setOnClickListener(v -> {
            dialog.dismiss();
            navigateToViewDetails();
        });

        dialog.show();
    }

    private void lockFormInputs() {
        btnSubmit.setEnabled(false);
        btnSubmit.setText("Already Submitted (Read Only)");
        btnSave.setEnabled(false);
        btnReset.setEnabled(false);
        btnAddFamilyMember.setEnabled(false);
        btnAddDocument.setEnabled(false);
        btnAadhaarFront.setEnabled(false);
        btnAadhaarBack.setEnabled(false);
        btnPanFile.setEnabled(false);
    }

    private void navigateToViewDetails() {
        if (!isAdded()) return;

        try {
            NavController navController = NavHostFragment.findNavController(this);
            // Uses your exact action ID
            navController.navigate(R.id.action_nav_onboarding_to_nav_onboarding_details);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Navigation failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void patchForm(EmployeeOnBoardDetails data) {
        if (data == null) return;

        etFullName.setText(safe(data.getFullName()));
        etDob.setText(formatToFormDate(data.getDob()));
        spGender.setText(safe(data.getGender()), false);
        etFatherOrHusbandName.setText(safe(data.getFatherOrHusbandName()));
        spMaritalStatus.setText(safe(data.getMaritalStatus()), false);
        etBloodGroup.setText(safe(data.getBloodGroup()));
        etNationality.setText(safe(data.getNationality()));

        etPersonalMobile.setText(safe(data.getPersonalMobile()));
        etAlternateMobile.setText(safe(data.getAlternateMobile()));
        etPersonalEmail.setText(safe(data.getPersonalEmail()));
        etCurrentAddress.setText(safe(data.getCurrentAddress()));
        etPermanentAddress.setText(safe(data.getPermanentAddress()));
        etCity.setText(safe(data.getCity()));
        etState.setText(safe(data.getState()));
        etPincode.setText(safe(data.getPincode()));
        etCountry.setText(safe(data.getCountry()).isEmpty() ? "India" : data.getCountry());

        etEmergencyName.setText(safe(data.getEmergencyName()));
        etEmergencyRelationship.setText(safe(data.getEmergencyRelationship()));
        etEmergencyNumber.setText(safe(data.getEmergencyNumber()));
        etEmergencyAlternateNumber.setText(safe(data.getEmergencyAlternateNumber()));
        etEmergencyAddress.setText(safe(data.getEmergencyAddress()));

        etEmployeeId.setText(safe(data.getEmployeeId()));
        etDateOfJoining.setText(formatToFormDate(data.getDateOfJoining()));
        spEmploymentType.setText(safe(data.getEmploymentType()), false);
        etDesignation.setText(safe(data.getDesignation()));
        etDepartment.setText(safe(data.getDepartment()));
        etGrade.setText(safe(data.getGrade()));
        etReportingManager.setText(safe(data.getReportingManager()));
        etWorkLocation.setText(safe(data.getWorkLocation()));

        etQualification.setText(safe(data.getQualification()));
        etInstitution.setText(safe(data.getInstitution()));
        etYearOfPassing.setText(safe(data.getYearOfPassing()));
        etPercentageGrade.setText(safe(data.getPercentageGrade()));

        etPreviousEmployer.setText(safe(data.getPreviousEmployer()));
        etPreviousDesignation.setText(safe(data.getPreviousDesignation()));
        etPreviousDuration.setText(safe(data.getPreviousDuration()));
        etLastDrawnCtc.setText(safe(data.getLastDrawnCtc()));
        etRelievingDate.setText("");
        etReasonForLeaving.setText(safe(data.getReasonForLeaving()));

        etBankName.setText(safe(data.getBankName()));
        etBranchName.setText(safe(data.getBranchName()));
        etIfscCode.setText(safe(data.getIfscCode()));
        etAccountHolderName.setText(safe(data.getAccountHolderName()));
        etUpiId.setText(safe(data.getUpiId()));

        if (data.getAccountNumberMasked() != null && !data.getAccountNumberMasked().isEmpty()) {
            etAccountNumber.setHint("On file: " + data.getAccountNumberMasked());
        }

        if (data.getUanNumberMasked() != null && !data.getUanNumberMasked().isEmpty()) {
            etUanNumber.setHint("On file: " + data.getUanNumberMasked());
        }
        if (data.getEsiNumberMasked() != null && !data.getEsiNumberMasked().isEmpty()) {
            etEsiNumber.setHint("On file: " + data.getEsiNumberMasked());
        }
        if (data.getPassportNumberMasked() != null && !data.getPassportNumberMasked().isEmpty()) {
            etPassportNumber.setHint("On file: " + data.getPassportNumberMasked());
        }
        etPassportExpiryDate.setText(formatToFormDate(data.getPassportExpiryDate()));

        etNomineeName.setText(safe(data.getNomineeName()));
        etNomineeRelationship.setText(safe(data.getNomineeRelationship()));
        etNomineeDob.setText(formatToFormDate(data.getNomineeDob()));
        etSharePercentage.setText(safe(data.getSharePercentage()));

        String medCond = safe(data.getMedicalCondition());
        spMedicalCondition.setText(medCond.isEmpty() ? "No" : medCond, false);
        boolean isYes = "Yes".equalsIgnoreCase(medCond);
        tilMedicalConditionDetails.setVisibility(isYes ? View.VISIBLE : View.GONE);
        etMedicalConditionDetails.setText(safe(data.getMedicalConditionDetails()));
        etKnownAllergies.setText(safe(data.getKnownAllergies()));

        // Show masked Aadhaar / PAN info to let the user know they are already on file
        if (data.getAadhaarNumberMasked() != null && !data.getAadhaarNumberMasked().isEmpty()) {
            tilAadhaarNumber.setHelperText("Current: " + data.getAadhaarNumberMasked());
        }
        if (data.getPanNumberMasked() != null && !data.getPanNumberMasked().isEmpty()) {
            tilPanNumber.setHelperText("Current: " + data.getPanNumberMasked());
        }

        existingAadhaarFrontURL = safe(data.getAadhaarFrontFileURL());
        existingAadhaarBackURL = safe(data.getAadhaarBackFileURL());
        existingPanURL = safe(data.getPanFileURL());

        if (!existingAadhaarFrontURL.isEmpty()) tvAadhaarFrontFileName.setText("File on server ✓");
        if (!existingAadhaarBackURL.isEmpty()) tvAadhaarBackFileName.setText("File on server ✓");
        if (!existingPanURL.isEmpty()) tvPanFileName.setText("File on server ✓");

        familyDetailsList.clear();
        if (data.getFamilyDetails() != null && !data.getFamilyDetails().isEmpty()) {
            for (FamilyDetailsItem item : data.getFamilyDetails()) {
                FamilyMember m = new FamilyMember();
                m.setFamilyMemberName(safe(item.getFamilyMemberName()));
                m.setFamilyRelationship(safe(item.getFamilyRelationship()));
                m.setFamilyDob(formatToFormDate(item.getFamilyDob()));
                m.setFamilyMobile(safe(item.getFamilyMobile()));
                m.setAddressProofFileURL(safe(item.getAddressProofFileURL()));
                familyDetailsList.add(m);
            }
        } else {
            familyDetailsList.add(new FamilyMember());
        }
        familyAdapter.notifyDataSetChanged();

        documentsList.clear();
        if (data.getDocuments() != null && !data.getDocuments().isEmpty()) {
            for (DocumentsItem item : data.getDocuments()) {
                OtherDocument doc = new OtherDocument();
                doc.setDocumentName(safe(item.getDocumentName()));
                doc.setDocumentFileURL(safe(item.getDocumentFileURL()));
                documentsList.add(doc);
            }
        } else {
            documentsList.add(new OtherDocument());
            documentsList.add(new OtherDocument());
        }
        otherDocAdapter.notifyDataSetChanged();
    }

    private String formatToFormDate(Object dateObj) {
        if (dateObj == null) return "";
        String dateStr = dateObj.toString().trim();
        if (dateStr.isEmpty() || "null".equalsIgnoreCase(dateStr)) return "";

        // If it already matches yyyy-MM-dd
        if (dateStr.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
            return dateStr;
        }

        String[] parseFormats = {"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "yyyy-MM-dd'T'HH:mm:ss'Z'", "yyyy-MM-dd'T'HH:mm:ss.SSS", "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd", "dd-MM-yyyy"};

        for (String format : parseFormats) {
            try {
                SimpleDateFormat inFmt = new SimpleDateFormat(format, Locale.US);
                inFmt.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date parsed = inFmt.parse(dateStr);
                if (parsed != null) {
                    SimpleDateFormat outFmt = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                    return outFmt.format(parsed);
                }
            } catch (Exception ignored) {
            }
        }

        if (dateStr.contains("T")) {
            return dateStr.split("T")[0];
        }
        return dateStr;
    }

    private void registerFilePicker() {
        filePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() != Activity.RESULT_OK || result.getData() == null) return;
            Uri uri = result.getData().getData();
            if (uri == null || !validateFile(uri)) return;

            String name = resolveFileName(uri);

            if (currentFileRequest == REQ_AADHAAR_FRONT) {
                formModel.setAadhaarFrontFile(uri);
                formModel.setAadhaarFrontFileName(name);
                tvAadhaarFrontFileName.setText(name);
                tvAadhaarFrontError.setVisibility(View.GONE);
            } else if (currentFileRequest == REQ_AADHAAR_BACK) {
                formModel.setAadhaarBackFile(uri);
                formModel.setAadhaarBackFileName(name);
                tvAadhaarBackFileName.setText(name);
                tvAadhaarBackError.setVisibility(View.GONE);
            } else if (currentFileRequest == REQ_PAN) {
                formModel.setPanFile(uri);
                formModel.setPanFileName(name);
                tvPanFileName.setText(name);
                tvPanError.setVisibility(View.GONE);
            } else if (currentFileRequest >= REQ_FAMILY_BASE && currentFileRequest < REQ_OTHER_BASE) {
                int pos = currentFileRequest - REQ_FAMILY_BASE;
                familyDetailsList.get(pos).setAddressProofFile(uri);
                familyAdapter.updateFile(pos, name);
            } else if (currentFileRequest >= REQ_OTHER_BASE) {
                int pos = currentFileRequest - REQ_OTHER_BASE;
                documentsList.get(pos).setDocumentFile(uri);
                otherDocAdapter.updateFile(pos, name);
            }
        });
    }

    private boolean validateFile(Uri uri) {
        try {
            String mimeType = requireContext().getContentResolver().getType(uri);
            if (mimeType == null || (!mimeType.equals("image/jpeg") && !mimeType.equals("image/png"))) {
                Toast.makeText(requireContext(), "Only JPEG/PNG files are allowed.", Toast.LENGTH_LONG).show();
                return false;
            }

            try (Cursor cursor = requireContext().getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                    if (sizeIndex >= 0) {
                        long size = cursor.getLong(sizeIndex);
                        if (size > MAX_FILE_SIZE) {
                            Toast.makeText(requireContext(), "File size should be less than 500KB.", Toast.LENGTH_LONG).show();
                            return false;
                        }
                    }
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void launchPicker(int requestCode) {
        currentFileRequest = requestCode;
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/jpeg", "image/png"});
        filePickerLauncher.launch(Intent.createChooser(intent, "Select Image"));
    }

    private String resolveFileName(Uri uri) {
        String name = null;
        if ("content".equals(uri.getScheme())) {
            try (Cursor c = requireContext().getContentResolver().query(uri, null, null, null, null)) {
                if (c != null && c.moveToFirst()) {
                    int idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (idx >= 0) name = c.getString(idx);
                }
            } catch (Exception ignored) {
            }
        }
        if (name == null) name = uri.getLastPathSegment();
        return name != null ? name : "Selected File";
    }

    private void setupRecyclerViews() {
        familyAdapter = new FamilyMemberAdapter(requireContext(), familyDetailsList, pos -> launchPicker(REQ_FAMILY_BASE + pos));
        rvFamilyDetails.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvFamilyDetails.setAdapter(familyAdapter);
        rvFamilyDetails.setNestedScrollingEnabled(false);

        otherDocAdapter = new OtherDocumentAdapter(requireContext(), documentsList, pos -> launchPicker(REQ_OTHER_BASE + pos));
        rvDocuments.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvDocuments.setAdapter(otherDocAdapter);
        rvDocuments.setNestedScrollingEnabled(false);
    }

    private void setupClickListeners() {
        btnAadhaarFront.setOnClickListener(v -> launchPicker(REQ_AADHAAR_FRONT));
        btnAadhaarBack.setOnClickListener(v -> launchPicker(REQ_AADHAAR_BACK));
        btnPanFile.setOnClickListener(v -> launchPicker(REQ_PAN));

        btnAddFamilyMember.setOnClickListener(v -> {
            familyDetailsList.add(new FamilyMember());
            familyAdapter.notifyItemInserted(familyDetailsList.size() - 1);
            familyAdapter.notifyDataSetChanged();
        });

        btnAddDocument.setOnClickListener(v -> {
            documentsList.add(new OtherDocument());
            otherDocAdapter.notifyItemInserted(documentsList.size() - 1);
        });

        btnSave.setOnClickListener(v -> {
            submitted = false;
            collectFormData();
            mViewModel.saveDraft(formModel);
            Toast.makeText(requireContext(), "Saved Draft Successfully!", Toast.LENGTH_SHORT).show();
        });

        btnReset.setOnClickListener(v -> resetForm());

        btnSubmit.setOnClickListener(v -> {
            submitted = true;
            if (validateForm()) {
                collectFormData();
                mViewModel.submitForm(formModel);
            }
        });
    }

    private void setupMedicalConditionToggle() {
        spMedicalCondition.setOnItemClickListener((parent, view, pos, id) -> {
            String sel = (String) parent.getItemAtPosition(pos);
            boolean isYes = "Yes".equals(sel);
            tilMedicalConditionDetails.setVisibility(isYes ? View.VISIBLE : View.GONE);
            if (!isYes) etMedicalConditionDetails.setText("");
        });
    }

    private void collectFormData() {
        formModel.setFullName(get(etFullName));
        formModel.setDob(get(etDob));
        formModel.setGender(spGender.getText().toString().trim());
        formModel.setFatherOrHusbandName(get(etFatherOrHusbandName));
        formModel.setMaritalStatus(spMaritalStatus.getText().toString().trim());
        formModel.setBloodGroup(get(etBloodGroup));
        formModel.setNationality(get(etNationality));

        formModel.setPersonalMobile(get(etPersonalMobile));
        formModel.setAlternateMobile(get(etAlternateMobile));
        formModel.setPersonalEmail(get(etPersonalEmail));
        formModel.setCurrentAddress(get(etCurrentAddress));
        formModel.setPermanentAddress(get(etPermanentAddress));
        formModel.setCity(get(etCity));
        formModel.setState(get(etState));
        formModel.setPincode(get(etPincode));
        formModel.setCountry(get(etCountry));

        formModel.setEmergencyName(get(etEmergencyName));
        formModel.setEmergencyRelationship(get(etEmergencyRelationship));
        formModel.setEmergencyNumber(get(etEmergencyNumber));
        formModel.setEmergencyAlternateNumber(get(etEmergencyAlternateNumber));
        formModel.setEmergencyAddress(get(etEmergencyAddress));

        formModel.setEmployeeId(get(etEmployeeId));
        formModel.setDateOfJoining(get(etDateOfJoining));
        formModel.setEmploymentType(spEmploymentType.getText().toString().trim());
        formModel.setDesignation(get(etDesignation));
        formModel.setDepartment(get(etDepartment));
        formModel.setGrade(get(etGrade));
        formModel.setReportingManager(get(etReportingManager));
        formModel.setWorkLocation(get(etWorkLocation));

        formModel.setQualification(get(etQualification));
        formModel.setInstitution(get(etInstitution));
        formModel.setYearOfPassing(get(etYearOfPassing));
        formModel.setPercentageGrade(get(etPercentageGrade));

        formModel.setPreviousEmployer(get(etPreviousEmployer));
        formModel.setPreviousDesignation(get(etPreviousDesignation));
        formModel.setPreviousDuration(get(etPreviousDuration));
        formModel.setLastDrawnCtc(get(etLastDrawnCtc));
        formModel.setRelievingDate(get(etRelievingDate));
        formModel.setReasonForLeaving(get(etReasonForLeaving));

        formModel.setBankName(get(etBankName));
        formModel.setBranchName(get(etBranchName));
        formModel.setAccountNumber(get(etAccountNumber));
        formModel.setIfscCode(get(etIfscCode));
        formModel.setAccountHolderName(get(etAccountHolderName));
        formModel.setUpiId(get(etUpiId));

        formModel.setUanNumber(get(etUanNumber));
        formModel.setEsiNumber(get(etEsiNumber));
        formModel.setPassportNumber(get(etPassportNumber));
        formModel.setPassportExpiryDate(get(etPassportExpiryDate));

        formModel.setNomineeName(get(etNomineeName));
        formModel.setNomineeRelationship(get(etNomineeRelationship));
        formModel.setNomineeDob(get(etNomineeDob));
        formModel.setSharePercentage(get(etSharePercentage));

        formModel.setMedicalCondition(spMedicalCondition.getText().toString().trim());
        formModel.setMedicalConditionDetails(get(etMedicalConditionDetails));
        formModel.setKnownAllergies(get(etKnownAllergies));

        formModel.setFamilyDetails(familyDetailsList);
        formModel.setAadhaarNumber(get(etAadhaarNumber));
        formModel.setPanNumber(get(etPanNumber).toUpperCase());
        formModel.setDocuments(documentsList);
    }

    private boolean validateForm() {
        boolean valid = true;
        clearAllErrors();

        if (get(etFullName).isEmpty()) {
            tilFullName.setError("Full Name is required");
            if (valid) etFullName.requestFocus();
            valid = false;
        }
        if (get(etDob).isEmpty()) {
            etDob.setError("Date of Birth is required");
            valid = false;
        }
        if (spGender.getText().toString().trim().isEmpty()) {
            spGender.setError("Gender is required");
            valid = false;
        }

        String mob = get(etPersonalMobile);
        if (mob.isEmpty()) {
            tilPersonalMobileLayout.setError("Personal Mobile is required");
            valid = false;
        } else if (!mob.matches("^[6-9][0-9]{9}$")) {
            tilPersonalMobileLayout.setError("Enter valid 10 digit mobile number");
            valid = false;
        }

        String email = get(etPersonalEmail);
        if (email.isEmpty()) {
            tilPersonalEmail.setError("Personal Email is required");
            valid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilPersonalEmail.setError("Enter valid email");
            valid = false;
        }

        String aadhaar = get(etAadhaarNumber);
        if (aadhaar.isEmpty() && existingAadhaarFrontURL.isEmpty()) {
            tilAadhaarNumber.setError("Aadhaar Number is required");
            valid = false;
        } else if (!aadhaar.isEmpty() && !aadhaar.matches("^[0-9]{12}$")) {
            tilAadhaarNumber.setError("Enter valid 12 digit Aadhaar Number");
            valid = false;
        }

        if (formModel.getAadhaarFrontFile() == null && existingAadhaarFrontURL.isEmpty()) {
            tvAadhaarFrontError.setVisibility(View.VISIBLE);
            tvAadhaarFrontError.setText("Aadhaar front file is required");
            valid = false;
        }
        if (formModel.getAadhaarBackFile() == null && existingAadhaarBackURL.isEmpty()) {
            tvAadhaarBackError.setVisibility(View.VISIBLE);
            tvAadhaarBackError.setText("Aadhaar back file is required");
            valid = false;
        }

        String pan = get(etPanNumber).toUpperCase();
        if (pan.isEmpty() && existingPanURL.isEmpty()) {
            tilPanNumber.setError("PAN Number is required");
            valid = false;
        } else if (!pan.isEmpty() && !pan.matches("^[A-Z]{5}[0-9]{4}[A-Z]{1}$")) {
            tilPanNumber.setError("Enter valid PAN Number");
            valid = false;
        }

        if (formModel.getPanFile() == null && existingPanURL.isEmpty()) {
            tvPanError.setVisibility(View.VISIBLE);
            tvPanError.setText("PAN document is required");
            valid = false;
        }

        valid = validateOtherDocuments() && valid;
        if (!valid) {
            Toast.makeText(requireContext(), "Please complete all mandatory fields.", Toast.LENGTH_LONG).show();
        }
        otherDocAdapter.setSubmitted(submitted);
        return valid;
    }

    private boolean validateOtherDocuments() {
        boolean valid = true;
        for (OtherDocument doc : documentsList) {
            boolean rowHasValue = !doc.getDocumentName().isEmpty() || doc.getDocumentFile() != null || !doc.getDocumentFileURL().isEmpty();
            if (rowHasValue && doc.getDocumentName().isEmpty()) {
                valid = false;
            }
            if (rowHasValue && doc.getDocumentFile() == null && doc.getDocumentFileURL().isEmpty()) {
                valid = false;
            }
        }
        return valid;
    }

    private void clearAllErrors() {
        tilFullName.setError(null);
        etDob.setError(null);
        spGender.setError(null);
        tilPersonalMobileLayout.setError(null);
        tilPersonalEmail.setError(null);
        tilAadhaarNumber.setError(null);
        tilPanNumber.setError(null);
        tvAadhaarFrontError.setVisibility(View.GONE);
        tvAadhaarBackError.setVisibility(View.GONE);
        tvPanError.setVisibility(View.GONE);
    }

    private void resetForm() {
        submitted = false;
        etFullName.setText("");
        etDob.setText("");
        spGender.setText("", false);
        etFatherOrHusbandName.setText("");
        spMaritalStatus.setText("", false);
        etBloodGroup.setText("");
        etNationality.setText("");
        etPersonalMobile.setText("");
        etAlternateMobile.setText("");
        etPersonalEmail.setText("");
        etCurrentAddress.setText("");
        etPermanentAddress.setText("");
        etCity.setText("");
        etState.setText("");
        etPincode.setText("");
        etCountry.setText("India");
        etEmergencyName.setText("");
        etEmergencyRelationship.setText("");
        etEmergencyNumber.setText("");
        etEmergencyAlternateNumber.setText("");
        etEmergencyAddress.setText("");
        etEmployeeId.setText("");
        etDateOfJoining.setText("");
        spEmploymentType.setText("", false);
        etDesignation.setText("");
        etDepartment.setText("");
        etGrade.setText("");
        etReportingManager.setText("");
        etWorkLocation.setText("");
        etQualification.setText("");
        etInstitution.setText("");
        etYearOfPassing.setText("");
        etPercentageGrade.setText("");
        etPreviousEmployer.setText("");
        etPreviousDesignation.setText("");
        etPreviousDuration.setText("");
        etLastDrawnCtc.setText("");
        etRelievingDate.setText("");
        etReasonForLeaving.setText("");
        etBankName.setText("");
        etBranchName.setText("");
        etAccountNumber.setText("");
        etIfscCode.setText("");
        etAccountHolderName.setText("");
        etUpiId.setText("");
        etUanNumber.setText("");
        etEsiNumber.setText("");
        etPassportNumber.setText("");
        etPassportExpiryDate.setText("");
        etNomineeName.setText("");
        etNomineeRelationship.setText("");
        etNomineeDob.setText("");
        etSharePercentage.setText("");
        spMedicalCondition.setText("", false);
        etMedicalConditionDetails.setText("");
        etKnownAllergies.setText("");
        etAadhaarNumber.setText("");
        etPanNumber.setText("");

        tilAadhaarNumber.setHelperText(null);
        tilPanNumber.setHelperText(null);

        tvAadhaarFrontFileName.setText("No file chosen");
        tvAadhaarBackFileName.setText("No file chosen");
        tvPanFileName.setText("No file chosen");
        existingAadhaarFrontURL = "";
        existingAadhaarBackURL = "";
        existingPanURL = "";

        familyDetailsList.clear();
        familyDetailsList.add(new FamilyMember());
        familyAdapter.notifyDataSetChanged();

        documentsList.clear();
        documentsList.add(new OtherDocument());
        documentsList.add(new OtherDocument());
        otherDocAdapter.notifyDataSetChanged();

        formModel = new OnboardingFormModel();
        clearAllErrors();
        tilMedicalConditionDetails.setVisibility(View.GONE);
    }

    private String get(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    private String safe(String value) {
        return value != null ? value.trim() : "";
    }
}
package app.xedigital.ai.ui.onboarding;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import app.xedigital.ai.R;
import app.xedigital.ai.model.employeeOnboarding.DocumentsItem;
import app.xedigital.ai.model.employeeOnboarding.EmployeeOnBoardDetails;
import app.xedigital.ai.model.employeeOnboarding.FamilyDetailsItem;
import app.xedigital.ai.utills.SecurePrefManager;

public class ViewOnboardingDetailsFragment extends Fragment {

    // ── ViewModel ─────────────────────────────────────────────────────────────
    private ViewOnboardingDetailsViewModel mViewModel;

    // ── Root views ────────────────────────────────────────────────────────────
    private ProgressBar progressBar;
    private TextView tvError;
    private ScrollView scrollContent;

    // ── Profile Header ────────────────────────────────────────────────────────
    private TextView tvAvatarChar;
    private TextView tvFullName;
    private TextView tvEmailHeader;
    private TextView tvStatusBadge;
    private TextView tvMobileHeader;
    private TextView tvEmployeeIdHeader;

    // ── Section 1: Personal Details ───────────────────────────────────────────
    private TextView tvDob;
    private TextView tvGender;
    private TextView tvFatherOrHusbandName;
    private TextView tvMaritalStatus;
    private TextView tvNationality;
    private TextView tvBloodGroup;

    // ── Section 2: Contact Details ────────────────────────────────────────────
    private TextView tvPersonalMobile;
    private TextView tvAlternateMobile;
    private TextView tvEmail;
    private TextView tvCurrentAddress;
    private TextView tvPermanentAddress;
    private TextView tvCity;
    private TextView tvState;
    private TextView tvPincode;
    private TextView tvCountry;

    // ── Section 3: Emergency Contact ──────────────────────────────────────────
    private TextView tvEmergencyName;
    private TextView tvEmergencyRelationship;
    private TextView tvEmergencyNumber;
    private TextView tvEmergencyAlternateNumber;
    private TextView tvEmergencyAddress;

    // ── Section 4: Employment Details ─────────────────────────────────────────
    private TextView tvDateOfJoining;
    private TextView tvDesignation;
    private TextView tvDepartment;
    private TextView tvReportingManager;
    private TextView tvWorkLocation;
    private TextView tvEmploymentType;
    private TextView tvGrade;
    private TextView tvEmployeeId;

    // ── Section 5: Education ──────────────────────────────────────────────────
    private TextView tvQualification;
    private TextView tvInstitution;
    private TextView tvYearOfPassing;
    private TextView tvPercentageGrade;

    // ── Section 6: Previous Employment ───────────────────────────────────────
    private TextView tvPreviousEmployer;
    private TextView tvPreviousDesignation;
    private TextView tvPreviousDuration;
    private TextView tvLastDrawnCtc;
    private TextView tvRelievingDate;
    private TextView tvReasonForLeaving;

    // ── Section 7: Bank Details ───────────────────────────────────────────────
    private TextView tvBankName;
    private TextView tvBranchName;
    private TextView tvAccountNumber;
    private TextView tvIfscCode;
    private TextView tvAccountHolderName;
    private TextView tvUpiId;

    // ── Section 8: Statutory Details ─────────────────────────────────────────
    private TextView tvUanNumber;
    private TextView tvEsiNumber;
    private TextView tvPassportNumber;
    private TextView tvPassportExpiryDate;

    // ── Section 9: Nominee Details ────────────────────────────────────────────
    private TextView tvNomineeName;
    private TextView tvNomineeRelationship;
    private TextView tvNomineeDob;
    private TextView tvSharePercentage;

    // ── Section 10: Medical & Health ──────────────────────────────────────────
    private TextView tvMedicalCondition;
    private TextView tvMedicalConditionDetails;
    private TextView tvKnownAllergies;

    // ── Section 11: Documents ─────────────────────────────────────────────────
    private TextView tvAadhaarNumberMasked;
    private TextView tvAadhaarFrontLink;
    private TextView tvAadhaarBackLink;
    private TextView tvPanNumberMasked;
    private TextView tvPanLink;

    // ── Family Details ────────────────────────────────────────────────────────
    private RecyclerView rvFamilyDetails;
    private LinearLayout llNoFamily;

    // ── Other Documents ───────────────────────────────────────────────────────
    private RecyclerView rvOtherDocuments;
    private LinearLayout llNoDocuments;

    // =========================================================================
    public static ViewOnboardingDetailsFragment newInstance() {
        return new ViewOnboardingDetailsFragment();
    }

    // ── onCreateView ──────────────────────────────────────────────────────────
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_view_onboarding_details, container, false);
    }

    // ── onViewCreated ─────────────────────────────────────────────────────────
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mViewModel = new ViewModelProvider(this).get(ViewOnboardingDetailsViewModel.class);

        initViews(view);
        setupRecyclerViews();
        observeViewModel();
        loadData();
    }

    // =========================================================================
    // INIT VIEWS
    // =========================================================================
    private void initViews(View v) {

        // Root
        progressBar = v.findViewById(R.id.progress_bar);
        tvError = v.findViewById(R.id.tv_error);
        scrollContent = v.findViewById(R.id.scroll_content);

        // Profile Header
        tvAvatarChar = v.findViewById(R.id.tv_avatar_char);
        tvFullName = v.findViewById(R.id.tv_full_name);
        tvEmailHeader = v.findViewById(R.id.tv_email_header);
        tvStatusBadge = v.findViewById(R.id.tv_status_badge);
        tvMobileHeader = v.findViewById(R.id.tv_mobile_header);
        tvEmployeeIdHeader = v.findViewById(R.id.tv_employee_id_header);

        // Section 1
        tvDob = v.findViewById(R.id.tv_dob);
        tvGender = v.findViewById(R.id.tv_gender);
        tvFatherOrHusbandName = v.findViewById(R.id.tv_father_or_husband_name);
        tvMaritalStatus = v.findViewById(R.id.tv_marital_status);
        tvNationality = v.findViewById(R.id.tv_nationality);
        tvBloodGroup = v.findViewById(R.id.tv_blood_group);

        // Section 2
        tvPersonalMobile = v.findViewById(R.id.tv_personal_mobile);
        tvAlternateMobile = v.findViewById(R.id.tv_alternate_mobile);
        tvEmail = v.findViewById(R.id.tv_email);
        tvCurrentAddress = v.findViewById(R.id.tv_current_address);
        tvPermanentAddress = v.findViewById(R.id.tv_permanent_address);
        tvCity = v.findViewById(R.id.tv_city);
        tvState = v.findViewById(R.id.tv_state);
        tvPincode = v.findViewById(R.id.tv_pincode);
        tvCountry = v.findViewById(R.id.tv_country);

        // Section 3
        tvEmergencyName = v.findViewById(R.id.tv_emergency_name);
        tvEmergencyRelationship = v.findViewById(R.id.tv_emergency_relationship);
        tvEmergencyNumber = v.findViewById(R.id.tv_emergency_number);
        tvEmergencyAlternateNumber = v.findViewById(R.id.tv_emergency_alternate_number);
        tvEmergencyAddress = v.findViewById(R.id.tv_emergency_address);

        // Section 4
        tvDateOfJoining = v.findViewById(R.id.tv_date_of_joining);
        tvDesignation = v.findViewById(R.id.tv_designation);
        tvDepartment = v.findViewById(R.id.tv_department);
        tvReportingManager = v.findViewById(R.id.tv_reporting_manager);
        tvWorkLocation = v.findViewById(R.id.tv_work_location);
        tvEmploymentType = v.findViewById(R.id.tv_employment_type);
        tvGrade = v.findViewById(R.id.tv_grade);
        tvEmployeeId = v.findViewById(R.id.tv_employee_id);

        // Section 5
        tvQualification = v.findViewById(R.id.tv_qualification);
        tvInstitution = v.findViewById(R.id.tv_institution);
        tvYearOfPassing = v.findViewById(R.id.tv_year_of_passing);
        tvPercentageGrade = v.findViewById(R.id.tv_percentage_grade);

        // Section 6
        tvPreviousEmployer = v.findViewById(R.id.tv_previous_employer);
        tvPreviousDesignation = v.findViewById(R.id.tv_previous_designation);
        tvPreviousDuration = v.findViewById(R.id.tv_previous_duration);
        tvLastDrawnCtc = v.findViewById(R.id.tv_last_drawn_ctc);
        tvRelievingDate = v.findViewById(R.id.tv_relieving_date);
        tvReasonForLeaving = v.findViewById(R.id.tv_reason_for_leaving);

        // Section 7
        tvBankName = v.findViewById(R.id.tv_bank_name);
        tvBranchName = v.findViewById(R.id.tv_branch_name);
        tvAccountNumber = v.findViewById(R.id.tv_account_number);
        tvIfscCode = v.findViewById(R.id.tv_ifsc_code);
        tvAccountHolderName = v.findViewById(R.id.tv_account_holder_name);
        tvUpiId = v.findViewById(R.id.tv_upi_id);

        // Section 8
        tvUanNumber = v.findViewById(R.id.tv_uan_number);
        tvEsiNumber = v.findViewById(R.id.tv_esi_number);
        tvPassportNumber = v.findViewById(R.id.tv_passport_number);
        tvPassportExpiryDate = v.findViewById(R.id.tv_passport_expiry_date);

        // Section 9
        tvNomineeName = v.findViewById(R.id.tv_nominee_name);
        tvNomineeRelationship = v.findViewById(R.id.tv_nominee_relationship);
        tvNomineeDob = v.findViewById(R.id.tv_nominee_dob);
        tvSharePercentage = v.findViewById(R.id.tv_share_percentage);

        // Section 10
        tvMedicalCondition = v.findViewById(R.id.tv_medical_condition);
        tvMedicalConditionDetails = v.findViewById(R.id.tv_medical_condition_details);
        tvKnownAllergies = v.findViewById(R.id.tv_known_allergies);

        // Section 11 – Documents
        tvAadhaarNumberMasked = v.findViewById(R.id.tv_aadhaar_number_masked);
        tvAadhaarFrontLink = v.findViewById(R.id.tv_aadhaar_front_link);
        tvAadhaarBackLink = v.findViewById(R.id.tv_aadhaar_back_link);
        tvPanNumberMasked = v.findViewById(R.id.tv_pan_number_masked);
        tvPanLink = v.findViewById(R.id.tv_pan_link);

        // Family & Other Docs lists
        rvFamilyDetails = v.findViewById(R.id.rv_family_details);
        llNoFamily = v.findViewById(R.id.ll_no_family);
        rvOtherDocuments = v.findViewById(R.id.rv_other_documents);
        llNoDocuments = v.findViewById(R.id.ll_no_documents);
    }

    // =========================================================================
    // SETUP RECYCLERVIEWS
    // =========================================================================
    private void setupRecyclerViews() {
        rvFamilyDetails.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvFamilyDetails.setNestedScrollingEnabled(false);

        rvOtherDocuments.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvOtherDocuments.setNestedScrollingEnabled(false);
    }

    // =========================================================================
    // LOAD DATA
    // Uses SecurePrefManager to get token & userId
    // =========================================================================
    private void loadData() {
        SecurePrefManager prefs = SecurePrefManager.getInstance(requireContext());

        // Get token – stored as "token" in your app
        String token = prefs.getString("authToken", "");
        String userId = prefs.getString("userId", "");

        if (token.isEmpty() || userId.isEmpty()) {
            showError("Session expired. Please login again.");
            return;
        }

        // Prefix "Bearer " to match your other API calls
        String authToken = "jwt " + token;

        mViewModel.fetchOnboardingDetails(authToken, userId);
    }

    // =========================================================================
    // OBSERVE VIEWMODEL
    // =========================================================================
    private void observeViewModel() {

        // Loading state
        mViewModel.isLoading.observe(getViewLifecycleOwner(), loading -> {
            if (Boolean.TRUE.equals(loading)) {
                progressBar.setVisibility(View.VISIBLE);
                scrollContent.setVisibility(View.GONE);
                tvError.setVisibility(View.GONE);
            } else {
                progressBar.setVisibility(View.GONE);
            }
        });

        // Error
        mViewModel.errorMessage.observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                showError(msg);
            }
        });

        // Success – uses your existing EmployeeOnboardingResponse model
        mViewModel.onboardingResponse.observe(getViewLifecycleOwner(), response -> {

            if (response == null) return;

            if (response.isSuccess() && response.getStatusCode() == 200 && response.getData() != null && response.getData().getEmployeeOnBoardDetails() != null) {

                scrollContent.setVisibility(View.VISIBLE);
                tvError.setVisibility(View.GONE);

                populateUI(response.getData().getEmployeeOnBoardDetails());

            } else {
                showError(response.getMessage() != null && !response.getMessage().isEmpty() ? response.getMessage() : "No onboarding data found.");
            }
        });
    }

    private void safeSetText(TextView tv, String text) {
        if (tv != null) {
            tv.setText(text != null ? text : "N/A");
        }
    }

    private void populateUI(EmployeeOnBoardDetails d) {

        // ── Profile Header ────────────────────────────────────────────────────────
        String name = safe(d.getFullName());
        safeSetText(tvAvatarChar, name.isEmpty() ? "XE" : String.valueOf(name.charAt(0)).toUpperCase());
        safeSetText(tvFullName, name.isEmpty() ? "N/A" : name);
        safeSetText(tvEmailHeader, safe(d.getPersonalEmail()));
        safeSetText(tvMobileHeader, orNA(d.getPersonalMobile()));
        safeSetText(tvEmployeeIdHeader, orNA(d.getEmployeeId()));

        // Status badge
        if (tvStatusBadge != null) {
            if (d.isStatus()) {
                tvStatusBadge.setText("Submitted");
                tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_active);
            } else {
                tvStatusBadge.setText("Pending");
                tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_inactive);
            }
        }

        // ── Section 1: Personal Details ───────────────────────────────────────────
        safeSetText(tvDob, formatDate(d.getDob()));
        safeSetText(tvGender, orNA(d.getGender()));
        safeSetText(tvFatherOrHusbandName, orNA(d.getFatherOrHusbandName()));
        safeSetText(tvMaritalStatus, orNA(d.getMaritalStatus()));
        safeSetText(tvNationality, orNA(d.getNationality()));
        safeSetText(tvBloodGroup, orNA(d.getBloodGroup()));

        // ── Section 2: Contact Details ────────────────────────────────────────────
        safeSetText(tvPersonalMobile, orNA(d.getPersonalMobile()));
        safeSetText(tvAlternateMobile, orNA(d.getAlternateMobile()));
        safeSetText(tvEmail, orNA(d.getPersonalEmail()));
        safeSetText(tvCurrentAddress, orNA(d.getCurrentAddress()));
        safeSetText(tvPermanentAddress, orNA(d.getPermanentAddress()));
        safeSetText(tvCity, orNA(d.getCity()));
        safeSetText(tvState, orNA(d.getState()));
        safeSetText(tvPincode, orNA(d.getPincode()));
        safeSetText(tvCountry, orNA(d.getCountry()));

        // ── Section 3: Emergency Contact ──────────────────────────────────────────
        safeSetText(tvEmergencyName, orNA(d.getEmergencyName()));
        safeSetText(tvEmergencyRelationship, orNA(d.getEmergencyRelationship()));
        safeSetText(tvEmergencyNumber, orNA(d.getEmergencyNumber()));
        safeSetText(tvEmergencyAlternateNumber, orNA(d.getEmergencyAlternateNumber()));
        safeSetText(tvEmergencyAddress, orNA(d.getEmergencyAddress()));

        // ── Section 4: Employment Details ─────────────────────────────────────────
        safeSetText(tvDateOfJoining, formatObjectDate(d.getDateOfJoining()));
        safeSetText(tvDesignation, orNA(d.getDesignation()));
        safeSetText(tvDepartment, orNA(d.getDepartment()));
        safeSetText(tvReportingManager, orNA(d.getReportingManager()));
        safeSetText(tvWorkLocation, orNA(d.getWorkLocation()));
        safeSetText(tvEmploymentType, orNA(d.getEmploymentType()));
        safeSetText(tvGrade, orNA(d.getGrade()));
        safeSetText(tvEmployeeId, orNA(d.getEmployeeId()));

        // ── Section 5: Education ──────────────────────────────────────────────────
        safeSetText(tvQualification, orNA(d.getQualification()));
        safeSetText(tvInstitution, orNA(d.getInstitution()));
        safeSetText(tvYearOfPassing, orNA(d.getYearOfPassing()));
        safeSetText(tvPercentageGrade, orNA(d.getPercentageGrade()));

        // ── Section 6: Previous Employment ───────────────────────────────────────
        safeSetText(tvPreviousEmployer, orNA(d.getPreviousEmployer()));
        safeSetText(tvPreviousDesignation, orNA(d.getPreviousDesignation()));
        safeSetText(tvPreviousDuration, orNA(d.getPreviousDuration()));
        safeSetText(tvLastDrawnCtc, orNA(d.getLastDrawnCtc()));
        safeSetText(tvRelievingDate, "N/A");
        safeSetText(tvReasonForLeaving, orNA(d.getReasonForLeaving()));

        // ── Section 7: Bank Details ───────────────────────────────────────────────
        safeSetText(tvBankName, orNA(d.getBankName()));
        safeSetText(tvBranchName, orNA(d.getBranchName()));
        safeSetText(tvAccountNumber, orNA(d.getAccountNumberMasked()));
        safeSetText(tvIfscCode, orNA(d.getIfscCode()));
        safeSetText(tvAccountHolderName, orNA(d.getAccountHolderName()));
        safeSetText(tvUpiId, orNA(d.getUpiId()));

        // ── Section 8: Statutory Details ─────────────────────────────────────────
        safeSetText(tvUanNumber, orNA(d.getUanNumberMasked()));
        safeSetText(tvEsiNumber, orNA(d.getEsiNumberMasked()));
        safeSetText(tvPassportNumber, orNA(d.getPassportNumberMasked()));
        safeSetText(tvPassportExpiryDate, formatObjectDate(d.getPassportExpiryDate()));

        // ── Section 9: Nominee Details ────────────────────────────────────────────
        safeSetText(tvNomineeName, orNA(d.getNomineeName()));
        safeSetText(tvNomineeRelationship, orNA(d.getNomineeRelationship()));
        safeSetText(tvNomineeDob, formatObjectDate(d.getNomineeDob()));
        safeSetText(tvSharePercentage, orNA(d.getSharePercentage()));

        // ── Section 10: Medical & Health ──────────────────────────────────────────
        safeSetText(tvMedicalCondition, orNA(d.getMedicalCondition()));
        safeSetText(tvMedicalConditionDetails, orNA(d.getMedicalConditionDetails()));
        safeSetText(tvKnownAllergies, orNA(d.getKnownAllergies()));

        // ── Section 11: Documents ─────────────────────────────────────────────────
        safeSetText(tvAadhaarNumberMasked, "Number: " + orNA(d.getAadhaarNumberMasked()));

        setupDocLink(tvAadhaarFrontLink, d.getAadhaarFrontFileURL(), "Aadhaar Front");
        setupDocLink(tvAadhaarBackLink, d.getAadhaarBackFileURL(), "Aadhaar Back");

        safeSetText(tvPanNumberMasked, "Number: " + orNA(d.getPanNumberMasked()));

        setupDocLink(tvPanLink, d.getPanFileURL(), "PAN Card");

        // ── Family Details ────────────────────────────────────────────────────────
        List<FamilyDetailsItem> familyList = d.getFamilyDetails();
        if (rvFamilyDetails != null && llNoFamily != null) {
            if (familyList != null && !familyList.isEmpty()) {
                llNoFamily.setVisibility(View.GONE);
                rvFamilyDetails.setVisibility(View.VISIBLE);
                rvFamilyDetails.setAdapter(new FamilyAdapter(familyList));
            } else {
                llNoFamily.setVisibility(View.VISIBLE);
                rvFamilyDetails.setVisibility(View.GONE);
            }
        }

        // ── Other Documents ───────────────────────────────────────────────────────
        List<DocumentsItem> docList = d.getDocuments();
        if (rvOtherDocuments != null && llNoDocuments != null) {
            if (docList != null && !docList.isEmpty()) {
                llNoDocuments.setVisibility(View.GONE);
                rvOtherDocuments.setVisibility(View.VISIBLE);
                rvOtherDocuments.setAdapter(new DocumentAdapter(docList));
            } else {
                llNoDocuments.setVisibility(View.VISIBLE);
                rvOtherDocuments.setVisibility(View.GONE);
            }
        }
    }

    // =========================================================================
    // HELPER: Setup clickable document link
    // =========================================================================
    private void setupDocLink(TextView tv, String url, String label) {
        if (url != null && !url.isEmpty()) {
            tv.setText("View " + label);
            tv.setVisibility(View.VISIBLE);
            tv.setOnClickListener(v -> openUrl(url));
        } else {
            tv.setVisibility(View.GONE);
        }
    }

    private void openUrl(String url) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Cannot open document.", Toast.LENGTH_SHORT).show();
        }
    }

    // =========================================================================
    // HELPER: Show error state
    // =========================================================================
    private void showError(String msg) {
        progressBar.setVisibility(View.GONE);
        scrollContent.setVisibility(View.GONE);
        tvError.setVisibility(View.VISIBLE);
        tvError.setText(msg);
    }

    // =========================================================================
    // HELPER: orNA – matches web || 'N/A'
    // =========================================================================
    private String orNA(String value) {
        return (value != null && !value.isEmpty()) ? value : "N/A";
    }

    private String safe(String value) {
        return value != null ? value : "";
    }

    // =========================================================================
    // HELPER: formatDate – matches web date:'dd-MM-yyyy' pipe
    // =========================================================================
    private String formatDate(String isoDate) {
        if (isoDate == null || isoDate.isEmpty()) return "N/A";
        // Try ISO format with time
        String[] formats = {"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "yyyy-MM-dd'T'HH:mm:ss'Z'", "yyyy-MM-dd"};
        for (String fmt : formats) {
            try {
                SimpleDateFormat inFmt = new SimpleDateFormat(fmt, Locale.getDefault());
                SimpleDateFormat outFmt = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                Date date = inFmt.parse(isoDate);
                if (date != null) return outFmt.format(date);
            } catch (Exception ignored) {
            }
        }
        return isoDate; // Return raw if parse fails
    }

    /**
     * Handles Object type dates (passportExpiryDate, nomineeDob, dateOfJoining)
     * These are declared as Object in your model because server can return
     * null or string
     */
    private String formatObjectDate(Object dateObj) {
        if (dateObj == null) return "N/A";
        return formatDate(dateObj.toString());
    }

    // =========================================================================
    // INNER ADAPTER: Family Details
    // Uses your existing FamilyDetailsItem model
    // =========================================================================
    private class FamilyAdapter extends RecyclerView.Adapter<FamilyAdapter.VH> {

        private final List<FamilyDetailsItem> list;

        FamilyAdapter(List<FamilyDetailsItem> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int type) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_family_detail_view, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            FamilyDetailsItem f = list.get(pos);

            h.tvName.setText("Name: " + orNA(f.getFamilyMemberName()));
            h.tvRelationship.setText("Relationship: " + orNA(f.getFamilyRelationship()));
            h.tvDob.setText("DOB: " + formatDate(f.getFamilyDob()));
            h.tvMobile.setText("Mobile: " + orNA(f.getFamilyMobile()));

            // Address proof link
            String url = f.getAddressProofFileURL();
            if (url != null && !url.isEmpty()) {
                h.tvDocLink.setText("View Document");
                h.tvDocLink.setTextColor(requireContext().getResources().getColor(android.R.color.holo_blue_dark));
                h.tvDocLink.setOnClickListener(v -> openUrl(url));
            } else {
                h.tvDocLink.setText("No file uploaded");
                h.tvDocLink.setTextColor(requireContext().getResources().getColor(android.R.color.darker_gray));
                h.tvDocLink.setOnClickListener(null);
            }
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class VH extends RecyclerView.ViewHolder {
            TextView tvName, tvRelationship, tvDob, tvMobile, tvDocLink;

            VH(@NonNull View v) {
                super(v);
                tvName = v.findViewById(R.id.tv_family_name);
                tvRelationship = v.findViewById(R.id.tv_family_relationship);
                tvDob = v.findViewById(R.id.tv_family_dob);
                tvMobile = v.findViewById(R.id.tv_family_mobile);
                tvDocLink = v.findViewById(R.id.tv_family_doc_link);
            }
        }
    }

    // =========================================================================
    // INNER ADAPTER: Other Documents
    // Uses your existing DocumentsItem model
    // =========================================================================
    private class DocumentAdapter extends RecyclerView.Adapter<DocumentAdapter.VH> {

        private final List<DocumentsItem> list;

        DocumentAdapter(List<DocumentsItem> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int type) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_document_view, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            DocumentsItem doc = list.get(pos);

            h.tvDocName.setText(orNA(doc.getDocumentName()));

            String url = doc.getDocumentFileURL();
            if (url != null && !url.isEmpty()) {
                h.tvDocLink.setText("View Document");
                h.tvDocLink.setTextColor(requireContext().getResources().getColor(android.R.color.holo_blue_dark));
                h.tvDocLink.setOnClickListener(v -> openUrl(url));
            } else {
                h.tvDocLink.setText("No file uploaded");
                h.tvDocLink.setTextColor(requireContext().getResources().getColor(android.R.color.darker_gray));
                h.tvDocLink.setOnClickListener(null);
            }
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class VH extends RecyclerView.ViewHolder {
            TextView tvDocName, tvDocLink;

            VH(@NonNull View v) {
                super(v);
                tvDocName = v.findViewById(R.id.tv_doc_name);
                tvDocLink = v.findViewById(R.id.tv_doc_link);
            }
        }
    }
}
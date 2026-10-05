package app.xedigital.ai.ui.leaves;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Parcel;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import app.xedigital.ai.R;
import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.databinding.FragmentLeavesBinding;
import app.xedigital.ai.model.ShortLeaveDetails.GetShortLeaveDetails;
import app.xedigital.ai.model.appliedLeaveDetails.AppliedLeaveDetailResponse;
import app.xedigital.ai.model.applyLeaves.ApplyLeaveRequest;
import app.xedigital.ai.model.branch.UserBranchResponse;
import app.xedigital.ai.model.debitLeave.DebitLeaveRequest;
import app.xedigital.ai.model.employeeLeaveType.EmployeeLeaveTypeResponse;
import app.xedigital.ai.model.holiday.HolidaysItem;
import app.xedigital.ai.model.leaveType.LeavetypesItem;
import app.xedigital.ai.model.profile.Employee;
import app.xedigital.ai.model.user.UserModelResponse;
import app.xedigital.ai.ui.holidays.HolidaysViewModel;
import app.xedigital.ai.ui.profile.ProfileViewModel;
import app.xedigital.ai.utills.CustomDialogHelper;
import app.xedigital.ai.utills.DateTimeUtils;
import app.xedigital.ai.utills.SecurePrefManager;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LeavesFragment extends Fragment {

    private static final String TAG = "LeavesFragment";

    // ── Leave Category Constants ───────────────────────────────────────
    private static final String FULL_DAY = "Full Day";
    private static final String FIRST_HALF_DAY = "First Half Day";
    private static final String SECOND_HALF_DAY = "Second Half Day";
    private static final String SHORT_LEAVE = "Short Leave";

    // ── Loader ─────────────────────────────────────────────────────────
    private final AtomicInteger activeLoaderCount = new AtomicInteger(0);
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // ── Views ──────────────────────────────────────────────────────────
    private FragmentLeavesBinding binding;
    private EditText etFromDate, etToDate;
    private EditText etStartTime, etEndTime;
    private LinearLayout llShortTimeContainer, llToDateContainer, llTimeSlotContainer;
    private TextInputLayout tilLeaveCategoryFrom, tilLeaveCategoryTo;
    private AutoCompleteTextView spinnerTimeSlot;
    private AlertDialog loadingDialog;
    private SimpleDateFormat dateFormat;

    // ── Leave Data ─────────────────────────────────────────────────────
    private double balanceLeave;
    private double totalDays;
    private double finalUsedDays;
    private LeavesViewModel leavesViewModel;
    private HolidaysViewModel holidaysViewModel;
    private ApplyLeaveRequest applyLeaveRequest;
    private DebitLeaveRequest debitLeaveRequest;
    private String selectedLeaveTypeName;
    private List<LeavetypesItem> leaveTypesList = new ArrayList<>();
    private String selectedLeaveTypeId;

    // ── Employee Data ──────────────────────────────────────────────────
    private String empId, empName, empLastname, empEmail;
    private String hrMail, empDepartment, department, empBirthday;
    private String reportingManagerName, reportingManagerLastname, reportingManagerEmail;
    private String crossFunctionalManagerName, crossFunctionalManagerEmail, crossFunctionalManagerId;
    private String authToken, restrictedHolidayId, lossOfPayId;

    // ── Short Leave Config (Dynamic from API) ──────────────────────────
    private boolean shortLeaveConfigLoaded = false;
    private boolean shortLeaveEnabled = false;
    private boolean shortLeaveTimingEnabled = false;
    private int maxShortLeavesPerMonth = 0;
    private double fixedShortLeaveHours = 0;
    private int usedShortLeavesThisMonth = 0;

    // ── ✅ Probation & Slot Configurations ─────────────────────────────
    private boolean confirmEmpShortLeave = false;
    private boolean probationEmpShortLeave = false;
    private boolean morningShortLeave = false;
    private boolean eveningShortLeave = false;
    private String empJoiningType = "";  // "probation" or "confirm"

    // ── Shift Data (from UserProfile) ──────────────────────────────────
    private boolean shiftDataLoaded = false;
    private String shiftStartTime = "";
    private String shiftEndTime = "";

    // ══════════════════════════════════════════════════════════════════════
    // LOADER
    // ══════════════════════════════════════════════════════════════════════

    private void showLoader() {
        activeLoaderCount.incrementAndGet();
        requireActivity().runOnUiThread(() -> {
            if (loadingDialog == null) {
                loadingDialog = CustomDialogHelper.showLoadingDialog(requireContext(), "Loading...");
            } else if (!loadingDialog.isShowing()) {
                loadingDialog.show();
            }
        });
    }

    private void hideLoader() {
        int remaining = activeLoaderCount.decrementAndGet();
        if (remaining <= 0) {
            activeLoaderCount.set(0);
            requireActivity().runOnUiThread(() -> {
                if (loadingDialog != null && loadingDialog.isShowing()) {
                    loadingDialog.dismiss();
                    loadingDialog = null;
                }
            });
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // LIFECYCLE
    // ══════════════════════════════════════════════════════════════════════

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        leavesViewModel = new ViewModelProvider(this).get(LeavesViewModel.class);
        binding = FragmentLeavesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        holidaysViewModel = new ViewModelProvider(requireActivity()).get(HolidaysViewModel.class);

        // ── Standard Views ─────────────────────────────────────────────
        etFromDate = binding.etFromDate;
        etToDate = binding.etToDate;

        // ── Short Leave Views ──────────────────────────────────────────
        etStartTime = binding.etStartTime;
        etEndTime = binding.etEndTime;
        llShortTimeContainer = binding.llShortTimeContainer;
        llToDateContainer = binding.llToDateContainer;
        llTimeSlotContainer = binding.llTimeSlotContainer;
        tilLeaveCategoryFrom = binding.tilLeaveCategoryFrom;
        tilLeaveCategoryTo = binding.tilLeaveCategoryTo;
        spinnerTimeSlot = binding.spinnerTimeSlot;

        binding.balanceLeaveTextView.setText("");
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        // ── Date Pickers ───────────────────────────────────────────────
        etFromDate.setOnClickListener(view -> showDatePicker(etFromDate));
        etToDate.setOnClickListener(view -> showDatePicker(etToDate));

        // ── Time Pickers (Manual mode only) ────────────────────────────
        etStartTime.setOnClickListener(view -> {
            if (etStartTime.isFocusable()) showTimePicker(etStartTime);
        });
        etEndTime.setOnClickListener(view -> {
            if (etEndTime.isFocusable()) showTimePicker(etEndTime);
        });

        // ── Spinners ───────────────────────────────────────────────────
        AutoCompleteTextView leaveCategorySpinnerFrom = binding.spinnerLeaveCategoryFrom;
        AutoCompleteTextView leaveCategorySpinnerTo = binding.spinnerLeaveCategoryTo;
        AutoCompleteTextView leavingStationSpinner = binding.spinnerLeavingStation;
        AutoCompleteTextView leavePlannedSpinner = binding.spinnerLeavePlanned;

        AtomicReference<TextInputEditText> leaveStationAddress = new AtomicReference<>(binding.etLeaveStationAddress);
        TextInputLayout leaveStationAddressLayout = binding.tilLeaveStationAddress;
        leaveStationAddressLayout.setVisibility(View.GONE);

        // ── Leaving Station Toggle ─────────────────────────────────────
        leavingStationSpinner.setOnItemClickListener((adapterView, view, position, id) -> {
            String selectedOption = (String) adapterView.getItemAtPosition(position);
            if ("Yes".equalsIgnoreCase(selectedOption)) {
                leaveStationAddressLayout.setVisibility(View.VISIBLE);
            } else {
                leaveStationAddressLayout.setVisibility(View.GONE);
                leaveStationAddress.get().setText("");
            }
        });

        // ── Adapters ───────────────────────────────────────────────────
        ArrayAdapter<String> leaveCategoriesAdapter = new ArrayAdapter<>(requireContext(), R.layout.dropdown_menu_popup_item, getResources().getStringArray(R.array.leave_categories));
        leaveCategorySpinnerFrom.setAdapter(leaveCategoriesAdapter);
        leaveCategorySpinnerTo.setAdapter(leaveCategoriesAdapter);

        ArrayAdapter<String> leavingStationsAdapter = new ArrayAdapter<>(requireContext(), R.layout.dropdown_menu_popup_item, getResources().getStringArray(R.array.leaving_stations));
        leavingStationSpinner.setAdapter(leavingStationsAdapter);

        ArrayAdapter<String> leavePlannedAdapter = new ArrayAdapter<>(requireContext(), R.layout.dropdown_menu_popup_item, getResources().getStringArray(R.array.leave_planned));
        leavePlannedSpinner.setAdapter(leavePlannedAdapter);

        // ── Time Slot Adapter (Morning / Evening) ──────────────────────
//        String[] timeSlots = {"Morning", "Evening"};
//        ArrayAdapter<String> timeSlotAdapter = new ArrayAdapter<>(requireContext(), R.layout.dropdown_menu_popup_item, timeSlots);
//        spinnerTimeSlot.setAdapter(timeSlotAdapter);
        // ── Time Slot Adapter (Morning / Evening) ──────────────────────
        // Initialize with empty adapter — will be dynamically populated based on API config
        ArrayAdapter<String> timeSlotAdapter = new ArrayAdapter<>(requireContext(), R.layout.dropdown_menu_popup_item, new ArrayList<>());
        spinnerTimeSlot.setAdapter(timeSlotAdapter);
        spinnerTimeSlot.setAdapter(timeSlotAdapter);

        // ── Time Slot Selection Handler ────────────────────────────────
        spinnerTimeSlot.setOnItemClickListener((parent, view, position, id) -> {
            String slot = (String) parent.getItemAtPosition(position);
            handleTimeSlotSelection(slot);
        });

        // ── Auth & User Setup ──────────────────────────────────────────
        SecurePrefManager prefManager = SecurePrefManager.getInstance(requireContext());
        authToken = prefManager.getString("authToken", "");
        String userId = prefManager.getString("userId", "");

        leavesViewModel.setUserId(authToken);
        leavesViewModel.fetchLeavesType();
        holidaysViewModel.loadHolidays(authToken);

        // ── Fetch Short Leave Config ───────────────────────────────────
        fetchShortLeaveDetails(authToken);

        ProfileViewModel profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        profileViewModel.storeLoginData(userId, authToken);
        profileViewModel.fetchUserProfile();

        binding.btnSubmit.setEnabled(false);

        showLoader(); // profile
        showLoader(); // user → branch chain
        showLoader(); // leave types

        callUserApi(userId, authToken);

        // ── Profile Observer ───────────────────────────────────────────
        profileViewModel.userProfile.observe(getViewLifecycleOwner(), userprofileResponse -> {
            if (userprofileResponse != null && userprofileResponse.getData() != null && userprofileResponse.getData().getEmployee() != null) {

                Employee employee = userprofileResponse.getData().getEmployee();

                empId = employee.getId();
                empName = employee.getFirstname();
                empLastname = employee.getLastname();
                empEmail = employee.getEmail();
                empBirthday = employee.getDateOfBirth();

                // ── ✅ Extract Joining Type ────────────────────────────
                empJoiningType = employee.getJoiningType() != null ? employee.getJoiningType().toLowerCase(Locale.getDefault()) : "";
                Log.d(TAG, "✅ Joining Type: " + empJoiningType);

                if (employee.getDepartment() != null) {
                    empDepartment = employee.getDepartment().getName();
                    department = (empDepartment != null) ? empDepartment.replace("\\u0026", "&") : "";
                } else {
                    empDepartment = "N/A";
                    department = "N/A";
                }

                if (employee.getReportingManager() != null) {
                    reportingManagerName = employee.getReportingManager().getFirstname();
                    reportingManagerLastname = employee.getReportingManager().getLastname();
                    reportingManagerEmail = employee.getReportingManager().getEmail();
                } else {
                    reportingManagerName = "N/A";
                    reportingManagerLastname = "";
                    reportingManagerEmail = "";
                }

                if (employee.getCrossmanager() != null) {
                    crossFunctionalManagerName = employee.getCrossmanager().getFirstname();
                    crossFunctionalManagerEmail = employee.getCrossmanager().getEmail();
                    crossFunctionalManagerId = employee.getCrossmanager().getId();
                } else {
                    crossFunctionalManagerName = "N/A";
                    crossFunctionalManagerEmail = "";
                    crossFunctionalManagerId = null;
                }

                // ── Extract Shift Data from UserProfile ────────────────
                extractShiftDataFromProfile(employee);

                binding.btnSubmit.setEnabled(true);
            }
            hideLoader();
        });

        // ── Leave Types Observer ───────────────────────────────────────
        leavesViewModel.leavesTypeData.observe(getViewLifecycleOwner(), leaveTypeResponse -> {
            if (leaveTypeResponse != null && leaveTypeResponse.getData() != null) {

                leaveTypesList = leaveTypeResponse.getData().getLeavetypes();
                List<String> leaveTypeNames = new ArrayList<>();

                for (LeavetypesItem leaveType : leaveTypesList) {
                    leaveTypeNames.add(leaveType.getLeavetypeName());
                }

                ArrayAdapter<String> leaveTypeAdapter = new ArrayAdapter<>(requireContext(), R.layout.dropdown_menu_popup_item, leaveTypeNames);
                binding.spinnerLeaveType.setAdapter(leaveTypeAdapter);

                for (LeavetypesItem leaveType : leaveTypesList) {
                    String name = leaveType.getLeavetypeName();
                    if ("Restricted Holidays".equals(name)) {
                        restrictedHolidayId = leaveType.getId();
                    }
                    if (lossOfPayId == null) {
                        if ("Loss of Pay (LOP) / Leave Without Pay (LWP)".equals(name) || "LOP".equals(name)) {
                            lossOfPayId = leaveType.getId();
                        }
                    }
                }
            }
            hideLoader();
        });

        // ── Clear Button ───────────────────────────────────────────────
        binding.btnClear.setOnClickListener(view -> clearForm());

        // ── ✅ Intercept Click/Touch on Leave Type Spinner ───────
        binding.spinnerLeaveType.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                if (etFromDate.getText().toString().isEmpty()) {
                    CustomDialogHelper.showInfoDialog(requireContext(), "Select Date", "Please select From Date first.");
                    return true; // Consumes event, blocks dropdown
                }
            }
            return false;
        });

        // ── Leave Type Spinner ─────────────────────────────────────────
        binding.spinnerLeaveType.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                LeavetypesItem selectedLeaveType = leaveTypesList.get(position);
                String tempLeaveTypeName = selectedLeaveType.getLeavetypeName();

                // Guard to block selection if To Date is empty (except for Short Leave)
                if (!SHORT_LEAVE.equalsIgnoreCase(tempLeaveTypeName) && etToDate.getText().toString().isEmpty()) {
                    CustomDialogHelper.showInfoDialog(requireContext(), "Select To Date", "Please select To Date first for this leave type.");
                    clearLeaveTypeOnly();
                    return;
                }

                selectedLeaveTypeId = selectedLeaveType.getId();
                selectedLeaveTypeName = tempLeaveTypeName;

                // Reset time inputs
                etStartTime.setText("");
                etEndTime.setText("");
                spinnerTimeSlot.setText("", false);

                // ── SHORT LEAVE UI TOGGLE ──────────────────────────
                if (SHORT_LEAVE.equalsIgnoreCase(selectedLeaveTypeName)) {

                    if (!shortLeaveConfigLoaded) {
                        CustomDialogHelper.showErrorDialog(requireContext(), "Please Wait", "Short Leave settings are still loading.\nPlease try again in a moment.", () -> clearLeaveTypeOnly());
                        return;
                    }

                    if (!shortLeaveEnabled) {
                        CustomDialogHelper.showErrorDialog(requireContext(), "Short Leave Disabled", "Short Leave is not enabled for your account.\n\nPlease contact HR.", () -> clearLeaveTypeOnly());
                        return;
                    }

                    // ── ✅ Check probation or confirmed eligibility ───
                    if (!isEmployeeEligibleForShortLeave()) {
                        String userTypeLabel = "probation".equalsIgnoreCase(empJoiningType) ? "Probationary" : "Confirmed";
                        CustomDialogHelper.showErrorDialog(requireContext(), "Not Eligible", "Short Leave is not enabled for " + userTypeLabel + " employees.\n\nPlease contact HR.", () -> clearLeaveTypeOnly());
                        return;
                    }

                    String fromDateVal = etFromDate.getText().toString();
                    if (!fromDateVal.isEmpty()) {
                        etToDate.setText(fromDateVal);
                    }

                    llToDateContainer.setVisibility(View.GONE);
                    tilLeaveCategoryFrom.setVisibility(View.GONE);
                    tilLeaveCategoryTo.setVisibility(View.GONE);
                    binding.spinnerLeaveCategoryFrom.setText("", false);
                    binding.spinnerLeaveCategoryTo.setText("", false);

                    llShortTimeContainer.setVisibility(View.VISIBLE);

                    // ── ✅ Dynamically evaluate timing slot visibility & setup listeners ──
                    setupTimeSlotAndManualFields();

                    // Fetch monthly short leave usage from appliedLeaves API
                    SecurePrefManager pm = SecurePrefManager.getInstance(requireContext());
                    String authTokenN = pm.getString("authToken", "");
                    String employeeId = pm.getString("userId", "");
                    fetchMonthlyShortLeaveUsage(authTokenN, employeeId);

                } else {
                    llToDateContainer.setVisibility(View.VISIBLE);
                    tilLeaveCategoryFrom.setVisibility(View.VISIBLE);
                    tilLeaveCategoryTo.setVisibility(View.VISIBLE);
                    llShortTimeContainer.setVisibility(View.GONE);
                    llTimeSlotContainer.setVisibility(View.GONE);

                    // Reset listeners to default manual behavior
                    etStartTime.setOnClickListener(v -> {
                        if (etStartTime.isFocusable()) showTimePicker(etStartTime);
                    });
                    etEndTime.setOnClickListener(v -> {
                        if (etEndTime.isFocusable()) showTimePicker(etEndTime);
                    });
                }

                SecurePrefManager pm = SecurePrefManager.getInstance(requireContext());
                String authTokenN = pm.getString("authToken", "");
                String employeeId = pm.getString("userId", "");
                String authHeader = "jwt " + authTokenN;

                showLoader();
                showLoader();

                fetchEmployeeLeave(selectedLeaveTypeId, employeeId, authHeader, selectedLeaveTypeName);
                fetchLeaveTypeDetails(selectedLeaveTypeId, authHeader);
            }

            // ── Fetch Leave Type Details ───────────────────────
            private void fetchLeaveTypeDetails(String leaveTypeId, String authHeader) {
                Call<ResponseBody> call = APIClient.getInstance().getLeaveTypeDetails().getLeaveTypeDetails(authHeader, leaveTypeId);

                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            try {
                                response.body().string();
                            } catch (IOException e) {
                                Log.e(TAG, e.getMessage());
                            }
                        }
                        hideLoader();
                    }

                    @Override
                    public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
                        Log.e(TAG, "Leave type details failure: " + throwable.getMessage());
                        hideLoader();
                    }
                });
            }

            // ── Fetch Employee Leave Balance ───────────────────
            private void fetchEmployeeLeave(String leaveTypeId, String employeeId, String authHeader, String leaveTypeName) {
                Call<EmployeeLeaveTypeResponse> call = APIClient.getInstance().getEmployeeLeave().getEmployeeLeave(authHeader, leaveTypeId, employeeId);

                call.enqueue(new Callback<EmployeeLeaveTypeResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<EmployeeLeaveTypeResponse> call, @NonNull Response<EmployeeLeaveTypeResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            EmployeeLeaveTypeResponse res = response.body();
                            balanceLeave = res.getData().getCreditLeave() - (res.getData().getUsedLeave() + res.getData().getDebitLeave());

                            String fromDate = Objects.requireNonNull(binding.etFromDate.getText()).toString();
                            String toDate = Objects.requireNonNull(binding.etToDate.getText()).toString();

                            if (!fromDate.isEmpty() && !toDate.isEmpty()) {
                                calculateTotalDaysAndCheckLeaveLimit(leaveTypeName);
                            }

                            if (leaveTypeId != null && leaveTypeId.equals(restrictedHolidayId) && !fromDate.isEmpty() && !toDate.isEmpty()) {
                                checkRestrictedHoliday(fromDate, toDate);
                            }

                            requireActivity().runOnUiThread(() -> {
                                if (leaveTypeName.equals("Loss of Pay (LOP) / Leave Without Pay (LWP)")) {
                                    binding.balanceLeaveTextView.setText("");
                                } else if (SHORT_LEAVE.equalsIgnoreCase(leaveTypeName)) {
                                    // Handled by fetchMonthlyShortLeaveUsage
                                } else {
                                    if (balanceLeave == 0.0) {
                                        binding.balanceLeaveTextView.setText("Apply with LOP/LWP  Balance: " + balanceLeave);
                                        Toast.makeText(requireContext(), "LOP/LWP", Toast.LENGTH_SHORT).show();
                                    } else {
                                        binding.balanceLeaveTextView.setText("Balance Leave: " + balanceLeave);
                                    }
                                }
                            });
                        }
                        hideLoader();
                    }

                    @Override
                    public void onFailure(@NonNull Call<EmployeeLeaveTypeResponse> call, @NonNull Throwable throwable) {
                        Log.e(TAG, "Employee leave failure: " + throwable.getMessage());
                        hideLoader();
                    }
                });
            }

            // ── Restricted Holiday Check ───────────────────────
            private void checkRestrictedHoliday(String fromDate, String toDate) {
                if (fromDate.isEmpty() || toDate.isEmpty()) return;

                String todayMD = LocalDate.now().format(DateTimeFormatter.ofPattern("MM-dd"));
                String empDOBMD = DateTimeUtils.getMonthDayFromISO(empBirthday);

                if (todayMD.equals(empDOBMD)) {
                    binding.balanceLeaveTextView.setText("Today is your birthday. Restricted leave allowed!");
                    return;
                }

                List<HolidaysItem> holidaysItems = holidaysViewModel.getHolidaysList().getValue();
                if (holidaysItems == null || holidaysItems.isEmpty()) {
                    CustomDialogHelper.showErrorDialog(requireContext(), "Data Unavailable", "Holiday data is not available.", () -> clearLeaveTypeOnly());
                    return;
                }

                List<String> dateRange = getDatesBetween(fromDate, toDate);
                boolean isValidDateRange = true;

                for (String date : dateRange) {
                    boolean isRestrictedHoliday = false;
                    for (HolidaysItem holiday : holidaysItems) {
                        String normalized = normalizeDate(holiday.getHolidayDate());
                        if (normalized.equals(date) && holiday.isIsOptional()) {
                            isRestrictedHoliday = true;
                            break;
                        }
                    }
                    if (!isRestrictedHoliday) {
                        binding.balanceLeaveTextView.setText("Selected date is not a Restricted Holiday. Balance: " + balanceLeave);
                        isValidDateRange = false;
                        break;
                    }
                }

                if (!isValidDateRange) {
                    CustomDialogHelper.showErrorDialog(requireContext(), "Not a Restricted Holiday", "The selected date range contains dates that are not restricted holidays.", () -> clearLeaveTypeOnly());
                } else {
                    binding.balanceLeaveTextView.setText("Balance Leave: " + balanceLeave);
                }
            }

            private String normalizeDate(String rawDate) {
                if (rawDate == null || rawDate.isEmpty()) return "";
                try {
                    if (rawDate.contains("T")) {
                        return Instant.parse(rawDate).atZone(ZoneId.of("UTC")).toLocalDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    }
                    return rawDate;
                } catch (Exception e) {
                    return rawDate;
                }
            }

            private List<String> getDatesBetween(String startDate, String endDate) {
                List<String> dates = new ArrayList<>();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                try {
                    Calendar startCal = Calendar.getInstance();
                    startCal.setTime(Objects.requireNonNull(sdf.parse(startDate)));
                    Calendar endCal = Calendar.getInstance();
                    endCal.setTime(Objects.requireNonNull(sdf.parse(endDate)));
                    while (startCal.before(endCal) || startCal.equals(endCal)) {
                        dates.add(sdf.format(startCal.getTime()));
                        startCal.add(Calendar.DAY_OF_MONTH, 1);
                    }
                } catch (ParseException e) {
                    Log.e(TAG, "getDatesBetween error: " + e.getMessage());
                }
                return dates;
            }
        });

        // ── Submit Button ──────────────────────────────────────────────
        binding.btnSubmit.setOnClickListener(view -> {
            if (validateForm()) {
                binding.btnSubmit.setEnabled(false);

                String fromDate = Objects.requireNonNull(binding.etFromDate.getText()).toString();
                String toDate = Objects.requireNonNull(binding.etToDate.getText()).toString();
                String leaveCategoryFrom = binding.spinnerLeaveCategoryFrom.getText().toString();
                String leaveCategoryTo = binding.spinnerLeaveCategoryTo.getText().toString();
                String leavingStation = binding.spinnerLeavingStation.getText().toString();
                String leaveStationAdd = Objects.requireNonNull(binding.etLeaveStationAddress.getText()).toString();
                String contactNumber = Objects.requireNonNull(binding.etContactNumber.getText()).toString();
                String reason = Objects.requireNonNull(binding.etReason.getText()).toString();
                String leavePlanned = Objects.requireNonNull(binding.spinnerLeavePlanned.getText()).toString();

                if (SHORT_LEAVE.equalsIgnoreCase(selectedLeaveTypeName)) {
                    toDate = fromDate;
                    leaveCategoryFrom = "";
                    leaveCategoryTo = "";
                }

                applyLeave(fromDate, toDate, leaveCategoryFrom, leaveCategoryTo, leavingStation, leaveStationAdd, contactNumber, reason, leavePlanned);
            }
        });

        return root;
    }

    // ══════════════════════════════════════════════════════════════════════
    // ✅ FETCH APPLIED LEAVES & COUNT MONTHLY SHORT LEAVES
    // ══════════════════════════════════════════════════════════════════════

    private void fetchMonthlyShortLeaveUsage(String authToken, String employeeId) {
        LocalDate targetDate = LocalDate.now();
        String fromDateText = etFromDate.getText().toString();
        if (!fromDateText.isEmpty()) {
            try {
                targetDate = LocalDate.parse(fromDateText, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } catch (Exception e) {
                Log.e(TAG, "Error parsing selected date: " + e.getMessage());
            }
        }

        String startOfMonth = targetDate.withDayOfMonth(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String endOfMonth = targetDate.withDayOfMonth(targetDate.lengthOfMonth()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String authHeader = "jwt " + authToken;

        Log.d(TAG, "🔎 [SHORT-LEAVE] Fetching range: " + startOfMonth + " to " + endOfMonth);

        showLoader();

        String sorting = "-createdAt";
        String page = "1";
        String limit = "100";
        String branch = "";
        String prefix = "";

        Call<AppliedLeaveDetailResponse> call = APIClient.getInstance().getApi().getAppliedLeavesWithFilters(authHeader, startOfMonth, endOfMonth, sorting, employeeId, page, limit, branch, prefix);

        call.enqueue(new Callback<AppliedLeaveDetailResponse>() {
            @Override
            public void onResponse(@NonNull Call<AppliedLeaveDetailResponse> call, @NonNull Response<AppliedLeaveDetailResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonResponse = gson.toJson(response.body());
                        JSONArray leavesArray = null;

                        if (jsonResponse.trim().startsWith("[")) {
                            leavesArray = new JSONArray(jsonResponse);
                        } else {
                            JSONObject root = new JSONObject(jsonResponse);
                            if (root.has("data")) {
                                Object dataObj = root.get("data");
                                if (dataObj instanceof JSONArray) {
                                    leavesArray = (JSONArray) dataObj;
                                } else if (dataObj instanceof JSONObject) {
                                    JSONObject dataJson = (JSONObject) dataObj;
                                    if (dataJson.has("employeesAppliedLeaves")) {
                                        leavesArray = dataJson.optJSONArray("employeesAppliedLeaves");
                                    }
                                }
                            }
                        }

                        usedShortLeavesThisMonth = calculateShortLeavesCount(leavesArray);
                        Log.d(TAG, "✅ [SHORT-LEAVE] Monthly Usage: " + usedShortLeavesThisMonth + "/" + maxShortLeavesPerMonth);

                        requireActivity().runOnUiThread(() -> updateShortLeaveBalanceUI());

                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing applied leaves array: " + e.getMessage());
                    }
                } else {
                    Log.e(TAG, "Applied leaves endpoint failed: " + response.message());
                }
                hideLoader();
            }

            @Override
            public void onFailure(@NonNull Call<AppliedLeaveDetailResponse> call, @NonNull Throwable throwable) {
                Log.e(TAG, "Applied leaves call failed: " + throwable.getMessage());
                hideLoader();
            }
        });
    }

    private int calculateShortLeavesCount(JSONArray leavesArray) {
        int count = 0;
        if (leavesArray == null) return 0;
        try {
            for (int i = 0; i < leavesArray.length(); i++) {
                JSONObject leave = leavesArray.getJSONObject(i);
                String leaveName = leave.optString("leaveName", "");
                String status = leave.optString("status", "");

                if (SHORT_LEAVE.equalsIgnoreCase(leaveName)) {
                    if (!"Rejected".equalsIgnoreCase(status) && !"Cancelled".equalsIgnoreCase(status)) {
                        count++;
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "calculateShortLeavesCount parsing error: " + e.getMessage());
        }
        return count;
    }

    private void updateShortLeaveBalanceUI() {
        if (!SHORT_LEAVE.equalsIgnoreCase(selectedLeaveTypeName)) return;

        boolean countBounded = maxShortLeavesPerMonth > 0;
        boolean durationBounded = fixedShortLeaveHours > 0;

        StringBuilder sb = new StringBuilder();

        if (countBounded) {
            int remaining = maxShortLeavesPerMonth - usedShortLeavesThisMonth;
            if (remaining <= 0) {
                sb.append("⚠ Monthly limit reached (").append(usedShortLeavesThisMonth).append("/").append(maxShortLeavesPerMonth).append(" used)");
                binding.balanceLeaveTextView.setText(sb.toString());
                return;
            }
            sb.append("Short Leave: ").append(remaining).append("/").append(maxShortLeavesPerMonth).append(" remaining");
        } else {
            sb.append("Short Leave: Unlimited applications");
        }

        sb.append(" • ");

        if (durationBounded) {
            sb.append(formatHours(fixedShortLeaveHours)).append(" hrs fixed duration");
        } else {
            sb.append("Flexible duration");
        }

        binding.balanceLeaveTextView.setText(sb.toString());
    }

    // ══════════════════════════════════════════════════════════════════════
    // EXTRACT SHIFT DATA FROM USER PROFILE
    // ══════════════════════════════════════════════════════════════════════

    private void extractShiftDataFromProfile(Employee employee) {
        try {
            if (employee.getShift() != null) {
                shiftStartTime = employee.getShift().getStartTime();
                shiftEndTime = employee.getShift().getEndTime();
                shiftDataLoaded = true;
            }

            if (shiftDataLoaded) {
                Log.d(TAG, "✅ Shift from profile: " + shiftStartTime + " → " + shiftEndTime);
            } else {
                Log.w(TAG, "No shift data found in user profile");
            }
        } catch (Exception e) {
            shiftDataLoaded = false;
            Log.e(TAG, "extractShiftDataFromProfile error: " + e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // TIME SLOT HANDLER (Morning / Evening)
    // ══════════════════════════════════════════════════════════════════════

    private void handleTimeSlotSelection(String slot) {
        if (!shiftDataLoaded) {
            CustomDialogHelper.showWarningDialog(requireContext(), "No Shift Assigned", "No shift timing found. Please select times manually.", "OK", "Cancel", new CustomDialogHelper.OnWarningActionListener() {
                @Override
                public void onConfirm() {
                    enableManualTimeSelection();
                }

                @Override
                public void onCancel() {
                    enableManualTimeSelection();
                }
            });
            return;
        }

        if (fixedShortLeaveHours <= 0) {
            etStartTime.setText(formatTimeTo12Hour(shiftStartTime));
            etEndTime.setText(formatTimeTo12Hour(shiftEndTime));
            return;
        }

        if ("Morning".equalsIgnoreCase(slot)) {
            String start = formatTimeTo12Hour(shiftStartTime);
            String end = addHoursToTime(shiftStartTime, fixedShortLeaveHours);
            etStartTime.setText(start);
            etEndTime.setText(end);
        } else if ("Evening".equalsIgnoreCase(slot)) {
            String start = subtractHoursFromTime(shiftEndTime, fixedShortLeaveHours);
            String end = formatTimeTo12Hour(shiftEndTime);
            etStartTime.setText(start);
            etEndTime.setText(end);
        }
    }

    // ── ✅ NEW: Configures and manages inputs in Short Leave Mode ──────
    // ── ✅ Configures and manages inputs in Short Leave Mode ──────
    private void setupTimeSlotAndManualFields() {
        if (shortLeaveTimingEnabled) {
            // Build the time slot list dynamically based on API flags
            List<String> dynamicSlots = getAvailableTimeSlots();

            Log.d(TAG, "🎯 Dynamic Slots: " + dynamicSlots + " | morning=" + morningShortLeave + ", evening=" + eveningShortLeave);

            if (dynamicSlots.isEmpty()) {
                // No slots available → fallback to manual entry
                llTimeSlotContainer.setVisibility(View.GONE);
                if (fixedShortLeaveHours > 0) {
                    setupStartTimeWithAutoCalculatedEnd();
                } else {
                    enableManualTimeSelection();
                }
            } else {
                llTimeSlotContainer.setVisibility(View.VISIBLE);

                // ✅ FIX: Force fresh adapter rebuild — prevents cached items
                spinnerTimeSlot.setText("", false);
                ArrayAdapter<String> freshAdapter = new ArrayAdapter<>(requireContext(), R.layout.dropdown_menu_popup_item, new ArrayList<>(dynamicSlots)  // Fresh copy to avoid reference issues
                );
                spinnerTimeSlot.setAdapter(freshAdapter);
                freshAdapter.notifyDataSetChanged();

                // Lock Start/End time fields (user selects via dropdown)
                etStartTime.setFocusable(false);
                etStartTime.setClickable(false);
                etEndTime.setFocusable(false);
                etEndTime.setClickable(false);
                etStartTime.setOnClickListener(null);
                etEndTime.setOnClickListener(null);

                // If only one slot is available, auto-select it
                if (dynamicSlots.size() == 1) {
                    String onlySlot = dynamicSlots.get(0);
                    spinnerTimeSlot.setText(onlySlot, false);
                    handleTimeSlotSelection(onlySlot);
                }
            }
        } else {
            // Timing dropdown disabled — manual mode
            llTimeSlotContainer.setVisibility(View.GONE);
            if (fixedShortLeaveHours > 0) {
                // Exemption fixed → user picks Start, End auto-calculated
                setupStartTimeWithAutoCalculatedEnd();
            } else {
                // Fully manual → user picks both
                enableManualTimeSelection();
            }
        }
    }

    // ── ✅ Helper: Setup Start Time → Auto-calculate End Time ─────────
    private void setupStartTimeWithAutoCalculatedEnd() {
        etStartTime.setFocusable(false);
        etStartTime.setClickable(true);
        etEndTime.setFocusable(false);
        etEndTime.setClickable(false);
        etEndTime.setOnClickListener(null);
        etStartTime.setOnClickListener(v -> showTimePickerAndCalculateEnd(etStartTime));
    }

    private void enableManualTimeSelection() {
        llTimeSlotContainer.setVisibility(View.GONE);
        etStartTime.setFocusable(true);
        etStartTime.setClickable(true);
        etEndTime.setFocusable(true);
        etEndTime.setClickable(true);
        etStartTime.setOnClickListener(v -> showTimePicker(etStartTime));
        etEndTime.setOnClickListener(v -> showTimePicker(etEndTime));
    }

    // ══════════════════════════════════════════════════════════════════════
    // SHORT LEAVE CONFIG FETCH
    // ══════════════════════════════════════════════════════════════════════

    private void fetchShortLeaveDetails(String authToken) {
        String authHeader = "jwt " + authToken;
        showLoader();

        Call<GetShortLeaveDetails> call = APIClient.getInstance().getApi().getShortLeaveDetails(authHeader);

        call.enqueue(new Callback<GetShortLeaveDetails>() {
            @Override
            public void onResponse(@NonNull Call<GetShortLeaveDetails> call, @NonNull Response<GetShortLeaveDetails> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    app.xedigital.ai.model.ShortLeaveDetails.Data data = response.body().getData();
                    if (data != null) {
                        shortLeaveEnabled = data.isShortLeave();
                        maxShortLeavesPerMonth = data.getShortLeaveCount();
                        fixedShortLeaveHours = data.getShortLeaveExemption();

                        confirmEmpShortLeave = data.isConfirmEmpShortLeave();
                        probationEmpShortLeave = data.isProbationEmpShortLeave();
                        morningShortLeave = data.isMorningShortLeave();
                        eveningShortLeave = data.isEveningShortLeave();

                        shortLeaveTimingEnabled = morningShortLeave || eveningShortLeave;

                        if (maxShortLeavesPerMonth < 0) maxShortLeavesPerMonth = 0;
                        if (fixedShortLeaveHours < 0) fixedShortLeaveHours = 0;

                        shortLeaveConfigLoaded = true;

                        Log.d(TAG, "✅ Short Leave Config → enabled=" + shortLeaveEnabled + ", monthlyLimit=" + maxShortLeavesPerMonth + ", fixedHours=" + fixedShortLeaveHours + ", timingDropdown=" + shortLeaveTimingEnabled + ", confirm=" + confirmEmpShortLeave + ", probation=" + probationEmpShortLeave + ", morning=" + morningShortLeave + ", evening=" + eveningShortLeave);
                    }
                } else {
                    shortLeaveConfigLoaded = false;
                    Log.e(TAG, "Short leave config failed: " + response.message());
                }
                hideLoader();
            }

            @Override
            public void onFailure(@NonNull Call<GetShortLeaveDetails> call, @NonNull Throwable throwable) {
                shortLeaveConfigLoaded = false;
                Log.e(TAG, "Short leave config failure: " + throwable.getMessage());
                hideLoader();
            }
        });
    }

    // ══════════════════════════════════════════════════════════════════════
    // TIME HELPERS
    // ══════════════════════════════════════════════════════════════════════

    private Date parseTimeString(String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) return null;
        String[] formats = {"HH:mm", "hh:mm a", "HH:mm:ss", "hh:mm:ss a", "h:mm a"};
        for (String fmt : formats) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(fmt, Locale.getDefault());
                sdf.setLenient(false);
                return sdf.parse(timeStr.trim());
            } catch (ParseException ignored) {
            }
        }
        Log.e(TAG, "Could not parse time: " + timeStr);
        return null;
    }

    private String formatTimeTo12Hour(String timeStr) {
        Date time = parseTimeString(timeStr);
        if (time == null) return timeStr;
        return new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(time);
    }

    private String addHoursToTime(String timeStr, double hours) {
        Date time = parseTimeString(timeStr);
        if (time == null) return timeStr;
        long millis = time.getTime() + (long) (hours * 60 * 60 * 1000);
        return new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date(millis));
    }

    private String subtractHoursFromTime(String timeStr, double hours) {
        Date time = parseTimeString(timeStr);
        if (time == null) return timeStr;
        long millis = time.getTime() - (long) (hours * 60 * 60 * 1000);
        return new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date(millis));
    }

    private double calculateHoursBetween(String startStr, String endStr) {
        try {
            Date start = parseTimeString(startStr);
            Date end = parseTimeString(endStr);
            if (start == null || end == null) return 0;
            long diffMillis = end.getTime() - start.getTime();
            return diffMillis / (1000.0 * 60 * 60);
        } catch (Exception e) {
            Log.e(TAG, "calculateHoursBetween error: " + e.getMessage());
            return 0;
        }
    }

    private String formatHours(double hours) {
        if (hours == Math.floor(hours)) return String.valueOf((int) hours);
        return String.valueOf(hours);
    }

    private String convertTo24HourFormat(String time12) {
        if (time12 == null || time12.isEmpty()) return "";
        try {
            Date date = parseTimeString(time12);
            if (date != null) {
                return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(date);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error converting to 24-hour: " + e.getMessage());
        }
        return time12;
    }

    // ══════════════════════════════════════════════════════════════════════
    // TIME PICKER (Manual mode)
    // ══════════════════════════════════════════════════════════════════════

    private void showTimePicker(final EditText editText) {
        Calendar now = Calendar.getInstance();

        MaterialTimePicker picker = new MaterialTimePicker.Builder().setTimeFormat(TimeFormat.CLOCK_12H).setHour(now.get(Calendar.HOUR_OF_DAY)).setMinute(now.get(Calendar.MINUTE)).setTitleText("Select Time").build();

        picker.addOnPositiveButtonClickListener(v -> {
            int hour = picker.getHour();
            int minute = picker.getMinute();
            String amPm = (hour >= 12) ? "PM" : "AM";
            int formattedHour = (hour == 0 || hour == 12) ? 12 : hour % 12;
            String timeString = String.format(Locale.getDefault(), "%02d:%02d %s", formattedHour, minute, amPm);
            editText.setText(timeString);

            if (editText == etEndTime) {
                validateShortLeaveTimeRange();
            }
        });

        picker.show(requireActivity().getSupportFragmentManager(), "timePicker");
    }

    // ── ✅ NEW: Opens time picker and auto-calculates end time ────────
    private void showTimePickerAndCalculateEnd(final EditText editText) {
        Calendar now = Calendar.getInstance();
        MaterialTimePicker picker = new MaterialTimePicker.Builder().setTimeFormat(TimeFormat.CLOCK_12H).setHour(now.get(Calendar.HOUR_OF_DAY)).setMinute(now.get(Calendar.MINUTE)).setTitleText("Select Start Time").build();

        picker.addOnPositiveButtonClickListener(v -> {
            int hour = picker.getHour();
            int minute = picker.getMinute();
            String amPm = (hour >= 12) ? "PM" : "AM";
            int formattedHour = (hour == 0 || hour == 12) ? 12 : hour % 12;
            String timeString = String.format(Locale.getDefault(), "%02d:%02d %s", formattedHour, minute, amPm);
            editText.setText(timeString);

            String calculatedEndTime = addHoursToTime(timeString, fixedShortLeaveHours);
            etEndTime.setText(calculatedEndTime);
            Log.d(TAG, "⏰ Calculated End Time: " + calculatedEndTime);
        });

        picker.show(requireActivity().getSupportFragmentManager(), "timePickerCalculated");
    }

    private void validateShortLeaveTimeRange() {
        String startStr = etStartTime.getText().toString();
        String endStr = etEndTime.getText().toString();
        if (startStr.isEmpty() || endStr.isEmpty()) return;

        Date startTime = parseTimeString(startStr);
        Date endTime = parseTimeString(endStr);

        if (startTime != null && endTime != null && !endTime.after(startTime)) {
            CustomDialogHelper.showErrorDialog(requireContext(), "Invalid Time Range", "End time must be after start time.");
            etEndTime.setText("");
            return;
        }

        if (fixedShortLeaveHours > 0) {
            double hours = calculateHoursBetween(startStr, endStr);
            if (Math.abs(hours - fixedShortLeaveHours) > 0.01) {
                CustomDialogHelper.showErrorDialog(requireContext(), "Invalid Duration", "Short Leave must be exactly " + formatHours(fixedShortLeaveHours) + " hours.\n\n" + "You selected " + String.format(Locale.getDefault(), "%.2f", hours) + " hours.");
                etEndTime.setText("");
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // ✅ NEW: UTILITY ELIGIBILITY EVALUATOR
    // ══════════════════════════════════════════════════════════════════════

    private boolean isEmployeeEligibleForShortLeave() {
        if ("probation".equalsIgnoreCase(empJoiningType)) {
            return probationEmpShortLeave;
        } else if ("confirm".equalsIgnoreCase(empJoiningType) || "confirmed".equalsIgnoreCase(empJoiningType)) {
            return confirmEmpShortLeave;
        }
        return confirmEmpShortLeave || probationEmpShortLeave;
    }

    // ══════════════════════════════════════════════════════════════════════
    // ✅ NEW: DYNAMIC TIME SLOT CALCULATOR OPTIONS
    // ══════════════════════════════════════════════════════════════════════

    private List<String> getAvailableTimeSlots() {
        List<String> dynamicSlots = new ArrayList<>();
        if (morningShortLeave) dynamicSlots.add("Morning");
        if (eveningShortLeave) dynamicSlots.add("Evening");
        return dynamicSlots;
    }

    // ══════════════════════════════════════════════════════════════════════
    // VALIDATION
    // ══════════════════════════════════════════════════════════════════════

    private boolean validateForm() {
        String fromDateText = etFromDate.getText().toString();
        String toDateText = etToDate.getText().toString();
        String leaveType = binding.spinnerLeaveType.getText().toString();
        String leaveCategoryFrom = binding.spinnerLeaveCategoryFrom.getText().toString();
        String leaveCategoryTo = binding.spinnerLeaveCategoryTo.getText().toString();
        String leavingStation = binding.spinnerLeavingStation.getText().toString();
        String leaveStationAddress = Objects.requireNonNull(binding.etLeaveStationAddress.getText()).toString();

        boolean isShortLeave = SHORT_LEAVE.equalsIgnoreCase(leaveType);

        if (fromDateText.isEmpty() || leaveType.isEmpty() || leavingStation.isEmpty()) {
            showMissingFieldsWarning();
            return false;
        }

        if (isShortLeave) {
            if (!shortLeaveEnabled) {
                CustomDialogHelper.showErrorDialog(requireContext(), "Short Leave Disabled", "Short Leave is not enabled for your account.");
                return false;
            }

            if (!isEmployeeEligibleForShortLeave()) {
                CustomDialogHelper.showErrorDialog(requireContext(), "Ineligible", "Short Leave is not configured for your current employment status.");
                return false;
            }

            if (maxShortLeavesPerMonth > 0 && usedShortLeavesThisMonth >= maxShortLeavesPerMonth) {
                CustomDialogHelper.showErrorDialog(requireContext(), "Monthly Limit Reached", "You have used all " + maxShortLeavesPerMonth + " short leaves this month.");
                return false;
            }

            if (shortLeaveTimingEnabled && llTimeSlotContainer.getVisibility() == View.VISIBLE) {
                if (spinnerTimeSlot.getText().toString().isEmpty()) {
                    CustomDialogHelper.showErrorDialog(requireContext(), "Time Slot Required", "Please select Morning or Evening time slot.");
                    return false;
                }
            }

            String startTimeText = etStartTime.getText().toString();
            String endTimeText = etEndTime.getText().toString();

            if (startTimeText.isEmpty() || endTimeText.isEmpty()) {
                CustomDialogHelper.showErrorDialog(requireContext(), "Missing Time Slot", "Please enter both Start and End times.");
                return false;
            }

            Date startTime = parseTimeString(startTimeText);
            Date endTime = parseTimeString(endTimeText);

            if (startTime != null && endTime != null && !endTime.after(startTime)) {
                CustomDialogHelper.showErrorDialog(requireContext(), "Invalid Time Range", "End time must be after start time.");
                return false;
            }

            if (fixedShortLeaveHours > 0) {
                double hours = calculateHoursBetween(startTimeText, endTimeText);
                if (Math.abs(hours - fixedShortLeaveHours) > 0.01) {
                    CustomDialogHelper.showErrorDialog(requireContext(), "Invalid Duration", "Short Leave must be exactly " + formatHours(fixedShortLeaveHours) + " hours.\n\n" + "You selected " + String.format(Locale.getDefault(), "%.2f", hours) + " hours.");
                    return false;
                }
            }
        } else {
            if (toDateText.isEmpty() || leaveCategoryFrom.isEmpty() || leaveCategoryTo.isEmpty()) {
                showMissingFieldsWarning();
                return false;
            }
        }

        if (leavingStation.equalsIgnoreCase("Yes") && leaveStationAddress.isEmpty()) {
            CustomDialogHelper.showErrorDialog(requireContext(), "Address Required", "You selected 'Yes' for leaving station.\nPlease enter your leave station address.");
            return false;
        }

        String contactNumber = Objects.requireNonNull(binding.etContactNumber.getText()).toString().trim();
        if (contactNumber.isEmpty()) {
            CustomDialogHelper.showErrorDialog(requireContext(), "Contact Required", "Please enter a contact number.");
            return false;
        }

        if (binding.spinnerLeavePlanned.getText().toString().isEmpty()) {
            binding.spinnerLeavePlanned.setError("This field is required");
            return false;
        } else {
            binding.spinnerLeavePlanned.setError(null);
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date fromDate = sdf.parse(fromDateText);
            Date toDate = isShortLeave ? fromDate : sdf.parse(toDateText);
            Date today = sdf.parse(sdf.format(new Date()));

            if (!leaveType.equals("Sick Leave") && fromDate != null && fromDate.before(today)) {
                CustomDialogHelper.showErrorDialog(requireContext(), "Invalid Date", "Past dates are not allowed for this leave type.");
                return false;
            }

            if (fromDate != null && toDate != null && fromDate.after(toDate)) {
                CustomDialogHelper.showErrorDialog(requireContext(), "Invalid Date Range", "The 'From' date cannot be after the 'To' date.");
                return false;
            }

        } catch (ParseException e) {
            Log.e(TAG, "Date parse error: " + e.getMessage());
            CustomDialogHelper.showErrorDialog(requireContext(), "Date Error", "Invalid date format.");
            return false;
        }

        if (!isShortLeave) {
            if (!isValidLeaveCategoryCombination(leaveCategoryFrom, leaveCategoryTo)) {
                showLeaveCombinationWarningDialog(leaveCategoryFrom, leaveCategoryTo);
                return false;
            }

            if (!isValidSingleDayHalfDaySelection(fromDateText, toDateText, leaveCategoryFrom, leaveCategoryTo)) {
                CustomDialogHelper.showWarningDialog(requireContext(), "Invalid Half-Day Selection", "Selecting \"First Half\" → \"Second Half\" on the same day equals a full day.\n\n" + "Please use \"Full Day → Full Day\" instead.", "Fix It", "Cancel", new CustomDialogHelper.OnWarningActionListener() {
                    @Override
                    public void onConfirm() {
                        binding.spinnerLeaveCategoryFrom.setText("", false);
                        binding.spinnerLeaveCategoryTo.setText("", false);
                    }

                    @Override
                    public void onCancel() {
                    }
                });
                return false;
            }
        }

        return true;
    }

    private void showMissingFieldsWarning() {
        CustomDialogHelper.showWarningDialog(requireContext(), "Missing Fields", "Please fill in all required fields before submitting.", "OK", "Cancel", new CustomDialogHelper.OnWarningActionListener() {
            @Override
            public void onConfirm() {
            }

            @Override
            public void onCancel() {
            }
        });
    }

    private boolean isValidLeaveCategoryCombination(String from, String to) {
        if (FULL_DAY.equals(from) && FULL_DAY.equals(to)) return true;
        if (FIRST_HALF_DAY.equals(from) && FIRST_HALF_DAY.equals(to)) return true;
        if (FIRST_HALF_DAY.equals(from) && SECOND_HALF_DAY.equals(to)) return true;
        return SECOND_HALF_DAY.equals(from) && SECOND_HALF_DAY.equals(to);
    }

    private boolean isValidSingleDayHalfDaySelection(String fromDate, String toDate, String from, String to) {
        if (!fromDate.equals(toDate)) return true;
        return !FIRST_HALF_DAY.equals(from) || !SECOND_HALF_DAY.equals(to);
    }

    private void showLeaveCombinationWarningDialog(String from, String to) {
        String message = buildCombinationErrorMessage(from, to);
        CustomDialogHelper.showWarningDialog(requireContext(), "Invalid Combination", message, "Fix Selection", "Cancel", new CustomDialogHelper.OnWarningActionListener() {
            @Override
            public void onConfirm() {
                binding.spinnerLeaveCategoryFrom.setText("", false);
                binding.spinnerLeaveCategoryTo.setText("", false);
            }

            @Override
            public void onCancel() {
            }
        });
    }

    private String buildCombinationErrorMessage(String from, String to) {
        if (SECOND_HALF_DAY.equals(from) && FIRST_HALF_DAY.equals(to)) {
            return "\"Second Half\" → \"First Half\" is not allowed.\n\nTry:\n• First Half → First Half (0.5 day)\n• First Half → Second Half (full)\n• Second Half → Second Half (0.5 day)";
        }
        if (SECOND_HALF_DAY.equals(from) && FULL_DAY.equals(to)) {
            return "\"Second Half\" → \"Full Day\" is not allowed.\n\nValid: Second Half → Second Half (0.5 day)";
        }
        if (FULL_DAY.equals(from) && FIRST_HALF_DAY.equals(to)) {
            return "\"Full Day\" → \"First Half\" is not allowed.\n\nValid: Full Day → Full Day";
        }
        if (FULL_DAY.equals(from) && SECOND_HALF_DAY.equals(to)) {
            return "\"Full Day\" → \"Second Half\" is not allowed.\n\nValid: Full Day → Full Day";
        }
        if (FIRST_HALF_DAY.equals(from) && FULL_DAY.equals(to)) {
            return "\"First Half\" → \"Full Day\" is not allowed.\n\nValid:\n• First Half → First Half\n• First Half → Second Half";
        }
        return "\"" + from + "\" → \"" + to + "\" is invalid.\n\nValid:\n• Full Day → Full Day\n• First Half → First Half (0.5)\n• First Half → Second Half (full)\n• Second Half → Second Half (0.5)";
    }

    // ══════════════════════════════════════════════════════════════════════
    // DAY CALCULATION
    // ══════════════════════════════════════════════════════════════════════

    private long calculateTotalDays(String startDate, String endDate) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate start = LocalDate.parse(startDate, formatter);
            LocalDate end = LocalDate.parse(endDate, formatter);
            return Math.abs(ChronoUnit.DAYS.between(start, end)) + 1;
        } catch (DateTimeParseException e) {
            Log.e(TAG, "calculateTotalDays error: " + e.getMessage());
            return 0;
        }
    }

    private void calculateTotalDaysAndCheckLeaveLimit(String leaveTypeName) {
        if (SHORT_LEAVE.equalsIgnoreCase(leaveTypeName)) {
            totalDays = 1.0; // Set to 1 for short leave payload requirement
            finalUsedDays = 1.0;
            return;
        }

        String fromDate = Objects.requireNonNull(binding.etFromDate.getText()).toString();
        String toDate = Objects.requireNonNull(binding.etToDate.getText()).toString();
        String catFrom = binding.spinnerLeaveCategoryFrom.getText().toString();
        String catTo = binding.spinnerLeaveCategoryTo.getText().toString();

        if (fromDate.isEmpty() || toDate.isEmpty() || catFrom.isEmpty() || catTo.isEmpty()) return;

        long tDays = calculateTotalDays(fromDate, toDate);
        totalDays = tDays;
        finalUsedDays = computeFinalUsedDays(tDays, catFrom, catTo);

        if (finalUsedDays > balanceLeave && leaveTypeName != null && !leaveTypeName.equals("Loss of Pay (LOP) / Leave Without Pay (LWP)")) {
            CustomDialogHelper.showWarningDialog(requireContext(), "Insufficient Balance", "You need " + finalUsedDays + " days but only have " + balanceLeave + " days available.\n\nPlease reduce days or apply for LOP.", "OK", "Cancel", new CustomDialogHelper.OnWarningActionListener() {
                @Override
                public void onConfirm() {
                    binding.spinnerLeaveType.setText("");
                }

                @Override
                public void onCancel() {
                }
            });
        }
    }

    private double computeFinalUsedDays(long tDays, String from, String to) {
        double fUsedDays = tDays;
        if (FIRST_HALF_DAY.equals(from) && FIRST_HALF_DAY.equals(to)) fUsedDays -= 0.5;
        else if (SECOND_HALF_DAY.equals(from) && SECOND_HALF_DAY.equals(to)) fUsedDays -= 0.5;
        return fUsedDays;
    }

    // ══════════════════════════════════════════════════════════════════════
    // ✅ APPLY LEAVE — Payload Updated as per Backend Spec
    // ══════════════════════════════════════════════════════════════════════

    private void applyLeave(String fromDate, String toDate, String leaveCategoryFrom, String leaveCategoryTo, String leavingStation, String leaveStationAdd, String contactNumber, String reason, String leavePlanned) {

        applyLeaveRequest = new ApplyLeaveRequest();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneId.of("UTC"));
        String appliedDate = formatter.format(Instant.now());

        boolean isShortLeave = SHORT_LEAVE.equalsIgnoreCase(selectedLeaveTypeName);

        // ── Common fields ──────────────────────────────────────────────
        applyLeaveRequest.setFromDate(fromDate);
        applyLeaveRequest.setToDate(toDate);
        applyLeaveRequest.setLeaveName(selectedLeaveTypeName);
        applyLeaveRequest.setLeavetype(selectedLeaveTypeId);
        applyLeaveRequest.setContactNumber(contactNumber);
        applyLeaveRequest.setReason(reason);
        applyLeaveRequest.setVacationAddress(leaveStationAdd);
        applyLeaveRequest.setAppliedDate(appliedDate);
        applyLeaveRequest.setDepartment(department);
        applyLeaveRequest.setEmployee(empId);
        applyLeaveRequest.setEmpFirstName(empName);
        applyLeaveRequest.setEmpLastName(empLastname);
        applyLeaveRequest.setEmpEmail(empEmail);
        applyLeaveRequest.setHrEmail(hrMail != null ? hrMail : "");
        applyLeaveRequest.setStatus("");
        applyLeaveRequest.setReportingManager(reportingManagerEmail);
        applyLeaveRequest.setReportingManagerName(reportingManagerName);
        applyLeaveRequest.setReportingManagerLastName(reportingManagerLastname);

        String leavingStationPayload = "Yes".equalsIgnoreCase(leavingStation) ? "true" : "false";
        applyLeaveRequest.setLeavingStation(leavingStationPayload);
        applyLeaveRequest.setLeavePlanned(leavePlanned.toLowerCase(Locale.getDefault()));

        if (isShortLeave) {
            applyLeaveRequest.setSelectTypeFrom(null);
            applyLeaveRequest.setSelectTypeTo(null);

            String rawStart = etStartTime.getText().toString();
            String rawEnd = etEndTime.getText().toString();
            applyLeaveRequest.setShortLeaveStartTime(convertTo24HourFormat(rawStart));
            applyLeaveRequest.setShortLeaveEndTime(convertTo24HourFormat(rawEnd));

            if (shortLeaveTimingEnabled) {
                String slot = spinnerTimeSlot.getText().toString().toLowerCase(Locale.getDefault());
                applyLeaveRequest.setShortLeaveTimingSlot(slot);
            } else {
                applyLeaveRequest.setShortLeaveTimingSlot(null);
            }

            Log.d(TAG, "Short Leave Payload — Start: " + convertTo24HourFormat(rawStart) + " | End: " + convertTo24HourFormat(rawEnd));
        } else {
            applyLeaveRequest.setSelectTypeFrom(leaveCategoryFrom);
            applyLeaveRequest.setSelectTypeTo(leaveCategoryTo);
            applyLeaveRequest.setShortLeaveStartTime("");
            applyLeaveRequest.setShortLeaveEndTime("");
            applyLeaveRequest.setShortLeaveTimingSlot(null);
        }

        if (crossFunctionalManagerName == null || crossFunctionalManagerName.isEmpty() || crossFunctionalManagerEmail == null || crossFunctionalManagerEmail.isEmpty()) {
            CustomDialogHelper.showWarningDialog(requireContext(), "Manager Not Assigned", "No cross-functional manager is assigned to your profile.\n\nPlease contact your Admin or HR.", "OK", "Cancel", new CustomDialogHelper.OnWarningActionListener() {
                @Override
                public void onConfirm() {
                    clearForm();
                }

                @Override
                public void onCancel() {
                }
            });
            binding.btnSubmit.setEnabled(true);
            return;
        }

        applyLeaveRequest.setCrossManager(crossFunctionalManagerId);
        applyLeaveRequest.setCrossManagerEmail(crossFunctionalManagerEmail);
        applyLeaveRequest.setCrossManagerName(crossFunctionalManagerName);

        Log.d(TAG, "📤 Apply Leave Payload:\n" + gson.toJson(applyLeaveRequest));

        showLoader();

        Call<ResponseBody> applyLeave = APIClient.getInstance().LeavesApply().LeavesApply("jwt " + authToken, applyLeaveRequest);

        applyLeave.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        String responseBody = response.body().string();
                        JSONObject jsonObject = new JSONObject(responseBody);
                        boolean isSuccess = jsonObject.optBoolean("success", false);
                        JSONObject data = jsonObject.optJSONObject("data");
                        String databaseId = (data != null) ? data.optString("_id") : null;

                        if (isSuccess && databaseId != null && !databaseId.isEmpty()) {
                            boolean skipDebit = Objects.equals(selectedLeaveTypeId, lossOfPayId);

                            if (!skipDebit) {
                                hideLoader();
                                showLoader();
                                debitLeave(fromDate, toDate, leaveCategoryFrom, leaveCategoryTo, leavingStation, leaveStationAdd, contactNumber, reason);
                            } else {
                                hideLoader();
                                CustomDialogHelper.showSuccessDialog(requireContext(), "Leave Applied! 🎉", "Your leave request has been submitted successfully.\n\nYour manager will be notified.", () -> clearForm());
                            }
                        } else {
                            hideLoader();
                            binding.btnSubmit.setEnabled(true);
                            CustomDialogHelper.showErrorDialog(requireContext(), "Application Failed", "Server did not return a valid record.\nPlease try again.");
                        }
                    } else {
                        hideLoader();
                        binding.btnSubmit.setEnabled(true);
                        CustomDialogHelper.showErrorDialog(requireContext(), "Server Error", "Error code: " + response.code() + "\nPlease try again later.");
                    }
                } catch (Exception e) {
                    hideLoader();
                    binding.btnSubmit.setEnabled(true);
                    CustomDialogHelper.showErrorDialog(requireContext(), "Parsing Error", "Could not process server response.\n\n" + e.getMessage());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
                hideLoader();
                binding.btnSubmit.setEnabled(true);
                CustomDialogHelper.showErrorDialog(requireContext(), "Network Error", "Could not connect to the server.\nPlease check your internet connection.");
            }
        });
    }

    // ══════════════════════════════════════════════════════════════════════
    // DEBIT LEAVE
    // ══════════════════════════════════════════════════════════════════════

    private void debitLeave(String fromDate, String toDate, String leaveCategoryFrom, String leaveCategoryTo, String leavingStation, String leaveStationAdd, String contactNumber, String reason) {

        debitLeaveRequest = new DebitLeaveRequest();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneId.of("UTC"));
        String appliedDate = formatter.format(Instant.now());

        boolean isShortLeave = SHORT_LEAVE.equalsIgnoreCase(selectedLeaveTypeName);

        debitLeaveRequest.setFromDate(fromDate);
        debitLeaveRequest.setToDate(toDate);
        debitLeaveRequest.setLeaveName(selectedLeaveTypeName);
        debitLeaveRequest.setLeavetype(selectedLeaveTypeId);
        debitLeaveRequest.setContactNumber(contactNumber);
        debitLeaveRequest.setReason(reason);
        debitLeaveRequest.setVacationAddress(leaveStationAdd);
        debitLeaveRequest.setAppliedDate(appliedDate);
        debitLeaveRequest.setEmployee(empId);
        debitLeaveRequest.setEmpFirstName(empName);
        debitLeaveRequest.setEmpLastName(empLastname);
        debitLeaveRequest.setEmpEmail(empEmail);
        debitLeaveRequest.setHrEmail(hrMail != null ? hrMail : "");
        debitLeaveRequest.setStatus("");
        debitLeaveRequest.setReportingManager(reportingManagerEmail);
        debitLeaveRequest.setReportingManagerName(reportingManagerName);
        debitLeaveRequest.setReportingManagerLastName(reportingManagerLastname);

        String leavingStationPayload = "Yes".equalsIgnoreCase(leavingStation) ? "true" : "false";
        debitLeaveRequest.setLeavingStation(leavingStationPayload);
        debitLeaveRequest.setLeavePlanned(binding.spinnerLeavePlanned.getText().toString().toLowerCase(Locale.getDefault()));
        debitLeaveRequest.setCrossManager(crossFunctionalManagerId);
        debitLeaveRequest.setCrossManagerEmail(crossFunctionalManagerEmail);
        debitLeaveRequest.setCrossManagerName(crossFunctionalManagerName);
        debitLeaveRequest.setDepartment(department);

        if (isShortLeave) {
            debitLeaveRequest.setSelectTypeFrom(null);
            debitLeaveRequest.setSelectTypeTo(null);

            String rawStart = etStartTime.getText().toString();
            String rawEnd = etEndTime.getText().toString();
            debitLeaveRequest.setShortLeaveStartTime(convertTo24HourFormat(rawStart));
            debitLeaveRequest.setShortLeaveEndTime(convertTo24HourFormat(rawEnd));

            if (shortLeaveTimingEnabled) {
                String slot = spinnerTimeSlot.getText().toString().toLowerCase(Locale.getDefault());
                debitLeaveRequest.setShortLeaveTimingSlot(slot);
            } else {
                debitLeaveRequest.setShortLeaveTimingSlot(null);
            }

            debitLeaveRequest.setTDays(1);
            debitLeaveRequest.setFUsedDays(1);
        } else {
            debitLeaveRequest.setSelectTypeFrom(leaveCategoryFrom);
            debitLeaveRequest.setSelectTypeTo(leaveCategoryTo);
            debitLeaveRequest.setShortLeaveStartTime("");
            debitLeaveRequest.setShortLeaveEndTime("");
            debitLeaveRequest.setShortLeaveTimingSlot(null);

            debitLeaveRequest.setTDays((int) totalDays);
            debitLeaveRequest.setFUsedDays((int) finalUsedDays);
        }

        Log.d(TAG, "📤 Debit Leave Payload:\n" + gson.toJson(debitLeaveRequest));

        Call<ResponseBody> debitLeave = APIClient.getInstance().debitLeave().LeavesUsedDebit("jwt " + authToken, debitLeaveRequest);

        debitLeave.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                hideLoader();
                binding.btnSubmit.setEnabled(true);
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        response.body().string();
                        CustomDialogHelper.showSuccessDialog(requireContext(), "Leave Applied! 🎉", "Your leave request has been submitted and balance updated.", () -> clearForm());
                    } else {
                        CustomDialogHelper.showWarningDialog(requireContext(), "Partial Success", "Leave recorded but balance update failed.\nPlease contact HR.", "OK", "Cancel", new CustomDialogHelper.OnWarningActionListener() {
                            @Override
                            public void onConfirm() {
                                clearForm();
                            }

                            @Override
                            public void onCancel() {
                            }
                        });
                    }
                } catch (IOException e) {
                    Log.e(TAG, "debitLeave parse error: " + e.getMessage());
                    CustomDialogHelper.showWarningDialog(requireContext(), "Partial Success", "Leave recorded but could not verify balance.\nPlease contact HR.", "OK", "Cancel", new CustomDialogHelper.OnWarningActionListener() {
                        @Override
                        public void onConfirm() {
                            clearForm();
                        }

                        @Override
                        public void onCancel() {
                        }
                    });
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
                hideLoader();
                binding.btnSubmit.setEnabled(true);
                CustomDialogHelper.showWarningDialog(requireContext(), "Partial Success", "Leave recorded but network error during balance update.\nPlease contact HR.", "OK", "Cancel", new CustomDialogHelper.OnWarningActionListener() {
                    @Override
                    public void onConfirm() {
                        clearForm();
                    }

                    @Override
                    public void onCancel() {
                    }
                });
            }
        });
    }
    // ══════════════════════════════════════════════════════════════════════
    // DATE PICKER
    // ══════════════════════════════════════════════════════════════════════

    private void showDatePicker(final EditText editText) {
        MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
        builder.setTitleText("Select date");
        builder.setSelection(MaterialDatePicker.todayInUtcMilliseconds());

        String selectedLeaveType = binding.spinnerLeaveType.getText().toString();
        long todayInMillis = MaterialDatePicker.todayInUtcMilliseconds();

        Calendar yesterdayCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        yesterdayCal.add(Calendar.DAY_OF_MONTH, -1);
        yesterdayCal.set(Calendar.HOUR_OF_DAY, 0);
        yesterdayCal.set(Calendar.MINUTE, 0);
        yesterdayCal.set(Calendar.SECOND, 0);
        yesterdayCal.set(Calendar.MILLISECOND, 0);
        long yesterdayInMillis = yesterdayCal.getTimeInMillis();

        Calendar yearStart = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        yearStart.set(Calendar.MONTH, Calendar.JANUARY);
        yearStart.set(Calendar.DAY_OF_MONTH, 1);
        yearStart.set(Calendar.HOUR_OF_DAY, 0);
        yearStart.set(Calendar.MINUTE, 0);
        yearStart.set(Calendar.SECOND, 0);

        Calendar yearEnd = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        yearEnd.set(Calendar.MONTH, Calendar.DECEMBER);
        yearEnd.set(Calendar.DAY_OF_MONTH, 31);
        yearEnd.set(Calendar.HOUR_OF_DAY, 23);
        yearEnd.set(Calendar.MINUTE, 59);
        yearEnd.set(Calendar.SECOND, 59);

        long yearStartMillis = yearStart.getTimeInMillis();
        long yearEndMillis = yearEnd.getTimeInMillis();

        CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();
        constraintsBuilder.setStart(yearStartMillis);
        constraintsBuilder.setEnd(yearEndMillis);
        constraintsBuilder.setOpenAt(todayInMillis);

        if (selectedLeaveType.equals("Sick Leave")) {
            constraintsBuilder.setValidator(new CalendarConstraints.DateValidator() {
                @Override
                public boolean isValid(long date) {
                    return date >= yesterdayInMillis && date <= yearEndMillis;
                }

                @Override
                public int describeContents() {
                    return 0;
                }

                @Override
                public void writeToParcel(@NonNull Parcel dest, int flags) {
                }
            });
        } else {
            constraintsBuilder.setValidator(new CalendarConstraints.DateValidator() {
                @Override
                public boolean isValid(long date) {
                    return date >= todayInMillis && date <= yearEndMillis;
                }

                @Override
                public int describeContents() {
                    return 0;
                }

                @Override
                public void writeToParcel(@NonNull Parcel dest, int flags) {
                }
            });
        }

        builder.setCalendarConstraints(constraintsBuilder.build());
        MaterialDatePicker<Long> picker = builder.build();

        picker.addOnPositiveButtonClickListener(selection -> {
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            calendar.setTimeInMillis(selection);
            String selectedDate = dateFormat.format(calendar.getTime());
            editText.setText(selectedDate);

            if (SHORT_LEAVE.equalsIgnoreCase(selectedLeaveType) && editText == etFromDate) {
                etToDate.setText(selectedDate);

                // Fetch dynamic Month usage on Date selection inside Short Leave Mode
                SecurePrefManager pm = SecurePrefManager.getInstance(requireContext());
                String authTokenN = pm.getString("authToken", "");
                String employeeId = pm.getString("userId", "");
                fetchMonthlyShortLeaveUsage(authTokenN, employeeId);
            }

            if (editText == etToDate) {
                validateDateRange();
                calculateTotalDaysAndCheckLeaveLimit(selectedLeaveTypeName);
            }
        });

        picker.show(requireActivity().getSupportFragmentManager(), "datePicker");
    }

    private void validateDateRange() {
        String fromDateText = etFromDate.getText().toString();
        String toDateText = etToDate.getText().toString();
        String selectedLeaveType = binding.spinnerLeaveType.getText().toString();

        if (!fromDateText.isEmpty() && !toDateText.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date fromDate = sdf.parse(fromDateText);
                Date toDate = sdf.parse(toDateText);
                Date today = sdf.parse(sdf.format(new Date()));

                if (selectedLeaveType.equals("Sick Leave")) {
                    if (fromDate != null && fromDate.after(toDate)) {
                        CustomDialogHelper.showErrorDialog(requireContext(), "Invalid Date Range", "'From' date cannot be after 'To' date.");
                    }
                } else {
                    if (fromDate != null && (fromDate.after(toDate) || fromDate.before(today))) {
                        CustomDialogHelper.showErrorDialog(requireContext(), "Invalid Date Range", "'From' date cannot be after 'To' date or before today.");
                    }
                }
            } catch (ParseException e) {
                Log.e(TAG, "validateDateRange error: " + e.getMessage());
                CustomDialogHelper.showErrorDialog(requireContext(), "Date Error", "Invalid date format.");
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // API HELPERS
    // ══════════════════════════════════════════════════════════════════════

    public void callUserApi(String userId, String authToken) {
        String authHeaderValue = "jwt " + authToken;
        Call<UserModelResponse> call = APIClient.getInstance().getUser().getUserData(userId, authHeaderValue);

        call.enqueue(new Callback<UserModelResponse>() {
            @Override
            public void onResponse(@NonNull Call<UserModelResponse> call, @NonNull Response<UserModelResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String branchId = response.body().getData().getBranch().getId();
                    callBranchApi(branchId, authToken);
                } else {
                    hideLoader();
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserModelResponse> call, @NonNull Throwable throwable) {
                Log.e(TAG, "callUserApi failure: " + throwable.getMessage());
                hideLoader();
            }
        });
    }

    public void callBranchApi(String branchId, String authToken) {
        String authHeaderValue = "jwt " + authToken;
        Call<UserBranchResponse> branchCall = APIClient.getInstance().getBranch().getBranchData(branchId, authHeaderValue);

        branchCall.enqueue(new Callback<UserBranchResponse>() {
            @Override
            public void onResponse(@NonNull Call<UserBranchResponse> call, @NonNull Response<UserBranchResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        hrMail = response.body().getData().getBranch().getNotificationEmail();
                    } catch (Exception e) {
                        Log.e(TAG, "callBranchApi error: " + e.getMessage());
                    }
                }
                hideLoader();
            }

            @Override
            public void onFailure(@NonNull Call<UserBranchResponse> call, @NonNull Throwable throwable) {
                Log.e(TAG, "callBranchApi failure: " + throwable.getMessage());
                hideLoader();
            }
        });
    }

    // ══════════════════════════════════════════════════════════════════════
    // UI HELPERS
    // ══════════════════════════════════════════════════════════════════════

    public void clearLeaveTypeOnly() {
        binding.spinnerLeaveType.setText("", false);
        binding.balanceLeaveTextView.setText("");
        etStartTime.setText("");
        etEndTime.setText("");
        spinnerTimeSlot.setText("", false);
        llShortTimeContainer.setVisibility(View.GONE);
        llTimeSlotContainer.setVisibility(View.GONE);
        llToDateContainer.setVisibility(View.VISIBLE);
        tilLeaveCategoryFrom.setVisibility(View.VISIBLE);
        tilLeaveCategoryTo.setVisibility(View.VISIBLE);
        selectedLeaveTypeId = null;
        selectedLeaveTypeName = null;
    }

    public void clearForm() {
        binding.etFromDate.setText("");
        binding.etToDate.setText("");
        binding.spinnerLeaveCategoryFrom.setText("");
        binding.spinnerLeaveCategoryTo.setText("");
        binding.spinnerLeavingStation.setText("");
        binding.etLeaveStationAddress.setText("");
        binding.spinnerLeaveType.setText("");
        binding.etContactNumber.setText("");
        binding.etReason.setText("");
        binding.balanceLeaveTextView.setText("");
        binding.tilLeaveStationAddress.setVisibility(View.GONE);
        binding.spinnerLeavePlanned.setText("");

        etStartTime.setText("");
        etEndTime.setText("");
        spinnerTimeSlot.setText("", false);
        llShortTimeContainer.setVisibility(View.GONE);
        llTimeSlotContainer.setVisibility(View.GONE);
        llToDateContainer.setVisibility(View.VISIBLE);
        tilLeaveCategoryFrom.setVisibility(View.VISIBLE);
        tilLeaveCategoryTo.setVisibility(View.VISIBLE);

        binding.btnSubmit.setEnabled(true);
        selectedLeaveTypeId = null;
        selectedLeaveTypeName = null;
    }
}
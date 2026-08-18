package app.xedigital.ai.ui.leaves;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Parcel;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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

    // ── Valid Leave Category Combinations ──────────────────────────────
    private static final String FULL_DAY = "Full Day";
    private static final String FIRST_HALF_DAY = "First Half Day";
    private static final String SECOND_HALF_DAY = "Second Half Day";

    // ── Active loader counter ──────────────────────────────────────────
    private final AtomicInteger activeLoaderCount = new AtomicInteger(0);
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private FragmentLeavesBinding binding;
    private EditText etFromDate, etToDate;
    private AlertDialog loadingDialog;
    private SimpleDateFormat dateFormat;
    private double balanceLeave;
    private double totalDays;
    private double finalUsedDays;
    private LeavesViewModel leavesViewModel;
    private HolidaysViewModel holidaysViewModel;
    private ApplyLeaveRequest applyLeaveRequest;
    private DebitLeaveRequest debitLeaveRequest;
    private String selectedLeaveTypeName;
    private List<LeavetypesItem> leaveTypesList = new ArrayList<>();
    private String empId, empName, empLastname, empEmail;
    private String hrMail, empDepartment, department, empBirthday;
    private String selectedLeaveTypeId;
    private String reportingManagerName, reportingManagerLastname, reportingManagerEmail;
    private String crossFunctionalManagerName, crossFunctionalManagerEmail, crossFunctionalManagerId;
    private String authToken, restrictedHolidayId, lossOfPayId;

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

        etFromDate = binding.etFromDate;
        etToDate = binding.etToDate;
        binding.balanceLeaveTextView.setText("");

        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        // ── Date Pickers ───────────────────────────────────────────────
        etFromDate.setOnClickListener(view -> showDatePicker(etFromDate));
        etToDate.setOnClickListener(view -> showDatePicker(etToDate));

        // ── Spinners ───────────────────────────────────────────────────
        AutoCompleteTextView leaveTypeSpinner = binding.spinnerLeaveType;
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

        // ── Auth & User Setup ──────────────────────────────────────────
        SecurePrefManager prefManager = SecurePrefManager.getInstance(requireContext());
        authToken = prefManager.getString("authToken", "");
        String userId = prefManager.getString("userId", "");

        leavesViewModel.setUserId(authToken);
        leavesViewModel.fetchLeavesType();
        holidaysViewModel.loadHolidays(authToken);

        ProfileViewModel profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        profileViewModel.storeLoginData(userId, authToken);
        profileViewModel.fetchUserProfile();

        // ── Disable Submit until all data is ready ─────────────────────
        binding.btnSubmit.setEnabled(false);

        // ── Initial page loaders ───────────────────────────────────────
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

        // ── Leave Type Guard (dates must be selected first) ────────────
        leaveTypeSpinner.setOnClickListener(view -> {
            if (etToDate.getText().toString().isEmpty()) {
                CustomDialogHelper.showInfoDialog(requireContext(), "Select Dates", "Please select From and To dates first.");
            }
        });

        // ── Leave Type Spinner ─────────────────────────────────────────
        binding.spinnerLeaveType.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                LeavetypesItem selectedLeaveType = leaveTypesList.get(position);
                selectedLeaveTypeId = selectedLeaveType.getId();
                selectedLeaveTypeName = selectedLeaveType.getLeavetypeName();

                // ✅ Removed duplicate checkRestrictedHoliday call
                // from here — it is handled ONLY inside
                // fetchEmployeeLeave response to prevent
                // double alert trigger.

                SecurePrefManager prefManager = SecurePrefManager.getInstance(requireContext());
                String authTokenN = prefManager.getString("authToken", "");
                String employeeId = prefManager.getString("userId", "");
                String authHeader = "jwt " + authTokenN;

                showLoader(); // fetchEmployeeLeave
                showLoader(); // fetchLeaveTypeDetails
                showLoader(); // fetchUnapprovedLeaves

                fetchEmployeeLeave(selectedLeaveTypeId, employeeId, authHeader, selectedLeaveTypeName);
                fetchLeaveTypeDetails(selectedLeaveTypeId, authHeader);
                fetchUnapprovedLeaves(selectedLeaveTypeId, employeeId, authHeader);
            }

            // ── Fetch Unapproved Leaves ────────────────────────
            private void fetchUnapprovedLeaves(String leaveTypeId, String employeeId, String authHeader) {
                Call<ResponseBody> call = APIClient.getInstance().getUnapprovedLeaves().getUnapprovedLeaves(authHeader, leaveTypeId, employeeId);

                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            try {
                                response.body().string();
                            } catch (IOException e) {
                                Log.e(TAG, "Unapproved leaves error: " + e.getMessage());
                            }
                        }
                        hideLoader();
                    }

                    @Override
                    public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
                        Log.e(TAG, "Unapproved leaves failure: " + throwable.getMessage());
                        hideLoader();
                    }
                });
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
                                Log.e(TAG, "Leave type details error: " + e.getMessage());
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

                            // ✅ ONLY place where
                            // checkRestrictedHoliday
                            // is called — prevents
                            // double alert
                            if (leaveTypeId != null && leaveTypeId.equals(restrictedHolidayId) && !fromDate.isEmpty() && !toDate.isEmpty()) {
                                checkRestrictedHoliday(fromDate, toDate);
                            }

                            requireActivity().runOnUiThread(() -> {
                                if (leaveTypeName.equals("Loss of Pay (LOP) / Leave Without Pay (LWP)")) {
                                    binding.balanceLeaveTextView.setText("");
                                } else {
                                    if (balanceLeave == 0.0) {
                                        binding.balanceLeaveTextView.setText("Apply with LOP/LWP" + "  Balance: " + balanceLeave);
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
            // Called ONCE only from fetchEmployeeLeave response.
            // This prevents the double-alert bug.
            private void checkRestrictedHoliday(String fromDate, String toDate) {
                if (fromDate.isEmpty() || toDate.isEmpty()) return;

                // ── Step 1: Birthday check ─────────────────────
                String todayMD = LocalDate.now().format(DateTimeFormatter.ofPattern("MM-dd"));
                String empDOBMD = DateTimeUtils.getMonthDayFromISO(empBirthday);

                if (todayMD.equals(empDOBMD)) {
                    binding.balanceLeaveTextView.setText("Today is your birthday." + " Restricted leave allowed!");
                    return;
                }

                // ── Step 2: Get holidays directly ──────────────
                List<HolidaysItem> holidaysItems = holidaysViewModel.getHolidaysList().getValue();

                if (holidaysItems == null || holidaysItems.isEmpty()) {
                    CustomDialogHelper.showErrorDialog(requireContext(), "Data Unavailable", "Holiday data is not available." + " Please try again later.", () -> clearLeaveTypeOnly()); // ✅ Only clear leave type
                    return;
                }

                // ── Step 3: Check each date in range ──────────
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
                        binding.balanceLeaveTextView.setText("Selected date is not a" + " Restricted Holiday." + " Balance: " + balanceLeave);
                        isValidDateRange = false;
                        break;
                    }
                }

                // ── Step 4: Show result ────────────────────────
                if (!isValidDateRange) {
                    CustomDialogHelper.showErrorDialog(requireContext(), "Not a Restricted Holiday", "The selected date range contains" + " dates that are not" + " restricted holidays." + "\n\nPlease select a" + " different leave type" + " or change your dates.", () -> clearLeaveTypeOnly()); // ✅ Only clear leave type
                } else {
                    binding.balanceLeaveTextView.setText("Balance Leave: " + balanceLeave);
                }
            }

            // ── Normalize API date to yyyy-MM-dd ──────────────
            private String normalizeDate(String rawDate) {
                if (rawDate == null || rawDate.isEmpty()) return "";
                try {
                    if (rawDate.contains("T")) {
                        return Instant.parse(rawDate).atZone(ZoneId.of("UTC")).toLocalDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    }
                    return rawDate;
                } catch (Exception e) {
                    Log.e(TAG, "normalizeDate error: " + e.getMessage());
                    return rawDate;
                }
            }

            // ── Get dates between two date strings ────────────
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

                applyLeave(fromDate, toDate, leaveCategoryFrom, leaveCategoryTo, leavingStation, leaveStationAdd, contactNumber, reason, leavePlanned);
            }
        });

        return root;
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

        // ── Step 1: Required fields ────────────────────────────────────
        if (fromDateText.isEmpty() || toDateText.isEmpty() || leaveType.isEmpty() || leaveCategoryFrom.isEmpty() || leaveCategoryTo.isEmpty() || leavingStation.isEmpty()) {
            CustomDialogHelper.showWarningDialog(requireContext(), "Missing Fields", "Please fill in all required fields before submitting.", "OK", "Cancel", new CustomDialogHelper.OnWarningActionListener() {
                @Override
                public void onConfirm() {
                }

                @Override
                public void onCancel() {
                }
            });
            return false;
        }

        // ── Step 2: Leaving station address ───────────────────────────
        if (leavingStation.equalsIgnoreCase("Yes") && leaveStationAddress.isEmpty()) {
            CustomDialogHelper.showErrorDialog(requireContext(), "Address Required", "You selected 'Yes' for leaving station." + "\nPlease enter your leave station address.");
            return false;
        }

        // ── Step 3: Contact number ─────────────────────────────────────
        String contactNumber = Objects.requireNonNull(binding.etContactNumber.getText()).toString().trim();
        if (contactNumber.isEmpty()) {
            CustomDialogHelper.showErrorDialog(requireContext(), "Contact Required", "Please enter a contact number where" + " you can be reached during leave.");
            return false;
        }

        // ── Step 4: Leave planned ──────────────────────────────────────
        if (binding.spinnerLeavePlanned.getText().toString().isEmpty()) {
            binding.spinnerLeavePlanned.setError("This field is required");
            return false;
        } else {
            binding.spinnerLeavePlanned.setError(null);
        }

        // ── Step 5: Date range ─────────────────────────────────────────
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date fromDate = sdf.parse(fromDateText);
            Date toDate = sdf.parse(toDateText);
            Date today = sdf.parse(sdf.format(new Date()));

            if (!leaveType.equals("Sick Leave") && fromDate != null && fromDate.before(today)) {
                CustomDialogHelper.showErrorDialog(requireContext(), "Invalid Date", "Past dates are not allowed for this leave type." + "\nPlease select today or a future date.");
                return false;
            }

            if (fromDate != null && fromDate.after(toDate)) {
                CustomDialogHelper.showErrorDialog(requireContext(), "Invalid Date Range", "The 'From' date cannot be after the 'To' date." + "\nPlease correct your date selection.");
                return false;
            }

        } catch (ParseException e) {
            Log.e(TAG, "Date parse error: " + e.getMessage());
            CustomDialogHelper.showErrorDialog(requireContext(), "Date Error", "Invalid date format. Please re-select your dates.");
            return false;
        }

        // ── Step 6: Leave category combination ────────────────────────
        if (!isValidLeaveCategoryCombination(leaveCategoryFrom, leaveCategoryTo)) {
            showLeaveCombinationWarningDialog(leaveCategoryFrom, leaveCategoryTo);
            return false;
        }

        // ── Step 7: Single day half-day consistency ───────────────────
        if (!isValidSingleDayHalfDaySelection(fromDateText, toDateText, leaveCategoryFrom, leaveCategoryTo)) {
            CustomDialogHelper.showWarningDialog(requireContext(), "Invalid Half-Day Selection", "Selecting \"First Half\" → \"Second Half\"" + " on the same day equals a full day.\n\n" + "Please use \"Full Day → Full Day\" instead.", "Fix It", "Cancel", new CustomDialogHelper.OnWarningActionListener() {
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

        return true;
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
            return "\"Second Half\" → \"First Half\" is not allowed.\n\n" + "Try:\n• First Half → First Half (0.5 day)\n" + "• First Half → Second Half (full)\n" + "• Second Half → Second Half (0.5 day)";
        }
        if (SECOND_HALF_DAY.equals(from) && FULL_DAY.equals(to)) {
            return "\"Second Half\" → \"Full Day\" is not allowed.\n\n" + "Valid: Second Half → Second Half (0.5 day)";
        }
        if (FULL_DAY.equals(from) && FIRST_HALF_DAY.equals(to)) {
            return "\"Full Day\" → \"First Half\" is not allowed.\n\n" + "Valid: Full Day → Full Day";
        }
        if (FULL_DAY.equals(from) && SECOND_HALF_DAY.equals(to)) {
            return "\"Full Day\" → \"Second Half\" is not allowed.\n\n" + "Valid: Full Day → Full Day";
        }
        if (FIRST_HALF_DAY.equals(from) && FULL_DAY.equals(to)) {
            return "\"First Half\" → \"Full Day\" is not allowed.\n\n" + "Valid:\n• First Half → First Half\n" + "• First Half → Second Half";
        }
        return "\"" + from + "\" → \"" + to + "\" is invalid.\n\n" + "Valid:\n• Full Day → Full Day\n" + "• First Half → First Half (0.5)\n" + "• First Half → Second Half (full)\n" + "• Second Half → Second Half (0.5)";
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
        String fromDate = Objects.requireNonNull(binding.etFromDate.getText()).toString();
        String toDate = Objects.requireNonNull(binding.etToDate.getText()).toString();
        String catFrom = binding.spinnerLeaveCategoryFrom.getText().toString();
        String catTo = binding.spinnerLeaveCategoryTo.getText().toString();

        if (fromDate.isEmpty() || toDate.isEmpty() || catFrom.isEmpty() || catTo.isEmpty()) return;

        long tDays = calculateTotalDays(fromDate, toDate);
        totalDays = tDays;
        finalUsedDays = computeFinalUsedDays(tDays, catFrom, catTo);

        if (finalUsedDays > balanceLeave && leaveTypeName != null && !leaveTypeName.equals("Loss of Pay (LOP) / Leave Without Pay (LWP)")) {
            CustomDialogHelper.showWarningDialog(requireContext(), "Insufficient Balance", "You need " + finalUsedDays + " days but only have " + balanceLeave + " days available.\n\n" + "Please reduce days or apply for LOP.", "OK", "Cancel", new CustomDialogHelper.OnWarningActionListener() {
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
        if (FIRST_HALF_DAY.equals(from) && FIRST_HALF_DAY.equals(to)) {
            fUsedDays -= 0.5;
        } else if (SECOND_HALF_DAY.equals(from) && SECOND_HALF_DAY.equals(to)) {
            fUsedDays -= 0.5;
        }
        return fUsedDays;
    }

    // ══════════════════════════════════════════════════════════════════════
    // APPLY LEAVE
    // ══════════════════════════════════════════════════════════════════════

    private void applyLeave(String fromDate, String toDate, String leaveCategoryFrom, String leaveCategoryTo, String leavingStation, String leaveStationAdd, String contactNumber, String reason, String leavePlanned) {

        applyLeaveRequest = new ApplyLeaveRequest();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneId.of("UTC"));
        String appliedDate = formatter.format(Instant.now());

        applyLeaveRequest.setFromDate(fromDate);
        applyLeaveRequest.setSelectTypeFrom(leaveCategoryFrom);
        applyLeaveRequest.setToDate(toDate);
        applyLeaveRequest.setSelectTypeTo(leaveCategoryTo);
        applyLeaveRequest.setLeaveName(selectedLeaveTypeName);
        applyLeaveRequest.setLeavingStation(leavingStation);
        applyLeaveRequest.setVacationAddress(leaveStationAdd);
        applyLeaveRequest.setLeavetype(selectedLeaveTypeId);
        applyLeaveRequest.setContactNumber(contactNumber);
        applyLeaveRequest.setReason(reason);
        applyLeaveRequest.setLeavePlanned(leavePlanned);
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

        // ── Cross-functional manager guard ─────────────────────────────
        if (crossFunctionalManagerName == null || crossFunctionalManagerName.isEmpty() || crossFunctionalManagerEmail == null || crossFunctionalManagerEmail.isEmpty()) {
            CustomDialogHelper.showWarningDialog(requireContext(), "Manager Not Assigned", "No cross-functional manager is assigned" + " to your profile." + "\n\nPlease contact your Admin or HR" + " to get this resolved.", "OK", "Cancel", new CustomDialogHelper.OnWarningActionListener() {
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
                            if (!Objects.equals(selectedLeaveTypeId, lossOfPayId)) {
                                hideLoader();
                                showLoader();
                                debitLeave(fromDate, toDate, leaveCategoryFrom, leaveCategoryTo, leavingStation, leaveStationAdd, contactNumber, reason);
                            } else {
                                hideLoader();
                                CustomDialogHelper.showSuccessDialog(requireContext(), "Leave Applied! 🎉", "Your leave request has been" + " submitted successfully." + "\n\nYour manager will be" + " notified for approval.", () -> clearForm());
                            }
                        } else {
                            hideLoader();
                            binding.btnSubmit.setEnabled(true);
                            CustomDialogHelper.showErrorDialog(requireContext(), "Application Failed", "Server did not return a valid record." + "\nPlease try again.");
                        }
                    } else {
                        hideLoader();
                        binding.btnSubmit.setEnabled(true);
                        CustomDialogHelper.showErrorDialog(requireContext(), "Server Error", "Error code: " + response.code() + "\nPlease try again later.");
                    }
                } catch (Exception e) {
                    hideLoader();
                    binding.btnSubmit.setEnabled(true);
                    CustomDialogHelper.showErrorDialog(requireContext(), "Parsing Error", "Could not process server response." + "\n\n" + e.getMessage());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
                hideLoader();
                binding.btnSubmit.setEnabled(true);
                CustomDialogHelper.showErrorDialog(requireContext(), "Network Error", "Could not connect to the server." + "\nPlease check your internet" + " connection and try again.");
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

        debitLeaveRequest.setFromDate(fromDate);
        debitLeaveRequest.setSelectTypeFrom(leaveCategoryFrom);
        debitLeaveRequest.setToDate(toDate);
        debitLeaveRequest.setSelectTypeTo(leaveCategoryTo);
        debitLeaveRequest.setLeaveName(selectedLeaveTypeName);
        debitLeaveRequest.setLeavingStation(leavingStation);
        debitLeaveRequest.setVacationAddress(leaveStationAdd);
        debitLeaveRequest.setLeavetype(selectedLeaveTypeId);
        debitLeaveRequest.setContactNumber(contactNumber);
        debitLeaveRequest.setReason(reason);
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
        debitLeaveRequest.setTDays(totalDays);
        debitLeaveRequest.setFUsedDays(finalUsedDays);

        Call<ResponseBody> debitLeave = APIClient.getInstance().debitLeave().LeavesUsedDebit("jwt " + authToken, debitLeaveRequest);

        debitLeave.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                hideLoader();
                binding.btnSubmit.setEnabled(true);
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        response.body().string();
                        CustomDialogHelper.showSuccessDialog(requireContext(), "Leave Applied! 🎉", "Your leave request has been submitted" + " and balance has been updated." + "\n\nYour manager will review" + " and approve your request.", () -> clearForm());
                    } else {
                        CustomDialogHelper.showWarningDialog(requireContext(), "Partial Success", "Your leave was recorded successfully" + " but balance update failed." + "\n\nPlease contact HR to verify.", "OK", "Cancel", new CustomDialogHelper.OnWarningActionListener() {
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
                    CustomDialogHelper.showWarningDialog(requireContext(), "Partial Success", "Leave recorded but could not verify" + " balance update." + "\nPlease contact HR.", "OK", "Cancel", new CustomDialogHelper.OnWarningActionListener() {
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
                CustomDialogHelper.showWarningDialog(requireContext(), "Partial Success", "Leave recorded but network error occurred" + " during balance update." + "\nPlease contact HR.", "OK", "Cancel", new CustomDialogHelper.OnWarningActionListener() {
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
                        CustomDialogHelper.showErrorDialog(requireContext(), "Invalid Date Range", "'From' date cannot be after 'To' date" + " or before today.");
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

    /**
     * Clears ONLY the leave type spinner and balance text.
     * ✅ Keeps all other fields (dates, category, etc.) intact.
     * Used when restricted holiday validation fails.
     */
    public void clearLeaveTypeOnly() {
        binding.spinnerLeaveType.setText("", false);
        binding.balanceLeaveTextView.setText("");
        selectedLeaveTypeId = null;
        selectedLeaveTypeName = null;
    }

    /**
     * Clears ALL form fields completely.
     * Used after successful leave submission or full reset.
     */
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
        binding.btnSubmit.setEnabled(true);
        selectedLeaveTypeId = null;
        selectedLeaveTypeName = null;
    }
}
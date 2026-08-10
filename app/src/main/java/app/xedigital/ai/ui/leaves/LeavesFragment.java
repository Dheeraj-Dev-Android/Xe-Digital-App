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
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
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
    // ── LOP Leave Type ID (special - skip used count update) ───────────
    private static final String SPECIAL_LEAVE_TYPE_ID = "615418abc2432d4d14990ecc";
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
    private String empId;
    private String empName;
    private String empLastname;
    private String empEmail;
    private String hrMail;
    private String empDepartment;
    private String department;
    private String empBirthday;
    private String selectedLeaveTypeId;
    private String reportingManagerName;
    private String reportingManagerLastname;
    private String reportingManagerEmail;
    private String crossFunctionalManagerName;
    private String crossFunctionalManagerEmail;
    private String crossFunctionalManagerId;
    private String authToken;
    private String restrictedHolidayId;
    private String lossOfPayId;

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
        callUserApi(userId, authToken);

        // ── Disable Submit until profile is ready ──────────────────────
        binding.btnSubmit.setEnabled(false);

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

                // ✅ Enable submit only after all profile data is loaded
                binding.btnSubmit.setEnabled(true);
            }
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

                // ── Find special leave type IDs ────────────────────────
                for (LeavetypesItem leaveType : leaveTypesList) {
                    String name = leaveType.getLeavetypeName();

                    if ("Restricted Holidays".equals(name)) {
                        restrictedHolidayId = leaveType.getId();
                    }

                    // ✅ Fix: Take first match only (break after finding)
                    if (lossOfPayId == null) {
                        if ("Loss of Pay (LOP) / Leave Without Pay (LWP)".equals(name) || "LOP".equals(name)) {
                            lossOfPayId = leaveType.getId();
                        }
                    }
                }

            } else {
                Log.e(TAG, "Error fetching leaves type data");
            }
        });

        // ── Clear Button ───────────────────────────────────────────────
        binding.btnClear.setOnClickListener(view -> clearForm());

        // ── Leave Type Guard (dates must be selected first) ────────────
        leaveTypeSpinner.setOnClickListener(view -> {
            if (etToDate.getText().toString().isEmpty()) {
                new AlertDialog.Builder(requireContext()).setTitle("Select Dates").setMessage("Please select dates first.").setPositiveButton("OK", null).show();
            }
        });

        // ── Leave Type Spinner ─────────────────────────────────────────
        binding.spinnerLeaveType.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                LeavetypesItem selectedLeaveType = leaveTypesList.get(position);
                selectedLeaveTypeId = selectedLeaveType.getId();
                selectedLeaveTypeName = selectedLeaveType.getLeavetypeName();

                if (selectedLeaveTypeId.equals(restrictedHolidayId)) {
                    String fromDate = Objects.requireNonNull(binding.etFromDate.getText()).toString();
                    String toDate = Objects.requireNonNull(binding.etToDate.getText()).toString();
                    checkRestrictedHoliday(fromDate, toDate);
                }

                SecurePrefManager prefManager = SecurePrefManager.getInstance(requireContext());
                String authTokenN = prefManager.getString("authToken", "");
                String employeeId = prefManager.getString("userId", "");
                String authHeader = "jwt " + authTokenN;

                fetchEmployeeLeave(selectedLeaveTypeId, employeeId, authHeader, selectedLeaveTypeName);
                fetchLeaveTypeDetails(selectedLeaveTypeId, authHeader);
                fetchUnapprovedLeaves(selectedLeaveTypeId, employeeId, authHeader);
            }

            // ── Fetch Unapproved Leaves ────────────────────────────────
            private void fetchUnapprovedLeaves(String leaveTypeId, String employeeId, String authHeader) {
                Call<ResponseBody> call = APIClient.getInstance().getUnapprovedLeaves().getUnapprovedLeaves(authHeader, leaveTypeId, employeeId);

                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            try {
                                String responseBody = response.body().string();
                                Log.d(TAG, "Unapproved Leaves fetched successfully");
                            } catch (IOException e) {
                                Log.e(TAG, "Error reading Unapproved Leaves response: " + e.getMessage());
                            }
                        } else {
                            Log.e(TAG, "Error fetching Unapproved Leaves: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
                        Log.e(TAG, "Unapproved Leaves failure: " + throwable.getMessage());
                    }
                });
            }

            // ── Fetch Leave Type Details ───────────────────────────────
            private void fetchLeaveTypeDetails(String leaveTypeId, String authHeader) {
                Call<ResponseBody> call = APIClient.getInstance().getLeaveTypeDetails().getLeaveTypeDetails(authHeader, leaveTypeId);

                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            try {
                                String responseBody = response.body().string();
                                Log.d(TAG, "Leave Type Details fetched successfully");
                            } catch (IOException e) {
                                Log.e(TAG, "Error reading Leave Type Details: " + e.getMessage());
                            }
                        } else {
                            Log.e(TAG, "Error fetching Leave Type Details: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
                        Log.e(TAG, "Leave Type Details failure: " + throwable.getMessage());
                    }
                });
            }

            // ── Fetch Employee Leave Balance ───────────────────────────
            private void fetchEmployeeLeave(String leaveTypeId, String employeeId, String authHeader, String leaveTypeName) {
                Call<EmployeeLeaveTypeResponse> call = APIClient.getInstance().getEmployeeLeave().getEmployeeLeave(authHeader, leaveTypeId, employeeId);

                call.enqueue(new Callback<EmployeeLeaveTypeResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<EmployeeLeaveTypeResponse> call, @NonNull Response<EmployeeLeaveTypeResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            EmployeeLeaveTypeResponse employeeLeaveResponse = response.body();
                            balanceLeave = employeeLeaveResponse.getData().getCreditLeave() - (employeeLeaveResponse.getData().getUsedLeave() + employeeLeaveResponse.getData().getDebitLeave());

                            String fromDate = Objects.requireNonNull(binding.etFromDate.getText()).toString();
                            String toDate = Objects.requireNonNull(binding.etToDate.getText()).toString();

                            if (!fromDate.isEmpty() && !toDate.isEmpty()) {
                                calculateTotalDaysAndCheckLeaveLimit(leaveTypeName);
                            }

                            if (leaveTypeId != null && leaveTypeId.equals(restrictedHolidayId)) {
                                checkRestrictedHoliday(fromDate, toDate);
                            }

                            requireActivity().runOnUiThread(() -> {
                                if (leaveTypeName.equals("Loss of Pay (LOP) / Leave Without Pay (LWP)")) {
                                    binding.balanceLeaveTextView.setText("");
                                } else {
                                    if (balanceLeave == 0.0) {
                                        binding.balanceLeaveTextView.setText("Apply with Loss of Pay (LOP) / Leave Without Pay (LWP)" + "  Balance Leave: " + balanceLeave);
                                        Toast.makeText(requireContext(), "Loss of Pay (LOP) / Leave Without Pay (LWP)", Toast.LENGTH_SHORT).show();
                                    } else {
                                        binding.balanceLeaveTextView.setText("Balance Leave: " + balanceLeave);
                                    }
                                }
                            });

                        } else {
                            Log.e(TAG, "Error fetching Employee Leave: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<EmployeeLeaveTypeResponse> call, @NonNull Throwable throwable) {
                        Log.e(TAG, "Employee Leave failure: " + throwable.getMessage());
                    }
                });
            }

            // ── Restricted Holiday Check ───────────────────────────────
            private void checkRestrictedHoliday(String fromDate, String toDate) {
                Log.e(TAG, "Checking Restricted Holiday: " + fromDate + " to " + toDate);

                // Step 1: Birthday check
                String todayMD = LocalDate.now().format(DateTimeFormatter.ofPattern("MM-dd"));
                String empDOBMD = DateTimeUtils.getMonthDayFromISO(empBirthday);
                Log.e(TAG, "todayMD: " + todayMD + ", empDOBMD: " + empDOBMD);

                if (todayMD.equals(empDOBMD)) {
                    Log.d(TAG, "Today is employee's birthday — restricted leave allowed!");
                    binding.balanceLeaveTextView.setText("Today is your birthday. Restricted leave allowed!");
                    return;
                }

                // Step 2: Holiday list check
                holidaysViewModel.getHolidaysList().observe(getViewLifecycleOwner(), new Observer<List<HolidaysItem>>() {
                    @Override
                    public void onChanged(List<HolidaysItem> holidaysItems) {
                        if (holidaysItems != null) {
                            boolean isValidDateRange = true;
                            List<String> dateRange = getDatesBetween(fromDate, toDate);

                            for (String date : dateRange) {
                                boolean isRestrictedHoliday = false;
                                for (HolidaysItem holiday : holidaysItems) {
                                    if (holiday.getHolidayDate().equals(date) && holiday.isIsOptional()) {
                                        isRestrictedHoliday = true;
                                        break;
                                    }
                                }
                                if (!isRestrictedHoliday) {
                                    binding.balanceLeaveTextView.setText("Selected Date is not Restricted Holiday." + " Balance Leave: " + balanceLeave);
                                    isValidDateRange = false;
                                    break;
                                }
                            }

                            if (!isValidDateRange) {
                                new AlertDialog.Builder(requireContext()).setTitle("Error").setMessage("Selected date range contains " + "non-restricted holidays.").setPositiveButton("OK", (dialog, which) -> {
                                    dialog.dismiss();
                                    clearForm();
                                }).show();
                            } else {
                                Log.d(TAG, "Selected date range is valid");
                            }

                            holidaysViewModel.getHolidaysList().removeObserver(this);
                        } else {
                            Log.e(TAG, "Holiday data not available");
                            Toast.makeText(getContext(), "Holiday data not available", Toast.LENGTH_LONG).show();
                            clearForm();
                        }
                    }
                });
            }

            // ── Helper: Get dates between two date strings ─────────────
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
                    Log.e(TAG, "getDatesBetween parse error: " + e.getMessage());
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
    // VALIDATION METHODS
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Master form validation — runs all checks in order.
     * Returns true only if ALL validations pass.
     */
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
            showErrorAlert("Please fill in all required fields.");
            return false;
        }

        // ── Step 2: Leaving station address ───────────────────────────
        if (leavingStation.equalsIgnoreCase("Yes") && leaveStationAddress.isEmpty()) {
            showErrorAlert("Please enter your leave station address.");
            return false;
        }

        // ── Step 3: Contact number ────────────────────────────────────
        String contactNumber = Objects.requireNonNull(binding.etContactNumber.getText()).toString().trim();
        if (contactNumber.isEmpty()) {
            showErrorAlert("Please enter a contact number.");
            return false;
        }

        // ── Step 4: Leave planned ─────────────────────────────────────
        if (binding.spinnerLeavePlanned.getText().toString().isEmpty()) {
            binding.spinnerLeavePlanned.setError("This field is required");
            return false;
        } else {
            binding.spinnerLeavePlanned.setError(null);
        }

        // ── Step 5: Date range ────────────────────────────────────────
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date fromDate = sdf.parse(fromDateText);
            Date toDate = sdf.parse(toDateText);
            Date today = sdf.parse(sdf.format(new Date()));

            if (!leaveType.equals("Sick Leave") && fromDate != null && fromDate.before(today)) {
                showErrorAlert("Past dates are not allowed. Please select correct dates.");
                return false;
            }

            if (fromDate != null && fromDate.after(toDate)) {
                showErrorAlert("From date cannot be after To date.");
                return false;
            }

        } catch (ParseException e) {
            Log.e(TAG, "Date parse error: " + e.getMessage());
            showErrorAlert("Invalid date format.");
            return false;
        }

        // ── Step 6: Leave category combination validation ──────────────
        if (!isValidLeaveCategoryCombination(leaveCategoryFrom, leaveCategoryTo)) {
            showLeaveCombinationErrorDialog(leaveCategoryFrom, leaveCategoryTo);
            return false;
        }

        // ── Step 7: Single day half-day consistency ────────────────────
        if (!isValidSingleDayHalfDaySelection(fromDateText, toDateText, leaveCategoryFrom, leaveCategoryTo)) {
            showErrorAlert("Invalid selection for a single day leave.\n\n" + "\"First Half Day\" to \"Second Half Day\" on the same day means a full day.\n\n" + "Please select:\n" + "  • Full Day → Full Day\n" + "  • First Half Day → First Half Day (0.5 day)\n" + "  • Second Half Day → Second Half Day (0.5 day)");
            return false;
        }

        // ── All validations passed ─────────────────────────────────────
        return true;
    }

    /**
     * Checks whether the From/To category combination is valid.
     * <p>
     * Valid combinations (from TypeScript reference):
     * 1. Full Day        → Full Day         (full days, no adjustment)
     * 2. First Half Day  → First Half Day   (tDays - 0.5)
     * 3. First Half Day  → Second Half Day  (full days, no adjustment)
     * 4. Second Half Day → Second Half Day  (tDays - 0.5)
     */
    private boolean isValidLeaveCategoryCombination(String from, String to) {
        if (FULL_DAY.equals(from) && FULL_DAY.equals(to)) return true;
        if (FIRST_HALF_DAY.equals(from) && FIRST_HALF_DAY.equals(to)) return true;
        if (FIRST_HALF_DAY.equals(from) && SECOND_HALF_DAY.equals(to)) return true;
        if (SECOND_HALF_DAY.equals(from) && SECOND_HALF_DAY.equals(to)) return true;
        return false;
    }

    /**
     * For single-day leave: "First Half Day" → "Second Half Day"
     * on the SAME day equals a full day — user should select Full Day instead.
     */
    private boolean isValidSingleDayHalfDaySelection(String fromDate, String toDate, String from, String to) {
        if (!fromDate.equals(toDate)) return true; // multi-day — no restriction

        // Same day: First Half + Second Half = Full Day → invalid (use Full Day instead)
        if (FIRST_HALF_DAY.equals(from) && SECOND_HALF_DAY.equals(to)) return false;

        return true;
    }

    /**
     * Shows a descriptive error dialog for invalid category combinations
     * and resets only the category spinners so the user can re-select.
     */
    private void showLeaveCombinationErrorDialog(String from, String to) {
        String errorMessage = buildCombinationErrorMessage(from, to);

        new AlertDialog.Builder(requireContext()).setTitle("Invalid Leave Category Combination").setMessage(errorMessage).setPositiveButton("Fix Selection", (dialog, which) -> {
            // Reset only category spinners — keep dates & leave type intact
            binding.spinnerLeaveCategoryFrom.setText("", false);
            binding.spinnerLeaveCategoryTo.setText("", false);
            dialog.dismiss();
        }).setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss()).setCancelable(false).show();
    }

    /**
     * Builds a specific, human-readable error message for each invalid combination.
     */
    private String buildCombinationErrorMessage(String from, String to) {

        // Second Half → First Half (reversed — most common mistake)
        if (SECOND_HALF_DAY.equals(from) && FIRST_HALF_DAY.equals(to)) {
            return "\"Second Half Day\" → \"First Half Day\" is not allowed.\n\n" + "Did you mean:\n" + "  • First Half Day → First Half Day  (0.5 day)\n" + "  • First Half Day → Second Half Day (full days)\n" + "  • Second Half Day → Second Half Day (0.5 day)";
        }

        // Second Half → Full Day
        if (SECOND_HALF_DAY.equals(from) && FULL_DAY.equals(to)) {
            return "\"Second Half Day\" → \"Full Day\" is not allowed.\n\n" + "Valid options starting with Second Half Day:\n" + "  • Second Half Day → Second Half Day (0.5 day)";
        }

        // Full Day → First Half
        if (FULL_DAY.equals(from) && FIRST_HALF_DAY.equals(to)) {
            return "\"Full Day\" → \"First Half Day\" is not allowed.\n\n" + "Valid options starting with Full Day:\n" + "  • Full Day → Full Day (full days)";
        }

        // Full Day → Second Half
        if (FULL_DAY.equals(from) && SECOND_HALF_DAY.equals(to)) {
            return "\"Full Day\" → \"Second Half Day\" is not allowed.\n\n" + "Valid options starting with Full Day:\n" + "  • Full Day → Full Day (full days)";
        }

        // First Half → Full Day
        if (FIRST_HALF_DAY.equals(from) && FULL_DAY.equals(to)) {
            return "\"First Half Day\" → \"Full Day\" is not allowed.\n\n" + "Valid options starting with First Half Day:\n" + "  • First Half Day → First Half Day  (0.5 day)\n" + "  • First Half Day → Second Half Day (full days)";
        }

        // Generic fallback
        return "\"" + from + "\" → \"" + to + "\" is not a valid combination.\n\n" + "Valid combinations are:\n" + "  • Full Day → Full Day\n" + "  • First Half Day → First Half Day  (0.5 day)\n" + "  • First Half Day → Second Half Day (full days)\n" + "  • Second Half Day → Second Half Day (0.5 day)";
    }

    // ══════════════════════════════════════════════════════════════════════
    // DAY CALCULATION METHODS
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Matches TypeScript: totalDay(sDate, eDate)
     * Math.abs handles reversed dates safely.
     * +1 includes both start and end date.
     */
    private long calculateTotalDays(String startDate, String endDate) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate startDateObj = LocalDate.parse(startDate, formatter);
            LocalDate endDateObj = LocalDate.parse(endDate, formatter);
            long diff = Math.abs(ChronoUnit.DAYS.between(startDateObj, endDateObj));
            return diff + 1;
        } catch (DateTimeParseException e) {
            Log.e(TAG, "Error parsing dates: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Calculates tDays and finalUsedDays.
     * Matches TypeScript flow exactly:
     * 1. totalDay()      → raw calendar days
     * 2. finalUsedDays() → adjusted days after half-day logic
     */
    private void calculateTotalDaysAndCheckLeaveLimit(String leaveTypeName) {
        String fromDate = Objects.requireNonNull(binding.etFromDate.getText()).toString();
        String toDate = Objects.requireNonNull(binding.etToDate.getText()).toString();
        String leaveCategoryFrom = binding.spinnerLeaveCategoryFrom.getText().toString();
        String leaveCategoryTo = binding.spinnerLeaveCategoryTo.getText().toString();

        Log.w(TAG, "calculateTotalDays → from=" + fromDate + ", to=" + toDate + ", catFrom=" + leaveCategoryFrom + ", catTo=" + leaveCategoryTo);

        if (fromDate.isEmpty() || toDate.isEmpty() || leaveCategoryFrom.isEmpty() || leaveCategoryTo.isEmpty()) {
            return;
        }

        // Step 1: Raw total days
        long tDays = calculateTotalDays(fromDate, toDate);
        Log.e(TAG, "tDays (raw): " + tDays);

        // Step 2: Adjusted final used days
        totalDays = tDays;
        finalUsedDays = computeFinalUsedDays(tDays, leaveCategoryFrom, leaveCategoryTo);

        Log.d(TAG, "totalDays=" + totalDays + ", finalUsedDays=" + finalUsedDays + ", balanceLeave=" + balanceLeave);

        // Step 3: Balance check (use finalUsedDays for accurate comparison)
        if (finalUsedDays > balanceLeave && leaveTypeName != null && !leaveTypeName.equals("Loss of Pay (LOP) / Leave Without Pay (LWP)")) {
            showLeaveLimitExceededAlert();
        }
    }

    /**
     * Matches TypeScript finalUsedDays() exactly:
     * <p>
     * firsthalf  + firsthalf  → tDays - 0.5
     * firsthalf  + secondhalf → tDays        (no change)
     * secondhalf + secondhalf → tDays - 0.5
     * anything else           → tDays        (no change)
     */
    private double computeFinalUsedDays(long tDays, String from, String to) {
        double fUsedDays = tDays;

        if (FIRST_HALF_DAY.equals(from) && FIRST_HALF_DAY.equals(to)) {
            fUsedDays -= 0.5;

        } else if (FIRST_HALF_DAY.equals(from) && SECOND_HALF_DAY.equals(to)) {
            fUsedDays = tDays; // full days — no change

        } else if (SECOND_HALF_DAY.equals(from) && SECOND_HALF_DAY.equals(to)) {
            fUsedDays -= 0.5;
        }
        // FULL_DAY + FULL_DAY → no change (default)

        Log.d(TAG, "computeFinalUsedDays → tDays=" + tDays + ", from=" + from + ", to=" + to + ", fUsedDays=" + fUsedDays);

        return fUsedDays;
    }

    private void showLeaveLimitExceededAlert() {
        new AlertDialog.Builder(requireContext()).setTitle("Leave Limit Exceeded").setMessage("The total leave days exceed your available balance.").setPositiveButton("OK", (dialog, which) -> {
            dialog.dismiss();
            binding.spinnerLeaveType.setText("");
        }).show();
    }

    // ══════════════════════════════════════════════════════════════════════
    // APPLY LEAVE
    // ══════════════════════════════════════════════════════════════════════

    private void applyLeave(String fromDate, String toDate, String leaveCategoryFrom, String leaveCategoryTo, String leavingStation, String leaveStationAdd, String contactNumber, String reason, String leavePlanned) {

        applyLeaveRequest = new ApplyLeaveRequest();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneId.of("UTC"));
        String appliedDate = formatter.format(Instant.now());

        // Form data
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

        // Employee data
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

        // Cross-functional manager guard
        if (crossFunctionalManagerName == null || crossFunctionalManagerName.isEmpty() || crossFunctionalManagerEmail == null || crossFunctionalManagerEmail.isEmpty()) {
            showAlertDialog1();
            binding.btnSubmit.setEnabled(true);
            return;
        }
        applyLeaveRequest.setCrossManager(crossFunctionalManagerId);
        applyLeaveRequest.setCrossManagerEmail(crossFunctionalManagerEmail);
        applyLeaveRequest.setCrossManagerName(crossFunctionalManagerName);

        showLoading(true);

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
                            // ✅ Confirmed in DB
                            if (!Objects.equals(selectedLeaveTypeId, lossOfPayId)) {
                                // Debit leave balance (keep loading visible)
                                debitLeave(fromDate, toDate, leaveCategoryFrom, leaveCategoryTo, leavingStation, leaveStationAdd, contactNumber, reason);
                            } else {
                                showLoading(false);
                                showAlertDialog("Success", "Leave applied successfully.");
                                clearForm();
                            }
                        } else {
                            showLoading(false);
                            binding.btnSubmit.setEnabled(true);
                            showErrorAlert("Application failed: Server did not return a valid record ID.");
                        }
                    } else {
                        showLoading(false);
                        binding.btnSubmit.setEnabled(true);
                        showErrorAlert("Server error: " + response.code());
                    }
                } catch (Exception e) {
                    showLoading(false);
                    binding.btnSubmit.setEnabled(true);
                    showErrorAlert("Error parsing response: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
                showLoading(false);
                binding.btnSubmit.setEnabled(true);
                Log.e(TAG, "applyLeave onFailure: " + throwable.getMessage());
                showAlertDialog("Error", throwable.getMessage());
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

        // Form data
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

        // Employee data
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

        // ✅ Correct day values
        debitLeaveRequest.setTDays(totalDays);       // raw calendar days
        debitLeaveRequest.setFUsedDays(finalUsedDays); // adjusted deduction days

        Log.d(TAG, "debitLeave → tDays=" + totalDays + ", fUsedDays=" + finalUsedDays);

        Call<ResponseBody> debitLeave = APIClient.getInstance().debitLeave().LeavesUsedDebit("jwt " + authToken, debitLeaveRequest);

        debitLeave.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                showLoading(false);
                binding.btnSubmit.setEnabled(true);
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        String responseBody = response.body().string();
                        JSONObject jsonObject = new JSONObject(responseBody);
                        String message = jsonObject.optString("message", "Leave balance updated.");
                        showAlertDialog("Success", "Leave applied and balance updated.");
                        clearForm();
                    } else {
                        showErrorAlert("Leave recorded, but balance update failed. Please contact HR.");
                    }
                } catch (IOException | JSONException e) {
                    Log.e(TAG, "debitLeave parse error: " + e.getMessage());
                    showErrorAlert("Leave recorded, but error reading response. Please contact HR.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
                showLoading(false);
                binding.btnSubmit.setEnabled(true);
                Log.e(TAG, "debitLeave onFailure: " + throwable.getMessage());
                showErrorAlert("Leave recorded, but network error during balance update.");
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

        // ✅ Fixed: Use Calendar for yesterday to avoid DST issues
        Calendar yesterdayCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        yesterdayCal.add(Calendar.DAY_OF_MONTH, -1);
        yesterdayCal.set(Calendar.HOUR_OF_DAY, 0);
        yesterdayCal.set(Calendar.MINUTE, 0);
        yesterdayCal.set(Calendar.SECOND, 0);
        yesterdayCal.set(Calendar.MILLISECOND, 0);
        long yesterdayInMillis = yesterdayCal.getTimeInMillis();

        // Current year bounds
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
            // Sick Leave: allow yesterday, today & future
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
            // Other leaves: today & future only
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
                        showErrorAlert("From date cannot be after To date.");
                    } else {
                        binding.spinnerLeaveType.setEnabled(true);
                    }
                } else {
                    if (fromDate != null && (fromDate.after(toDate) || fromDate.before(today))) {
                        showErrorAlert("From date cannot be after To date or before today.");
                    } else {
                        binding.spinnerLeaveType.setEnabled(true);
                    }
                }
            } catch (ParseException e) {
                Log.e(TAG, "validateDateRange parse error: " + e.getMessage());
                showErrorAlert("Invalid date format.");
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
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserModelResponse> call, @NonNull Throwable throwable) {
                Log.d(TAG, "callUserApi onFailure: " + throwable.getMessage());
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
                        Log.d(TAG, "HR Mail loaded: " + hrMail);
                    } catch (Exception e) {
                        Log.e(TAG, "callBranchApi parse error: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserBranchResponse> call, @NonNull Throwable throwable) {
                Log.d(TAG, "callBranchApi onFailure: " + throwable.getMessage());
            }
        });
    }

    // ══════════════════════════════════════════════════════════════════════
    // UI HELPERS
    // ══════════════════════════════════════════════════════════════════════

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
    }

    private void showAlertDialog(String title, String message) {
        new AlertDialog.Builder(requireContext()).setTitle(title).setMessage(message).setPositiveButton("OK", (dialog, which) -> {
            clearForm();
            dialog.dismiss();
        }).show();
    }

    private void showAlertDialog1() {
        new AlertDialog.Builder(requireContext()).setTitle("No Cross-functional Manager").setMessage("No cross-functional manager available. Please contact your Admin or HR.").setPositiveButton("OK", (dialog, which) -> {
            clearForm();
            dialog.dismiss();
        }).show();
    }

    private void showErrorAlert(String message) {
        new AlertDialog.Builder(requireContext()).setTitle("Error").setMessage(message).setPositiveButton("OK", null).show();
    }

    private void showLoading(boolean show) {
        if (show) {
            if (loadingDialog == null) {
                ProgressBar progressBar = new ProgressBar(requireContext());
                progressBar.setPadding(50, 50, 50, 50);
                loadingDialog = new AlertDialog.Builder(requireContext()).setView(progressBar).setCancelable(false).create();
                if (loadingDialog.getWindow() != null) {
                    loadingDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                }
            }
            loadingDialog.show();
        } else {
            if (loadingDialog != null && loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }
        }
    }
}
package app.xedigital.ai.ui.regularize_attendance;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;

import app.xedigital.ai.R;
import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.api.APIInterface;
import app.xedigital.ai.model.attendance.EmployeePunchDataItem;
import app.xedigital.ai.model.profile.UserProfileResponse;
import app.xedigital.ai.model.regularize.RegularizeAttendanceRequest;
import app.xedigital.ai.ui.profile.ProfileViewModel;
import app.xedigital.ai.utills.CustomDialogHelper;
import app.xedigital.ai.utills.SecurePrefManager;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegularizeFragment extends Fragment {

    public static final String ARG_ATTENDANCE_ITEM = "attendanceItem";
    private static final String TAG = "RegularizeFragment";
    // ── Active loader counter ──────────────────────────────────────────
    private final AtomicInteger activeLoaderCount = new AtomicInteger(0);

    private EmployeePunchDataItem attendanceItem;
    private APIInterface apiInterface;
    private String token;
    private TextInputEditText atDate;
    private TextInputEditText timePunchIn;
    private TextInputEditText timePunchOut;
    private TextInputEditText etPunchInAddress;
    private TextInputEditText etPunchOutAddress;
    private TextInputEditText etRemarks;
    private Button btSubmit;
    private Button btnClear;
    private ProfileViewModel profileViewModel;
    private UserProfileResponse userProfile;
    private android.app.AlertDialog loadingDialog;

    // ══════════════════════════════════════════════════════════════════════
    // LOADER
    // ══════════════════════════════════════════════════════════════════════

    private void showLoader() {
        activeLoaderCount.incrementAndGet();
        requireActivity().runOnUiThread(() -> {
            if (loadingDialog == null) {
                loadingDialog = CustomDialogHelper.showLoadingDialog(requireContext(), "Please wait...");
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
    // LIFECYCLE — onCreate
    // ══════════════════════════════════════════════════════════════════════

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiInterface = APIClient.getInstance().RegularizeAttendance();
        if (getArguments() != null) {
            attendanceItem = (EmployeePunchDataItem) getArguments().getSerializable(ARG_ATTENDANCE_ITEM);
            if (attendanceItem == null) {
                Log.d(TAG, "Attendance Item is null");
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // LIFECYCLE — onCreateView
    // ══════════════════════════════════════════════════════════════════════

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_regularize, container, false);

        profileViewModel = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);

        atDate = view.findViewById(R.id.atDate);
        timePunchIn = view.findViewById(R.id.timePunchIn);
        timePunchOut = view.findViewById(R.id.timePunchOut);
        etPunchInAddress = view.findViewById(R.id.etPunchInAddress);
        etPunchOutAddress = view.findViewById(R.id.etPunchOutAddress);
        etRemarks = view.findViewById(R.id.etRemarks);
        btSubmit = view.findViewById(R.id.btSubmit);
        btnClear = view.findViewById(R.id.btClear);

        return view;
    }

    // ══════════════════════════════════════════════════════════════════════
    // LIFECYCLE — onViewCreated
    // ══════════════════════════════════════════════════════════════════════

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SecurePrefManager prefManager = SecurePrefManager.getInstance(requireContext());
        token = prefManager.getString("authToken", "");
        String authToken = prefManager.getString("authToken", "");
        String userId = prefManager.getString("userId", "");

        profileViewModel = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);
        profileViewModel.storeLoginData(userId, authToken);

        // ── Show loader while profile loads ────────────────────────────
        showLoader();

        // ── Profile Observer ───────────────────────────────────────────
        profileViewModel.userProfile.observe(getViewLifecycleOwner(), userProfile -> {
            if (userProfile != null) {
                this.userProfile = userProfile;
                Log.d(TAG, "User profile loaded successfully");
            } else {
                Log.d(TAG, "User Profile is null");
                CustomDialogHelper.showErrorDialog(requireContext(), "Profile Error", "Could not load your profile data." + "\nSome fields may not" + " be filled automatically.");
            }
            hideLoader();
        });

        profileViewModel.fetchUserProfile();

        // ── Safe Autofill Logic ────────────────────────────────────────
        if (attendanceItem != null) {
            autofillAttendanceFields();
        }

        // ── Submit Button ──────────────────────────────────────────────
        btSubmit.setOnClickListener(v -> {
            if (validateForm()) {
                String date = Objects.requireNonNull(atDate.getText()).toString();
                String punchIn = Objects.requireNonNull(timePunchIn.getText()).toString();
                String punchOut = Objects.requireNonNull(timePunchOut.getText()).toString();
                String punchInAddress = Objects.requireNonNull(etPunchInAddress.getText()).toString();
                String punchOutAddress = Objects.requireNonNull(etPunchOutAddress.getText()).toString();
                String remarks = Objects.requireNonNull(etRemarks.getText()).toString();

                if (attendanceItem != null) {
                    regularize(token, attendanceItem.getId(), date, punchIn, punchOut, punchInAddress, punchOutAddress, remarks);
                } else {
                    Log.d(TAG, "Attendance Item is null");
                    CustomDialogHelper.showErrorDialog(requireContext(), "Missing Data", "Attendance record not found." + "\nPlease go back and try again.");
                }
            }
        });

        // ── Clear Button ───────────────────────────────────────────────
        btnClear.setOnClickListener(v -> CustomDialogHelper.showWarningDialog(requireContext(), "Clear Form", "Are you sure you want to clear all fields?" + "\nThis action cannot be undone.", "Yes, Clear", "Cancel", new CustomDialogHelper.OnWarningActionListener() {
            @Override
            public void onConfirm() {
                clearForm();
            }

            @Override
            public void onCancel() {
                // User cancelled — do nothing
            }
        }));

        // ── Punch In Time Picker ───────────────────────────────────────
        timePunchIn.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            new TimePickerDialog(requireContext(), (view12, hourOfDay, minute1) -> {
                String selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute1);
                timePunchIn.setText(selectedTime);
            }, hour, minute, false).show();
        });

        // ── Punch Out Time Picker ──────────────────────────────────────
        timePunchOut.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            new TimePickerDialog(requireContext(), (view12, hourOfDay, minute1) -> {
                String selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute1);
                timePunchOut.setText(selectedTime);
            }, hour, minute, false).show();
        });
    }

    // ══════════════════════════════════════════════════════════════════════
    // AUTOFILL
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Safely autofills attendance fields from attendanceItem.
     * <p>
     * Handles all these invalid value cases gracefully:
     * • null
     * • ""  (empty string)
     * • "NA"
     * • "N/A"
     * • "null"
     * • Any string that fails date/time parsing
     */
    private void autofillAttendanceFields() {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm", Locale.getDefault());
        timeFormatter.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));

        SimpleDateFormat inputFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        inputFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));

        String punchDateString = attendanceItem.getPunchDate();
        String punchInString = attendanceItem.getPunchIn();
        String punchOutString = attendanceItem.getPunchOut();

        Log.d(TAG, "autofill → punchDate='" + punchDateString + "' punchIn='" + punchInString + "' punchOut='" + punchOutString + "'");

        // ── Punch Date ─────────────────────────────────────────────────
        if (isValidDateString(punchDateString)) {
            try {
                Date punchDate = inputFormatter.parse(punchDateString);
                if (punchDate != null) {
                    atDate.setText(dateFormatter.format(punchDate));
                    atDate.setFocusable(false);
                    atDate.clearFocus();
                    Log.d(TAG, "punchDate set: " + dateFormatter.format(punchDate));
                }
            } catch (ParseException e) {
                Log.e(TAG, "punchDate parse failed: '" + punchDateString + "' → " + e.getMessage());
                atDate.setText("");
            }
        } else {
            Log.w(TAG, "punchDate skipped (invalid value): '" + punchDateString + "'");
            atDate.setText("");
        }

        // ── Punch In ───────────────────────────────────────────────────
        if (isValidDateString(punchInString)) {
            try {
                Date punchInTime = inputFormatter.parse(punchInString);
                if (punchInTime != null) {
                    timePunchIn.setText(timeFormatter.format(punchInTime));
                    Log.d(TAG, "punchIn set: " + timeFormatter.format(punchInTime));
                }
            } catch (ParseException e) {
                Log.e(TAG, "punchIn parse failed: '" + punchInString + "' → " + e.getMessage());
                timePunchIn.setText("");
            }
        } else {
            Log.w(TAG, "punchIn skipped (invalid value): '" + punchInString + "'");
            timePunchIn.setText("");
        }

        // ── Punch Out ──────────────────────────────────────────────────
        if (isValidDateString(punchOutString)) {
            try {
                Date punchOutTime = inputFormatter.parse(punchOutString);
                if (punchOutTime != null) {
                    timePunchOut.setText(timeFormatter.format(punchOutTime));
                    Log.d(TAG, "punchOut set: " + timeFormatter.format(punchOutTime));
                }
            } catch (ParseException e) {
                Log.e(TAG, "punchOut parse failed: '" + punchOutString + "' → " + e.getMessage());
                timePunchOut.setText("");
            }
        } else {
            Log.w(TAG, "punchOut skipped (invalid value): '" + punchOutString + "'");
            timePunchOut.setText("");
        }

        // ── Addresses ──────────────────────────────────────────────────
        String punchInAddress = attendanceItem.getPunchInAddress();
        String punchOutAddress = attendanceItem.getPunchOutAddress();

        etPunchInAddress.setText(isValidStringValue(punchInAddress) ? punchInAddress : "");
        etPunchOutAddress.setText(isValidStringValue(punchOutAddress) ? punchOutAddress : "");

        Log.d(TAG, "autofillAttendanceFields() completed");
    }

    // ══════════════════════════════════════════════════════════════════════
    // VALIDATION HELPERS
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Returns true only if the string is a potentially parseable
     * date/time string.
     * <p>
     * Rejects: null, "", "NA", "N/A", "null", "undefined", "none"
     */
    private boolean isValidDateString(String value) {
        if (value == null) return false;
        String trimmed = value.trim();
        if (trimmed.isEmpty()) return false;

        // ✅ Reject all known invalid placeholder values
        switch (trimmed.toUpperCase(Locale.getDefault())) {
            case "NA":
            case "N/A":
            case "NULL":
            case "UNDEFINED":
            case "NONE":
            case "-":
            case "--":
                return false;
        }

        return true;
    }

    /**
     * Returns true if string is a non-null, non-empty,
     * non-placeholder value safe to display in a text field.
     */
    private boolean isValidStringValue(String value) {
        if (value == null) return false;
        String trimmed = value.trim();
        if (trimmed.isEmpty()) return false;

        switch (trimmed.toUpperCase(Locale.getDefault())) {
            case "NA":
            case "N/A":
            case "NULL":
            case "UNDEFINED":
            case "NONE":
            case "-":
            case "--":
                return false;
        }

        return true;
    }

    // ══════════════════════════════════════════════════════════════════════
    // FORM VALIDATION
    // ══════════════════════════════════════════════════════════════════════

    private boolean validateForm() {
        boolean isValid = true;

        // ── Punch In Time ──────────────────────────────────────────────
        if (Objects.requireNonNull(timePunchIn.getText()).toString().isEmpty()) {
            timePunchIn.setError("Punch-in time is required");
            isValid = false;
        } else {
            timePunchIn.setError(null);
        }

        // ── Punch Out Time ─────────────────────────────────────────────
        if (Objects.requireNonNull(timePunchOut.getText()).toString().isEmpty()) {
            timePunchOut.setError("Punch-out time is required");
            isValid = false;
        } else {
            timePunchOut.setError(null);
        }

        // ── Punch In Address ───────────────────────────────────────────
        if (Objects.requireNonNull(etPunchInAddress.getText()).toString().isEmpty()) {
            etPunchInAddress.setError("Punch-in address is required");
            isValid = false;
        } else {
            etPunchInAddress.setError(null);
        }

        // ── Punch Out Address ──────────────────────────────────────────
        if (Objects.requireNonNull(etPunchOutAddress.getText()).toString().isEmpty()) {
            etPunchOutAddress.setError("Punch-out address is required");
            isValid = false;
        } else {
            etPunchOutAddress.setError(null);
        }

        // ── Remarks ────────────────────────────────────────────────────
        if (Objects.requireNonNull(etRemarks.getText()).toString().isEmpty()) {
            etRemarks.setError("Remarks is required");
            isValid = false;
        } else {
            etRemarks.setError(null);
        }

        // ── Summary dialog if any field is missing ─────────────────────
        if (!isValid) {
            CustomDialogHelper.showWarningDialog(requireContext(), "Missing Fields", "Please fill in all required fields" + " before submitting.", "OK", "Cancel", new CustomDialogHelper.OnWarningActionListener() {
                @Override
                public void onConfirm() {
                }

                @Override
                public void onCancel() {
                }
            });
        }

        return isValid;
    }

    // ══════════════════════════════════════════════════════════════════════
    // REGULARIZE API CALL
    // ══════════════════════════════════════════════════════════════════════

    private void regularize(String token, String id, String date, String punchIn, String punchOut, String punchInAddress, String punchOutAddress, String remarks) {

        String attendanceId = attendanceItem != null ? String.valueOf(attendanceItem.getId()) : "";

        RegularizeAttendanceRequest requestBody = new RegularizeAttendanceRequest();

        requestBody.setAttendenceRegularizationRemark(remarks);
        requestBody.setEmpDepartment(attendanceItem.getEmployee().getDepartment());
        requestBody.setEmpDesignation(attendanceItem.getEmployee().getDesignation());
        requestBody.setEmployee(attendanceItem.getEmployee().getId());
        requestBody.setEmployeeEmail(attendanceItem.getEmpEmail());
        requestBody.setEmployeeFirstName(attendanceItem.getEmployee().getFirstname());
        requestBody.setEmployeeId(attendanceItem.getEmployee().getEmployeeCode());
        requestBody.setEmployeeLastName(attendanceItem.getEmpLastName());
        requestBody.setHrEmail("hr@cloudfence.ai");
        requestBody.setPunchIn(attendanceItem.getPunchIn());

        // ── Fixed Date Logic ───────────────────────────────────────────
        String dateFieldStr = Objects.requireNonNull(atDate.getText()).toString();

        // ── Guard: date field must not be empty ────────────────────────
        if (dateFieldStr.isEmpty()) {
            CustomDialogHelper.showErrorDialog(requireContext(), "Date Missing", "The attendance date is missing." + "\nPlease go back and select" + " a valid attendance record.");
            return;
        }

        DateTimeFormatter dateFieldFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault());
        LocalDate punchDate;
        try {
            punchDate = LocalDate.parse(dateFieldStr, dateFieldFormatter);
        } catch (Exception e) {
            Log.e(TAG, "Date field parse error: " + e.getMessage());
            CustomDialogHelper.showErrorDialog(requireContext(), "Invalid Date", "The attendance date format is invalid." + "\nPlease go back and select" + " a valid attendance record.");
            return;
        }

        requestBody.setPunchDate(dateFieldStr);
        requestBody.setPunchInAddress(punchInAddress);
        requestBody.setPunchInTime(punchIn);
        requestBody.setPunchOut(attendanceItem.getPunchOut());
        requestBody.setPunchOutAddress(punchOutAddress);
        requestBody.setPunchOutTime(punchOut);

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault());
        ZoneId inputTimeZone = ZoneId.of("Asia/Kolkata");
        DateTimeFormatter outFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());

        try {
            // ── PunchIn UTC conversion ─────────────────────────────────
            LocalTime localPunchInTime = LocalTime.parse(punchIn, timeFormatter);
            ZonedDateTime zonedPunchInTime = ZonedDateTime.of(punchDate, localPunchInTime, inputTimeZone);
            OffsetDateTime utcPunchInTime = zonedPunchInTime.withZoneSameInstant(ZoneOffset.UTC).toOffsetDateTime();
            requestBody.setPunchInUpdate(utcPunchInTime.format(outFormatter));

            // ── PunchOut UTC conversion ────────────────────────────────
            LocalTime localPunchOutTime = LocalTime.parse(punchOut, timeFormatter);
            ZonedDateTime zonedPunchOutTime = ZonedDateTime.of(punchDate, localPunchOutTime, inputTimeZone);
            OffsetDateTime utcPunchOutTime = zonedPunchOutTime.withZoneSameInstant(ZoneOffset.UTC).toOffsetDateTime();
            requestBody.setPunchOutUpdate(utcPunchOutTime.format(outFormatter));

        } catch (Exception e) {
            Log.e(TAG, "Time UTC conversion error: " + e.getMessage());
            CustomDialogHelper.showErrorDialog(requireContext(), "Time Error", "Could not process the selected punch times." + "\nPlease check your time inputs" + " and try again.");
            return;
        }

        // ── Reporting & Cross Manager ──────────────────────────────────
        if (userProfile != null) {
            try {
                String reportingManagerId = userProfile.getData().getEmployee().getReportingManager().getId();
                String reportingManagerEmail = userProfile.getData().getEmployee().getReportingManager().getEmail();
                String reportingManagerFirstName = userProfile.getData().getEmployee().getReportingManager().getFirstname();
                String reportingManagerLastName = userProfile.getData().getEmployee().getReportingManager().getLastname();
                String crossManager = String.valueOf(userProfile.getData().getEmployee().getCrossmanager().getId());

                Log.d(TAG, "Cross Manager ID: " + crossManager);

                requestBody.setReportingManager(reportingManagerId);
                requestBody.setReportingManagerEmail(reportingManagerEmail);
                requestBody.setReportingManagerFirstName(reportingManagerFirstName);
                requestBody.setReportingManagerLastName(reportingManagerLastName);
                requestBody.setCrossManager(crossManager);

            } catch (Exception e) {
                Log.e(TAG, "Manager data error: " + e.getMessage());
                CustomDialogHelper.showErrorDialog(requireContext(), "Manager Data Error", "Could not read your manager information." + "\nPlease contact HR.");
                return;
            }
        } else {
            CustomDialogHelper.showErrorDialog(requireContext(), "Profile Not Loaded", "Your profile data is not loaded yet." + "\nPlease wait a moment and try again.");
            return;
        }

        // ── Show loader & disable submit ───────────────────────────────
        showLoader();
        btSubmit.setEnabled(false);

        Call<ResponseBody> call = apiInterface.RegularizeApi("jwt " + token, attendanceId, requestBody);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                hideLoader();
                btSubmit.setEnabled(true);

                try {
                    if (response.isSuccessful() && response.body() != null) {
                        String responseBodyString = response.body().string();
                        JSONObject jsonObject = new JSONObject(responseBodyString);
                        String message = jsonObject.optString("message", "Regularization submitted.");

                        CustomDialogHelper.showSuccessDialog(requireContext(), "Request Submitted! ✅", message + "\n\nYour attendance" + " regularization request" + " has been sent for approval.", () -> clearForm());

                    } else {
                        CustomDialogHelper.showErrorDialog(requireContext(), "Submission Failed", "Regularization request failed." + "\nServer error: " + response.code() + "\n\nPlease try again later.");
                    }
                } catch (IOException | JSONException e) {
                    Log.e(TAG, "Response parse error: " + e.getMessage());
                    CustomDialogHelper.showErrorDialog(requireContext(), "Parsing Error", "Could not process the server response." + "\nPlease try again or" + " contact HR.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
                hideLoader();
                btSubmit.setEnabled(true);
                Log.e(TAG, "API failure: " + throwable.getMessage());

                CustomDialogHelper.showErrorDialog(requireContext(), "Network Error", "Could not connect to the server." + "\nPlease check your internet" + " connection and try again.");
            }
        });
    }

    // ══════════════════════════════════════════════════════════════════════
    // UI HELPERS
    // ══════════════════════════════════════════════════════════════════════

    private void clearForm() {
        timePunchIn.setText("");
        timePunchOut.setText("");
        etPunchInAddress.setText("");
        etPunchOutAddress.setText("");
        etRemarks.setText("");
    }
}
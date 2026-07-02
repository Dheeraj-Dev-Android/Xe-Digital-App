package app.xedigital.ai.ui.regularize_attendance;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

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

import app.xedigital.ai.R;
import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.api.APIInterface;
import app.xedigital.ai.model.attendance.EmployeePunchDataItem;
import app.xedigital.ai.model.profile.UserProfileResponse;
import app.xedigital.ai.model.regularize.RegularizeAttendanceRequest;
import app.xedigital.ai.ui.profile.ProfileViewModel;
import app.xedigital.ai.utills.SecurePrefManager;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegularizeFragment extends Fragment {

    public static final String ARG_ATTENDANCE_ITEM = "attendanceItem";
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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SecurePrefManager prefManager = SecurePrefManager.getInstance(requireContext());
        token = prefManager.getString("authToken", "");
        String authToken = prefManager.getString("authToken", "");
        String userId = prefManager.getString("userId", "");
        profileViewModel = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);
        profileViewModel.storeLoginData(userId, authToken);

        profileViewModel.userProfile.observe(getViewLifecycleOwner(), userProfile -> {
            if (userProfile != null) {
                this.userProfile = userProfile;
            } else {
                Log.d("RegularizeFragment", "User Profile is null");
            }
        });

        profileViewModel.fetchUserProfile();

        // --- Safe Autofill Logic ---
        if (attendanceItem != null) {
            SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm", Locale.getDefault());
            timeFormatter.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));

            try {
                SimpleDateFormat inputFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                inputFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));

                String punchDateString = attendanceItem.getPunchDate();
                String punchInString = attendanceItem.getPunchIn();
                String punchOutString = attendanceItem.getPunchOut();

                Date punchDate = null;
                Date punchInTime = null;
                Date punchOutTime = null;

                if (punchDateString != null && !punchDateString.isEmpty()) {
                    punchDate = inputFormatter.parse(punchDateString);
                }

                if (punchInString != null && !punchInString.isEmpty()) {
                    punchInTime = inputFormatter.parse(punchInString);
                }

                if (punchOutString != null && !punchOutString.isEmpty()) {
                    punchOutTime = inputFormatter.parse(punchOutString);
                }

                if (punchDate != null) {
                    atDate.setText(dateFormatter.format(punchDate));
                    atDate.setFocusable(false);
                    atDate.clearFocus();
                }

                if (punchInTime != null) {
                    timePunchIn.setText(timeFormatter.format(punchInTime));
                } else {
                    timePunchIn.setText("");
                }

                if (punchOutTime != null) {
                    timePunchOut.setText(timeFormatter.format(punchOutTime));
                } else {
                    timePunchOut.setText("");
                }

                // Handles cases where addresses are null without causing a crash
                etPunchInAddress.setText(attendanceItem.getPunchInAddress() != null ? attendanceItem.getPunchInAddress() : "");
                etPunchOutAddress.setText(attendanceItem.getPunchOutAddress() != null ? attendanceItem.getPunchOutAddress() : "");

            } catch (ParseException e) {
                Log.e("RegularizeFragment", "Error parsing date/time: " + e.getMessage());
            }
        }

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
                    Log.d("RegularizeFragment", "Attendance Item is null");
                    Toast.makeText(requireContext(), "Attendance Item is null", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnClear.setOnClickListener(v -> clearForm());

        timePunchIn.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(requireContext(), (view12, hourOfDay, minute1) -> {
                // Formats with %02d to ensure standard HH:mm layout
                String selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute1);
                timePunchIn.setText(selectedTime);
            }, hour, minute, false);
            timePickerDialog.show();
        });

        timePunchOut.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(requireContext(), (view12, hourOfDay, minute1) -> {
                // Formats with %02d to ensure standard HH:mm layout
                String selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute1);
                timePunchOut.setText(selectedTime);
            }, hour, minute, false);
            timePickerDialog.show();
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiInterface = APIClient.getInstance().RegularizeAttendance();
        if (getArguments() != null) {
            attendanceItem = (EmployeePunchDataItem) getArguments().getSerializable(ARG_ATTENDANCE_ITEM);
            if (attendanceItem == null) {
                Log.d("RegularizeFragment", "Attendance Item is null");
            }
        }
    }

    private void clearForm() {
        timePunchIn.setText("");
        timePunchOut.setText("");
        etPunchInAddress.setText("");
        etPunchOutAddress.setText("");
        etRemarks.setText("");
    }

    private boolean validateForm() {
        boolean isValid = true;
        if (Objects.requireNonNull(timePunchIn.getText()).toString().isEmpty()) {
            timePunchIn.setError("Punch-in time is required");
            isValid = false;
        }
        if (Objects.requireNonNull(timePunchOut.getText()).toString().isEmpty()) {
            timePunchOut.setError("Punch-out time is required");
            isValid = false;
        }
        if (Objects.requireNonNull(etPunchInAddress.getText()).toString().isEmpty()) {
            etPunchInAddress.setError("Punch-in address is required");
            isValid = false;
        }
        if (Objects.requireNonNull(etPunchOutAddress.getText()).toString().isEmpty()) {
            etPunchOutAddress.setError("Punch-out address is required");
            isValid = false;
        }
        if (Objects.requireNonNull(etRemarks.getText()).toString().isEmpty()) {
            etRemarks.setError("Remarks is required");
            isValid = false;
        }
        return isValid;
    }

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

        // --- Fixed Date Logic ---
        // Reads directly from user-selected field to guarantee calculations build on the proper day
        String dateFieldStr = Objects.requireNonNull(atDate.getText()).toString();
        DateTimeFormatter dateFieldFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault());
        LocalDate punchDate = LocalDate.parse(dateFieldStr, dateFieldFormatter);

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
            // --- punchIn ---
            LocalTime localPunchInTime = LocalTime.parse(punchIn, timeFormatter);
            ZonedDateTime zonedPunchInTime = ZonedDateTime.of(punchDate, localPunchInTime, inputTimeZone);
            OffsetDateTime utcPunchInTime = zonedPunchInTime.withZoneSameInstant(ZoneOffset.UTC).toOffsetDateTime();
            String punchInUpdate = utcPunchInTime.format(outFormatter);
            requestBody.setPunchInUpdate(punchInUpdate);

            // --- punchOut ---
            LocalTime localPunchOutTime = LocalTime.parse(punchOut, timeFormatter);
            ZonedDateTime zonedPunchOutTime = ZonedDateTime.of(punchDate, localPunchOutTime, inputTimeZone);
            OffsetDateTime utcPunchOutTime = zonedPunchOutTime.withZoneSameInstant(ZoneOffset.UTC).toOffsetDateTime();
            String punchOutUpdate = utcPunchOutTime.format(outFormatter);
            requestBody.setPunchOutUpdate(punchOutUpdate);

        } catch (Exception e) {
            Log.e("RegularizeFragment", "Error converting times to UTC: " + e.getMessage());
            Toast.makeText(requireContext(), "Error processing selected times.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (userProfile != null) {
            String reportingManagerId = userProfile.getData().getEmployee().getReportingManager().getId();
            String reportingManagerEmail = userProfile.getData().getEmployee().getReportingManager().getEmail();
            String reportingManagerFirstName = userProfile.getData().getEmployee().getReportingManager().getFirstname();
            String reportingManagerLastName = userProfile.getData().getEmployee().getReportingManager().getLastname();
            String crossManager = String.valueOf(userProfile.getData().getEmployee().getCrossmanager().getId());
            Log.d("RegularizeFragment", "Cross Manager Manager ID: " + crossManager);

            requestBody.setReportingManager(reportingManagerId);
            requestBody.setReportingManagerEmail(reportingManagerEmail);
            requestBody.setReportingManagerFirstName(reportingManagerFirstName);
            requestBody.setReportingManagerLastName(reportingManagerLastName);
            requestBody.setCrossManager(crossManager);
        }

        Call<ResponseBody> call = apiInterface.RegularizeApi("jwt " + token, attendanceId, requestBody);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        String responseBodyString = response.body().string();
                        JSONObject jsonObject = new JSONObject(responseBodyString);
                        String message = jsonObject.getString("message");
                        showAlertDialog("Attendance Regularize Applied", message);
                    } else {
                        showAlertDialog("Error", "Regularization failed");
                    }
                } catch (IOException | JSONException e) {
                    showAlertDialog("Error", "An error occurred parsing server response");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
                showAlertDialog("Error", "Network connection failure");
            }

            private void showAlertDialog(String title, String message) {
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setTitle(title).setMessage(message).setPositiveButton("OK", (dialog, which) -> {
                    clearForm();
                    dialog.dismiss();
                }).show();
            }
        });
    }
}
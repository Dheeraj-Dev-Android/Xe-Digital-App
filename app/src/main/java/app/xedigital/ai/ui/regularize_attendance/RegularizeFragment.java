package app.xedigital.ai.ui.regularize_attendance;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
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

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        token = sharedPreferences.getString("authToken", "");
        String authToken = sharedPreferences.getString("authToken", "");
        String userId = sharedPreferences.getString("userId", "");
        profileViewModel = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);
        profileViewModel.storeLoginData(userId, authToken);

        profileViewModel.userProfile.observe(getViewLifecycleOwner(), userProfile -> {
//            Log.d("RegularizeFragment", "Observer triggered");
            if (userProfile != null) {
                this.userProfile = userProfile;
            } else {
                Log.d("RegularizeFragment", "User Profile is null");
            }
        });

        profileViewModel.fetchUserProfile();
        if (attendanceItem != null) {
            SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat timeFormatter = new SimpleDateFormat("hh:mm", Locale.getDefault());
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
                }

                if (punchOutTime != null) {
                    timePunchOut.setText(timeFormatter.format(punchOutTime));
                }

                etPunchInAddress.setText(attendanceItem.getPunchInAddress());
                etPunchOutAddress.setText(attendanceItem.getPunchOutAddress());

            } catch (ParseException e) {
                // Handle ParseException, e.g., log the error
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
        btnClear.setOnClickListener(v -> {
            timePunchIn.setText("");
            timePunchOut.setText("");
            etPunchInAddress.setText("");
            etPunchOutAddress.setText("");
            etRemarks.setText("");
        });

        timePunchIn.setOnClickListener(v -> {
            // Get current time
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);
            // Launch Time Picker Dialog
            TimePickerDialog timePickerDialog = new TimePickerDialog(requireContext(), (view12, hourOfDay, minute1) -> {
                // Set the selected time to the TextView
                String selectedTime = hourOfDay + ":" + minute1;
                timePunchIn.setText(selectedTime);
            }, hour, minute, false);
            timePickerDialog.show();
        });
        timePunchOut.setOnClickListener(v -> {
            // Get current time
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);
            // Launch Time Picker Dialog
            TimePickerDialog timePickerDialog = new TimePickerDialog(requireContext(), (view12, hourOfDay, minute1) -> {
                // Set the selected time to the TextView
                String selectedTime = hourOfDay + ":" + minute1;
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
//            Log.d("RegularizeFragment", "Attendance Item: " + attendanceItem);
            if (attendanceItem != null) {
//                Log.d("RegularizeFragment", "Attendance Item ID: " + attendanceItem.getId());
            } else {
                Log.d("RegularizeFragment", "Attendance Item is null");
            }
        }
    }

    private void clearForm() {
        atDate.setText("");
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
        String dateTimeStr = attendanceItem.getPunchDate();

// Parse the date and time string
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        LocalDate punchDate = LocalDate.parse(dateTimeStr, dateTimeFormatter);
// Format the date only
        DateTimeFormatter dateOnlyFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String punchDateOnly = punchDate.format(dateOnlyFormatter);
// Set the formatted date to punchDate
        requestBody.setPunchDate(punchDateOnly);
        requestBody.setPunchInAddress(punchInAddress);
        requestBody.setPunchInTime(punchIn);
        requestBody.setPunchOut(attendanceItem.getPunchOut());
        requestBody.setPunchOutAddress(punchOutAddress);
        requestBody.setPunchOutTime(punchOut);

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("H:mm");
        ZoneId inputTimeZone = ZoneId.of("Asia/Kolkata");
        // --- punchIn ---
        LocalTime localPunchInTime = LocalTime.parse(punchIn, timeFormatter);
        // Convert local time to UTC
        ZonedDateTime zonedPunchInTime = ZonedDateTime.of(punchDate, localPunchInTime, inputTimeZone);
        OffsetDateTime utcPunchInTime = zonedPunchInTime.withZoneSameInstant(ZoneOffset.UTC).toOffsetDateTime();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        String punchInUpdate = utcPunchInTime.format(formatter);
        requestBody.setPunchInUpdate(punchInUpdate);

        // --- punchOut ---
        LocalTime localPunchOutTime = LocalTime.parse(punchOut, timeFormatter);
        // Convert local time to UTC
        ZonedDateTime zonedPunchOutTime = ZonedDateTime.of(punchDate, localPunchOutTime, inputTimeZone);
        OffsetDateTime utcPunchOutTime = zonedPunchOutTime.withZoneSameInstant(ZoneOffset.UTC).toOffsetDateTime();

        String punchOutUpdate = utcPunchOutTime.format(formatter);
        requestBody.setPunchOutUpdate(punchOutUpdate);
        if (userProfile != null) {
            String reportingManagerId = userProfile.getData().getEmployee().getReportingManager().getId();
            String reportingManagerEmail = userProfile.getData().getEmployee().getReportingManager().getEmail();
            String reportingManagerFirstName = userProfile.getData().getEmployee().getReportingManager().getFirstname();
            String reportingManagerLastName = userProfile.getData().getEmployee().getReportingManager().getLastname();

            requestBody.setReportingManager(reportingManagerId);
            requestBody.setReportingManagerEmail(reportingManagerEmail);
            requestBody.setReportingManagerFirstName(reportingManagerFirstName);
            requestBody.setReportingManagerLastName(reportingManagerLastName);
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
//                        Log.d("AddAttendanceMessage", message);
                        showAlertDialog("Attendance Regularize Applied", message);
                    } else {
                        showAlertDialog("Error", "Regularization failed");
                    }
                } catch (IOException | JSONException e) {
                    showAlertDialog("Error", "An error occurred");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
                showAlertDialog("Error", "Regularization failed");
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
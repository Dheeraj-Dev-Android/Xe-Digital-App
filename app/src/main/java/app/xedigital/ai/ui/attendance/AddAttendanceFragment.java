package app.xedigital.ai.ui.attendance;

import static app.xedigital.ai.ui.regularize_attendance.RegularizeFragment.ARG_ATTENDANCE_ITEM;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
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
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;

import app.xedigital.ai.R;
import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.api.APIInterface;
import app.xedigital.ai.model.addAttendance.AddAttendanceRequest;
import app.xedigital.ai.model.attendance.EmployeePunchDataItem;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class AddAttendanceFragment extends Fragment {

    private APIInterface apiInterface;
    private TextInputEditText atDate;
    private TextInputEditText timePunchIn;
    private TextInputEditText timePunchOut;
    private TextInputEditText etPunchInAddress;
    private TextInputEditText etPunchOutAddress;
    private TextInputEditText etRemarks;
    private Button btSubmit;
    private Button btnClear;
    private String token;
    private Chip viewAppliedAddAttendance;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiInterface = APIClient.getInstance().AddAttendance();
        if (getArguments() != null) {
            EmployeePunchDataItem attendanceItem = (EmployeePunchDataItem) getArguments().getSerializable(ARG_ATTENDANCE_ITEM);
            if (attendanceItem != null) {
                Log.d("AttendanceItem", attendanceItem.toString());
            } else {
                Log.d("AttendanceItem", "Attendance item is null");
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_attendance, container, false);

        atDate = view.findViewById(R.id.atDate);
        timePunchIn = view.findViewById(R.id.timePunchIn);
        timePunchOut = view.findViewById(R.id.timePunchOut);
        etPunchInAddress = view.findViewById(R.id.etPunchInAddress);
        etPunchOutAddress = view.findViewById(R.id.etPunchOutAddress);
        etRemarks = view.findViewById(R.id.etRemarks);
        btSubmit = view.findViewById(R.id.btSubmit);
        btnClear = view.findViewById(R.id.btClear);
        viewAppliedAddAttendance = view.findViewById(R.id.btn_viewAppliedAddAttendance);


        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        token = sharedPreferences.getString("authToken", "");
        String userId = sharedPreferences.getString("userId", "");

        btSubmit.setOnClickListener(v -> {
            if (validateForm()) {
                String date = atDate.getText().toString();
                String punchIn = timePunchIn.getText().toString();
                String punchOut = timePunchOut.getText().toString();
                String punchInAddress = etPunchInAddress.getText().toString();
                String punchOutAddress = etPunchOutAddress.getText().toString();
                String remarks = etRemarks.getText().toString();

                addAttendance(userId, date, punchIn, punchOut, punchInAddress, punchOutAddress, remarks);
            }
        });

        btnClear.setOnClickListener(v -> {
            atDate.setText("");
            timePunchIn.setText("");
            timePunchOut.setText("");
            etPunchInAddress.setText("");
            etPunchOutAddress.setText("");
            etRemarks.setText("");
        });

        timePunchIn.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(requireContext(), (view1, hourOfDay, minute1) -> {
                String selectedTime = String.format(Locale.getDefault(), "%02d:%02d:00", hourOfDay, minute1);
                timePunchIn.setText(selectedTime);
            }, hour, minute, true);
            timePickerDialog.show();
        });

        timePunchOut.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);
            TimePickerDialog timePickerDialog = new TimePickerDialog(requireContext(), (view1, hourOfDay, minute1) -> {
                String selectedTime = String.format(Locale.getDefault(), "%02d:%02d:00", hourOfDay, minute1);
                timePunchOut.setText(selectedTime);
            }, hour, minute, true);
            timePickerDialog.show();
        });

        atDate.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), (view12, year1, monthOfYear, dayOfMonth) -> {
                String formattedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year1, monthOfYear + 1, dayOfMonth);
                atDate.setText(formattedDate);
            }, year, month, day);
            datePickerDialog.show();
        });
        viewAppliedAddAttendance.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_nav_add_attendance_to_nav_View_add_attendance_fragment);
            Toast.makeText(requireContext(), "Applied Attendance Add", Toast.LENGTH_SHORT).show();
        });

    }

    private void addAttendance(String userId, String date, String punchIn, String punchOut, String punchInAddress, String punchOutAddress, String remarks) {

        String formattedPunchIn = date + "T" + punchIn + ".000Z";
        String formattedPunchOut = date + "T" + punchOut + ".000Z";

        AddAttendanceRequest requestBody = new AddAttendanceRequest();
        requestBody.setEmployee(userId);
        requestBody.setPunchDate(date);
        requestBody.setPunchIn(formattedPunchIn);
        requestBody.setPunchOut(formattedPunchOut);
        requestBody.setPunchInAddress(punchInAddress);
        requestBody.setPunchOutAddress(punchOutAddress);
        requestBody.setRemark(remarks);

        Call<ResponseBody> call = apiInterface.AddAttendanceApi("jwt " + token, requestBody);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        String responseBody = response.body().string();
                        Log.d("AddAttendanceResponse", responseBody);

                        JSONObject jsonObject = new JSONObject(responseBody);
                        String message = jsonObject.getString("message");
                        Log.d("AddAttendanceMessage", message);
//                        clearForm();
                        showAlertDialog("Attendance Regularize Applied", message);
                    } else {
                        showAlertDialog("Attendance Regularize", response.message());
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                    showAlertDialog("Error", "Failed to process response");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
                throwable.printStackTrace();
                showAlertDialog("Failed to process response", throwable.getMessage());
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
        if (atDate.getText().toString().isEmpty()) {
            atDate.setError("Date is required");
            isValid = false;
        }
        if (timePunchIn.getText().toString().isEmpty()) {
            timePunchIn.setError("Punch-in time is required");
            isValid = false;
        }
        if (timePunchOut.getText().toString().isEmpty()) {
            timePunchOut.setError("Punch-out time is required");
            isValid = false;
        }
        if (etPunchInAddress.getText().toString().isEmpty()) {
            etPunchInAddress.setError("Punch-in address is required");
            isValid = false;
        }
        if (etPunchOutAddress.getText().toString().isEmpty()) {
            etPunchOutAddress.setError("Punch-out address is required");
            isValid = false;
        }
        if (etRemarks.getText().toString().isEmpty()) {
            etRemarks.setError("Remarks is required");
            isValid = false;
        }
        return isValid;

    }

}
package app.xedigital.ai.ui.timesheet;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import app.xedigital.ai.R;
import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.databinding.FragmentDcrFormBinding;
import app.xedigital.ai.model.branch.UserBranchResponse;
import app.xedigital.ai.model.dcrSubmit.DcrFormRequest;
import app.xedigital.ai.model.user.UserModelResponse;
import app.xedigital.ai.ui.profile.ProfileViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TimesheetFormFragment extends Fragment {
    private static final String TAG = "TimeSheet";
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    public String employeeName;
    public String employeeEmail;
    public String employeeLastName;
    public String notificationMail;
    public String reportingManagerEmail;
    public String reportingManagerFirstName;
    public String reportingManagerLastName;
    private TextInputEditText dcrDate;
    private TextInputEditText inTime;
    private TextInputEditText outTime;
    private TextInputEditText highlightOfTheDay;
    private TextInputEditText outcomeOfTheDay;
    private TextInputEditText nextDayPlan;
    private TextInputEditText feelingOfTheDay;
    private MaterialButton btnDcrSubmit;
    private MaterialButton btnDcrClear;
    private FragmentDcrFormBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.fragment_dcr_form, container, false);
        binding = FragmentDcrFormBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        dcrDate = view.findViewById(R.id.dcrDate);
        inTime = view.findViewById(R.id.inTime);
        outTime = view.findViewById(R.id.outTime);
        highlightOfTheDay = view.findViewById(R.id.highlightOfTheDay);
        outcomeOfTheDay = view.findViewById(R.id.outcomeOfTheDay);
        nextDayPlan = view.findViewById(R.id.nextDayPlan);
        feelingOfTheDay = view.findViewById(R.id.feelingOfTheDay);
        btnDcrSubmit = view.findViewById(R.id.btnDcrSubmit);
        btnDcrClear = view.findViewById(R.id.btnDcrClear);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ProfileViewModel profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        getContext();
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String authToken = sharedPreferences.getString("authToken", "");
        String userId = sharedPreferences.getString("userId", "");
        callUserApi(userId, authToken);
        profileViewModel.storeLoginData(userId, authToken);
        profileViewModel.fetchUserProfile();

        profileViewModel.userProfile.observe(getViewLifecycleOwner(), userprofileResponse -> {
            String responseString = gson.toJson(userprofileResponse);
            Log.d(TAG, "Response: " + responseString);
            employeeName = userprofileResponse.getData().getEmployee().getFirstname();
            Log.d(TAG, "Employee Name: " + employeeName);
            employeeEmail = userprofileResponse.getData().getEmployee().getEmail();
            Log.d(TAG, "Employee Email: " + employeeEmail);
            employeeLastName = userprofileResponse.getData().getEmployee().getLastname();
            Log.d(TAG, "Employee Last Name: " + employeeLastName);
            reportingManagerEmail = userprofileResponse.getData().getEmployee().getReportingManager().getEmail();
            Log.d(TAG, "Reporting Manager Email: " + reportingManagerEmail);
            reportingManagerFirstName = userprofileResponse.getData().getEmployee().getReportingManager().getFirstname();
            Log.d(TAG, "Reporting Manager First Name: " + reportingManagerFirstName);
            reportingManagerLastName = userprofileResponse.getData().getEmployee().getReportingManager().getLastname();
            Log.d(TAG, "Reporting Manager Last Name: " + reportingManagerLastName);


        });

        btnDcrSubmit.setOnClickListener(v -> {
            if (validateForm()) {
                String date = Objects.requireNonNull(dcrDate.getText()).toString();
                String inTimeValue = Objects.requireNonNull(inTime.getText()).toString();
                String outTimeValue = Objects.requireNonNull(outTime.getText()).toString();
                String highlightOfTheDayValue = Objects.requireNonNull(highlightOfTheDay.getText()).toString();
                String outcomeOfTheDayValue = Objects.requireNonNull(outcomeOfTheDay.getText()).toString();
                String nextDayPlanValue = Objects.requireNonNull(nextDayPlan.getText()).toString();
                String feelingOfTheDayValue = Objects.requireNonNull(feelingOfTheDay.getText()).toString();
                dcrFormSubmit(userId, authToken, date, inTimeValue, outTimeValue, highlightOfTheDayValue, outcomeOfTheDayValue, nextDayPlanValue, feelingOfTheDayValue);

                Toast.makeText(getContext(), "Timesheet submitted", Toast.LENGTH_SHORT).show();
            }
        });


        binding.btnDcrClear.setOnClickListener(v -> clearForm());

        binding.dcrDate.setOnClickListener(v -> {
            dcrDate.requestFocus();
            Calendar calendar = Calendar.getInstance();
            long today = calendar.getTimeInMillis();

            CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();
            constraintsBuilder.setValidator(DateValidatorPointForward.now());

            MaterialDatePicker.Builder<Long> materialDatePickerBuilder = MaterialDatePicker.Builder.datePicker();
            materialDatePickerBuilder.setTitleText("Select Date");
            materialDatePickerBuilder.setSelection(today);
            materialDatePickerBuilder.setCalendarConstraints(constraintsBuilder.build());

            MaterialDatePicker<Long> materialDatePicker = materialDatePickerBuilder.build();
            materialDatePicker.show(getParentFragmentManager(), "DATE_PICKER");

            materialDatePicker.addOnPositiveButtonClickListener(selection -> {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                dcrDate.setText(simpleDateFormat.format(selection));
            });
        });


        binding.inTime.setOnClickListener(v -> {
            inTime.requestFocus();
            MaterialTimePicker materialTimePicker = new MaterialTimePicker.Builder().setTitleText("Select In Time").setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK).build();

            materialTimePicker.show(getParentFragmentManager(), "inTime");

            materialTimePicker.addOnPositiveButtonClickListener(dialog -> {
                int hour = materialTimePicker.getHour();
                int minute = materialTimePicker.getMinute();
                String selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
                inTime.setText(selectedTime);
            });
        });

        binding.outTime.setOnClickListener(v -> {
            outTime.requestFocus();
            MaterialTimePicker materialTimePicker = new MaterialTimePicker.Builder().setTitleText("Select Out Time").setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK).build();
            materialTimePicker.show(getParentFragmentManager(), "outTime");

            materialTimePicker.addOnPositiveButtonClickListener(dialog -> {
                int hour = materialTimePicker.getHour();
                int minute = materialTimePicker.getMinute();
                String selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
                outTime.setText(selectedTime);
            });
        });
    }

    private void dcrFormSubmit(String userId, String authToken, String date, String inTimeValue, String outTimeValue, String highlightOfTheDayValue, String outcomeOfTheDayValue, String nextDayPlanValue, String feelingOfTheDayValue) {

        String formattedInTime = date + "T" + inTimeValue + ":00.000Z";
        String formattedOutTime = date + "T" + outTimeValue + ":00.000Z";

        DcrFormRequest requestBody = new DcrFormRequest();
        requestBody.setDcrDate(date);
        requestBody.setEmployeeEmail(employeeEmail);
        requestBody.setEmployeeLastName(employeeLastName);
        requestBody.setEmployee(userId);
        requestBody.setEmployeeName(employeeName);
        requestBody.setHrEmail(notificationMail);
        requestBody.setInTime(formattedInTime);
        requestBody.setOutTime(formattedOutTime);
        requestBody.setTodayReport(highlightOfTheDayValue);
        requestBody.setOutcome(outcomeOfTheDayValue);
        requestBody.setTommarowPlan(nextDayPlanValue);
        requestBody.setTodayFeeling(feelingOfTheDayValue);
        requestBody.setReportingManagerEmail(reportingManagerEmail);
        requestBody.setReportingManagerFirstName(reportingManagerFirstName);
        requestBody.setReportingManagerLastName(reportingManagerLastName);

        Call<ResponseBody> call = APIClient.getInstance().DcrFormSubmit().DcrSubmit("jwt " + authToken, requestBody);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        String responseBody = response.body().string();
                        Log.d(TAG, "Response: " + responseBody);

                        JSONObject jsonObject = new JSONObject(responseBody);
                        String message = jsonObject.getString("message");
                        Log.d(TAG, "Message: " + message);
                        showAlertDialog("Timesheet Submitted", message);
                    } else {
                        showAlertDialog("Error submitting timesheet", response.message());
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                    showAlertDialog("Error", "Error submitting timesheet");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
                Log.d(TAG, "onFailure: " + throwable.getMessage());
                showAlertDialog("Error", throwable.getMessage());
            }

            private void showAlertDialog(String title, String message) {
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setTitle(title).setMessage(message).setPositiveButton("OK", (dialog, which) -> {
                    clearForm();
                    dialog.dismiss();

                    NavController navController = Navigation.findNavController(requireView());
                    navController.navigate(R.id.action_nav_dcr_form_to_nav_dcr);
                }).show();
            }
        });
    }

    private void clearForm() {
        dcrDate.setText("");
        inTime.setText("");
        outTime.setText("");
        highlightOfTheDay.setText("");
        outcomeOfTheDay.setText("");
        nextDayPlan.setText("");
        feelingOfTheDay.setText("");

        dcrDate.setError(null);
        inTime.setError(null);
        outTime.setError(null);
        highlightOfTheDay.setError(null);
        outcomeOfTheDay.setError(null);
        nextDayPlan.setError(null);
        feelingOfTheDay.setError(null);
    }

    private boolean validateForm() {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        dateFormat.setLenient(false);
        try {
            dateFormat.parse(Objects.requireNonNull(dcrDate.getText()).toString());
        } catch (ParseException e) {
            dcrDate.setError("Invalid date format. Use dd-MM-yyyy");
            Toast.makeText(getContext(), "Invalid date format. Use yyyy-MM-dd", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(dcrDate.getText())) {
            dcrDate.setError("Please enter a date");
            Toast.makeText(getContext(), "Please enter a date", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(inTime.getText())) {
            inTime.setError("Please enter In Time");
            Toast.makeText(getContext(), "Please enter In Time", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(outTime.getText())) {
            outTime.setError("Please enter Out Time");
            Toast.makeText(getContext(), "Please enter Out Time", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(highlightOfTheDay.getText())) {
            highlightOfTheDay.setError("Please enter Highlight of the Day");
            Toast.makeText(getContext(), "Please enter Highlight of the Day", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(outcomeOfTheDay.getText())) {
            outcomeOfTheDay.setError("Please enter Outcome of the Day");
            Toast.makeText(getContext(), "Please enter Outcome of the Day", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(nextDayPlan.getText())) {
            nextDayPlan.setError("Please enter Next Day Plan");
            Toast.makeText(getContext(), "Please enter Next Day Plan", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(feelingOfTheDay.getText())) {
            feelingOfTheDay.setError("Please enter Feeling of the Day");
            Toast.makeText(getContext(), "Please enter Feeling of the Day", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public void callUserApi(String userId, String authToken) {
        String authHeaderValue = "jwt " + authToken;

        Call<UserModelResponse> call = APIClient.getInstance().getUser().getUserData(userId, authHeaderValue);
        call.enqueue(new Callback<UserModelResponse>() {
            @Override
            public void onResponse(@NonNull Call<UserModelResponse> call, @NonNull Response<UserModelResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserModelResponse userResponse = response.body();
                    String responseJson = gson.toJson(userResponse.getData());
                    Log.d(TAG, "Response: " + responseJson);
                    String branchId = userResponse.getData().getBranch().getId();
                    callBranchApi(branchId, authToken);
                    Log.d(TAG, "Branch ID: " + branchId);
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserModelResponse> call, @NonNull Throwable throwable) {
                Log.d(TAG, "onFailure: " + throwable.getMessage());
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
                        UserBranchResponse responseBranch = response.body();
                        String responseString = gson.toJson(responseBranch);
                        Log.d("BranchData", "Response: " + responseString);
                        notificationMail = responseBranch.getData().getBranch().getNotificationEmail();
                        Log.d("BranchData", "Notification Email: " + notificationMail);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.d("BranchData", "Error parsing response: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserBranchResponse> call, @NonNull Throwable throwable) {
                Log.d("BranchData", "onFailure: " + throwable.getMessage());
            }
        });
    }

}
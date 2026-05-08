package app.xedigital.ai.ui.timesheet;

import static app.xedigital.ai.utills.DateTimeUtils.formatTime;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

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

import app.xedigital.ai.R;
import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.api.APIInterface;
import app.xedigital.ai.databinding.FragmentDcrFormBinding;
import app.xedigital.ai.model.attendance.EmployeeAttendanceResponse;
import app.xedigital.ai.model.attendance.EmployeePunchDataItem;
import app.xedigital.ai.model.branch.UserBranchResponse;
import app.xedigital.ai.model.dcrSubmit.DcrFormRequest;
import app.xedigital.ai.model.user.UserModelResponse;
import app.xedigital.ai.ui.profile.ProfileViewModel;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TimesheetFormFragment extends Fragment {
    private static final String TAG = "TimeSheet";
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final OnBackPressedCallback backPressedCallback = new OnBackPressedCallback(false) {
        @Override
        public void handleOnBackPressed() {
            // Do nothing when back is pressed while submitting
            Toast.makeText(getContext(), "Please wait while we submit your timesheet", Toast.LENGTH_SHORT).show();
        }
    };
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
    private boolean isSubmitting = false;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
        // Register the back press handler
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), backPressedCallback);
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String authToken = sharedPreferences.getString("authToken", "");
        String userId = sharedPreferences.getString("userId", "");
        callUserApi(userId, authToken);
        profileViewModel.storeLoginData(userId, authToken);
        profileViewModel.fetchUserProfile();

        profileViewModel.userProfile.observe(getViewLifecycleOwner(), userprofileResponse -> {
            String responseString = gson.toJson(userprofileResponse);
            employeeName = userprofileResponse.getData().getEmployee().getFirstname();
            employeeEmail = userprofileResponse.getData().getEmployee().getEmail();
            employeeLastName = userprofileResponse.getData().getEmployee().getLastname();
            reportingManagerEmail = userprofileResponse.getData().getEmployee().getReportingManager().getEmail();
            reportingManagerFirstName = userprofileResponse.getData().getEmployee().getReportingManager().getFirstname();
            reportingManagerLastName = userprofileResponse.getData().getEmployee().getReportingManager().getLastname();
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

//            materialDatePicker.addOnPositiveButtonClickListener(selection -> {
//                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
//                dcrDate.setText(simpleDateFormat.format(selection));
//            });
            materialDatePicker.addOnPositiveButtonClickListener(selection -> {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                String selectedDateStr = simpleDateFormat.format(selection);
                dcrDate.setText(selectedDateStr); //[cite: 1]

                // Trigger the auto-fill
                fetchEmployeeAttendanceForDate(selectedDateStr);
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
            materialTimePicker.show(getParentFragmentManager(), "OutTime");
            materialTimePicker.addOnPositiveButtonClickListener(dialog -> {
                int hour = materialTimePicker.getHour();
                int minute = materialTimePicker.getMinute();
                String selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
                outTime.setText(selectedTime);
            });
        });
    }

    public void fetchEmployeeAttendanceForDate(String date) {
        Context context = getContext(); // Use getContext() which returns null if detached
        if (context == null) return;
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String authToken = sharedPreferences.getString("authToken", "");
        String authHeaderValue = "jwt " + authToken;

        APIInterface apiInterface = APIClient.getInstance().getAttendance();
        apiInterface.getAttendance(authHeaderValue, date, date, "", "", "", "", "", "").enqueue(new Callback<EmployeeAttendanceResponse>() {
            @Override
            public void onResponse(@NonNull Call<EmployeeAttendanceResponse> call, @NonNull Response<EmployeeAttendanceResponse> response) {
                if (!isAdded() || binding == null) return;
                if (response.isSuccessful() && response.body() != null) {
                    EmployeeAttendanceResponse responseBody = response.body();

                    // Check if data and the list are not null/empty
                    if (responseBody.getData() != null &&
                            responseBody.getData().getEmployeePunchData() != null &&
                            !responseBody.getData().getEmployeePunchData().isEmpty()) {

                        // Get the first entry for the selected date
                        EmployeePunchDataItem punchData = responseBody.getData().getEmployeePunchData().get(0);

                        String pIn = punchData.getPunchIn();
                        String pOut = punchData.getPunchOut();

                        // Null & Empty Check for Punch In
                        if (pIn != null && !pIn.trim().isEmpty() && !pIn.equalsIgnoreCase("null")) {
                            inTime.setText(formatTime(pIn));
                        } else {
                            Log.d(TAG, "Punch In is null or empty for this date");
                        }

                        // Null & Empty Check for Punch Out
                        if (pOut != null && !pOut.trim().isEmpty() && !pOut.equalsIgnoreCase("null")) {
                            outTime.setText(formatTime(pOut));
                        } else {
                            Log.d(TAG, "Punch Out is null or empty for this date");
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<EmployeeAttendanceResponse> call, @NonNull Throwable throwable) {
                Log.e(TAG, "Error fetching attendance: " + throwable.getMessage());
            }
        });
    }

    private void dcrFormSubmit(String userId, String authToken, String date, String inTimeValue, String outTimeValue, String highlightOfTheDayValue, String outcomeOfTheDayValue, String nextDayPlanValue, String feelingOfTheDayValue) {
        setLoading(true);
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
                setLoading(false); // Stop the spinner
                if (!isAdded()) return;
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        String responseBody = response.body().string();
                        JSONObject jsonObject = new JSONObject(responseBody);
                        String message = jsonObject.getString("message");
                        showAlertDialog(true, "Submitted!", "Your DCR form has been successfully submitted.");
                    } else {

                        showAlertDialog(false, "Submission failed", "Something went wrong. Please try again later.");
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();

                    showAlertDialog(false, "Submission failed", "Something went wrong. Please try again later.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
                setLoading(false); // Stop the spinner even on failure
                if (!isAdded()) return;

                showAlertDialog(false, "Submission failed", "Something went wrong. Please try again later.");
            }
        });
    }

    private void showAlertDialog(boolean isSuccess, String title, String message) {
        if (!isAdded() || getContext() == null) {
            return;
        }

        // Inflate custom layout
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_custom_alert, null);

        ImageView icon = dialogView.findViewById(R.id.dialog_icon);
        TextView tvTitle = dialogView.findViewById(R.id.dialog_title);
        TextView tvMessage = dialogView.findViewById(R.id.dialog_message);

        if (isSuccess) {
            icon.setImageResource(R.drawable.ic_check_circle);   // green check icon
            icon.setColorFilter(ContextCompat.getColor(getContext(), R.color.approved_color));
        } else {
            icon.setImageResource(R.drawable.ic_cancel_circle);  // red cross icon
            icon.setColorFilter(ContextCompat.getColor(getContext(), R.color.rejected_color));
        }

        tvTitle.setText(title);
        tvMessage.setText(message);

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .setCancelable(false)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        dialogView.findViewById(R.id.btn_ok).setOnClickListener(v -> {
            dialog.dismiss();
            if (isSuccess) {
                View view = getView();
                if (view != null) {
                    clearForm();
                    try {
                        NavController navController = Navigation.findNavController(view);
                        navController.navigate(R.id.action_nav_dcr_form_to_nav_dcr);
                    } catch (IllegalArgumentException e) {
                        Log.e(TAG, "Navigation error: " + e.getMessage());
                    }
                }
            }
        });

        dialog.show();
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
            dcrDate.setError("Invalid date format. Use yyyy-MM-dd");
            Toast.makeText(getContext(), "Invalid date format. Use yyyy-MM-dd", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(dcrDate.getText())) {
            dcrDate.setError("Please Enter a date");
            Toast.makeText(getContext(), "Please Enter a date", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(inTime.getText())) {
            inTime.setError("Please Enter In Time");
            Toast.makeText(getContext(), "Please Enter In Time", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(outTime.getText())) {
            outTime.setError("Please Enter Out Time");
            Toast.makeText(getContext(), "Please Enter Out Time", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(highlightOfTheDay.getText())) {
            highlightOfTheDay.setError("Please Enter Highlight of the Day");
            Toast.makeText(getContext(), "Please Enter Highlight of the Day", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(outcomeOfTheDay.getText())) {
            outcomeOfTheDay.setError("Please Enter Outcome of the Day");
            Toast.makeText(getContext(), "Please Enter Outcome of the Day", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(nextDayPlan.getText())) {
            nextDayPlan.setError("Please Enter Next Day Plan");
            Toast.makeText(getContext(), "Please Enter Next Day Plan", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(feelingOfTheDay.getText())) {
            feelingOfTheDay.setError("Please Enter Feeling of the Day");
            Toast.makeText(getContext(), "Please Enter Feeling of the Day", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void setLoading(boolean isLoading) {
        if (binding == null) return;

        this.isSubmitting = isLoading;

        // This line enables/disables the back button interception
        backPressedCallback.setEnabled(isLoading);

        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnDcrSubmit.setEnabled(!isLoading);
        btnDcrClear.setEnabled(!isLoading);

        // Also disable form fields so user can't type while loading
        dcrDate.setEnabled(!isLoading);
        inTime.setEnabled(!isLoading);
        outTime.setEnabled(!isLoading);
        highlightOfTheDay.setEnabled(!isLoading);
        outcomeOfTheDay.setEnabled(!isLoading);
        nextDayPlan.setEnabled(!isLoading);
        feelingOfTheDay.setEnabled(!isLoading);
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
                    String branchId = userResponse.getData().getBranch().getId();
                    callBranchApi(branchId, authToken);
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserModelResponse> call, @NonNull Throwable throwable) {
                throwable.printStackTrace();
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
                        notificationMail = responseBranch.getData().getBranch().getNotificationEmail();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserBranchResponse> call, @NonNull Throwable throwable) {
                throwable.printStackTrace();
            }
        });
    }

    @Override
    public void onDestroyView() {
        // 1. Disable the back button callback to prevent it from holding onto the fragment/activity reference
        if (backPressedCallback != null) {
            backPressedCallback.setEnabled(false);
            backPressedCallback.remove();
        }

        // 2. Clear the binding reference to avoid memory leaks
        binding = null;

        super.onDestroyView();
    }

}
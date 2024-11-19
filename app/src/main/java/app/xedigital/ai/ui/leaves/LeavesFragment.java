package app.xedigital.ai.ui.leaves;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
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

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import app.xedigital.ai.R;
import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.databinding.FragmentLeavesBinding;
import app.xedigital.ai.model.branch.UserBranchResponse;
import app.xedigital.ai.model.employeeLeaveType.EmployeeLeaveTypeResponse;
import app.xedigital.ai.model.leaveType.LeavetypesItem;
import app.xedigital.ai.model.profile.Employee;
import app.xedigital.ai.model.user.UserModelResponse;
import app.xedigital.ai.ui.profile.ProfileViewModel;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class LeavesFragment extends Fragment {

    private static final String TAG = "LeavesFragment";
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private FragmentLeavesBinding binding;
    private EditText etFromDate, etToDate;
    private SimpleDateFormat dateFormat;
    private double totalDays;
    private List<LeavetypesItem> leaveTypesList = new ArrayList<>();


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LeavesViewModel leavesViewModel = new ViewModelProvider(this).get(LeavesViewModel.class);
        binding = FragmentLeavesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        etFromDate = binding.etFromDate;
        etToDate = binding.etToDate;
        binding.balanceLeaveTextView.setText("");
        Calendar calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

        etFromDate.setOnClickListener(view -> showDatePicker(etFromDate));
        etToDate.setOnClickListener(view -> showDatePicker(etToDate));

        AutoCompleteTextView leaveTypeSpinner = binding.spinnerLeaveType;
        AutoCompleteTextView leaveCategorySpinnerFrom = binding.spinnerLeaveCategoryFrom;
        AutoCompleteTextView leaveCategorySpinnerTo = binding.spinnerLeaveCategoryTo;
        AutoCompleteTextView leavingStationSpinner = binding.spinnerLeavingStation;

        TextInputEditText leaveStationAddress = binding.etLeaveStationAddress;
        TextInputLayout leaveStationAddressLayout = binding.tilLeaveStationAddress;

        leaveStationAddressLayout.setVisibility(View.GONE);

        leavingStationSpinner.setOnItemClickListener((adapterView, view, position, id) -> {
            String selectedOption = (String) adapterView.getItemAtPosition(position);
            if ("Yes".equalsIgnoreCase(selectedOption)) {
                leaveStationAddressLayout.setVisibility(View.VISIBLE);
            } else {
                leaveStationAddressLayout.setVisibility(View.GONE);
                leaveStationAddress.setText("");
            }
        });


        ArrayAdapter<String> leaveCategoriesAdapter = new ArrayAdapter<>(requireContext(), R.layout.dropdown_menu_popup_item, getResources().getStringArray(R.array.leave_categories));
        leaveCategorySpinnerFrom.setAdapter(leaveCategoriesAdapter);
        leaveCategorySpinnerTo.setAdapter(leaveCategoriesAdapter);

        ArrayAdapter<String> leavingStationsAdapter = new ArrayAdapter<>(requireContext(), R.layout.dropdown_menu_popup_item, getResources().getStringArray(R.array.leaving_stations));
        leavingStationSpinner.setAdapter(leavingStationsAdapter);

        leavesViewModel = new ViewModelProvider(this).get(LeavesViewModel.class);

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String authToken = sharedPreferences.getString("authToken", "");
        leavesViewModel.setUserId(authToken);
        leavesViewModel.fetchLeavesType();

        leavesViewModel.leavesTypeData.observe(getViewLifecycleOwner(), leaveTypeResponse -> {
            if (leaveTypeResponse != null && leaveTypeResponse.getData() != null) {
                leaveTypesList = leaveTypeResponse.getData().getLeavetypes();
                List<String> leaveTypeNames = new ArrayList<>();

                for (LeavetypesItem leaveType : leaveTypesList) {
                    leaveTypeNames.add(leaveType.getLeavetypeName());
                }

                ArrayAdapter<String> leaveTypeAdapter = new ArrayAdapter<>(requireContext(), R.layout.dropdown_menu_popup_item, leaveTypeNames);
                binding.spinnerLeaveType.setAdapter(leaveTypeAdapter);

                // Log the leave type names for debugging
//                Log.d(TAG, "Leave Type Names: " + gson.toJson(leaveTypeResponse));
            } else {
                Log.e("LeavesFragment", "Error fetching leaves type data");
            }
        });

        binding.btnClear.setOnClickListener(view -> {
            etFromDate.setText("");
            etToDate.setText("");
            binding.spinnerLeaveCategoryFrom.setText("");
            binding.spinnerLeaveCategoryTo.setText("");
            binding.spinnerLeavingStation.setText("");
            leaveStationAddress.setText("");
            binding.spinnerLeaveType.setText("");
            binding.etContactNumber.setText("");
            binding.etReason.setText("");
            leaveStationAddressLayout.setVisibility(View.GONE);
        });
        leaveCategorySpinnerFrom.setOnItemClickListener((adapterView, view, position, id) -> {
            Log.d(TAG, "leaveCategoryFrom selected: " + adapterView.getItemAtPosition(position));
            calculateTotalDaysAndCheckLeaveLimit();
        });

        leaveCategorySpinnerTo.setOnItemClickListener((adapterView, view, position, id) -> {
            Log.d(TAG, "leaveCategoryTo selected: " + adapterView.getItemAtPosition(position));
            calculateTotalDaysAndCheckLeaveLimit();
        });

        leaveTypeSpinner.setOnClickListener(view -> {
            if (etToDate.getText().toString().isEmpty()) {
                new AlertDialog.Builder(requireContext()).setTitle("Select Dates").setMessage("Please select dates first.").setPositiveButton("OK", null).show();
            }
        });
        binding.spinnerLeaveType.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                LeavetypesItem selectedLeaveType = leaveTypesList.get(position);
                String selectedLeaveTypeId = selectedLeaveType.getId();
                String selectedLeaveTypeName = selectedLeaveType.getLeavetypeName();
                Log.d(TAG, "Selected Leave Type ID: " + selectedLeaveTypeId);

                SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                String authTokenN = sharedPreferences.getString("authToken", "");
                String employeeId = sharedPreferences.getString("userId", "");
                String authToken = "jwt " + authTokenN;
                fetchEmployeeLeave(selectedLeaveTypeId, employeeId, authToken, selectedLeaveTypeName);
                fetchLeaveTypeDetails(selectedLeaveTypeId, authToken);
                fetchUnapprovedLeaves(selectedLeaveTypeId, employeeId, authToken);

            }

            private void fetchUnapprovedLeaves(String selectedLeaveTypeId, String employeeId, String authToken) {
                Call<ResponseBody> UnapprovedLeaves = APIClient.getInstance().getUnapprovedLeaves().getUnapprovedLeaves(authToken, selectedLeaveTypeId, employeeId);
                UnapprovedLeaves.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            try {
                                String responseBody = response.body().string();
//                                Log.d(TAG, "Employee Unapproved Leave: " + responseBody);
                            } catch (IOException e) {
                                e.printStackTrace();
                                Log.e(TAG, "Error reading Employee Leave response: " + e.getMessage());
                            }
                        } else {
                            Log.e(TAG, "Error fetching Unapproved Leaves: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
                        Log.e(TAG, "Error fetching Unapproved Leaves: " + throwable.getMessage());

                    }
                });


            }

            private void fetchLeaveTypeDetails(String selectedLeaveTypeId, String authToken) {
                Call<ResponseBody> leaveTypeDetails = APIClient.getInstance().getLeaveTypeDetails().getLeaveTypeDetails(authToken, selectedLeaveTypeId);
                leaveTypeDetails.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            try {
                                String responseBody = response.body().string();
//                                Log.e(TAG, "Leave Type Details: " + responseBody);
                            } catch (IOException e) {
                                Log.e(TAG, "Error reading Leave Type Details response: " + e.getMessage());
                            }
                        } else {
                            Log.e(TAG, "Error fetching Leave Type Details: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
                        Log.e(TAG, "Error fetching Leave Type Details: " + throwable.getMessage());
                    }
                });
            }

            private void fetchEmployeeLeave(String selectedLeaveTypeId, String employeeId, String authToken, String selectedLeaveTypeName) {
                Call<EmployeeLeaveTypeResponse> employeeLeave = APIClient.getInstance().getEmployeeLeave().getEmployeeLeave(authToken, selectedLeaveTypeId, employeeId);
                employeeLeave.enqueue(new Callback<EmployeeLeaveTypeResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<EmployeeLeaveTypeResponse> call, @NonNull Response<EmployeeLeaveTypeResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            EmployeeLeaveTypeResponse employeeLeaveResponse = response.body();
                            Log.e(TAG, "Employee Leave: " + employeeLeaveResponse);

                            double balanceLeave = employeeLeaveResponse.getData().getCreditLeave() - employeeLeaveResponse.getData().getUsedLeave();
                            Log.e(TAG, "Balance Leave: " + balanceLeave);
                            Log.e(TAG, "Selected Leave Type: " + selectedLeaveTypeName);
                            // --- Update UI on the main thread ---
                            requireActivity().runOnUiThread(() -> {
                                if (selectedLeaveTypeName.equals("Loss of Pay (LOP) / Leave Without Pay (LWP)")) {
                                    binding.balanceLeaveTextView.setText("");
                                } else {
                                    binding.balanceLeaveTextView.setText("Balance Leave: " + balanceLeave);

                                    if (balanceLeave == 0.0) {
                                        binding.balanceLeaveTextView.setText("Apply with Loss of Pay (LOP) / Leave Without Pay (LWP) " + " Balance Leave: " + balanceLeave);
                                        Toast.makeText(requireContext(), "Loss of Pay (LOP) / Leave Without Pay (LWP)", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        } else {
                            Log.e(TAG, "Error fetching Employee Leave: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<EmployeeLeaveTypeResponse> call, @NonNull Throwable throwable) {
                        Log.e(TAG, "Error fetching Employee Leave: " + throwable.getMessage());
                    }
                });
            }

        });
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ProfileViewModel profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        getContext();
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String authToken = sharedPreferences.getString("authToken", "");
        String userId = sharedPreferences.getString("userId", "");
        profileViewModel.storeLoginData(userId, authToken);
        profileViewModel.fetchUserProfile();
        callUserApi(userId, authToken);

        profileViewModel.userProfile.observe(getViewLifecycleOwner(), userprofileResponse -> {
            if (userprofileResponse != null && userprofileResponse.getData() != null && userprofileResponse.getData().getEmployee() != null) {
                Employee employee = userprofileResponse.getData().getEmployee();
                String empId = employee.getId();
                Log.e(TAG, "userID: " + userId);
                Log.e(TAG, "employee Id: " + empId);
            }
        });


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
//                    Log.d(TAG, "Response: " + responseJson);
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
//                        Log.d("BranchData", "Response: " + responseString);
                        String notificationMail = responseBranch.getData().getBranch().getNotificationEmail();
                        Log.d("BranchData", "Notification Email: " + notificationMail);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserBranchResponse> call, @NonNull Throwable throwable) {
                Log.d("BranchData", "onFailure: " + throwable.getMessage());
            }
        });
    }


    private long calculateTotalDays(String startDate, String endDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        LocalDate startDateObj = LocalDate.parse(startDate, formatter);
        LocalDate endDateObj = LocalDate.parse(endDate, formatter);
        return ChronoUnit.DAYS.between(startDateObj, endDateObj) + 1;
    }

//    private void calculateTotalDaysAndCheckLeaveLimit() {
//        String fromDate = binding.etFromDate.getText().toString();
//        String toDate = binding.etToDate.getText().toString();
//        String leaveCategoryFrom = binding.spinnerLeaveCategoryFrom.getText().toString();
//        String leaveCategoryTo = binding.spinnerLeaveCategoryTo.getText().toString();
//
//        Log.w(TAG, "fromDate: " + fromDate + ", toDate: " + toDate + ", leaveCategoryFrom: " + leaveCategoryFrom + ", leaveCategoryTo: " + leaveCategoryTo);
//
//        if (!fromDate.isEmpty() && !toDate.isEmpty() && !leaveCategoryFrom.isEmpty() && !leaveCategoryTo.isEmpty()) {
//            long totalDaysInDateRange = calculateTotalDays(fromDate, toDate);
//
//            if (leaveCategoryFrom.equals("First Half Day") && leaveCategoryTo.equals("First Half Day")) {
//                totalDays = (totalDaysInDateRange > 1) ? totalDaysInDateRange - 0.5 : 0.5;
//            } else if (leaveCategoryFrom.equals("Second Half Day") && leaveCategoryTo.equals("Second Half Day")) {
//                totalDays = (totalDaysInDateRange > 1) ? totalDaysInDateRange - 0.5 : 0.5;
//            } else if (leaveCategoryFrom.equals("Full Day") && leaveCategoryTo.equals("Full Day")) {
//                totalDays = totalDaysInDateRange;
//            } else if ((leaveCategoryFrom.equals("Full Day") && leaveCategoryTo.equals("First Half Day")) || (leaveCategoryFrom.equals("Full Day") && leaveCategoryTo.equals("Second Half Day")) || (leaveCategoryFrom.equals("First Half Day") && leaveCategoryTo.equals("Full Day")) || (leaveCategoryFrom.equals("Second Half Day") && leaveCategoryTo.equals("Full Day"))) {
//                totalDays = totalDaysInDateRange - 0.5;
//            } else if (leaveCategoryFrom.equals("First Half Day") && leaveCategoryTo.equals("Second Half Day")) {
//                totalDays = totalDaysInDateRange;
//            } else if (leaveCategoryFrom.equals("Second Half Day") && leaveCategoryTo.equals("First Half Day")) {
//                totalDays = totalDaysInDateRange;
//            } else {
//                // Handle other combinations if needed, default to totalDaysInDateRange
//                totalDays = totalDaysInDateRange;
//            }
//
//            Log.d(TAG, "Calculated totalDays: " + totalDays);
//            //checkLeaveLimit();
//        }
//    }

    private void calculateTotalDaysAndCheckLeaveLimit() {
        String fromDate = binding.etFromDate.getText().toString();
        String toDate = binding.etToDate.getText().toString();
        String leaveCategoryFrom = binding.spinnerLeaveCategoryFrom.getText().toString();
        String leaveCategoryTo = binding.spinnerLeaveCategoryTo.getText().toString();

        Log.w(TAG, "fromDate: " + fromDate + ", toDate: " + toDate + ", leaveCategoryFrom: " + leaveCategoryFrom + ", leaveCategoryTo: " + leaveCategoryTo);

        if (!fromDate.isEmpty() && !toDate.isEmpty() && !leaveCategoryFrom.isEmpty() && !leaveCategoryTo.isEmpty()) {
            // Calculate total days using your existing logic or a helper function
            long totalDaysInDateRange = calculateTotalDays(fromDate, toDate);

            // Apply logic from getTotalNumberOfDays()
            if (leaveCategoryFrom.equals("First Half Day") && leaveCategoryTo.equals("First Half Day")) {
                totalDays = 0.5; // or 0.5f if totalDays is a float
            } else if (leaveCategoryFrom.equals("Second Half Day") && leaveCategoryTo.equals("Second Half Day")) {
                totalDays = 0.5; // or 0.5f if totalDays is a float
            } else {
                totalDays = totalDaysInDateRange;
            }

            Log.d(TAG, "Calculated totalDays: " + totalDays);

            // Apply logic from checkLeaveLimit()
            if (totalDays > balanceLeave) {
                binding.balanceLeaveTextView.setText("Leave limit exceeded");
            }
        }
    }

    private boolean validateForm() {
        String fromDateText = etFromDate.getText().toString();
        String toDateText = etToDate.getText().toString();
        String leaveType = binding.spinnerLeaveType.getText().toString();
        String leaveCategoryFrom = binding.spinnerLeaveCategoryFrom.getText().toString();
        String leaveCategoryTo = binding.spinnerLeaveCategoryTo.getText().toString();
        String leavingStation = binding.spinnerLeavingStation.getText().toString();
        String leaveStationAddress = binding.etLeaveStationAddress.getText().toString();

        // Check if any required field is empty
        if (fromDateText.isEmpty() || toDateText.isEmpty() || leaveType.isEmpty() || leaveCategoryFrom.isEmpty() || leaveCategoryTo.isEmpty() || leavingStation.isEmpty()) {
            showErrorAlert("Please fill in all required fields.");
            return false;
        }

        // Check if leaving station is "Yes" and address is empty
        if (leavingStation.equalsIgnoreCase("Yes") && leaveStationAddress.isEmpty()) {
            showErrorAlert("Please enter your leave station address.");
            return false;
        }

        // Check date range validation
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            Date fromDate = dateFormat.parse(fromDateText);
            Date toDate = dateFormat.parse(toDateText);
            Date today = dateFormat.parse(dateFormat.format(new Date()));

            // Check if it's Sick Leave and allow previous dates
            if (!leaveType.equals("Sick Leave") && fromDate.before(today)) {
                showErrorAlert("Please select correct dates.");
                return false;
            }

            if (fromDate.after(toDate)) {
                showErrorAlert("Please select correct dates.");
                return false;
            }

        } catch (ParseException e) {
            e.printStackTrace();
            showErrorAlert("Invalid date format.");
            return false;
        }

        // If all validations pass
        return true;
    }

    private void validateDateRange() {
        String fromDateText = etFromDate.getText().toString();
        String toDateText = etToDate.getText().toString();
        String selectedLeaveType = binding.spinnerLeaveType.getText().toString();
        String leaveCategoryFrom = binding.spinnerLeaveCategoryFrom.getText().toString();
//        String leaveCategoryTo = binding.spinnerLeaveCategoryTo.getText().toString();

        if (!fromDateText.isEmpty() && !toDateText.isEmpty()) {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                Date fromDate = dateFormat.parse(fromDateText);
                Date toDate = dateFormat.parse(toDateText);
                Date today = dateFormat.parse(dateFormat.format(new Date()));
                long totalDays = calculateTotalDays(fromDateText, toDateText);
                Log.w("LeavesFragment", "totalDays " + totalDays);

//                Log.d(TAG, "fromDate: " + fromDateText + ", toDate: " + toDateText +
//                        ", leaveCategoryFrom: " + leaveCategoryFrom +
//                        ", leaveCategoryTo: " + leaveCategoryTo);
//                Log.w(TAG, "totalDays " + totalDays);
                if (selectedLeaveType.equals("Sick Leave")) {
                    // For Sick Leave, only check if fromDate is after toDate
                    if (fromDate.after(toDate)) {
                        showErrorAlert("From date cannot be after To date.");
                    } else {
                        binding.spinnerLeaveType.setEnabled(true);
                    }
                } else {
                    // For other leave types, apply the usual validation
                    if (fromDate.after(toDate) || fromDate.before(today)) {
                        showErrorAlert("From date cannot be after To date or before today.");
                    } else {
                        binding.spinnerLeaveType.setEnabled(true);
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
                showErrorAlert("Invalid date format.");
            }
        } else {
            showErrorAlert("Please fill in all required fields.");
        }
    }

    private void showDatePicker(final EditText editText) {
        MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
        builder.setTitleText("Select date");
        builder.setSelection(MaterialDatePicker.todayInUtcMilliseconds());

        String selectedLeaveType = binding.spinnerLeaveType.getText().toString();
        long todayInMillis = MaterialDatePicker.todayInUtcMilliseconds();
        long oneDayBeforeInMillis = todayInMillis - (24 * 60 * 60 * 1000);
        Log.d("LeavesFragment", "oneDayBeforeInMillis " + oneDayBeforeInMillis);


        CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();
        // Restrict to current date or future dates, unless it's Sick Leave
        if (!selectedLeaveType.equals("Sick Leave")) {
            constraintsBuilder.setStart(oneDayBeforeInMillis);
            constraintsBuilder.setEnd(todayInMillis);
            constraintsBuilder.setOpenAt(oneDayBeforeInMillis);
            CalendarConstraints.DateValidator dateValidator = new CalendarConstraints.DateValidator() {
                @Override
                public boolean isValid(long date) {
                    return date >= MaterialDatePicker.todayInUtcMilliseconds();
                }

                @Override
                public int describeContents() {
                    return 0;
                }

                @Override
                public void writeToParcel(@NonNull Parcel dest, int flags) {
                }
            };
            constraintsBuilder.setValidator(dateValidator);
        } else {
            constraintsBuilder.setStart(todayInMillis);
            constraintsBuilder.setOpenAt(todayInMillis);

        }

        builder.setCalendarConstraints(constraintsBuilder.build());
        MaterialDatePicker<Long> picker = builder.build();

        picker.addOnPositiveButtonClickListener(selection -> {
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            calendar.setTimeInMillis(selection);
            String selectedDate = dateFormat.format(calendar.getTime());
            editText.setText(selectedDate);

            // Validate date range after 'to date' is selected
            if (editText == etToDate) {
                validateDateRange();
                calculateTotalDaysAndCheckLeaveLimit();
            }
        });

        binding.btnSubmit.setOnClickListener(view -> {
            if (validateForm()) {
                Toast.makeText(requireContext(), "Form submitted successfully", Toast.LENGTH_SHORT).show();
            }
        });

        picker.show(requireActivity().getSupportFragmentManager(), "datePicker");
    }

    private void showErrorAlert(String s) {
        new AlertDialog.Builder(requireContext()).setTitle("Invalid Dates").setMessage("Please select correct dates.").setPositiveButton("OK", null).show();
    }
}


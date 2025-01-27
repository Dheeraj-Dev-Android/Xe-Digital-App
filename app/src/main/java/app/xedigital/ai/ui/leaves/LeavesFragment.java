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
    private double balanceLeave;
    private double totalDays;
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
    private String selectedLeaveTypeId;
    private String reportingManagerName;
    private String reportingManagerLastname;
    private String reportingManagerEmail;
    private String crossFunctionalManagerName;
    private String crossFunctionalManagerEmail;
    private String crossFunctionalManagerId;
    private String authToken;
    private double finalUsedDays;
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
        Calendar calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        etFromDate.setOnClickListener(view -> showDatePicker(etFromDate));
        etToDate.setOnClickListener(view -> showDatePicker(etToDate));

        AutoCompleteTextView leaveTypeSpinner = binding.spinnerLeaveType;
        AutoCompleteTextView leaveCategorySpinnerFrom = binding.spinnerLeaveCategoryFrom;
        AutoCompleteTextView leaveCategorySpinnerTo = binding.spinnerLeaveCategoryTo;
        AutoCompleteTextView leavingStationSpinner = binding.spinnerLeavingStation;

        AtomicReference<TextInputEditText> leaveStationAddress = new AtomicReference<>(binding.etLeaveStationAddress);
        TextInputLayout leaveStationAddressLayout = binding.tilLeaveStationAddress;

        leaveStationAddressLayout.setVisibility(View.GONE);

        leavingStationSpinner.setOnItemClickListener((adapterView, view, position, id) -> {
            String selectedOption = (String) adapterView.getItemAtPosition(position);
            if ("Yes".equalsIgnoreCase(selectedOption)) {
                leaveStationAddressLayout.setVisibility(View.VISIBLE);
            } else {
                leaveStationAddressLayout.setVisibility(View.GONE);
                leaveStationAddress.get().setText("");
            }
        });

        ArrayAdapter<String> leaveCategoriesAdapter = new ArrayAdapter<>(requireContext(), R.layout.dropdown_menu_popup_item, getResources().getStringArray(R.array.leave_categories));
        leaveCategorySpinnerFrom.setAdapter(leaveCategoriesAdapter);
        leaveCategorySpinnerTo.setAdapter(leaveCategoriesAdapter);

        ArrayAdapter<String> leavingStationsAdapter = new ArrayAdapter<>(requireContext(), R.layout.dropdown_menu_popup_item, getResources().getStringArray(R.array.leaving_stations));
        leavingStationSpinner.setAdapter(leavingStationsAdapter);

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        authToken = sharedPreferences.getString("authToken", "");
        String userId = sharedPreferences.getString("userId", "");
        leavesViewModel.setUserId(authToken);
        leavesViewModel.fetchLeavesType();


        ProfileViewModel profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        getContext();
        profileViewModel.storeLoginData(userId, authToken);
        profileViewModel.fetchUserProfile();
        callUserApi(userId, authToken);

        profileViewModel.userProfile.observe(getViewLifecycleOwner(), userprofileResponse -> {
            if (userprofileResponse != null && userprofileResponse.getData() != null && userprofileResponse.getData().getEmployee() != null) {
                Employee employee = userprofileResponse.getData().getEmployee();

                empId = employee.getId();
                empName = employee.getFirstname();
                empLastname = employee.getLastname();
                empEmail = employee.getEmail();
                empDepartment = employee.getDepartment().getName();
                department = empDepartment.replace("\\u0026", "&");
                reportingManagerName = employee.getReportingManager().getFirstname();
                reportingManagerLastname = employee.getReportingManager().getLastname();
                reportingManagerEmail = employee.getReportingManager().getEmail();
                crossFunctionalManagerName = employee.getCrossmanager().getFirstname();
                crossFunctionalManagerEmail = employee.getCrossmanager().getEmail();
                crossFunctionalManagerId = employee.getCrossmanager().getId();
            }
        });

        leavesViewModel.leavesTypeData.observe(getViewLifecycleOwner(), leaveTypeResponse -> {
            if (leaveTypeResponse != null && leaveTypeResponse.getData() != null) {
                leaveTypesList = leaveTypeResponse.getData().getLeavetypes();
                List<String> leaveTypeNames = new ArrayList<>();

                for (LeavetypesItem leaveType : leaveTypesList) {
                    leaveTypeNames.add(leaveType.getLeavetypeName());
                }
                ArrayAdapter<String> leaveTypeAdapter = new ArrayAdapter<>(requireContext(), R.layout.dropdown_menu_popup_item, leaveTypeNames);
                binding.spinnerLeaveType.setAdapter(leaveTypeAdapter);

                List<LeavetypesItem> leaveTypes = leaveTypeResponse.getData().getLeavetypes();
                for (LeavetypesItem leaveType : leaveTypes) {
                    if ("Restricted holiday".equals(leaveType.getLeavetypeName())) {
                        restrictedHolidayId = leaveType.getId();
                        Log.w(TAG, "Restricted Holiday ID: " + restrictedHolidayId);
                        break; // Exit loop once found
                    }
                    if ("Loss of Pay (LOP) / Leave Without Pay (LWP)".equals(leaveType.getLeavetypeName())) {
                        lossOfPayId = leaveType.getId();
                        Log.w(TAG, "Loss of Pay (LOP) / Leave Without Pay (LWP) ID: " + lossOfPayId);
                        break;
                    }
                }

            } else {
                Log.e("LeavesFragment", "Error fetching leaves type data");
            }
        });

        binding.btnClear.setOnClickListener(view -> clearForm());
        leaveCategorySpinnerFrom.setOnItemClickListener((adapterView, view, position, id) -> {
//            Log.d(TAG, "leaveCategoryFrom selected: " + adapterView.getItemAtPosition(position));
//            calculateTotalDaysAndCheckLeaveLimit(selectedLeaveTypeName);
        });

        leaveCategorySpinnerTo.setOnItemClickListener((adapterView, view, position, id) -> {
//            Log.d(TAG, "leaveCategoryTo selected: " + adapterView.getItemAtPosition(position));
//            calculateTotalDaysAndCheckLeaveLimit(selectedLeaveTypeName);
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
                selectedLeaveTypeId = selectedLeaveType.getId();
                selectedLeaveTypeName = selectedLeaveType.getLeavetypeName();
//                Log.d(TAG, "Selected Leave Type ID: " + selectedLeaveTypeId);

//                if (selectedLeaveTypeId.equals("617135e82027d64869450a79")) {
//                    String selectedDate = Objects.requireNonNull(binding.etFromDate.getText()).toString();
//                    checkRestrictedHoliday(selectedDate);
//                }
                if (selectedLeaveTypeName.equals(restrictedHolidayId)) {
                    String fromDate = Objects.requireNonNull(binding.etFromDate.getText()).toString();
                    String toDate = Objects.requireNonNull(binding.etToDate.getText()).toString();
                    checkRestrictedHoliday(fromDate, toDate);
                }

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
//                            Log.e(TAG, "Employee Leave: " + employeeLeaveResponse);
                            balanceLeave = employeeLeaveResponse.getData().getCreditLeave() - (employeeLeaveResponse.getData().getUsedLeave() + employeeLeaveResponse.getData().getDebitLeave());

//                            Log.e(TAG, "Balance Leave: " + balanceLeave);
//                            Log.e(TAG, "Selected Leave Type: " + selectedLeaveTypeName);
                            if (!Objects.requireNonNull(binding.etFromDate.getText()).toString().isEmpty() && !Objects.requireNonNull(binding.etToDate.getText()).toString().isEmpty()) {
                                calculateTotalDaysAndCheckLeaveLimit(selectedLeaveTypeName);
                            }
//                            if (selectedLeaveTypeId.equals(restrictedHolidayId)) {
//                                String selectedDate = binding.etFromDate.getText().toString();
//                                checkRestrictedHoliday(selectedDate);
//                            }
                            if (selectedLeaveTypeName.equals(restrictedHolidayId)) {
                                String fromDate = binding.etFromDate.getText().toString();
                                String toDate = Objects.requireNonNull(binding.etToDate.getText()).toString();
                                checkRestrictedHoliday(fromDate, toDate);
                            }
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

            //            private void checkRestrictedHoliday(String selectedDate) {
//                holidaysViewModel = new ViewModelProvider(requireActivity()).get(HolidaysViewModel.class);
//                holidaysViewModel.getHolidaysList().observe(getViewLifecycleOwner(), new Observer<List<HolidaysItem>>() {
//                    @Override
//                    public void onChanged(List<HolidaysItem> holidaysItems) {
//                        if (holidaysItems != null) {
//                            boolean isRestrictedHoliday = false;
//                            for (HolidaysItem holiday : holidaysItems) {
//                                if (holiday.getHolidayDate().equals(selectedDate) && holiday.isIsOptional()) {
////                                    Log.d(TAG, "Selected date is a restricted holiday");
//                                    isRestrictedHoliday = true;
//                                    break;
//                                }
//                            }
//                            if (!isRestrictedHoliday) {
//                                new AlertDialog.Builder(requireContext()).setTitle("Error").setMessage("Selected date is not a restricted holiday").setPositiveButton("OK", (dialog, which) -> {
//                                    dialog.dismiss();
//                                    clearForm();
//                                }).show();
//                            }
//                            holidaysViewModel.getHolidaysList().removeObserver(this);
//                        } else {
//                            Toast.makeText(getContext(), "Holiday data not available", Toast.LENGTH_LONG).show();
//                            Log.e(TAG, "Holiday data not available");
//                        }
//                    }
//                });
//            }
            private void checkRestrictedHoliday(String fromDate, String toDate) {
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
                                        Log.d(TAG, "Selected date is a restricted holiday");
                                        isRestrictedHoliday = true;
                                        break;
                                    }
                                }
                                if (!isRestrictedHoliday) {
                                    Log.d(TAG, "Selected date is not a restricted holiday");
                                    binding.balanceLeaveTextView.setText("Selected Date is not Restricted Holiday " + " Balance Leave: " + balanceLeave);
                                    isValidDateRange = false;
                                    break;
                                }
                            }

                            if (!isValidDateRange) {
                                // Show alert and clear form
                                new AlertDialog.Builder(requireContext()).setTitle("Error").setMessage("Selected date range contains non-restricted holidays.").setPositiveButton("OK", (dialog, which) -> {
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

            // Helper function to get all dates between two dates
            private List<String> getDatesBetween(String startDate, String endDate) {
                List<String> dates = new ArrayList<>();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                try {
                    Calendar startCalendar = Calendar.getInstance();
                    startCalendar.setTime(sdf.parse(startDate));
                    Calendar endCalendar = Calendar.getInstance();
                    endCalendar.setTime(sdf.parse(endDate));

                    while (startCalendar.before(endCalendar) || startCalendar.equals(endCalendar)) {
                        dates.add(sdf.format(startCalendar.getTime()));
                        startCalendar.add(Calendar.DAY_OF_MONTH, 1);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                return dates;
            }
        });

        binding.btnSubmit.setOnClickListener(view -> {

            if (validateForm()) {
                String fromDate = Objects.requireNonNull(binding.etFromDate.getText()).toString();
                String toDate = Objects.requireNonNull(binding.etToDate.getText()).toString();
                String leaveCategoryFrom = binding.spinnerLeaveCategoryFrom.getText().toString();
                String leaveCategoryTo = binding.spinnerLeaveCategoryTo.getText().toString();
                String leavingStation = binding.spinnerLeavingStation.getText().toString();
                String leaveStationAdd = Objects.requireNonNull(binding.etLeaveStationAddress.getText()).toString();
                String contactNumber = Objects.requireNonNull(binding.etContactNumber.getText()).toString();
                String reason = Objects.requireNonNull(binding.etReason.getText()).toString();

                applyLeave(fromDate, toDate, leaveCategoryFrom, leaveCategoryTo, leavingStation, leaveStationAdd, contactNumber, reason);
            }
        });
        return root;
    }

    private void applyLeave(String fromDate, String toDate, String leaveCategoryFrom, String leaveCategoryTo, String leavingStation, String leaveStationAdd, String contactNumber, String reason) {
        applyLeaveRequest = new ApplyLeaveRequest();
//        Log.d(TAG, "applyLeaveRequest: " + applyLeaveRequest);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneId.of("UTC"));
        String appliedDate = formatter.format(Instant.now());

        department = empDepartment.replace("\\u0026", "&");
//Form Data
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

//        Employee Data
        applyLeaveRequest.setAppliedDate(appliedDate);
        applyLeaveRequest.setDepartment(department);
        applyLeaveRequest.setEmployee(empId);
        applyLeaveRequest.setEmpFirstName(empName);
        applyLeaveRequest.setEmpLastName(empLastname);
        applyLeaveRequest.setEmpEmail(empEmail);
        applyLeaveRequest.setHrEmail(hrMail);
        applyLeaveRequest.setStatus("");
        applyLeaveRequest.setReportingManager(reportingManagerEmail);
        applyLeaveRequest.setReportingManagerName(reportingManagerName);
        applyLeaveRequest.setReportingManagerLastName(reportingManagerLastname);
        if (crossFunctionalManagerName == null || crossFunctionalManagerName.isEmpty() || crossFunctionalManagerEmail == null || crossFunctionalManagerEmail.isEmpty()) {
            showAlertDialog1();
            return;
        }
        applyLeaveRequest.setCrossManager(crossFunctionalManagerId);
        applyLeaveRequest.setCrossManagerEmail(crossFunctionalManagerEmail);
        applyLeaveRequest.setCrossManagerName(crossFunctionalManagerName);

        Call<ResponseBody> applyLeave = APIClient.getInstance().LeavesApply().LeavesApply("jwt " + authToken, applyLeaveRequest);

        applyLeave.enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        String responseBody = response.body().string();
//                        Log.d(TAG, "Response: " + responseBody);
//                        Condition to not deduct leave if its LOP
                        if (!Objects.equals(selectedLeaveTypeId, lossOfPayId)) {
                            debitLeave(fromDate, toDate, leaveCategoryFrom, leaveCategoryTo, leavingStation, leaveStationAdd, contactNumber, reason);
                        }

                        JSONObject jsonObject = new JSONObject(responseBody);
                        String message = jsonObject.getString("message");
//                        Log.d(TAG, "Message: " + message);
                        clearForm();
                        showAlertDialog("Leave Applied", message);
                    } else {
                        showAlertDialog("Leave Error ", response.message());
                        Log.e(TAG, "Error: " + response.code());
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                    showAlertDialog("Error", "Failed to apply leave");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
                Log.e(TAG, "onFailure: " + throwable.getMessage());
                showAlertDialog("Error", throwable.getMessage());

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

    private void showAlertDialog1() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("No Cross-functional Manager").setMessage("No cross-functional manager available. Please contact your Admin Or HR.").setPositiveButton("OK", (dialog, which) -> {
            clearForm();
            dialog.dismiss();
        }).show();

    }

    private void debitLeave(String fromDate, String toDate, String leaveCategoryFrom, String leaveCategoryTo, String leavingStation, String leaveStationAdd, String contactNumber, String reason) {
        debitLeaveRequest = new DebitLeaveRequest();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneId.of("UTC"));
        String appliedDate = formatter.format(Instant.now());

//        department = empDepartment.replace("\\u0026", "&");
//Form Data
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

//        Employee Data
        debitLeaveRequest.setAppliedDate(appliedDate);
        debitLeaveRequest.setDepartment(empDepartment);
        debitLeaveRequest.setEmployee(empId);
        debitLeaveRequest.setEmpFirstName(empName);
        debitLeaveRequest.setEmpLastName(empLastname);
        debitLeaveRequest.setEmpEmail(empEmail);
        debitLeaveRequest.setHrEmail(hrMail);
        debitLeaveRequest.setStatus("");
        debitLeaveRequest.setReportingManager(reportingManagerEmail);
        debitLeaveRequest.setReportingManagerName(reportingManagerName);
        debitLeaveRequest.setReportingManagerLastName(reportingManagerLastname);

        debitLeaveRequest.setTDays((int) totalDays);
        debitLeaveRequest.setFUsedDays((int) finalUsedDays);

        Call<ResponseBody> debitLeave = APIClient.getInstance().debitLeave().LeavesUsedDebit("jwt " + authToken, debitLeaveRequest);

        debitLeave.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        String responseBody = response.body().string();
                        JSONObject jsonObject = new JSONObject(responseBody);
                        String message = jsonObject.getString("message");
                        clearForm();
                    } else {
                        Log.e(TAG, "Debit Error: " + response.code());
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();

                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
                Log.e(TAG, "onFailure: " + throwable.getMessage());
            }
        });

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
//                    Log.d(TAG, "Branch ID: " + branchId);
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
                        hrMail = responseBranch.getData().getBranch().getNotificationEmail();
//                        Log.d("BranchData", "Notification Email: " + hrMail);
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
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate startDateObj = LocalDate.parse(startDate, formatter);
            LocalDate endDateObj = LocalDate.parse(endDate, formatter);
            return ChronoUnit.DAYS.between(startDateObj, endDateObj) + 1;
        } catch (DateTimeParseException e) {
            Log.e(TAG, "Error parsing dates: " + e.getMessage());
            return 0;
        }
    }

    private void calculateTotalDaysAndCheckLeaveLimit(String selectedLeaveTypeName) {
        String fromDate = binding.etFromDate.getText().toString();
        String toDate = binding.etToDate.getText().toString();
        String leaveCategoryFrom = binding.spinnerLeaveCategoryFrom.getText().toString();
        String leaveCategoryTo = binding.spinnerLeaveCategoryTo.getText().toString();

//        Log.w(TAG, "fromDate: " + fromDate + ", toDate: " + toDate + ", leaveCategoryFrom: " + leaveCategoryFrom + ", leaveCategoryTo: " + leaveCategoryTo);

        if (!fromDate.isEmpty() && !toDate.isEmpty() && !leaveCategoryFrom.isEmpty() && !leaveCategoryTo.isEmpty()) {
            long totalDaysInDateRange = calculateTotalDays(fromDate, toDate);

            // Apply logic from getTotalNumberOfDays()
            if (leaveCategoryFrom.equals("First Half Day") && leaveCategoryTo.equals("First Half Day")) {
                totalDays = 0.5; // or 0.5f if totalDays is a float
            } else if (leaveCategoryFrom.equals("Second Half Day") && leaveCategoryTo.equals("Second Half Day")) {
                totalDays = 0.5; // or 0.5f if totalDays is a float
            } else {
                totalDays = totalDaysInDateRange;
            }
            finalUsedDays(totalDays);
//            Log.d(TAG, "Calculated totalDays: " + totalDays + "balanceLeave : " + balanceLeave);
            if (totalDays > balanceLeave && selectedLeaveTypeName != null && !selectedLeaveTypeName.equals("Loss of Pay (LOP) / Leave Without Pay (LWP)")) {
                showLeaveLimitExceededAlert();
            }
        }
    }

    public void finalUsedDays(double totalDays) {
        String leaveCategoryFrom = binding.spinnerLeaveCategoryFrom.getText().toString();
        String leaveCategoryTo = binding.spinnerLeaveCategoryTo.getText().toString();
        finalUsedDays = totalDays;

        if (leaveCategoryFrom.equals("First Half Day") && leaveCategoryTo.equals("First Half Day")) {
            finalUsedDays -= 0.5;
        } else if (leaveCategoryFrom.equals("First Half Day") && leaveCategoryTo.equals("Second Half Day")) {
            // No adjustment needed for full day
        } else if (leaveCategoryFrom.equals("Second Half Day") && leaveCategoryTo.equals("Second Half Day")) {
            finalUsedDays -= 0.5;
        }
//        Log.e(TAG, "finalUsedDays: " + finalUsedDays);

    }

    private void showLeaveLimitExceededAlert() {
        new AlertDialog.Builder(requireContext()).setTitle("Leave Limit Exceeded").setMessage("The total leave days exceed your available balance.").setPositiveButton("OK", (dialog, which) -> {
            dialog.dismiss();
            binding.spinnerLeaveType.setText("");
        }).show();
    }

    private boolean validateForm() {
        String fromDateText = etFromDate.getText().toString();
        String toDateText = etToDate.getText().toString();
        String leaveType = binding.spinnerLeaveType.getText().toString();
        String leaveCategoryFrom = binding.spinnerLeaveCategoryFrom.getText().toString();
        String leaveCategoryTo = binding.spinnerLeaveCategoryTo.getText().toString();
        String leavingStation = binding.spinnerLeavingStation.getText().toString();
        String leaveStationAddress = binding.etLeaveStationAddress.getText().toString();

        if (fromDateText.isEmpty() || toDateText.isEmpty() || leaveType.isEmpty() || leaveCategoryFrom.isEmpty() || leaveCategoryTo.isEmpty() || leavingStation.isEmpty()) {
            showErrorAlert("Please fill in all required fields.");
            return false;
        }
        if (leavingStation.equalsIgnoreCase("Yes") && leaveStationAddress.isEmpty()) {
            showErrorAlert("Please enter your leave station address.");
            return false;
        }
        // Check date range validation
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date fromDate = dateFormat.parse(fromDateText);
            Date toDate = dateFormat.parse(toDateText);
            Date today = dateFormat.parse(dateFormat.format(new Date()));
            // Check if it's Sick Leave and allow previous dates
            if (!leaveType.equals("Sick Leave") && fromDate.before(today)) {
                showErrorAlert("Please select correct dates.");
                return false;
            }
            assert fromDate != null;
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
//        String leaveCategoryFrom = binding.spinnerLeaveCategoryFrom.getText().toString();

        if (!fromDateText.isEmpty() && !toDateText.isEmpty()) {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date fromDate = dateFormat.parse(fromDateText);
                Date toDate = dateFormat.parse(toDateText);
                Date today = dateFormat.parse(dateFormat.format(new Date()));
                long totalDays = calculateTotalDays(fromDateText, toDateText);
//                Log.w("LeavesFragment", "totalDays " + totalDays);

                if (selectedLeaveType.equals("Sick Leave")) {
                    // For Sick Leave, only check if fromDate is after toDate
                    assert fromDate != null;
                    if (fromDate.after(toDate)) {
                        showErrorAlert("From date cannot be after To date.");
                    } else {
                        binding.spinnerLeaveType.setEnabled(true);
                    }
                } else {
                    // For other leave types, apply the usual validation
                    assert fromDate != null;
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
//        Log.d("LeavesFragment", "oneDayBeforeInMillis " + oneDayBeforeInMillis);


        CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();
        // Restrict to current date or future dates, unless it's Sick Leave
        if (!selectedLeaveType.equals("Sick Leave")) {
            constraintsBuilder.setStart(oneDayBeforeInMillis);
//            constraintsBuilder.setEnd(todayInMillis);
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
                calculateTotalDaysAndCheckLeaveLimit(selectedLeaveTypeName);
            }
        });

        picker.show(requireActivity().getSupportFragmentManager(), "datePicker");
    }

    private void showErrorAlert(String message) {
        new AlertDialog.Builder(requireContext()).setTitle("Error").setMessage(message).setPositiveButton("OK", null).show();
    }
}


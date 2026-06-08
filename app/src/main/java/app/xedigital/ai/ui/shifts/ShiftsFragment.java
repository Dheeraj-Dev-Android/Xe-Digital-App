package app.xedigital.ai.ui.shifts;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import app.xedigital.ai.R;
import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.api.APIInterface;
import app.xedigital.ai.databinding.FragmentShiftsBinding;
import app.xedigital.ai.model.profile.Data;
import app.xedigital.ai.model.profile.Employee;
import app.xedigital.ai.model.shiftTime.ShiftTypesItem;
import app.xedigital.ai.model.shiftUpdate.ReportingManager;
import app.xedigital.ai.model.shiftUpdate.ShiftUpdateRequest;
import app.xedigital.ai.model.shifts.ShiftsItem;
import app.xedigital.ai.ui.profile.ProfileViewModel;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;

public class ShiftsFragment extends Fragment {
    private String fName;
    private String lName;
    private String email;
    private String contact;

    private String EmployeeId;
    private String EmployeeCode;
    private String EmployeeDepartment;
    private String EmpReportingManagerId;
    private String EmpReportingManagerEmail;
    private String EmpReportingManagerName;
    private String EmpReportingManagerFName;
    private String EmpReportingManagerLName;


    private FragmentShiftsBinding binding;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentShiftsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        ShiftsViewModel shiftsViewModel = new ViewModelProvider(this).get(ShiftsViewModel.class);
        ProfileViewModel profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String authToken = sharedPreferences.getString("authToken", "");
        String userId = sharedPreferences.getString("userId", "");
        shiftsViewModel.callUserApi(userId, authToken);
        shiftsViewModel.setAuthToken(authToken);
        profileViewModel.storeLoginData(userId, authToken);
        profileViewModel.fetchUserProfile();

        shiftsViewModel.getShiftTypes().observe(getViewLifecycleOwner(), shiftDataList -> {
            if (shiftDataList != null) {
                List<String> shiftNames = new ArrayList<>();
                for (ShiftsItem shift : shiftDataList) {
                    shiftNames.add(shift.getShifttypeName());
                }
                ArrayAdapter<String> shiftTypeAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, shiftNames);
                binding.shiftTypeSpinner.setAdapter(shiftTypeAdapter);

                binding.shiftTypeSpinner.setOnItemClickListener((parent, view, position, id) -> {
                    String selectedShiftType = (String) parent.getItemAtPosition(position);

                    String selectedShiftId = null;
                    for (ShiftsItem shift : shiftDataList) {
                        if (shift.getShifttypeName().equals(selectedShiftType)) {
                            selectedShiftId = shift.getId();
                            break;
                        }
                    }
                    if (selectedShiftId != null) {
                        shiftsViewModel.fetchShiftTimes(selectedShiftId);
                    } else {
                        Log.e("ShiftsFragment", "Selected shift type not found in shiftDataList");
                        Toast.makeText(requireContext(), "Selected shift type not found in shiftDataList", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        shiftsViewModel.getShiftTimes().observe(getViewLifecycleOwner(), shiftTimes -> {
            if (shiftTimes != null) {
                List<String> formattedShiftTimes = new ArrayList<>();
                for (ShiftTypesItem shift : shiftTimes) {
                    String formattedTime = String.format("%s(%s-%s)", shift.getName(), shift.getStartTime(), shift.getEndTime());
                    formattedShiftTimes.add(formattedTime);
                }

                ArrayAdapter<String> shiftTimeAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, formattedShiftTimes);
                binding.shiftTimeSpinner.setAdapter(shiftTimeAdapter);
            }
        });
        shiftsViewModel.getHrMailLiveData().observe(getViewLifecycleOwner(), hrMail -> {
            if (hrMail != null) {
                binding.hrEmailEditText.setText(hrMail);
            }
        });

        profileViewModel.userProfile.observe(getViewLifecycleOwner(), userProfile -> {
            if (userProfile == null || userProfile.getData() == null) {
                Log.e("ShiftsFragment", "UserProfileResponse or Data object is completely null");
                return;
            }

            Data profileData = userProfile.getData();
            if (profileData.getEmployee() == null) {
                Log.e("ShiftsFragment", "Data exists, but the 'Employee' object field is null");
                return;
            }

            Employee employee = profileData.getEmployee();

            fName = employee.getFirstname();
            lName = employee.getLastname();
            email = employee.getEmail();
            contact = employee.getContact();

            binding.firstNameEditText.setText(fName);
            binding.lastNameEditText.setText(lName);
            binding.emailEditText.setText(email);
            binding.contactEditText.setText(contact);

            EmployeeId = employee.getId();
            EmployeeCode = employee.getEmployeeCode();

            // 3. Null-safe check for Department nested object
            if (employee.getDepartment() != null) {
                EmployeeDepartment = employee.getDepartment().getName();
            } else {
                EmployeeDepartment = "N/A";
            }

            // 4. Null-safe check for Reporting Manager nested object
            if (employee.getReportingManager() != null) {
                app.xedigital.ai.model.profile.ReportingManager manager = employee.getReportingManager();
                EmpReportingManagerId = manager.getId();
                EmpReportingManagerEmail = manager.getEmail();
                EmpReportingManagerFName = manager.getFirstname();
                EmpReportingManagerLName = manager.getLastname();
                EmpReportingManagerName = (EmpReportingManagerFName != null ? EmpReportingManagerFName : "")
                        + " " + (EmpReportingManagerLName != null ? EmpReportingManagerLName : "");
            } else {
                Log.w("ShiftsFragment", "Reporting Manager details are missing for this employee instance");
                EmpReportingManagerId = "";
                EmpReportingManagerEmail = "";
                EmpReportingManagerName = "No Manager Assigned";
            }
        });

        TextInputEditText firstNameEditText = binding.firstNameEditText;
        TextInputEditText lastNameEditText = binding.lastNameEditText;
        TextInputEditText contactEditText = binding.contactEditText;
        TextInputEditText emailEditText = binding.emailEditText;
        TextInputEditText hrEmailEditText = binding.hrEmailEditText;

        hrEmailEditText.setEnabled(false);
        firstNameEditText.setEnabled(false);
        lastNameEditText.setEnabled(false);
        contactEditText.setEnabled(false);
        emailEditText.setEnabled(false);

        AutoCompleteTextView shiftTypeSpinner = binding.shiftTypeSpinner;
        AutoCompleteTextView shiftTimeSpinner = binding.shiftTimeSpinner;

        Button clearButton = binding.clearButton;
        Button submitButton = binding.submitButton;
        Chip appliedShiftsChip = binding.viewAppliedShiftsChip;
        Chip pendingApproval = binding.pendingApprovalChip;

        appliedShiftsChip.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_nav_shifts_to_nav_shift_applied);
            Toast.makeText(requireContext(), "Applied Shifts", Toast.LENGTH_SHORT).show();
        });
        pendingApproval.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_nav_shifts_to_nav_shift_approve);
            Toast.makeText(requireContext(), "Pending Approval", Toast.LENGTH_SHORT).show();
        });


        clearButton.setOnClickListener(view -> {
            shiftTypeSpinner.setText("");
            shiftTimeSpinner.setText("");
            Toast.makeText(requireContext(), "Cleared", Toast.LENGTH_SHORT).show();
        });


        binding.shiftTypeSpinner.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().isEmpty()) {
                    binding.shiftTypeTextInputLayout.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        binding.shiftTimeSpinner.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().isEmpty()) {
                    binding.shiftTimeTextInputLayout.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        submitButton.setOnClickListener(view -> {
            if (validateForm()) {
                String firstName = firstNameEditText.getText().toString();
                String lastName = lastNameEditText.getText().toString();
                String email = emailEditText.getText().toString();
                String contact = contactEditText.getText().toString();
                String shiftType = shiftTypeSpinner.getText().toString();
                String shiftTime = shiftTimeSpinner.getText().toString();
                String hrEmail = hrEmailEditText.getText().toString();

                ShiftsItem selectedShiftItem = null;
                if (shiftsViewModel.getShiftTypes().getValue() != null) {
                    for (ShiftsItem shift : shiftsViewModel.getShiftTypes().getValue()) {
                        if (shift.getShifttypeName().equals(shiftType)) {
                            selectedShiftItem = shift;
                            break;
                        }
                    }
                }

                if (selectedShiftItem != null) {
                    String shiftTypeId = selectedShiftItem.getId();

                    ShiftTypesItem selectedShiftTimeItem = null;
                    if (shiftsViewModel.getShiftTimes().getValue() != null) {
                        for (ShiftTypesItem shift : shiftsViewModel.getShiftTimes().getValue()) {
                            if (String.format("%s(%s-%s)", shift.getName(), shift.getStartTime(), shift.getEndTime()).equals(shiftTime)) {
                                selectedShiftTimeItem = shift;
                                break;
                            }
                        }
                    }

                    if (selectedShiftTimeItem != null) {
                        String shiftTimeId = selectedShiftTimeItem.getId();

                        ShiftUpdateRequest requestBody = new ShiftUpdateRequest();
                        requestBody.setContact(contact);
                        requestBody.setEmail(email);
                        requestBody.setFirstname(firstName);
                        requestBody.setLastname(lastName);
                        requestBody.setShiftType(shiftTypeId);
                        requestBody.setShift(shiftTimeId);
                        requestBody.setHrEmail(hrEmail);
                        requestBody.setEmployee(EmployeeId);
                        requestBody.setEmployeeCode(EmployeeCode);
                        requestBody.setDepartment(EmployeeDepartment);
                        requestBody.setReportingManagerEmail(EmpReportingManagerEmail);
                        requestBody.setReportingManagerName(EmpReportingManagerName);
                        requestBody.setStatus("");

                        ReportingManager reportingManager = new ReportingManager();
                        reportingManager.setId(EmpReportingManagerId);
                        reportingManager.setFirstname(EmpReportingManagerFName);
                        reportingManager.setLastname(EmpReportingManagerLName);
                        reportingManager.setEmail(EmpReportingManagerEmail);

                        requestBody.setReportingManager(reportingManager);

                        APIInterface shiftChangeApi = APIClient.getInstance().getShiftTypes();
                        Call<ResponseBody> call = shiftChangeApi.ShiftChange("jwt " + authToken, requestBody);
                        call.enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull retrofit2.Response<ResponseBody> response) {
                                if (response.isSuccessful()) {
                                    shiftTypeSpinner.setText("");
                                    shiftTimeSpinner.setText("");
                                    Toast.makeText(requireContext(), "Shift change request submitted successfully", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(requireContext(), "Failed to submit shift change request", Toast.LENGTH_SHORT).show();
                                    Log.e("ShiftsFragment", "API Error: " + response.errorBody());
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                                Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                Log.e("ShiftsFragment", "Network Error: " + t.getMessage());
                            }
                        });
                    } else {
                        Toast.makeText(requireContext(), "Invalid shift time selected", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(requireContext(), "Invalid shift type selected", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(requireContext(), "Please select values from both the dropdown list", Toast.LENGTH_SHORT).show();
            }
        });
        return root;

    }

    private boolean validateForm() {
        boolean isValid = true;

        if (binding.shiftTypeSpinner.getText().toString().isEmpty()) {
            binding.shiftTypeTextInputLayout.setError("Shift type is required");
            isValid = false;
        } else {
            binding.shiftTypeTextInputLayout.setError(null);
        }

        if (binding.shiftTimeSpinner.getText().toString().isEmpty()) {
            binding.shiftTimeTextInputLayout.setError("Shift time is required");
            isValid = false;
        } else {
            binding.shiftTimeTextInputLayout.setError(null);
        }

        return isValid;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
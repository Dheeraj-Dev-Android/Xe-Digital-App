package app.xedigital.ai.ui.shifts;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

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
import app.xedigital.ai.utills.SecurePrefManager;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;

public class ShiftsFragment extends Fragment {

    private static final String TAG = "ShiftsFragment";

    // Employee details
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
    private SecurePrefManager prefManager;
    private String authToken;
    private ShiftsViewModel shiftsViewModel;
    private ProfileViewModel profileViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentShiftsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        initViewModels();
        setupSpinners();
        setupObservers();
        setupClickListeners();
        setupTextWatchers();
        disableReadOnlyFields();

        return root;
    }

    // ==============================================
    // INIT VIEWMODELS & DATA LOADING
    // ==============================================
    private void initViewModels() {
        ShiftsViewModel shiftsViewModel = new ViewModelProvider(this).get(ShiftsViewModel.class);
        ProfileViewModel profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        prefManager = SecurePrefManager.getInstance(requireContext());
        authToken = prefManager.getString("authToken", "");
        String userId = prefManager.getString("userId", "");

        shiftsViewModel.callUserApi(userId, authToken);
        shiftsViewModel.setAuthToken(authToken);
        profileViewModel.storeLoginData(userId, authToken);
        profileViewModel.fetchUserProfile();

        // Store as instance so observers can use them
        this.shiftsViewModel = shiftsViewModel;
        this.profileViewModel = profileViewModel;
    }

    // ==============================================
    // SPINNERS SETUP (True Native Behaviour)
    // ==============================================
    private void setupSpinners() {
        // Force dropdown behavior — dismiss soft keyboard, show list on click
        binding.shiftTypeSpinner.setOnClickListener(v -> binding.shiftTypeSpinner.showDropDown());
        binding.shiftTimeSpinner.setOnClickListener(v -> binding.shiftTimeSpinner.showDropDown());
    }

    // ==============================================
    // OBSERVERS
    // ==============================================
    private void setupObservers() {

        // Shift Types Observer
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
                        // Clear previous shift time selection
                        binding.shiftTimeSpinner.setText("");
                        shiftsViewModel.fetchShiftTimes(selectedShiftId);
                    } else {
                        Log.e(TAG, "Selected shift type not found in shiftDataList");
                        Toast.makeText(requireContext(), "Shift type not found", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        // Shift Times Observer
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

        // HR Email Observer
        shiftsViewModel.getHrMailLiveData().observe(getViewLifecycleOwner(), hrMail -> {
            if (hrMail != null) {
                binding.hrEmailEditText.setText(hrMail);
            }
        });

        // User Profile Observer
        profileViewModel.userProfile.observe(getViewLifecycleOwner(), userProfile -> {
            if (userProfile == null || userProfile.getData() == null) {
                Log.e(TAG, "UserProfileResponse or Data is null");
                return;
            }

            Data profileData = userProfile.getData();
            if (profileData.getEmployee() == null) {
                Log.e(TAG, "Employee data is null");
                return;
            }

            Employee employee = profileData.getEmployee();
            populateEmployeeData(employee);
        });
    }

    // ==============================================
    // POPULATE EMPLOYEE DATA
    // ==============================================
    private void populateEmployeeData(Employee employee) {
        fName = employee.getFirstname();
        lName = employee.getLastname();
        email = employee.getEmail();
        contact = employee.getContact();

        // Hidden edit texts (for form submission compatibility)
        binding.firstNameEditText.setText(fName);
        binding.lastNameEditText.setText(lName);
        binding.emailEditText.setText(email);
        binding.contactEditText.setText(contact);

        // Display full name in identity card
        String fullName = ((fName != null ? fName : "") + " " + (lName != null ? lName : "")).trim();
        binding.userNameDisplay.setText(fullName.isEmpty() ? "Employee" : fullName);

        // Load profile image via Glide
        loadProfileImage(employee);

        // Save critical IDs
        EmployeeId = employee.getId();
        EmployeeCode = employee.getEmployeeCode();

        // Department (null-safe)
        if (employee.getDepartment() != null) {
            EmployeeDepartment = employee.getDepartment().getName();
        } else {
            EmployeeDepartment = "N/A";
        }

        // Reporting Manager (null-safe)
        if (employee.getReportingManager() != null) {
            app.xedigital.ai.model.profile.ReportingManager manager = employee.getReportingManager();
            EmpReportingManagerId = manager.getId();
            EmpReportingManagerEmail = manager.getEmail();
            EmpReportingManagerFName = manager.getFirstname();
            EmpReportingManagerLName = manager.getLastname();
            EmpReportingManagerName = (EmpReportingManagerFName != null ? EmpReportingManagerFName : "") + " " + (EmpReportingManagerLName != null ? EmpReportingManagerLName : "");
        } else {
            Log.w(TAG, "Reporting Manager missing");
            EmpReportingManagerId = "";
            EmpReportingManagerEmail = "";
            EmpReportingManagerName = "No Manager Assigned";
        }
    }

    // ==============================================
    // LOAD PROFILE IMAGE VIA GLIDE
    // ==============================================
    private void loadProfileImage(Employee employee) {
        try {
            String imageUrl = null;

            // 👇 Try to get image URL from employee — adjust based on your model
            // Common getter names: getImage(), getProfilePic(), getAvatar(), getPhoto()
            if (employee.getProfileImageUrl() != null && !employee.getProfileImageUrl().isEmpty()) {
                imageUrl = employee.getProfileImageUrl();
            }

            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(requireContext()).load(imageUrl).placeholder(R.drawable.ic_person).error(R.drawable.ic_person).diskCacheStrategy(DiskCacheStrategy.ALL).circleCrop().into(binding.avatarImageView);
            } else {
                // No image URL — show placeholder
                binding.avatarImageView.setImageResource(R.drawable.ic_person);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading profile image: " + e.getMessage());
            binding.avatarImageView.setImageResource(R.drawable.ic_person);
        }
    }

    // ==============================================
    // DISABLE READ-ONLY FIELDS
    // ==============================================
    private void disableReadOnlyFields() {
        binding.firstNameEditText.setEnabled(false);
        binding.lastNameEditText.setEnabled(false);
        binding.emailEditText.setEnabled(false);
        binding.contactEditText.setEnabled(false);
        binding.hrEmailEditText.setEnabled(false);
    }

    // ==============================================
    // CLICK LISTENERS
    // ==============================================
    private void setupClickListeners() {

        // Applied Shifts Chip
        binding.viewAppliedShiftsChip.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_nav_shifts_to_nav_shift_applied);
            Toast.makeText(requireContext(), "Applied Shifts", Toast.LENGTH_SHORT).show();
        });

        // Pending Approval Chip
        binding.pendingApprovalChip.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_nav_shifts_to_nav_shift_approve);
            Toast.makeText(requireContext(), "Pending Approval", Toast.LENGTH_SHORT).show();
        });

        // Clear Button
        binding.clearButton.setOnClickListener(view -> {
            binding.shiftTypeSpinner.setText("");
            binding.shiftTimeSpinner.setText("");
            binding.shiftTypeTextInputLayout.setError(null);
            binding.shiftTimeTextInputLayout.setError(null);
            Toast.makeText(requireContext(), "Cleared", Toast.LENGTH_SHORT).show();
        });

        // Submit Button
        binding.submitButton.setOnClickListener(view -> handleSubmit());
    }

    // ==============================================
    // TEXT WATCHERS
    // ==============================================
    private void setupTextWatchers() {

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
    }

    // ==============================================
    // FORM SUBMISSION
    // ==============================================
    private void handleSubmit() {
        if (!validateForm()) {
            Toast.makeText(requireContext(), "Please select both shift type and timing", Toast.LENGTH_SHORT).show();
            return;
        }

        String firstName = binding.firstNameEditText.getText().toString();
        String lastName = binding.lastNameEditText.getText().toString();
        String userEmail = binding.emailEditText.getText().toString();
        String userContact = binding.contactEditText.getText().toString();
        String shiftType = binding.shiftTypeSpinner.getText().toString();
        String shiftTime = binding.shiftTimeSpinner.getText().toString();
        String hrEmail = binding.hrEmailEditText.getText().toString();

        // Find selected shift type
        ShiftsItem selectedShiftItem = null;
        if (shiftsViewModel.getShiftTypes().getValue() != null) {
            for (ShiftsItem shift : shiftsViewModel.getShiftTypes().getValue()) {
                if (shift.getShifttypeName().equals(shiftType)) {
                    selectedShiftItem = shift;
                    break;
                }
            }
        }

        if (selectedShiftItem == null) {
            Toast.makeText(requireContext(), "Invalid shift type selected", Toast.LENGTH_SHORT).show();
            return;
        }

        String shiftTypeId = selectedShiftItem.getId();

        // Find selected shift time
        ShiftTypesItem selectedShiftTimeItem = null;
        if (shiftsViewModel.getShiftTimes().getValue() != null) {
            for (ShiftTypesItem shift : shiftsViewModel.getShiftTimes().getValue()) {
                String formattedTime = String.format("%s(%s-%s)", shift.getName(), shift.getStartTime(), shift.getEndTime());
                if (formattedTime.equals(shiftTime)) {
                    selectedShiftTimeItem = shift;
                    break;
                }
            }
        }

        if (selectedShiftTimeItem == null) {
            Toast.makeText(requireContext(), "Invalid shift time selected", Toast.LENGTH_SHORT).show();
            return;
        }

        String shiftTimeId = selectedShiftTimeItem.getId();

        // Build request body
        ShiftUpdateRequest requestBody = new ShiftUpdateRequest();
        requestBody.setContact(userContact);
        requestBody.setEmail(userEmail);
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

        // API Call
        submitShiftChange(requestBody);
    }

    // ==============================================
    // API SUBMISSION
    // ==============================================
    private void submitShiftChange(ShiftUpdateRequest requestBody) {
        APIInterface shiftChangeApi = APIClient.getInstance().getShiftTypes();
        Call<ResponseBody> call = shiftChangeApi.ShiftChange("jwt " + authToken, requestBody);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull retrofit2.Response<ResponseBody> response) {
                if (binding == null) return;

                if (response.isSuccessful()) {
                    binding.shiftTypeSpinner.setText("");
                    binding.shiftTimeSpinner.setText("");
                    Toast.makeText(requireContext(), "✓ Shift change request submitted successfully", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(requireContext(), "Failed to submit shift change request", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "API Error: " + response.errorBody());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                if (binding == null) return;

                Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Network Error: " + t.getMessage());
            }
        });
    }

    // ==============================================
    // FORM VALIDATION
    // ==============================================
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
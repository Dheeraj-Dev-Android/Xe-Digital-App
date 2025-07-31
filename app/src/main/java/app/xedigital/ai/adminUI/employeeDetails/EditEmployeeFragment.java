package app.xedigital.ai.adminUI.employeeDetails;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import app.xedigital.ai.R;
import app.xedigital.ai.databinding.FragmentEditEmployeeBinding;
import app.xedigital.ai.model.Admin.ActiveShift.ShiftsItem;
import app.xedigital.ai.model.Admin.EmployeeDetails.EmployeesItem;
import app.xedigital.ai.model.Admin.department.DepartmentsItem;
import app.xedigital.ai.model.Admin.updateEmployee.UpdateEmployeeRequest;
import app.xedigital.ai.utills.DateTimeUtils;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditEmployeeFragment extends Fragment {

    public static final String ARG_SELECTED_ITEM = "selected_item";

    private FragmentEditEmployeeBinding binding;
    private EditEmployeeViewModel viewModel;
    private String token;
    private EmployeesItem employee;
    private boolean isHROrAdmin;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEditEmployeeBinding.inflate(inflater, container, false);
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("AdminCred", Context.MODE_PRIVATE);
        token = sharedPreferences.getString("authToken", "");

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(EditEmployeeViewModel.class);

        setupDepartmentSpinner();
        setupShiftSpinner();
        setupReportingManagerSpinner();
        setupCrossManagerSpinner();

        // Get data from bundle
        Bundle args = getArguments();
        if (args != null && args.containsKey(ARG_SELECTED_ITEM)) {
            employee = (EmployeesItem) args.getSerializable(ARG_SELECTED_ITEM);
            if (employee != null) {

                viewModel.setSelectedEmployee(employee);
                populateEmployeeForm(employee);
                viewModel.fetchDepartments(token);
                viewModel.fetchShifts(token);
                viewModel.fetchEmployees(token);
            }
        }

        ArrayAdapter<String> differentlyAbleAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, getResources().getStringArray(R.array.differently_able_options));

        binding.spinnerDifferentlyAbledType.setAdapter(differentlyAbleAdapter);

// Set selection from existing employee data if available
        employee = viewModel.getSelectedEmployee().getValue();
        if (employee != null && employee.getDifferentlyAbled() != null) {
            String currentValue = employee.getDifferentlyAbled();
            int position = differentlyAbleAdapter.getPosition(currentValue);
            if (position >= 0) {
                binding.spinnerDifferentlyAbledType.setText(currentValue, false);
            }
        }

        // Joining Type Spinner
        ArrayAdapter<String> joiningTypeAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, getResources().getStringArray(R.array.joining_type_options));
        binding.spinnerJoiningType.setAdapter(joiningTypeAdapter);

        employee = viewModel.getSelectedEmployee().getValue();
        if (employee != null && employee.getJoiningType() != null) {
            int pos = joiningTypeAdapter.getPosition(employee.getJoiningType());
            if (pos >= 0) binding.spinnerJoiningType.setText(employee.getJoiningType(), false);
        }

// Status Spinner
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, getResources().getStringArray(R.array.status_options));
        binding.spinnerStatus.setAdapter(statusAdapter);

        if (employee != null) {
            String statusValue = employee.isActive() ? "Active" : "Deactive";
            int pos = statusAdapter.getPosition(statusValue);
            if (pos >= 0) binding.spinnerStatus.setText(statusValue, false);
        }

// Employee Type Spinner
        ArrayAdapter<String> employeeTypeAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, getResources().getStringArray(R.array.employee_type_options));
        binding.spinnerEmployeeType.setAdapter(employeeTypeAdapter);

        if (employee != null && employee.getEmployeeType() != null) {
            int pos = employeeTypeAdapter.getPosition(employee.getEmployeeType());
            if (pos >= 0) binding.spinnerEmployeeType.setText(employee.getEmployeeType(), false);
        }

        SwitchMaterial switchIsHROrAdmin = view.findViewById(R.id.switchIsHROrAdmin);
        isHROrAdmin = switchIsHROrAdmin.isChecked();

        binding.btnClear.setOnClickListener(v -> clearForm());

        binding.btnSubmit.setOnClickListener(v -> {
            EmployeesItem selected = viewModel.getSelectedEmployee().getValue();
            if (selected == null) return;

            UpdateEmployeeRequest payload = new UpdateEmployeeRequest();
            payload.setEmployeeCode(String.valueOf(binding.etEmployeeCode.getText()));
            payload.setFirstname(Objects.requireNonNull(binding.etFirstName.getText()).toString());
            payload.setLastname(Objects.requireNonNull(binding.etLastName.getText()).toString());
            payload.setEmail(Objects.requireNonNull(binding.etEmail.getText()).toString());
            payload.setContact(Objects.requireNonNull(binding.etContact.getText()).toString());
            payload.setDateOfBirth(Objects.requireNonNull(binding.etDOB.getText()).toString());
            payload.setJoiningDate(Objects.requireNonNull(binding.etJoiningDate.getText()).toString());
            payload.setJoiningType(binding.spinnerJoiningType.getText().toString().toLowerCase());
            payload.setReportingManager(selected.getReportingManager().getId());
            payload.setCrossmanager(selected.getCrossmanager().getId());
            payload.setDesignation(Objects.requireNonNull(binding.etDesignation.getText()).toString());
            payload.setLevel(Objects.requireNonNull(binding.etLevel.getText()).toString());
            payload.setGrade(Objects.requireNonNull(binding.etGrade.getText()).toString());
            payload.setIsHROrAdmin(binding.switchIsHROrAdmin.isChecked());
            payload.setCompany(selected.getCompany());
            payload.setDepartment(selected.getDepartment().getId());
            payload.setPartner("");
            payload.setShift(selected.getShift().getId());

            // Convert to lowercase or format as needed
            payload.setEmployeeType(binding.spinnerEmployeeType.getText().toString().equals("Internal Employee") ? "employee" : "partner");

            String status = binding.spinnerStatus.getText().toString();
            payload.setActive(status.equalsIgnoreCase("Active"));

            payload.setIsVerified(selected.isIsVerified());
            payload.setFatherName(Objects.requireNonNull(binding.etFatherName.getText()).toString());
            payload.setPanNo(Objects.requireNonNull(binding.etPAN.getText()).toString());
            payload.setAdharNo(Objects.requireNonNull(binding.etAadhaar.getText()).toString());
            payload.setDifferentlyAbled(binding.spinnerDifferentlyAbledType.getText().toString());
            payload.setAddress(Objects.requireNonNull(binding.etResidentialAddress.getText()).toString());
            payload.setState(Objects.requireNonNull(binding.etState.getText()).toString());
            payload.setPincode(Objects.requireNonNull(binding.etPinCode.getText()).toString());

            // Call API
            viewModel.updateEmployee(token, selected.getId(), payload, new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    if (response.isSuccessful()) {

                        NavController navController = Navigation.findNavController(binding.getRoot());
                        navController.navigate(R.id.nav_editEmployee_to_nav_employees);
                        Toast.makeText(getContext(), "Employee updated successfully!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Failed to update employee", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

    }

    private void setupDepartmentSpinner() {
        viewModel.getDepartments().observe(getViewLifecycleOwner(), departments -> {
            if (departments != null) {
                List<String> names = new ArrayList<>();
                for (DepartmentsItem item : departments) {
                    names.add(item.getName());
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, names);
                binding.spinnerDepartment.setAdapter(adapter);

                binding.spinnerDepartment.setOnItemClickListener((parent, view, position, id) -> {
                    DepartmentsItem selected = departments.get(position);
                    viewModel.setSelectedDepartment(selected);
                });

                // Preselect if editing
                if (employee != null && employee.getDepartment() != null) {
                    int position = names.indexOf(employee.getDepartment().getName());
                    if (position >= 0) binding.spinnerDepartment.setSelection(position);
                }
            }
        });
    }

    private void setupShiftSpinner() {
        viewModel.getShifts().observe(getViewLifecycleOwner(), shifts -> {
            if (shifts != null) {
                List<String> names = new ArrayList<>();
                for (ShiftsItem item : shifts) {
                    names.add(item.getName());
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, names);
                binding.spinnerShift.setAdapter(adapter);

                binding.spinnerShift.setOnItemClickListener((parent, view, position, id) -> {
                    ShiftsItem selected = shifts.get(position);
                    viewModel.setSelectedShift(selected);
                });

                if (employee != null && employee.getShift() != null) {
                    int position = names.indexOf(employee.getShift().getName() + " (" + employee.getShift().getStartTime() + "-" + employee.getShift().getEndTime() + ")");
                    if (position >= 0) binding.spinnerShift.setSelection(position);
                }
            }
        });
    }

    private void setupReportingManagerSpinner() {
        viewModel.getEmployees().observe(getViewLifecycleOwner(), employees -> {
            if (employees != null) {
                List<String> names = new ArrayList<>();
                for (EmployeesItem item : employees) {
                    names.add(item.getFirstname());
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, names);
                binding.spinnerReportingManager.setAdapter(adapter);

                binding.spinnerReportingManager.setOnItemClickListener((parent, view, position, id) -> {
                    EmployeesItem selected = employees.get(position);
                    viewModel.setSelectedReportingManager(selected);
                });

                if (employee != null && employee.getReportingManager() != null) {
                    int position = names.indexOf(employee.getReportingManager().getFirstname() + " " + employee.getReportingManager().getLastname());
                    if (position >= 0) binding.spinnerReportingManager.setSelection(position);
                }
            }
        });
    }

    private void setupCrossManagerSpinner() {
        viewModel.getEmployees().observe(getViewLifecycleOwner(), employees -> {
            if (employees != null) {
                List<String> names = new ArrayList<>();
                for (EmployeesItem item : employees) {
                    names.add(item.getFirstname());
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, names);
                binding.spinnerCrossFunctionalManager.setAdapter(adapter);

                binding.spinnerCrossFunctionalManager.setOnItemClickListener((parent, view, position, id) -> {
                    EmployeesItem selected = employees.get(position);
                    viewModel.setSelectedCrossManager(selected);
                });

                if (employee != null && employee.getCrossmanager() != null) {
                    int position = names.indexOf(employee.getCrossmanager().getFirstname() + " " + employee.getCrossmanager().getLastname());
                    if (position >= 0) binding.spinnerCrossFunctionalManager.setSelection(position);
                }
            }
        });
    }


    private void populateEmployeeForm(EmployeesItem employee) {
        binding.etFirstName.setText(employee.getFirstname());
        binding.etLastName.setText(employee.getLastname());
        binding.etFatherName.setText(employee.getFatherName());
        binding.etEmail.setText(employee.getEmail());
        binding.etContact.setText(employee.getContact());
        binding.etResidentialAddress.setText(employee.getAddress());
        binding.etState.setText(employee.getState());
        binding.etPinCode.setText(employee.getPincode());
        binding.etEmployeeCode.setText(employee.getEmployeeCode());
        binding.etDesignation.setText(employee.getDesignation());
        binding.etLevel.setText(employee.getLevel());
        binding.etGrade.setText(employee.getGrade());
        binding.etDOB.setText(DateTimeUtils.formatTime(employee.getDateOfBirth()));
        binding.etJoiningDate.setText(DateTimeUtils.formatTime(employee.getJoiningDate()));
        binding.etPAN.setText(employee.getPanNo());
        binding.etAadhaar.setText(employee.getAdharNo());
        binding.spinnerStatus.setText(employee.isActive() ? "Active" : "Inactive");
        binding.spinnerEmployeeType.setText(employee.getEmployeeType());
        binding.spinnerDepartment.setText(employee.getDepartment().getName());
        binding.spinnerShift.setText(employee.getShift().getName() + " " + employee.getShift().getStartTime() + "-" + employee.getShift().getEndTime());
        binding.spinnerJoiningType.setText(employee.getJoiningType());
        binding.spinnerReportingManager.setText(employee.getReportingManager().getFirstname());
        binding.spinnerCrossFunctionalManager.setText(employee.getCrossmanager().getFirstname());
        binding.spinnerDifferentlyAbledType.setText(employee.getDifferentlyAbled());
        binding.switchIsHROrAdmin.setChecked(employee.isIsHROrAdmin());


    }

    private void clearForm() {
        binding.etFirstName.setText("");
        binding.etLastName.setText("");
        binding.etFatherName.setText("");
        binding.etEmail.setText("");
        binding.etContact.setText("");
        binding.etResidentialAddress.setText("");
        binding.etState.setText("");
        binding.etPinCode.setText("");
        binding.etEmployeeCode.setText("");
        binding.etDesignation.setText("");
        binding.etLevel.setText("");
        binding.etGrade.setText("");
        binding.etDOB.setText("");
        binding.etJoiningDate.setText("");
        binding.etPAN.setText("");
        binding.etAadhaar.setText("");
        binding.spinnerStatus.setText("");
        binding.spinnerEmployeeType.setText("");
        binding.spinnerDepartment.setText("");
        binding.spinnerShift.setText("");
        binding.spinnerJoiningType.setText("");
        binding.spinnerReportingManager.setText("");
        binding.spinnerCrossFunctionalManager.setText("");
        binding.spinnerDifferentlyAbledType.setText("");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

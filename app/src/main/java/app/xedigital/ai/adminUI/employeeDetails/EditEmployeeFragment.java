package app.xedigital.ai.adminUI.employeeDetails;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.Objects;

import app.xedigital.ai.databinding.FragmentEditEmployeeBinding;
import app.xedigital.ai.model.Admin.EmployeeDetails.EmployeesItem;

public class EditEmployeeFragment extends Fragment {

    public static final String ARG_SELECTED_ITEM = "selected_item";

    private FragmentEditEmployeeBinding binding;
    private EditEmployeeViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEditEmployeeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(EditEmployeeViewModel.class);

        // Get data from bundle
        Bundle args = getArguments();
        if (args != null && args.containsKey(ARG_SELECTED_ITEM)) {
            EmployeesItem employee = (EmployeesItem) args.getSerializable(ARG_SELECTED_ITEM);
            if (employee != null) {

                viewModel.setSelectedEmployee(employee);
                populateEmployeeForm(employee);
            }
        }

        // Submit button listener
        binding.btnSubmit.setOnClickListener(v -> {
            String firstName = Objects.requireNonNull(binding.etFirstName.getText()).toString().trim();
            String lastName = Objects.requireNonNull(binding.etLastName.getText()).toString().trim();

            Toast.makeText(getContext(), "Submitted: " + firstName + " " + lastName, Toast.LENGTH_SHORT).show();
        });

        binding.btnClear.setOnClickListener(v -> clearForm());
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
        binding.etDOB.setText(employee.getDateOfBirth());
        binding.etJoiningDate.setText(employee.getJoiningDate());
        binding.etPAN.setText(employee.getPanNo());
        binding.etAadhaar.setText(employee.getAdharNo());
        binding.spinnerStatus.setText(employee.isActive() ? "Active" : "Inactive");
        binding.spinnerEmployeeType.setText(employee.getEmployeeType());
        binding.spinnerDepartment.setText(employee.getDepartment().getName());
        binding.spinnerShift.setText(employee.getShift().getName());
        binding.spinnerJoiningType.setText(employee.getJoiningType());
        binding.spinnerReportingManager.setText(employee.getReportingManager().getFirstname());
        binding.spinnerCrossFunctionalManager.setText(employee.getCrossmanager().getFirstname());
        binding.spinnerDifferentlyAbledType.setText(employee.getDifferentlyAbled());
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

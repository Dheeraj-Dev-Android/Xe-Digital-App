package app.xedigital.ai.admin.adminUI.allUsers;

import static app.xedigital.ai.ui.timesheet.SelectedTimesheetFragment.ARG_SELECTED_ITEM;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import app.xedigital.ai.R;
import app.xedigital.ai.admin.adminModal.AdminUsers.UsersItem;
import app.xedigital.ai.admin.adminModal.Role.RolesItem;
import app.xedigital.ai.databinding.FragmentEditUserBinding;
import app.xedigital.ai.utills.CustomDialogHelper;
import app.xedigital.ai.utills.SecurePrefManager;

public class EditUserFragment extends Fragment {

    private FragmentEditUserBinding binding;
    private EditUserViewModel viewModel;
    private UsersItem selectedUser;
    private String authToken;

    // Colors for box states
    private int colorRed;
    private int colorGreen;
    private int colorDefault;

    public EditUserFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(EditUserViewModel.class);

        if (getArguments() != null && getArguments().containsKey(ARG_SELECTED_ITEM)) {
            selectedUser = (UsersItem) getArguments().getSerializable(ARG_SELECTED_ITEM);
        }

        SecurePrefManager prefs = SecurePrefManager.getInstance(requireContext());
        authToken = "jwt " + prefs.getString("authToken", "");

        viewModel.fetchRoles(authToken);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentEditUserBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize colors
        colorRed = ContextCompat.getColor(requireContext(), R.color.rejected_color);
        colorGreen = ContextCompat.getColor(requireContext(), R.color.approved_color);
        colorDefault = ContextCompat.getColor(requireContext(), R.color.accent_employee);

        // Length filters for passwords
        binding.editTextPassword.setFilters(
                new InputFilter[]{new InputFilter.LengthFilter(16)}
        );
        binding.editTextConfirmPassword.setFilters(
                new InputFilter[]{new InputFilter.LengthFilter(16)}
        );

        // Populate selected user data
        if (selectedUser != null) {
            binding.editTextFirstName.setText(selectedUser.getFirstname());
            binding.editTextLastName.setText(selectedUser.getLastname());
            binding.editTextEmail.setText(selectedUser.getEmail());
        } else {
            CustomDialogHelper.showErrorDialog(
                    requireContext(),
                    "Missing Data",
                    "User data is not available. Please go back and try again.",
                    () -> {
                        if (isAdded() && getView() != null) {
                            Navigation.findNavController(requireView()).popBackStack();
                        }
                    }
            );
        }

        // Setup status dropdown
        setupStatusDropdown();

        // Observe ViewModel
        observeViewModel();

        // Password watchers
        setupPasswordWatcher();
        setupConfirmPasswordWatcher();

        // Button handlers
        binding.buttonUpdate.setOnClickListener(v -> onUpdateClicked());
        binding.clearForm.setOnClickListener(v -> onClearClicked());
    }

    private void setupStatusDropdown() {
        List<String> statusOptions = new ArrayList<>();
        statusOptions.add("Active");
        statusOptions.add("Deactive");

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                statusOptions
        );
        binding.spinnerStatus.setAdapter(statusAdapter);

        if (selectedUser != null) {
            String currentStatus = selectedUser.isActive() ? "Active" : "Deactive";
            binding.spinnerStatus.setText(currentStatus, false);
        }
    }

    private void observeViewModel() {
        // Roles
        viewModel.getRolesLiveData().observe(getViewLifecycleOwner(), roles -> {
            if (roles != null && !roles.isEmpty()) {
                List<String> roleNames = new ArrayList<>();
                for (RolesItem role : roles) {
                    roleNames.add(role.getDisplayName());
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        roleNames
                );
                binding.spinnerRole.setAdapter(adapter);

                if (selectedUser != null && selectedUser.getRole() != null) {
                    binding.spinnerRole.setText(
                            selectedUser.getRole().getDisplayName(), false
                    );
                }
            } else {
                CustomDialogHelper.showErrorDialog(
                        requireContext(),
                        "Load Failed",
                        "Could not load roles. Please check your connection and try again.",
                        null
                );
            }
        });

        // Loading state
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
            if (binding == null) return;
            binding.progressBar.setVisibility(
                    Boolean.TRUE.equals(loading) ? View.VISIBLE : View.GONE
            );
        });

        // Validation errors
        viewModel.getValidationError().observe(getViewLifecycleOwner(), error -> {
            if (error == null || error.isEmpty()) return;
            CustomDialogHelper.showErrorDialog(
                    requireContext(),
                    "Validation Error",
                    error,
                    null
            );
            viewModel.clearValidationError();
        });

        // Update result
        viewModel.getUpdateResult().observe(getViewLifecycleOwner(), result -> {
            if (result == null) return;

            if (result.success) {
                CustomDialogHelper.showSuccessDialog(
                        requireContext(),
                        "Updated",
                        result.message,
                        () -> {
                            if (isAdded() && getView() != null) {
                                Navigation.findNavController(requireView()).popBackStack();
                            }
                        }
                );
            } else {
                CustomDialogHelper.showErrorDialog(
                        requireContext(),
                        "Update Failed",
                        result.message,
                        null
                );
            }
            viewModel.clearUpdateResult();
        });
    }

    private void setupPasswordWatcher() {
        binding.editTextPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String password = s.toString();

                if (password.isEmpty()) {
                    binding.passwordRulesContainer.setVisibility(View.GONE);
                    binding.textInputLayoutPassword.setError(null);
                    setPasswordBoxColor(colorDefault);
                    resetConfirmPasswordBox();
                    return;
                }

                binding.passwordRulesContainer.setVisibility(View.VISIBLE);

                String firstName = Objects.requireNonNull(
                        binding.editTextFirstName.getText()
                ).toString().trim();
                String lastName = Objects.requireNonNull(
                        binding.editTextLastName.getText()
                ).toString().trim();

                EditUserViewModel.PasswordRuleStatus st =
                        viewModel.evaluatePasswordRules(password, firstName, lastName);

                updateRule(binding.ruleLength, st.lengthOk);
                updateRule(binding.ruleUppercase, st.upperOk);
                updateRule(binding.ruleLowercase, st.lowerOk);
                updateRule(binding.ruleNumber, st.numberOk);
                updateRule(binding.ruleSpecial, st.specialOk);
                updateRule(binding.ruleNoSpaceComma, st.noSpaceCommaOk);
                updateRule(binding.ruleNoName, st.noNameOk);
                updateRule(binding.ruleNoSequential, st.noSequentialOk);
                updateRule(binding.ruleNoRepeated, st.noRepeatedOk);

                setPasswordBoxColor(st.allPassed() ? colorGreen : colorRed);
                updateConfirmPasswordBoxColor();
            }
        });
    }

    private void setupConfirmPasswordWatcher() {
        binding.editTextConfirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().isEmpty()) {
                    resetConfirmPasswordBox();
                    return;
                }
                updateConfirmPasswordBoxColor();
            }
        });
    }

    private void setPasswordBoxColor(int color) {
        binding.textInputLayoutPassword.setBoxStrokeColor(color);
        binding.textInputLayoutPassword.setHintTextColor(
                ColorStateList.valueOf(color)
        );
    }

    private void updateConfirmPasswordBoxColor() {
        String password = Objects.requireNonNull(
                binding.editTextPassword.getText()
        ).toString().trim();
        String confirm = Objects.requireNonNull(
                binding.editTextConfirmPassword.getText()
        ).toString().trim();

        if (confirm.isEmpty()) {
            resetConfirmPasswordBox();
            return;
        }

        if (password.equals(confirm)) {
            binding.textInputLayoutConfirmPassword.setBoxStrokeColor(colorGreen);
            binding.textInputLayoutConfirmPassword.setHintTextColor(
                    ColorStateList.valueOf(colorGreen)
            );
            binding.textInputLayoutConfirmPassword.setError(null);
        } else {
            binding.textInputLayoutConfirmPassword.setBoxStrokeColor(colorRed);
            binding.textInputLayoutConfirmPassword.setHintTextColor(
                    ColorStateList.valueOf(colorRed)
            );
            binding.textInputLayoutConfirmPassword.setError("Passwords do not match");
        }
    }

    private void resetConfirmPasswordBox() {
        binding.textInputLayoutConfirmPassword.setBoxStrokeColor(colorDefault);
        binding.textInputLayoutConfirmPassword.setHintTextColor(
                ColorStateList.valueOf(colorDefault)
        );
        binding.textInputLayoutConfirmPassword.setError(null);
    }

    private void updateRule(TextView ruleTextView, boolean isPassed) {
        int colorRes = isPassed ? R.color.approved_color : R.color.rejected_color;
        int iconRes = isPassed ? R.drawable.ic_check_circle : R.drawable.ic_cancel_circle;

        int color = ContextCompat.getColor(requireContext(), colorRes);
        int sizePx = dpToPx(14);

        ruleTextView.setTextColor(color);

        Drawable drawable = ContextCompat.getDrawable(requireContext(), iconRes);
        if (drawable != null) {
            drawable = drawable.mutate();
            drawable.setBounds(0, 0, sizePx, sizePx);
            drawable.setTint(color);
        }
        ruleTextView.setCompoundDrawables(drawable, null, null, null);
        ruleTextView.setCompoundDrawablePadding(dpToPx(6));
    }

    private int dpToPx(int dp) {
        float density = requireContext().getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private void onUpdateClicked() {
        String firstName = Objects.requireNonNull(
                binding.editTextFirstName.getText()).toString().trim();
        String lastName = Objects.requireNonNull(
                binding.editTextLastName.getText()).toString().trim();
        String email = Objects.requireNonNull(
                binding.editTextEmail.getText()).toString().trim();
        String password = Objects.requireNonNull(
                binding.editTextPassword.getText()).toString().trim();
        String confirmPassword = Objects.requireNonNull(
                binding.editTextConfirmPassword.getText()).toString().trim();
        String roleName = binding.spinnerRole.getText().toString().trim();
        String status = binding.spinnerStatus.getText().toString().trim();

        boolean valid = viewModel.validateForm(
                firstName, lastName, email, roleName, status,
                password, confirmPassword
        );

        if (!valid) return;

        // Confirm before updating
        CustomDialogHelper.showWarningDialog(
                requireContext(),
                "Confirm Update",
                "Are you sure you want to update this user's details?",
                "Update",
                "Cancel",
                new CustomDialogHelper.OnWarningActionListener() {
                    @Override
                    public void onConfirm() {
                        viewModel.updateUser(
                                authToken, selectedUser,
                                firstName, lastName, email,
                                roleName, status,
                                password, confirmPassword
                        );
                    }

                    @Override
                    public void onCancel() {
                        // No-op
                    }
                }
        );
    }

    private void onClearClicked() {
        CustomDialogHelper.showWarningDialog(
                requireContext(),
                "Clear Form",
                "This will clear all the fields. Do you want to continue?",
                "Clear",
                "Cancel",
                new CustomDialogHelper.OnWarningActionListener() {
                    @Override
                    public void onConfirm() {
                        binding.editTextFirstName.setText("");
                        binding.editTextLastName.setText("");
                        binding.editTextEmail.setText("");
                        binding.editTextPassword.setText("");
                        binding.editTextConfirmPassword.setText("");
                        binding.spinnerRole.setText("", false);
                        binding.spinnerStatus.setText("", false);

                        binding.textInputLayoutPassword.setError(null);
                        binding.textInputLayoutConfirmPassword.setError(null);
                        binding.passwordRulesContainer.setVisibility(View.GONE);

                        setPasswordBoxColor(colorDefault);
                        resetConfirmPasswordBox();
                    }

                    @Override
                    public void onCancel() {
                        // No-op
                    }
                }
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
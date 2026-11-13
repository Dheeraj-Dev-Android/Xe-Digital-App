package app.xedigital.ai.admin.adminUI.allUsers;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import app.xedigital.ai.R;
import app.xedigital.ai.admin.adminModal.Role.RolesItem;
import app.xedigital.ai.admin.adminModal.registerNewUser.RegisterUserRequestBody;
import app.xedigital.ai.utills.CustomAlertDialog;
import app.xedigital.ai.utills.SecurePrefManager;

public class AddUserFragment extends Fragment {

    // Role data
    private final List<RolesItem> rolesList = new ArrayList<>();
    private AddUserViewModel mViewModel;
    private String token;
    // Colors for box states
    private int colorRed;
    private int colorGreen;
    private int colorDefault;
    // Form fields
    private AutoCompleteTextView spinnerRole;
    private AutoCompleteTextView spinnerStatus;
    private TextInputEditText editTextFirstName;
    private TextInputEditText editTextLastName;
    private TextInputEditText editTextEmail;
    private TextInputEditText editTextPassword;
    private TextInputEditText editTextConfirmPassword;
    // TextInputLayouts for box color control
    private TextInputLayout textInputLayoutPassword;
    private TextInputLayout textInputLayoutConfirmPassword;
    // Password rules
    private View passwordRulesContainer;
    private TextView ruleLength, ruleUppercase, ruleLowercase, ruleNumber;
    private TextView ruleSpecial, ruleNoSpaceComma, ruleNoName, ruleNoSequential, ruleNoRepeated;
    // Buttons & progress
    private MaterialButton clearFormBtn;
    private MaterialButton submitBtn;
    private ProgressBar progressBar;
    private String selectedRoleId = null;

    public static AddUserFragment newInstance() {
        return new AddUserFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_user, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(AddUserViewModel.class);

        // Initialize colors
        colorRed = ContextCompat.getColor(requireContext(), R.color.rejected_color);
        colorGreen = ContextCompat.getColor(requireContext(), R.color.approved_color);
        colorDefault = ContextCompat.getColor(requireContext(), R.color.accent_employee);

        SecurePrefManager sharedPreferences = SecurePrefManager.getInstance(requireContext());
        token = sharedPreferences.getString("authToken", "");

        initViews(view);
        applyInputFilters();
        setupStatusDropdown();
        setupPasswordWatcher();
        setupConfirmPasswordWatcher();
        setupButtons();
        observeViewModel();

        mViewModel.fetchRoles("jwt " + token);
    }

    private void initViews(View view) {
        spinnerRole = view.findViewById(R.id.spinnerRole);
        spinnerStatus = view.findViewById(R.id.spinnerStatus);
        editTextFirstName = view.findViewById(R.id.editTextFirstName);
        editTextLastName = view.findViewById(R.id.editTextLastName);
        editTextEmail = view.findViewById(R.id.editTextEmail);
        editTextPassword = view.findViewById(R.id.editTextPassword);
        editTextConfirmPassword = view.findViewById(R.id.editTextConfirmPassword);

        textInputLayoutPassword = view.findViewById(R.id.textInputLayoutPassword);
        textInputLayoutConfirmPassword = view.findViewById(R.id.textInputLayoutConfirmPassword);

        passwordRulesContainer = view.findViewById(R.id.passwordRulesContainer);
        ruleLength = view.findViewById(R.id.ruleLength);
        ruleUppercase = view.findViewById(R.id.ruleUppercase);
        ruleLowercase = view.findViewById(R.id.ruleLowercase);
        ruleNumber = view.findViewById(R.id.ruleNumber);
        ruleSpecial = view.findViewById(R.id.ruleSpecial);
        ruleNoSpaceComma = view.findViewById(R.id.ruleNoSpaceComma);
        ruleNoName = view.findViewById(R.id.ruleNoName);
        ruleNoSequential = view.findViewById(R.id.ruleNoSequential);
        ruleNoRepeated = view.findViewById(R.id.ruleNoRepeated);

        clearFormBtn = view.findViewById(R.id.clearForm);
        submitBtn = view.findViewById(R.id.buttonSubmit);
        progressBar = view.findViewById(R.id.progress_bar);
    }

    private void applyInputFilters() {
        editTextPassword.setFilters(new InputFilter[]{new InputFilter.LengthFilter(16)});
        editTextConfirmPassword.setFilters(new InputFilter[]{new InputFilter.LengthFilter(16)});
    }

    private void setupStatusDropdown() {
        String[] statusOptions = {"active", "deactive"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, statusOptions);
        spinnerStatus.setAdapter(statusAdapter);
        // No default selection — hint "Select Option" will show
    }

    private void populateRoleDropdown(List<RolesItem> roles) {
        rolesList.clear();
        rolesList.addAll(roles);

        List<String> roleNames = new ArrayList<>();
        Map<String, String> roleNameToIdMap = new HashMap<>();

        for (RolesItem role : roles) {
            if (role == null) continue;
            String displayName = role.getDisplayName();
            if (displayName == null || displayName.trim().isEmpty()) {
                displayName = role.getName();
            }
            if (displayName != null && !displayName.trim().isEmpty()) {
                String trimmed = displayName.trim();
                roleNames.add(trimmed);
                roleNameToIdMap.put(trimmed, role.getId());
            }
        }

        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, roleNames);
        spinnerRole.setAdapter(roleAdapter);
        // No default selection — hint "Select Option" will show
        selectedRoleId = null;

        spinnerRole.setOnItemClickListener((parent, v, position, id) -> {
            String selectedName = (String) parent.getItemAtPosition(position);
            selectedRoleId = roleNameToIdMap.get(selectedName);
        });
    }

    // ─────────────────────────────────────────────────────────────
    // Real-time Password Rule Watcher
    // ─────────────────────────────────────────────────────────────
    private void setupPasswordWatcher() {
        editTextPassword.addTextChangedListener(new TextWatcher() {
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
                    passwordRulesContainer.setVisibility(View.GONE);
                    textInputLayoutPassword.setError(null);
                    setPasswordBoxColor(colorDefault);
                    resetConfirmPasswordBox();
                    return;
                }

                passwordRulesContainer.setVisibility(View.VISIBLE);

                String firstName = Objects.requireNonNull(editTextFirstName.getText()).toString().trim().toLowerCase();
                String lastName = Objects.requireNonNull(editTextLastName.getText()).toString().trim().toLowerCase();

                boolean lengthOk = password.length() >= 16 && password.length() <= 18;
                boolean upperOk = password.matches(".*[A-Z].*");
                boolean lowerOk = password.matches(".*[a-z].*");
                boolean numberOk = password.matches(".*[0-9].*");
                boolean specialOk = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|.<>/?`~].*");
                boolean noSpaceCommaOk = !password.contains(" ") && !password.contains(",");

                boolean nameValid = firstName.isEmpty() || !password.toLowerCase().contains(firstName);
                if (!lastName.isEmpty() && password.toLowerCase().contains(lastName)) {
                    nameValid = false;
                }

                boolean noSequentialOk = !hasSequentialChars(password, 4);
                boolean noRepeatedOk = !hasRepeatedChars(password, 4);

                updateRule(ruleLength, lengthOk);
                updateRule(ruleUppercase, upperOk);
                updateRule(ruleLowercase, lowerOk);
                updateRule(ruleNumber, numberOk);
                updateRule(ruleSpecial, specialOk);
                updateRule(ruleNoSpaceComma, noSpaceCommaOk);
                updateRule(ruleNoName, nameValid);
                updateRule(ruleNoSequential, noSequentialOk);
                updateRule(ruleNoRepeated, noRepeatedOk);

                boolean allPassed = lengthOk && upperOk && lowerOk && numberOk && specialOk && noSpaceCommaOk && nameValid && noSequentialOk && noRepeatedOk;

                if (allPassed) {
                    setPasswordBoxColor(colorGreen);
                } else {
                    setPasswordBoxColor(colorRed);
                }

                updateConfirmPasswordBoxColor();
            }
        });
    }

    // ─────────────────────────────────────────────────────────────
    // Real-time Confirm Password Watcher
    // ─────────────────────────────────────────────────────────────
    private void setupConfirmPasswordWatcher() {
        editTextConfirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String confirmPassword = s.toString();
                if (confirmPassword.isEmpty()) {
                    resetConfirmPasswordBox();
                    return;
                }
                updateConfirmPasswordBoxColor();
            }
        });
    }

    // ─────────────────────────────────────────────────────────────
    // Password Box Color Helpers
    // ─────────────────────────────────────────────────────────────
    private void setPasswordBoxColor(int color) {
        textInputLayoutPassword.setBoxStrokeColor(color);
        textInputLayoutPassword.setHintTextColor(ColorStateList.valueOf(color));
    }

    private void updateConfirmPasswordBoxColor() {
        String password = Objects.requireNonNull(editTextPassword.getText()).toString().trim();
        String confirmPassword = Objects.requireNonNull(editTextConfirmPassword.getText()).toString().trim();

        if (confirmPassword.isEmpty()) {
            resetConfirmPasswordBox();
            return;
        }

        if (password.equals(confirmPassword)) {
            textInputLayoutConfirmPassword.setBoxStrokeColor(colorGreen);
            textInputLayoutConfirmPassword.setHintTextColor(ColorStateList.valueOf(colorGreen));
            textInputLayoutConfirmPassword.setError(null);
        } else {
            textInputLayoutConfirmPassword.setBoxStrokeColor(colorRed);
            textInputLayoutConfirmPassword.setHintTextColor(ColorStateList.valueOf(colorRed));
            textInputLayoutConfirmPassword.setError("Passwords do not match");
        }
    }

    private void resetConfirmPasswordBox() {
        textInputLayoutConfirmPassword.setBoxStrokeColor(colorDefault);
        textInputLayoutConfirmPassword.setHintTextColor(ColorStateList.valueOf(colorDefault));
        textInputLayoutConfirmPassword.setError(null);
    }

    // ─────────────────────────────────────────────────────────────
    // Rule Row UI Update (color + icon)
    // ─────────────────────────────────────────────────────────────
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

    // ─────────────────────────────────────────────────────────────
    // Sequential & Repeated Character Checks
    // ─────────────────────────────────────────────────────────────
    private boolean hasSequentialChars(String password, int sequenceLength) {
        if (password.length() < sequenceLength) return false;
        String lowerPassword = password.toLowerCase();
        for (int i = 0; i <= lowerPassword.length() - sequenceLength; i++) {
            boolean isAscending = true;
            boolean isDescending = true;
            for (int j = 0; j < sequenceLength - 1; j++) {
                char current = lowerPassword.charAt(i + j);
                char next = lowerPassword.charAt(i + j + 1);
                if (next - current != 1) isAscending = false;
                if (current - next != 1) isDescending = false;
            }
            if (isAscending || isDescending) return true;
        }
        return false;
    }

    private boolean hasRepeatedChars(String password, int repeatLength) {
        if (password.length() < repeatLength) return false;
        String lowerPassword = password.toLowerCase();
        for (int i = 0; i <= lowerPassword.length() - repeatLength; i++) {
            boolean allSame = true;
            char first = lowerPassword.charAt(i);
            for (int j = 1; j < repeatLength; j++) {
                if (lowerPassword.charAt(i + j) != first) {
                    allSame = false;
                    break;
                }
            }
            if (allSame) return true;
        }
        return false;
    }

    // ─────────────────────────────────────────────────────────────
    // Buttons
    // ─────────────────────────────────────────────────────────────
    private void setupButtons() {
        clearFormBtn.setOnClickListener(v -> clearForm());
        submitBtn.setOnClickListener(v -> {
            if (isValidFormData()) {
                submitUser();
            }
        });
    }

    private void clearForm() {
        editTextFirstName.setText("");
        editTextLastName.setText("");
        editTextEmail.setText("");
        editTextPassword.setText("");
        editTextConfirmPassword.setText("");
        spinnerRole.setText("", false);
        spinnerStatus.setText("", false);
        selectedRoleId = null;

        textInputLayoutPassword.setError(null);
        textInputLayoutConfirmPassword.setError(null);
        setPasswordBoxColor(colorDefault);
        resetConfirmPasswordBox();
        passwordRulesContainer.setVisibility(View.GONE);
    }

    // ─────────────────────────────────────────────────────────────
    // Full Form Validation (all fields mandatory + all password rules)
    // ─────────────────────────────────────────────────────────────
    private boolean isValidFormData() {
        String firstName = Objects.requireNonNull(editTextFirstName.getText()).toString().trim();
        String lastName = Objects.requireNonNull(editTextLastName.getText()).toString().trim();
        String email = Objects.requireNonNull(editTextEmail.getText()).toString().trim();
        String password = Objects.requireNonNull(editTextPassword.getText()).toString().trim();
        String confirmPassword = Objects.requireNonNull(editTextConfirmPassword.getText()).toString().trim();
        String role = spinnerRole.getText() != null ? spinnerRole.getText().toString().trim() : "";
        String status = spinnerStatus.getText() != null ? spinnerStatus.getText().toString().trim() : "";

        textInputLayoutPassword.setError(null);
        textInputLayoutConfirmPassword.setError(null);

        // --- Mandatory field checks ---
        if (firstName.isEmpty()) {
            showToast("Please enter first name.");
            return false;
        }
        if (lastName.isEmpty()) {
            showToast("Please enter last name.");
            return false;
        }
        if (email.isEmpty()) {
            showToast("Please enter email address.");
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast("Please enter a valid email address.");
            return false;
        }
        if (role.isEmpty() || selectedRoleId == null) {
            showToast("Please select a role.");
            return false;
        }
        if (status.isEmpty()) {
            showToast("Please select a status.");
            return false;
        }

        // --- Password mandatory ---
        if (password.isEmpty()) {
            textInputLayoutPassword.setError("Please enter a password");
            setPasswordBoxColor(colorRed);
            return false;
        }

        // --- Password rule checks ---
        if (password.length() < 16) {
            textInputLayoutPassword.setError("Password must be at least 16 characters");
            setPasswordBoxColor(colorRed);
            return false;
        }
        if (password.length() > 18) {
            textInputLayoutPassword.setError("Password cannot exceed 18 characters");
            setPasswordBoxColor(colorRed);
            return false;
        }
        if (!password.matches(".*[A-Z].*")) {
            textInputLayoutPassword.setError("Password must contain at least one uppercase letter");
            setPasswordBoxColor(colorRed);
            return false;
        }
        if (!password.matches(".*[a-z].*")) {
            textInputLayoutPassword.setError("Password must contain at least one lowercase letter");
            setPasswordBoxColor(colorRed);
            return false;
        }
        if (!password.matches(".*[0-9].*")) {
            textInputLayoutPassword.setError("Password must contain at least one number");
            setPasswordBoxColor(colorRed);
            return false;
        }
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|.<>/?`~].*")) {
            textInputLayoutPassword.setError("Password must contain at least one special character");
            setPasswordBoxColor(colorRed);
            return false;
        }
        if (password.contains(" ") || password.contains(",")) {
            textInputLayoutPassword.setError("Password must not contain spaces or commas");
            setPasswordBoxColor(colorRed);
            return false;
        }

        String lowerFirst = firstName.toLowerCase();
        String lowerLast = lastName.toLowerCase();
        if (!lowerFirst.isEmpty() && password.toLowerCase().contains(lowerFirst)) {
            textInputLayoutPassword.setError("Password must not contain your first name");
            setPasswordBoxColor(colorRed);
            return false;
        }
        if (!lowerLast.isEmpty() && password.toLowerCase().contains(lowerLast)) {
            textInputLayoutPassword.setError("Password must not contain your last name");
            setPasswordBoxColor(colorRed);
            return false;
        }
        if (hasSequentialChars(password, 4)) {
            textInputLayoutPassword.setError("Password must not contain sequential characters (e.g., 1234, abcd)");
            setPasswordBoxColor(colorRed);
            return false;
        }
        if (hasRepeatedChars(password, 4)) {
            textInputLayoutPassword.setError("Password must not contain repeated characters (e.g., aaaa, 1111)");
            setPasswordBoxColor(colorRed);
            return false;
        }

        // --- Confirm password ---
        if (confirmPassword.isEmpty()) {
            textInputLayoutConfirmPassword.setError("Please confirm your password");
            textInputLayoutConfirmPassword.setBoxStrokeColor(colorRed);
            textInputLayoutConfirmPassword.setHintTextColor(ColorStateList.valueOf(colorRed));
            return false;
        }
        if (!password.equals(confirmPassword)) {
            textInputLayoutConfirmPassword.setError("Passwords do not match");
            textInputLayoutConfirmPassword.setBoxStrokeColor(colorRed);
            textInputLayoutConfirmPassword.setHintTextColor(ColorStateList.valueOf(colorRed));
            return false;
        }

        return true;
    }

    // ─────────────────────────────────────────────────────────────
    // Submit User
    // ─────────────────────────────────────────────────────────────
    private void submitUser() {
        RegisterUserRequestBody body = new RegisterUserRequestBody();
        body.setFirstname(Objects.requireNonNull(editTextFirstName.getText()).toString().trim());
        body.setLastname(Objects.requireNonNull(editTextLastName.getText()).toString().trim());
        body.setEmail(Objects.requireNonNull(editTextEmail.getText()).toString().trim());
        body.setPassword(Objects.requireNonNull(editTextPassword.getText()).toString().trim());
        body.setConfirmPassword(Objects.requireNonNull(editTextConfirmPassword.getText()).toString().trim());
        body.setRole(selectedRoleId);

        String status = spinnerStatus.getText() != null ? spinnerStatus.getText().toString().trim() : "active";
        body.setActive("active".equalsIgnoreCase(status));

//        body.setCompany("");
//        body.setBranch("");

        mViewModel.registerUser("jwt " + token, body);
    }

    // ─────────────────────────────────────────────────────────────
    // Observe ViewModel
    // ─────────────────────────────────────────────────────────────
    private void observeViewModel() {
        mViewModel.getRoles().observe(getViewLifecycleOwner(), roles -> {
            if (roles != null && !roles.isEmpty()) {
                populateRoleDropdown(roles);
            }
        });

        mViewModel.getLoadingStatus().observe(getViewLifecycleOwner(), loading -> {
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
            submitBtn.setEnabled(!loading);
            clearFormBtn.setEnabled(!loading);
        });

        mViewModel.getSuccessMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null) {
                new CustomAlertDialog(requireContext()).setType(CustomAlertDialog.Type.SUCCESS).setTitle("Success").setMessage(message).setPositiveButton("OK", () -> {
                    if (isAdded() && getView() != null) {
                        Navigation.findNavController(requireView()).popBackStack();
                    }
                }).show();
            }
        });

        mViewModel.getErrorMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null) {
                new CustomAlertDialog(requireContext()).setType(CustomAlertDialog.Type.ERROR).setTitle("Error").setMessage(message).setPositiveButton("OK", null).show();
            }
        });
    }

    private void showToast(String message) {
        if (isAdded() && getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}
package app.xedigital.ai.ui.profile;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

import app.xedigital.ai.R;
import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.api.APIInterface;
import app.xedigital.ai.databinding.FragmentEditProfileBinding;
import app.xedigital.ai.model.user.UserModelResponse;
import app.xedigital.ai.utills.SecurePrefManager;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileFragment extends Fragment {

    private FragmentEditProfileBinding binding;
    private String authToken, userId;
    private APIInterface apiService;
    private String companyId, branchId, roleId;

    // Colors for box states
    private int colorRed;
    private int colorGreen;
    private int colorDefault;

    public EditProfileFragment() {
        // Required empty public constructor
    }

    // ─────────────────────────────────────────────────────────────
    // STEP 1 : Fragment View Creation
    // ─────────────────────────────────────────────────────────────
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentEditProfileBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Initialize colors
        colorRed = ContextCompat.getColor(requireContext(), R.color.rejected_color);
        colorGreen = ContextCompat.getColor(requireContext(), R.color.approved_color);
        colorDefault = ContextCompat.getColor(requireContext(), R.color.accent_employee);

        // Initialize API service
        apiService = APIClient.getInstance().getUser();

        // Retrieve stored auth credentials
        SecurePrefManager prefManager = SecurePrefManager.getInstance(requireContext());
        authToken = prefManager.getString("authToken", "");
        userId = prefManager.getString("userId", "");

        // Fetch and populate user profile from server
        fetchUserProfile(userId, authToken);

        // Lock display-only fields
        binding.editTextFirstName.setEnabled(false);
        binding.editTextLastName.setEnabled(false);
        binding.editTextEmail.setEnabled(false);
        binding.spinnerRole.setEnabled(false);
        binding.spinnerStatus.setEnabled(false);

        // Real-time password checker
        setupPasswordWatcher();

        // Real-time confirm password checker
        setupConfirmPasswordWatcher();

        // Update button click
        binding.buttonUpdate.setOnClickListener(v -> {
            if (isValidFormData()) {
                processUpdate();
            }
        });

        // Clear button click
        binding.clearForm.setOnClickListener(v -> clearForm());

        return view;
    }

    // ─────────────────────────────────────────────────────────────
    // STEP 2 : View Created — Apply Input Filters
    // ─────────────────────────────────────────────────────────────
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.editTextPassword.setFilters(
                new InputFilter[]{new InputFilter.LengthFilter(16)}
        );
        binding.editTextConfirmPassword.setFilters(
                new InputFilter[]{new InputFilter.LengthFilter(16)}
        );
    }

    // ─────────────────────────────────────────────────────────────
    // STEP 3 : Real-time Password Rule Watcher
    // ─────────────────────────────────────────────────────────────
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
                    // Also reset confirm box if password cleared
                    resetConfirmPasswordBox();
                    return;
                }

                binding.passwordRulesContainer.setVisibility(View.VISIBLE);

                String firstName = Objects.requireNonNull(
                        binding.editTextFirstName.getText()
                ).toString().trim().toLowerCase();

                String lastName = Objects.requireNonNull(
                        binding.editTextLastName.getText()
                ).toString().trim().toLowerCase();

                // Evaluate each rule
                boolean lengthOk = password.length() >= 12 && password.length() <= 16;
                boolean upperOk = password.matches(".*[A-Z].*");
                boolean lowerOk = password.matches(".*[a-z].*");
                boolean numberOk = password.matches(".*[0-9].*");
                boolean specialOk = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|.<>/?`~].*");
                boolean noSpaceCommaOk = !password.contains(" ") && !password.contains(",");

                boolean nameValid = true;
                if (!firstName.isEmpty() && password.toLowerCase().contains(firstName)) {
                    nameValid = false;
                }
                if (!lastName.isEmpty() && password.toLowerCase().contains(lastName)) {
                    nameValid = false;
                }

                boolean noSequentialOk = !hasSequentialChars(password, 4);
                boolean noRepeatedOk = !hasRepeatedChars(password, 4);

                // Update rule UI
                updateRule(binding.ruleLength, lengthOk);
                updateRule(binding.ruleUppercase, upperOk);
                updateRule(binding.ruleLowercase, lowerOk);
                updateRule(binding.ruleNumber, numberOk);
                updateRule(binding.ruleSpecial, specialOk);
                updateRule(binding.ruleNoSpaceComma, noSpaceCommaOk);
                updateRule(binding.ruleNoName, nameValid);
                updateRule(binding.ruleNoSequential, noSequentialOk);
                updateRule(binding.ruleNoRepeated, noRepeatedOk);

                // Check if ALL rules pass
                boolean allPassed = lengthOk && upperOk && lowerOk && numberOk
                        && specialOk && noSpaceCommaOk && nameValid
                        && noSequentialOk && noRepeatedOk;

                // Update password input box color
                if (allPassed) {
                    setPasswordBoxColor(colorGreen);
                } else {
                    setPasswordBoxColor(colorRed);
                }

                // Also re-check confirm password match in real time
                updateConfirmPasswordBoxColor();
            }
        });
    }

    // ─────────────────────────────────────────────────────────────
    // STEP 4 : Real-time Confirm Password Watcher
    // ─────────────────────────────────────────────────────────────
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
    // HELPER : Update Password Input Box Stroke Color
    // ─────────────────────────────────────────────────────────────
    private void setPasswordBoxColor(int color) {
        binding.textInputLayoutPassword.setBoxStrokeColor(color);
        binding.textInputLayoutPassword.setHintTextColor(
                ColorStateList.valueOf(color)
        );
    }

    // ─────────────────────────────────────────────────────────────
    // HELPER : Update Confirm Password Box Based on Match
    // ─────────────────────────────────────────────────────────────
    private void updateConfirmPasswordBoxColor() {
        String password = Objects.requireNonNull(
                binding.editTextPassword.getText()
        ).toString().trim();

        String confirmPassword = Objects.requireNonNull(
                binding.editTextConfirmPassword.getText()
        ).toString().trim();

        if (confirmPassword.isEmpty()) {
            resetConfirmPasswordBox();
            return;
        }

        if (password.equals(confirmPassword)) {
            // Passwords match — green box
            binding.textInputLayoutConfirmPassword.setBoxStrokeColor(colorGreen);
            binding.textInputLayoutConfirmPassword.setHintTextColor(
                    ColorStateList.valueOf(colorGreen)
            );
            binding.textInputLayoutConfirmPassword.setError(null);
        } else {
            // Passwords don't match — red box
            binding.textInputLayoutConfirmPassword.setBoxStrokeColor(colorRed);
            binding.textInputLayoutConfirmPassword.setHintTextColor(
                    ColorStateList.valueOf(colorRed)
            );
            binding.textInputLayoutConfirmPassword.setError("Passwords do not match");
        }
    }

    // ─────────────────────────────────────────────────────────────
    // HELPER : Reset Confirm Password Box to Default
    // ─────────────────────────────────────────────────────────────
    private void resetConfirmPasswordBox() {
        binding.textInputLayoutConfirmPassword.setBoxStrokeColor(colorDefault);
        binding.textInputLayoutConfirmPassword.setHintTextColor(
                ColorStateList.valueOf(colorDefault)
        );
        binding.textInputLayoutConfirmPassword.setError(null);
    }

    // ─────────────────────────────────────────────────────────────
    // HELPER : Update Individual Rule Row UI
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

    // ─────────────────────────────────────────────────────────────
    // HELPER : Convert dp to px
    // ─────────────────────────────────────────────────────────────
    private int dpToPx(int dp) {
        float density = requireContext().getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    // ─────────────────────────────────────────────────────────────
    // HELPER : Check Sequential Characters (1234, abcd, dcba)
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

                if (next - current != 1) {
                    isAscending = false;
                }
                if (current - next != 1) {
                    isDescending = false;
                }
            }

            if (isAscending || isDescending) {
                return true;
            }
        }

        return false;
    }

    // ─────────────────────────────────────────────────────────────
    // HELPER : Check Repeated Characters (aaaa, 1111)
    // ─────────────────────────────────────────────────────────────
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

            if (allSame) {
                return true;
            }
        }

        return false;
    }

    // ─────────────────────────────────────────────────────────────
    // STEP 5 : Fetch User Profile from Server
    // ─────────────────────────────────────────────────────────────
    private void fetchUserProfile(String userId, String authToken) {

        binding.progressBar.setVisibility(View.VISIBLE);

        Call<UserModelResponse> call = apiService.getUserData(userId, "jwt " + authToken);
        call.enqueue(new Callback<UserModelResponse>() {

            @Override
            public void onResponse(@NonNull Call<UserModelResponse> call,
                                   @NonNull Response<UserModelResponse> response) {

                if (binding == null || !isAdded()) return;

                binding.progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    UserModelResponse userModel = response.body();

                    if (userModel.getData() != null
                            && userModel.getData().getUser() != null) {

                        binding.editTextFirstName.setText(
                                userModel.getData().getUser().getFirstname()
                        );
                        binding.editTextLastName.setText(
                                userModel.getData().getUser().getLastname()
                        );
                        binding.editTextEmail.setText(
                                userModel.getData().getUser().getEmail()
                        );

                        if (userModel.getData().getRole() != null) {
                            binding.spinnerRole.setText(
                                    userModel.getData().getRole().getName()
                            );
                            roleId = userModel.getData().getRole().getId();
                        }

                        binding.spinnerStatus.setText(
                                userModel.getData().getUser().isActive()
                                        ? "Active"
                                        : "Inactive"
                        );

                        if (userModel.getData().getCompany() != null) {
                            companyId = userModel.getData().getCompany().getId();
                        }
                        if (userModel.getData().getBranch() != null) {
                            branchId = userModel.getData().getBranch().getId();
                        }

                    } else {
                        showEmptyDataAlert();
                    }

                } else {
                    showToast("Failed to fetch profile data.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserModelResponse> call,
                                  @NonNull Throwable t) {

                if (binding == null || !isAdded()) return;

                binding.progressBar.setVisibility(View.GONE);
                showToast("Network error. Please try again.");
            }
        });
    }

    // ─────────────────────────────────────────────────────────────
    // STEP 6 : Validate Form Before Submitting
    // ─────────────────────────────────────────────────────────────
    private boolean isValidFormData() {

        String password = Objects.requireNonNull(
                binding.editTextPassword.getText()
        ).toString().trim();

        String confirmPassword = Objects.requireNonNull(
                binding.editTextConfirmPassword.getText()
        ).toString().trim();

        String firstName = Objects.requireNonNull(
                binding.editTextFirstName.getText()
        ).toString().trim().toLowerCase();

        String lastName = Objects.requireNonNull(
                binding.editTextLastName.getText()
        ).toString().trim().toLowerCase();

        binding.textInputLayoutPassword.setError(null);
        binding.textInputLayoutConfirmPassword.setError(null);

        if (password.isEmpty() && !confirmPassword.isEmpty()) {
            binding.textInputLayoutPassword.setError(
                    "Please enter a password first"
            );
            setPasswordBoxColor(colorRed);
            return false;
        }

        if (password.isEmpty() && confirmPassword.isEmpty()) {
            return true;
        }

        if (password.length() < 12) {
            binding.textInputLayoutPassword.setError(
                    "Password must be at least 12 characters"
            );
            setPasswordBoxColor(colorRed);
            return false;
        }

        if (password.length() > 16) {
            binding.textInputLayoutPassword.setError(
                    "Password cannot exceed 16 characters"
            );
            setPasswordBoxColor(colorRed);
            return false;
        }

        if (!password.matches(".*[A-Z].*")) {
            binding.textInputLayoutPassword.setError(
                    "Password must contain at least one uppercase letter"
            );
            setPasswordBoxColor(colorRed);
            return false;
        }

        if (!password.matches(".*[a-z].*")) {
            binding.textInputLayoutPassword.setError(
                    "Password must contain at least one lowercase letter"
            );
            setPasswordBoxColor(colorRed);
            return false;
        }

        if (!password.matches(".*[0-9].*")) {
            binding.textInputLayoutPassword.setError(
                    "Password must contain at least one number"
            );
            setPasswordBoxColor(colorRed);
            return false;
        }

        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|.<>/?`~].*")) {
            binding.textInputLayoutPassword.setError(
                    "Password must contain at least one special character"
            );
            setPasswordBoxColor(colorRed);
            return false;
        }

        if (password.contains(" ") || password.contains(",")) {
            binding.textInputLayoutPassword.setError(
                    "Password must not contain spaces or commas"
            );
            setPasswordBoxColor(colorRed);
            return false;
        }

        if (!firstName.isEmpty() && password.toLowerCase().contains(firstName)) {
            binding.textInputLayoutPassword.setError(
                    "Password must not contain your first name"
            );
            setPasswordBoxColor(colorRed);
            return false;
        }

        if (!lastName.isEmpty() && password.toLowerCase().contains(lastName)) {
            binding.textInputLayoutPassword.setError(
                    "Password must not contain your last name"
            );
            setPasswordBoxColor(colorRed);
            return false;
        }

        if (hasSequentialChars(password, 4)) {
            binding.textInputLayoutPassword.setError(
                    "Password must not contain sequential characters (e.g., 1234, abcd)"
            );
            setPasswordBoxColor(colorRed);
            return false;
        }

        if (hasRepeatedChars(password, 4)) {
            binding.textInputLayoutPassword.setError(
                    "Password must not contain repeated characters (e.g., aaaa, 1111)"
            );
            setPasswordBoxColor(colorRed);
            return false;
        }

        if (confirmPassword.isEmpty()) {
            binding.textInputLayoutConfirmPassword.setError(
                    "Please confirm your password"
            );
            binding.textInputLayoutConfirmPassword.setBoxStrokeColor(colorRed);
            binding.textInputLayoutConfirmPassword.setHintTextColor(
                    ColorStateList.valueOf(colorRed)
            );
            return false;
        }

        if (!password.equals(confirmPassword)) {
            binding.textInputLayoutConfirmPassword.setError(
                    "Passwords do not match"
            );
            binding.textInputLayoutConfirmPassword.setBoxStrokeColor(colorRed);
            binding.textInputLayoutConfirmPassword.setHintTextColor(
                    ColorStateList.valueOf(colorRed)
            );
            return false;
        }

        return true;
    }

    // ─────────────────────────────────────────────────────────────
    // STEP 7 : Process and Submit Profile Update
    // ─────────────────────────────────────────────────────────────
    private void processUpdate() {

        String password = Objects.requireNonNull(
                binding.editTextPassword.getText()
        ).toString().trim();

        if (password.isEmpty()) {
            showToast("No modifications detected to save.");
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);

        try {
            JSONObject requestJson = new JSONObject();
            requestJson.put("company", companyId);
            requestJson.put("branch", branchId);
            requestJson.put("role", roleId);
            requestJson.put("firstname",
                    Objects.requireNonNull(
                            binding.editTextFirstName.getText()
                    ).toString()
            );
            requestJson.put("lastname",
                    Objects.requireNonNull(
                            binding.editTextLastName.getText()
                    ).toString()
            );
            requestJson.put("email",
                    Objects.requireNonNull(
                            binding.editTextEmail.getText()
                    ).toString()
            );

            String statusValue = binding.spinnerStatus.getText() != null
                    ? binding.spinnerStatus.getText().toString()
                    : "";
            requestJson.put("active", statusValue.equalsIgnoreCase("Active"));

            requestJson.put("password", password);
            requestJson.put("confirmPassword",
                    Objects.requireNonNull(
                            binding.editTextConfirmPassword.getText()
                    ).toString().trim()
            );

            RequestBody requestBody = RequestBody.create(
                    MediaType.parse("application/json; charset=utf-8"),
                    requestJson.toString()
            );

            Call<ResponseBody> call = apiService.editUserProfile(
                    userId, "jwt " + authToken, requestBody
            );

            call.enqueue(new Callback<ResponseBody>() {

                @Override
                public void onResponse(@NonNull Call<ResponseBody> call,
                                       @NonNull Response<ResponseBody> response) {

                    if (binding == null || !isAdded()) return;

                    binding.progressBar.setVisibility(View.GONE);

                    if (response.isSuccessful()) {
                        showToast("Profile updated successfully.");
                        clearForm();
                        navigateToProfileFragment();
                    } else {
                        showToast("Update failed. Please try again.");
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call,
                                      @NonNull Throwable t) {

                    if (binding == null || !isAdded()) return;

                    binding.progressBar.setVisibility(View.GONE);
                    showToast("Network error. Please try again.");
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
            binding.progressBar.setVisibility(View.GONE);
            showToast("Something went wrong. Please try again.");
        }
    }

    // ─────────────────────────────────────────────────────────────
    // HELPER : Show Alert When API Returns Empty User Data
    // ─────────────────────────────────────────────────────────────
    private void showEmptyDataAlert() {
        if (isAdded() && getContext() != null) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Data Sync Error")
                    .setMessage("User data is empty. Please try again later.")
                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                    .show();
        }
    }

    // ─────────────────────────────────────────────────────────────
    // HELPER : Clear Password Fields, Errors, Rules and Box Colors
    // ─────────────────────────────────────────────────────────────
    private void clearForm() {
        binding.editTextPassword.setText("");
        binding.editTextConfirmPassword.setText("");
        binding.textInputLayoutPassword.setError(null);
        binding.textInputLayoutConfirmPassword.setError(null);
        binding.passwordRulesContainer.setVisibility(View.GONE);

        // Reset box colors to default
        setPasswordBoxColor(colorDefault);
        resetConfirmPasswordBox();
    }

    // ─────────────────────────────────────────────────────────────
    // HELPER : Navigate Back to Profile Fragment
    // ─────────────────────────────────────────────────────────────
    private void navigateToProfileFragment() {
        if (isAdded() && getView() != null) {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_nav_edit_profile_to_nav_profile);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // HELPER : Safe Toast
    // ─────────────────────────────────────────────────────────────
    private void showToast(String message) {
        if (isAdded() && getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    // ─────────────────────────────────────────────────────────────
    // LIFECYCLE : Clean up binding
    // ─────────────────────────────────────────────────────────────
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
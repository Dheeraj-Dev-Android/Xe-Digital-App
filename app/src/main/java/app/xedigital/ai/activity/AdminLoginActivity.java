package app.xedigital.ai.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

import app.xedigital.ai.AdminMainActivity;
import app.xedigital.ai.R;
import app.xedigital.ai.adminApi.AdminAPIClient;
import app.xedigital.ai.adminApi.AdminAPIInterface;
import app.xedigital.ai.model.Admin.UserDetails.Role;
import app.xedigital.ai.model.Admin.UserDetails.UserDetailsResponse;
import app.xedigital.ai.model.login.LoginModelResponse;
import app.xedigital.ai.utills.BlurUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminLoginActivity extends AppCompatActivity {

    private static final String TAG = "AdminLogin";
    private static final int MAX_VERIFY_RETRIES = 3;

    private TextInputEditText emailEditText, passwordEditText;
    private MaterialButton loginButton;
    private FrameLayout loadingOverlay;
    private boolean isEmployee;
    private MaterialCheckBox rememberMeCheckBox;

    // Tracks how many times we've retried role verification for the current login attempt
    private int verifyRetryCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_admin_login);

        isEmployee = getIntent().getBooleanExtra("isEmployee", false);

        emailEditText = findViewById(R.id.edit_email);
        passwordEditText = findViewById(R.id.edit_password);
        loginButton = findViewById(R.id.btn_signin);
        loadingOverlay = findViewById(R.id.loadingOverlay);
        rememberMeCheckBox = findViewById(R.id.cb_remember_me);

        SharedPreferences pref = getSharedPreferences("AdminCred", MODE_PRIVATE);
        boolean isRemembered = pref.getBoolean("remember_me", false);

        if (isRemembered) {
            String savedEmail = pref.getString("saved_email", "");
            String savedPass = pref.getString("saved_password", "");

            if (!savedEmail.isEmpty() && !savedPass.isEmpty()) {
                emailEditText.setText(savedEmail);
                passwordEditText.setText(savedPass);
                rememberMeCheckBox.setChecked(true);
                performLogin();
            }
        }

        loginButton.setOnClickListener(v -> performLogin());
    }

    private void performLogin() {
        String email = Objects.requireNonNull(emailEditText.getText()).toString().trim();
        String password = Objects.requireNonNull(passwordEditText.getText()).toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        verifyRetryCount = 0; // reset retry counter for a fresh login attempt
        showLoading(true);

        Call<LoginModelResponse> call = AdminAPIClient.getInstance().getBase2().loginApi1(email, password);
        call.enqueue(new Callback<LoginModelResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginModelResponse> call, @NonNull Response<LoginModelResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginModelResponse loginResponse = response.body();

                    if (loginResponse.isSuccess() && loginResponse.getData() != null && loginResponse.getData().getUser() != null) {
                        handleRememberMe(email, password);
                        String userId = loginResponse.getData().getUser().getId();
                        String authToken = loginResponse.getData().getToken();
                        String empEmail = loginResponse.getData().getUser().getEmail();
                        String empFirstName = loginResponse.getData().getUser().getFirstname();

                        GetEmployee(authToken, userId, empEmail, empFirstName);
                    } else {
                        showLoading(false);
                        showAlertDialog(loginResponse.getMessage() != null ? loginResponse.getMessage() : "Failed to validate credentials payload layout.");
                    }
                } else {
                    showLoading(false);
                    Toast.makeText(AdminLoginActivity.this, "Invalid credentials or Server error response", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginModelResponse> call, @NonNull Throwable t) {
                showLoading(false);
                Toast.makeText(AdminLoginActivity.this, "Login failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Fetches the user's role/profile to authorize dashboard access.
     * <p>
     * SECURITY: This method must FAIL CLOSED. If we cannot positively confirm
     * the user's role is branchadmin/humanresource, we must NOT grant access —
     * regardless of whether the failure was due to network issues, server errors,
     * or malformed responses. A previous version of this code fell back to
     * granting access on failure, which allowed an "employee" role (or any
     * unverified user) to slip into the admin dashboard during a network hiccup.
     */
    private void GetEmployee(String authToken, String userId, String empEmail, String empFirstName) {
        String token = "jwt " + authToken;
        AdminAPIInterface employeeDetails = AdminAPIClient.getInstance().getBase2();
        Call<UserDetailsResponse> employees = employeeDetails.getUser(token, userId);

        employees.enqueue(new Callback<UserDetailsResponse>() {
            @Override
            public void onResponse(@NonNull Call<UserDetailsResponse> call, @NonNull Response<UserDetailsResponse> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    UserDetailsResponse userDetailResponse = response.body();

                    if (userDetailResponse.isSuccess() && userDetailResponse.getData() != null) {
                        Role employeeRole = userDetailResponse.getData().getRole();
                        String currentCompany = (userDetailResponse.getData().getCompany() != null) ? userDetailResponse.getData().getCompany().getCollectionName() : "";

                        if (employeeRole != null) {
                            String roleName = employeeRole.getName();

                            if ("branchadmin".equalsIgnoreCase(roleName) || "humanresource".equalsIgnoreCase(roleName)) {
                                storeInSharedPreferences(userId, authToken, empEmail, empFirstName, currentCompany, false);
                                Intent intent = new Intent(AdminLoginActivity.this, AdminMainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                                Toast.makeText(AdminLoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                            } else if ("employee".equalsIgnoreCase(roleName)) {
                                showAlertDialogWithLogout();
                            } else {
                                Toast.makeText(AdminLoginActivity.this, "Unrecognized role: " + roleName, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Role missing from an otherwise successful response — treat as unverifiable, fail closed.
                            showRoleVerificationFailedDialog(authToken, userId, empEmail, empFirstName,
                                    "Your account role could not be determined. Please contact support or try again.");
                        }
                    } else {
                        // Response succeeded but payload signals failure — fail closed.
                        showRoleVerificationFailedDialog(authToken, userId, empEmail, empFirstName,
                                "We couldn't verify your account details. Please try again.");
                    }
                } else {
                    // Server returned an error status — fail closed, do NOT auto-login.
                    Log.e(TAG, "Server profile fetch failed with HTTP error. Blocking access until role is verified.");
                    showRoleVerificationFailedDialog(authToken, userId, empEmail, empFirstName,
                            "We couldn't verify your account role due to a server error. Please try again.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserDetailsResponse> call, @NonNull Throwable throwable) {
                showLoading(false);
                // Network/connection failure — fail closed, do NOT auto-login.
                Log.e(TAG, "Network error while verifying role: " + throwable.getMessage());
                showRoleVerificationFailedDialog(authToken, userId, empEmail, empFirstName,
                        "Network error. We couldn't verify your account role. Please check your connection and try again.");
            }
        });
    }

    /**
     * Shows a fail-closed error dialog when role verification could not be completed.
     * Offers a Retry option (up to MAX_VERIFY_RETRIES) instead of silently granting access.
     */
    private void showRoleVerificationFailedDialog(String authToken, String userId, String empEmail, String empFirstName, String message) {
        verifyRetryCount++;

        if (verifyRetryCount > MAX_VERIFY_RETRIES) {
            showStyledDialog(
                    "Verification Failed",
                    "We're unable to verify your account after multiple attempts. Please try logging in again later.",
                    R.drawable.ic_error_outline,
                    R.color.error_bg,
                    R.color.error_tint,
                    true,
                    null // no retry — force user to restart login flow
            );
            return;
        }

        showStyledDialog(
                "Verification Failed",
                message,
                R.drawable.ic_error_outline,
                R.color.error_bg,
                R.color.error_tint,
                true,
                () -> {
                    showLoading(true);
                    GetEmployee(authToken, userId, empEmail, empFirstName);
                }
        );
    }

    private void handleRememberMe(String email, String password) {
        SharedPreferences preferences = getSharedPreferences("AdminCred", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        if (rememberMeCheckBox.isChecked()) {
            editor.putString("saved_email", email);
            editor.putString("saved_password", password);
            editor.putBoolean("remember_me", true);
        } else {
            editor.remove("saved_email");
            editor.remove("saved_password");
            editor.putBoolean("remember_me", false);
        }
        editor.apply();
    }

    /**
     * Single-button styled dialog (backward-compatible convenience overload).
     */
    private void showStyledDialog(String title, String message, int iconRes,
                                  int iconBgColorRes, int iconTintColorRes,
                                  boolean cancelable) {
        showStyledDialog(title, message, iconRes, iconBgColorRes, iconTintColorRes, cancelable, null);
    }

    /**
     * Full styled dialog builder. Shows a blurred screenshot of the current screen
     * as backdrop, with an icon badge, title, message, and either:
     * - a single "OK" button (onRetry == null), or
     * - a "Cancel" / "Retry" button pair (onRetry != null)
     */
    private void showStyledDialog(String title, String message, int iconRes,
                                  int iconBgColorRes, int iconTintColorRes,
                                  boolean cancelable, Runnable onRetry) {
        if (isFinishing() || isDestroyed()) return;

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_custom_alert, null);

        ImageView blurBackground = dialogView.findViewById(R.id.blurBackgroundImage);
        ImageView icon = dialogView.findViewById(R.id.dialogIcon);
        View iconBackground = dialogView.findViewById(R.id.iconBackground);
        TextView titleView = dialogView.findViewById(R.id.dialogTitle);
        TextView messageView = dialogView.findViewById(R.id.dialogMessage);
        MaterialButton positiveButton = dialogView.findViewById(R.id.dialogPositiveButton);
        MaterialButton negativeButton = dialogView.findViewById(R.id.dialogNegativeButton);

        // --- Capture + blur the current screen and set as backdrop ---
        Bitmap blurredBitmap = BlurUtils.getBlurredScreenshot(this, 20);
        if (blurredBitmap != null) {
            blurBackground.setImageBitmap(blurredBitmap);
        }

        icon.setImageResource(iconRes);
        iconBackground.setBackgroundTintList(
                ColorStateList.valueOf(ContextCompat.getColor(this, iconBgColorRes)));
        icon.setImageTintList(
                ColorStateList.valueOf(ContextCompat.getColor(this, iconTintColorRes)));

        titleView.setText(title);
        messageView.setText(message);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(cancelable)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            );
            dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        }

        if (onRetry != null) {
            // Two-button mode: Cancel + Retry
            negativeButton.setVisibility(View.VISIBLE);
            negativeButton.setText("Cancel");
            positiveButton.setText("Retry");

            negativeButton.setOnClickListener(v -> dialog.dismiss());
            positiveButton.setOnClickListener(v -> {
                dialog.dismiss();
                onRetry.run();
            });
        } else {
            // Single-button mode: just OK
            negativeButton.setVisibility(View.GONE);
            positiveButton.setText("OK");
            positiveButton.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();
    }

    private void showAlertDialogWithLogout() {
        showStyledDialog(
                "Access Denied",
                "Please login with Admin or HR credentials",
                R.drawable.ic_warning_outline,
                R.color.warning_bg,
                R.color.warning_tint,
                false
        );
    }

    private void showAlertDialog(String message) {
        showStyledDialog(
                "Login Failed",
                message,
                R.drawable.ic_error_outline,
                R.color.error_bg,
                R.color.error_tint,
                true
        );
    }

    // ================================================================

    private void showLoading(boolean show) {
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void storeInSharedPreferences(String userId, String authToken, String empEmail, String empFirstName, String collectionName, boolean isEmployee) {
        SharedPreferences sharedPreferences = getSharedPreferences("AdminCred", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("userId", userId);
        editor.putString("authToken", authToken);
        editor.putString("empEmail", empEmail);
        editor.putString("empFirstName", empFirstName);
        editor.putBoolean("isEmployee", isEmployee);
        editor.putString("collectionName", collectionName);
        editor.apply();
    }
}
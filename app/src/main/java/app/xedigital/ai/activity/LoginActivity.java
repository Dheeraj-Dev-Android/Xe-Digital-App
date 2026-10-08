package app.xedigital.ai.activity;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.bumptech.glide.Glide;

import java.util.Objects;

import app.xedigital.ai.AdminMainActivity;
import app.xedigital.ai.R;
import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.databinding.ActivityLoginBinding;
import app.xedigital.ai.model.login.LoginModelResponse;
import app.xedigital.ai.model.user.UserModelResponse;
import app.xedigital.ai.utills.PermissionManager;
import app.xedigital.ai.utills.RoleAccessManager;
import app.xedigital.ai.utills.RoleGroup;
import app.xedigital.ai.utills.SecurePrefManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private ActivityLoginBinding binding;
    private View loadingOverlay;
    private boolean isRedirectInProgress = false;
    private boolean isCheckingPermissions = false;
    private PermissionManager permissionManager;
    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                permissionManager.syncAllPermissions();
                isCheckingPermissions = false;
                proceedToAuthCheck();
            });
    private AlertDialog batteryDialog;
    private AlertDialog infoDialog;
    private final ActivityResultLauncher<String[]> foregroundPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                permissionManager.syncAllPermissions();
                boolean locationGranted = permissionManager.isGranted(PermissionManager.TAG_LOCATION);

                SecurePrefManager prefManager = SecurePrefManager.getInstance(this);
                String cachedToken = prefManager.getString("cachedTokenPermission", null);

                if (locationGranted) {
                    if (cachedToken != null) {
                        proceedToRoleBasedNavigation(cachedToken);
                    }
                } else {
                    showLoginScreen();
                    showAlertDialog("Foreground location permission is required for shift tracking.");
                }
            });

    @Override
    protected void onResume() {
        super.onResume();
        if (!isCheckingPermissions) {
            isRedirectInProgress = false;
            evaluateSessionWorkflow();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        loadingOverlay = binding.loadingOverlay;

        permissionManager = PermissionManager.getInstance(this);

        hideLoginScreen();
        Glide.with(this).load(R.mipmap.ic_launcher).into(binding.logoImage);

        binding.btnSignIn.setOnClickListener(v -> {
            String email = Objects.requireNonNull(binding.editEmail.getText()).toString().trim();
            String password = Objects.requireNonNull(binding.editPassword.getText()).toString().trim();
            if (email.isEmpty() || password.isEmpty()) {
                if (email.isEmpty()) binding.editEmail.setError("Email Required");
                if (password.isEmpty()) binding.editPassword.setError("Password Required");
            } else {
                callLoginApi(email, password, true);
            }
        });
    }

    private void evaluateSessionWorkflow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!permissionManager.isGranted(PermissionManager.TAG_NOTIFICATION)) {
                isCheckingPermissions = true;
                String notifPerm = permissionManager.getManifestPermission(PermissionManager.TAG_NOTIFICATION);
                if (notifPerm != null) {
                    notificationPermissionLauncher.launch(notifPerm);
                }
                return;
            }
        }
        proceedToAuthCheck();
    }

    private void proceedToAuthCheck() {
        SecurePrefManager prefManager = SecurePrefManager.getInstance(this);
        String authToken = prefManager.getString("authToken", null);
        boolean isFallback = getIntent().getBooleanExtra("isFallback", false);

        if (isFallback) {
            prefManager.remove("authToken");
            prefManager.remove("cachedTokenPermission");
            showLoginScreen();
            return;
        }

        if (authToken != null) {
            silentlyRefreshToken();
        } else {
            showLoginScreen();
        }
    }

    private void silentlyRefreshToken() {
        SecurePrefManager prefManager = SecurePrefManager.getInstance(this);
        String savedEmail = prefManager.getString("emailId", null);
        String savedPassword = prefManager.getString("userPassword", null);

        if (savedEmail == null || savedPassword == null) {
            clearSessionAndShowLogin();
            return;
        }

        Log.d(TAG, "Silently refreshing token...");
        showLoading(true);
        callLoginApi(savedEmail, savedPassword, false);
    }

    private void callLoginApi(String email, String password, boolean isManualLogin) {
        if (isManualLogin) showLoading(true);

        Call<LoginModelResponse> call = APIClient.getInstance().getLogin().loginApi1(email, password);
        call.enqueue(new Callback<LoginModelResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginModelResponse> call, @NonNull Response<LoginModelResponse> response) {
                if (isFinishing() || isDestroyed()) return;
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    LoginModelResponse loginResponse = response.body();

                    if (loginResponse.isSuccess()) {
                        String userId = loginResponse.getData().getUser().getId();
                        String token = loginResponse.getData().getToken();
                        String emailId = loginResponse.getData().getUser().getEmail();

                        storeInSecurePrefs(userId, emailId, password, token);

                        if (isManualLogin) {
                            fetchAndSaveUserData(userId, token);
                        } else {
                            checkPermissionsAndNavigate(token);
                        }
                    } else {
                        if (isManualLogin) {
                            showAlertDialog(loginResponse.getMessage());
                        } else {
                            Log.e(TAG, "Silent refresh failed: " + loginResponse.getMessage());
                            clearSessionAndShowLogin();
                        }
                    }
                } else {
                    if (isManualLogin) {
                        showAlertDialog("Invalid Credentials");
                    } else {
                        Log.e(TAG, "Silent refresh bad response: " + response.code());
                        clearSessionAndShowLogin();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginModelResponse> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                showLoading(false);

                if (isManualLogin) {
                    showAlertDialog(t.getMessage());
                } else {
                    clearSessionAndShowLogin();
                }
            }
        });
    }

    private void fetchAndSaveUserData(String userId, String authToken) {
        showLoading(true);
        String authHeaderValue = "jwt " + authToken;

        Call<UserModelResponse> userCall = APIClient.getInstance().getUser().getUserData(userId, authHeaderValue);
        userCall.enqueue(new Callback<UserModelResponse>() {
            @Override
            public void onResponse(@NonNull Call<UserModelResponse> call, @NonNull Response<UserModelResponse> response) {
                if (isFinishing() || isDestroyed()) return;
                showLoading(false);

                if (response.isSuccessful() && response.body() != null
                        && response.body().isSuccess() && response.body().getData() != null) {

                    app.xedigital.ai.model.user.Data data = response.body().getData();

                    // ✅ Cache role + permissions + branch toggles
                    RoleAccessManager.getInstance(LoginActivity.this).applyUserData(data);

                    if (data.getCompany() != null) {
                        String collectionName = data.getCompany().getCollectionName();
                        SecurePrefManager.getInstance(LoginActivity.this).putString("collection", collectionName);
                    }
                } else {
                    Log.e(TAG, "User data fetch failed: "
                            + (response.body() != null ? response.body().getMessage() : response.code()));
                }

                checkPermissionsAndNavigate(authToken);
            }

            @Override
            public void onFailure(@NonNull Call<UserModelResponse> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                showLoading(false);
                checkPermissionsAndNavigate(authToken);
            }
        });
    }

    private void storeInSecurePrefs(String userId, String emailId, String password, String authToken) {
        SecurePrefManager prefManager = SecurePrefManager.getInstance(this);
        prefManager.putString("userId", userId);
        prefManager.putString("emailId", emailId);
        prefManager.putString("userPassword", password);
        prefManager.putString("authToken", authToken);
    }

    private void clearSessionAndShowLogin() {
        runOnUiThread(() -> {
            if (isFinishing() || isDestroyed()) return;
            showLoading(false);

            SecurePrefManager prefManager = SecurePrefManager.getInstance(this);
            prefManager.remove("authToken");
            prefManager.remove("cachedTokenPermission");

            showLoginScreen();
        });
    }

    private void checkPermissionsAndNavigate(String token) {
        SecurePrefManager.getInstance(this).putString("cachedTokenPermission", token);

        // ✅ Admin tier should NEVER have landed here, but safety-guard
        String role = RoleAccessManager.getInstance(this).getRoleName();
        if (RoleGroup.isAdminTier(role)) {
            Intent intent = new Intent(this, AdminMainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        // Employee requires location permission
        if (!permissionManager.isGranted(PermissionManager.TAG_LOCATION)) {
            foregroundPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
            return;
        }
        proceedToRoleBasedNavigation(token);
    }

    private void proceedToRoleBasedNavigation(String token) {
        if (isFinishing() || isDestroyed()) return;
        navigateToFaceLogin(token);
    }

    private void navigateToFaceLogin(String token) {
        if (isFinishing() || isDestroyed()) return;
        isRedirectInProgress = true;
        Intent intent = new Intent(this, FaceLoginActivity.class);
        intent.putExtra("authToken", token);
        startActivity(intent);
        finish();
    }

    // ─── UI Helpers ────────────────────────────────────────────────

    private void showLoginScreen() {
        binding.layoutEmail.setVisibility(View.VISIBLE);
        binding.layoutPassword.setVisibility(View.VISIBLE);
        binding.btnSignIn.setVisibility(View.VISIBLE);
        binding.logoCard.setVisibility(View.VISIBLE);
    }

    private void hideLoginScreen() {
        binding.layoutEmail.setVisibility(View.GONE);
        binding.layoutPassword.setVisibility(View.GONE);
        binding.btnSignIn.setVisibility(View.GONE);
        binding.logoCard.setVisibility(View.INVISIBLE);
    }

    private void showLoading(boolean show) {
        if (loadingOverlay != null) loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showAlertDialog(String message) {
        if (isFinishing() || isDestroyed()) return;
        if (infoDialog != null && infoDialog.isShowing()) infoDialog.dismiss();
        infoDialog = new AlertDialog.Builder(this)
                .setTitle("Login Info")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .create();
        infoDialog.show();
    }

    @Override
    protected void onDestroy() {
        if (batteryDialog != null && batteryDialog.isShowing()) batteryDialog.dismiss();
        if (infoDialog != null && infoDialog.isShowing()) infoDialog.dismiss();
        super.onDestroy();
    }
}
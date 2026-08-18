package app.xedigital.ai.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;

import java.util.Objects;

import app.xedigital.ai.R;
import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.databinding.ActivityLoginBinding;
import app.xedigital.ai.model.login.LoginModelResponse;
import app.xedigital.ai.model.user.UserModelResponse;
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
    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                isCheckingPermissions = false;
                proceedToAuthCheck();
            });
    private AlertDialog batteryDialog;
    private AlertDialog infoDialog;
    // ─── Session Workflow ─────────────────────────────────────────────────────
    private final ActivityResultLauncher<String[]> foregroundPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                boolean fineGranted = Boolean.TRUE.equals(
                        result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false));
                boolean coarseGranted = Boolean.TRUE.equals(
                        result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false));

                SecurePrefManager prefManager = SecurePrefManager.getInstance(this);
                String cachedToken = prefManager.getString("cachedTokenPermission", null);

                if (fineGranted || coarseGranted) {
                    if (cachedToken != null) {
                        navigateToFaceLogin(cachedToken);
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

    // ─── Silent Token Refresh ─────────────────────────────────────────────────

    private void evaluateSessionWorkflow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                isCheckingPermissions = true;
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                return;
            }
        }
        proceedToAuthCheck();
    }

    // ─── Login API ────────────────────────────────────────────────────────────

    private void proceedToAuthCheck() {
        SecurePrefManager prefManager = SecurePrefManager.getInstance(this);
        String authToken = prefManager.getString("authToken", null);
        boolean isFallback = getIntent().getBooleanExtra("isFallback", false);

        if (isFallback) {
            // User was redirected from FaceLoginActivity
            // Clear tokens but keep credentials for future silent refresh
            prefManager.remove("authToken");
            prefManager.remove("cachedTokenPermission");
            showLoginScreen();
            return;
        }

        if (authToken != null) {
            // Token exists → silently refresh it
            silentlyRefreshToken();
        } else {
            showLoginScreen();
        }
    }

    // ─── Fetch & Save User Data (after manual login only) ────────────────────

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

    // ─── Store Credentials ────────────────────────────────────────────────────

    private void callLoginApi(String email, String password, boolean isManualLogin) {
        if (isManualLogin) showLoading(true);

        Call<LoginModelResponse> call = APIClient.getInstance().getLogin().loginApi1(email, password);
        call.enqueue(new Callback<LoginModelResponse>() {

            @Override
            public void onResponse(@NonNull Call<LoginModelResponse> call,
                                   @NonNull Response<LoginModelResponse> response) {
                if (isFinishing() || isDestroyed()) return;
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    LoginModelResponse loginResponse = response.body();

                    if (loginResponse.isSuccess()) {
                        String userId = loginResponse.getData().getUser().getId();
                        String token = loginResponse.getData().getToken();
                        String emailId = loginResponse.getData().getUser().getEmail();

                        // Save all credentials + new token
                        storeInSharedPreferences(userId, emailId, password, token);

                        if (isManualLogin) {
                            // Manual login → fetch user data first then navigate
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

    // ─── Session Cleanup ──────────────────────────────────────────────────────

    private void fetchAndSaveUserData(String userId, String authToken) {
        showLoading(true);

        String authHeaderValue = "jwt " + authToken;

        Call<UserModelResponse> userCall = APIClient.getInstance()
                .getUser()
                .getUserData(userId, authHeaderValue);

        userCall.enqueue(new Callback<UserModelResponse>() {

            @Override
            public void onResponse(@NonNull Call<UserModelResponse> call,
                                   @NonNull Response<UserModelResponse> response) {
                if (isFinishing() || isDestroyed()) return;
                showLoading(false);

                if (response.isSuccessful()
                        && response.body() != null
                        && response.body().isSuccess()
                        && response.body().getData() != null
                        && response.body().getData().getCompany() != null) {

                    String collectionName = response.body().getData().getCompany().getCollectionName();
                    SecurePrefManager.getInstance(LoginActivity.this)
                            .putString("collection", collectionName);

                    Log.d(TAG, "Collection name saved: " + collectionName);

                } else {
                    // Collection fetch failed but still proceed
                    // FaceLoginActivity will handle fallback
                    Log.e(TAG, "User data fetch failed: "
                            + (response.body() != null ? response.body().getMessage()
                            : response.code()));
                }

                // Always proceed to face login regardless
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

    // ─── Permission Handling ──────────────────────────────────────────────────

    private void storeInSharedPreferences(String userId, String emailId,
                                          String password, String authToken) {
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
        if (!hasForegroundLocationPermission()) {
            foregroundPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
            return;
        }
        navigateToFaceLogin(token);
    }

    private boolean hasForegroundLocationPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    // ─── Navigation ───────────────────────────────────────────────────────────

    private void navigateToFaceLogin(String token) {
        if (isFinishing() || isDestroyed()) return;
        isRedirectInProgress = true;
        Intent intent = new Intent(this, FaceLoginActivity.class);
        intent.putExtra("authToken", token);
        startActivity(intent);
        finish();
    }

    // ─── UI Helpers ───────────────────────────────────────────────────────────

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
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void showAlertDialog(String message) {
        if (isFinishing() || isDestroyed()) return;

        if (infoDialog != null && infoDialog.isShowing()) {
            infoDialog.dismiss();
        }

        infoDialog = new AlertDialog.Builder(this)
                .setTitle("Login Info")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .create();

        infoDialog.show();
    }

    // ─── Lifecycle ────────────────────────────────────────────────────────────

    @Override
    protected void onDestroy() {
        if (batteryDialog != null && batteryDialog.isShowing()) batteryDialog.dismiss();
        if (infoDialog != null && infoDialog.isShowing()) infoDialog.dismiss();
        super.onDestroy();
    }
}
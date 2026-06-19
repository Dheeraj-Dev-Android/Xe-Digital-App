package app.xedigital.ai.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Objects;

import app.xedigital.ai.MainActivity;
import app.xedigital.ai.R;
import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.databinding.ActivityLoginBinding;
import app.xedigital.ai.model.login.LoginModelResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private View loadingOverlay;
    private boolean isRedirectInProgress = false;

    private AlertDialog backgroundDialog;
    private AlertDialog batteryDialog;
    private AlertDialog infoDialog;

    // Launcher for Background Location (Android 10+)
    private final ActivityResultLauncher<String> backgroundPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                SharedPreferences prefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                String cachedToken = prefs.getString("cachedTokenPermission", null);

                if (isGranted) {
                    if (cachedToken != null) navigateToFaceLogin(cachedToken);
                } else {
                    showLoginScreen();
                    showAlertDialog("Shift tracking requires background location 'Allow all the time' to run properly when closed.");
                }
            });

    private void checkPermissionsAndNavigate(String token) {
        SharedPreferences prefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        prefs.edit().putString("cachedTokenPermission", token).apply();

        if (!hasForegroundLocationPermission()) {
            foregroundPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
            return;
        }
        navigateToFaceLogin(token);
//        if (hasBackgroundLocationPermission()) {
//            navigateToFaceLogin(token);
//        } else {
//            showBackgroundPermissionDialog(token);
//        }
    }    // Launcher for Notifications (Android 13+)
    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                // If granted, we can run evaluateSessionWorkflow to proceed.
                // If denied, we bypass evaluateSessionWorkflow directly to avoid the loop,
                // jumping straight to checking session credentials.
                if (isGranted) {
                    evaluateSessionWorkflow();
                } else {
                    proceedToAuthCheck();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        loadingOverlay = binding.loadingOverlay;

        hideLoginScreen();

        Glide.with(this).load(R.mipmap.ic_launcher)
                .into(binding.logoImage);

        binding.btnSignIn.setOnClickListener(v -> {
            String email = Objects.requireNonNull(binding.editEmail.getText()).toString().trim();
            String password = Objects.requireNonNull(binding.editPassword.getText()).toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                if (email.isEmpty()) binding.editEmail.setError("Email Required");
                if (password.isEmpty()) binding.editPassword.setError("Password Required");
            } else {
                callLoginApi(email, password);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        isRedirectInProgress = false;
        evaluateSessionWorkflow();
    }

    @Override
    protected void onDestroy() {
        if (backgroundDialog != null && backgroundDialog.isShowing()) {
            backgroundDialog.dismiss();
        }
        if (batteryDialog != null && batteryDialog.isShowing()) {
            batteryDialog.dismiss();
        }
        if (infoDialog != null && infoDialog.isShowing()) {
            infoDialog.dismiss();
        }
        super.onDestroy();
    }

    /**
     * Entry point for checking runtime configurations like Notifications.
     */
    private void evaluateSessionWorkflow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                return;
            }
        }

        // If notification permission is granted (or device is < Android 13), proceed with auth checks
        proceedToAuthCheck();
    }

    /**
     * Separated logic to process routing and credential authorization status.
     * This isolates auth checking from the notification launcher callbacks to prevent runtime StackOverflows.
     */
    private void proceedToAuthCheck() {
        SharedPreferences prefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String authToken = prefs.getString("authToken", null);
        String cachedToken = prefs.getString("cachedTokenPermission", null);
        boolean isFallback = getIntent().getBooleanExtra("isFallback", false);

        if (isFallback) {
            showLoginScreen();
            return;
        }

        if (cachedToken != null) {
            if (hasForegroundLocationPermission() && hasBackgroundLocationPermission()) {
                navigateToFaceLogin(cachedToken);
            } else {
                checkPermissionsAndNavigate(cachedToken);
            }
        } else if (authToken != null && !isRedirectInProgress) {
            checkPermissionsAndNavigate(authToken);
        } else {
            showLoginScreen();
        }
    }

    private void navigateToFaceLogin(String token) {
        if (isFinishing() || isDestroyed()) return;
        isRedirectInProgress = true;
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("authToken", token);
        startActivity(intent);
        finish();
    }

    private void showBackgroundPermissionDialog(String token) {
        if (isFinishing() || isDestroyed()) return;
        if (backgroundDialog != null && backgroundDialog.isShowing()) return;

        backgroundDialog = new MaterialAlertDialogBuilder(this)
                .setTitle("Background Location Required")
                .setMessage("To track your shift accurately while the app is closed, please change location access permissions to 'Allow all the time' in the system settings screen.")
                .setCancelable(false)
                .setPositiveButton("Configure", (dialog, which) -> {
                    dialog.dismiss();

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        backgroundPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
                    }
                })
                .setNegativeButton("Logout", (dialog, which) -> {
                    dialog.dismiss();
                    getSharedPreferences("MyPrefs", MODE_PRIVATE).edit().clear().apply();
                    showLoginScreen();
                })
                .create();

        backgroundDialog.show();
    }

    private void showLoginScreen() {
        binding.layoutEmail.setVisibility(View.VISIBLE);
        binding.layoutPassword.setVisibility(View.VISIBLE);
        binding.btnSignIn.setVisibility(View.VISIBLE);
        binding.logoCard.setVisibility(View.VISIBLE);
    }

    private void callLoginApi(String email, String password) {
        showLoading(true);
        Call<LoginModelResponse> call = APIClient.getInstance().getLogin().loginApi1(email, password);
        call.enqueue(new Callback<LoginModelResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginModelResponse> call, @NonNull Response<LoginModelResponse> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    LoginModelResponse loginResponse = response.body();
                    if (loginResponse.isSuccess()) {
                        String userId = loginResponse.getData().getUser().getId();
                        String token = loginResponse.getData().getToken();
                        String emailId = loginResponse.getData().getUser().getEmail();

                        storeInSharedPreferences(userId, emailId, token);
                        checkPermissionsAndNavigate(token);
                    } else {
                        showAlertDialog(loginResponse.getMessage());
                    }
                } else {
                    showAlertDialog("Invalid Credentials");
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginModelResponse> call, @NonNull Throwable t) {
                showLoading(false);
                showAlertDialog(t.getMessage());
            }
        });
    }

    private boolean hasForegroundLocationPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private final ActivityResultLauncher<String[]> foregroundPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                boolean fineGranted = Boolean.TRUE.equals(result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false));
                boolean coarseGranted = Boolean.TRUE.equals(result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false));

                SharedPreferences prefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                String cachedToken = prefs.getString("cachedTokenPermission", null);

                if (fineGranted || coarseGranted) {
                    if (cachedToken != null) {
                        checkPermissionsAndNavigate(cachedToken);
                    }
                } else {
                    showLoginScreen();
                    showAlertDialog("Foreground location permission is required for shift tracking.");
                }
            });

    private void hideLoginScreen() {
        binding.layoutEmail.setVisibility(View.GONE);
        binding.layoutPassword.setVisibility(View.GONE);
        binding.btnSignIn.setVisibility(View.GONE);
        binding.logoCard.setVisibility(View.INVISIBLE);
    }

    private boolean hasBackgroundLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }



    private void storeInSharedPreferences(String userId, String emailId, String authToken) {
        SharedPreferences.Editor editor = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).edit();
        editor.putString("userId", userId);
        editor.putString("emailId", emailId);
        editor.putString("authToken", authToken);
        editor.apply();
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

    private void showLoading(boolean show) {
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
}
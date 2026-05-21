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
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Objects;

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

    // Retain dialog references to cleanly dismiss them on destroy/navigation
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
                    // Inform them why they are seeing the settings dialog layout or notice block
                    showAlertDialog("Shift tracking requires background location 'Allow all the time' to run properly when closed.");
                }
            });    // Launcher for Notification Permission (Android 13+)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        loadingOverlay = binding.loadingOverlay;

        hideLoginScreen();
        checkBatteryOptimization();

        Glide.with(this).load(R.mipmap.ic_launcher)
                .apply(RequestOptions.bitmapTransform(new CircleCrop()))
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
    }    // Launcher for Foreground Location (Fine/Coarse)    private final ActivityResultLauncher<String> notificationPermissionLauncher =

    @Override
    protected void onResume() {
        super.onResume();
        isRedirectInProgress = false;
        evaluateSessionWorkflow();
    }    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                evaluateSessionWorkflow();
            });

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

    private void evaluateSessionWorkflow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                return;
            }
        }

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
                // To avoid an automatic loop on device resume, only force dialog checks
                // if the screen elements indicate explicit user-navigation setups.
                checkPermissionsAndNavigate(cachedToken);
            }
        } else if (authToken != null && !isRedirectInProgress) {
            checkPermissionsAndNavigate(authToken);
        } else {
            showLoginScreen();
        }
    }

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

        if (hasBackgroundLocationPermission()) {
            navigateToFaceLogin(token);
        } else {
            showBackgroundPermissionDialog(token);
        }
    }    private final ActivityResultLauncher<String[]> foregroundPermissionLauncher =
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

    private void showBackgroundPermissionDialog(String token) {
        if (isFinishing() || isDestroyed()) return;
        if (backgroundDialog != null && backgroundDialog.isShowing()) return;

        backgroundDialog = new MaterialAlertDialogBuilder(this)
                .setTitle("Background Location Required")
                .setMessage("To track your shift accurately while the app is closed, please change location access permissions to 'Allow all the time' in the system settings screen.")
                .setCancelable(false)
                .setPositiveButton("Configure", (dialog, which) -> {
                    dialog.dismiss();

                    // Android 11+ (API 30) requires going to the application settings page
                    // for Background Permissions, while Android 10 can use system prompt.
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
        runOnUiThread(() -> {
            binding.editEmail.setVisibility(View.VISIBLE);
            binding.editPassword.setVisibility(View.VISIBLE);
            binding.btnSignIn.setVisibility(View.VISIBLE);
            binding.logoImage.setVisibility(View.VISIBLE);
        });
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

                        storeInSharedPreferences(userId, token);
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

    private void hideLoginScreen() {
        binding.editEmail.setVisibility(View.GONE);
        binding.editPassword.setVisibility(View.GONE);
        binding.btnSignIn.setVisibility(View.GONE);
        binding.logoImage.setVisibility(View.INVISIBLE);
    }

    private boolean hasBackgroundLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    private void navigateToFaceLogin(String token) {
        if (isFinishing() || isDestroyed()) return;
        isRedirectInProgress = true;
        Intent intent = new Intent(this, FaceLoginActivity.class);
        intent.putExtra("authToken", token);
        startActivity(intent);
        finish();
    }

    private void storeInSharedPreferences(String userId, String authToken) {
        SharedPreferences.Editor editor = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).edit();
        editor.putString("userId", userId);
        editor.putString("authToken", authToken);
        editor.apply();
    }

    private void checkBatteryOptimization() {
        if (isFinishing() || isDestroyed()) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String packageName = getPackageName();
            android.os.PowerManager pm = (android.os.PowerManager) getSystemService(Context.POWER_SERVICE);
            if (pm != null && !pm.isIgnoringBatteryOptimizations(packageName)) {

                if (batteryDialog != null && batteryDialog.isShowing()) return;

                batteryDialog = new MaterialAlertDialogBuilder(this)
                        .setTitle("Keep App Running")
                        .setMessage("To ensure tracking works while your screen is off, please disable battery optimization for this app.")
                        .setPositiveButton("Allow", (dialog, which) -> {
                            dialog.dismiss();
                            Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                            intent.setData(Uri.parse("package:" + packageName));
                            startActivity(intent);
                        })
                        .create();

                batteryDialog.show();
            }
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

    private void showLoading(boolean show) {
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }




}
package app.xedigital.ai.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.biometric.BiometricManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Objects;

import app.xedigital.ai.MainActivity;
import app.xedigital.ai.R;
import app.xedigital.ai.adminApi.AdminAPIClient;
import app.xedigital.ai.adminApi.AdminAPIInterface;
import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.databinding.ActivityLoginBinding;
import app.xedigital.ai.model.Admin.UserDetails.Role;
import app.xedigital.ai.model.Admin.UserDetails.UserDetailsResponse;
import app.xedigital.ai.model.login.LoginModelResponse;
import app.xedigital.ai.utills.BioMetric;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private boolean isEmployee;
    private ActivityLoginBinding binding;
    private View loadingOverlay;
    private BioMetric bioMetric;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        setContentView(binding.getRoot());
        loadingOverlay = binding.loadingOverlay;

        isEmployee = getIntent().getBooleanExtra("isEmployee", true);

        BiometricManager biometricManager = BiometricManager.from(this);

        boolean isBiometricSupported;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            isBiometricSupported = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL) == BiometricManager.BIOMETRIC_SUCCESS;
        } else {
            isBiometricSupported = biometricManager.canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS;
        }
        boolean isLoggedIn = isLoggedIn();

        if (isLoggedIn) {
            if (isBiometricSupported) {
                initializeBiometricAuth();
            } else {
                Log.e(TAG, "Biometric is not supported on this device. Proceeding via persistent token.");
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            }
        } else {
            showLoginScreen();
        }

        Glide.with(this).load(R.mipmap.ic_launcher).apply(RequestOptions.bitmapTransform(new CircleCrop())).into(binding.logoImage);
        binding.btnSignin.setOnClickListener(v -> {
            String email = Objects.requireNonNull(binding.editEmail.getText()).toString().trim();
            String password = Objects.requireNonNull(binding.editPassword.getText()).toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                binding.editEmail.setError("Please Enter Your Email");
                binding.editPassword.setError("Please Enter Your Password");
            } else {
                callLoginApi(email, password);
            }
        });
    }

    private void initializeBiometricAuth() {
        showLoading(true);
        bioMetric = new BioMetric(this, this, new BioMetric.BiometricAuthListener() {
            @Override
            public void onAuthenticationSucceeded() {
                runOnUiThread(() -> {
                    SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                    String userId = sharedPreferences.getString("userId", null);
                    String authToken = sharedPreferences.getString("authToken", null);
                    String empEmail = sharedPreferences.getString("empEmail", null);
                    String empFirstName = sharedPreferences.getString("empFirstName", null);

                    if (userId != null && authToken != null) {
                        showLoading(false);
                        storeInSharedPreferences(userId, authToken, empEmail, empFirstName, true);
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        showLoading(false);
                        showLoginScreen();
                    }
                });
            }

            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                showLoading(false);
                runOnUiThread(() -> {
                    Log.e(TAG, "Authentication Error: Code=" + errorCode + ", Message='" + errString + "'");
                    // Keep them on the screen to let them try password login manually without resetting data
                    showLoginScreen();
                });
            }

            @Override
            public void onAuthenticationFailed() {
                showLoading(false);
                runOnUiThread(() -> {
                    Toast.makeText(LoginActivity.this, "Authentication Failed. Please try again.", Toast.LENGTH_LONG).show();
                    // FIX: Removed clearSharedPreferences() to prevent losing session state on touch errors
                    showLoginScreen();
                });
            }
        });
        bioMetric.authenticate(true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (bioMetric != null) {
            bioMetric.handlePermissionResult(requestCode, permissions, grantResults, true);
        }
    }

    private void showLoginScreen() {
        binding.editEmail.setVisibility(View.VISIBLE);
        binding.editPassword.setVisibility(View.VISIBLE);
        binding.btnSignin.setVisibility(View.VISIBLE);
        binding.logoImage.setVisibility(View.VISIBLE);
    }

    private boolean isLoggedIn() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String authToken = sharedPreferences.getString("authToken", null);
        return authToken != null;
    }

    private void showAlertDialog(String message) {
        if (isFinishing() || isDestroyed()) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Error");
        builder.setMessage(message);
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showLoading(boolean show) {
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
        }
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

                    if (loginResponse.isSuccess() && loginResponse.getData() != null && loginResponse.getData().getUser() != null) {
                        String userId = loginResponse.getData().getUser().getId();
                        String authToken = loginResponse.getData().getToken();
                        String empEmail = loginResponse.getData().getUser().getEmail();
                        String empFirstName = loginResponse.getData().getUser().getFirstname();
                        isEmployee = true;

                        getUserDetails(userId, authToken, empEmail, empFirstName);
                    } else {
                        showAlertDialog(loginResponse.getMessage() != null ? loginResponse.getMessage() : "Invalid server response layout.");
                    }
                } else {
                    showAlertDialog("Something went wrong with the server interaction");
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginModelResponse> call, @NonNull Throwable t) {
                showLoading(false);
                showAlertDialog("Network failure: " + t.getMessage());
            }
        });
    }

    private void getUserDetails(String userId, String authToken, String empEmail, String empFirstName) {
        String token = "jwt " + authToken;
        AdminAPIInterface employeeDetails = AdminAPIClient.getInstance().getBase2();
        Call<UserDetailsResponse> employees = employeeDetails.getUser(token, userId);

        employees.enqueue(new Callback<UserDetailsResponse>() {
            @Override
            public void onResponse(@NonNull Call<UserDetailsResponse> call, @NonNull Response<UserDetailsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserDetailsResponse userDetailResponse = response.body();

                    if (userDetailResponse.isSuccess() && userDetailResponse.getData() != null) {
                        Role employeeRole = userDetailResponse.getData().getRole();

                        if (employeeRole != null) {
                            String roleName = employeeRole.getName();
                            Log.d(TAG, "User Role: " + roleName);

                            if ("employee".equalsIgnoreCase(roleName)) {
                                storeInSharedPreferences(userId, authToken, empEmail, empFirstName, true);
                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                                finish();
                            } else if ("branchadmin".equalsIgnoreCase(roleName) || "humanresource".equalsIgnoreCase(roleName)) {
                                // If they have an Admin role inside Employee login, warn them but DO NOT clear preferences
                                showAlertDialogWithLogout();
                            } else {
                                Toast.makeText(LoginActivity.this, "Unrecognized role: " + roleName, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, "No role data found inside payload", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Failed to parse system profile details template", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // FIX: If server response drops, bypass and login offline via cache instead of forcing a logout sequence
                    Log.e(TAG, "Server verification dropped. Logging in using current offline cached session state.");
                    storeInSharedPreferences(userId, authToken, empEmail, empFirstName, true);
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserDetailsResponse> call, @NonNull Throwable throwable) {
                // FIX: Network timed out, allow local offline access
                Log.e(TAG, "Network timeout: " + throwable.getMessage() + ". Defaulting to local persistent state session.");
                storeInSharedPreferences(userId, authToken, empEmail, empFirstName, true);
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            }
        });
    }

    private void showAlertDialogWithLogout() {
        if (isFinishing() || isDestroyed()) return;

        new AlertDialog.Builder(this)
                .setTitle("Access Denied")
                .setMessage("Please login with employee credentials, or try Admin Or HR login.")
                .setCancelable(false)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss()) // FIX: Removed destructive logoutUser() loop
                .show();
    }

    private void storeInSharedPreferences(String userId, String authToken, String empEmail, String empFirstName, boolean b) {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("userId", userId);
        editor.putString("authToken", authToken);
        editor.putString("empEmail", empEmail);
        editor.putString("empFirstName", empFirstName);
        editor.putBoolean("isLoggedIn", b);
        editor.apply();
    }
}
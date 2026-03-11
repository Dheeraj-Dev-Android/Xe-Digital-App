package app.xedigital.ai.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.biometric.BiometricManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Objects;

import app.xedigital.ai.MainActivity;
import app.xedigital.ai.R;
import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.databinding.ActivityLoginBinding;
import app.xedigital.ai.model.login.LoginModelResponse;
import app.xedigital.ai.utills.BioMetric;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
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

        // --- 1. SESSION LOGIC ---
        SharedPreferences prefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String authToken = prefs.getString("authToken", null);
        boolean isBioEnabled = prefs.getBoolean("isBioEnabled", false);

//        if (authToken != null) {
//            // User has logged in before. Now, how do they get in?
//            if (isBioEnabled && isBiometricHardwareAvailable()) {
//                // Option A: Use Biometrics
//                initializeBiometricAuth();
//            } else {
//                showLoginScreen();
//            }
//        } else {
//            // Brand new user
//            showLoginScreen();
//        }

        // Inside onCreate
        if (authToken != null) {
            // Only show biometrics if the activity is being created for the first time
            // and not due to a system configuration change or internal navigation
            if (savedInstanceState == null && isBioEnabled && isBiometricHardwareAvailable()) {
                initializeBiometricAuth();
            } else {
                showLoginScreen();
            }
        } else {
            showLoginScreen();
        }

        // --- 2. UI SETUP ---
        Glide.with(this).load(R.mipmap.ic_launcher).apply(RequestOptions.bitmapTransform(new CircleCrop())).into(binding.logoImage);

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

    private boolean isBiometricHardwareAvailable() {
        BiometricManager biometricManager = BiometricManager.from(this);
        int authenticators = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R ? BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL : BiometricManager.Authenticators.BIOMETRIC_STRONG;

        return biometricManager.canAuthenticate(authenticators) == BiometricManager.BIOMETRIC_SUCCESS;
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
                        String empEmail = loginResponse.getData().getUser().getEmail();
                        String empName = loginResponse.getData().getUser().getFirstname();

                        // ASK FOR BIOMETRIC PREFERENCE ON FIRST SUCCESSFUL LOGIN
                        showBiometricOptInDialog(userId, token, empEmail, empName);
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

    private void showBiometricOptInDialog(String id, String token, String email, String name) {
        if (isFinishing() || isDestroyed()) return;

        // 1. Hardware Check
        if (!isBiometricHardwareAvailable()) {
            storeInSharedPreferences(id, token, email, name, false);
            navigateToMain();
            return;
        }

        // 2. Inflate the layout
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_biometric_opt_in, null);

        // 3. Use MaterialAlertDialogBuilder but create an AlertDialog object
        androidx.appcompat.app.AlertDialog alertDialog = new MaterialAlertDialogBuilder(this, R.style.AppTheme_CustomAlertDialog)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        // 4. IMPORTANT: Make background transparent so rounded corners show
        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // 5. Initialize UI Components
        ImageView iconView = dialogView.findViewById(R.id.dialogIcon);
        Glide.with(this)
                .asGif()
                .load(R.raw.biometric_scan)
                .into(iconView);
        com.google.android.material.button.MaterialButton btnEnable = dialogView.findViewById(R.id.btnEnable);
        com.google.android.material.button.MaterialButton btnLater = dialogView.findViewById(R.id.btnLater);

        // Start Pulse Animation
        Animation pulse = AnimationUtils.loadAnimation(this, R.anim.pulse);
        if (iconView != null) iconView.startAnimation(pulse);

        // 6. Listeners
        btnEnable.setOnClickListener(v -> {
            vibratePhone();
            storeInSharedPreferences(id, token, email, name, true);
            alertDialog.dismiss(); // Prevent Leak
            navigateToMain();
        });

        btnLater.setOnClickListener(v -> {
            storeInSharedPreferences(id, token, email, name, false);
            alertDialog.dismiss(); // Prevent Leak
            navigateToMain();
        });

        alertDialog.show();
    }

    private void vibratePhone() {
        View view = findViewById(android.R.id.content);
        if (view != null) {
            view.performHapticFeedback(android.view.HapticFeedbackConstants.CONFIRM);
        }
    }

    private void initializeBiometricAuth() {
        showLoading(true);
        bioMetric = new BioMetric(this, this, new BioMetric.BiometricAuthListener() {
            @Override
            public void onAuthenticationSucceeded() {
                runOnUiThread(() -> {
                    showLoading(false);
                    navigateToMain();
                    Toast.makeText(LoginActivity.this, "Welcome Back!", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                showLoading(false);
                runOnUiThread(() -> {
                    Toast.makeText(LoginActivity.this, "Biometric Unavailable: " + errString, Toast.LENGTH_SHORT).show();
                    showLoginScreen();
                });
            }

            @Override
            public void onAuthenticationFailed() {
                showLoading(false);
                runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Authentication Failed", Toast.LENGTH_SHORT).show());
            }
        });
        bioMetric.authenticate(true);
    }

    private void storeInSharedPreferences(String userId, String authToken, String empEmail, String empFirstName, boolean isBioEnabled) {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("userId", userId);
        editor.putString("authToken", authToken);
        editor.putString("empEmail", empEmail);
        editor.putString("empFirstName", empFirstName);
        editor.putBoolean("isBioEnabled", isBioEnabled);
        editor.apply();
    }

    private void navigateToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showLoginScreen() {
        binding.editEmail.setVisibility(View.VISIBLE);
        binding.editPassword.setVisibility(View.VISIBLE);
        binding.btnSignIn.setVisibility(View.VISIBLE);
    }

    private void showLoading(boolean show) {
        loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showAlertDialog(String message) {
        // Check if the activity is valid to host a dialog
        if (isFinishing() || isDestroyed() || isChangingConfigurations()) {
            return;
        }
        // 2. Check for configuration changes (rotation, etc.)
        // We don't want to bind a Dialog to an Activity that is about to be recreated
        if (isChangingConfigurations()) {
            return;
        }
        new AlertDialog.Builder(this).setTitle("Login Information").setMessage(message).setPositiveButton("OK", null).show();
    }
}
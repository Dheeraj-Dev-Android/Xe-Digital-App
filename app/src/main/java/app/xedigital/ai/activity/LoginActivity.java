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
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Objects;

import app.xedigital.ai.MainActivity;
import app.xedigital.ai.R;
import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.databinding.ActivityLoginBinding;
import app.xedigital.ai.model.login.LoginModelResponse;
import app.xedigital.ai.ui.profile.ProfileViewModel;
import app.xedigital.ai.utills.BioMetric;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final int BIOMETRIC_PERMISSION_REQUEST_CODE = 100;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private ActivityLoginBinding binding;
    private View loadingOverlay;
    private BioMetric bioMetric;
    private BiometricManager biometricManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        loadingOverlay = binding.loadingOverlay;

//        if (isLoggedIn()) {
//            startActivity(new Intent(LoginActivity.this, MainActivity.class));
//            finish();
//            return;
//        }

        // Initialize BiometricManager
        biometricManager = BiometricManager.from(this);

        // Check if biometric is supported and if user is logged in
        boolean isBiometricSupported;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            isBiometricSupported = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL) == BiometricManager.BIOMETRIC_SUCCESS;
        } else {
            isBiometricSupported = biometricManager.canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS;
        }
        boolean isLoggedIn = isLoggedIn();

        // Determine initial state of the activity based on login state
        if (isLoggedIn) {
            // User is logged in, attempt biometric login
            if (isBiometricSupported) {
                // Biometric is supported, initialize and authenticate
                initializeBiometricAuth();
            } else {
                // Biometric is not supported, jump directly to MainActivity
                Log.e(TAG, "Biometric is not supported on this device.");
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            }
        } else {
            showLoginScreen();
        }

        Glide.with(this).load(R.mipmap.ic_launcher).apply(RequestOptions.bitmapTransform(new CircleCrop())).into(binding.logoImage);

        binding.btnSignin.setOnClickListener(v -> {
            String email, password;
            email = Objects.requireNonNull(binding.editEmail.getText()).toString();
            password = Objects.requireNonNull(binding.editPassword.getText()).toString();

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
        // Biometric setup
        bioMetric = new BioMetric(this, this, new BioMetric.BiometricAuthListener() {
            @Override
            public void onAuthenticationSucceeded() {

                runOnUiThread(() -> {
                    // When biometric is successful, attempt to retrieve data from SharedPreferences
                    SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                    String userId = sharedPreferences.getString("userId", null);
                    String authToken = sharedPreferences.getString("authToken", null);
                    String empEmail = sharedPreferences.getString("empEmail", null);
                    String empFirstName = sharedPreferences.getString("empFirstName", null);

                    if (userId != null && authToken != null) {
                        showLoading(false);
                        storeInSharedPreferences(userId, authToken, empEmail, empFirstName);
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        showLoading(false);
                        if (userId == null && authToken == null) {
                            Log.e(TAG, "No data found for biometric login. Please login with email and password.");
                            showLoginScreen();
                        }
                        // No data found in SharedPreferences, prompt the user to login again
                        showLoginScreen();
                        showAlertDialog("No data found for biometric login. Please login with email and password.");
                    }
                });
            }

            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                showLoading(false);
                runOnUiThread(() -> {
                    Log.e(TAG, "Authentication Error: Code=" + errorCode + ", Message='" + errString + "'");
                    Toast.makeText(LoginActivity.this, "Authentication Error", Toast.LENGTH_SHORT).show();
                    showLoginScreen();
                });
            }

            @Override
            public void onAuthenticationFailed() {
                showLoading(false);
                runOnUiThread(() -> {
                    Toast.makeText(LoginActivity.this, "Authentication Failed", Toast.LENGTH_LONG).show();
                    clearSharedPreferences();
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
//            bioMetric.handlePermissionResult(requestCode, permissions, grantResults);
            bioMetric.handlePermissionResult(requestCode, permissions, grantResults, true);
        }
    }

    private void showLoginScreen() {
        binding.editEmail.setVisibility(View.VISIBLE);
        binding.editPassword.setVisibility(View.VISIBLE);
        binding.btnSignin.setVisibility(View.VISIBLE);
        binding.logoImage.setVisibility(View.VISIBLE);
        binding.editEmail.setText("");
        binding.editPassword.setText("");
    }

    private boolean isLoggedIn() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String authToken = sharedPreferences.getString("authToken", null);
        return authToken != null;
    }

    private void showAlertDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Error");
        builder.setMessage(message);
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showLoading(boolean show) {
        if (show) {
            loadingOverlay.setVisibility(View.VISIBLE);
        } else {
            loadingOverlay.setVisibility(View.GONE);
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
                    Log.w(TAG, "Response: " + gson.toJson(loginResponse));

                    if (loginResponse.isSuccess() == Boolean.parseBoolean("true")) {
                        getIntent().putExtra("userId", loginResponse.getData().getUser().getId());
                        getIntent().putExtra("authToken", loginResponse.getData().getToken());
                        getIntent().putExtra("empEmail", loginResponse.getData().getUser().getEmail());
                        getIntent().putExtra("empFirstName", loginResponse.getData().getUser().getFirstname());

                        String userId = loginResponse.getData().getUser().getId();
                        String authToken = loginResponse.getData().getToken();
                        String empEmail = loginResponse.getData().getUser().getEmail();
                        String empFirstName = loginResponse.getData().getUser().getFirstname();

                        storeInSharedPreferences(userId, authToken, empEmail, empFirstName);
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        showAlertDialog(loginResponse.getMessage());
                    }
                } else {
                    showAlertDialog("Something went wrong");
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginModelResponse> call, @NonNull Throwable t) {
                showLoading(false);
                showAlertDialog(t.getMessage());
            }
        });
    }

    private void storeInSharedPreferences(String userId, String authToken, String empEmail, String empFirstName) {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("userId", userId);
        editor.putString("authToken", authToken);
        editor.putString("empEmail", empEmail);
        editor.putString("empFirstName", empFirstName);
        editor.apply();
    }

    private void storeInViewModel(String userId, String authToken) {
        ProfileViewModel profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        profileViewModel.storeLoginData(userId, authToken);

    }

    private void clearSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("userId");
        editor.remove("authToken");
        editor.remove("empEmail");
        editor.remove("empFirstName");
        editor.apply();
    }
}
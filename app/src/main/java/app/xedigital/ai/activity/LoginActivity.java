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


        biometricManager = BiometricManager.from(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            biometricManager = BiometricManager.from(this);
        } else {
            biometricManager = BiometricManager.from(this);
        }

        boolean isBiometricSupported = false;
        //check for the version support
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            isBiometricSupported = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL) == BiometricManager.BIOMETRIC_SUCCESS;
        } else {
            isBiometricSupported = biometricManager.canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS; // deprecated from api 29
        }
        // Check if the user is logged in and then perform the appropriate action
        if (isLoggedIn()) {
            String userId = getSharedPreferences("MyPrefs", MODE_PRIVATE).getString("userId", null);
            String authToken = getSharedPreferences("MyPrefs", MODE_PRIVATE).getString("authToken", null);
            if (userId != null && authToken != null) {
                if (isBiometricSupported) {
                    bioMetric = new BioMetric(this, this, new BioMetric.BiometricAuthListener() {
                        @Override
                        public void onAuthenticationSucceeded() {
                            runOnUiThread(() -> {
                                // When biometric is successful, attempt to retrieve data from SharedPreferences
                                SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                                String userId = sharedPreferences.getString("userId", null);
                                String authToken = sharedPreferences.getString("authToken", null);
                                String empEmail = sharedPreferences.getString("empEmail", null);

                                if (userId != null && authToken != null && empEmail != null) {
                                    storeInSharedPreferences(userId, authToken, empEmail);
                                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                    Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                                    finish();
                                } else {
                                    // No data found in SharedPreferences, prompt the user to login again
                                    showAlertDialog("No data found for biometric login. Please login with email and password.");
                                    //if the biometric is successful then what can i send in the call login api to get the user details and auth token
                                    binding.bioMetricButton.setVisibility(View.GONE);
                                }
                            });
                        }

                        @Override
                        public void onAuthenticationError(int errorCode, CharSequence errString) {
                            runOnUiThread(() -> {
                                Log.e(TAG, "Authentication Error: Code=$errorCode, Message='$errString'");
                                Toast.makeText(LoginActivity.this, "Authentication Error", Toast.LENGTH_SHORT).show();
                            });
                            Log.e("MainActivity", "onAuthenticationError called");
                        }

                        @Override
                        public void onAuthenticationFailed() {
                            runOnUiThread(() -> {
                                Toast.makeText(LoginActivity.this, "Authentication Failed", Toast.LENGTH_LONG).show();
                                binding.bioMetricButton.setVisibility(View.GONE);
                                clearSharedPreferences();

                                // Show login screen
                                binding.editEmail.setVisibility(View.VISIBLE);
                                binding.editPassword.setVisibility(View.VISIBLE);
                                binding.btnSignin.setVisibility(View.VISIBLE);
                                binding.logoImage.setVisibility(View.VISIBLE);
                                binding.bioMetricButton.setVisibility(View.GONE);
                                binding.editEmail.setText("");
                                binding.editPassword.setText("");
                            });
                            Log.e("MainActivity", "onAuthenticationFailed called");
                        }
                    });
                    bioMetric.authenticate();
                } else {
                    Log.e(TAG, "Biometric is not supported on this device.");
                    //biometric is not available but user is login then directly jump to main activity
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                }

            } else {
                // If user is not logged in, show login screen
                binding.editEmail.setVisibility(View.VISIBLE);
                binding.editPassword.setVisibility(View.VISIBLE);
                binding.btnSignin.setVisibility(View.VISIBLE);
                binding.logoImage.setVisibility(View.VISIBLE);
            }

        } else {
            // If user is not logged in, show login screen
            binding.editEmail.setVisibility(View.VISIBLE);
            binding.editPassword.setVisibility(View.VISIBLE);
            binding.btnSignin.setVisibility(View.VISIBLE);
            binding.logoImage.setVisibility(View.VISIBLE);
        }
        boolean finalIsBiometricSupported = isBiometricSupported;
        binding.bioMetricButton.setOnClickListener(v -> {
            if (finalIsBiometricSupported) {
                bioMetric.authenticate();
            } else {
                Toast.makeText(LoginActivity.this, "Biometric is not supported on this device.", Toast.LENGTH_SHORT).show();
            }
        });
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (bioMetric != null) {
            bioMetric.handlePermissionResult(requestCode, permissions, grantResults);
        }
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

                        String userId = loginResponse.getData().getUser().getId();
                        String authToken = loginResponse.getData().getToken();
                        String empEmail = loginResponse.getData().getUser().getEmail();
//                        Log.w(TAG, "User ID: " + userId+"empEmail"+empEmail);

                        storeInSharedPreferences(userId, authToken, empEmail);
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


    private void storeInSharedPreferences(String userId, String authToken, String empEmail) {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("userId", userId);
        editor.putString("authToken", authToken);
        editor.putString("empEmail", empEmail);
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
        editor.apply();
    }


}
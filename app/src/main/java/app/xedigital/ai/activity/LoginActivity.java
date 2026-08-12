package app.xedigital.ai.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
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
import app.xedigital.ai.utills.SecurePrefManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private View loadingOverlay;
    private boolean isRedirectInProgress = false;
    private boolean isCheckingPermissions = false;
    private AlertDialog batteryDialog;
    private AlertDialog infoDialog;

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
                callLoginApi(email, password);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isCheckingPermissions) {
            isRedirectInProgress = false;
            evaluateSessionWorkflow();
        }
    }

    private void evaluateSessionWorkflow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                isCheckingPermissions = true;
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                return;
            }
        }
        proceedToAuthCheck();
    }

    private void proceedToAuthCheck() {
        SecurePrefManager prefManager = SecurePrefManager.getInstance(this);
        String authToken = prefManager.getString("authToken", null);
        String cachedToken = prefManager.getString("cachedTokenPermission", null);
        boolean isFallback = getIntent().getBooleanExtra("isFallback", false);

        if (isFallback) {
            showLoginScreen();
            return;
        }

        if (cachedToken != null) {
            if (hasForegroundLocationPermission()) {
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

    private void storeInSharedPreferences(String userId, String emailId, String authToken) {
        SecurePrefManager prefManager = SecurePrefManager.getInstance(this);
        prefManager.putString("userId", userId);
        prefManager.putString("emailId", emailId);
        prefManager.putString("authToken", authToken);
    }    private final ActivityResultLauncher<String> notificationPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
        isCheckingPermissions = false;
        proceedToAuthCheck();
    });

    private void checkPermissionsAndNavigate(String token) {
        SecurePrefManager.getInstance(this).putString("cachedTokenPermission", token);
        if (!hasForegroundLocationPermission()) {
            foregroundPermissionLauncher.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION});
            return;
        }
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

    private final ActivityResultLauncher<String[]> foregroundPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
        boolean fineGranted = Boolean.TRUE.equals(result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false));
        boolean coarseGranted = Boolean.TRUE.equals(result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false));

        SecurePrefManager prefManager = SecurePrefManager.getInstance(this);
        String cachedToken = prefManager.getString("cachedTokenPermission", null);

        if (fineGranted || coarseGranted) {
            if (cachedToken != null) {
                checkPermissionsAndNavigate(cachedToken);
            }
        } else {
            showLoginScreen();
            showAlertDialog("Foreground location permission is required for shift tracking.");
        }
    });

    private boolean hasForegroundLocationPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void showLoginScreen() {
        binding.layoutEmail.setVisibility(View.VISIBLE);
        binding.layoutPassword.setVisibility(View.VISIBLE);
        binding.btnSignIn.setVisibility(View.VISIBLE);
        binding.logoCard.setVisibility(View.VISIBLE);
    }

    private void showAlertDialog(String message) {
        if (isFinishing() || isDestroyed()) return;

        if (infoDialog != null && infoDialog.isShowing()) {
            infoDialog.dismiss();
        }

        infoDialog = new AlertDialog.Builder(this).setTitle("Login Info").setMessage(message).setPositiveButton("OK", null).create();

        infoDialog.show();
    }

    @Override
    protected void onDestroy() {
        if (batteryDialog != null && batteryDialog.isShowing()) {
            batteryDialog.dismiss();
        }
        if (infoDialog != null && infoDialog.isShowing()) {
            infoDialog.dismiss();
        }
        super.onDestroy();
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







}
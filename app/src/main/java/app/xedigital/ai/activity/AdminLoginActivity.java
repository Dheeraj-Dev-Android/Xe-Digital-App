package app.xedigital.ai.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

import app.xedigital.ai.R;
import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.model.login.LoginModelResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminLoginActivity extends AppCompatActivity {

    private TextInputEditText emailEditText, passwordEditText;
    private MaterialButton loginButton;
    private FrameLayout loadingOverlay;
    private boolean isEmployee = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        // Retrieve isEmployee from intent
        isEmployee = getIntent().getBooleanExtra("isEmployee", false);

        // Updated IDs from layout
        emailEditText = findViewById(R.id.edit_email);
        passwordEditText = findViewById(R.id.edit_password);
        loginButton = findViewById(R.id.btn_signin);
        loadingOverlay = findViewById(R.id.loadingOverlay);

        loginButton.setOnClickListener(v -> performLogin());
    }

    private void performLogin() {
        String email = Objects.requireNonNull(emailEditText.getText()).toString().trim();
        String password = Objects.requireNonNull(passwordEditText.getText()).toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);

        Call<LoginModelResponse> call = APIClient.getInstance().getLogin().loginApi1(email, password);

        call.enqueue(new Callback<LoginModelResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginModelResponse> call, @NonNull Response<LoginModelResponse> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    LoginModelResponse loginResponse = response.body();

                    if (loginResponse.isSuccess() == Boolean.parseBoolean("true")) {
                        getIntent().putExtra("userId", loginResponse.getData().getUser().getId());
                        getIntent().putExtra("authToken", loginResponse.getData().getToken());
                        getIntent().putExtra("empEmail", loginResponse.getData().getUser().getEmail());
                        getIntent().putExtra("empFirstName", loginResponse.getData().getUser().getFirstname());

                        String userId = loginResponse.getData().getUser().getId();
                        String authToken = loginResponse.getData().getToken();
                        String empEmail = loginResponse.getData().getUser().getEmail();
                        String empFirstName = loginResponse.getData().getUser().getFirstname();

                        storeInSharedPreferences(userId, authToken, empEmail, empFirstName, isEmployee);
                        startActivity(new Intent(AdminLoginActivity.this, AdminDashboardActivity.class));

                        Toast.makeText(AdminLoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        showAlertDialog(loginResponse.getMessage());
                    }
                } else {
                    Toast.makeText(AdminLoginActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginModelResponse> call, @NonNull Throwable t) {
                showLoading(false);
                Toast.makeText(AdminLoginActivity.this, "Login failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void storeInSharedPreferences(String userId, String authToken, String empEmail, String empFirstName, boolean isEmployee) {
        SharedPreferences sharedPreferences = getSharedPreferences("AdminCred", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("userId", userId);
        editor.putString("authToken", authToken);
        editor.putString("empEmail", empEmail);
        editor.putString("empFirstName", empFirstName);
        editor.putBoolean("isEmployee", isEmployee);
        editor.apply();
    }
}

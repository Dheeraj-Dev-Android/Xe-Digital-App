package app.xedigital.ai.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.ViewModelProvider;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import app.xedigital.ai.MainActivity;
import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.databinding.ActivityLoginBinding;
import app.xedigital.ai.model.login.LoginModelResponse1;
import app.xedigital.ai.ui.profile.ProfileViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (isLoggedIn()) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
            return;
        }
        binding.btnSignin.setOnClickListener(v -> {
            String email, password;
            email = binding.editEmail.getText().toString();
            password = binding.editPassword.getText().toString();

            if (email.isEmpty() || password.isEmpty()) {
                binding.editEmail.setError("Please Enter Your Email");
                binding.editPassword.setError("Please Enter Your Password");
            } else {
                callLoginApi1(email, password);
            }
        });
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

    private void callLoginApi1(String email, String password) {
        Call<LoginModelResponse1> call = APIClient.getInstance().getLogin().loginApi1(email, password);
        call.enqueue(new Callback<LoginModelResponse1>() {
            @Override
            public void onResponse(@NonNull Call<LoginModelResponse1> call, @NonNull Response<LoginModelResponse1> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginModelResponse1 loginResponse = response.body();

                    String responseJson = gson.toJson(loginResponse);

                    Log.d(TAG, "Login Response:\n " + responseJson);

                    if (loginResponse.isSuccess() == Boolean.parseBoolean("true")) {
                        getIntent().putExtra("userId", loginResponse.getData().getUser().getId());
                        getIntent().putExtra("authToken", loginResponse.getData().getToken());

                        String userId = loginResponse.getData().getUser().getId();
                        String authToken = loginResponse.getData().getToken();

                        Log.d(TAG, "User ID: " + userId);

                        storeInSharedPreferences(userId, authToken);
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
            public void onFailure(@NonNull Call<LoginModelResponse1> call, @NonNull Throwable t) {
                showAlertDialog(t.getMessage());
            }
        });
    }


    private void storeInSharedPreferences(String userId, String authToken) {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("userId", userId);
        editor.putString("authToken", authToken);
        editor.apply();

    }

    private void storeInViewModel(String userId, String authToken) {
        ProfileViewModel profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        profileViewModel.storeLoginData(userId, authToken);

    }
}
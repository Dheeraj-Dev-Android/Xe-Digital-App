package app.xedigital.ai.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
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
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private ActivityLoginBinding binding;
    private View loadingOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        loadingOverlay = binding.loadingOverlay;

        if (isLoggedIn()) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
            return;
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
}
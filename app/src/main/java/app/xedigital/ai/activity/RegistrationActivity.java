package app.xedigital.ai.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import app.xedigital.ai.R;
import app.xedigital.ai.databinding.ActivityRegistrationBinding;


public class RegistrationActivity extends AppCompatActivity {

    ActivityRegistrationBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegistrationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);


        TextView loginTextView = findViewById(R.id.txt_login);

        loginTextView.setOnClickListener(v -> {
            Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
            startActivity(intent);
        });


        binding.btnSignup.setOnClickListener(v -> {
            String username, email, password;
            username = binding.editUsername.getText().toString();
            email = binding.editEmail.getText().toString();
            password = binding.editPassword.getText().toString();

            if (username.isEmpty() && email.isEmpty() && password.isEmpty()) {
                // All fields are empty, show errors for all
                binding.editUsername.setError("Please enter username");
                binding.editEmail.setError("Please enter email");
                binding.editPassword.setError("Please enter password");
            } else {
                // Check each field individually
                if (username.isEmpty()) {
                    binding.editUsername.setError("Please enter username");
                }
                if (email.isEmpty()) {
                    binding.editEmail.setError("Please enter email");
                }
                if (password.isEmpty()) {
                    binding.editPassword.setError("Please enter password");
                }

                // If all fields are filled (no errors set above), call the API
//                    if (binding.editUsername.getError() == null &&
//                            binding.editEmail.getError() == null &&
//                            binding.editPassword.getError() == null) {
//                        callRegisterApi(username, email, password);
//                    }
            }
        });
    }

//    private void callRegisterApi(String username, String email, String password) {
//        Call<RegisterModelResponse> call = APIClient.getInstance().getApi().registerApi(username, email, password);
//        call.enqueue(new Callback<RegisterModelResponse>() {
//            @Override
//            public void onResponse(@NonNull Call<RegisterModelResponse> call, @NonNull Response<RegisterModelResponse> response) {
//                if (response.isSuccessful() && response.body() != null) {
//                    RegisterModelResponse registerResponse = response.body();
//
//
//                    if (registerResponse.isStatus()) {
//                        startActivity(new Intent(RegistrationActivity.this, LoginActivity1.class));
//                        Toast.makeText(RegistrationActivity.this, registerResponse.getMessage(), Toast.LENGTH_SHORT).show();
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                showAlertDialog("Success", "Login Successful");
//                            }
//                        });
//                    } else {
//                        showAlertDialog("Error", registerResponse.getMessage());
//                        Toast.makeText(RegistrationActivity.this, registerResponse.getMessage(), Toast.LENGTH_SHORT).show();
//
//                    }
//
//                }  else {
//                    try {
//                        // Attempt to get a more descriptive error message from the server
//                        String errorMessage = response.errorBody() != null ? response.errorBody().string() : "Unknown Error";
//                        showAlertDialog("Error", "Registration Failed: " + errorMessage);
//                        Toast.makeText(RegistrationActivity.this, "Registration Failed: " + errorMessage, Toast.LENGTH_SHORT).show();
//
//                        Log.e("RegistrationActivity", "Registration API Error: Code " + response.code() + ", Message: " + errorMessage);
//                    } catch (IOException e) {
//                        Toast.makeText(RegistrationActivity.this, "Registration Failed: Error parsing error response", Toast.LENGTH_SHORT).show();
//                        showAlertDialog("Error", "Registration Failed: Error parsing error response");
//                        Log.e("RegistrationActivity", "Error parsing registration error response", e);
//                    }
//                }
//            }
//
//            @Override
//            public void onFailure(Call<RegisterModelResponse> call, Throwable t) {
//                Toast.makeText(RegistrationActivity.this, "Registration Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
//                showAlertDialog("Error", "Registration Error: " + t.getMessage());
//                Log.e("RegistrationActivity", "Registration API Failure", t);
//            }
//
//        });
//    }

    private void showAlertDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(RegistrationActivity.this);
        builder.setTitle(title).setMessage(message).setPositiveButton("OK", (dialog, id) -> {
            // Close the dialog if needed, or do nothing
            dialog.dismiss();
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
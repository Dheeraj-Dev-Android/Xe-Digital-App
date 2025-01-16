package app.xedigital.ai.ui.profile;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

import app.xedigital.ai.R;
import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.api.APIInterface;
import app.xedigital.ai.databinding.FragmentEditProfileBinding;
import app.xedigital.ai.model.user.UserModelResponse;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileFragment extends Fragment {

    private FragmentEditProfileBinding binding;
    private String authToken, userId;
    private APIInterface apiService;
    private String companyId, branchId, roleId;

    public EditProfileFragment() {
        // Required empty public constructor
    }

    public static EditProfileFragment newInstance(String param1, String param2) {
        EditProfileFragment fragment = new EditProfileFragment();
        Bundle args = new Bundle();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment using View Binding
        binding = FragmentEditProfileBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        apiService = APIClient.getInstance().getUser();
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        authToken = sharedPreferences.getString("authToken", "");
        userId = sharedPreferences.getString("userId", "");
        fetchUserProfile(userId, authToken);

        // Make fields non-editable
        binding.editTextFirstName.setEnabled(false);
        binding.editTextLastName.setEnabled(false);
        binding.editTextEmail.setEnabled(false);
        binding.spinnerRole.setEnabled(false);
        binding.spinnerStatus.setEnabled(false);

        binding.buttonUpdate.setOnClickListener(v -> {
            if (isValidFormData()) {
                updateProfile();
            }
        });
        binding.clearForm.setOnClickListener(v -> clearForm());
        return view;
    }

    private void updateProfile() {
        binding.progressBar.setVisibility(View.VISIBLE);

        try {
            // Create JSON payload
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("company", companyId);
            jsonObject.put("branch", branchId);
            jsonObject.put("role", roleId);
            jsonObject.put("firstname", Objects.requireNonNull(binding.editTextFirstName.getText()).toString());
            jsonObject.put("lastname", Objects.requireNonNull(binding.editTextLastName.getText()).toString());
            jsonObject.put("email", Objects.requireNonNull(binding.editTextEmail.getText()).toString());
            jsonObject.put("active", Objects.requireNonNull(binding.spinnerStatus.getText()).toString().equalsIgnoreCase("Active"));
            jsonObject.put("password", Objects.requireNonNull(binding.editTextPassword.getText()).toString());
            jsonObject.put("confirmPassword", Objects.requireNonNull(binding.editTextConfirmPassword.getText()).toString());

            // Create RequestBody
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody requestBody = RequestBody.create(JSON, jsonObject.toString());

            // Make API call
            Call<ResponseBody> call = apiService.editUserProfile(userId, "jwt " + authToken, requestBody);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    binding.progressBar.setVisibility(View.GONE);
                    if (response.isSuccessful()) {
                        // Handle successful response
                        Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                        clearForm();
                        navigateToProfileFragment();
                    } else {
                        // Handle error response
                        Toast.makeText(requireContext(), "Failed to update profile", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    binding.progressBar.setVisibility(View.GONE);
                    // Handle network or API call failure
                    Toast.makeText(requireContext(), "Network error", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
            binding.progressBar.setVisibility(View.GONE);
            Toast.makeText(requireContext(), "Error creating JSON payload", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchUserProfile(String userId, String authToken) {
        binding.progressBar.setVisibility(View.VISIBLE);
        Call<UserModelResponse> call = apiService.getUserData(userId, "jwt " + authToken);
        call.enqueue(new Callback<UserModelResponse>() {
            @Override
            public void onResponse(@NonNull Call<UserModelResponse> call, @NonNull Response<UserModelResponse> response) {
                binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    UserModelResponse userModel = response.body();
                    if (userModel != null && userModel.getData() != null && userModel.getData().getUser() != null) {
                        // Update UI with user data
                        binding.editTextFirstName.setText(userModel.getData().getUser().getFirstname());
                        binding.editTextLastName.setText(userModel.getData().getUser().getLastname());
                        binding.editTextEmail.setText(userModel.getData().getUser().getEmail());
                        binding.spinnerRole.setText(userModel.getData().getRole().getName());
                        binding.spinnerStatus.setText(userModel.getData().getUser().isActive() ? "Active" : "Inactive");

                        companyId = userModel.getData().getCompany().getId();
                        branchId = userModel.getData().getBranch().getId();
                        roleId = userModel.getData().getRole().getId();

                    } else {
                        showEmptyDataAlert();
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to fetch user data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserModelResponse> call, @NonNull Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                // Handle network or other errors
            }
        });
    }

    private void showEmptyDataAlert() {
        new AlertDialog.Builder(requireContext()).setTitle("No Data Found").setMessage("User data is empty. Please try again later.").setPositiveButton("OK", (dialog, which) -> dialog.dismiss()).show();
    }

    private void clearForm() {
        binding.editTextPassword.setText("");
        binding.editTextConfirmPassword.setText("");
    }

    private void navigateToProfileFragment() {
        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(R.id.action_nav_edit_profile_to_nav_profile);
        Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
    }

    private boolean isValidFormData() {
        String password = Objects.requireNonNull(binding.editTextPassword.getText()).toString();
        String confirmPassword = Objects.requireNonNull(binding.editTextConfirmPassword.getText()).toString();

        binding.textInputLayoutPassword.setError(null);
        binding.textInputLayoutConfirmPassword.setError(null);

        if (password.isEmpty()) {
            binding.textInputLayoutPassword.setError("Password is required");
            return false;
        }

        if (password.length() < 8) {
            binding.textInputLayoutPassword.setError("Password must be at least 8 characters");
            return false;
        }

        if (password.length() > 16) {
            binding.textInputLayoutPassword.setError("Password cannot be more than 16 characters");
            return false;
        }

        if (confirmPassword.isEmpty()) {
            binding.textInputLayoutConfirmPassword.setError("Confirm password is required");
            return false;
        }


        if (confirmPassword.length() < 8) {
            binding.textInputLayoutConfirmPassword.setError("Confirm password must be at least 8 characters");
            return false;
        }

        if (confirmPassword.length() > 16) {
            binding.textInputLayoutConfirmPassword.setError("Confirm password cannot be more than 16 characters");
            return false;
        }


        if (!password.equals(confirmPassword)) {
            binding.textInputLayoutConfirmPassword.setError("Passwords do not match");
            return false;
        }

        return true;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.editTextPassword.setFilters(new InputFilter[]{new InputFilter.LengthFilter(16)});
        binding.editTextConfirmPassword.setFilters(new InputFilter[]{new InputFilter.LengthFilter(16)});

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
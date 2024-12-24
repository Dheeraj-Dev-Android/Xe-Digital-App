package app.xedigital.ai.ui.vms;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.datepicker.MaterialDatePicker;

import java.io.IOException;
import java.util.Calendar;
import java.util.Objects;

import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.api.APIInterface;
import app.xedigital.ai.databinding.FragmentVisitorPreApprovedBinding;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VisitorPreApprovedFragment extends Fragment {

    private FragmentVisitorPreApprovedBinding binding;
    private VisitorPreApprovedViewModel mViewModel;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri selectedImageUri;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    public static VisitorPreApprovedFragment newInstance() {
        return new VisitorPreApprovedFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentVisitorPreApprovedBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(VisitorPreApprovedViewModel.class);
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String authToken = sharedPreferences.getString("authToken", "");
        String empEmail = sharedPreferences.getString("empEmail", "");
        Log.w("VisitorPreApprovedFragment", "empEmail: " + empEmail);
        mViewModel.fetchUserProfile(empEmail, "jwt " + authToken);

        mViewModel.getUserProfileLiveData().observe(getViewLifecycleOwner(), userProfileByEmailResponse -> {
            if (userProfileByEmailResponse != null && userProfileByEmailResponse.getData() != null && userProfileByEmailResponse.getData().getEmployee() != null) {
                // Access isActive only if all objects are not null
                boolean isActive = userProfileByEmailResponse.getData().getEmployee().isActive();
                Log.w("VisitorPreApprovedFragment", "isActive: " + isActive);

                binding.etStatus.setText(isActive ? "Active" : "Inactive");

                Log.i("UserProfile", userProfileByEmailResponse.toString());
            } else {
                Log.e("UserProfile", "User profile response or its sub-objects are null");
            }
        });

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null && data.getData() != null) {
                            selectedImageUri = data.getData();
                            binding.ivProfile.setImageURI(selectedImageUri);
                        }
                    }
                });

        binding.btnSelectImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });


        // Material Date Picker setup
        binding.etPreApprovedDate.setOnClickListener(v -> {
            long today = Calendar.getInstance().getTimeInMillis();
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select Pre-Approved Date")
                    .setSelection(today)
                    .build();

            datePicker.addOnPositiveButtonClickListener(selection -> {
                // Format the date and set it to the EditText
                binding.etPreApprovedDate.setText(datePicker.getHeaderText());
            });

            datePicker.show(getParentFragmentManager(), "datePicker");
        });

        binding.btnClear.setOnClickListener(v -> {
            binding.etContact.setText("");
            binding.etName.setText("");
            binding.etEmail.setText("");
            binding.etCompany.setText("");
            binding.etPreApprovedDate.setText("");

        });
        binding.btnSubmit.setOnClickListener(v -> {
            if (isValidInput()) {
                Toast.makeText(requireContext(), "Form submitted successfully", Toast.LENGTH_SHORT).show();

                String status = Objects.requireNonNull(binding.etStatus.getText()).toString();
                String contact = Objects.requireNonNull(binding.etContact.getText()).toString();
                String name = Objects.requireNonNull(binding.etName.getText()).toString();
                String email = Objects.requireNonNull(binding.etEmail.getText()).toString();
                String company = Objects.requireNonNull(binding.etCompany.getText()).toString();
                String preApprovedDate = Objects.requireNonNull(binding.etPreApprovedDate.getText()).toString();

            }
        });

        binding.btnCheckContact.setOnClickListener(v -> {
            String contact = Objects.requireNonNull(binding.etContact.getText()).toString().trim();
            if (contact.isEmpty()) {
                binding.etContact.setError("Contact is required");
                return;
            }


            SharedPreferences sharedPreferences1 = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
            String authToken1 = sharedPreferences1.getString("authToken", "");

            APIInterface checkContact = APIClient.getInstance().getApi();

            // Make the API call
            Call<ResponseBody> call = checkContact.getVisitorDetail(contact, "jwt " + authToken1);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        // Handle successful response
                        try {
                            String responseBody = response.body().string();

                            Log.d("API Response", responseBody);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        // Handle error response
                        Log.e("API Error", "Error: " + response.code() + " - " + response.message());
                        Toast.makeText(requireContext(), "Failed to fetch visitor details", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    // Handle network or other errors
                    Log.e("API Failure", "Failure: " + t.getMessage(), t);
                    Toast.makeText(requireContext(), "Failed to fetch visitor details", Toast.LENGTH_SHORT).show();
                }
            });
        });

    }
    private boolean isValidInput() {
        String contact = Objects.requireNonNull(binding.etContact.getText()).toString().trim();
        String name = Objects.requireNonNull(binding.etName.getText()).toString().trim();
        String email = Objects.requireNonNull(binding.etEmail.getText()).toString().trim();
        String company = Objects.requireNonNull(binding.etCompany.getText()).toString().trim();
        String preApprovedDate = Objects.requireNonNull(binding.etPreApprovedDate.getText()).toString().trim();

        if (contact.isEmpty()) {
            binding.etContact.setError("Contact is required");
            return false;
        }
        if (name.isEmpty()) {
            binding.etName.setError("Name is required");
            return false;
        }
        if (email.isEmpty()) {
            binding.etEmail.setError("Email is required");
            return false;
        }
        if (company.isEmpty()) {
            binding.etCompany.setError("Company is required");
            return false;
        }
        if (preApprovedDate.isEmpty()) {
            binding.etPreApprovedDate.setError("Pre-approved date is required");
            return false;
        }


        return true;
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
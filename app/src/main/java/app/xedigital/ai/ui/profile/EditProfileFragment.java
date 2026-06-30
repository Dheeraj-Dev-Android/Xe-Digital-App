package app.xedigital.ai.ui.profile;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Base64;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Objects;

import app.xedigital.ai.R;
import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.api.APIInterface;
import app.xedigital.ai.databinding.FragmentEditProfileBinding;
import app.xedigital.ai.model.UpdateProfile.UpdateProfileImageResponse;
import app.xedigital.ai.model.user.UserModelResponse;
import app.xedigital.ai.utills.SecurePrefManager;
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

    private File photoFile;
    private String currentPhotoPath;
    private boolean isImageCaptured = false;

//    private final ActivityResultLauncher<Uri> takePictureLauncher = registerForActivityResult(
//            new ActivityResultContracts.TakePicture(),
//            success -> {
//                if (success) {
//                    if (photoFile == null && currentPhotoPath != null) {
//                        photoFile = new File(currentPhotoPath);
//                    }
//                    if (photoFile != null && photoFile.exists()) {
//                        binding.imageViewProfile.setImageURI(Uri.fromFile(photoFile));
//                        isImageCaptured = true;
//                    }
//                } else {
//                    Toast.makeText(requireContext(), "Camera capture cancelled", Toast.LENGTH_SHORT).show();
//                }
//            }
//    );

    public EditProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            currentPhotoPath = savedInstanceState.getString("photo_path");
            isImageCaptured = savedInstanceState.getBoolean("image_captured", false);
            if (currentPhotoPath != null) {
                photoFile = new File(currentPhotoPath);
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("photo_path", currentPhotoPath);
        outState.putBoolean("image_captured", isImageCaptured);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEditProfileBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        apiService = APIClient.getInstance().getUser();
//        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SecurePrefManager prefManager = SecurePrefManager.getInstance(requireContext());
        authToken = prefManager.getString("authToken", "");
        userId = prefManager.getString("userId", "");

        fetchUserProfile(userId, authToken);

        binding.editTextFirstName.setEnabled(false);
        binding.editTextLastName.setEnabled(false);
        binding.editTextEmail.setEnabled(false);
        binding.spinnerRole.setEnabled(false);
        binding.spinnerStatus.setEnabled(false);

//        binding.btnPickImage.setOnClickListener(v -> openCamera());
//
        binding.buttonUpdate.setOnClickListener(v -> {
            if (isValidFormData()) {
                processUnifiedUpdate();
            }
        });

        binding.clearForm.setOnClickListener(v -> clearForm());

//        if (isImageCaptured && photoFile != null && photoFile.exists()) {
//            binding.imageViewProfile.setImageURI(Uri.fromFile(photoFile));
//        }

        return view;
    }

//    private void openCamera() {
//        try {
//            File storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
//            photoFile = File.createTempFile("profile_", ".jpg", storageDir);
//            currentPhotoPath = photoFile.getAbsolutePath();
//
//            Uri photoURI = FileProvider.getUriForFile(requireContext(), requireContext().getPackageName() + ".provider", photoFile);
//            takePictureLauncher.launch(photoURI);
//        } catch (IOException e) {
//            e.printStackTrace();
//            Toast.makeText(requireContext(), "Failed to create storage for photo", Toast.LENGTH_SHORT).show();
//        }
//    }

    private String encodeImageToBase64(File file) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(file.getAbsolutePath(), options);

            final int REQUIRED_SIZE = 1080;
            int scale = 1;
            while (options.outWidth / scale / 2 >= REQUIRED_SIZE && options.outHeight / scale / 2 >= REQUIRED_SIZE) {
                scale *= 2;
            }

            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            Bitmap scaledBitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), o2);

            if (scaledBitmap == null) return null;

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 60, baos);
            byte[] b = baos.toByteArray();

            scaledBitmap.recycle();
            return Base64.encodeToString(b, Base64.NO_WRAP);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void processUnifiedUpdate() {
        String password = Objects.requireNonNull(binding.editTextPassword.getText()).toString().trim();

        if (!isImageCaptured && password.isEmpty()) {
            Toast.makeText(requireContext(), "No modifications detected to save.", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);

        final int totalTasks = (isImageCaptured ? 1 : 0) + (!password.isEmpty() ? 1 : 0);
        final int[] completedTasks = {0};
        final boolean[] globalSuccessFlag = {true};

        // Execution Track A: Profile Image Api Pipeline Destination
        if (isImageCaptured && photoFile != null) {
            String base64PayloadString = encodeImageToBase64(photoFile);
            if (base64PayloadString != null) {
                try {
                    JSONObject imageJsonObject = new JSONObject();
                    imageJsonObject.put("employeeId", userId);
                    imageJsonObject.put("image", base64PayloadString);

                    RequestBody imageBody = RequestBody.create(
                            MediaType.parse("application/json; charset=utf-8"),
                            imageJsonObject.toString()
                    );

                    Call<UpdateProfileImageResponse> imageCall = apiService.updateProfileImage("jwt " + authToken, userId, imageBody);
                    imageCall.enqueue(new Callback<UpdateProfileImageResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<UpdateProfileImageResponse> call, @NonNull Response<UpdateProfileImageResponse> response) {
                            completedTasks[0]++;
                            if (!response.isSuccessful()) {
                                globalSuccessFlag[0] = false;
                            }
                            evaluateTotalAsyncFlowCompletion(completedTasks[0], totalTasks, globalSuccessFlag[0]);
                        }

                        @Override
                        public void onFailure(@NonNull Call<UpdateProfileImageResponse> call, @NonNull Throwable t) {
                            completedTasks[0]++;
                            globalSuccessFlag[0] = false;
                            evaluateTotalAsyncFlowCompletion(completedTasks[0], totalTasks, globalSuccessFlag[0]);
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                    completedTasks[0]++;
                    globalSuccessFlag[0] = false;
                    evaluateTotalAsyncFlowCompletion(completedTasks[0], totalTasks, globalSuccessFlag[0]);
                }
            } else {
                completedTasks[0]++;
                globalSuccessFlag[0] = false;
                evaluateTotalAsyncFlowCompletion(completedTasks[0], totalTasks, globalSuccessFlag[0]);
            }
        }

        // Execution Track B: Password Meta Fields Profile Route Destination
        if (!password.isEmpty()) {
            try {
                JSONObject passwordJsonObject = new JSONObject();
                passwordJsonObject.put("company", companyId);
                passwordJsonObject.put("branch", branchId);
                passwordJsonObject.put("role", roleId);
                passwordJsonObject.put("firstname", Objects.requireNonNull(binding.editTextFirstName.getText()).toString());
                passwordJsonObject.put("lastname", Objects.requireNonNull(binding.editTextLastName.getText()).toString());
                passwordJsonObject.put("email", Objects.requireNonNull(binding.editTextEmail.getText()).toString());

                String statusValue = binding.spinnerStatus.getText() != null ? binding.spinnerStatus.getText().toString() : "";
                passwordJsonObject.put("active", statusValue.equalsIgnoreCase("Active"));

                passwordJsonObject.put("password", password);
                passwordJsonObject.put("confirmPassword", Objects.requireNonNull(binding.editTextConfirmPassword.getText()).toString().trim());

                RequestBody passwordBody = RequestBody.create(
                        MediaType.parse("application/json; charset=utf-8"),
                        passwordJsonObject.toString()
                );

                Call<ResponseBody> passwordCall = apiService.editUserProfile(userId, "jwt " + authToken, passwordBody);
                passwordCall.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                        completedTasks[0]++;
                        if (!response.isSuccessful()) {
                            globalSuccessFlag[0] = false;
                        }
                        evaluateTotalAsyncFlowCompletion(completedTasks[0], totalTasks, globalSuccessFlag[0]);
                    }

                    @Override
                    public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                        completedTasks[0]++;
                        globalSuccessFlag[0] = false;
                        evaluateTotalAsyncFlowCompletion(completedTasks[0], totalTasks, globalSuccessFlag[0]);
                    }
                });

            } catch (JSONException e) {
                e.printStackTrace();
                completedTasks[0]++;
                globalSuccessFlag[0] = false;
                evaluateTotalAsyncFlowCompletion(completedTasks[0], totalTasks, globalSuccessFlag[0]);
            }
        }
    }

    private void evaluateTotalAsyncFlowCompletion(int finishedCount, int expectedTotal, boolean processesValid) {
        if (finishedCount >= expectedTotal) {
            binding.progressBar.setVisibility(View.GONE);
            if (processesValid) {
                Toast.makeText(requireContext(), "Profile updates deployed completely.", Toast.LENGTH_SHORT).show();
                clearForm();
                navigateToProfileFragment();
            } else {
                Toast.makeText(requireContext(), "One or more endpoint writes encountered errors.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void fetchUserProfile(String userId, String authToken) {
        binding.progressBar.setVisibility(View.VISIBLE);
        Call<UserModelResponse> call = apiService.getUserData(userId, "jwt " + authToken);
        call.enqueue(new Callback<UserModelResponse>() {
            @Override
            public void onResponse(@NonNull Call<UserModelResponse> call, @NonNull Response<UserModelResponse> response) {
                binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    UserModelResponse userModel = response.body();
                    if (userModel.getData() != null && userModel.getData().getUser() != null) {
                        binding.editTextFirstName.setText(userModel.getData().getUser().getFirstname());
                        binding.editTextLastName.setText(userModel.getData().getUser().getLastname());
                        binding.editTextEmail.setText(userModel.getData().getUser().getEmail());

                        if (userModel.getData().getRole() != null) {
                            binding.spinnerRole.setText(userModel.getData().getRole().getName());
                            roleId = userModel.getData().getRole().getId();
                        }

                        binding.spinnerStatus.setText(userModel.getData().getUser().isActive() ? "Active" : "Inactive");

                        if (userModel.getData().getCompany() != null) {
                            companyId = userModel.getData().getCompany().getId();
                        }
                        if (userModel.getData().getBranch() != null) {
                            branchId = userModel.getData().getBranch().getId();
                        }
                    } else {
                        showEmptyDataAlert();
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to fetch profile metadata", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserModelResponse> call, @NonNull Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Network error while syncing context", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEmptyDataAlert() {
        if (isAdded() && getContext() != null) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Data Sync Error")
                    .setMessage("User parameters are empty. Please try again later.")
                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss()).show();
        }
    }

    private void clearForm() {
        binding.editTextPassword.setText("");
        binding.editTextConfirmPassword.setText("");
        binding.textInputLayoutPassword.setError(null);
        binding.textInputLayoutConfirmPassword.setError(null);
        photoFile = null;
        currentPhotoPath = null;
        isImageCaptured = false;
//        binding.imageViewProfile.setImageResource(R.mipmap.ic_default_profile);
    }

    private void navigateToProfileFragment() {
        if (isAdded() && getView() != null) {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_nav_edit_profile_to_nav_profile);
        }
    }

    private boolean isValidFormData() {
        String password = Objects.requireNonNull(binding.editTextPassword.getText()).toString().trim();
        String confirmPassword = Objects.requireNonNull(binding.editTextConfirmPassword.getText()).toString().trim();

        binding.textInputLayoutPassword.setError(null);
        binding.textInputLayoutConfirmPassword.setError(null);

        if (password.isEmpty() && confirmPassword.isEmpty()) {
            return true;
        }

        if (password.isEmpty()) {
            binding.textInputLayoutPassword.setError("Password is required");
            return false;
        }

        if (password.length() < 8) {
            binding.textInputLayoutPassword.setError("Password must be at least 8 characters");
            return false;
        }

        if (password.length() > 16) {
            binding.textInputLayoutPassword.setError("Password cannot exceed 16 characters");
            return false;
        }

        if (confirmPassword.isEmpty()) {
            binding.textInputLayoutConfirmPassword.setError("Confirmation required");
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
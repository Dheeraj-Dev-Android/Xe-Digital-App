package app.xedigital.ai.ui.vms;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.google.android.material.datepicker.MaterialDatePicker;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Objects;

import app.xedigital.ai.R;
import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.api.APIInterface;
import app.xedigital.ai.databinding.FragmentVisitorPreApprovedBinding;
import app.xedigital.ai.model.visitorsDetails.VisitorsDetailsResponse;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VisitorPreApprovedFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private FragmentVisitorPreApprovedBinding binding;
    private VisitorPreApprovedViewModel mViewModel;
    private Uri selectedImageUri;
    private String authToken;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    public static VisitorPreApprovedFragment newInstance() {
        return new VisitorPreApprovedFragment();
    }

    private void showLoader() {
        binding.loaderContainer.setVisibility(View.VISIBLE);
    }

    private void hideLoader() {
        binding.loaderContainer.setVisibility(View.GONE);
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
        authToken = sharedPreferences.getString("authToken", "");
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

        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Intent data = result.getData();
                if (data != null && data.getData() != null) {
                    selectedImageUri = data.getData();
                    binding.ivProfile.setImageURI(selectedImageUri);
                    callFaceRecognitionApi(selectedImageUri);
                }
            }
        });

        binding.btnSelectImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });


        binding.etPreApprovedDate.setOnClickListener(v -> {
            long today = Calendar.getInstance().getTimeInMillis();
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker().setTitleText("Select Pre-Approved Date").setSelection(today).setInputMode(MaterialDatePicker.INPUT_MODE_CALENDAR).build();

            datePicker.addOnPositiveButtonClickListener(selection -> {
                // Validate selected date (comparing date parts)
                Calendar selectedCal = Calendar.getInstance();
                selectedCal.setTimeInMillis(selection);

                Calendar todayCal = Calendar.getInstance();

                if (selectedCal.get(Calendar.YEAR) >= todayCal.get(Calendar.YEAR) && selectedCal.get(Calendar.MONTH) >= todayCal.get(Calendar.MONTH) && selectedCal.get(Calendar.DAY_OF_MONTH) >= todayCal.get(Calendar.DAY_OF_MONTH)) {
                    // Format the date and set it to the EditText
                    binding.etPreApprovedDate.setText(datePicker.getHeaderText());
                } else {
                    Toast.makeText(requireContext(), "Please select a future date", Toast.LENGTH_SHORT).show();
                }
            });

            datePicker.show(getParentFragmentManager(), "datePicker");
        });

        binding.btnClear.setOnClickListener(v -> {
            showLoader();
            binding.etContact.setText("");
            binding.etName.setText("");
            binding.etEmail.setText("");
            binding.etCompany.setText("");
            binding.etPreApprovedDate.setText("");
            binding.ivProfile.setImageDrawable(ResourcesCompat.getDrawable(getResources(), android.R.drawable.ic_menu_camera, null));
            hideLoader();

        });
        binding.btnSubmit.setOnClickListener(v -> {
            if (isValidInput()) {
                showLoader();
                String status = Objects.requireNonNull(binding.etStatus.getText()).toString();
                String contact = Objects.requireNonNull(binding.etContact.getText()).toString();
                String name = Objects.requireNonNull(binding.etName.getText()).toString();
                String email = Objects.requireNonNull(binding.etEmail.getText()).toString();
                String company = Objects.requireNonNull(binding.etCompany.getText()).toString();
                String preApprovedDate = Objects.requireNonNull(binding.etPreApprovedDate.getText()).toString();
                Toast.makeText(requireContext(), "Form submitted successfully", Toast.LENGTH_SHORT).show();

            }
        });

        binding.btnCheckContact.setOnClickListener(v -> {
            showLoader();
            String contact = Objects.requireNonNull(binding.etContact.getText()).toString().trim();
            if (contact.isEmpty()) {
                binding.etContact.setError("Contact is required");
                return;
            }


            APIInterface checkContact = APIClient.getInstance().getApi();

            // Make the API call
            Call<VisitorsDetailsResponse> call = checkContact.getVisitorDetail(contact, "jwt " + authToken);
            call.enqueue(new Callback<VisitorsDetailsResponse>() {
                @Override
                public void onResponse(@NonNull Call<VisitorsDetailsResponse> call, @NonNull Response<VisitorsDetailsResponse> response) {
                    hideLoader();
                    if (response.isSuccessful()) {
                        VisitorsDetailsResponse visitorsDetails = response.body();
                        if (visitorsDetails != null && visitorsDetails.getData() != null && visitorsDetails.getData().getVisitor() != null) {
                            Toast.makeText(requireContext(), "Visitor Found", Toast.LENGTH_SHORT).show();
                            binding.etName.setText(visitorsDetails.getData().getVisitor().getName());
                            binding.etEmail.setText(visitorsDetails.getData().getVisitor().getEmail());
                            binding.etCompany.setText(visitorsDetails.getData().getVisitor().getCompanyFrom());

                            // Set the image if available
                            String imageUrl = visitorsDetails.getData().getVisitor().getProfileImagePath();
                            if (imageUrl != null && !imageUrl.isEmpty()) {
                                // Use a library like Glide or Picasso to load the image from the URL
                                Glide.with(requireContext()).load(imageUrl).placeholder(R.drawable.placeholder_image).error(R.drawable.error_image).into(binding.ivProfile);
                            }

                        } else {
                            binding.etContact.setText("");
                            binding.etName.setText("");
                            binding.etEmail.setText("");
                            binding.etCompany.setText("");
                            binding.etPreApprovedDate.setText("");
//                            binding.ivProfile.setImageDrawable(Resources.getSystem().getDrawable(android.R.drawable.ic_menu_camera));

                            binding.ivProfile.setImageDrawable(ResourcesCompat.getDrawable(getResources(), android.R.drawable.ic_menu_camera, null));
                            Toast.makeText(requireContext(), "No Visitor Found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(requireContext(), "Failed to fetch visitor details", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<VisitorsDetailsResponse> call, @NonNull Throwable t) {
                    // Handle network or other errors
                    hideLoader();
                    Log.e("API Failure", "Failure: " + t.getMessage(), t);
                    Toast.makeText(requireContext(), "Failed to fetch visitor details", Toast.LENGTH_SHORT).show();
                }
            });
        });

    }

    private void callFaceRecognitionApi(Uri imageUri) {
        if (imageUri != null) {
            try {
                long imageSizeBytes = getImageSize(imageUri);
                long imageSizeMB = imageSizeBytes / (1024 * 1024);

                if (imageSizeMB > 1) {
                    // Image is larger than 1MB, compress it
                    imageUri = compressImage(imageUri);
                }

                String imageBase64 = imageUriToBase64(imageUri);

                // 2. Create JSON payload
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("collection_name", "cloudffence_y8bzj");
                jsonObject.put("image", imageBase64);

                // 3. Create RequestBody from JSON
                RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonObject.toString());

                // 4. Make the API call (rest of your code remains the same)
                APIInterface apiInterface = APIClient.getInstance().getRecognize();
                Call<ResponseBody> call = apiInterface.VmsFaceRecognitionApi("jwt " + authToken, requestBody);
                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                        // Handle API response
                        if (response.isSuccessful()) {

                            try {
                                assert response.body() != null;
                                Log.d("FaceRecognition", "Success: " + response.body().string());
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        } else {
                            // Error: Handle error response
                            try {
                                assert response.errorBody() != null;
                                Log.e("FaceRecognition", "Error: " + response.errorBody().string());
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                        // Handle API failure
                        Log.e("FaceRecognition", "Failure: " + t.getMessage());
                    }
                });

            } catch (IOException | JSONException e) {
                Log.e("FaceRecognition", "Error creating request body: " + e.getMessage());
            }
        }
    }

    // Helper function to convert image Uri to base64 string
    private String imageUriToBase64(Uri imageUri) throws IOException {
        InputStream inputStream = requireContext().getContentResolver().openInputStream(imageUri);
        byte[] bytes = getBytes(inputStream);
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    // Helper function to get bytes from InputStream
    private byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    // Get image size from Uri
    private long getImageSize(Uri imageUri) throws IOException {
        InputStream inputStream = requireContext().getContentResolver().openInputStream(imageUri);
        assert inputStream != null;
        long size = inputStream.available();
        inputStream.close();
        return size;
    }

    // Compress image and return new Uri
    private Uri compressImage(Uri imageUri) throws IOException {
        // 1. Get Bitmap from Uri
        InputStream inputStream = requireContext().getContentResolver().openInputStream(imageUri);
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
        assert inputStream != null;
        inputStream.close();

        // 2. Compress Bitmap to a ByteArrayOutputStream
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream); // Adjust quality as needed

        // 3. Get new Uri from compressed ByteArrayOutputStream
        String fileName = "compressed_image.jpg";
        Uri compressedImageUri = Uri.parse(MediaStore.Images.Media.insertImage(requireContext().getContentResolver(), bitmap, fileName, null));

        return compressedImageUri;
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
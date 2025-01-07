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
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

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
import app.xedigital.ai.model.preApprovedVisitorRequest.PreApprovedVisitorRequest;
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
    private String authToken, userId, empEmail;
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
        userId = sharedPreferences.getString("userId", "");
        empEmail = sharedPreferences.getString("empEmail", "");
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

                    try {
                        // 1. Get image size and format
                        long imageSizeBytes = getImageSize(selectedImageUri);
                        long imageSizeMB = imageSizeBytes / (1024 * 1024);
                        String imageFormat = getImageFormat(selectedImageUri);

                        // 2. Check size and format
                        if (imageSizeMB <= 1 && isValidFormat(imageFormat)) {
                            // Image is within limits, proceed
                            binding.ivProfile.setImageURI(selectedImageUri);
                            callFaceRecognitionApi(selectedImageUri);
                        } else {
                            // Image is too large or invalid format, display error
                            String errorMessage = imageSizeMB > 1 ? "Image size must be less than 1MB" : "Invalid image format. Only JPG, JPEG, and PNG are allowed.";
                            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException e) {
                        Log.e("ImageCheck", "Error checking image: " + e.getMessage());
                        Toast.makeText(requireContext(), "Error checking image", Toast.LENGTH_SHORT).show();
                    }
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
                submitPreApprovedVisitor(v);
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
            Call<VisitorsDetailsResponse> call = checkContact.getVisitorDetail(contact, "jwt " + authToken);
            call.enqueue(new Callback<VisitorsDetailsResponse>() {
                @Override
                public void onResponse(@NonNull Call<VisitorsDetailsResponse> call, @NonNull Response<VisitorsDetailsResponse> response) {
                    hideLoader();
                    if (response.isSuccessful()) {
                        VisitorsDetailsResponse visitorsDetails = response.body();
                        Log.w("VisitorPreApprovedFragment", "VisitorsDetailsResponse: " + visitorsDetails);
                        if (visitorsDetails != null && visitorsDetails.getData() != null && visitorsDetails.getData().getVisitor() != null) {
                            Toast.makeText(requireContext(), "Visitor Found", Toast.LENGTH_SHORT).show();
                            binding.etName.setText(visitorsDetails.getData().getVisitor().getName());
                            binding.etEmail.setText(visitorsDetails.getData().getVisitor().getEmail());
                            binding.etCompany.setText(visitorsDetails.getData().getVisitor().getCompanyFrom());

                            // Set the image if available
                            String imageUrl = visitorsDetails.getData().getVisitor().getProfileImagePath();
                            if (imageUrl != null && !imageUrl.isEmpty()) {
                                Glide.with(requireContext()).load(imageUrl).placeholder(R.drawable.placeholder_image).error(R.drawable.error_image).into(binding.ivProfile);
                            }

                        } else {
                            binding.etName.setText("");
                            binding.etEmail.setText("");
                            binding.etCompany.setText("");
                            binding.etPreApprovedDate.setText("");
                            binding.ivProfile.setImageDrawable(ResourcesCompat.getDrawable(getResources(), android.R.drawable.ic_menu_camera, null));
                            Toast.makeText(requireContext(), "No Visitor Found,please fill detail", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(requireContext(), "Failed to fetch visitor details", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<VisitorsDetailsResponse> call, @NonNull Throwable t) {
                    hideLoader();
                    Log.e("API Failure", "Failure: " + t.getMessage(), t);
                    Toast.makeText(requireContext(), "Failed to fetch visitor details", Toast.LENGTH_SHORT).show();
                }
            });
        });

    }

    private void submitPreApprovedVisitor(View button) {
        if (isValidInput()) {
            showLoader();
            button.setEnabled(false);

            // Create an instance of PreApprovedVisitorRequest
            PreApprovedVisitorRequest request = new PreApprovedVisitorRequest();
            request.setName(Objects.requireNonNull(binding.etName.getText()).toString());
            request.setEmail(Objects.requireNonNull(binding.etEmail.getText()).toString());
            request.setContact(Objects.requireNonNull(binding.etContact.getText()).toString());
            request.setCompanyFrom(Objects.requireNonNull(binding.etCompany.getText()).toString());
            request.setPreApprovedDate(Objects.requireNonNull(binding.etPreApprovedDate.getText()).toString());

            request.setWhomToMeet(userId);
//            request.setDepartment("60e82cf36ba1893ddb00191d");
            request.setProfileImage("");
//            request.setProfileImagePath("https://visitors-profile-images.s3.ap-south-1.amazonaws.com/814gdko69t.jpg");
            request.setGovernmentIdUploadedImage("");
            request.setGovernmentIdUploadedImagePath("");
            request.setApprovalStatus("approved");
//            request.setApprovalDate("2025-01-06T10:41:33.159Z");
//            request.setCompany("60b47a7c777af75d3f8346b1");
//            request.setActive(true);
            request.setIsPreApproved(true);
            request.setType("create");
//            request.setIsProfileImageDetailFound(false);
//
//
//            FaceData faceData = new FaceData();
//            Face face = new Face();
//            BoundingBox boundingBox = new BoundingBox();
//            boundingBox.setWidth(0.2820533215999603);
//            boundingBox.setHeight(0.4170183539390564);
//            boundingBox.setLeft(0.3498215079307556);
//            boundingBox.setTop(0.14774657785892487);
//            face.setBoundingBox(boundingBox);
//
//            request.setFaceData(faceData);

            APIInterface submitPreApprovedVisitor = APIClient.getInstance().getApi();
            Call<ResponseBody> call = submitPreApprovedVisitor.PreApprovedVisitor("jwt " + authToken, request);

            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    hideLoader();
                    button.setEnabled(true);

                    if (response.isSuccessful()) {
                        Toast.makeText(requireContext(), "Form submitted successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "Failed to submit the form", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    hideLoader();
                    button.setEnabled(true);

                    Toast.makeText(requireContext(), "Failed to submit the form", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void callFaceRecognitionApi(Uri imageUri) {
        if (imageUri != null) {
            try {

                byte[] compressedImageBytes = compressImage(imageUri);
                String imageBase64 = Base64.encodeToString(compressedImageBytes, Base64.DEFAULT);

                // 2. Create JSON payload
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("collection_name", "cloudffence_y8bzj");
                jsonObject.put("image", imageBase64);

                // 3. Create RequestBody from JSON
                RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonObject.toString());

                APIInterface callFaceRecognitionApi = APIClient.getInstance().getRecognize();
                Call<ResponseBody> call = callFaceRecognitionApi.VmsFaceRecognitionApi("jwt " + authToken, requestBody);
                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            try {
                                assert response.body() != null;
                                String faceRecognitionResponse = response.body().string();
                                Log.d("FaceRecognition", "Success: " + faceRecognitionResponse);

                                // Call VisitorFaceDetailApi with the response data
                                JsonObject requestBody = getVisitorFaceDetailRequestBody(faceRecognitionResponse);
                                callVisitorFaceDetailApi(requestBody);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        } else {
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
                        Log.e("FaceRecognition", "Failure: " + t.getMessage());
                    }
                });

            } catch (IOException | JSONException e) {
                Log.e("FaceRecognition", "Error creating request body: " + e.getMessage());
            }
        }
    }

    // Helper method to extract data and create request body
    private JsonObject getVisitorFaceDetailRequestBody(String faceRecognitionResponse) {
        Gson gson = new Gson();
        JsonElement jsonElement = gson.fromJson(faceRecognitionResponse, JsonElement.class);

        if (jsonElement != null && jsonElement.isJsonObject()) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();

            if (jsonObject.has("data") && !jsonObject.get("data").isJsonNull()) {
                JsonElement dataElement = jsonObject.get("data");

                if (dataElement.isJsonObject()) {
                    JsonObject data = dataElement.getAsJsonObject();

                    // Create the request body for callVisitorFaceDetailApi
                    JsonObject requestBody = new JsonObject();
                    if (data.has("Similarity") && !data.get("Similarity").isJsonNull()) {
                        requestBody.add("Similarity", data.get("Similarity"));
                    }
                    if (data.has("Face") && !data.get("Face").isJsonNull()) {
                        requestBody.add("Face", data.get("Face"));
                    }

                    return requestBody;
                }
            }
        }

        // Handle the case where data is null or not a JSON object
        Log.e("VisitorPreApprovedFragment", "Invalid face recognition response: " + faceRecognitionResponse);
        // You might want to throw an exception or return an empty JsonObject here
        return new JsonObject();
    }

    private void callVisitorFaceDetailApi(JsonObject requestBody) {
        String requestBodyJson = new Gson().toJson(requestBody);

        // Create RequestBody from JSON string
        RequestBody requestBodyFinal = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), requestBodyJson);

        APIInterface callVisitorFaceDetailApi = APIClient.getInstance().getFace();
        Call<ResponseBody> call = callVisitorFaceDetailApi.VisitorFaceDetailApi("jwt " + authToken, requestBodyFinal);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        assert response.body() != null;
                        Log.d("VisitorFaceDetail", "Success: " + response.body().string());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    try {
                        assert response.errorBody() != null;
                        Log.e("VisitorFaceDetail", "Error: " + response.errorBody().string());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e("VisitorFaceDetail", "Failure: " + t.getMessage());
            }
        });

    }

    // Helper function to convert image Uri to base64 string
    private String imageUriToBase64(Uri imageUri) throws IOException {
        InputStream inputStream = requireContext().getContentResolver().openInputStream(imageUri);
        assert inputStream != null;
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
    private byte[] compressImage(Uri imageUri) throws IOException {
        // 1. Get Bitmap from Uri
        InputStream inputStream = requireContext().getContentResolver().openInputStream(imageUri);
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
        if (inputStream != null) {
            inputStream.close();
        }

        // 2. Compress Bitmap to a ByteArrayOutputStream
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        if (bitmap != null) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 60, outputStream);
        }

        // 3. Return the compressed byte array
        return outputStream.toByteArray();
    }

    // Helper function to get image format
    private String getImageFormat(Uri imageUri) {
        String mimeType = requireContext().getContentResolver().getType(imageUri);
        if (mimeType != null) {
            return mimeType.substring(mimeType.lastIndexOf("/") + 1);
        }
        return "";
    }

    // Helper function to validate image format
    private boolean isValidFormat(String format) {
        return format.equalsIgnoreCase("jpg") || format.equalsIgnoreCase("jpeg") || format.equalsIgnoreCase("png");
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
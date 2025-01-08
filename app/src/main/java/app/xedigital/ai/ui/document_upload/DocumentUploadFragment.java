package app.xedigital.ai.ui.document_upload;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import app.xedigital.ai.R;
import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.api.APIInterface;
import app.xedigital.ai.databinding.FragmentDocumentUploadBinding;
import app.xedigital.ai.model.profile.Employee;
import app.xedigital.ai.model.uploadDocument.UploadDocumentRequest;
import app.xedigital.ai.ui.profile.ProfileViewModel;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DocumentUploadFragment extends Fragment {

    private static final int PICK_FILE_REQUEST_CODE = 1;
    private FragmentDocumentUploadBinding binding;
    private String imageUrl;
    private String imageKey;
    private String authToken;
    private String authTokenHeader;
    private String userId;
    private String documentCategory;
    private ProfileViewModel profileViewModel;
    private String employeeName;
    private String employeeEmail;
    private String employeeLastName;
    private String employeeId;
    private String company;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        DocumentUploadViewModel documentUploadViewModel = new ViewModelProvider(this).get(DocumentUploadViewModel.class);

        binding = FragmentDocumentUploadBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        authToken = sharedPreferences.getString("authToken", "");
        authTokenHeader = "jwt " + authToken;
        userId = sharedPreferences.getString("userId", "");
        profileViewModel.storeLoginData(userId, authToken);
        profileViewModel.fetchUserProfile();

        profileViewModel.userProfile.observe(getViewLifecycleOwner(), userprofileResponse -> {
            if (userprofileResponse != null && userprofileResponse.getData() != null && userprofileResponse.getData().getEmployee() != null) {
                Employee employee = userprofileResponse.getData().getEmployee();
                employeeName = employee.getFirstname();
                employeeEmail = employee.getEmail();
                employeeLastName = employee.getLastname();
                employeeId = employee.getId();
                company = employee.getCompany();
            }
        });

        String[] categories = {"Aadhaar card", "Pan card", "Certificate 10th", "Certificate 12th", "Cancelled cheque", "Graduation", "Post Graduation", "Other certificates", "Experience letter"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, categories);
        binding.tvDocumentCategory.setAdapter(adapter);

        binding.tvDocumentCategory.setOnItemClickListener((parent, view, position, id) -> documentCategory = categories[position]);

        binding.btnSubmitDocument.setOnClickListener(v -> {
            if (documentCategory != null) {
                uploadDocumentMetadata();
            } else {
                Toast.makeText(requireContext(), "Please select a document and category", Toast.LENGTH_SHORT).show();
            }
        });
        binding.btnClearDocument.setOnClickListener(v -> {
            binding.tvSelectedFile.setText("");
            binding.tvDocumentCategory.setText("");
            binding.tvSelectedFile.setVisibility(View.GONE);
//            binding.btnSubmitDocument.setEnabled(false);
            imageUrl = null;
            imageKey = null;
            Toast.makeText(requireContext(), "Document cleared", Toast.LENGTH_SHORT).show();
        });

        binding.btnUploadDocument.setOnClickListener(view -> {
            if (documentCategory != null) {
                openFilePicker();
            } else {
                Toast.makeText(requireContext(), "Please select a document category first", Toast.LENGTH_SHORT).show();
            }
        });

        return root;
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        startActivityForResult(intent, PICK_FILE_REQUEST_CODE);
    }

    private boolean isValidDocument(Uri uri) {
        String mimeType = requireContext().getContentResolver().getType(uri);
        long fileSize = getFileSize(uri);

        return (mimeType != null && (mimeType.equals("image/jpeg") || mimeType.equals("image/png"))) && (fileSize <= 500 * 1024);
    }

    private long getFileSize(Uri uri) {
        try (Cursor cursor = requireContext().getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                if (!cursor.isNull(sizeIndex)) {
                    return cursor.getLong(sizeIndex);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();

            if (uri != null) {
                if (isValidDocument(uri)) {
                    binding.tvSelectedFile.setText(uri.getPath());
                    binding.tvSelectedFile.setVisibility(View.VISIBLE);
                    binding.btnSubmitDocument.setEnabled(true);
                    convertFileToBase64(uri);

                } else {
                    // Show error message to the user
                    Toast.makeText(requireContext(), "Invalid document format (JPEG or PNG) or size(Must be less than 500kb)", Toast.LENGTH_SHORT).show();
                    binding.tvSelectedFile.setVisibility(View.GONE);
                    binding.btnSubmitDocument.setEnabled(false);
                }
            }
        }
    }

    private void convertFileToBase64(Uri uri) {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
            byte[] fileBytes = getBytes(inputStream);
            String base64String = Base64.encodeToString(fileBytes, Base64.DEFAULT);

            uploadDocument(base64String);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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

    private void uploadDocument(String base64String) {
        APIInterface uploadDocToUri = APIClient.getInstance().getUploadImage();
        try {

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("image", "data:image/png;base64," + base64String);
            jsonObject.put("bucketName", "companies-policy-file");

            RequestBody requestBody = RequestBody.create(okhttp3.MediaType.parse("application/json"), jsonObject.toString());
            Call<ResponseBody> call = uploadDocToUri.uploadDoc(authTokenHeader, requestBody);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        try (ResponseBody responseBody = response.body()) {
                            if (responseBody != null) {
                                String responseBodyString = responseBody.string();
                                JSONObject jsonResponse = new JSONObject(responseBodyString);
                                JSONObject dataObject = jsonResponse.getJSONObject("data");
                                imageUrl = dataObject.getString("imageUrl");
                                imageKey = dataObject.getString("imageKey");
//                                Log.d("uploadDocument Response", "Full Response (JSON): " + responseBodyString);
                                Toast.makeText(requireContext(), "File uploaded successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Log.e("uploadDocument Response", "Response body is null");
                            }
                        } catch (IOException e) {
                            Log.e("uploadDocument Response", "Error reading response body: " + e.getMessage(), e);
                            throw new RuntimeException(e);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        Log.e("uploadDocument Response", "API request failed: " + response.code() + " " + response.message());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
                    Log.e("uploadDocument Response", "API call failed: " + throwable.getMessage(), throwable);
                }
            });
        } catch (JSONException e) {
            Log.e("File Handling", "Error handling file: " + e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void uploadDocumentMetadata() {
        APIInterface apiInterface = APIClient.getInstance().getUploadImage();
        try {
            UploadDocumentRequest uploadDocumentRequest = new UploadDocumentRequest();
            uploadDocumentRequest.setDocumentName(documentCategory);
            uploadDocumentRequest.setImage("");
            uploadDocumentRequest.setDocFileURL(imageUrl);
            uploadDocumentRequest.setDocFileURLKey(imageKey);
            uploadDocumentRequest.setUser(userId);
            uploadDocumentRequest.setEmpFirstName(employeeName);
            uploadDocumentRequest.setEmpLastName(employeeLastName);
            uploadDocumentRequest.setCompany(company);
            uploadDocumentRequest.setEmpEmail(employeeEmail);


            Call<ResponseBody> call = apiInterface.uploadDocument(authTokenHeader, uploadDocumentRequest);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        Navigation.findNavController(requireView()).navigate(R.id.action_nav_document_upload_to_nav_documents);
                        Toast.makeText(requireContext(), "Document metadata uploaded successfully", Toast.LENGTH_SHORT).show();

                    } else {
                        Log.e("Metadata Upload", "Failed: " + response.code() + " " + response.message());
                        Toast.makeText(requireContext(), "Failed to upload document metadata", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    Log.e("Metadata Upload", "Failed: " + t.getMessage(), t);
                    Toast.makeText(requireContext(), "Failed to upload document metadata", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Log.e("Metadata Upload", "Error: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Failed to upload document metadata", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
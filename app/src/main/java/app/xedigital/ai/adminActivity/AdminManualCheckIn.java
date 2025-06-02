package app.xedigital.ai.adminActivity;

import android.Manifest;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;

import app.xedigital.ai.R;
import app.xedigital.ai.adminActivity.VisitorMapper.DepartmentMapper;
import app.xedigital.ai.adminActivity.VisitorMapper.ReportingManagerMapper;
import app.xedigital.ai.adminActivity.VisitorMapper.ShiftMapper;
import app.xedigital.ai.adminApi.AdminAPIClient;
import app.xedigital.ai.adminApi.AdminAPIInterface;
import app.xedigital.ai.model.Admin.Branches.BranchesItem;
import app.xedigital.ai.model.Admin.Branches.CompanyBranchResponse;
import app.xedigital.ai.model.Admin.EmployeeDetails.EmployeeDetailResponse;
import app.xedigital.ai.model.Admin.EmployeeDetails.EmployeesItem;
import app.xedigital.ai.model.Admin.UserDetails.UserDetailsResponse;
import app.xedigital.ai.model.Admin.VisitorManual.FaceData;
import app.xedigital.ai.model.Admin.VisitorManual.VisitorManualRequest;
import app.xedigital.ai.model.Admin.VisitorManual.WhomToMeetItem;
import app.xedigital.ai.model.Admin.addFace.AddFaceResponse;
import app.xedigital.ai.model.Admin.addFace.Data;
import app.xedigital.ai.model.Admin.addFace.Face;
import app.xedigital.ai.model.Admin.addFace.FaceDetail;
import app.xedigital.ai.utills.DateTimeUtils;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminManualCheckIn extends AppCompatActivity {
    private final String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private final Handler handler = new Handler();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final String CollectionName = "cloudfencedemo_wr8c2p";
    private final String bucketName = "visitors-profile-images";
    private final Map<String, EmployeesItem> selectedEmployeeMap = new HashMap<>();
    private final boolean hasDevice = false;
    private String token, userId;
    private PreviewView previewView;
    private ProcessCameraProvider cameraProvider;
    private ImageCapture imageCapture;
    private String uploadedImageUrl = null;
    private String uploadedImageKey = null;

    private Camera camera;
    private FragmentManager fragmentManager;
    private ExecutorService cameraExecutor;
    private TextView captureOverlay;
    private String base64Image;
    private String faceId, imageId;
    private String companyI;
    private String companyName;
    // Form views
    private NestedScrollView formScrollView;
    private TextInputEditText nameEditText, contactEditText, emailEditText, companyEditText, purposeEditText, serialNumber;
    private TextInputLayout serialLayout;
    private AutoCompleteTextView deviceAutoComplete;
    private MultiAutoCompleteTextView whomToMeetAutoComplete;
    private MaterialButton nextButton;
    private String faceDataJson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manual);

        SharedPreferences sharedPreferences = getSharedPreferences("AdminCred", MODE_PRIVATE);
        token = sharedPreferences.getString("authToken", "");
        userId = sharedPreferences.getString("userId", "");
        previewView = findViewById(R.id.previewView);
        captureOverlay = findViewById(R.id.capture_overlay);

        initializeViews();
        setupDropdowns();
        setupClickListeners();
        GetEmployee(token, userId);

        ActivityResultLauncher<String[]> requestPermissionsLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
            boolean allGranted = true;
            for (boolean granted : result.values()) {
                if (!granted) {
                    allGranted = true;
                    break;
                }
            }
            startCamera();
        });

        if (allPermissionsGranted()) {
            startCamera();
        } else {
            requestPermissionsLauncher.launch(REQUIRED_PERMISSIONS);
        }
    }

    private void initializeViews() {

        formScrollView = findViewById(R.id.formScrollView);

        // Form fields
        nameEditText = findViewById(R.id.nameEditText);
        contactEditText = findViewById(R.id.contactEditText);
        emailEditText = findViewById(R.id.emailEditText);
        companyEditText = findViewById(R.id.companyEditText);
        purposeEditText = findViewById(R.id.purposeEditText);
        whomToMeetAutoComplete = findViewById(R.id.whomToMeetAutoComplete);
        serialNumber = findViewById(R.id.serialNumber);
        serialLayout = findViewById(R.id.serialNumberLayout);
        deviceAutoComplete = findViewById(R.id.deviceAutoComplete);
        nextButton = findViewById(R.id.nextButton);
    }

    private void setupDropdowns() {
        String[] deviceOptions = {"Yes", "No"};
        ArrayAdapter<String> deviceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, deviceOptions);
        deviceAutoComplete.setAdapter(deviceAdapter);
        deviceAutoComplete.setText("");

        deviceAutoComplete.setOnItemClickListener((parent, view, position, id) -> {
            String selectedOption = (String) parent.getItemAtPosition(position);
            serialLayout = findViewById(R.id.serialNumberLayout);

            if ("Yes".equalsIgnoreCase(selectedOption)) {
                serialLayout.setVisibility(View.VISIBLE);
            } else {
                serialLayout.setVisibility(View.GONE);
            }
        });
    }

    private void setupClickListeners() {
        nextButton.setOnClickListener(v -> handleFormSubmission());
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        int REQUEST_CODE_PERMISSIONS = 10;
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera();
            }
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                imageCapture = new ImageCapture.Builder().build();
                CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_FRONT).build();

                preview.setSurfaceProvider(previewView.getSurfaceProvider());
                cameraProvider.unbindAll();

                try {
                    camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
                    if (camera == null) {
                        Log.e("AdminManualCheckIn", "Camera is null");
                        showRetryAlert();
                    } else {
                        showCapturingOverlay();
                        handler.postDelayed(this::captureImage, 3000);
                    }
                } catch (IllegalArgumentException e) {
                    Log.e("AdminManualCheckIn", "Error binding camera: " + e.getMessage(), e);
                    showRetryAlert();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void captureImage() {
        File photoFile = new File(getOutputDirectory(), System.currentTimeMillis() + "_photo.jpg");
        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                String savedUri = Uri.fromFile(photoFile).toString();
                String savedImagePath = photoFile.getAbsolutePath();

                try {
                    ProcessCameraProvider cameraProvider = ProcessCameraProvider.getInstance(AdminManualCheckIn.this).get();
                    cameraProvider.unbindAll();
                } catch (ExecutionException | InterruptedException e) {
                    Log.e("AdminManualCheckIn", "Error unbinding camera preview: " + e.getMessage(), e);
                }

                try {
                    AtomicReference<Bitmap> bitmap;
                    bitmap = new AtomicReference<>(BitmapFactory.decodeFile(savedImagePath));

                    try {
                        int newWidth = 500;
                        int newHeight = (int) (bitmap.get().getHeight() * (newWidth / (float) bitmap.get().getWidth()));
                        bitmap.set(Bitmap.createScaledBitmap(bitmap.get(), newWidth, newHeight, false));

                        base64Image = convertImageToBase64(bitmap.get());
                        Log.d("AdminManualCheckIn", "Base64 Image: " + base64Image);
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("collection_name", CollectionName);
                        jsonObject.put("image", base64Image);
                        String requestBodyJson = jsonObject.toString();
                        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), requestBodyJson);
                        sendToAPI(requestBody);
                        addFace(requestBody);

//                        Add Bucket
                        JSONObject jsonObject1 = new JSONObject();
                        jsonObject1.put("bucketName", bucketName);
                        jsonObject1.put("image", base64Image);
                        jsonObject1.put("keyName", "");
                        String requestBodyJson1 = jsonObject1.toString();
                        RequestBody requestBody1 = RequestBody.create(MediaType.parse("application/json"), requestBodyJson1);
                        addBucket(requestBody1);

                        showForm();
                    } catch (Exception e) {
                        Log.e("AdminManualCheckIn", "Error processing image: " + e.getMessage(), e);
                        handleError("Error processing image: " + e.getMessage());
                    }
                } catch (Exception e) {
                    Log.e("AdminManualCheckIn", "Error during face detection: " + e.getMessage(), e);
                    handleError("Error detecting faces. Please try again.");
                }
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Log.e("AdminManualCheckIn", "Photo capture failed: " + exception.getMessage(), exception);
                handleError("Photo capture failed: " + exception.getMessage());
            }
        });
    }

    private void sendToAPI(RequestBody requestBody) {
        AdminAPIInterface apiService = AdminAPIClient.getInstance().getBase1();
        String authToken = "jwt " + token;

        Call<ResponseBody> call = apiService.recognizeFace(authToken, requestBody);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                hideCapturingOverlay();
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseBody = response.body().string();
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        if (jsonResponse.has("data") && !jsonResponse.isNull("data")) {
//                            JSONObject dataObject = jsonResponse.getJSONObject("data");

//                            if (dataObject.has("Face") && !dataObject.isNull("Face")) {
//                                JSONObject faceObject = dataObject.getJSONObject("Face");
//
//                            } else {
//                                Log.e("AdminManualCheckIn", "'Face' object not found in 'data'");
//                                handleError("Face not recognized. Please try again.");
//                            }

                        } else {
                            Log.e("AdminManualCheckIn", "Null or missing 'data' in API response: " + responseBody);
                            Toast.makeText(AdminManualCheckIn.this, "No face data found in response.", Toast.LENGTH_SHORT).show();
                            handleError("No face found.");
                        }
                    } catch (JSONException | IOException e) {
                        Log.e("AdminManualCheckIn", "Error parsing API response: " + e.getMessage(), e);
                        handleError("Error processing response. Please try again.");
                    }

                } else {
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (IOException e) {
                        Log.e("AdminManualCheckIn", "Error reading error body: " + e.getMessage(), e);
                    }
                    Log.e("AdminManualCheckIn", "Recognize Response Error: " + response.code() + " - " + response.message() + "\nError Body: " + errorBody);
                    handleError("Server Error: " + response.code() + " - " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                hideCapturingOverlay();
                handleError("Network Error: " + t.getMessage());
            }
        });
    }

    private void addFace(RequestBody requestBody) {
        AdminAPIInterface apiService = AdminAPIClient.getInstance().getBase1();
        String authToken = "jwt " + token;

        Call<AddFaceResponse> call = apiService.addFace(authToken, requestBody);

        call.enqueue(new Callback<AddFaceResponse>() {
            @Override
            public void onResponse(@NonNull Call<AddFaceResponse> call, @NonNull Response<AddFaceResponse> response) {
                hideCapturingOverlay();

                if (response.isSuccessful() && response.body() != null) {
                    AddFaceResponse faceResponse = response.body();

                    Data data = faceResponse.getData();
                    if (data != null && data.getFace() != null && data.getFaceDetail() != null) {

                        Face face = data.getFace();
                        FaceDetail faceDetail = data.getFaceDetail();

                        faceId = face.getFaceId();
                        imageId = face.getImageId();

                        // Convert to JSON
                        Gson gson = new Gson();
                        JsonObject combinedData = new JsonObject();
                        combinedData.add("Face", gson.toJsonTree(face));
                        combinedData.add("FaceDetail", gson.toJsonTree(faceDetail));

                        // Store for later use in handleFormSubmission
                        faceDataJson = gson.toJson(combinedData);

                    } else {
                        handleError("Face or face details missing.");
                    }

                } else {
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (IOException e) {
                        Log.e("AdminManualCheckIn", "Error reading error body: " + e.getMessage(), e);
                    }

                    handleError("Server Error: " + response.code() + " - " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<AddFaceResponse> call, @NonNull Throwable t) {
                hideCapturingOverlay();
                handleError("Network Error: " + t.getMessage());
            }
        });
    }

    private void addBucket(RequestBody requestBody1) {
        AdminAPIInterface apiService = AdminAPIClient.getInstance().getBase2();
        String authToken = "jwt " + token;

        Call<ResponseBody> call = apiService.addBucket(authToken, requestBody1);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                hideCapturingOverlay();
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseBody = response.body().string();
                        JSONObject jsonResponse = new JSONObject(responseBody);

                        JSONObject dataObject = jsonResponse.optJSONObject("data");
                        if (dataObject != null) {
                            uploadedImageUrl = dataObject.optString("imageUrl", null);
                            uploadedImageKey = dataObject.optString("imageKey", null);
                        }

                        if (uploadedImageUrl == null || uploadedImageKey == null) {
                            Toast.makeText(getApplicationContext(), "Image upload succeeded, but image URL or key is missing.", Toast.LENGTH_LONG).show();
                        }

                    } catch (JSONException | IOException e) {
                        Log.e("AdminManualCheckIn", "Error parsing API response: " + e.getMessage(), e);
                        handleError("Error processing response. Please try again.");
                    }

                } else {
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (IOException e) {
                        Log.e("AdminManualCheckIn", "Error reading error body: " + e.getMessage(), e);
                    }
                    Log.e("AdminManualCheckIn", "Recognize Response Error: " + response.code() + " - " + response.message() + "\nError Body: " + errorBody);
                    handleError("Server Error: " + response.code() + " - " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                hideCapturingOverlay();
                handleError("Network Error: " + t.getMessage());
            }
        });
    }

    private void GetEmployee(String authToken, String userId) {
        String token = "jwt " + authToken;
        AdminAPIInterface employeeDetails = AdminAPIClient.getInstance().getBase2();

        retrofit2.Call<UserDetailsResponse> employees = employeeDetails.getUser(token, userId);

        employees.enqueue(new Callback<UserDetailsResponse>() {
            @Override
            public void onResponse(@NonNull Call<UserDetailsResponse> call, @NonNull Response<UserDetailsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserDetailsResponse userDetailResponse = response.body();

                    if (userDetailResponse.isSuccess()) {
                        String CompanyId = userDetailResponse.getData().getCompany().getId();
                        getBranches(token, CompanyId);


                    } else {
                        Toast.makeText(AdminManualCheckIn.this, "Failed to retrieve employee details", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(AdminManualCheckIn.this, "Error in employee details response", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserDetailsResponse> call, @NonNull Throwable throwable) {
                Toast.makeText(AdminManualCheckIn.this, "Failed to get employee data: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getBranches(String token, String companyId) {
        AdminAPIInterface getBranches = AdminAPIClient.getInstance().getBase2();

        retrofit2.Call<CompanyBranchResponse> branches = getBranches.getBranches(token, companyId);
        branches.enqueue(new Callback<CompanyBranchResponse>() {
            @Override
            public void onResponse(@NonNull Call<CompanyBranchResponse> call, @NonNull Response<CompanyBranchResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CompanyBranchResponse companyBranchResponse = response.body();
                    List<BranchesItem> branches = companyBranchResponse.getData().getBranches();

                    if (branches != null && !branches.isEmpty()) {
                        BranchesItem firstBranch = branches.get(0);

                        companyI = firstBranch.getCompany();
                        companyName = firstBranch.getName();

//                        Log.d("AdminManualCheckIn", "Company ID: " + companyId);
//                        Log.d("AdminManualCheckIn", "Company Name: " + companyName);
                    }

                    fetchEmployees(token);
                } else {
                    handleError("Failed to get a valid response.");
                }
            }


            @Override
            public void onFailure(@NonNull Call<CompanyBranchResponse> call, @NonNull Throwable throwable) {
                Toast.makeText(AdminManualCheckIn.this, "Failed to get branches: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("AdminManualCheckIn", "Error getting branches: " + throwable.getMessage(), throwable);
                handleError("Error getting branches: " + throwable.getMessage());

            }
        });
    }

    private void fetchEmployees(String token) {
        AdminAPIInterface apiService = AdminAPIClient.getInstance().getBase2();
        Call<EmployeeDetailResponse> call = apiService.getEmployees(token);

        call.enqueue(new Callback<EmployeeDetailResponse>() {
            @Override
            public void onResponse(@NonNull Call<EmployeeDetailResponse> call, @NonNull Response<EmployeeDetailResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<EmployeesItem> employees = response.body().getData().getEmployees();
                    List<EmployeeDropdownItem> employeeDropdownItems = new ArrayList<>();

                    for (EmployeesItem employee : employees) {
                        employeeDropdownItems.add(new EmployeeDropdownItem(employee.getId(), employee.getFirstname(), employee.getLastname(), employee));
                    }

                    CustomEmployeeAdapter adapter = new CustomEmployeeAdapter(AdminManualCheckIn.this, employeeDropdownItems, selectedEmployeeMap.keySet());

                    whomToMeetAutoComplete.setAdapter(adapter);
                    whomToMeetAutoComplete.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
                    whomToMeetAutoComplete.setThreshold(1);
                    whomToMeetAutoComplete.setText("");

                    whomToMeetAutoComplete.setOnClickListener(v -> adapter.getFilter().filter(""));
                    whomToMeetAutoComplete.setOnFocusChangeListener((v, hasFocus) -> {
                        if (hasFocus) adapter.getFilter().filter("");
                    });

                    whomToMeetAutoComplete.setOnItemClickListener((parent, view, position, id) -> {
                        EmployeeDropdownItem selected = adapter.getItem(position);
                        if (selected == null) return;

                        String fullName = selected.getFirstName() + " " + selected.getLastName();
                        String employeeId = selected.getId();

                        if (selectedEmployeeMap.containsKey(employeeId)) {
                            // Unselect
                            selectedEmployeeMap.remove(employeeId);

                            String currentText = whomToMeetAutoComplete.getText().toString();
                            String updatedText = currentText.replace(fullName + ", ", "").replace(fullName, "");
                            whomToMeetAutoComplete.setText(updatedText.trim());
                            whomToMeetAutoComplete.setSelection(whomToMeetAutoComplete.getText().length());

                        } else {
                            // Select
                            selectedEmployeeMap.put(employeeId, selected.getEmployeeItem());

                            String currentText = whomToMeetAutoComplete.getText().toString();
                            if (!currentText.contains(fullName)) {
                                if (!currentText.isEmpty() && !currentText.endsWith(",")) {
                                    currentText += ", ";
                                }
                                whomToMeetAutoComplete.setText(currentText + fullName + ", ");
                                whomToMeetAutoComplete.setSelection(whomToMeetAutoComplete.getText().length());
                            }
                        }

                        adapter.notifyDataSetChanged();
                    });


                } else {
                    Toast.makeText(AdminManualCheckIn.this, "Failed to get employees, please try again.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<EmployeeDetailResponse> call, @NonNull Throwable t) {
                Toast.makeText(AdminManualCheckIn.this, "Failed to get employees, please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void submitFormData(String name, long contact, String email, String company, String whomToMeetStr, String purpose, boolean hasDevice, String serialNo, List<EmployeesItem> selectedEmployees, String faceDataJson, String imageUrl, String imageKey) {

        nextButton.setEnabled(false);
        nextButton.setText("SUBMITTING...");

        VisitorManualRequest request = new VisitorManualRequest();

        request.setName(name);
        request.setContact(contact);
        request.setEmail(email);
        request.setCompanyFrom(company);
        request.setPurposeOfmeeting(purpose);
        request.setIsLaptop(String.valueOf(hasDevice));
        request.setSerialNumber(serialNo);
        request.setSignIn(DateTimeUtils.getCurrentUtcTimestamp());

        // Fill with default/empty values
        request.setApprovalStatus("");
        request.setEmpcontact(null);
        request.setEmpfirstname(null);
        request.setEmplastname(null);
        request.setGovernmentIdUploadedImage("");
        request.setGovernmentIdUploadedImagePath("");
        request.setItemImageUploadedPath("");
        request.setItemUploadedImage("");
        request.setCompany(companyI);
        request.setCompanyName(companyName);
        request.setProfileImage(base64Image);
        request.setProfileImagePath(imageUrl);
        request.setProfileImageKey(imageKey);
        request.setSignatureImagePath("");
        request.setVisitorCategory("");
        request.setVisitorVisit("");


        // ✅ Set list of WhomToMeetItem
        List<WhomToMeetItem> whomToMeetList = new ArrayList<>();
        for (EmployeesItem employee : selectedEmployees) {
            WhomToMeetItem whom = new WhomToMeetItem();
            whom.setId(employee.getId());
            whom.setFirstname(employee.getFirstname());
            whom.setLastname(employee.getLastname());
            whom.setEmail(employee.getEmail());
            whom.setContact(employee.getContact());
            whom.setCompany(employee.getCompany());
            whom.setDesignation(employee.getDesignation());
            whom.setEmployeeCode(employee.getEmployeeCode());
            whom.setJoiningDate(employee.getJoiningDate());
            whom.setJoiningType(employee.getJoiningType());
            whom.setIsVerified(employee.isIsVerified());
            whom.setIsHROrAdmin(employee.isIsHROrAdmin());
            whom.setProfileImageUrl(employee.getProfileImageUrl());
            whom.setLevel(employee.getLevel());
            whom.setActive(employee.isActive());
            whom.setDateOfBirth(employee.getDateOfBirth());
            whom.setEmployeeType(employee.getEmployeeType());
            whom.setGrade(employee.getGrade());
            whom.setCrossmanager(employee.getCrossmanager());
            whom.setCreatedAt(employee.getCreatedAt());
            whom.setUpdatedAt(employee.getUpdatedAt());
            whom.setV(employee.getV());
            // Use mappers to convert EmployeeDetails objects to VisitorManual objects
            whom.setDepartment(DepartmentMapper.mapFromEmployeeDetailsDepartment(employee.getDepartment()));
            whom.setShift(ShiftMapper.mapFromEmployeeDetailsShift(employee.getShift()));
            whom.setReportingManager(ReportingManagerMapper.mapFromEmployeeDetailsReportingManager(employee.getReportingManager()));
            // ✅ Deserialize and assign face data
            if (faceDataJson != null && !faceDataJson.isEmpty()) {
                try {
                    Gson gson = new Gson();
                    FaceData faceData = gson.fromJson(faceDataJson, FaceData.class);
                    request.setFaceData(faceData);
                } catch (JsonSyntaxException e) {
                    Log.e("AdminManualCheckIn", "Invalid face data JSON: " + e.getMessage());
                }
            }

            whomToMeetList.add(whom);
        }

        request.setWhomToMeet(whomToMeetList);

        AdminAPIInterface apiService = AdminAPIClient.getInstance().getBase2();
        String authToken = "jwt " + token;

        Call<ResponseBody> call = apiService.ManualVisitor(authToken, request);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                nextButton.setEnabled(true);
                nextButton.setText("NEXT");

                if (response.isSuccessful()) {
                    Toast.makeText(AdminManualCheckIn.this, "Form submitted successfully!", Toast.LENGTH_SHORT).show();
                    setResult(Activity.RESULT_OK);
                    finish();
                } else {
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (IOException e) {
                        Log.e("AdminManualCheckIn", "Error reading error body: " + e.getMessage(), e);
                    }
                    Log.e("AdminManualCheckIn", "Form submission error: " + response.code() + " - " + response.message() + "\nError Body: " + errorBody);
                    Toast.makeText(AdminManualCheckIn.this, "Failed to submit form. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                nextButton.setEnabled(true);
                nextButton.setText("NEXT");
                Log.e("AdminManualCheckIn", "Form submission network error: " + t.getMessage(), t);
                Toast.makeText(AdminManualCheckIn.this, "Network error. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }


//    private void submitFormData(String name, long contact, String email, String company, String whomToMeetStr, String purpose, boolean hasDevice, String serialNo) {
//        nextButton.setEnabled(false);
//        nextButton.setText("SUBMITTING...");
//
//        VisitorManualRequest request = new VisitorManualRequest();
//
//        // Required basic fields
//        request.setName(name);
//        request.setContact(contact);
//        request.setEmail(email);
//        request.setCompanyFrom(company);
//        request.setPurposeOfmeeting(purpose);
//        request.setIsLaptop(String.valueOf(hasDevice));
//        request.setSerialNumber(serialNo);
//        request.setSignIn(DateTimeUtils.getCurrentUtcTimestamp());
//
//        request.setApprovalStatus("");
//        request.setEmpcontact(null);
//        request.setEmpfirstname(null);
//        request.setEmplastname(null);
//        request.setGovernmentIdUploadedImage("");
//        request.setGovernmentIdUploadedImagePath("");
//        request.setItemImageUploadedPath("");
//        request.setItemUploadedImage("");
//        request.setCompany(companyI);
//        request.setCompanyName(companyName);
//        request.setItemUploadedImage("");
//        request.setItemImageUploadedPath("");
//        request.setProfileImage(base64Image);
//        request.setProfileImagePath("");
//        request.setProfileImageKey("");
//
//        request.setSignatureImagePath("");
//        request.setVisitorCategory("");
//        request.setVisitorVisit("");
//
//
//        // --- Set FaceData
//        Face face = new Face();
//        FaceDetail faceDetail = new FaceDetail();
//        FaceData faceData = new FaceData();
//
//        // --- Set whomToMeet list
//        List<WhomToMeetItem> whomToMeetList = new ArrayList<>();
//        WhomToMeetItem whomToMeet = new WhomToMeetItem();
//        whomToMeetList.add(whomToMeet);
//        request.setWhomToMeet(whomToMeetList);
//
//        // --- Now make the API call
//        AdminAPIInterface apiService = AdminAPIClient.getInstance().getBase2();
//        String authToken = "jwt " + token;
//
//        Call<ResponseBody> call = apiService.ManualVisitor(authToken, request);
//
//        call.enqueue(new Callback<ResponseBody>() {
//            @Override
//            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
//                nextButton.setEnabled(true);
//                nextButton.setText("NEXT");
//
//                if (response.isSuccessful()) {
//                    Toast.makeText(AdminManualCheckIn.this, "Form submitted successfully!", Toast.LENGTH_SHORT).show();
//                    setResult(Activity.RESULT_OK);
//                    finish();
//                } else {
//                    String errorBody = "";
//                    try {
//                        if (response.errorBody() != null) {
//                            errorBody = response.errorBody().string();
//                        }
//                    } catch (IOException e) {
//                        Log.e("AdminManualCheckIn", "Error reading error body: " + e.getMessage(), e);
//                    }
//                    Log.e("AdminManualCheckIn", "Form submission error: " + response.code() + " - " + response.message() + "\nError Body: " + errorBody);
//                    Toast.makeText(AdminManualCheckIn.this, "Failed to submit form. Please try again.", Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
//                nextButton.setEnabled(true);
//                nextButton.setText("NEXT");
//                Log.e("AdminManualCheckIn", "Form submission network error: " + t.getMessage(), t);
//                Toast.makeText(AdminManualCheckIn.this, "Network error. Please try again.", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }

    private void showForm() {
        try {
            ProcessCameraProvider cameraProvider = ProcessCameraProvider.getInstance(AdminManualCheckIn.this).get();
            cameraProvider.unbindAll();
        } catch (ExecutionException | InterruptedException e) {
            Log.e("AdminCheckOutActivity", "Error unbinding camera preview: " + e.getMessage(), e);
        }
        runOnUiThread(() -> {

            previewView.setVisibility(View.GONE);
            formScrollView.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Face recognized! Please fill the form.", Toast.LENGTH_SHORT).show();
        });
    }

    private void handleFormSubmission() {
        if (validateForm()) {
            String name = nameEditText.getText().toString().trim();
            String contactStr = contactEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();
            String company = companyEditText.getText().toString().trim();
            String whomToMeetStr = whomToMeetAutoComplete.getText().toString().trim();
            String purpose = purposeEditText.getText().toString().trim();
            String device = deviceAutoComplete.getText().toString().trim();
            String serialNo = serialNumber.getText().toString().trim();

            boolean hasDevice = "Yes".equalsIgnoreCase(device);

            try {
                long contact = Long.parseLong(contactStr);
                List<EmployeesItem> selectedEmployees = new ArrayList<>(selectedEmployeeMap.values());
                if (faceDataJson == null || faceDataJson.isEmpty()) {
                    Toast.makeText(this, "Please capture face data first.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (uploadedImageUrl == null || uploadedImageKey == null) {
                    Toast.makeText(this, "Please upload image first.", Toast.LENGTH_SHORT).show();
                    return;
                }

                submitFormData(name, contact, email, company, whomToMeetStr, purpose, hasDevice, serialNo, selectedEmployees, faceDataJson, uploadedImageUrl, uploadedImageKey);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid contact number", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private boolean validateForm() {
        boolean isValid = true;

        // Validate name
        if (nameEditText.getText().toString().trim().isEmpty()) {
            nameEditText.setError("Name is required");
            isValid = false;
        }

        // Validate contact
        String contact = contactEditText.getText().toString().trim();
        if (contact.isEmpty()) {
            contactEditText.setError("Contact number is required");
            isValid = false;
        } else if (contact.length() < 10) {
            contactEditText.setError("Please enter a valid contact number");
            isValid = false;
        }

        // Validate email
        String email = emailEditText.getText().toString().trim();
        if (email.isEmpty()) {
            emailEditText.setError("Email is required");
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Please enter a valid email address");
            isValid = false;
        }

        // Validate company
        if (companyEditText.getText().toString().trim().isEmpty()) {
            companyEditText.setError("Company name is required");
            isValid = false;
        }

        // Validate whom to meet
        if (whomToMeetAutoComplete.getText().toString().trim().isEmpty()) {
            whomToMeetAutoComplete.setError("Please select whom to meet");
            isValid = false;
        }

        // Validate purpose
        if (purposeEditText.getText().toString().trim().isEmpty()) {
            purposeEditText.setError("Purpose of meeting is required");
            isValid = false;
        }

        // Validate device selection
        if (deviceAutoComplete.getText().toString().trim().isEmpty()) {
            deviceAutoComplete.setError("Please select device option");
            isValid = false;
        }

        return isValid;
    }

    private String convertImageToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.NO_WRAP);
    }

    private void handleError(String message) {
        new MaterialAlertDialogBuilder(this).setTitle("Error").setMessage(message).setPositiveButton("Retry", (dialog, which) -> {
            dialog.dismiss();
            // Reset views and restart camera
            formScrollView.setVisibility(View.GONE);
            previewView.setVisibility(View.VISIBLE);
            startCamera();
        }).setNegativeButton("Cancel", (dialog, which) -> {
            dialog.dismiss();
            finish();
        }).show();
    }

    private File getOutputDirectory() {
        File mediaDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (mediaDir != null && mediaDir.exists()) return mediaDir;
        return getFilesDir();
    }

    private void showRetryAlert() {
        new MaterialAlertDialogBuilder(this).setTitle("Camera Not Found").setMessage("No camera found or selected. Please check your device and try again.").setPositiveButton("Retry", (dialog, which) -> {
            dialog.dismiss();
            startCamera();
        }).setNegativeButton("Cancel", (dialog, which) -> {
            dialog.dismiss();
            setResult(Activity.RESULT_CANCELED);
            finish();
        }).show();
    }

    private void showCapturingOverlay() {
        runOnUiThread(() -> {
            captureOverlay.setVisibility(TextView.VISIBLE);
            Animation fadeAnim = new AlphaAnimation(0.2f, 1.0f);
            fadeAnim.setDuration(800);
            fadeAnim.setRepeatMode(Animation.REVERSE);
            fadeAnim.setRepeatCount(Animation.INFINITE);
            captureOverlay.startAnimation(fadeAnim);
        });
    }

    private void hideCapturingOverlay() {
        runOnUiThread(() -> {
            captureOverlay.clearAnimation();
            captureOverlay.setVisibility(TextView.GONE);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
    }
}
package app.xedigital.ai.ui.onboarding;

import android.app.Application;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.api.APIInterface;
import app.xedigital.ai.model.employeeOnboarding.EmployeeOnboardingResponse;
import app.xedigital.ai.ui.onboarding.model.FamilyMember;
import app.xedigital.ai.ui.onboarding.model.OnboardingFormModel;
import app.xedigital.ai.ui.onboarding.model.OtherDocument;
import app.xedigital.ai.utills.SecurePrefManager;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EmployeeOnboardingViewModel extends AndroidViewModel {

    private final MutableLiveData<OnboardingFormModel> draftLiveData = new MutableLiveData<>(new OnboardingFormModel());
    private final MutableLiveData<EmployeeOnboardingResponse> fetchResponse = new MutableLiveData<>();
    private final MutableLiveData<Boolean> submitSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    private final APIInterface apiInterface = APIClient.getInstance().getApi();

    public EmployeeOnboardingViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<OnboardingFormModel> getDraftLiveData() {
        return draftLiveData;
    }

    public LiveData<EmployeeOnboardingResponse> getFetchResponse() {
        return fetchResponse;
    }

    public LiveData<Boolean> getSubmitSuccess() {
        return submitSuccess;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void fetchOnboardingDetails(String authToken, String userId) {
        isLoading.setValue(true);
        apiInterface.getOnBoardEmployeeById(authToken, userId).enqueue(new Callback<EmployeeOnboardingResponse>() {
            @Override
            public void onResponse(@NonNull Call<EmployeeOnboardingResponse> call, @NonNull Response<EmployeeOnboardingResponse> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    fetchResponse.setValue(response.body());
                } else {
                    errorMessage.setValue("Unable to load onboarding progress.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<EmployeeOnboardingResponse> call, @NonNull Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue("Network failure: " + t.getMessage());
            }
        });
    }

    public void saveDraft(OnboardingFormModel model) {
        model.setDraft(true);
        draftLiveData.setValue(model);
        executeOnboardingPost(model);
    }

    public void submitForm(OnboardingFormModel model) {
        model.setDraft(false);
        executeOnboardingPost(model);
    }

    private void executeOnboardingPost(OnboardingFormModel model) {
        isLoading.setValue(true);
        SecurePrefManager prefs = SecurePrefManager.getInstance(getApplication());
        String token = "jwt " + prefs.getString("authToken", "");
        String userId = prefs.getString("userId", "");

        if (userId.isEmpty()) {
            errorMessage.setValue("Session expired. Please log in again.");
            isLoading.setValue(false);
            return;
        }

        Map<String, RequestBody> fields = new HashMap<>();
        fields.put("userId", createPartFromString(userId));
        fields.put("isDraft", createPartFromString(String.valueOf(model.isDraft())));
        fields.put("fullName", createPartFromString(model.getFullName()));
        fields.put("dob", createPartFromString(model.getDob()));
        fields.put("gender", createPartFromString(model.getGender()));
        fields.put("fatherOrHusbandName", createPartFromString(model.getFatherOrHusbandName()));
        fields.put("maritalStatus", createPartFromString(model.getMaritalStatus()));
        fields.put("bloodGroup", createPartFromString(model.getBloodGroup()));
        fields.put("nationality", createPartFromString(model.getNationality()));
        fields.put("personalMobile", createPartFromString(model.getPersonalMobile()));
        fields.put("alternateMobile", createPartFromString(model.getAlternateMobile()));
        fields.put("personalEmail", createPartFromString(model.getPersonalEmail()));
        fields.put("currentAddress", createPartFromString(model.getCurrentAddress()));
        fields.put("permanentAddress", createPartFromString(model.getPermanentAddress()));
        fields.put("city", createPartFromString(model.getCity()));
        fields.put("state", createPartFromString(model.getState()));
        fields.put("pincode", createPartFromString(model.getPincode()));
        fields.put("country", createPartFromString(model.getCountry()));

        fields.put("emergencyName", createPartFromString(model.getEmergencyName()));
        fields.put("emergencyRelationship", createPartFromString(model.getEmergencyRelationship()));
        fields.put("emergencyNumber", createPartFromString(model.getEmergencyNumber()));
        fields.put("emergencyAlternateNumber", createPartFromString(model.getEmergencyAlternateNumber()));
        fields.put("emergencyAddress", createPartFromString(model.getEmergencyAddress()));

        fields.put("bankName", createPartFromString(model.getBankName()));
        fields.put("branchName", createPartFromString(model.getBranchName()));
        fields.put("accountNumber", createPartFromString(model.getAccountNumber()));
        fields.put("ifscCode", createPartFromString(model.getIfscCode()));
        fields.put("accountHolderName", createPartFromString(model.getAccountHolderName()));
        fields.put("upiId", createPartFromString(model.getUpiId()));

        fields.put("aadhaarNumber", createPartFromString(model.getAadhaarNumber()));
        fields.put("panNumber", createPartFromString(model.getPanNumber()));
        fields.put("uanNumber", createPartFromString(model.getUanNumber()));
        fields.put("esiNumber", createPartFromString(model.getEsiNumber()));
        fields.put("passportNumber", createPartFromString(model.getPassportNumber()));
        fields.put("passportExpiryDate", createPartFromString(model.getPassportExpiryDate()));

        fields.put("nomineeName", createPartFromString(model.getNomineeName()));
        fields.put("nomineeRelationship", createPartFromString(model.getNomineeRelationship()));
        fields.put("nomineeDob", createPartFromString(model.getNomineeDob()));
        fields.put("sharePercentage", createPartFromString(model.getSharePercentage()));

        fields.put("medicalCondition", createPartFromString(model.getMedicalCondition()));
        fields.put("medicalConditionDetails", createPartFromString(model.getMedicalConditionDetails()));
        fields.put("knownAllergies", createPartFromString(model.getKnownAllergies()));

        Gson gson = new Gson();
        fields.put("familyDetailsMetadata", createPartFromString(gson.toJson(model.getFamilyDetails())));
        fields.put("documentsMetadata", createPartFromString(gson.toJson(model.getDocuments())));

        // Convert files using context and local helper methods directly
        MultipartBody.Part aadhaarFrontPart = prepareFilePart("aadhaarFront", model.getAadhaarFrontFile());
        MultipartBody.Part aadhaarBackPart = prepareFilePart("aadhaarBack", model.getAadhaarBackFile());
        MultipartBody.Part panPart = prepareFilePart("pan", model.getPanFile());

        List<MultipartBody.Part> familyProofs = new ArrayList<>();
        if (model.getFamilyDetails() != null) {
            for (int i = 0; i < model.getFamilyDetails().size(); i++) {
                FamilyMember member = model.getFamilyDetails().get(i);
                if (member.getAddressProofFile() != null) {
                    familyProofs.add(prepareFilePart("familyProof_" + i, member.getAddressProofFile()));
                }
            }
        }

        List<MultipartBody.Part> otherDocs = new ArrayList<>();
        if (model.getDocuments() != null) {
            for (int i = 0; i < model.getDocuments().size(); i++) {
                OtherDocument doc = model.getDocuments().get(i);
                if (doc.getDocumentFile() != null) {
                    otherDocs.add(prepareFilePart("otherDoc_" + i, doc.getDocumentFile()));
                }
            }
        }

//        apiInterface.submitOnboarding(token, fields, aadhaarFrontPart, aadhaarBackPart, panPart, familyProofs, otherDocs).enqueue(new Callback<EmployeeOnboardingResponse>() {
//            @Override
//            public void onResponse(@NonNull Call<EmployeeOnboardingResponse> call, @NonNull Response<EmployeeOnboardingResponse> response) {
//                isLoading.setValue(false);
//                if (response.isSuccessful() && response.body() != null) {
//                    submitSuccess.setValue(true);
//                } else {
//                    errorMessage.setValue("Failed to update onboarding form. (Error: " + response.code() + ")");
//                }
//            }
//
//            @Override
//            public void onFailure(@NonNull Call<EmployeeOnboardingResponse> call, @NonNull Throwable t) {
//                isLoading.setValue(false);
//                errorMessage.setValue("Error posting details: " + t.getMessage());
//            }
//        });
    }

    // =========================================================================
    // INLINE FILE CONVERSION METHODS (Replaces RetrofitUtils)
    // =========================================================================
    private RequestBody createPartFromString(String value) {
        if (value == null) value = "";
        return RequestBody.create(value, MediaType.parse("multipart/form-data"));
    }

    private MultipartBody.Part prepareFilePart(String partName, Uri fileUri) {
        if (fileUri == null) return null;
        try {
            String fileName = resolveFileName(fileUri);
            String mimeType = getApplication().getContentResolver().getType(fileUri);
            if (mimeType == null) {
                mimeType = "image/jpeg";
            }

            InputStream inputStream = getApplication().getContentResolver().openInputStream(fileUri);
            if (inputStream == null) return null;

            File tempFile = new File(getApplication().getCacheDir(), fileName);
            try (FileOutputStream outputStream = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
            inputStream.close();

            RequestBody requestFile = RequestBody.create(tempFile, MediaType.parse(mimeType));
            return MultipartBody.Part.createFormData(partName, fileName, requestFile);
        } catch (Exception e) {
            Log.e("OnboardingViewModel", "Error processing file: " + partName, e);
            return null;
        }
    }

    private String resolveFileName(Uri uri) {
        String name = null;
        if ("content".equals(uri.getScheme())) {
            try (Cursor c = getApplication().getContentResolver().query(uri, null, null, null, null)) {
                if (c != null && c.moveToFirst()) {
                    int idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (idx >= 0) name = c.getString(idx);
                }
            } catch (Exception ignored) {
            }
        }
        if (name == null) {
            name = uri.getLastPathSegment();
        }
        return name != null ? name : "temp_upload_file.jpg";
    }
}
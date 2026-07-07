package app.xedigital.ai.ui.claim_management;

import android.app.Application;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.api.APIInterface;
import app.xedigital.ai.model.EmployeeByBusinessUnit.EmployeeByBusinessUnitResponse;
import app.xedigital.ai.model.Food.FoodRequest;
import app.xedigital.ai.model.allEmployee.AllEmployeeResponse;
import app.xedigital.ai.model.branch.UserBranchResponse;
import app.xedigital.ai.model.businessUnit.BusItem;
import app.xedigital.ai.model.businessUnit.BusinessUnitResponse;
import app.xedigital.ai.model.businessUnit.BusinessUnitSpinnerItem;
import app.xedigital.ai.model.claimLength.ClaimLengthResponse;
import app.xedigital.ai.model.claimPrice.ClaimPriceResponse;
import app.xedigital.ai.model.claimSave.ClaimSaveRequest;
import app.xedigital.ai.model.claimSubmit.ClaimUpdateRequest;
import app.xedigital.ai.model.profile.Employee;
import app.xedigital.ai.model.user.UserModelResponse;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ClaimManagementViewModel extends AndroidViewModel {

    // UI Dropdowns
    public final LiveData<List<String>> meetingTypes = new MutableLiveData<>(Arrays.asList("Select an option", "Business", "Project", "Pre Sales"));
    public final LiveData<List<String>> claimCategories = new MutableLiveData<>(Arrays.asList("Select an option", "General", "Standard"));
    public final LiveData<List<String>> travelCategories = new MutableLiveData<>(Arrays.asList("Select an option", "Local", "Domestic", "International"));
    public final LiveData<List<String>> transportModes = new MutableLiveData<>(Arrays.asList("Select an option", "Shared", "Dedicated"));
    public final LiveData<List<String>> sharedTransportModes = new MutableLiveData<>(Arrays.asList("Select an option", "Auto", "Car", "E-Rickshaw", "Metro", "Others"));
    public final LiveData<List<String>> dedicatedTransportModes = new MutableLiveData<>(Arrays.asList("Select an option", "Two-Wheeler", "Three-Wheeler", "Others"));
    public final LiveData<List<String>> currencyDropdown = new MutableLiveData<>(Arrays.asList("Select an option", "INR", "USD", "EUR", "GBP", "JPY", "CNY", "AUD", "CAD", "CHF", "HKD", "SEK", "NZD"));
    public final MutableLiveData<List<BusinessUnitSpinnerItem>> businessUnitsSpinnerData = new MutableLiveData<>();
    public final MutableLiveData<String> selectedBusinessUnitId = new MutableLiveData<>("");
    public final MutableLiveData<List<String>> employeesList = new MutableLiveData<>(new ArrayList<>(Collections.singletonList("Please Select")));
    public final MutableLiveData<String> claimDate = new MutableLiveData<>("");
    public final MutableLiveData<String> projectName = new MutableLiveData<>("");
    public final MutableLiveData<String> purposeOfMeeting = new MutableLiveData<>("");
    public final MutableLiveData<String> startLocation = new MutableLiveData<>("");
    public final MutableLiveData<String> endLocation = new MutableLiveData<>("");
    public final MutableLiveData<String> totalAmount = new MutableLiveData<>("");
    public final MutableLiveData<String> remarks = new MutableLiveData<>("");
    public final MutableLiveData<String> customTransportInput = new MutableLiveData<>("");
    public final MutableLiveData<String> underProcessTextState = new MutableLiveData<>("");
    public final MutableLiveData<Boolean> isTravelSelected = new MutableLiveData<>(false);
    public final MutableLiveData<Integer> transportLayoutType = new MutableLiveData<>(0);
    public final MutableLiveData<Boolean> showCustomTransportInput = new MutableLiveData<>(false);
    public final MutableLiveData<String> toastMessage = new MutableLiveData<>();
    public final MutableLiveData<Boolean> operationSuccess = new MutableLiveData<>();
    public final String[] imageUrls = new String[10];
    public final String[] imageKeys = new String[10];
    private final APIInterface apiInterface;
    public int claimLength;
    public String hrMail;
    private Employee employeeCache;
    private String authTokenHeader;

    public ClaimManagementViewModel(@NonNull Application application) {
        super(application);
        apiInterface = APIClient.getInstance().getApi();
    }

    public void initAuth(String authToken) {
        this.authTokenHeader = "jwt " + authToken;
        getClaimLength(authToken);
    }

    private void getClaimLength(String authToken) {
        if (authToken == null) return;
        apiInterface.getClaimLength(authTokenHeader).enqueue(new Callback<ClaimLengthResponse>() {
            @Override
            public void onResponse(@NonNull Call<ClaimLengthResponse> call, @NonNull Response<ClaimLengthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    claimLength = response.body().getData().getCliamlength();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ClaimLengthResponse> call, @NonNull Throwable t) {
                Log.e("ClaimLength", "Error: " + t.getMessage());
            }
        });
    }

    public void cacheEmployeeProfile(Employee employee) {
        this.employeeCache = employee;
    }

    public void fetchBranchDetails(String userId) {
        APIClient.getInstance().getUser().getUserData(userId, authTokenHeader).enqueue(new Callback<UserModelResponse>() {
            @Override
            public void onResponse(@NonNull Call<UserModelResponse> call, @NonNull Response<UserModelResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String branchId = response.body().getData().getBranch().getId();
                    getBranchData(branchId);
                } else {
                    Log.e("User", "Failed to fetch user data: " + response.code());
                }

            }

            @Override
            public void onFailure(@NonNull Call<UserModelResponse> call, @NonNull Throwable t) {
                Log.e("User", "Error: " + t.getMessage());
            }
        });
    }

    private void getBranchData(String branchId) {
        APIClient.getInstance().getBranch().getBranchData(branchId, authTokenHeader).enqueue(new Callback<UserBranchResponse>() {
            @Override
            public void onResponse(@NonNull Call<UserBranchResponse> call, @NonNull Response<UserBranchResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    hrMail = response.body().getData().getBranch().getNotificationEmail();
                } else {
                    Log.e("Branch", "Failed to fetch branch data: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserBranchResponse> call, @NonNull Throwable t) {
                Log.e("Branch", "Error: " + t.getMessage());
            }
        });
    }

    public void getClaimPrices(String authTokenHeader) {
        String authToken = "jwt " + authTokenHeader;
        APIClient.getInstance().getApi().getClaimPrices(authToken).enqueue(new Callback<ClaimPriceResponse>() {
            @Override
            public void onResponse(@NonNull Call<ClaimPriceResponse> call, @NonNull Response<ClaimPriceResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("ClaimPrices", "Claim Prices: " + response.body().getData().toString());
                } else {
                    Log.e("ClaimPrices", "Failed to fetch claim prices: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ClaimPriceResponse> call, @NonNull Throwable t) {
                Log.e("ClaimPrices", "Error: " + t.getMessage());
            }
        });
    }

    public void getBusinessUnit(String authTokenHeader) {
        String authToken = "jwt " + authTokenHeader;
        APIClient.getInstance().getApi().getBusinessUnit(authToken).enqueue(new Callback<BusinessUnitResponse>() {
            @Override
            public void onResponse(@NonNull Call<BusinessUnitResponse> call, @NonNull Response<BusinessUnitResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<BusinessUnitSpinnerItem> list = new ArrayList<>();
                    list.add(new BusinessUnitSpinnerItem("", "Please Select"));

                    if (response.body().getData() != null && response.body().getData().getBus() != null) {
                        for (BusItem busItem : response.body().getData().getBus()) {
                            list.add(new BusinessUnitSpinnerItem(busItem.getId(), busItem.getName()));
                        }
                    }
                    businessUnitsSpinnerData.postValue(list);
                }
            }

            @Override
            public void onFailure(@NonNull Call<BusinessUnitResponse> call, @NonNull Throwable t) {
                Log.e("ClaimViewModel", "Failed to fetch business units", t);
            }
        });
    }

    public void getEmployeesByBusinessUnit(String authTokenHeader, String businessUnitId) {
        if (businessUnitId == null || businessUnitId.isEmpty()) {
            return;
        }
        String authToken = "jwt " + authTokenHeader;
        APIClient.getInstance().getApi().getEmployeesByBusinessUnit(authToken, businessUnitId).enqueue(new Callback<EmployeeByBusinessUnitResponse>() {
            @Override
            public void onResponse(@NonNull Call<EmployeeByBusinessUnitResponse> call, @NonNull Response<EmployeeByBusinessUnitResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<String> derivedEmployees = new ArrayList<>();
                    derivedEmployees.add("Please Select");

                    if (response.body().getData() != null && response.body().getData().getResult() != null) {
                        for (app.xedigital.ai.model.EmployeeByBusinessUnit.ResultItem emp : response.body().getData().getResult()) {
                            String firstName = emp.getFirstname() != null ? emp.getFirstname() : "";
                            String lastName = emp.getLastname() != null ? emp.getLastname() : "";
                            String fullName = (firstName + " " + lastName).trim();

                            if (!fullName.isEmpty()) {
                                derivedEmployees.add(fullName);
                            }
                        }
                    }
                    employeesList.postValue(derivedEmployees);
                } else {
                    Log.e("BusinessUnitEmployee", "Failed to fetch employees: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<EmployeeByBusinessUnitResponse> call, @NonNull Throwable t) {
                Log.e("BusinessUnitEmployee", "Error fetching employees: " + t.getMessage());
            }
        });
    }

    public void getAllEmployees(String authTokenHeader) {
        String authToken = "jwt " + authTokenHeader;
        APIClient.getInstance().getApi().getAllEmployees(authToken).enqueue(new Callback<AllEmployeeResponse>() {
            @Override
            public void onResponse(@NonNull Call<AllEmployeeResponse> call, @NonNull Response<AllEmployeeResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<String> derivedEmployees = new ArrayList<>();
                    derivedEmployees.add("Please Select");
                    if (response.body().getData() != null && response.body().getData().getEmployees() != null) {
                        for (app.xedigital.ai.model.allEmployee.EmployeesItem emp : response.body().getData().getEmployees()) {
                            String firstName = emp.getFirstname() != null ? emp.getFirstname() : "";
                            String lastName = emp.getLastname() != null ? emp.getLastname() : "";
                            String fullName = (firstName + " " + lastName).trim();

                            if (!fullName.isEmpty()) {
                                derivedEmployees.add(fullName);
                            }
                        }
                    }

                    employeesList.postValue(derivedEmployees);
                    Log.d("AllEmployees", "All employees successfully parsed and posted.");

                } else {
                    Log.e("AllEmployees", "Failed to fetch all employees: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<AllEmployeeResponse> call, @NonNull Throwable t) {
                Log.e("AllEmployees", "Error fetching all employees: " + t.getMessage());
            }
        });
    }

    // Single unified iterative file upload methodology replacing handleFile1 to handleFile10
    public void uploadFileAtIndex(Uri uri, int index) {
        if (index > 9) return;
        try {
            InputStream inputStream = getApplication().getContentResolver().openInputStream(uri);
            if (inputStream == null) return;

            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
            String base64String = Base64.encodeToString(byteBuffer.toByteArray(), Base64.DEFAULT);

            JSONObject jsonObject = new JSONObject();
            String keyPrefix = index == 0 ? "image" : "image" + getWordExtension(index);
            jsonObject.put(keyPrefix, "data:image/png;base64," + base64String);
            jsonObject.put("bucketName", "xe-digital-bucket/claims");

            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());
            Call<ResponseBody> uploadCall;
            if (index == 0)
                uploadCall = APIClient.getInstance().getUploadImage().uploadImage(authTokenHeader, requestBody);
            else if (index == 1)
                uploadCall = APIClient.getInstance().getUploadImage().uploadImage1(authTokenHeader, requestBody);
            else if (index == 2)
                uploadCall = APIClient.getInstance().getUploadImage().uploadImage2(authTokenHeader, requestBody);
            else if (index == 3)
                uploadCall = APIClient.getInstance().getUploadImage().uploadImage3(authTokenHeader, requestBody);
            else if (index == 4)
                uploadCall = APIClient.getInstance().getUploadImage().uploadImage4(authTokenHeader, requestBody);
            else if (index == 5)
                uploadCall = APIClient.getInstance().getUploadImage().uploadImage5(authTokenHeader, requestBody);
            else if (index == 6)
                uploadCall = APIClient.getInstance().getUploadImage().uploadImage6(authTokenHeader, requestBody);
            else if (index == 7)
                uploadCall = APIClient.getInstance().getUploadImage().uploadImage7(authTokenHeader, requestBody);
            else if (index == 8)
                uploadCall = APIClient.getInstance().getUploadImage().uploadImage8(authTokenHeader, requestBody);
            else
                uploadCall = APIClient.getInstance().getUploadImage().uploadImage9(authTokenHeader, requestBody);

            uploadCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response.body().string());
                            JSONObject data = jsonResponse.getJSONObject("data");
                            String urlSuffix = index == 0 ? "imageUrl" : "imageUrl" + getWordExtension(index);
                            String keySuffix = index == 0 ? "imageKey" : "imageKey" + getWordExtension(index);
                            imageUrls[index] = data.getString(urlSuffix);
                            imageKeys[index] = data.getString(keySuffix);
                            toastMessage.setValue("File " + (index + 1) + " uploaded successfully");
                        } catch (Exception e) {
                            Log.e("Upload", "Parsing Error", e);
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                }
            });

        } catch (IOException | JSONException e) {
            Log.e("Upload", "File streaming error", e);
        }
    }

    private String getWordExtension(int index) {
        String[] words = {"", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine"};
        return words[index];
    }

    public boolean validateAndExecute(boolean isSubmit, String meetingType, String claimCategory, String travelCategory, String transportMode, String sharedMode, String dedicatedMode, String currency) {
        String dateVal = claimDate.getValue() != null ? claimDate.getValue().trim() : "";
        String projectVal = projectName.getValue() != null ? projectName.getValue().trim() : "";
        String purposeVal = purposeOfMeeting.getValue() != null ? purposeOfMeeting.getValue().trim() : "";
        String amountVal = totalAmount.getValue() != null ? totalAmount.getValue().trim() : "";
        if (dateVal.isEmpty() || projectVal.isEmpty() || purposeVal.isEmpty()) {
            toastMessage.setValue("Please populate all required text details.");
            return false;
        }

        if (amountVal.isEmpty() || !amountVal.matches("^[0-9]+(?:\\.[0-9]{0,2})?$") || Double.parseDouble(amountVal) <= 0) {
            toastMessage.setValue("Invalid base calculation amount.");
            return false;
        }

        if (isSubmit) {
            executeSubmit(meetingType, claimCategory, travelCategory, transportMode, sharedMode, dedicatedMode, currency);
            executeFood(meetingType, claimCategory, travelCategory, transportMode, sharedMode, dedicatedMode, currency);
        } else {
            executeSave(meetingType, claimCategory, travelCategory, transportMode, sharedMode, dedicatedMode, currency);
        }
        return true;
    }

    private void executeSave(String mType, String cCat, String tCat, String tMode, String sMode, String dMode, String curr) {
        ClaimSaveRequest req = new ClaimSaveRequest();
        mapCommonSaveData(req, mType, cCat, tCat, tMode, sMode, dMode, curr);

        APIClient.getInstance().ClaimSave().claimSave(authTokenHeader, req).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    operationSuccess.setValue(true);
                    toastMessage.setValue("Claim Saved successfully");
                } else {
                    Log.e("ClaimSave", "Failed to save claim: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e("ClaimSave", "Error: " + t.getMessage());
            }
        });
    }

    private void executeSubmit(String mType, String cCat, String tCat, String tMode, String sMode, String dMode, String curr) {
        ClaimUpdateRequest req = new ClaimUpdateRequest();
        mapCommonUpdateData(req, mType, cCat, tCat, tMode, sMode, dMode, curr);

        APIClient.getInstance().ClaimSubmit().claimSubmit(authTokenHeader, req).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    operationSuccess.setValue(true);
                    toastMessage.setValue("Claim submitted successfully");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
            }
        });
    }

    private void executeFood(String mType, String cCat, String tCat, String tMode, String sMode, String dMode, String curr) {
        FoodRequest foodRequest = new FoodRequest();
//        mapCommonSaveData(req, mType, cCat, tCat, tMode, sMode, dMode, curr);

        APIClient.getInstance().getApi().FoodClaimApi(authTokenHeader, foodRequest).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    operationSuccess.setValue(true);
                    toastMessage.setValue("Food claim submitted successfully");
                } else {
                    Log.e("FoodClaim", "Failed to submit food claim: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e("FoodClaim", "Error: " + t.getMessage());
            }
        });
    }

    private void mapCommonSaveData(ClaimSaveRequest req, String mType, String cCat, String tCat, String tMode, String sMode, String dMode, String curr) {
        if (employeeCache != null) {
            req.setEmployee(employeeCache.getId());
            req.setEmployeeCode(employeeCache.getEmployeeCode());
            req.setEmail(employeeCache.getEmail());
            req.setFirstname(employeeCache.getFirstname());
            req.setLastname(employeeCache.getLastname());
            req.setDesignation(employeeCache.getDesignation());
            req.setGrade(employeeCache.getGrade());

            app.xedigital.ai.model.claimSave.ReportingManager rm = new app.xedigital.ai.model.claimSave.ReportingManager();
            rm.setId(employeeCache.getReportingManager().getId());
            rm.setFirstname(employeeCache.getReportingManager().getFirstname());
            rm.setLastname(employeeCache.getReportingManager().getLastname());
            rm.setEmail(employeeCache.getReportingManager().getEmail());
            req.setReportingManager(rm);
            req.setReportingManagerName(employeeCache.getReportingManager().getFirstname());
            req.setReportingManagerEmail(employeeCache.getReportingManager().getEmail());
        }
        req.setClaimDate(claimDate.getValue());
        req.setProject(projectName.getValue());
        req.setFromaddress(startLocation.getValue());
        req.setToaddress(endLocation.getValue());
        req.setMeeting(mType);
        req.setPerposeofmeet(purposeOfMeeting.getValue());
        req.setTotalClaim(claimLength);
        req.setCurrency(curr != null ? curr : "INR");
        req.setModeofcal(cCat);
        req.setTotalamount(totalAmount.getValue());
        req.setIsTravel("Travel".equals(cCat));
        req.setTravelCategory(tCat);
        req.setModeoftransport(tMode);
        req.setShared(sMode);
        req.setDedicated(dMode);
        req.setEmpTransport(customTransportInput.getValue());
        req.setRemark(remarks.getValue());
        req.setHrEmail(hrMail != null ? hrMail : "");
//        req.setBusinessUnit(selectedBusinessUnitId.getValue());

        req.setDocFileURL(imageUrls[0] != null ? imageUrls[0] : "");
        req.setDocFileURLKey(imageKeys[0] != null ? imageKeys[0] : "");
        req.setDocFileURLOne(imageUrls[1] != null ? imageUrls[1] : "");
        req.setDocFileURLKeyOne(imageKeys[1] != null ? imageKeys[1] : "");
        req.setDocFileURLTwo(imageUrls[2] != null ? imageUrls[2] : "");
        req.setDocFileURLKeyTwo(imageKeys[2] != null ? imageKeys[2] : "");
        req.setDocFileURLThree(imageUrls[3] != null ? imageUrls[3] : "");
        req.setDocFileURLKeyThree(imageKeys[3] != null ? imageKeys[3] : "");
        req.setDocFileURLFour(imageUrls[4] != null ? imageUrls[4] : "");
        req.setDocFileURLKeyFour(imageKeys[4] != null ? imageKeys[4] : "");
        req.setDocFileURLFive(imageUrls[5] != null ? imageUrls[5] : "");
        req.setDocFileURLKeyFive(imageKeys[5] != null ? imageKeys[5] : "");
        req.setDocFileURLSix(imageUrls[6] != null ? imageUrls[6] : "");
        req.setDocFileURLKeySix(imageKeys[6] != null ? imageKeys[6] : "");
        req.setDocFileURLSeven(imageUrls[7] != null ? imageUrls[7] : "");
        req.setDocFileURLKeySeven(imageKeys[7] != null ? imageKeys[7] : "");
        req.setDocFileURLEight(imageUrls[8] != null ? imageUrls[8] : "");
        req.setDocFileURLKeyEight(imageKeys[8] != null ? imageKeys[8] : "");
        req.setDocFileURLNine(imageUrls[9] != null ? imageUrls[9] : "");
        req.setDocFileURLKeyNine(imageKeys[9] != null ? imageKeys[9] : "");
    }

    private void mapCommonUpdateData(ClaimUpdateRequest req, String mType, String cCat, String tCat, String tMode, String sMode, String dMode, String curr) {
        if (employeeCache != null) {
            req.setEmployee(employeeCache.getId());
            req.setEmployeeCode(employeeCache.getEmployeeCode());
            req.setEmail(employeeCache.getEmail());
            req.setFirstname(employeeCache.getFirstname());
            req.setLastname(employeeCache.getLastname());
            req.setDesignation(employeeCache.getDesignation());
            req.setGrade(employeeCache.getGrade());

            app.xedigital.ai.model.claimSubmit.ReportingManager rm = new app.xedigital.ai.model.claimSubmit.ReportingManager();
            rm.setId(employeeCache.getReportingManager().getId());
            rm.setFirstname(employeeCache.getReportingManager().getFirstname());
            rm.setLastname(employeeCache.getReportingManager().getLastname());
            rm.setEmail(employeeCache.getReportingManager().getEmail());
            req.setReportingManager(rm);
            req.setReportingManagerName(employeeCache.getReportingManager().getFirstname());
            req.setReportingManagerEmail(employeeCache.getReportingManager().getEmail());
        }
        req.setClaimDate(claimDate.getValue());
        req.setProject(projectName.getValue());
        req.setFromaddress(startLocation.getValue());
        req.setToaddress(endLocation.getValue());
        req.setMeeting(mType);
        req.setPerposeofmeet(purposeOfMeeting.getValue());
        req.setTotalClaim(claimLength);
        req.setCurrency(curr != null ? curr : "INR");
        req.setModeofcal(cCat);
        req.setTotalamount(totalAmount.getValue());
        req.setIsTravel("Travel".equals(cCat));
        req.setTravelCategory(tCat);
        req.setModeoftransport(tMode);
        req.setShared(sMode);
        req.setDedicated(dMode);
        req.setEmpTransport(customTransportInput.getValue());
        req.setRemark(remarks.getValue());
        req.setHrEmail(hrMail != null ? hrMail : "");

        req.setDocFileURL(imageUrls[0] != null ? imageUrls[0] : "");
        req.setDocFileURLKey(imageKeys[0] != null ? imageKeys[0] : "");
        req.setDocFileURLOne(imageUrls[1] != null ? imageUrls[1] : "");
        req.setDocFileURLKeyOne(imageKeys[1] != null ? imageKeys[1] : "");
        req.setDocFileURLTwo(imageUrls[2] != null ? imageUrls[2] : "");
        req.setDocFileURLKeyTwo(imageKeys[2] != null ? imageKeys[2] : "");
        req.setDocFileURLThree(imageUrls[3] != null ? imageUrls[3] : "");
        req.setDocFileURLKeyThree(imageKeys[3] != null ? imageKeys[3] : "");
        req.setDocFileURLFour(imageUrls[4] != null ? imageUrls[4] : "");
        req.setDocFileURLKeyFour(imageKeys[4] != null ? imageKeys[4] : "");
        req.setDocFileURLFive(imageUrls[5] != null ? imageUrls[5] : "");
        req.setDocFileURLKeyFive(imageKeys[5] != null ? imageKeys[5] : "");
        req.setDocFileURLSix(imageUrls[6] != null ? imageUrls[6] : "");
        req.setDocFileURLKeySix(imageKeys[6] != null ? imageKeys[6] : "");
        req.setDocFileURLSeven(imageUrls[7] != null ? imageUrls[7] : "");
        req.setDocFileURLKeySeven(imageKeys[7] != null ? imageKeys[7] : "");
        req.setDocFileURLEight(imageUrls[8] != null ? imageUrls[8] : "");
        req.setDocFileURLKeyEight(imageKeys[8] != null ? imageKeys[8] : "");
        req.setDocFileURLNine(imageUrls[9] != null ? imageUrls[9] : "");
        req.setDocFileURLKeyNine(imageKeys[9] != null ? imageKeys[9] : "");
    }

    public void clearFormFields() {
        claimDate.setValue("");
        projectName.setValue("");
        purposeOfMeeting.setValue("");
        startLocation.setValue("");
        endLocation.setValue("");
        totalAmount.setValue("");
        remarks.setValue("");
        customTransportInput.setValue("");
        selectedBusinessUnitId.setValue("");
        underProcessTextState.setValue("");
        isTravelSelected.setValue(false);
        transportLayoutType.setValue(0);
        showCustomTransportInput.setValue(false);
        Arrays.fill(imageUrls, null);
        Arrays.fill(imageKeys, null);
        employeesList.setValue(new ArrayList<>(Collections.singletonList("Please Select")));
    }
}
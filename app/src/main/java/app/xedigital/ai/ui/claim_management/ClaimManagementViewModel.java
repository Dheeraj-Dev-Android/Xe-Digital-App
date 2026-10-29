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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.api.APIInterface;
import app.xedigital.ai.model.EmployeeByBusinessUnit.EmployeeByBusinessUnitResponse;
import app.xedigital.ai.model.allEmployee.AllEmployeeResponse;
import app.xedigital.ai.model.branch.UserBranchResponse;
import app.xedigital.ai.model.businessUnit.BusItem;
import app.xedigital.ai.model.businessUnit.BusinessUnitResponse;
import app.xedigital.ai.model.businessUnit.BusinessUnitSpinnerItem;
import app.xedigital.ai.model.claim.ExpenseRequest;
import app.xedigital.ai.model.claimLength.ClaimLengthResponse;
import app.xedigital.ai.model.claimLimit.ClaimDateLengthResponse;
import app.xedigital.ai.model.claimPrice.ClaimPriceResponse;
import app.xedigital.ai.model.claimSave.ClaimSaveRequest;
import app.xedigital.ai.model.editClaim.EditClaimResponse;
import app.xedigital.ai.model.employeeClaim.EmployeeClaimdataItem;
import app.xedigital.ai.model.profile.Employee;
import app.xedigital.ai.model.user.UserModelResponse;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ClaimManagementViewModel extends AndroidViewModel {

    public final LiveData<List<String>> meetingTypes = new MutableLiveData<>(Arrays.asList("Select an option", "Business", "Project", "Pre Sales"));
    public final LiveData<List<String>> claimCategories = new MutableLiveData<>(Arrays.asList("Select an option", "General", "Standard"));
    public final LiveData<List<String>> travelCategories = new MutableLiveData<>(Arrays.asList("Select an option", "Local", "Domestic", "International"));
    public final LiveData<List<String>> transportModes = new MutableLiveData<>(Arrays.asList("Select an option", "Shared", "Dedicated"));
    public final LiveData<List<String>> sharedTransportModes = new MutableLiveData<>(Arrays.asList("Select an option", "Auto", "Car", "E-Rickshaw", "Metro", "Others"));
    public final LiveData<List<String>> dedicatedTransportModes = new MutableLiveData<>(Arrays.asList("Select an option", "Two-Wheeler", "Three-Wheeler", "Others"));
    public final LiveData<List<String>> currencyDropdown = new MutableLiveData<>(Arrays.asList("Select an option", "INR", "USD", "EUR", "GBP", "JPY", "CNY", "AUD", "CAD", "CHF", "HKD", "SEK", "NZD"));

    public final MutableLiveData<List<BusinessUnitSpinnerItem>> businessUnitsSpinnerData = new MutableLiveData<>();
    public final MutableLiveData<String> selectedBusinessUnitId = new MutableLiveData<>("");
    public final MutableLiveData<String> selectedBusinessUnitName = new MutableLiveData<>("");
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

    public final MutableLiveData<String> selectedMealType = new MutableLiveData<>("");
    public final MutableLiveData<Boolean> isGuestClaim = new MutableLiveData<>(false);
    public final MutableLiveData<String> foodRestaurant = new MutableLiveData<>("");
    public final MutableLiveData<String> foodPersonsCount = new MutableLiveData<>("1");
    public final MutableLiveData<String> foodDescription = new MutableLiveData<>("");

    public final MutableLiveData<String> selectedAccommodationType = new MutableLiveData<>("");
    public final MutableLiveData<String> accommodationName = new MutableLiveData<>("");
    public final MutableLiveData<String> accommodationLocation = new MutableLiveData<>("");
    public final MutableLiveData<String> accommodationState = new MutableLiveData<>("");
    public final MutableLiveData<String> accommodationCheckIn = new MutableLiveData<>("");
    public final MutableLiveData<String> accommodationCheckOut = new MutableLiveData<>("");
    public final MutableLiveData<String> accommodationPurpose = new MutableLiveData<>("");

    public final MutableLiveData<String> selectedMiscCategory = new MutableLiveData<>("");
    public final MutableLiveData<String> billingPeriod = new MutableLiveData<>("");
    public final MutableLiveData<String> netProvider = new MutableLiveData<>("");
    public final MutableLiveData<String> billNumber = new MutableLiveData<>("");
    public final MutableLiveData<String> vehicleNumber = new MutableLiveData<>("");
    public final MutableLiveData<String> selectedFuelType = new MutableLiveData<>("");
    public final MutableLiveData<String> fuelQuantity = new MutableLiveData<>("");
    public final MutableLiveData<String> fuelStationName = new MutableLiveData<>("");
    public final MutableLiveData<String> parkingLocation = new MutableLiveData<>("");
    public final MutableLiveData<String> parkingDate = new MutableLiveData<>("");
    public final MutableLiveData<String> parkingDuration = new MutableLiveData<>("");
    public final MutableLiveData<String> tollVehicleNumber = new MutableLiveData<>("");
    public final MutableLiveData<String> tollPlazaName = new MutableLiveData<>("");
    public final MutableLiveData<String> tollLocation = new MutableLiveData<>("");

    public final MutableLiveData<Boolean> isTravelSelected = new MutableLiveData<>(false);
    public final MutableLiveData<Integer> transportLayoutType = new MutableLiveData<>(0);
    public final MutableLiveData<Boolean> showCustomTransportInput = new MutableLiveData<>(false);
    public final MutableLiveData<String> toastMessage = new MutableLiveData<>();
    public final MutableLiveData<Boolean> operationSuccess = new MutableLiveData<>();
    public final MutableLiveData<String> restaurantName = new MutableLiveData<>("");
    public final MutableLiveData<String> numberOfPersons = new MutableLiveData<>("");
    public final MutableLiveData<String> foodComment = new MutableLiveData<>("");
    public final MutableLiveData<String> foodTravelId = new MutableLiveData<>("");
    public final MutableLiveData<String> accommodationAddress = new MutableLiveData<>("");
    public final MutableLiveData<String> accommodationRegion = new MutableLiveData<>("");
    public final MutableLiveData<String> purposeOfStay = new MutableLiveData<>("");
    public final MutableLiveData<String> accommodationTravelId = new MutableLiveData<>("");
    public final MutableLiveData<String> internetProvider = new MutableLiveData<>("");
    public final MutableLiveData<String> internetBillNumber = new MutableLiveData<>("");
    public final MutableLiveData<String> fuelVehicleNumber = new MutableLiveData<>("");
    public final MutableLiveData<String> fuelStation = new MutableLiveData<>("");
    public final MutableLiveData<Integer> claimBackDateLimit = new MutableLiveData<>(-1);
    private final String[] imageUrls = new String[10];  // [L3 FIX]
    private final String[] imageKeys = new String[10];  // [L3 FIX]
    private final Map<String, String> employeeEmailMap = new HashMap<>();  // [M3 FIX]
    private final MutableLiveData<EmployeeClaimdataItem> _editClaimData = new MutableLiveData<>();
    private final APIInterface apiInterface;
    private final MutableLiveData<Boolean> _submissionSuccess = new MutableLiveData<>();
    private final MutableLiveData<Boolean> _saveSuccess = new MutableLiveData<>();
    // ══ NEW: Edit mode fields ══
    public String editingClaimId = null;
    public boolean isEditMode = false;
    public LiveData<EmployeeClaimdataItem> editClaimData = _editClaimData;
    public double claimLength;
    public String hrMail;
    public String bUemail;
    private Employee employeeCache;
    private String authTokenHeader;
    public ClaimManagementViewModel(@NonNull Application application) {
        super(application);
        apiInterface = APIClient.getInstance().getApi();
    }

    public String getEmployeeEmail(String name) {  // [M3 FIX]
        String email = employeeEmailMap.get(name);
        return email != null ? email : "";
    }

    public LiveData<Boolean> getSaveSuccess() {
        return _saveSuccess;
    }

    public void resetSaveSuccess() {
        _saveSuccess.setValue(false);
    }

    public LiveData<Boolean> getSubmissionSuccess() {
        return _submissionSuccess;
    }

    public void resetSubmissionSuccess() {
        _submissionSuccess.setValue(false);
    }

    public void initAuth(String authToken) {
        this.authTokenHeader = "jwt " + authToken;
        getClaimLength(authToken);
        getClaimLimit(authToken);
    }

    // ════════════════════════════════════════════════════════════════
    //  NEW: Fetch claim by type for editing
    // ════════════════════════════════════════════════════════════════
    public void fetchClaimForEdit(String claimId, String expenseType) {
        this.editingClaimId = claimId;
        this.isEditMode = true;

        Call<EditClaimResponse> call;
        switch (expenseType != null ? expenseType.toLowerCase() : "") {
            case "travel":
                call = apiInterface.getTravelClaimById(authTokenHeader, claimId);
                break;
            case "food":
                call = apiInterface.getFoodClaimById(authTokenHeader, claimId);
                break;
            case "accommodation":
                call = apiInterface.getAccommodationClaimById(authTokenHeader, claimId);
                break;
            case "miscellaneous":
                call = apiInterface.getMiscClaimById(authTokenHeader, claimId);
                break;
            default:
                toastMessage.setValue("Unknown expense type: " + expenseType);
                return;
        }

        call.enqueue(new Callback<EditClaimResponse>() {
            @Override
            public void onResponse(@NonNull Call<EditClaimResponse> call, @NonNull Response<EditClaimResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    EmployeeClaimdataItem claim = response.body().getData().getClaim();
                    if (claim != null) {
                        _editClaimData.postValue(claim);
                        populateFromClaim(claim);
                    } else {
                        toastMessage.postValue("Claim data is empty.");
                    }
                } else {
                    Log.e("FetchEdit", "Error code: " + response.code());
                    toastMessage.postValue("Failed to fetch claim for editing.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<EditClaimResponse> call, @NonNull Throwable t) {
                Log.e("FetchEdit", "Network error", t);
                toastMessage.postValue("Network error while fetching claim.");
            }
        });
    }

    // ════════════════════════════════════════════════════════════════
    //  NEW: Populate all LiveData fields from claim data (null-safe)
    // ════════════════════════════════════════════════════════════════
    public void populateFromClaim(EmployeeClaimdataItem c) {
        // Common
        claimDate.setValue(sliceDate(safe(c.getClaimDate())));
        projectName.setValue(safe(c.getProject()));
        purposeOfMeeting.setValue(safe(c.getPerposeofmeet()));
        totalAmount.setValue(c.getTotalamount() != 0 ? String.valueOf(c.getTotalamount()) : "");
        remarks.setValue(safe(c.getComment()));

        // Travel
        startLocation.setValue(safe(c.getFromaddress()));
        endLocation.setValue(safe(c.getToaddress()));
        customTransportInput.setValue(c.getDistance() != null ? String.valueOf(c.getDistance()) : safe(c.getEmpTransport()));

        // Food (NEW field names that TextWatchers write to)
        restaurantName.setValue(safe(c.getRestaurant()));
        numberOfPersons.setValue(c.getPersons() != null ? String.valueOf(c.getPersons()) : "");
        foodComment.setValue(safe(c.getDescription()));
        selectedMealType.setValue(safe(c.getMeantype()));

        // Accommodation (NEW field names)
        accommodationName.setValue(safe(c.getAccommodationname()));
        accommodationAddress.setValue(safe(c.getLocation()));
        accommodationRegion.setValue(safe(c.getState()));
        accommodationCheckIn.setValue(sliceDate(safe(c.getCheckin())));
        accommodationCheckOut.setValue(sliceDate(safe(c.getCheckout())));
        purposeOfStay.setValue(""); // Not available in EmployeeClaimdataItem
        selectedAccommodationType.setValue(safe(c.getAccommodationtype()));

        // Miscellaneous (NEW field names)
        selectedMiscCategory.setValue(safe(c.getExpancecategory()));
        billingPeriod.setValue(sliceDate(safe(c.getBillingperiod())));
        internetProvider.setValue(safe(c.getNetprovider()));
        internetBillNumber.setValue(safe(c.getBillnumber()));
        fuelVehicleNumber.setValue(safe(c.getVehiclenumber()));
        selectedFuelType.setValue(safe(c.getFueltype()));
        fuelQuantity.setValue(safe(c.getFuelquantity()));
        fuelStation.setValue(safe(c.getFuelstationname()));
        parkingLocation.setValue(safe(c.getParkinglocation()));
        parkingDate.setValue(sliceDate(safe(c.getParkingdate())));
        parkingDuration.setValue(safe(c.getDuration()));
        tollVehicleNumber.setValue(safe(c.getTollvehiclenumber()));
        tollPlazaName.setValue(safe(c.getTollplazaname()));
        tollLocation.setValue(safe(c.getTolllocation()));

        // Also populate OLD fields so prepareBasePayload has values
        // (belt-and-suspenders — the FIX below makes this redundant but safe)
        foodRestaurant.setValue(safe(c.getRestaurant()));
        foodPersonsCount.setValue(c.getPersons() != null ? String.valueOf(c.getPersons()) : "1");
        foodDescription.setValue(safe(c.getDescription()));
        accommodationLocation.setValue(safe(c.getLocation()));
        accommodationState.setValue(safe(c.getState()));
        accommodationPurpose.setValue("");
        netProvider.setValue(safe(c.getNetprovider()));
        billNumber.setValue(safe(c.getBillnumber()));
        vehicleNumber.setValue(safe(c.getVehiclenumber()));
        fuelStationName.setValue(safe(c.getFuelstationname()));
    }

    // ══ NEW: Helper methods ══
    private String safe(String val) {
        if (val == null || val.trim().isEmpty() || "null".equalsIgnoreCase(val.trim())) return "";
        return val.trim();
    }

    private String sliceDate(String dateStr) {
        if (dateStr != null && dateStr.length() >= 10) {
            return dateStr.substring(0, 10);
        }
        return dateStr != null ? dateStr : "";
    }

    // ── Existing methods (unchanged) ──

    private void getClaimLength(String authToken) {
        if (authToken == null) return;
        apiInterface.getClaimLength(authTokenHeader).enqueue(new Callback<ClaimLengthResponse>() {
            @Override
            public void onResponse(@NonNull Call<ClaimLengthResponse> call, @NonNull Response<ClaimLengthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    claimLength = response.body().getData().getEmployeeCount();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ClaimLengthResponse> call, @NonNull Throwable t) {
                Log.e("ClaimLength", "Error: " + t.getMessage());
            }
        });
    }

    private void getClaimLimit(String authToken) {
        if (authToken == null) return;
        apiInterface.getClaimLimit(authTokenHeader).enqueue(new Callback<ClaimDateLengthResponse>() {
            @Override
            public void onResponse(@NonNull Call<ClaimDateLengthResponse> call, @NonNull Response<ClaimDateLengthResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    if (response.body().getData() != null) {
                        claimBackDateLimit.postValue(response.body().getData().getClaimBackDateLimit());
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ClaimDateLengthResponse> call, @NonNull Throwable t) {
                Log.e("ClaimLimit", "Error: " + t.getMessage());
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
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserBranchResponse> call, @NonNull Throwable t) {
                Log.e("Branch", "Error: " + t.getMessage());
            }
        });
    }

    public void getClaimPrices(String rawAuthToken) {  // [L1 FIX]
        String authToken = "jwt " + rawAuthToken;      // [L1 FIX]
        apiInterface.getClaimPrices(authToken).enqueue(new Callback<ClaimPriceResponse>() {
            @Override
            public void onResponse(@NonNull Call<ClaimPriceResponse> call, @NonNull Response<ClaimPriceResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("ClaimPrices", "Claim Prices: " + response.body().getData().toString());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ClaimPriceResponse> call, @NonNull Throwable t) {
                Log.e("ClaimPrices", "Error: " + t.getMessage());
            }
        });
    }

    public void getBusinessUnit(String rawAuthToken) {  // [L1 FIX]
        String authToken = "jwt " + rawAuthToken;       // [L1 FIX]
        apiInterface.getBusinessUnit(authToken).enqueue(new Callback<BusinessUnitResponse>() {
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

    public void getEmployeesByBusinessUnit(String rawAuthToken, String businessUnitId) {  // [L1 FIX]
        if (businessUnitId == null || businessUnitId.isEmpty()) return;
        String authToken = "jwt " + rawAuthToken;  // [L1 FIX]
        apiInterface.getEmployeesByBusinessUnit(authToken, businessUnitId).enqueue(new Callback<EmployeeByBusinessUnitResponse>() {
            @Override
            public void onResponse(@NonNull Call<EmployeeByBusinessUnitResponse> call, @NonNull Response<EmployeeByBusinessUnitResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<String> derivedEmployees = new ArrayList<>();
                    derivedEmployees.add("Please Select");
                    if (response.body().getData() != null && response.body().getData().getResult() != null) {
                        for (app.xedigital.ai.model.EmployeeByBusinessUnit.ResultItem emp : response.body().getData().getResult()) {
                            String fullName = ((emp.getFirstname() != null ? emp.getFirstname() : "") + " " + (emp.getLastname() != null ? emp.getLastname() : "")).trim();
                            if (!fullName.isEmpty()) {
                                derivedEmployees.add(fullName);
                                employeeEmailMap.put(fullName, emp.getEmail());  // [M3 FIX]
                            }
                        }
                    }
                    employeesList.postValue(derivedEmployees);
                }
            }

            @Override
            public void onFailure(@NonNull Call<EmployeeByBusinessUnitResponse> call, @NonNull Throwable t) {
                Log.e("BusinessUnitEmployee", "Error: " + t.getMessage());
            }
        });
    }

    public void getAllEmployees(String rawAuthToken) {  // [L1 FIX]
        String authToken = "jwt " + rawAuthToken;       // [L1 FIX]
        apiInterface.getAllEmployees(authToken).enqueue(new Callback<AllEmployeeResponse>() {
            @Override
            public void onResponse(@NonNull Call<AllEmployeeResponse> call, @NonNull Response<AllEmployeeResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<String> derivedEmployees = new ArrayList<>();
                    derivedEmployees.add("Please Select");
                    if (response.body().getData() != null && response.body().getData().getEmployees() != null) {
                        for (app.xedigital.ai.model.allEmployee.EmployeesItem emp : response.body().getData().getEmployees()) {
                            String fullName = ((emp.getFirstname() != null ? emp.getFirstname() : "") + " " + (emp.getLastname() != null ? emp.getLastname() : "")).trim();
                            if (!fullName.isEmpty()) {
                                derivedEmployees.add(fullName);
                                employeeEmailMap.put(fullName, emp.getEmail());  // [M3 FIX]
                            }
                        }
                    }
                    employeesList.postValue(derivedEmployees);
                }
            }

            @Override
            public void onFailure(@NonNull Call<AllEmployeeResponse> call, @NonNull Throwable t) {
                Log.e("AllEmployees", "Error: " + t.getMessage());
            }
        });
    }

    public void uploadFileAtIndex(Uri uri, int index) {
        if (index > 9) return;
        new Thread(() -> {  // [M2 FIX] Move file I/O off main thread
            try (InputStream inputStream = getApplication().getContentResolver().openInputStream(uri)) {  // [M1 FIX] try-with-resources
                if (inputStream == null) return;
                ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len;
                while ((len = inputStream.read(buffer)) != -1) byteBuffer.write(buffer, 0, len);
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
                                toastMessage.postValue("File " + (index + 1) + " uploaded successfully");  // [M2 FIX] postValue from bg thread
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
        }).start();
    }

    private String getWordExtension(int index) {
        String[] words = {"", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine"};
        return words[index];
    }

    private String sanitize(String val) {
        if (val == null || val.trim().isEmpty() || "Please Select".equalsIgnoreCase(val.trim()) || "Select an option".equalsIgnoreCase(val.trim()) || "N/A".equalsIgnoreCase(val.trim()))
            return "";
        return val.trim();
    }

    private ExpenseRequest prepareBasePayload(String meetingType, String currency) {
        ExpenseRequest payload = new ExpenseRequest();

        // ══ NEW: Set _id for edit mode ══
        if (isEditMode && editingClaimId != null) {
            payload.setId(editingClaimId);
        }

        if (employeeCache != null) {
            payload.setEmployee(sanitize(employeeCache.getId()));
            payload.setEmpId(sanitize(employeeCache.getId()));
            payload.setEmployeeCode(sanitize(employeeCache.getEmployeeCode()));
            payload.setFirstname(sanitize(employeeCache.getFirstname()));
            payload.setLastname(sanitize(employeeCache.getLastname()));
            payload.setEmail(sanitize(employeeCache.getEmail()));
            payload.setDesignation(sanitize(employeeCache.getDesignation()));
            payload.setGrade(sanitize(employeeCache.getGrade()));
            payload.setHrEmail(sanitize(hrMail));
            if (employeeCache.getReportingManager() != null) {
                ExpenseRequest.Manager mgr = new ExpenseRequest.Manager();
                mgr.setId(sanitize(employeeCache.getReportingManager().getId()));
                mgr.setFirstname(sanitize(employeeCache.getReportingManager().getFirstname()));
                mgr.setLastname(sanitize(employeeCache.getReportingManager().getLastname()));
                mgr.setEmail(sanitize(employeeCache.getReportingManager().getEmail()));
                payload.setReportingManager(mgr);
                payload.setReportingManagerName(sanitize(employeeCache.getReportingManager().getFirstname()));
                payload.setReportingManagerEmail(sanitize(employeeCache.getReportingManager().getEmail()));
            }
            if (employeeCache.getDepartment() != null) {
                ExpenseRequest.Department dept = new ExpenseRequest.Department();
                dept.setId(sanitize(employeeCache.getDepartment().getId()));
                dept.setName(sanitize(employeeCache.getDepartment().getName()));
                payload.setDepartment(dept);
                app.xedigital.ai.model.claimSave.ReqDepartment reqDept = new app.xedigital.ai.model.claimSave.ReqDepartment();
                reqDept.setId(sanitize(employeeCache.getDepartment().getId()));
                reqDept.setName(sanitize(employeeCache.getDepartment().getName()));
                payload.setReqDepartment(reqDept);
            }
            payload.setRequestedfirstname(sanitize(employeeCache.getFirstname()));
            payload.setRequestedlastname(sanitize(employeeCache.getLastname()));
        }
        payload.setBu(sanitize(selectedBusinessUnitId.getValue()));
        payload.setBuName(sanitize(selectedBusinessUnitName.getValue()));
        payload.setBuEmail(bUemail);
        payload.setClaimDate(sanitize(claimDate.getValue()));
        payload.setProject(sanitize(projectName.getValue()));
        payload.setMeeting(sanitize(meetingType != null ? meetingType.toLowerCase() : ""));
        payload.setPerposeofmeet(sanitize(purposeOfMeeting.getValue()));
        payload.setTotalClaim(claimLength);
        payload.setCurrency(sanitize(currency));
        payload.setTotalamount(sanitize(totalAmount.getValue()));
        payload.setRemark(sanitize(remarks.getValue()));
        payload.setConfbutton(false);
        payload.setModeofcal("general");
        payload.setModeofcalPascal("");
        payload.setRequest("Expense Claim Request");
        payload.setFromaddress(sanitize(startLocation.getValue()));
        payload.setToaddress(sanitize(endLocation.getValue()));
        payload.setDistance(sanitize(customTransportInput.getValue()));
        payload.setEmpTransport(sanitize(customTransportInput.getValue()));
        payload.setGuest(isGuestClaim.getValue() != null ? isGuestClaim.getValue() : false);

        // ══ FIX: Read from NEW LiveData fields (the ones TextWatchers actually write to) ══
        payload.setMeantype(sanitize(selectedMealType.getValue()));
        payload.setRestaurant(sanitize(restaurantName.getValue()));          // was: foodRestaurant
        payload.setPersons(sanitize(numberOfPersons.getValue()));            // was: foodPersonsCount
        payload.setDescription(sanitize(foodComment.getValue()));            // was: foodDescription
        payload.setAccommodationtype(sanitize(selectedAccommodationType.getValue()));
        payload.setAccommodationname(sanitize(accommodationName.getValue()));
        payload.setLocation(sanitize(accommodationAddress.getValue()));      // was: accommodationLocation
        payload.setState(sanitize(accommodationRegion.getValue()));          // was: accommodationState
        payload.setCheckin(sanitize(accommodationCheckIn.getValue()));
        payload.setCheckout(sanitize(accommodationCheckOut.getValue()));
        payload.setPurposeofstay(sanitize(purposeOfStay.getValue()));        // was: accommodationPurpose
        payload.setExpancecategory(sanitize(selectedMiscCategory.getValue()));
        payload.setBillingperiod(sanitize(billingPeriod.getValue()));
        payload.setNetprovider(sanitize(internetProvider.getValue()));       // was: netProvider
        payload.setBillnumber(sanitize(internetBillNumber.getValue()));      // was: billNumber
        payload.setVehiclenumber(sanitize(fuelVehicleNumber.getValue()));    // was: vehicleNumber
        payload.setFueltype(sanitize(selectedFuelType.getValue()));
        payload.setFuelquantity(sanitize(fuelQuantity.getValue()));
        payload.setFuelstationname(sanitize(fuelStation.getValue()));        // was: fuelStationName
        payload.setParkinglocation(sanitize(parkingLocation.getValue()));
        payload.setParkingdate(sanitize(parkingDate.getValue()));
        payload.setDuration(sanitize(parkingDuration.getValue()));
        payload.setTollvehiclenumber(sanitize(tollVehicleNumber.getValue()));
        payload.setTollplazaname(sanitize(tollPlazaName.getValue()));
        payload.setTolllocation(sanitize(tollLocation.getValue()));

        payload.setImage(imageUrls[0] != null ? imageUrls[0] : "");
        payload.setDocFileURL(imageUrls[0] != null ? imageUrls[0] : "");
        payload.setDocFileURLKey(imageKeys[0] != null ? imageKeys[0] : "");
        payload.setImageOne(imageUrls[1] != null ? imageUrls[1] : "");
        payload.setDocFileURLOne(imageUrls[1] != null ? imageUrls[1] : "");
        payload.setDocFileURLKeyOne(imageKeys[1] != null ? imageKeys[1] : "");
        payload.setImageTwo(imageUrls[2] != null ? imageUrls[2] : "");
        payload.setDocFileURLTwo(imageUrls[2] != null ? imageUrls[2] : "");
        payload.setDocFileURLKeyTwo(imageKeys[2] != null ? imageKeys[2] : "");
        payload.setImageThree(imageUrls[3] != null ? imageUrls[3] : "");
        payload.setDocFileURLThree(imageUrls[3] != null ? imageUrls[3] : "");
        payload.setDocFileURLKeyThree(imageKeys[3] != null ? imageKeys[3] : "");
        payload.setImageFour(imageUrls[4] != null ? imageUrls[4] : "");
        payload.setDocFileURLFour(imageUrls[4] != null ? imageUrls[4] : "");
        payload.setDocFileURLKeyFour(imageKeys[4] != null ? imageKeys[4] : "");
        payload.setImageFive(imageUrls[5] != null ? imageUrls[5] : "");
        payload.setDocFileURLFive(imageUrls[5] != null ? imageUrls[5] : "");
        payload.setDocFileURLKeyFive(imageKeys[5] != null ? imageKeys[5] : "");
        payload.setImageSix(imageUrls[6] != null ? imageUrls[6] : "");
        payload.setDocFileURLSix(imageUrls[6] != null ? imageUrls[6] : "");
        payload.setDocFileURLKeySix(imageKeys[6] != null ? imageKeys[6] : "");
        payload.setImageSeven(imageUrls[7] != null ? imageUrls[7] : "");
        payload.setDocFileURLSeven(imageUrls[7] != null ? imageUrls[7] : "");
        payload.setDocFileURLKeySeven(imageKeys[7] != null ? imageKeys[7] : "");
        payload.setImageEight(imageUrls[8] != null ? imageUrls[8] : "");
        payload.setDocFileURLEight(imageUrls[8] != null ? imageUrls[8] : "");
        payload.setDocFileURLKeyEight(imageKeys[8] != null ? imageKeys[8] : "");
        payload.setImageNine(imageUrls[9] != null ? imageUrls[9] : "");
        payload.setDocFileURLNine(imageUrls[9] != null ? imageUrls[9] : "");
        payload.setDocFileURLKeyNine(imageKeys[9] != null ? imageKeys[9] : "");

        return payload;
    }

    public void executeFoodSubmit(String meetingType, String currency) {
        ExpenseRequest request = prepareBasePayload(meetingType, currency);
        request.setExpenseType("food");
        request.setGuest(isGuestClaim.getValue() != null ? isGuestClaim.getValue() : false);
        request.setMeantype(sanitize(selectedMealType.getValue()));
        request.setRestaurant(sanitize(restaurantName.getValue()));
        request.setPersons(sanitize(numberOfPersons.getValue()));
        request.setDescription(sanitize(foodComment.getValue()));
        executeNetworkCall(request, "Food");
    }

    public void executeTravelSubmit(String meetingType, String travelCategory, String transportMode, String sharedMode, String dedicatedMode, String currency) {
        ExpenseRequest request = prepareBasePayload(meetingType, currency);
        request.setExpenseType("travel");
        request.setFromaddress(sanitize(startLocation.getValue()));
        request.setToaddress(sanitize(endLocation.getValue()));
        request.setTravelCategory(sanitize(travelCategory));
        request.setModeoftransport(sanitize(transportMode));
        request.setShared(sanitize(sharedMode));
        request.setDedicated(sanitize(dedicatedMode));
        request.setDistance(sanitize(customTransportInput.getValue()));
        request.setEmpTransport(sanitize(customTransportInput.getValue()));
        executeNetworkCall(request, "Travel");
    }

    public void executeAccommodationSubmit(String meetingType, String currency) {
        ExpenseRequest request = prepareBasePayload(meetingType, currency);
        request.setExpenseType("accommodation");
        request.setAccommodationtype(sanitize(selectedAccommodationType.getValue()));
        request.setAccommodationname(sanitize(accommodationName.getValue()));
        request.setLocation(sanitize(accommodationAddress.getValue()));
        request.setState(sanitize(accommodationRegion.getValue()));
        request.setCheckin(sanitize(accommodationCheckIn.getValue()));
        request.setCheckout(sanitize(accommodationCheckOut.getValue()));
        request.setPurposeofstay(sanitize(purposeOfStay.getValue()));
        executeNetworkCall(request, "Accommodation");
    }

    public void executeMiscSubmit(String meetingType, String currency) {
        ExpenseRequest request = prepareBasePayload(meetingType, currency);
        request.setExpenseType("miscellaneous");
        request.setExpancecategory(sanitize(selectedMiscCategory.getValue()));
        request.setBillingperiod(sanitize(billingPeriod.getValue()));
        request.setNetprovider(sanitize(internetProvider.getValue()));
        request.setBillnumber(sanitize(internetBillNumber.getValue()));
        request.setVehiclenumber(sanitize(fuelVehicleNumber.getValue()));
        request.setFueltype(sanitize(selectedFuelType.getValue()));
        request.setFuelquantity(sanitize(fuelQuantity.getValue()));
        request.setFuelstationname(sanitize(fuelStation.getValue()));
        request.setParkinglocation(sanitize(parkingLocation.getValue()));
        request.setParkingdate(sanitize(parkingDate.getValue()));
        request.setDuration(sanitize(parkingDuration.getValue()));
        request.setTollvehiclenumber(sanitize(tollVehicleNumber.getValue()));
        request.setTollplazaname(sanitize(tollPlazaName.getValue()));
        request.setTolllocation(sanitize(tollLocation.getValue()));
        executeNetworkCall(request, "Miscellaneous");
    }

    // ════════════════════════════════════════════════════════════════
    //  CHANGED: Route to PUT endpoints when in edit mode
    // ════════════════════════════════════════════════════════════════
    private void executeNetworkCall(ExpenseRequest requestBody, final String label) {
        Call<ResponseBody> apiCall;

        if (isEditMode && editingClaimId != null) {
            // ── EDIT MODE: Use PUT endpoints ──
            switch (label) {
                case "Food":
                    apiCall = apiInterface.updateFoodClaim(authTokenHeader, editingClaimId, requestBody);
                    break;
                case "Travel":
                    apiCall = apiInterface.updateTravelClaim(authTokenHeader, editingClaimId, requestBody);
                    break;
                case "Accommodation":
                    apiCall = apiInterface.updateAccommodationClaim(authTokenHeader, editingClaimId, requestBody);
                    break;
                case "Miscellaneous":
                    apiCall = apiInterface.updateMiscClaim(authTokenHeader, editingClaimId, requestBody);
                    break;
                default:
                    toastMessage.setValue("Unsupported expense type routing.");
                    return;
            }
        } else {
            // ── CREATE MODE: Use POST endpoints (original behavior) ──
            switch (label) {
                case "Food":
                    apiCall = apiInterface.FoodClaimApi(authTokenHeader, requestBody);
                    break;
                case "Travel":
                    apiCall = apiInterface.TravelClaimApi(authTokenHeader, requestBody);
                    break;
                case "Accommodation":
                    apiCall = apiInterface.AccommodationClaimApi(authTokenHeader, requestBody);
                    break;
                case "Miscellaneous":
                    apiCall = apiInterface.MiscellaneousClaimApi(authTokenHeader, requestBody);
                    break;
                default:
                    toastMessage.setValue("Unsupported expense type routing.");
                    return;
            }
        }

        apiCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    operationSuccess.setValue(true);
                    _submissionSuccess.setValue(true);
                    toastMessage.setValue(label + " claim " + (isEditMode ? "updated" : "submitted") + " successfully");
                } else {
                    Log.e("ExpenseSubmitAPI", label + " error code: " + response.code());
                    toastMessage.setValue("Failed to " + (isEditMode ? "update" : "submit") + " " + label.toLowerCase() + " claim.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e("ExpenseSubmitAPI", label + " network error", t);
                toastMessage.setValue("Network error.");
            }
        });
    }

    private ClaimSaveRequest prepareSavePayload(String meetingType, String currency, String expenseType, String travelCategory, String transportMode, String sharedMode, String dedicatedMode) {
        ClaimSaveRequest payload = new ClaimSaveRequest();

        // ══ NEW: Set _id for edit mode ══
        if (isEditMode && editingClaimId != null) {
            payload.setId(editingClaimId);
        }

        if (employeeCache != null) {
            payload.setEmployee(sanitize(employeeCache.getId()));
            payload.setEmpId(sanitize(employeeCache.getId()));
            payload.setEmployeeCode(sanitize(employeeCache.getEmployeeCode()));
            payload.setFirstname(sanitize(employeeCache.getFirstname()));
            payload.setLastname(sanitize(employeeCache.getLastname()));
            payload.setEmail(sanitize(employeeCache.getEmail()));
            payload.setDesignation(sanitize(employeeCache.getDesignation()));
            payload.setGrade(sanitize(employeeCache.getGrade()));
            payload.setHrEmail(sanitize(hrMail));
            if (employeeCache.getReportingManager() != null) {
                app.xedigital.ai.model.claimSave.ReportingManager mgr = new app.xedigital.ai.model.claimSave.ReportingManager();
                mgr.setId(sanitize(employeeCache.getReportingManager().getId()));
                mgr.setFirstname(sanitize(employeeCache.getReportingManager().getFirstname()));
                mgr.setLastname(sanitize(employeeCache.getReportingManager().getLastname()));
                mgr.setEmail(sanitize(employeeCache.getReportingManager().getEmail()));
                payload.setReportingManager(mgr);
                payload.setReportingManagerName(sanitize(employeeCache.getReportingManager().getFirstname()));
                payload.setReportingManagerEmail(sanitize(employeeCache.getReportingManager().getEmail()));
            }
            if (employeeCache.getDepartment() != null) {
                app.xedigital.ai.model.claimSave.Department dept = new app.xedigital.ai.model.claimSave.Department();
                dept.setId(sanitize(employeeCache.getDepartment().getId()));
                dept.setName(sanitize(employeeCache.getDepartment().getName()));
                payload.setDepartment(dept);
            }
            payload.setRequestedfirstname(sanitize(employeeCache.getFirstname()));
            payload.setRequestedlastname(sanitize(employeeCache.getLastname()));
        }
        payload.setBu(sanitize(selectedBusinessUnitId.getValue()));
        payload.setBuName(sanitize(selectedBusinessUnitName.getValue()));
        payload.setBuEmail(bUemail);
        payload.setClaimDate(sanitize(claimDate.getValue()));
        payload.setProject(sanitize(projectName.getValue()));
        payload.setMeeting(sanitize(meetingType != null ? meetingType.toLowerCase() : ""));
        payload.setPerposeofmeet(sanitize(purposeOfMeeting.getValue()));
        payload.setTotalClaim((int) claimLength);
        payload.setCurrency(sanitize(currency));
        payload.setTotalamount(sanitize(totalAmount.getValue()));
        payload.setRemark(sanitize(remarks.getValue()));
        payload.setConfbutton(false);
        payload.setModeofcal("general");
        payload.setRequest("");
        payload.setStatus("");
        payload.setVarients("");
        payload.setIndividually("");
        payload.setFromaddress(sanitize(startLocation.getValue()));
        payload.setToaddress(sanitize(endLocation.getValue()));
        payload.setDistance(sanitize(customTransportInput.getValue()));
        payload.setEmpTransport(sanitize(customTransportInput.getValue()));
        payload.setGuest(isGuestClaim.getValue() != null ? isGuestClaim.getValue() : false);
        payload.setMeantype(sanitize(selectedMealType.getValue()));
        payload.setRestaurant(sanitize(restaurantName.getValue()));
        payload.setPersons(sanitize(numberOfPersons.getValue()));
        payload.setDescription(sanitize(foodComment.getValue()));
        payload.setTravelCategory(sanitize(travelCategory));
        payload.setModeoftransport(sanitize(transportMode));
        payload.setShared(sanitize(sharedMode));
        payload.setDedicated(sanitize(dedicatedMode));
        payload.setTravelRefId(sanitize(accommodationTravelId.getValue()));
        payload.setAccommodationtype(sanitize(selectedAccommodationType.getValue()));
        payload.setAccommodationname(sanitize(accommodationName.getValue()));
        payload.setLocation(sanitize(accommodationAddress.getValue()));
        payload.setState(sanitize(accommodationRegion.getValue()));
        payload.setCheckin(sanitize(accommodationCheckIn.getValue()));
        payload.setCheckout(sanitize(accommodationCheckOut.getValue()));
        payload.setPurposeofstay(sanitize(purposeOfStay.getValue()));
        payload.setExpancecategory(sanitize(selectedMiscCategory.getValue()));
        payload.setBillingperiod(sanitize(billingPeriod.getValue()));
        payload.setNetprovider(sanitize(internetProvider.getValue()));
        payload.setBillnumber(sanitize(internetBillNumber.getValue()));
        payload.setVehiclenumber(sanitize(fuelVehicleNumber.getValue()));
        payload.setFueltype(sanitize(selectedFuelType.getValue()));
        payload.setFuelquantity(sanitize(fuelQuantity.getValue()));
        payload.setFuelstationname(sanitize(fuelStation.getValue()));
        payload.setParkinglocation(sanitize(parkingLocation.getValue()));
        payload.setParkingdate(sanitize(parkingDate.getValue()));
        payload.setDuration(sanitize(parkingDuration.getValue()));
        payload.setTollvehiclenumber(sanitize(tollVehicleNumber.getValue()));
        payload.setTollplazaname(sanitize(tollPlazaName.getValue()));
        payload.setTolllocation(sanitize(tollLocation.getValue()));
        payload.setExpenseType(sanitize(expenseType).toLowerCase());

        payload.setImage(imageUrls[0] != null ? imageUrls[0] : "");
        payload.setDocFileURL(imageUrls[0] != null ? imageUrls[0] : "");
        payload.setDocFileURLKey(imageKeys[0] != null ? imageKeys[0] : "");
        payload.setImageOne(imageUrls[1] != null ? imageUrls[1] : "");
        payload.setDocFileURLOne(imageUrls[1] != null ? imageUrls[1] : "");
        payload.setDocFileURLKeyOne(imageKeys[1] != null ? imageKeys[1] : "");
        payload.setImageTwo(imageUrls[2] != null ? imageUrls[2] : "");
        payload.setDocFileURLTwo(imageUrls[2] != null ? imageUrls[2] : "");
        payload.setDocFileURLKeyTwo(imageKeys[2] != null ? imageKeys[2] : "");
        payload.setImageThree(imageUrls[3] != null ? imageUrls[3] : "");
        payload.setDocFileURLThree(imageUrls[3] != null ? imageUrls[3] : "");
        payload.setDocFileURLKeyThree(imageKeys[3] != null ? imageKeys[3] : "");
        payload.setImageFour(imageUrls[4] != null ? imageUrls[4] : "");
        payload.setDocFileURLFour(imageUrls[4] != null ? imageUrls[4] : "");
        payload.setDocFileURLKeyFour(imageKeys[4] != null ? imageKeys[4] : "");
        payload.setImageFive(imageUrls[5] != null ? imageUrls[5] : "");
        payload.setDocFileURLFive(imageUrls[5] != null ? imageUrls[5] : "");
        payload.setDocFileURLKeyFive(imageKeys[5] != null ? imageKeys[5] : "");
        payload.setImageSix(imageUrls[6] != null ? imageUrls[6] : "");
        payload.setDocFileURLSix(imageUrls[6] != null ? imageUrls[6] : "");
        payload.setDocFileURLKeySix(imageKeys[6] != null ? imageKeys[6] : "");
        payload.setImageSeven(imageUrls[7] != null ? imageUrls[7] : "");
        payload.setDocFileURLSeven(imageUrls[7] != null ? imageUrls[7] : "");
        payload.setDocFileURLKeySeven(imageKeys[7] != null ? imageKeys[7] : "");
        payload.setImageEight(imageUrls[8] != null ? imageUrls[8] : "");
        payload.setDocFileURLEight(imageUrls[8] != null ? imageUrls[8] : "");
        payload.setDocFileURLKeyEight(imageKeys[8] != null ? imageKeys[8] : "");
        payload.setImageNine(imageUrls[9] != null ? imageUrls[9] : "");
        payload.setDocFileURLNine(imageUrls[9] != null ? imageUrls[9] : "");
        payload.setDocFileURLKeyNine(imageKeys[9] != null ? imageKeys[9] : "");

        return payload;
    }

    public void executeSave(String expenseType, String meetingType, String currency, String travelCategory, String transportMode, String sharedMode, String dedicatedMode) {
        ClaimSaveRequest request = prepareSavePayload(meetingType, currency, expenseType, travelCategory, transportMode, sharedMode, dedicatedMode);
        apiInterface.claimSave(authTokenHeader, request).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    _saveSuccess.setValue(true);
                    toastMessage.setValue("Claim saved as draft successfully");
                } else {
                    Log.e("ClaimSave", "Save error code: " + response.code());
                    toastMessage.setValue("Failed to save claim draft.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e("ClaimSave", "Save network error", t);
                toastMessage.setValue("Network error while saving claim.");
            }
        });
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
        selectedBusinessUnitName.setValue("");
        underProcessTextState.setValue("");
        selectedMealType.setValue("");
        isGuestClaim.setValue(false);
        foodRestaurant.setValue("");
        foodPersonsCount.setValue("1");
        foodDescription.setValue("");
        selectedAccommodationType.setValue("");
        accommodationName.setValue("");
        accommodationLocation.setValue("");
        accommodationState.setValue("");
        accommodationCheckIn.setValue("");
        accommodationCheckOut.setValue("");
        accommodationPurpose.setValue("");
        selectedMiscCategory.setValue("");
        billingPeriod.setValue("");
        netProvider.setValue("");
        billNumber.setValue("");
        vehicleNumber.setValue("");
        selectedFuelType.setValue("");
        fuelQuantity.setValue("");
        fuelStationName.setValue("");
        parkingLocation.setValue("");
        parkingDate.setValue("");
        parkingDuration.setValue("");
        tollVehicleNumber.setValue("");
        tollPlazaName.setValue("");
        tollLocation.setValue("");
        isTravelSelected.setValue(false);
        transportLayoutType.setValue(0);
        showCustomTransportInput.setValue(false);
        Arrays.fill(imageUrls, null);
        Arrays.fill(imageKeys, null);
        employeesList.setValue(new ArrayList<>(Collections.singletonList("Please Select")));

        // ══ NEW: Clear NEW fields too ══
        restaurantName.setValue("");
        numberOfPersons.setValue("");
        foodComment.setValue("");
        accommodationAddress.setValue("");
        accommodationRegion.setValue("");
        purposeOfStay.setValue("");
        internetProvider.setValue("");
        internetBillNumber.setValue("");
        fuelVehicleNumber.setValue("");
        fuelStation.setValue("");

        operationSuccess.setValue(false);  // [L2 FIX]
        employeeEmailMap.clear();          // [M3 FIX]

        // ══ NEW: Reset edit mode ══
        isEditMode = false;
        editingClaimId = null;
    }
}
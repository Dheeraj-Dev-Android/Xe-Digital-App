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
import app.xedigital.ai.model.allEmployee.AllEmployeeResponse;
import app.xedigital.ai.model.branch.UserBranchResponse;
import app.xedigital.ai.model.businessUnit.BusItem;
import app.xedigital.ai.model.businessUnit.BusinessUnitResponse;
import app.xedigital.ai.model.businessUnit.BusinessUnitSpinnerItem;
import app.xedigital.ai.model.claim.ExpenseRequest;
import app.xedigital.ai.model.claimLength.ClaimLengthResponse;
import app.xedigital.ai.model.claimPrice.ClaimPriceResponse;
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

    // Accommodation Observables
    public final MutableLiveData<String> selectedAccommodationType = new MutableLiveData<>("");
    public final MutableLiveData<String> accommodationName = new MutableLiveData<>("");
    public final MutableLiveData<String> accommodationLocation = new MutableLiveData<>("");
    public final MutableLiveData<String> accommodationState = new MutableLiveData<>("");
    public final MutableLiveData<String> accommodationCheckIn = new MutableLiveData<>("");
    public final MutableLiveData<String> accommodationCheckOut = new MutableLiveData<>("");
    public final MutableLiveData<String> accommodationPurpose = new MutableLiveData<>("");

    // Miscellaneous Observables
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

    public final String[] imageUrls = new String[10];
    public final String[] imageKeys = new String[10];
    // --- Food Details ---
    public final MutableLiveData<String> restaurantName = new MutableLiveData<>("");
    public final MutableLiveData<String> numberOfPersons = new MutableLiveData<>("");
    public final MutableLiveData<String> foodComment = new MutableLiveData<>("");
    public final MutableLiveData<String> foodTravelId = new MutableLiveData<>("");
    // --- Accommodation Details ---
    public final MutableLiveData<String> accommodationAddress = new MutableLiveData<>("");
    public final MutableLiveData<String> accommodationRegion = new MutableLiveData<>("");
    public final MutableLiveData<String> purposeOfStay = new MutableLiveData<>("");
    public final MutableLiveData<String> accommodationTravelId = new MutableLiveData<>("");
    // --- Miscellaneous Details: Internet Expense ---
    public final MutableLiveData<String> internetProvider = new MutableLiveData<>("");
    public final MutableLiveData<String> internetBillNumber = new MutableLiveData<>("");
    // --- Miscellaneous Details: Fuel Expense ---
    public final MutableLiveData<String> fuelVehicleNumber = new MutableLiveData<>("");
    public final MutableLiveData<String> fuelStation = new MutableLiveData<>("");
    private final APIInterface apiInterface;
    private final MutableLiveData<Boolean> _submissionSuccess = new MutableLiveData<>();
    public int claimLength;
    public String hrMail;
    private Employee employeeCache;
    private int bUemail;
    private String authTokenHeader;

    public ClaimManagementViewModel(@NonNull Application application) {
        super(application);
        apiInterface = APIClient.getInstance().getApi();
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
        apiInterface.getClaimPrices(authToken).enqueue(new Callback<ClaimPriceResponse>() {
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

    public void getEmployeesByBusinessUnit(String authTokenHeader, String businessUnitId) {
        if (businessUnitId == null || businessUnitId.isEmpty()) {
            return;
        }
        String authToken = "jwt " + authTokenHeader;
        apiInterface.getEmployeesByBusinessUnit(authToken, businessUnitId).enqueue(new Callback<EmployeeByBusinessUnitResponse>() {
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
                            bUemail = Integer.parseInt(emp.getEmail() != null ? emp.getEmail() : "");

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
        apiInterface.getAllEmployees(authToken).enqueue(new Callback<AllEmployeeResponse>() {
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
                    Log.d("AllEmployees", "All employees successfully parsed.");
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

    private String sanitize(String val) {
        if (val == null || val.trim().isEmpty() || "Please Select".equalsIgnoreCase(val.trim()) || "Select an option".equalsIgnoreCase(val.trim()) || "N/A".equalsIgnoreCase(val.trim())) {
            return "";
        }
        return val.trim();
    }

    private ExpenseRequest prepareBasePayload(String meetingType, String currency) {
        ExpenseRequest payload = new ExpenseRequest();

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
            }

            payload.setRequestedfirstname(sanitize(employeeCache.getFirstname()));
            payload.setRequestedlastname(sanitize(employeeCache.getLastname()));
        }
        payload.setBu(sanitize(selectedBusinessUnitId.getValue()));
        payload.setBuName(sanitize(selectedBusinessUnitName.getValue()));
        payload.setBuEmail(bUemail == 0 ? "" : String.valueOf(bUemail));
        payload.setClaimDate(sanitize(claimDate.getValue()));
        payload.setProject(sanitize(projectName.getValue()));
        payload.setMeeting(sanitize(meetingType != null ? meetingType.toLowerCase() : ""));
        payload.setPerposeofmeet(sanitize(purposeOfMeeting.getValue()));
        payload.setTotalClaim((double) claimLength);
        payload.setCurrency(sanitize(currency));
        payload.setTotalamount(sanitize(totalAmount.getValue()));
        payload.setRemark(sanitize(remarks.getValue()));
        payload.setStatus("Pending");
        payload.setConfbutton(false);
        payload.setModeofcal("general");
        payload.setModeofcalPascal("");
        payload.setRequest("Expense Claim Request");
        payload.setFromaddress(sanitize(startLocation.getValue()));
        payload.setToaddress(sanitize(endLocation.getValue()));
        payload.setDistance(sanitize(customTransportInput.getValue()));
        payload.setEmpTransport(sanitize(customTransportInput.getValue()));
        payload.setGuest(isGuestClaim.getValue() != null ? isGuestClaim.getValue() : false);
        payload.setMeantype(sanitize(selectedMealType.getValue()));
        payload.setRestaurant(sanitize(foodRestaurant.getValue()));
        payload.setPersons(sanitize(foodPersonsCount.getValue()));
        payload.setDescription(sanitize(foodDescription.getValue()));
        payload.setAccommodationtype(sanitize(selectedAccommodationType.getValue()));
        payload.setAccommodationname(sanitize(accommodationName.getValue()));
        payload.setLocation(sanitize(accommodationLocation.getValue()));
        payload.setState(sanitize(accommodationState.getValue()));
        payload.setCheckin(sanitize(accommodationCheckIn.getValue()));
        payload.setCheckout(sanitize(accommodationCheckOut.getValue()));
        payload.setPurposeofstay(sanitize(accommodationPurpose.getValue()));
        payload.setExpancecategory(sanitize(selectedMiscCategory.getValue()));
        payload.setBillingperiod(sanitize(billingPeriod.getValue()));
        payload.setNetprovider(sanitize(netProvider.getValue()));
        payload.setBillnumber(sanitize(billNumber.getValue()));
        payload.setVehiclenumber(sanitize(vehicleNumber.getValue()));
        payload.setFueltype(sanitize(selectedFuelType.getValue()));
        payload.setFuelquantity(sanitize(fuelQuantity.getValue()));
        payload.setFuelstationname(sanitize(fuelStationName.getValue()));
        payload.setParkinglocation(sanitize(parkingLocation.getValue()));
        payload.setParkingdate(sanitize(parkingDate.getValue()));
        payload.setDuration(sanitize(parkingDuration.getValue()));
        payload.setTollvehiclenumber(sanitize(tollVehicleNumber.getValue()));
        payload.setTollplazaname(sanitize(tollPlazaName.getValue()));
        payload.setTolllocation(sanitize(tollLocation.getValue()));

        // Document File Attachments
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

    // --- EXPENSE SUBMIT API TARGET METHODS ---

    public void executeFoodSubmit(String meetingType, String currency) {
        ExpenseRequest request = prepareBasePayload(meetingType, currency);
        request.setExpenseType("food");
        request.setGuest(isGuestClaim.getValue() != null ? isGuestClaim.getValue() : false);

        request.setMeantype(sanitize(selectedMealType.getValue()));
        request.setRestaurant(sanitize(foodRestaurant.getValue()));
        request.setPersons(sanitize(foodPersonsCount.getValue()));
        request.setDescription(sanitize(foodDescription.getValue()));

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
        request.setLocation(sanitize(accommodationLocation.getValue()));
        request.setState(sanitize(accommodationState.getValue()));
        request.setCheckin(sanitize(accommodationCheckIn.getValue()));
        request.setCheckout(sanitize(accommodationCheckOut.getValue()));
        request.setPurposeofstay(sanitize(accommodationPurpose.getValue()));

        executeNetworkCall(request, "Accommodation");
    }

    public void executeMiscSubmit(String meetingType, String currency) {
        ExpenseRequest request = prepareBasePayload(meetingType, currency);
        request.setExpenseType("miscellaneous");

        request.setExpancecategory(sanitize(selectedMiscCategory.getValue()));
        request.setBillingperiod(sanitize(billingPeriod.getValue()));
        request.setNetprovider(sanitize(netProvider.getValue()));
        request.setBillnumber(sanitize(billNumber.getValue()));
        request.setVehiclenumber(sanitize(vehicleNumber.getValue()));
        request.setFueltype(sanitize(selectedFuelType.getValue()));
        request.setFuelquantity(sanitize(fuelQuantity.getValue()));
        request.setFuelstationname(sanitize(fuelStationName.getValue()));
        request.setParkinglocation(sanitize(parkingLocation.getValue()));
        request.setParkingdate(sanitize(parkingDate.getValue()));
        request.setDuration(sanitize(parkingDuration.getValue()));
        request.setTollvehiclenumber(sanitize(tollVehicleNumber.getValue()));
        request.setTollplazaname(sanitize(tollPlazaName.getValue()));
        request.setTolllocation(sanitize(tollLocation.getValue()));

        executeNetworkCall(request, "Miscellaneous");
    }

    // --- DYNAMIC ENDPOINT ROUTING NETWORK EXECUTION BLOCK ---
    private void executeNetworkCall(ExpenseRequest requestBody, final String label) {
        Call<ResponseBody> apiCall;

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

        apiCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    operationSuccess.setValue(true);
                    _submissionSuccess.setValue(true);
                    toastMessage.setValue(label + " claim submitted successfully");
                } else {
                    Log.e("ExpenseSubmitAPI", label + " transaction error code: " + response.code());
                    toastMessage.setValue("Failed to process " + label.toLowerCase() + " request parameters.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e("ExpenseSubmitAPI", label + " networking engine terminal exception", t);
                toastMessage.setValue("Network transaction channel failed.");
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
    }
}
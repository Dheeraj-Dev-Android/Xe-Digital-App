package app.xedigital.ai.ui.deviceRegister;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.model.deviceRegistration.DeviceDetailsResponse;
import app.xedigital.ai.model.deviceRegistration.DeviceInfo;
import app.xedigital.ai.model.deviceRegistration.DeviceRegistrationRequest;
import app.xedigital.ai.utills.DeviceInfoHelper;
import app.xedigital.ai.utills.SecurePrefManager;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DeviceRegistrationViewModel extends AndroidViewModel {

    private static final String TAG = "DeviceRegViewModel";

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> successMessage = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isDeviceBlocked = new MutableLiveData<>(false);
    private final MutableLiveData<String> blockedReason = new MutableLiveData<>("");

    private final SecurePrefManager prefManager;
    private Call<DeviceDetailsResponse> checkCall;
    private Call<ResponseBody> registrationCall;

    public DeviceRegistrationViewModel(@NonNull Application application) {
        super(application);
        prefManager = SecurePrefManager.getInstance(application);
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getSuccessMessage() {
        return successMessage;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getIsDeviceBlocked() {
        return isDeviceBlocked;
    }

    public LiveData<String> getBlockedReason() {
        return blockedReason;
    }

    public void validateAndRegisterDevice() {
        Log.i(TAG, "🚀 Starting 2-Way Hardware Binding Verification...");

        String currentUserId = prefManager.getString("userId", null);
        String currentEmail = prefManager.getString("emailId", null);
        String authToken = prefManager.getString("authToken", null);

        Log.d(TAG, "🔍 Session Credentials Loaded:");
        Log.d(TAG, "   └─ User ID   : " + currentUserId);
        Log.d(TAG, "   └─ Email     : " + currentEmail);

        if (currentUserId == null) {
            Log.e(TAG, "❌ Aborting Validation Check: User ID is completely null!");
            errorMessage.setValue("User session not found. Please log in again.");
            return;
        }

        if (authToken == null) {
            Log.w(TAG, "   ⚠️ Session token null! Applying fallback bypass token.");
            authToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.YOUR_HARDCODED_DEBUG_TOKEN_HERE";
        }

        String authHeader = authToken.startsWith("jwt ") ? authToken : "jwt " + authToken;

        // =========================================================================
        // 🔒 CHECK 1 (Local Device-Lock): Is this phone locked to a different user?
        // =========================================================================
        String boundUserId = prefManager.getString("bound_user_id", null);
        String boundUserEmail = prefManager.getString("bound_user_email", null);

        if (boundUserId != null && !boundUserId.equals(currentUserId)) {
            Log.e(TAG, "🚨 [LOCAL BLOCK] Hardware already registered to: " + boundUserEmail);
            isLoading.setValue(false);
            blockedReason.setValue("This device is already registered to another account ("
                    + (boundUserEmail != null ? boundUserEmail : boundUserId)
                    + ").\n\nOnly one account is permitted per device.\n\nContact your Admin to release this device.");
            isDeviceBlocked.setValue(true);
            return;
        }

        isLoading.setValue(true);

        Log.d(TAG, "┌─────────────────────────────────────────────────────────");
        Log.d(TAG, "│ 📡 ENQUEUING RETROFIT API GET DEVICE DETAILS REQUEST");
        Log.d(TAG, "├─────────────────────────────────────────────────────────");
        Log.d(TAG, "│ URL          : " + APIClient.class.getSimpleName() + " -> getDeviceDetails");
        Log.d(TAG, "└─────────────────────────────────────────────────────────");

        // =========================================================================
        // 🔒 CHECK 2 (Server-Side): Is this user account bound to a DIFFERENT device?
        // =========================================================================
        checkCall = APIClient.getInstance().getDevice().getDeviceDetails(authHeader);
        checkCall.enqueue(new Callback<DeviceDetailsResponse>() {
            @Override
            public void onResponse(@NonNull Call<DeviceDetailsResponse> call, @NonNull Response<DeviceDetailsResponse> response) {
                Log.d(TAG, "┌─────────────────────────────────────────────────────────");
                Log.d(TAG, "│ 📥 GET DEVICE DETAILS API RESPONSE RECEIVED");
                Log.d(TAG, "├─────────────────────────────────────────────────────────");
                Log.d(TAG, "│ HTTP Status Code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    DeviceDetailsResponse details = response.body();

                    if (details.getData() != null && !details.getData().isEmpty()) {
                        DeviceDetailsResponse.DeviceDetailItem registeredDevice = details.getData().get(0);
                        String serverInstallId = registeredDevice.getInstallationId();
                        String localInstallId = DeviceInfoHelper.getDeviceInfo(getApplication(), currentUserId).getInstallationId();

                        Log.d(TAG, "│ Server Registered HW ID : " + serverInstallId);
                        Log.d(TAG, "│ Local Physical HW ID    : " + localInstallId);

                        if (localInstallId.equalsIgnoreCase(serverInstallId)) {
                            // Match confirmed: Lock user to phone locally too
                            prefManager.putString("bound_user_id", currentUserId);
                            if (currentEmail != null)
                                prefManager.putString("bound_user_email", currentEmail);

                            Log.i(TAG, "│ Status: OK - Hardware Match Verified.");
                            Log.d(TAG, "└─────────────────────────────────────────────────────────");
                            isLoading.setValue(false);
                            successMessage.setValue("Device Verified");
                        } else {
                            // Mismatch: Account is on another hardware
                            Log.e(TAG, "│ Status: BLOCKED - Account is bound to a different physical device.");
                            Log.e(TAG, "└─────────────────────────────────────────────────────────");
                            isLoading.setValue(false);
                            blockedReason.setValue("Your account is registered on another physical device ("
                                    + registeredDevice.getDeviceName()
                                    + ").\n\nYou can only access your account from your registered device.\n\nContact your Admin to release the old device.");
                            isDeviceBlocked.setValue(true);
                        }
                    } else {
                        // 🔓 ADMIN BYPASS: Server has no records = admin unbound them
                        Log.i(TAG, "│ 🔓 [ADMIN BYPASS] Empty binding on server. Clearing local locks & re-registering...");
                        Log.d(TAG, "└─────────────────────────────────────────────────────────");

                        // Wipe old lock cache and re-register
                        prefManager.remove("bound_user_id");
                        prefManager.remove("bound_user_email");
                        registerDevice(authHeader, currentUserId, currentEmail);
                    }
                } else {
                    isLoading.setValue(false);
                    Log.e(TAG, "│ Status: API execution failed or invalid response parse");
                    Log.d(TAG, "└─────────────────────────────────────────────────────────");
                    errorMessage.setValue("Validation check server failure.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<DeviceDetailsResponse> call, @NonNull Throwable t) {
                isLoading.setValue(false);
                Log.e(TAG, "┌─────────────────────────────────────────────────────────");
                Log.e(TAG, "│ 🚨 FAILURE: NETWORK ERROR CALLING GET DETAILS");
                Log.e(TAG, "├─────────────────────────────────────────────────────────");
                Log.e(TAG, "│ Cause: " + t.getMessage(), t);
                Log.e(TAG, "└─────────────────────────────────────────────────────────");
                errorMessage.setValue("Validation network error.");
            }
        });
    }

    private void registerDevice(String authHeader, String userId, String email) {
        DeviceInfo info = DeviceInfoHelper.getDeviceInfo(getApplication().getApplicationContext(), userId);
        DeviceRegistrationRequest request = new DeviceRegistrationRequest(
                info.getUserId(), info.getInstallationId(), info.getPlatform(),
                info.getDeviceName(), info.getManufacturer(), info.getModel(),
                info.getOsVersion(), info.getAppVersion(), info.getPushToken(), info.isActive()
        );

        Log.d(TAG, "┌─────────────────────────────────────────────────────────");
        Log.d(TAG, "│ 📡 ENQUEUING RETROFIT POST PAYLOAD SIGNUP DEVICE");
        Log.d(TAG, "├─────────────────────────────────────────────────────────");
        Log.d(TAG, "│ Endpoint Name: registerDevice");
        Log.d(TAG, "│ Device Details: " + request.getDeviceName());
        Log.d(TAG, "└─────────────────────────────────────────────────────────");

        registrationCall = APIClient.getInstance().getDevice().registerDevice(authHeader, request);
        registrationCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                isLoading.setValue(false);
                Log.d(TAG, "┌─────────────────────────────────────────────────────────");
                Log.d(TAG, "│ 📥 POST REGISTER DEVICE RESPONSE RECEIVED");
                Log.d(TAG, "├─────────────────────────────────────────────────────────");
                Log.d(TAG, "│ HTTP Status Code: " + response.code());

                if (response.isSuccessful()) {
                    // Bind the physical hardware to this user account
                    prefManager.putString("bound_user_id", userId);
                    if (email != null) prefManager.putString("bound_user_email", email);
                    prefManager.putBoolean("device_registered_" + userId, true);

                    Log.i(TAG, "│ Status: Success - Hardware permanently bound to " + userId);
                    Log.d(TAG, "└─────────────────────────────────────────────────────────");
                    successMessage.setValue("Device registered and bound successfully!");
                } else {
                    Log.e(TAG, "│ Status: Registration rejected by security policy on backend.");
                    Log.d(TAG, "└─────────────────────────────────────────────────────────");
                    errorMessage.setValue("Device registration denied.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                isLoading.setValue(false);
                Log.e(TAG, "┌─────────────────────────────────────────────────────────");
                Log.e(TAG, "│ 🚨 FAILURE: NETWORK CONNECTION TIMEOUT WRITING BIND");
                Log.e(TAG, "├─────────────────────────────────────────────────────────");
                Log.e(TAG, "│ Cause: " + t.getMessage());
                Log.d(TAG, "└─────────────────────────────────────────────────────────");
                errorMessage.setValue("Registration setup connection timeout.");
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (checkCall != null && !checkCall.isCanceled()) checkCall.cancel();
        if (registrationCall != null && !registrationCall.isCanceled()) registrationCall.cancel();
    }
}
package app.xedigital.ai.ui.claim_management;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.api.APIInterface;
import app.xedigital.ai.model.claimLength.ClaimLengthResponse;
import retrofit2.Call;
import retrofit2.Callback;

public class ClaimManagementViewModel extends ViewModel {
    public final APIInterface apiInterface;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final MutableLiveData<List<String>> meetingTypes = new MutableLiveData<>();
    private final MutableLiveData<List<String>> claimCategories = new MutableLiveData<>();
    private final MutableLiveData<List<String>> travelCategories = new MutableLiveData<>();
    private final MutableLiveData<List<String>> transportModes = new MutableLiveData<>();
    private final MutableLiveData<List<String>> sharedTransportModes = new MutableLiveData<>();
    private final MutableLiveData<List<String>> dedicatedTransportModes = new MutableLiveData<>();
    private final MutableLiveData<List<String>> currencyDropdown = new MutableLiveData<>();
    public int claimLength;

    public ClaimManagementViewModel() {
        apiInterface = APIClient.getInstance().getClaimLength();
        meetingTypes.setValue(Arrays.asList("Select an option", "Business", "Project", "Pre Sales"));
        claimCategories.setValue(Arrays.asList("Select an option", "General", "Standard"));
        travelCategories.setValue(Arrays.asList("Select an option", "Local", "Domestic", "International"));
        transportModes.setValue(Arrays.asList("Select an option", "Shared", "Dedicated"));
        sharedTransportModes.setValue(Arrays.asList("Select an option", "Auto", "Car", "E-Rickshaw", "Metro", "Others"));
        dedicatedTransportModes.setValue(Arrays.asList("Select an option", "Two-Wheeler", "Three-Wheeler", "Others"));
        currencyDropdown.setValue(Arrays.asList("Select an option", "INR", "USD", "EUR", "GBP", "JPY", "CNY", "AUD", "CAD", "CHF", "HKD", "SEK", "NZD"));
    }

    // Getters for dropdown data (exposed as LiveData)
    public LiveData<List<String>> getMeetingTypes() {
        return meetingTypes;
    }

    public LiveData<List<String>> getClaimCategories() {
        return claimCategories;
    }

    public LiveData<List<String>> getTravelCategories() {
        return travelCategories;
    }

    public LiveData<List<String>> getTransportModes() {
        return transportModes;
    }

    public LiveData<List<String>> getSharedTransportModes() {
        return sharedTransportModes;
    }

    public LiveData<List<String>> getDedicatedTransportModes() {
        return dedicatedTransportModes;
    }

    public LiveData<List<String>> getCurrencyDropdown() {
        return currencyDropdown;
    }

    public void getClaimLength(String authToken) {
        if (authToken != null) {
            String authHeaderValue = "jwt " + authToken;
            apiInterface.getClaimLength(authHeaderValue).enqueue(new Callback<ClaimLengthResponse>() {
                @Override
                public void onResponse(@NonNull Call<ClaimLengthResponse> call, @NonNull retrofit2.Response<ClaimLengthResponse> response) {
                    if (response.isSuccessful()) {
                        ClaimLengthResponse claimLengthResponse = response.body();
                        if (claimLengthResponse != null) {
                            claimLength = claimLengthResponse.getData().getCliamlength();
                            Log.d("ClaimLength", "claimLength : " + claimLength);
                        } else {
                            Log.e("ClaimLength", "Response body is null");
                        }
                    } else {
                        try {
                            if (response.errorBody() != null) {
                                String errorBody = response.errorBody().string();
                                Log.e("ClaimLength", "Response Error: " + response.code() + ", " + errorBody);
                            } else {
                                Log.e("ClaimLength", "Response Error: " + response.code() + ", Error body is null");
                            }
                        } catch (IOException e) {
                            Log.e("ClaimLength", "Error reading error body: " + e.getMessage());
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ClaimLengthResponse> call, @NonNull Throwable throwable) {
                    Log.e("ClaimLength", "Network Error: " + throwable.getMessage());
                }
            });
        } else {
            Log.e("LeavesViewModel", "Auth Token is null");
        }
    }
}
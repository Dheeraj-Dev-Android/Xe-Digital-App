package app.xedigital.ai.ui.claim_management;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;

import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.api.APIInterface;
import app.xedigital.ai.model.claimLength.ClaimLengthResponse;
import retrofit2.Call;
import retrofit2.Callback;

public class ClaimManagementViewModel extends ViewModel {
    public final APIInterface apiInterface;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    String authToken;
    int claimLength;


    public ClaimManagementViewModel() {
        apiInterface = APIClient.getInstance().getClaimLength();
    }


    public void getClaimLength(String authToken) {
        if (authToken != null) {
            Log.d("ClaimManagementViewModel", "Auth Token: " + authToken.substring(0, 10) + "...");
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
                        // Handle error responses
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
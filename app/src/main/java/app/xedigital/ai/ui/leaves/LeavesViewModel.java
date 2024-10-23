package app.xedigital.ai.ui.leaves;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.api.APIInterface;
import app.xedigital.ai.model.leaves.LeavesResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LeavesViewModel extends ViewModel {

    private final MutableLiveData<LeavesResponse> _leavesData = new MutableLiveData<>();
    public LiveData<LeavesResponse> leavesData = _leavesData;
    private final APIInterface apiInterface;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private String authToken;

    public LeavesViewModel() {
        apiInterface = APIClient.getInstance().getLeaves();
    }

    public void setUserId(String authToken) {
        this.authToken = authToken;
    }

    public void fetchLeavesData() {
        Log.d("LeavesViewModel", "Fetching leaves data...");

        if (authToken != null) {
            Log.d("LeavesViewModel", "Auth Token: " + authToken.substring(0, 10) + "...");
            String authHeaderValue = "jwt " + authToken;

            apiInterface.getLeaves(authHeaderValue).enqueue(new Callback<LeavesResponse>() {
                @Override
                public void onResponse(@NonNull Call<LeavesResponse> call, @NonNull Response<LeavesResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        mainHandler.post(() -> _leavesData.setValue(response.body()));
                        String responseJson = gson.toJson(response.body());
                        Log.d("LeavesViewModel", "Leave Response:\n " + responseJson);
                    } else {
                        Log.e("LeavesViewModel", "Error fetching leaves data: " + response.message());
                    }
                }
                @Override
                public void onFailure(@NonNull Call<LeavesResponse> call, @NonNull Throwable throwable) {
                    Log.e("LeavesViewModel", "Error fetching leaves data: " + throwable.getMessage());
                }
            });
        } else {
            Log.e("LeavesViewModel", "Auth Token is null");
        }
    }
}

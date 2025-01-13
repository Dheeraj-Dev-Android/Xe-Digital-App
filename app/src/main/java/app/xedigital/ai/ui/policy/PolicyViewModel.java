package app.xedigital.ai.ui.policy;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.gson.Gson;

import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.model.policy.PolicyResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PolicyViewModel extends ViewModel {

    private final MutableLiveData<String> mText = new MutableLiveData<>();
    private final MutableLiveData<PolicyResponse> policyData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<Throwable> error = new MutableLiveData<>();

    public PolicyViewModel() {
        mText.setValue("This is Policy fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }

    public LiveData<PolicyResponse> getPolicyData() {
        return policyData;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<Throwable> getError() {
        return error;
    }

    public void fetchPolicies(String authToken) {
        isLoading.setValue(true);

        Call<PolicyResponse> call = APIClient.getInstance().getPolicies().getPolicies(authToken);
        call.enqueue(new Callback<PolicyResponse>() {
            @Override
            public void onResponse(@NonNull Call<PolicyResponse> call, @NonNull Response<PolicyResponse> response) {
                isLoading.setValue(false);
                if (response.isSuccessful()) {
                    policyData.setValue(response.body());

                    // Log the JSON response using Gson
                    Gson gson = new Gson();
                    String jsonResponse = gson.toJson(response.body());
//                    Log.d("PolicyViewModel", "Policies Response JSON: " + jsonResponse);
                } else {
                    error.setValue(new Exception("API request failed with code " + response.code()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<PolicyResponse> call, @NonNull Throwable t) {
                isLoading.setValue(false);
                error.setValue(t);
            }
        });
    }
}
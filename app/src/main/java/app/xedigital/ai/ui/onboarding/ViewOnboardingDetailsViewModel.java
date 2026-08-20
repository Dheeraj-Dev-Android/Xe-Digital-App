package app.xedigital.ai.ui.onboarding;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.api.APIInterface;
import app.xedigital.ai.model.employeeOnboarding.EmployeeOnboardingResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ViewOnboardingDetailsViewModel extends ViewModel {

    // ── LiveData ──────────────────────────────────────────────────────────────
    public final MutableLiveData<EmployeeOnboardingResponse> onboardingResponse =
            new MutableLiveData<>();

    public final MutableLiveData<String> errorMessage =
            new MutableLiveData<>();

    public final MutableLiveData<Boolean> isLoading =
            new MutableLiveData<>(false);

    // ── API Interface ─────────────────────────────────────────────────────────
    private final APIInterface apiInterface =
            APIClient.getInstance().getApi();

    // ── Fetch Onboarding Details ──────────────────────────────────────────────
    public void fetchOnboardingDetails(String authToken, String userId) {

        isLoading.setValue(true);

        apiInterface.getOnBoardEmployeeById(authToken, userId)
                .enqueue(new Callback<EmployeeOnboardingResponse>() {

                    @Override
                    public void onResponse(
                            Call<EmployeeOnboardingResponse> call,
                            Response<EmployeeOnboardingResponse> response) {

                        isLoading.setValue(false);

                        if (response.isSuccessful()
                                && response.body() != null) {
                            onboardingResponse.setValue(response.body());
                        } else {
                            errorMessage.setValue(
                                    "Failed to load onboarding details.");
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<EmployeeOnboardingResponse> call,
                            Throwable t) {

                        isLoading.setValue(false);
                        errorMessage.setValue(
                                "Network error: " + t.getMessage());
                    }
                });
    }
}
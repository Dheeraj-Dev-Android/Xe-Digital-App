package app.xedigital.ai.ui.teamTimesheet;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.model.TeamTimesheetResponse.TeamTimesheetResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeamTimesheetViewModel extends ViewModel {
    private final MutableLiveData<TeamTimesheetResponse> dcrDataResponse = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    public LiveData<TeamTimesheetResponse> getDcrDataResponse() {
        return dcrDataResponse;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void fetchDcrDataForRM(String authToken, String userId) {
        if (authToken == null || userId == null || authToken.trim().isEmpty() || userId.trim().isEmpty()) {
            errorMessage.setValue("Session expired. Please log in again.");
            return;
        }
        isLoading.setValue(true);
        String formattedToken = authToken.startsWith("jwt ") ? authToken : "jwt " + authToken;

        APIClient.getInstance().getApi().getDcrDataForRM(formattedToken, userId).enqueue(new Callback<TeamTimesheetResponse>() {
            @Override
            public void onResponse(@NonNull Call<TeamTimesheetResponse> call, @NonNull Response<TeamTimesheetResponse> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isSuccess() && response.body().getData() != null) {
                        dcrDataResponse.setValue(response.body());
                    } else {
                        String msg = response.body().getMessage() != null ? response.body().getMessage() : "No records matching query found.";
                        errorMessage.setValue(msg);
                    }
                } else {
                    errorMessage.setValue("Server error response: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<TeamTimesheetResponse> call, @NonNull Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue(t != null ? t.getMessage() : "Network error. Please try again.");
            }
        });
    }
}
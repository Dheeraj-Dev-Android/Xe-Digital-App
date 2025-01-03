package app.xedigital.ai.ui.vms;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.api.APIInterface;
import app.xedigital.ai.ui.userProfileEmail.UserProfileByEmailResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VisitorPreApprovedViewModel extends ViewModel {

    private final MutableLiveData<UserProfileByEmailResponse> userProfileLiveData;
    private final APIInterface apiService;

    public VisitorPreApprovedViewModel() {
        userProfileLiveData = new MutableLiveData<UserProfileByEmailResponse>();
        apiService = APIClient.getInstance().getUser();
    }

    public LiveData<UserProfileByEmailResponse> getUserProfileLiveData() {
        return userProfileLiveData;
    }


    public void fetchUserProfile(String empEmail, String authToken) {

        Call<UserProfileByEmailResponse> call = apiService.getUserProfileByEmail(empEmail, authToken);
        call.enqueue(new Callback<UserProfileByEmailResponse>() {
            @Override
            public void onResponse(@NonNull Call<UserProfileByEmailResponse> call, @NonNull Response<UserProfileByEmailResponse> response) {
                if (response.isSuccessful()) {
                    userProfileLiveData.setValue(response.body());
                } else {

                    userProfileLiveData.setValue(null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserProfileByEmailResponse> call, @NonNull Throwable t) {

                userProfileLiveData.setValue(null);
            }
        });
    }
}
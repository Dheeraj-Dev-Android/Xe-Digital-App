package app.xedigital.ai.ui.vms;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.api.APIInterface;
import app.xedigital.ai.model.bucket.BucketRequest;
import app.xedigital.ai.model.bucket.BucketResponse;
import app.xedigital.ai.model.faceAdd.AddFaceRequest;
import app.xedigital.ai.model.faceAdd.AddFaceResponse;
import app.xedigital.ai.model.userProfileEmail.UserProfileByEmailResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VisitorPreApprovedViewModel extends ViewModel {

    private final MutableLiveData<UserProfileByEmailResponse> userProfileLiveData;
    private final APIInterface apiService;

    public VisitorPreApprovedViewModel() {
        userProfileLiveData = new MutableLiveData<>();
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

    public void addFace(String token, AddFaceRequest requestBody, Callback<AddFaceResponse> callback) {
        apiService.FaceAddApi(token, requestBody).enqueue(callback);
    }

    public void addBucket(String token, BucketRequest requestBody, Callback<BucketResponse> callback) {
        apiService.addBucket(token, requestBody).enqueue(callback);
    }

}
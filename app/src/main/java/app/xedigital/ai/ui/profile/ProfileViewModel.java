package app.xedigital.ai.ui.profile;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.api.APIInterface;
import app.xedigital.ai.model.profile.Crossmanager;
import app.xedigital.ai.model.profile.UserProfileResponse;
import app.xedigital.ai.utills.CrossmanagerTypeAdapter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileViewModel extends ViewModel {
    private static final String TAG = "ProfileViewModel";
    private final MutableLiveData<UserProfileResponse> _userProfile = new MutableLiveData<>();
    private final APIInterface apiInterface;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
//    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private final Gson gson;
    public LiveData<UserProfileResponse> userProfile = _userProfile;
    private String userId;
    private String authToken;

    //    public LiveData<UserProfileResponse> getUserProfile(){
//        return userProfile;
//    }
    public ProfileViewModel() {
        apiInterface = APIClient.getInstance().getLogin();
        APIInterface apiInterface1 = APIClient.getInstance().getUser();

//        // Initialize Gson with the custom deserializer
        gson = new GsonBuilder()
                .registerTypeAdapter(Crossmanager.class, new CrossmanagerTypeAdapter())
                .setPrettyPrinting()
                .create();
    }


    public void storeLoginData(String userId, String authToken) {
        this.userId = userId;
        this.authToken = authToken;
    }

    public void fetchUserProfile() {
        Log.d(TAG, "Fetching user profile...");
        if (userId != null && authToken != null) {
            new Thread(() -> {
                String authHeaderValue = "jwt " + authToken;

                apiInterface.getUserProfile(userId, authHeaderValue).enqueue(new Callback<UserProfileResponse>() {

                    @Override
                    public void onResponse(@NonNull Call<UserProfileResponse> call, @NonNull Response<UserProfileResponse> response) {
                        if (response.isSuccessful()) {
                            mainHandler.post(() -> _userProfile.setValue(response.body()));
                            String responseJson = gson.toJson(response.body());
                            Log.d(TAG, "Response JSON: " + responseJson);
                        } else {
                            Log.e("ProfileViewModel", "Error: userId or authToken is null. Cannot fetch profile.");
                            System.err.println("API Error: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<UserProfileResponse> call, @NonNull Throwable t) {
                        System.err.println("Network Error: " + t.getMessage());
                    }
                });
            }).start();
        } else {
            Log.e("ProfileViewModel", "Error: userId or authToken is null. Cannot fetch profile.");
        }
    }


}
package app.xedigital.ai.utills;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import app.xedigital.ai.model.Admin.UserDetails.UserDetailsResponse;

public class UserViewModel extends ViewModel {
    private final MutableLiveData<UserDetailsResponse> userDetails = new MutableLiveData<>();

    public LiveData<UserDetailsResponse> getUserDetails() {
        return userDetails;
    }

    public void setUserDetails(UserDetailsResponse details) {
        userDetails.setValue(details);
    }
}

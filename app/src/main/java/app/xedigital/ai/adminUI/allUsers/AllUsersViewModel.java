package app.xedigital.ai.adminUI.allUsers;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import app.xedigital.ai.adminApi.AdminAPIClient;
import app.xedigital.ai.adminApi.AdminAPIInterface;
import app.xedigital.ai.model.Admin.AdminUsers.AdminUserResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AllUsersViewModel extends ViewModel {

    private final MutableLiveData<AdminUserResponse> userResponse = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public LiveData<AdminUserResponse> getUserResponse() {
        return userResponse;
    }

    public LiveData<Boolean> getLoadingStatus() {
        return isLoading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public void fetchAllUsers(String authToken) {
        isLoading.setValue(true);
        AdminAPIInterface service = AdminAPIClient.getInstance().getBase2();
        Call<AdminUserResponse> call = service.getAllUsers(authToken);

        call.enqueue(new Callback<AdminUserResponse>() {
            @Override
            public void onResponse(@NonNull Call<AdminUserResponse> call, @NonNull Response<AdminUserResponse> response) {
                isLoading.setValue(false);
                if (response.isSuccessful()) {
                    userResponse.setValue(response.body());
                } else {
                    error.setValue("Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<AdminUserResponse> call, Throwable t) {
                isLoading.setValue(false);
                error.setValue("Failure: " + t.getMessage());
            }
        });
    }
}

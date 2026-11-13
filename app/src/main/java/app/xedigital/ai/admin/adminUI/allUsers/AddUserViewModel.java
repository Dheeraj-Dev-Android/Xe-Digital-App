package app.xedigital.ai.admin.adminUI.allUsers;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import app.xedigital.ai.admin.adminAPI.AdminAPIClient;
import app.xedigital.ai.admin.adminAPI.AdminAPIInterface;
import app.xedigital.ai.admin.adminModal.Role.RolesItem;
import app.xedigital.ai.admin.adminModal.Role.UserRoleResponse;
import app.xedigital.ai.admin.adminModal.registerNewUser.RegisterUserRequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddUserViewModel extends ViewModel {

    private final MutableLiveData<List<RolesItem>> rolesLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> successMessage = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public LiveData<List<RolesItem>> getRoles() {
        return rolesLiveData;
    }

    public LiveData<Boolean> getLoadingStatus() {
        return isLoading;
    }

    public LiveData<String> getSuccessMessage() {
        return successMessage;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void fetchRoles(String authToken) {
        isLoading.setValue(true);
        AdminAPIInterface service = AdminAPIClient.getInstance().getBase2();
        Call<UserRoleResponse> call = service.getRoles(authToken);

        call.enqueue(new Callback<UserRoleResponse>() {
            @Override
            public void onResponse(@NonNull Call<UserRoleResponse> call, @NonNull Response<UserRoleResponse> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null && response.body().getData().getRoles() != null) {
                    rolesLiveData.setValue(response.body().getData().getRoles());
                } else {
                    errorMessage.setValue("Failed to load roles: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserRoleResponse> call, @NonNull Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue("Network error: " + t.getMessage());
            }
        });
    }

    public void registerUser(String authToken, RegisterUserRequestBody body) {
        isLoading.setValue(true);
        AdminAPIInterface service = AdminAPIClient.getInstance().getBase2();
        Call<ResponseBody> call = service.addNewUser(authToken, body);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                isLoading.setValue(false);
                if (response.isSuccessful()) {
                    successMessage.setValue("User created successfully.");
                } else {
                    errorMessage.setValue("Failed to create user. Server returned code: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue("Network error: " + t.getMessage());
            }
        });
    }
}
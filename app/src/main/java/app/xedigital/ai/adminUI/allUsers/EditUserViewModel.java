package app.xedigital.ai.adminUI.allUsers;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import app.xedigital.ai.adminApi.AdminAPIClient;
import app.xedigital.ai.adminApi.AdminAPIInterface;
import app.xedigital.ai.model.Admin.Role.RolesItem;
import app.xedigital.ai.model.Admin.Role.UserRoleResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditUserViewModel extends ViewModel {

    private final MutableLiveData<List<RolesItem>> rolesLiveData = new MutableLiveData<>();

    public LiveData<List<RolesItem>> getRolesLiveData() {
        return rolesLiveData;
    }

    public void fetchRoles(String authToken) {
        AdminAPIInterface apiService = AdminAPIClient.getInstance().getBase2();
        apiService.getRoles(authToken).enqueue(new Callback<UserRoleResponse>() {
            @Override
            public void onResponse(@NonNull Call<UserRoleResponse> call, @NonNull Response<UserRoleResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    rolesLiveData.postValue(response.body().getData().getRoles());
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserRoleResponse> call, @NonNull Throwable t) {
                rolesLiveData.postValue(null); // Or handle with error message LiveData
            }
        });
    }
}

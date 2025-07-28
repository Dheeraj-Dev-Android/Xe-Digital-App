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
//    private final Context context;

//    public EditUserViewModel(Context context) {
//        this.context = context.getApplicationContext();
//    }


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
                rolesLiveData.postValue(null);
            }
        });
    }

//    public void updateUser(String authToken, String userId, RequestBody requestBody) {
//        AdminAPIInterface apiService = AdminAPIClient.getInstance().getBase2();
//        Call<ResponseBody> call = apiService.updateUser(authToken, userId, requestBody);
//
//        call.enqueue(new Callback<ResponseBody>() {
//            @Override
//            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
//                if (response.isSuccessful()) {
//                    Toast.makeText(context, "User updated successfully", Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(context, "Failed to update user: " + response.code(), Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onFailure(@NonNull Call<ResponseBody> call, Throwable t) {
//                Toast.makeText(context, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
////                Toast.makeText(context, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
//    }

}

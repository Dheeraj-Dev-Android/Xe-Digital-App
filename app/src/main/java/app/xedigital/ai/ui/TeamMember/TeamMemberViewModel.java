package app.xedigital.ai.ui.TeamMember;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.api.APIInterface;
import app.xedigital.ai.model.TeamMember.TeamMemberResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeamMemberViewModel extends ViewModel {

    private final MutableLiveData<TeamMemberResponse> teamMemberData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public LiveData<TeamMemberResponse> getTeamMemberData() {
        return teamMemberData;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    //    public void fetchTeamMembers(String authToken, String userId) {
//        isLoading.setValue(true);
//
//        // Using your specific APIClient and APIInterface naming
//        APIInterface apiInterface = APIClient.getInstance().getApi();
//
//        Call<TeamMemberResponse> call = apiInterface.getEmployeesByManager("jwt " + authToken, userId);
//
//        call.enqueue(new Callback<TeamMemberResponse>() {
//            @Override
//            public void onResponse(@NonNull Call<TeamMemberResponse> call, @NonNull Response<TeamMemberResponse> response) {
//                isLoading.setValue(false);
//                if (response.isSuccessful() && response.body() != null) {
//                    teamMemberData.setValue(response.body());
//                } else {
//                    errorMessage.setValue("Error: " + response.code());
//                }
//            }
//
//            @Override
//            public void onFailure(@NonNull Call<TeamMemberResponse> call, @NonNull Throwable t) {
//                isLoading.setValue(false);
//                errorMessage.setValue(t.getMessage());
//            }
//        });
//    }
    public void fetchTeamMembers(String authToken, String userId) {
        isLoading.setValue(true);
        Log.e("TreeDebug", "Fetching API for UserId: " + userId);

        APIInterface apiInterface = APIClient.getInstance().AppliedLeave();
        Call<TeamMemberResponse> call = apiInterface.getEmployeesByManager("jwt " + authToken, userId);

        call.enqueue(new Callback<TeamMemberResponse>() {
            @Override
            public void onResponse(@NonNull Call<TeamMemberResponse> call, @NonNull Response<TeamMemberResponse> response) {
                isLoading.setValue(false);
                Log.e("TreeDebug", "Response Code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    Log.e("TreeDebug", "API Success. Message: " + response.body().getMessage());
                    if (response.body().getData() != null) {
                        Log.e("TreeDebug", "Employee Count: " + (response.body().getData().getEmployees() != null ? response.body().getData().getEmployees().size() : 0));
                    }
                    teamMemberData.setValue(response.body());
                } else {
                    Log.e("TreeDebug", "API Response Error Body: " + response.errorBody());
                    errorMessage.setValue("Error: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<TeamMemberResponse> call, @NonNull Throwable t) {
                isLoading.setValue(false);
                Log.e("TreeDebug", "API Failure: " + t.getMessage(), t);
                errorMessage.setValue(t.getMessage());
            }
        });
    }
}
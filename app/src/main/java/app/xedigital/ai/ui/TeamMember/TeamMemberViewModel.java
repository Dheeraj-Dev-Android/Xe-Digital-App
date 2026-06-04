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

    private final MutableLiveData<TeamMemberResponse> subTeamData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isSubTeamLoading = new MutableLiveData<>();

    public LiveData<TeamMemberResponse> getTeamMemberData() {
        return teamMemberData;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<TeamMemberResponse> getSubTeamData() {
        return subTeamData;
    }

    public LiveData<Boolean> getIsSubTeamLoading() {
        return isSubTeamLoading;
    }

    public void clearSubTeamData() {
        subTeamData.postValue(null);
    }

    public void fetchTeamMembers(String authToken, String userId) {
        isLoading.setValue(true);
        Log.e("TreeDebug", "Fetching API for UserId: " + userId);

        APIInterface apiInterface = APIClient.getInstance().AppliedLeave();
        Call<TeamMemberResponse> call = apiInterface.getEmployeesByManager("jwt " + authToken, userId);

        call.enqueue(new Callback<TeamMemberResponse>() {
            @Override
            public void onResponse(@NonNull Call<TeamMemberResponse> call, @NonNull Response<TeamMemberResponse> response) {
                // FIX: Utilizing postValue ensures continuous background thread-safety execution paths
                isLoading.postValue(false);
                Log.e("TreeDebug", "Response Code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    teamMemberData.postValue(response.body());
                } else {
                    errorMessage.postValue("Error: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<TeamMemberResponse> call, @NonNull Throwable t) {
                isLoading.postValue(false);
                errorMessage.postValue(t.getMessage());
            }
        });
    }

    public void fetchSubTeamMembers(String authToken, String clickedUserId) {
        isSubTeamLoading.setValue(true);

        APIInterface apiInterface = APIClient.getInstance().AppliedLeave();
        Call<TeamMemberResponse> call = apiInterface.getEmployeesByManager("jwt " + authToken, clickedUserId);

        call.enqueue(new Callback<TeamMemberResponse>() {
            @Override
            public void onResponse(@NonNull Call<TeamMemberResponse> call, @NonNull Response<TeamMemberResponse> response) {
                isSubTeamLoading.postValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    subTeamData.postValue(response.body());
                } else {
                    subTeamData.postValue(null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<TeamMemberResponse> call, @NonNull Throwable t) {
                isSubTeamLoading.postValue(false);
                subTeamData.postValue(null);
            }
        });
    }
}
package app.xedigital.ai.ui.mrm;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.model.meetingRoom.MeetingRoomResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MeetingRoomViewModel extends ViewModel {

    private final MutableLiveData<MeetingRoomResponse> meetingsLiveData = new MutableLiveData<MeetingRoomResponse>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>();

    public LiveData<MeetingRoomResponse> getMeetingsLiveData() {
        return meetingsLiveData;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    public LiveData<Boolean> getIsLoadingLiveData() {
        return isLoadingLiveData;
    }

    public void fetchMeetings(String authToken) {
        if (authToken == null) {
            errorLiveData.setValue("Authentication token is missing");
            return;
        }

        isLoadingLiveData.setValue(true);

        APIClient.getInstance().getApi().getMeetings("jwt " + authToken).enqueue(new Callback<MeetingRoomResponse>() {
            @Override
            public void onResponse(@NonNull Call<MeetingRoomResponse> call, @NonNull Response<MeetingRoomResponse> response) {
                isLoadingLiveData.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    meetingsLiveData.setValue(response.body());
                } else {
                    errorLiveData.setValue("Failed to fetch meetings: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<MeetingRoomResponse> call, @NonNull Throwable t) {
                isLoadingLiveData.setValue(false);
                errorLiveData.setValue(t.getMessage());
            }
        });
    }
}
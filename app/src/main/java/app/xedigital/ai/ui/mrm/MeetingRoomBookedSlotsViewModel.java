package app.xedigital.ai.ui.mrm;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.model.MeetingRoomBookedSlotsResponse.MeetingRoomBookedSlotsResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MeetingRoomBookedSlotsViewModel extends ViewModel {

    private final MutableLiveData<MeetingRoomBookedSlotsResponse> bookedSlotsLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>();

    public LiveData<MeetingRoomBookedSlotsResponse> getBookedSlotsLiveData() {
        return bookedSlotsLiveData;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    public LiveData<Boolean> getIsLoadingLiveData() {
        return isLoadingLiveData;
    }

    public void fetchBookedSlots(String authToken, String roomId) {
        if (authToken == null || roomId == null) {
            errorLiveData.setValue("Missing auth token or room id");
            return;
        }

        isLoadingLiveData.setValue(true);

        APIClient.getInstance().getApi()
                .getMeetingRoomBookedSlots("jwt " + authToken, roomId)
                .enqueue(new Callback<MeetingRoomBookedSlotsResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<MeetingRoomBookedSlotsResponse> call,
                                           @NonNull Response<MeetingRoomBookedSlotsResponse> response) {
                        isLoadingLiveData.setValue(false);
                        if (response.isSuccessful() && response.body() != null) {
                            bookedSlotsLiveData.setValue(response.body());
                        } else {
                            errorLiveData.setValue("Failed to fetch booked slots: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<MeetingRoomBookedSlotsResponse> call,
                                          @NonNull Throwable t) {
                        isLoadingLiveData.setValue(false);
                        errorLiveData.setValue(t.getMessage());
                    }
                });
    }
}
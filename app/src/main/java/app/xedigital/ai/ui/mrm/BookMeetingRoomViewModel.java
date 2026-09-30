package app.xedigital.ai.ui.mrm;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.model.MeetingRoomBookedSlotsResponse.MeetingRoomBookedSlotsResponse;
import app.xedigital.ai.model.meetingRoom.MeetingRoomBookingRequest;
import app.xedigital.ai.model.profile.UserProfileResponse;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookMeetingRoomViewModel extends ViewModel {

    // Host & Profile LiveData
    private final MutableLiveData<UserProfileResponse> userProfileLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>();

    // Booked Slots LiveData
    private final MutableLiveData<MeetingRoomBookedSlotsResponse> bookedSlotsLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> bookedSlotsErrorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isBookedSlotsLoadingLiveData = new MutableLiveData<>();

    // Booking Submission LiveData
    private final MutableLiveData<Boolean> bookingSuccessLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> bookingErrorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isBookingSubmittingLiveData = new MutableLiveData<>();

    // Getters
    public LiveData<UserProfileResponse> getUserProfileLiveData() {
        return userProfileLiveData;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    public LiveData<Boolean> getIsLoadingLiveData() {
        return isLoadingLiveData;
    }

    public LiveData<MeetingRoomBookedSlotsResponse> getBookedSlotsLiveData() {
        return bookedSlotsLiveData;
    }

    public LiveData<String> getBookedSlotsErrorLiveData() {
        return bookedSlotsErrorLiveData;
    }

    public LiveData<Boolean> getBookingSuccessLiveData() {
        return bookingSuccessLiveData;
    }

    public LiveData<String> getBookingErrorLiveData() {
        return bookingErrorLiveData;
    }

    public LiveData<Boolean> getIsBookingSubmittingLiveData() {
        return isBookingSubmittingLiveData;
    }

    // Fetch Host Profile
    public void fetchUserProfile(String userId, String authToken) {
        if (userId == null || authToken == null) {
            errorLiveData.setValue("Missing user id or auth token");
            return;
        }
        isLoadingLiveData.setValue(true);
        APIClient.getInstance().getApi()
                .getUserProfile(userId, "jwt " + authToken)
                .enqueue(new Callback<UserProfileResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<UserProfileResponse> call, @NonNull Response<UserProfileResponse> response) {
                        isLoadingLiveData.setValue(false);
                        if (response.isSuccessful() && response.body() != null) {
                            userProfileLiveData.setValue(response.body());
                        } else {
                            errorLiveData.setValue("Failed to load host: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<UserProfileResponse> call, @NonNull Throwable t) {
                        isLoadingLiveData.setValue(false);
                        errorLiveData.setValue(t.getMessage());
                    }
                });
    }

    // Fetch Booked Slots
    public void fetchBookedSlots(String authToken, String roomId) {
        if (authToken == null || roomId == null) {
            bookedSlotsErrorLiveData.setValue("Missing auth token or room id");
            return;
        }
        isBookedSlotsLoadingLiveData.setValue(true);
        APIClient.getInstance().getApi()
                .getMeetingRoomBookedSlots("jwt " + authToken, roomId)
                .enqueue(new Callback<MeetingRoomBookedSlotsResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<MeetingRoomBookedSlotsResponse> call, @NonNull Response<MeetingRoomBookedSlotsResponse> response) {
                        isBookedSlotsLoadingLiveData.setValue(false);
                        if (response.isSuccessful() && response.body() != null) {
                            bookedSlotsLiveData.setValue(response.body());
                        } else {
                            bookedSlotsErrorLiveData.setValue("Failed to load booked slots: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<MeetingRoomBookedSlotsResponse> call, @NonNull Throwable t) {
                        isBookedSlotsLoadingLiveData.setValue(false);
                        bookedSlotsErrorLiveData.setValue(t.getMessage());
                    }
                });
    }

    // Submit Meeting Room Booking
    public void executeBooking(String authToken, MeetingRoomBookingRequest request) {
        if (authToken == null || request == null) {
            bookingErrorLiveData.setValue("Authorization details or payload is missing");
            return;
        }
        isBookingSubmittingLiveData.setValue(true);

        APIClient.getInstance().getApi()
                .MeetingRoomBooking("jwt " + authToken, request)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                        isBookingSubmittingLiveData.setValue(false);
                        if (response.isSuccessful()) {
                            bookingSuccessLiveData.setValue(true);
                        } else {
                            try {
                                String errorMsg = response.errorBody() != null ? response.errorBody().string() : "Error " + response.code();
                                bookingErrorLiveData.setValue(errorMsg);
                            } catch (Exception e) {
                                bookingErrorLiveData.setValue("An error occurred during reservation.");
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                        isBookingSubmittingLiveData.setValue(false);
                        bookingErrorLiveData.setValue(t.getMessage());
                    }
                });
    }

    public void resetBookingStatus() {
        bookingSuccessLiveData.setValue(null);
        bookingErrorLiveData.setValue(null);
    }
}
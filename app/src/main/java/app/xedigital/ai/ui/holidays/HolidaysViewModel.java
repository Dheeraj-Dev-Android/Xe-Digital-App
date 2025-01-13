package app.xedigital.ai.ui.holidays;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.api.APIInterface;
import app.xedigital.ai.model.holiday.HolidayModelResponse;
import app.xedigital.ai.model.holiday.HolidaysItem;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HolidaysViewModel extends ViewModel {

    private final MutableLiveData<List<HolidaysItem>> holidaysList;

    public HolidaysViewModel() {
        holidaysList = new MutableLiveData<>();
    }

    public LiveData<List<HolidaysItem>> getHolidaysList() {
        return holidaysList;
    }

    public void loadHolidays(String authToken) {
        APIInterface service = APIClient.getInstance().getHolidays();

        Call<HolidayModelResponse> call = service.getHolidays("jwt " + authToken);
        call.enqueue(new Callback<HolidayModelResponse>() {
            @Override
            public void onResponse(@NonNull Call<HolidayModelResponse> call, @NonNull Response<HolidayModelResponse> response) {
//                Log.d("HolidaysViewModel", "API call successful: " + response.isSuccessful());
                if (response.isSuccessful() && response.body() != null) {
                    List<HolidaysItem> holidays = response.body().getData().getHolidays();
//                    Log.d("HolidaysViewModel", "Fetched holidays: " + holidays);
                    new Handler(Looper.getMainLooper()).post(() -> holidaysList.setValue(holidays));
                } else {
                    new Handler(Looper.getMainLooper()).post(() -> holidaysList.setValue(null));
                }
            }

            @Override
            public void onFailure(@NonNull Call<HolidayModelResponse> call, @NonNull Throwable t) {
                // Handle failure
                new Handler(Looper.getMainLooper()).post(() -> holidaysList.setValue(null));
                Log.d("HolidaysViewModel", "API call failed: " + t.getMessage());
            }
        });
    }
}

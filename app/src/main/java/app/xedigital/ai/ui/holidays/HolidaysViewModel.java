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

        Call<HolidayModelResponse> call = service.getHolidays("jwt eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJmaXJzdG5hbWUiOiJEaGVlcmFqIiwibGFzdG5hbWUiOiJUcmlwYXRoaSIsImVtYWlsIjoiZGhlZXJhai50QGNvbnN1bHRlZGdlLmdsb2JhbCIsImFjdGl2ZSI6dHJ1ZSwicHJvdmlkZXIiOiJ3ZWIiLCJfaWQiOiI2NjdkMzcyMzMxMzhkMDVmMzRiMmMwYWIiLCJyb2xlIjp7Im5hbWUiOiJlbXBsb3llZSIsImRpc3BsYXlOYW1lIjoiRW1wbG95ZWUiLCJkZXNjcmlwdGlvbiI6IlRoaXMgaXMgZW1wbG95ZWUgcm9sZSIsImRlZmF1bHQiOnRydWUsImFjdGl2ZSI6dHJ1ZSwiX2lkIjoiNWY2OWRjODUxODUxMWUyYTk0NDUyYWM2IiwiY3JlYXRlZEF0IjoiMjAyMC0wMy0yOFQxNzo0NToyMS44MTFaIn0sImJyYW5jaCI6eyJuYW1lIjoiQ29uU3VsdEVkZ2UgR3VyZ2FvbiIsImVtYWlsIjoiaHJAY29uc3VsdGVkZ2UuZ2xvYmFsIiwibm90aWZpY2F0aW9uRW1haWwiOiJockBjb25zdWx0ZWRnZS5nbG9iYWwiLCJjb250YWN0IjoiOTI4OTExNTAxMCIsImxvZ28iOiIiLCJhZGRyZXNzIjoiTWFnbnVtIFRvd2VyIOKAkyAyIHwgVW5pdCBObyAtIDkwMSB8IDl0aCBGbG9vciB8LCBHb2xmIENvdXJzZSBFeHQgUmQsIHwsIFNlY3RvciA1OCwgR3VydWdyYW0sIEhhcnlhbmEiLCJjaXR5IjoiR3VydWdyYW0iLCJzdGF0ZSI6IkhhcnlhbmEiLCJ6aXAiOiIxMjIwMTEiLCJ3ZWJzaXRlIjoiIiwicHJlZml4IjoiY29uc3VsdGVkZ2VndXJnYW9uMGQwdGlxIiwiYWN0aXZlIjp0cnVlLCJhY2NvdW50RXhwaXJ5RGF0ZSI6IjIwMzAtMDEtMDFUMDA6MDA6MDAuMDAwWiIsImFjY291bnRQbGFuIjoidW5saW1pdGVkIiwiY3VzdG9tUGxhblZhbHVlIjoiIiwiaXNWaXNpdG9yQXBwcm92YWwiOnRydWUsImlzVG91Y2hsZXNzIjp0cnVlLCJpc0dvdmVybm1lbnRJZFVwbG9hZCI6ZmFsc2UsImlzSXRlbUltYWdlVXBsb2FkIjpmYWxzZSwiX2lkIjoiNjU3ODU0N2I1NmY4MmE3YjY4ZTNlZDgxIiwiY3JlYXRlZEF0IjoiMjAyMy0xMi0xMlQxMjozOToyMy42MzVaIn0sImNvbXBhbnkiOnsibmFtZSI6IkNvbnN1bHRFZGdlIEdsb2JhbCBQdnQgTHRkIiwiZW1haWwiOiJockBjb25zdWx0ZWRnZS5nbG9iYWwiLCJlbWFpbERvbWFpbiI6ImNvbnN1bHRlZGdlLmdsb2JhbCIsImxvZ28iOiJodHRwczovL2NvbXBhbmllcy1wcm9maWxlLWltYWdlcy5zMy5hcC1zb3V0aC0xLmFtYXpvbmF3cy5jb20vdXdxNHRncDhwNy5qcGciLCJsb2dvS2V5IjoidXdxNHRncDhwNy5qcGciLCJhZGRyZXNzIjoiTWFnbnVtIFRvd2VyIOKAkyAyIHwgVW5pdCBObyAtIDkwMSB8IDl0aCBGbG9vciB8LCBHb2xmIENvdXJzZSBFeHQgUmQsIHwsIFNlY3RvciA1OCwgR3VydWdyYW0sIEhhcnlhbmEiLCJjb250YWN0IjoiOTI4OTExNTAxMCIsImNpdHkiOiJHdXJnYW9uIiwic3RhdGUiOiJIYXJ5YW5hIiwiemlwIjoiMTIyMDExIiwid2Vic2l0ZSI6Imh0dHBzOi8vY29uc3VsdGVkZ2UuZ2xvYmFsLyIsImFjdGl2ZSI6dHJ1ZSwiZGJOYW1lIjoiY29uc3VsdGVkZ2VnbG9iYWxwdnRsdGRfNWU5NzBuIiwibGljZW5zZSI6IiIsIl9pZCI6IjY1Nzg1NDBmNTZmODJhN2I2OGUzZWQ4MCIsImNyZWF0ZWRBdCI6IjIwMjMtMTItMTJUMTI6Mzc6MzUuNDg2WiJ9LCJjcmVhdGVkQnkiOiI2NjdkNTZlNjIxMjA5ZjcxNjg5OTJkZjUiLCJjcmVhdGVkQXQiOiIyMDI0LTA2LTI4VDA5OjE3OjQ0Ljk3N1oiLCJ1cGRhdGVkQXQiOiIyMDI0LTA2LTI4VDA5OjE3OjQ0Ljk3N1oiLCJfX3YiOjAsImlhdCI6MTcyMzQ1NTQ0MH0.ZLeqdX1Wv7S1JT7BRdI0dK2tDAmV2tpjgb-qIdJu2ik");
        call.enqueue(new Callback<HolidayModelResponse>() {
            @Override
            public void onResponse(@NonNull Call<HolidayModelResponse> call, @NonNull Response<HolidayModelResponse> response) {
//                Log.d("HolidaysViewModel", "API call successful: " + response.isSuccessful());
                if (response.isSuccessful() && response.body() != null) {
                    List<HolidaysItem> holidays = response.body().getData().getHolidays();
                    Log.d("HolidaysViewModel", "Fetched holidays: " + holidays);
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

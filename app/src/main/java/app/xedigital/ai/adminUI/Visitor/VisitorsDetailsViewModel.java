package app.xedigital.ai.adminUI.Visitor;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import app.xedigital.ai.adminApi.AdminAPIClient;
import app.xedigital.ai.adminApi.AdminAPIInterface;
import app.xedigital.ai.model.Admin.VisitorsAdminDetails.VisitorsAdminDetailsResponse;
import app.xedigital.ai.model.Admin.VisitorsAdminDetails.VisitorsItem;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class VisitorsDetailsViewModel extends ViewModel {

    private final MutableLiveData<VisitorsAdminDetailsResponse> visitorsLiveData = new MutableLiveData<>();

    public LiveData<List<VisitorsItem>> getVisitors(String authToken) {
        MutableLiveData<List<VisitorsItem>> liveData = new MutableLiveData<>();

        AdminAPIInterface apiService = AdminAPIClient.getInstance().getBase2();
        apiService.getVisitors(authToken).enqueue(new Callback<VisitorsAdminDetailsResponse>() {
            @Override
            public void onResponse(@NonNull Call<VisitorsAdminDetailsResponse> call, @NonNull Response<VisitorsAdminDetailsResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    liveData.postValue(response.body().getData().getVisitors());
                } else {
                    liveData.postValue(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(@NonNull Call<VisitorsAdminDetailsResponse> call, @NonNull Throwable t) {
                liveData.postValue(new ArrayList<>());
            }
        });

        return liveData;
    }

    public LiveData<Boolean> checkInVisitor(String token, String visitorId, VisitorsItem visitorData) {
        MutableLiveData<Boolean> status = new MutableLiveData<>();

        // Quick safety check
        if (visitorId == null || visitorId.isEmpty()) {
            status.setValue(false);
            return status;
        }
        AdminAPIInterface apiService = AdminAPIClient.getInstance().getBase2();
        // Ensure this method name matches your AdminAPIInterface exactly
        apiService.checkInManual(token, visitorId, visitorData).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                status.postValue(response.isSuccessful());
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                status.postValue(false);
            }
        });

        return status;
    }

    public LiveData<Boolean> checkOutVisitor(String token, String visitorId, VisitorsItem visitorData) {
        MutableLiveData<Boolean> status = new MutableLiveData<>();
        // Quick safety check
        if (visitorId == null || visitorId.isEmpty()) {
            status.setValue(false);
            return status;
        }
        AdminAPIInterface apiService = AdminAPIClient.getInstance().getBase2();

        apiService.checkOut(token, visitorId, visitorData).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                status.postValue(response.isSuccessful());

            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                status.postValue(false);
            }
        });
        return status;
    }

}

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

}

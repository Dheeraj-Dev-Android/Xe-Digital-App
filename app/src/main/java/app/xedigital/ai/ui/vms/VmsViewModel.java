package app.xedigital.ai.ui.vms;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.api.APIInterface;
import app.xedigital.ai.model.vms.GetVisitorsResponse;
import app.xedigital.ai.model.vms.VisitorsItem;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VmsViewModel extends ViewModel {

    private MutableLiveData<List<VisitorsItem>> visitors;
    private MutableLiveData<Boolean> isLoading;
    private MutableLiveData<String> error;

    public VmsViewModel() {
        visitors = new MutableLiveData<>();
        isLoading = new MutableLiveData<>();
        error = new MutableLiveData<>();
    }

    public LiveData<List<VisitorsItem>> getVisitors() {
        return visitors;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public void fetchVisitors(String authToken) {
        isLoading.setValue(true);
        APIInterface visitorsApi = APIClient.getInstance().getVisitors();
        Call<GetVisitorsResponse> call = visitorsApi.getVisitors("jwt " + authToken);

        call.enqueue(new Callback<GetVisitorsResponse>() {
            @Override
            public void onResponse(Call<GetVisitorsResponse> call, Response<GetVisitorsResponse> response) {
                isLoading.setValue(false);
                if (response.isSuccessful()) {
                    GetVisitorsResponse visitorsResponse = response.body();
                    if (visitorsResponse != null) {
                        visitors.setValue(visitorsResponse.getData().getVisitors());
                    } else {
                        error.setValue("Empty response body");
                    }
                } else {
                    error.setValue("Response code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<GetVisitorsResponse> call, Throwable t) {
                isLoading.setValue(false);
                error.setValue("Network error: " + t.getMessage());
            }
        });
    }
}
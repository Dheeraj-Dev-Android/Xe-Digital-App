package app.xedigital.ai.ui.vms;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.Collections;
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
            public void onResponse(@NonNull Call<GetVisitorsResponse> call, @NonNull Response<GetVisitorsResponse> response) {
                isLoading.setValue(false);
                if (response.isSuccessful()) {
                    GetVisitorsResponse visitorsResponse = response.body();
                    if (visitorsResponse != null && visitorsResponse.getData() != null) {
                        List<VisitorsItem> visitorsList = visitorsResponse.getData().getVisitors();
                        // Check if the visitors list is empty and post an empty list if it is
                        if (visitorsList == null || visitorsList.isEmpty()) {
                            visitors.setValue(Collections.emptyList());
                        } else {
                            visitors.setValue(visitorsList);
                        }
                    } else {
                        // Handle case where visitorsResponse or visitorsResponse.getData() is null
                        error.setValue("Empty response body or data");
                        visitors.setValue(Collections.emptyList()); // Post an empty list for empty state
                    }
                } else {
                    error.setValue("Response code: " + response.code());
                    visitors.setValue(Collections.emptyList()); // Post an empty list for error state
                }
            }

            @Override
            public void onFailure(@NonNull Call<GetVisitorsResponse> call, @NonNull Throwable t) {
                isLoading.setValue(false);
                error.setValue("Network error: " + t.getMessage());
                visitors.setValue(Collections.emptyList()); // Post an empty list for network error state
            }
        });
    }
}
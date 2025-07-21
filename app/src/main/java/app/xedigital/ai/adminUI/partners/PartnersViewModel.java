package app.xedigital.ai.adminUI.partners;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import app.xedigital.ai.adminApi.AdminAPIClient;
import app.xedigital.ai.adminApi.AdminAPIInterface;
import app.xedigital.ai.model.Admin.partners.PartnersItem;
import app.xedigital.ai.model.Admin.partners.PartnersResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PartnersViewModel extends ViewModel {

    private final MutableLiveData<List<PartnersItem>> partnersList = new MutableLiveData<>();

    public LiveData<List<PartnersItem>> getPartnersList() {
        return partnersList;
    }

    public void fetchPartners(String authToken) {
        AdminAPIInterface apiService = AdminAPIClient.getInstance().getBase2();
        Call<PartnersResponse> call = apiService.getPartners(authToken);

        call.enqueue(new Callback<PartnersResponse>() {
            @Override
            public void onResponse(@NonNull Call<PartnersResponse> call, @NonNull Response<PartnersResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    partnersList.setValue(response.body().getData().getPartners());
                }
            }

            @Override
            public void onFailure(@NonNull Call<PartnersResponse> call, @NonNull Throwable t) {
                t.printStackTrace();
                partnersList.setValue(null);
            }
        });
    }
}

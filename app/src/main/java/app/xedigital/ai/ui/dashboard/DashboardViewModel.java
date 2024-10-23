package app.xedigital.ai.ui.dashboard;

import androidx.lifecycle.ViewModel;

import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.api.APIInterface;

public class DashboardViewModel extends ViewModel {


    public DashboardViewModel() {
        APIInterface apiInterface = APIClient.getInstance().getApi();

    }


}
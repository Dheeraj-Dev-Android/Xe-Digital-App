package app.xedigital.ai.ui.claim_management;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ClaimManagementViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public ClaimManagementViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is Claim Management fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }

}
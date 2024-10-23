package app.xedigital.ai.ui.payroll;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class PayrollViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public PayrollViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is Payroll");
    }

    public LiveData<String> getText() {
        return mText;
    }

}
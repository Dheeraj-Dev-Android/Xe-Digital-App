package app.xedigital.ai.ui.shifts;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ShiftsViewModel extends ViewModel {

    private MutableLiveData<String> someLiveData;
//
//    public ShiftsViewModel() {
//        mText = new MutableLiveData<>();
//        mText.setValue("This is Shift fragment");
//    }

    public LiveData<String> getSomeLiveData() {
        if (someLiveData == null) {
            someLiveData = new MutableLiveData<>();
        }
        return someLiveData;
    }


    public void processShiftData(String firstName, String lastName, String email, String contact, String shiftType, String shift) {
    }
}
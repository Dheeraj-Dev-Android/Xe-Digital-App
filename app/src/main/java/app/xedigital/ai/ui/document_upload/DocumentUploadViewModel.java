package app.xedigital.ai.ui.document_upload;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class DocumentUploadViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public DocumentUploadViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is Document Upload");
    }

    public MutableLiveData<String> getText() {
        return mText;
    }

}
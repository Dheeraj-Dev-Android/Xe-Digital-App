package app.xedigital.ai.ui.onboarding;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import app.xedigital.ai.ui.onboarding.model.OnboardingFormModel;

public class EmployeeOnboardingViewModel extends ViewModel {

    // Draft model – survives rotation
    private final MutableLiveData<OnboardingFormModel> draftLiveData =
            new MutableLiveData<>(new OnboardingFormModel());

    // Submit result
    private final MutableLiveData<Boolean> submitSuccess =
            new MutableLiveData<>();

    // Error message
    private final MutableLiveData<String> errorMessage =
            new MutableLiveData<>();

    // Loading state
    private final MutableLiveData<Boolean> isLoading =
            new MutableLiveData<>(false);

    public MutableLiveData<OnboardingFormModel> getDraftLiveData() {
        return draftLiveData;
    }

    public MutableLiveData<Boolean> getSubmitSuccess() {
        return submitSuccess;
    }

    public MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public MutableLiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void saveDraft(OnboardingFormModel model) {
        model.setDraft(true);
        draftLiveData.setValue(model);
        // TODO: call repository.saveOnboarding(model) when API ready
    }

    public void submitForm(OnboardingFormModel model) {
        model.setDraft(false);
        // TODO: call repository.submitOnboarding(model) when API ready
        submitSuccess.setValue(true);
    }
}
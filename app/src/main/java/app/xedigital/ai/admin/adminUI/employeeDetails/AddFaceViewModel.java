package app.xedigital.ai.admin.adminUI.employeeDetails;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import app.xedigital.ai.admin.adminModal.EmployeeDetails.EmployeesItem;

public class AddFaceViewModel extends ViewModel {

    private final MutableLiveData<AddFaceUiState> uiState = new MutableLiveData<>(AddFaceUiState.idle());
    private final AddFaceRepository repository = new AddFaceRepository();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public LiveData<AddFaceUiState> getUiState() {
        return uiState;
    }

    public void uploadFace(@NonNull File imageFile, String collectionName, String token, @NonNull EmployeesItem employee) {
        if (!validateCommon(imageFile, token, employee)) return;
        if (!validateCollectionName(collectionName)) return;
        uiState.postValue(AddFaceUiState.loading());
        executor.execute(() -> repository.addFaceAndLink(imageFile, collectionName, token, employee, buildCallback()));
    }

    public void uploadProfileImage(@NonNull File imageFile, String token, @NonNull EmployeesItem employee) {
        if (!validateCommon(imageFile, token, employee)) return;
        uiState.postValue(AddFaceUiState.loading());
        executor.execute(() -> repository.addProfileImage(imageFile, token, employee, buildCallback()));
    }

    public void uploadFaceAndProfile(@NonNull File imageFile, String collectionName, String token, @NonNull EmployeesItem employee) {
        if (!validateCommon(imageFile, token, employee)) return;
        if (!validateCollectionName(collectionName)) return;
        uiState.postValue(AddFaceUiState.loading());
        executor.execute(() -> repository.addFaceAndLinkAndProfile(imageFile, collectionName, token, employee, buildCallback()));
    }

    // ============================================================
    //  VALIDATION HELPERS
    // ============================================================

    private boolean validateCommon(@NonNull File imageFile, String token, @NonNull EmployeesItem employee) {
        if (!imageFile.exists()) {
            uiState.postValue(AddFaceUiState.error("No photo captured. Please retake.", true));
            return false;
        }
        if (token == null || token.trim().isEmpty()) {
            uiState.postValue(AddFaceUiState.error("Authentication token missing. Please login again.", false));
            return false;
        }
        if (employee.getId() == null || employee.getId().trim().isEmpty()) {
            uiState.postValue(AddFaceUiState.error("Employee ID is missing.", false));
            return false;
        }
        return true;
    }

    private boolean validateCollectionName(String collectionName) {
        if (collectionName == null || collectionName.trim().isEmpty()) {
            uiState.postValue(AddFaceUiState.error("Collection name is missing. Please contact administrator.", false));
            return false;
        }
        return true;
    }

    private AddFaceRepository.ResultCallback buildCallback() {
        return new AddFaceRepository.ResultCallback() {
            @Override
            public void onSuccess() {
                uiState.postValue(AddFaceUiState.success());
            }

            @Override
            public void onError(@NonNull String message, boolean isRetryable) {
                uiState.postValue(AddFaceUiState.error(message, isRetryable));
            }
        };
    }

    public void resetState() {
        uiState.setValue(AddFaceUiState.idle());
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (!executor.isShutdown()) {
            executor.shutdownNow();
        }
    }
}
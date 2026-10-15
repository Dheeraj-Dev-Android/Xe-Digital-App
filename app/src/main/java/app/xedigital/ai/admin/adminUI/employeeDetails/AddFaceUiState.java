package app.xedigital.ai.admin.adminUI.employeeDetails;

public class AddFaceUiState {

    public final Status status;
    public final String errorMessage;
    public final boolean isRetryable;

    private AddFaceUiState(Status status, String errorMessage, boolean isRetryable) {
        this.status = status;
        this.errorMessage = errorMessage;
        this.isRetryable = isRetryable;
    }

    public static AddFaceUiState idle() {
        return new AddFaceUiState(Status.IDLE, null, false);
    }

    public static AddFaceUiState loading() {
        return new AddFaceUiState(Status.LOADING, null, false);
    }

    public static AddFaceUiState success() {
        return new AddFaceUiState(Status.SUCCESS, null, false);
    }

    public static AddFaceUiState error(String message, boolean isRetryable) {
        return new AddFaceUiState(Status.ERROR, message, isRetryable);
    }

    public enum Status {
        IDLE,
        LOADING,
        SUCCESS,
        ERROR
    }
}
package app.xedigital.ai.model.employeeClaim;

public class EmployeeClaimResponse {
    private Data data;
    private boolean success;
    private String message;
    private int statusCode;

    public Data getData() {
        return data;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public int getStatusCode() {
        return statusCode;
    }
}

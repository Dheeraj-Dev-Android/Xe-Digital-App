package app.xedigital.ai.admin.adminModal.empTimesheet;

import com.google.gson.annotations.SerializedName;

public class EmployeeTimesheetResponse {

    @SerializedName("data")
    private Data data;

    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("statusCode")
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
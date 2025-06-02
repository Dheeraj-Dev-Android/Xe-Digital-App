package app.xedigital.ai.model.Admin.addFace;

import com.google.gson.annotations.SerializedName;

public class AddFaceResponse {

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
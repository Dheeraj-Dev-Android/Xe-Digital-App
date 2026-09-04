package app.xedigital.ai.model.deviceRegistration;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DeviceDetailsResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("statusCode")
    private int statusCode;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private List<DeviceDetailItem> data;

    public boolean isSuccess() {
        return success;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getMessage() {
        return message;
    }

    public List<DeviceDetailItem> getData() {
        return data;
    }

    public static class DeviceDetailItem {
        @SerializedName("userId")
        private String userId;

        @SerializedName("installationId")
        private String installationId;

        @SerializedName("deviceName")
        private String deviceName;

        @SerializedName("isActive")
        private boolean isActive;

        public String getUserId() {
            return userId;
        }

        public String getInstallationId() {
            return installationId;
        }

        public String getDeviceName() {
            return deviceName;
        }

        public boolean isActive() {
            return isActive;
        }
    }
}
package app.xedigital.ai.model.deviceRegistration;

import com.google.gson.annotations.SerializedName;

public class DeviceRegistrationRequest {

    @SerializedName("userId")
    private final String userId;
    @SerializedName("installationId")
    private final String installationId;
    @SerializedName("platform")
    private final String platform;
    @SerializedName("deviceName")
    private final String deviceName;
    @SerializedName("manufacturer")
    private final String manufacturer;
    @SerializedName("model")
    private final String model;
    @SerializedName("osVersion")
    private final String osVersion;
    @SerializedName("appVersion")
    private final String appVersion;
    @SerializedName("pushToken")
    private final String pushToken;
    @SerializedName("isActive")
    private final boolean isActive;

    public DeviceRegistrationRequest(String userId, String installationId, String platform, String deviceName, String manufacturer, String model, String osVersion, String appVersion, String pushToken, boolean isActive) {
        this.userId = userId;
        this.installationId = installationId;
        this.platform = platform;
        this.deviceName = deviceName;
        this.manufacturer = manufacturer;
        this.model = model;
        this.osVersion = osVersion;
        this.appVersion = appVersion;
        this.pushToken = pushToken;
        this.isActive = isActive;
    }

    public String getUserId() {
        return userId;
    }

    public String getInstallationId() {
        return installationId;
    }

    public String getPlatform() {
        return platform;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public String getModel() {
        return model;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public String getPushToken() {
        return pushToken;
    }

    public boolean isActive() {
        return isActive;
    }
}
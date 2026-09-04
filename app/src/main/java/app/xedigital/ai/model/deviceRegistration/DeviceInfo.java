package app.xedigital.ai.model.deviceRegistration;

public class DeviceInfo {
    private final String userId;
    private final String installationId;
    private final String platform;
    private final String deviceName;
    private final String manufacturer;
    private final String model;
    private final String osVersion;
    private final String appVersion;
    private final String pushToken;
    private final boolean isActive;

    public DeviceInfo(String userId, String installationId, String platform, String deviceName,
                      String manufacturer, String model, String osVersion, String appVersion,
                      String pushToken, boolean isActive) {
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
package app.xedigital.ai.utills;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import java.util.UUID;

import app.xedigital.ai.model.deviceRegistration.DeviceInfo;

public class DeviceInfoHelper {

    private static final String TAG = "DeviceInfoHelper";
    private static final String KEY_INSTALLATION_ID = "installation_id";

    public static DeviceInfo getDeviceInfo(Context context, String userId) {
        Log.d(TAG, "┌─────────────────────────────────────────────────────────");
        Log.d(TAG, "│ 📱 GATHERING UNBREAKABLE DEVICE HARDWARE INFORMATION");
        Log.d(TAG, "├─────────────────────────────────────────────────────────");

        // 🛡️ Fetch reinstall-proof Android Hardware ID
        String hardwareId = Settings.Secure.getString(
                context.getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        String installationId;
        SecurePrefManager prefManager = SecurePrefManager.getInstance(context);

        // Safety Fallback: If ANDROID_ID missing or is known emulator bug ID
        if (hardwareId == null || hardwareId.isEmpty() || "9774d56d682e549c".equalsIgnoreCase(hardwareId)) {
            installationId = prefManager.getString(KEY_INSTALLATION_ID, null);
            if (installationId == null) {
                installationId = "FALLBACK-" + UUID.randomUUID().toString().replaceAll("-", "").substring(0, 12).toUpperCase();
                prefManager.putString(KEY_INSTALLATION_ID, installationId);
            }
            Log.w(TAG, "│ [Warning] ANDROID_ID invalid. Using UUID fallback: " + installationId);
        } else {
            installationId = "HW-" + hardwareId.toUpperCase();
            // Persist locally for consistency across sessions
            prefManager.putString(KEY_INSTALLATION_ID, installationId);
            Log.i(TAG, "│ [Hardware Lock] Physical Device ID: " + installationId);
        }

        String platform = "android";
        String deviceName = getDeviceName();
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        String osVersion = Build.VERSION.RELEASE;
        String appVersion = getAppVersion(context);
        String pushToken = "FCM_TOKEN_PLACEHOLDER";
        boolean isActive = true;

        Log.d(TAG, "│ User ID      : " + userId);
        Log.d(TAG, "│ Device Name  : " + deviceName);
        Log.d(TAG, "│ Manufacturer : " + manufacturer);
        Log.d(TAG, "│ Model        : " + model);
        Log.d(TAG, "│ OS Version   : Android " + osVersion + " (API " + Build.VERSION.SDK_INT + ")");
        Log.d(TAG, "│ App Version  : " + appVersion);
        Log.d(TAG, "└─────────────────────────────────────────────────────────");

        return new DeviceInfo(userId, installationId, platform, deviceName, manufacturer, model, osVersion, appVersion, pushToken, isActive);
    }

    private static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.toLowerCase().startsWith(manufacturer.toLowerCase())) {
            return capitalize(model);
        }
        return capitalize(manufacturer) + " " + model;
    }

    private static String getAppVersion(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Error fetching app package version name", e);
            return "1.0.0";
        }
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return "";
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
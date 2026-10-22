package app.xedigital.ai.utills;

import android.app.AppOpsManager;
import android.content.Context;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Centralized security check manager.
 * Detects device-level tampering/fraud risks before allowing app usage.
 */
public class SecurityCheckManager {

    private static final String TAG = "SecurityCheckManager";

    private SecurityCheckManager() {
    }

    /**
     * Runs all security checks.
     *
     * @param context      Application or Activity context
     * @param isDebugBuild true = skip Developer Options check only;
     *                     false = run all three checks (release build)
     * @return List of active violations. Empty = device is clean.
     */
    public static List<Violation> runAllChecks(Context context, boolean isDebugBuild) {
        List<Violation> violations = new ArrayList<>();

        // ✅ Always checked — even in debug builds
        if (isMockLocationEnabled(context)) {
            violations.add(Violation.MOCK_LOCATION);
        }

        // ✅ Always checked — even in debug builds
        if (isAutoTimeDisabled(context)) {
            violations.add(Violation.AUTO_TIME_DISABLED);
        }

        // 🔒 Only checked in RELEASE builds
        if (!isDebugBuild && isDeveloperOptionsEnabled(context)) {
            violations.add(Violation.DEVELOPER_OPTIONS);
        }

        return violations;
    }

    public static boolean isMockLocationEnabled(Context context) {
        try {
            // Primary check — AppOpsManager (API 19+)
            AppOpsManager opsManager =
                    (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            if (opsManager != null) {
                int mode = opsManager.checkOp(
                        AppOpsManager.OPSTR_MOCK_LOCATION,
                        android.os.Process.myUid(),
                        context.getPackageName()
                );
                if (mode == AppOpsManager.MODE_ALLOWED) {
                    Log.w(TAG, "⚠️ Mock location allowed via AppOpsManager");
                    return true;
                }
            }

            // Secondary check — test provider / isMock() on last known location
            LocationManager lm =
                    (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            if (lm != null) {
                for (String provider : lm.getAllProviders()) {
                    try {
                        if (lm.isProviderEnabled(provider)) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                android.location.Location loc =
                                        lm.getLastKnownLocation(provider);
                                if (loc != null && loc.isMock()) {
                                    Log.w(TAG, "⚠️ Mock location on provider: " + provider);
                                    return true;
                                }
                            }
                        }
                    } catch (SecurityException ignored) {
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking mock location: " + e.getMessage());
        }
        return false;
    }

    // ─────────────────────────────────────────────────────────────
    // 1️⃣  MOCK LOCATION  (checked in ALL builds)
    // ─────────────────────────────────────────────────────────────

    public static boolean isAutoTimeDisabled(Context context) {
        try {
            int autoTime = Settings.Global.getInt(
                    context.getContentResolver(),
                    Settings.Global.AUTO_TIME,
                    0
            );
            int autoTimeZone = Settings.Global.getInt(
                    context.getContentResolver(),
                    Settings.Global.AUTO_TIME_ZONE,
                    0
            );
            return autoTime == 0 || autoTimeZone == 0;
        } catch (Exception e) {
            Log.e(TAG, "Error checking auto time: " + e.getMessage());
            return false;
        }
    }

    // ─────────────────────────────────────────────────────────────
    // 2️⃣  AUTO DATE & TIME  (checked in ALL builds)
    // ─────────────────────────────────────────────────────────────

    public static boolean isDeveloperOptionsEnabled(Context context) {
        try {
            return Settings.Global.getInt(
                    context.getContentResolver(),
                    Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
                    0
            ) == 1;
        } catch (Exception e) {
            Log.e(TAG, "Error checking developer options: " + e.getMessage());
            return false;
        }
    }

    // ─────────────────────────────────────────────────────────────
    // 3️⃣  DEVELOPER OPTIONS  (checked ONLY in release builds)
    // ─────────────────────────────────────────────────────────────

    public enum Violation {
        MOCK_LOCATION(
                "Mock Location is enabled",
                "Please disable 'Mock Location App' in Developer Options."
        ),
        AUTO_TIME_DISABLED(
                "Automatic Date & Time is OFF",
                "Please enable 'Set time automatically' in Date & Time settings."
        ),
        DEVELOPER_OPTIONS(
                "Developer Options is enabled",
                "Please disable 'Developer Options' from your device settings."
        );

        public final String title;
        public final String message;

        Violation(String title, String message) {
            this.title = title;
            this.message = message;
        }
    }
}
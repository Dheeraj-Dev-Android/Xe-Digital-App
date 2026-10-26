package app.xedigital.ai.utills;

import android.app.AppOpsManager;
import android.content.Context;
import android.location.LocationManager;
import android.os.Build;
import android.os.Process;
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
     * @param isDebugBuild true = skip Developer Options check only
     * @return List of active violations. Empty = device is clean.
     */
    public static List<Violation> runAllChecks(Context context, boolean isDebugBuild) {
        List<Violation> violations = new ArrayList<>();

        if (isMockLocationEnabled(context)) {
            violations.add(Violation.MOCK_LOCATION);
        }

        if (isAutoTimeDisabled(context)) {
            violations.add(Violation.AUTO_TIME_DISABLED);
        }

        if (!isDebugBuild && isDeveloperOptionsEnabled(context)) {
            violations.add(Violation.DEVELOPER_OPTIONS);
        }

        return violations;
    }

    /**
     * Multi-layered mock location detection:
     * <p>
     * Layer 1: AppOpsManager — checks if THIS app is the mock provider
     * Layer 2: Settings.Secure — legacy mock location toggle (pre-API 23)
     * Layer 3: LocationManager — checks for "test" provider registration
     * <p>
     * NOTE: The most reliable check (Location.isMock()) happens at the
     * point of actual location retrieval — see PunchActivity.isMockLocation().
     * This pre-check is a best-effort early gate.
     */
    public static boolean isMockLocationEnabled(Context context) {
        // ── Layer 1: AppOpsManager ──────────────────────────────
        try {
            AppOpsManager opsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            if (opsManager != null) {
                int mode = opsManager.checkOpNoThrow(AppOpsManager.OPSTR_MOCK_LOCATION, Process.myUid(), context.getPackageName());
                if (mode == AppOpsManager.MODE_ALLOWED) {
                    Log.w(TAG, "⚠️ Mock location allowed via AppOpsManager");
                    return true;
                }
                // MODE_ERRORED or MODE_IGNORED or SecurityException
                // → this app is NOT the mock provider, continue checking
            }
        } catch (SecurityException e) {
            // Expected on Android 12+ when app isn't the mock provider.
            // This means the system blocked the check — NOT that mock is active.
            Log.d(TAG, "AppOps check blocked by system (normal): " + e.getMessage());
        } catch (Exception e) {
            Log.d(TAG, "AppOps check unavailable: " + e.getMessage());
        }

        // ── Layer 2: Legacy Settings (pre-API 23 fallback) ─────
        try {
            String mockLocation = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ALLOW_MOCK_LOCATION);
            if ("1".equals(mockLocation)) {
                Log.w(TAG, "⚠️ Mock location enabled via Settings.Secure");
                return true;
            }
        } catch (Exception e) {
            Log.d(TAG, "Settings.Secure check unavailable: " + e.getMessage());
        }

        // ── Layer 3: Test provider in LocationManager ──────────
        try {
            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            if (lm != null) {
                // Check if "test" provider exists (used by mock location apps)
                if (lm.getProvider("test") != null) {
                    Log.w(TAG, "⚠️ 'test' location provider detected");
                    return true;
                }

                // On API 31+, check last known locations for isMock() flag
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    for (String provider : lm.getAllProviders()) {
                        try {
                            if (lm.isProviderEnabled(provider)) {
                                android.location.Location loc = lm.getLastKnownLocation(provider);
                                if (loc != null && loc.isMock()) {
                                    Log.w(TAG, "⚠️ Mock location on provider: " + provider);
                                    return true;
                                }
                            }
                        } catch (SecurityException ignored) {
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "LocationManager check unavailable: " + e.getMessage());
        }

        return false;
    }

    // ─────────────────────────────────────────────────────────────
    // 1️⃣  MOCK LOCATION  (checked in ALL builds)
    // ─────────────────────────────────────────────────────────────

    public static boolean isAutoTimeDisabled(Context context) {
        try {
            int autoTime = Settings.Global.getInt(context.getContentResolver(), Settings.Global.AUTO_TIME, 0);
            int autoTimeZone = Settings.Global.getInt(context.getContentResolver(), Settings.Global.AUTO_TIME_ZONE, 0);
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
            return Settings.Global.getInt(context.getContentResolver(), Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) == 1;
        } catch (Exception e) {
            Log.e(TAG, "Error checking developer options: " + e.getMessage());
            return false;
        }
    }

    // ─────────────────────────────────────────────────────────────
    // 3️⃣  DEVELOPER OPTIONS  (checked ONLY in release builds)
    // ─────────────────────────────────────────────────────────────

    public enum Violation {
        MOCK_LOCATION("Mock Location is enabled", "Please disable 'Mock Location App' in Developer Options."), AUTO_TIME_DISABLED("Automatic Date & Time is OFF", "Please enable 'Set time automatically' in Date & Time settings."), DEVELOPER_OPTIONS("Developer Options is enabled", "Please disable 'Developer Options' from your device settings.");

        public final String title;
        public final String message;

        Violation(String title, String message) {
            this.title = title;
            this.message = message;
        }
    }
}
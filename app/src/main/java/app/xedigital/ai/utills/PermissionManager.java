package app.xedigital.ai.utills;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.HashMap;
import java.util.Map;

/**
 * Centralized Permission Authority for the entire application.
 * <p>
 * USAGE FROM ANYWHERE:
 * boolean canUse = PermissionManager.getInstance(context).isGranted("CAMERA");
 * <p>
 * This class is the SINGLE SOURCE OF TRUTH for all permission states.
 */
public class PermissionManager {

    // ── Permission Tag Constants (use these everywhere) ──
    public static final String TAG_CAMERA = "CAMERA";
    public static final String TAG_LOCATION = "LOCATION";
    public static final String TAG_BACKGROUND_LOCATION = "BACKGROUND_LOCATION";
    public static final String TAG_NOTIFICATION = "NOTIFICATION";
    public static final String TAG_BIOMETRIC = "BIOMETRIC";
    public static final String TAG_INTERNET = "INTERNET";
    private static volatile PermissionManager instance;
    private final Context appContext;
    private final SecurePrefManager prefManager;
    // LiveData so any observer (Fragment/Activity) can react to changes
    private final MutableLiveData<Map<String, Boolean>> permissionStates =
            new MutableLiveData<>(new HashMap<>());

    private PermissionManager(Context context) {
        this.appContext = context.getApplicationContext();
        this.prefManager = SecurePrefManager.getInstance(appContext);
        syncAllPermissions();
    }

    public static PermissionManager getInstance(Context context) {
        if (instance == null) {
            synchronized (PermissionManager.class) {
                if (instance == null) {
                    instance = new PermissionManager(context);
                }
            }
        }
        return instance;
    }

    // ─────────────────────────────────────────────
    //  CORE API — Call from ANY Activity/Fragment
    // ─────────────────────────────────────────────

    /**
     * Check if a permission is currently granted.
     * This is the PRIMARY method your app should use everywhere.
     * <p>
     * Example:
     * if (PermissionManager.getInstance(ctx).isGranted(PermissionManager.TAG_CAMERA)) {
     * openCamera();
     * }
     */
    public boolean isGranted(String tag) {
        syncAllPermissions(); // Always fresh
        Map<String, Boolean> states = permissionStates.getValue();
        if (states != null && states.containsKey(tag)) {
            return Boolean.TRUE.equals(states.get(tag));
        }
        return false;
    }

    /**
     * Check MULTIPLE permissions at once.
     * Returns true only if ALL are granted.
     * <p>
     * Example:
     * if (pm.areAllGranted(TAG_CAMERA, TAG_LOCATION)) { ... }
     */
    public boolean areAllGranted(String... tags) {
        for (String tag : tags) {
            if (!isGranted(tag)) return false;
        }
        return true;
    }

    /**
     * Returns the list of tags that are NOT granted from the given set.
     * Useful for showing the user what's missing.
     */
    public String[] getMissingPermissions(String... requiredTags) {
        java.util.List<String> missing = new java.util.ArrayList<>();
        for (String tag : requiredTags) {
            if (!isGranted(tag)) missing.add(tag);
        }
        return missing.toArray(new String[0]);
    }

    // ─────────────────────────────────────────────
    //  APP-LEVEL PERMISSIONS (Biometric, etc.)
    // ─────────────────────────────────────────────

    /**
     * Toggle an app-level permission (like Biometric).
     * System permissions CANNOT be toggled this way —
     * they must go through Android's permission dialog or Settings.
     */
    public void setAppLevelPermission(String tag, boolean enabled) {
        switch (tag) {
            case TAG_BIOMETRIC:
                prefManager.putBoolean("isBioEnabled", enabled);
                break;
            // Add more app-level toggles here as needed
        }
        syncAllPermissions();
    }

    // ─────────────────────────────────────────────
    //  SYNC ENGINE
    // ─────────────────────────────────────────────

    /**
     * Re-reads ALL permission states from the system and SecurePrefs.
     * Called automatically, but you can call it manually after
     * returning from Settings or permission dialogs.
     */
    public void syncAllPermissions() {
        Map<String, Boolean> states = new HashMap<>();

        // 1. Camera
        states.put(TAG_CAMERA,
                isSystemPermissionGranted(android.Manifest.permission.CAMERA));

        // 2. Precise Location
        states.put(TAG_LOCATION,
                isSystemPermissionGranted(android.Manifest.permission.ACCESS_FINE_LOCATION));

        // 3. Background Location (Android 10+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            states.put(TAG_BACKGROUND_LOCATION,
                    isSystemPermissionGranted(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION));
        }

        // 4. Notifications (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            states.put(TAG_NOTIFICATION,
                    isSystemPermissionGranted(android.Manifest.permission.POST_NOTIFICATIONS));
        }

        // 5. Biometric (App-level)
        states.put(TAG_BIOMETRIC,
                prefManager.getBoolean("isBioEnabled", false));

        // 6. Internet (Always true — manifest-level)
        states.put(TAG_INTERNET, true);

        permissionStates.setValue(states);
    }

    /**
     * Observe all permission states as LiveData.
     * Useful for the PermissionFragment dashboard.
     */
    public LiveData<Map<String, Boolean>> observePermissionStates() {
        return permissionStates;
    }

    // ─────────────────────────────────────────────
    //  HELPERS
    // ─────────────────────────────────────────────

    private boolean isSystemPermissionGranted(String manifestPermission) {
        return ContextCompat.checkSelfPermission(appContext, manifestPermission)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Get the Android Manifest permission string for a tag.
     * Returns null for app-level permissions.
     */
    public String getManifestPermission(String tag) {
        switch (tag) {
            case TAG_CAMERA:
                return android.Manifest.permission.CAMERA;
            case TAG_LOCATION:
                return android.Manifest.permission.ACCESS_FINE_LOCATION;
            case TAG_BACKGROUND_LOCATION:
                return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                        ? android.Manifest.permission.ACCESS_BACKGROUND_LOCATION : null;
            case TAG_NOTIFICATION:
                return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                        ? android.Manifest.permission.POST_NOTIFICATIONS : null;
            default:
                return null;
        }
    }

    /**
     * Human-readable name for a tag (for dialogs/toasts).
     */
    public String getReadableName(String tag) {
        switch (tag) {
            case TAG_CAMERA:
                return "Camera";
            case TAG_LOCATION:
                return "Location";
            case TAG_BACKGROUND_LOCATION:
                return "Background Location";
            case TAG_NOTIFICATION:
                return "Notifications";
            case TAG_BIOMETRIC:
                return "Biometric Login";
            case TAG_INTERNET:
                return "Internet";
            default:
                return tag;
        }
    }
}
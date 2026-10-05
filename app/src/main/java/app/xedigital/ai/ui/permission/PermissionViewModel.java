package app.xedigital.ai.ui.permission;

import android.app.Application;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

import app.xedigital.ai.utills.PermissionManager;

public class PermissionViewModel extends AndroidViewModel {

    private final PermissionManager permissionManager;
    private final MutableLiveData<List<PermissionItem>> permissions = new MutableLiveData<>();

    public PermissionViewModel(@NonNull Application application) {
        super(application);
        permissionManager = PermissionManager.getInstance(application);
        loadPermissions();
    }

    public LiveData<List<PermissionItem>> getPermissions() {
        return permissions;
    }

    /**
     * Re-syncs all permission states from the central manager.
     * Call this onResume, after returning from Settings, etc.
     */
    public void refreshAll() {
        permissionManager.syncAllPermissions();
        updateItemsFromManager();
    }

    private void loadPermissions() {
        List<PermissionItem> list = new ArrayList<>();

        // ── CORE PERMISSIONS ──
        list.add(new PermissionItem(
                "Camera Access",
                "Required for face verification and photo capture.",
                permissionManager.getManifestPermission(PermissionManager.TAG_CAMERA),
                true, PermissionManager.TAG_CAMERA, "CORE"));

        list.add(new PermissionItem(
                "Precise Location",
                "Verifies your work location during attendance punch.",
                permissionManager.getManifestPermission(PermissionManager.TAG_LOCATION),
                true, PermissionManager.TAG_LOCATION, "CORE"));

        // ── BACKGROUND SERVICES ──
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            list.add(new PermissionItem(
                    "Always-on Location",
                    "Tracks attendance even when the app is closed.",
                    permissionManager.getManifestPermission(PermissionManager.TAG_BACKGROUND_LOCATION),
                    true, PermissionManager.TAG_BACKGROUND_LOCATION, "BACKGROUND"));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            list.add(new PermissionItem(
                    "Notifications",
                    "Keeps shift tracking service active in background.",
                    permissionManager.getManifestPermission(PermissionManager.TAG_NOTIFICATION),
                    true, PermissionManager.TAG_NOTIFICATION, "BACKGROUND"));
        }

        // ── SECURITY ──
        list.add(new PermissionItem(
                "Biometric Login",
                "Secure login using fingerprint or face.",
                null,
                false, PermissionManager.TAG_BIOMETRIC, "SECURITY"));

        // ── SYSTEM ──
        list.add(new PermissionItem(
                "Internet Access",
                "Syncs your data with company servers.",
                null,
                true, PermissionManager.TAG_INTERNET, "SYSTEM"));

        permissions.setValue(list);
        updateItemsFromManager();
    }

    /**
     * Reads the TRUE state from PermissionManager and updates the UI list.
     */
    private void updateItemsFromManager() {
        List<PermissionItem> items = permissions.getValue();
        if (items == null) return;

        for (PermissionItem item : items) {
            item.setGranted(permissionManager.isGranted(item.getTag()));
        }

        // Trigger LiveData update
        permissions.setValue(items);
    }

    /**
     * Toggle an app-level permission (Biometric, etc.)
     */
    public void toggleAppPermission(String tag, boolean enabled) {
        permissionManager.setAppLevelPermission(tag, enabled);
        updateItemsFromManager();
    }
}
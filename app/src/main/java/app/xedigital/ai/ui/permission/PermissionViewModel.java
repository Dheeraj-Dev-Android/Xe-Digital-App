package app.xedigital.ai.ui.permission;

import android.Manifest;
import android.os.Build;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class PermissionViewModel extends ViewModel {
    private final MutableLiveData<List<PermissionItem>> permissions = new MutableLiveData<>();

    public LiveData<List<PermissionItem>> getPermissions() {
        if (permissions.getValue() == null) {
            loadPermissions();
        }
        return permissions;
    }

    private void loadPermissions() {
        List<PermissionItem> list = new ArrayList<>();

        // 1. Camera (Mandatory for Face Login/Punch)
        list.add(new PermissionItem("Camera Access", "Required for capturing images to verify identity.", Manifest.permission.CAMERA, true, "CAMERA"));

        // 2. Precise Location (Mandatory for Punch Activity)
        list.add(new PermissionItem("Precise Location", "Used to verify your work location during attendance.", Manifest.permission.ACCESS_FINE_LOCATION, true, "LOCATION"));

        // 3. Background Location (Mandatory for Shift Tracking)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            list.add(new PermissionItem("Always-on Location", "Allows tracking even when the app is closed. Select 'Allow all the time'.", Manifest.permission.ACCESS_BACKGROUND_LOCATION, true, "BACKGROUND_LOCATION"));
        }

        // 4. Notifications (Mandatory for Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            list.add(new PermissionItem("Notifications", "Required to keep the shift tracking service active in the background.", Manifest.permission.POST_NOTIFICATIONS, true, "NOTIFICATION"));
        }

        // 5. Biometric (Optional)
        list.add(new PermissionItem("Biometric Login", "Secure login using fingerprint or PIN.", Manifest.permission.USE_BIOMETRIC, false, "BIOMETRIC"));

        // 6. System Status (Informational)
        list.add(new PermissionItem("Internet Access", "Required to sync data with servers.", null, true, "INTERNET"));

        permissions.setValue(list);
    }
}
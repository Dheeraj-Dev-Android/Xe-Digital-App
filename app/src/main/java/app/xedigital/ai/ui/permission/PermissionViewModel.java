package app.xedigital.ai.ui.permission;

import android.Manifest;

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
        // 1. Camera
        list.add(new PermissionItem(
                "Camera Access",
                "Required for capturing images within the app to Mark Your Attendance Inside the Punch Activity.",
                Manifest.permission.CAMERA,
                true,
                "CAMERA"));
        // 2. Location
        list.add(new PermissionItem(
                "Precise Location",
                "Used to verify your work location during the Punch Activity.",
                Manifest.permission.ACCESS_FINE_LOCATION,
                true,
                "LOCATION"));

        // 3. Biometric
        list.add(new PermissionItem(
                "Biometric Authentication",
                "Allows you to log in securely using your fingerprint or your Pin.",
                Manifest.permission.USE_BIOMETRIC,
                false,
                "BIOMETRIC"));

        // 4. Internet
        list.add(new PermissionItem(
                "Internet Access",
                "Allows the app to sync your data with our secure servers.",
                null,
                true,
                "INTERNET"));

        // 5. Network Status
        list.add(new PermissionItem(
                "Network Status",
                "Helps the app detect when you are offline to prevent data loss.",
                null,
                true,
                "NETWORK"));

        permissions.setValue(list);
    }
}
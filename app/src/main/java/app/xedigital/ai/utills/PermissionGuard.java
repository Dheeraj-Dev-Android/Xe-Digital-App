package app.xedigital.ai.utills;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;

import androidx.activity.result.ActivityResultLauncher;
import androidx.fragment.app.Fragment;

/**
 * GATEKEEPER for the entire app.
 * <p>
 * Every Activity/Fragment MUST use this before accessing
 * any feature that requires a permission.
 * <p>
 * USAGE EXAMPLES:
 * <p>
 * 1. Simple check (no request):
 * if (PermissionGuard.isGranted(context, TAG_CAMERA)) { openCamera(); }
 * <p>
 * 2. Check + Auto-request:
 * PermissionGuard.require(fragment, TAG_CAMERA, launcher, () -> openCamera());
 * <p>
 * 3. Full guard with redirect to Permission Screen:
 * PermissionGuard.guard(activity, TAG_CAMERA, launcher,
 * () -> openCamera(),
 * () -> showToast("Permission needed!")
 * );
 */
public class PermissionGuard {

    // ─────────────────────────────────────────────
    //  1. SIMPLE SYNCHRONOUS CHECK
    // ─────────────────────────────────────────────

    /**
     * Quick check — does the app currently have this permission?
     * Use this for conditional UI (show/hide buttons, etc.)
     */
    public static boolean isGranted(Context context, String tag) {
        return PermissionManager.getInstance(context).isGranted(tag);
    }

    /**
     * Check multiple permissions at once.
     */
    public static boolean areAllGranted(Context context, String... tags) {
        return PermissionManager.getInstance(context).areAllGranted(tags);
    }

    // ─────────────────────────────────────────────
    //  2. CHECK + REQUEST (From Fragment)
    // ─────────────────────────────────────────────

    /**
     * The main guard method for Fragments.
     * <p>
     * Flow:
     * ✅ Granted  → onGranted runs immediately
     * ❌ Not granted → Requests permission via launcher
     * After result, call this method AGAIN to re-check
     *
     * @param fragment  The calling fragment
     * @param tag       Permission tag (e.g., PermissionManager.TAG_CAMERA)
     * @param launcher  The fragment's ActivityResultLauncher<String>
     * @param onGranted What to do when permission is available
     */
    public static void require(Fragment fragment, String tag,
                               ActivityResultLauncher<String> launcher,
                               Runnable onGranted) {
        Context ctx = fragment.requireContext();
        PermissionManager pm = PermissionManager.getInstance(ctx);

        if (pm.isGranted(tag)) {
            onGranted.run();
            return;
        }

        // App-level permissions can't be "requested" via system dialog
        if (pm.getManifestPermission(tag) == null) {
            showRedirectDialog(fragment.requireActivity(), tag);
            return;
        }

        // Request the system permission
        launcher.launch(pm.getManifestPermission(tag));
    }

    // ─────────────────────────────────────────────
    //  3. CHECK + REQUEST (From Activity)
    // ─────────────────────────────────────────────

    /**
     * Same as above but for Activities.
     */
    public static void require(Activity activity, String tag,
                               ActivityResultLauncher<String> launcher,
                               Runnable onGranted) {
        PermissionManager pm = PermissionManager.getInstance(activity);

        if (pm.isGranted(tag)) {
            onGranted.run();
            return;
        }

        if (pm.getManifestPermission(tag) == null) {
            showRedirectDialog(activity, tag);
            return;
        }

        launcher.launch(pm.getManifestPermission(tag));
    }

    // ─────────────────────────────────────────────
    //  4. FULL GUARD (Check → Request → Fallback)
    // ─────────────────────────────────────────────

    /**
     * Complete guard with success AND failure callbacks.
     * Use this when you need to handle denial gracefully.
     * <p>
     * NOTE: Call this AFTER your launcher receives the result
     * to verify if the user actually granted it.
     */
    public static void guard(Fragment fragment, String tag,
                             ActivityResultLauncher<String> launcher,
                             Runnable onGranted,
                             Runnable onDenied) {
        Context ctx = fragment.requireContext();
        PermissionManager pm = PermissionManager.getInstance(ctx);

        if (pm.isGranted(tag)) {
            onGranted.run();
        } else {
            // First time or can still ask
            if (pm.getManifestPermission(tag) != null) {
                launcher.launch(pm.getManifestPermission(tag));
            } else {
                onDenied.run();
            }
        }
    }

    // ─────────────────────────────────────────────
    //  5. POST-REQUEST VERIFICATION
    // ─────────────────────────────────────────────

    /**
     * Call this INSIDE your ActivityResultLauncher callback
     * to verify and react to the result.
     * <p>
     * Example:
     * launcher = registerForActivityResult(..., isGranted -> {
     * PermissionGuard.verify(context, TAG_CAMERA, isGranted,
     * () -> openCamera(),
     * () -> showSettingsPrompt()
     * );
     * });
     */
    public static void verify(Context context, String tag, boolean systemResult,
                              Runnable onGranted, Runnable onDenied) {
        PermissionManager.getInstance(context).syncAllPermissions();

        if (PermissionManager.getInstance(context).isGranted(tag)) {
            onGranted.run();
        } else {
            onDenied.run();
        }
    }

    // ─────────────────────────────────────────────
    //  6. NAVIGATION HELPERS
    // ─────────────────────────────────────────────

    /**
     * Opens the app's Permission Manager screen.
     * Call this when the user needs to fix multiple permissions.
     * <p>
     * NOTE: Replace PermissionActivity.class with your actual
     * Activity that hosts the PermissionFragment.
     */
    public static void openPermissionScreen(Context context) {
        // Option A: If you have a dedicated PermissionActivity
        // Intent intent = new Intent(context, PermissionActivity.class);
        // context.startActivity(intent);

        // Option B: If PermissionFragment is inside a Navigation graph
        // Use NavController to navigate
        // Navigation.findNavController(activity, R.id.nav_host).navigate(R.id.permissionFragment);

        // For now, open App Settings as fallback
        openAppSettings(context);
    }

    /**
     * Opens Android's App Settings page for this app.
     */
    public static void openAppSettings(Context context) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", context.getPackageName(), null);
        intent.setData(uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    // ─────────────────────────────────────────────
    //  7. RATIONALE DIALOGS
    // ─────────────────────────────────────────────

    /**
     * Shows a dialog explaining WHY the permission is needed,
     * with an option to go to Settings.
     */
    public static void showRationaleDialog(Activity activity, String tag,
                                           Runnable onGoToSettings) {
        PermissionManager pm = PermissionManager.getInstance(activity);
        String name = pm.getReadableName(tag);

        new AlertDialog.Builder(activity)
                .setTitle(name + " Permission Required")
                .setMessage("This feature needs " + name + " access to work properly. " +
                        "Please enable it in Settings.")
                .setPositiveButton("Open Settings", (d, w) -> {
                    if (onGoToSettings != null) onGoToSettings.run();
                    else openAppSettings(activity);
                })
                .setNegativeButton("Not Now", null)
                .setCancelable(true)
                .show();
    }

    private static void showRedirectDialog(Activity activity, String tag) {
        PermissionManager pm = PermissionManager.getInstance(activity);
        String name = pm.getReadableName(tag);

        new AlertDialog.Builder(activity)
                .setTitle(name + " Not Enabled")
                .setMessage("Please enable " + name + " from the Permission Manager screen.")
                .setPositiveButton("Go to Permissions", (d, w) -> openPermissionScreen(activity))
                .setNegativeButton("Cancel", null)
                .show();
    }
}
package app.xedigital.ai.utills;

import android.content.Intent;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.datatransport.runtime.BuildConfig;
import com.google.android.material.button.MaterialButton;

import java.util.List;

import app.xedigital.ai.R;

/**
 * Base activity that enforces device-level security checks on every resume.
 * <p>
 * Build-type behavior:
 * DEBUG   → checks Mock Location + Auto Time only (skips Developer Options)
 * RELEASE → checks Mock Location + Auto Time + Developer Options
 */
public abstract class SecureBaseActivity extends AppCompatActivity {

    private static final String TAG = "SecureBaseActivity";
    private AlertDialog securityDialog;

    @Override
    protected void onResume() {
        super.onResume();
        enforceSecurityChecks();
    }

    @Override
    protected void onDestroy() {
        dismissSecurityDialog();
        super.onDestroy();
    }

    /**
     * Override in subclasses to completely disable all checks on a specific screen.
     * Default = always enforce.
     */
    protected boolean shouldEnforceSecurityChecks() {
        return true;
    }

    private void enforceSecurityChecks() {
        if (!shouldEnforceSecurityChecks()) return;
        if (isFinishing() || isDestroyed()) return;

        boolean isDebug = BuildConfig.DEBUG;

        if (isDebug) {
            Log.d(TAG, "🔧 Debug build — checking Mock Location & Auto Time only (Dev Options skipped)");
        }

        List<SecurityCheckManager.Violation> violations =
                SecurityCheckManager.runAllChecks(this, isDebug);

        if (violations.isEmpty()) {
            dismissSecurityDialog();
        } else {
            showSecurityBlockingDialog(violations);
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  DIALOG
    // ─────────────────────────────────────────────────────────────

    private void showSecurityBlockingDialog(List<SecurityCheckManager.Violation> violations) {
        if (securityDialog != null && securityDialog.isShowing()) {
            updateDialogContent(violations);
            return;
        }

        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_security_violation, null);

        TextView messageView = dialogView.findViewById(R.id.tvViolationMessage);
        MaterialButton btnSettings = dialogView.findViewById(R.id.btnOpenSettings);
        MaterialButton btnExit = dialogView.findViewById(R.id.btnExitApp);

        messageView.setText(buildViolationMessage(violations));

        securityDialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        if (securityDialog.getWindow() != null) {
            securityDialog.getWindow()
                    .setBackgroundDrawableResource(android.R.color.transparent);
            securityDialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            );
        }

        btnSettings.setOnClickListener(v -> openRelevantSettings(violations.get(0)));
        btnExit.setOnClickListener(v -> exitApp());

        securityDialog.show();
    }

    private void updateDialogContent(List<SecurityCheckManager.Violation> violations) {
        try {
            View root = securityDialog.findViewById(R.id.tvViolationMessage);
            if (root instanceof TextView) {
                ((TextView) root).setText(buildViolationMessage(violations));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating dialog: " + e.getMessage());
        }
    }

    private CharSequence buildViolationMessage(List<SecurityCheckManager.Violation> violations) {
        StringBuilder sb = new StringBuilder();
        sb.append("For your security and data integrity, the following must be fixed before using the app:\n\n");

        int index = 1;
        for (SecurityCheckManager.Violation v : violations) {
            sb.append(index++)
                    .append(". ")
                    .append(v.title)
                    .append("\n   → ")
                    .append(v.message)
                    .append("\n\n");
        }
        sb.append("Please correct these settings and return to the app.");
        return sb.toString();
    }

    // ─────────────────────────────────────────────────────────────
    //  SETTINGS DEEP-LINKS
    // ─────────────────────────────────────────────────────────────

    private void openRelevantSettings(SecurityCheckManager.Violation violation) {
        try {
            Intent intent;
            switch (violation) {
                case AUTO_TIME_DISABLED:
                    intent = new Intent(Settings.ACTION_DATE_SETTINGS);
                    break;
                case MOCK_LOCATION:
                case DEVELOPER_OPTIONS:
                default:
                    intent = new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS);
                    break;
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Falling back to general settings: " + e.getMessage());
            try {
                startActivity(new Intent(Settings.ACTION_SETTINGS));
            } catch (Exception ignored) {
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  LIFECYCLE HELPERS
    // ─────────────────────────────────────────────────────────────

    private void dismissSecurityDialog() {
        if (securityDialog != null && securityDialog.isShowing()) {
            try {
                securityDialog.dismiss();
            } catch (Exception ignored) {
            }
        }
        securityDialog = null;
    }

    private void exitApp() {
        dismissSecurityDialog();
        finishAffinity();
        System.exit(0);
    }
}
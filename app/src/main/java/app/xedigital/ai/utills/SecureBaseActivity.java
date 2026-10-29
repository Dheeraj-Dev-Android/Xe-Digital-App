package app.xedigital.ai.utills;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;

import java.util.List;

import app.xedigital.ai.R;
import app.xedigital.ai.activity.LoginSelectionActivity;
import app.xedigital.ai.admin.adminAPI.AdminAPIClient;
import app.xedigital.ai.api.APIClient;

public abstract class SecureBaseActivity extends AppCompatActivity {

    private static final String TAG = "SecureBaseActivity";
    private AlertDialog securityDialog;

    private BroadcastReceiver sessionExpiredReceiver;

    @Override
    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize API clients with application context for CookieJar + Authenticator
        APIClient.init(this);
        AdminAPIClient.init(this);

        // Receiver for session-expired broadcasts from TokenAuthenticator
        sessionExpiredReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("ACTION_SESSION_EXPIRED".equals(intent.getAction())) {
                    handleSessionExpired();
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        enforceSecurityChecks();

        // Register session-expired receiver while activity is visible
        if (sessionExpiredReceiver != null) {
            try {
                IntentFilter filter = new IntentFilter("ACTION_SESSION_EXPIRED");
                ContextCompat.registerReceiver(this, sessionExpiredReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
            } catch (Exception e) {
                Log.e(TAG, "Error registering session receiver: " + e.getMessage());
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Unregister to avoid leaks and background handling
        if (sessionExpiredReceiver != null) {
            try {
                unregisterReceiver(sessionExpiredReceiver);
            } catch (IllegalArgumentException e) {
                // Receiver was not registered — safe to ignore
            }
        }
    }

    @Override
    protected void onDestroy() {
        dismissSecurityDialog();
        super.onDestroy();
    }

    /**
     * Called when TokenAuthenticator determines the session is dead
     * (refresh token expired, revoked, or reuse detected).
     */
    private void handleSessionExpired() {
        if (isFinishing() || isDestroyed()) return;

        Log.w(TAG, "Session expired — redirecting to login");

        // Prevent the receiver from firing again during navigation
        try {
            unregisterReceiver(sessionExpiredReceiver);
        } catch (IllegalArgumentException ignored) {
        }

        Intent intent = new Intent(this, LoginSelectionActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    protected boolean shouldEnforceSecurityChecks() {
        return true;
    }

    private boolean isAppDebuggable() {
        return (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
    }

    private void enforceSecurityChecks() {
        if (!shouldEnforceSecurityChecks()) return;
        if (isFinishing() || isDestroyed()) return;

        boolean debuggable = isAppDebuggable();

        if (debuggable) {
            Log.d(TAG, "🔧 Debuggable APK detected — Dev Options check skipped (Mock Location & Auto Time still enforced)");
        }

        List<SecurityCheckManager.Violation> violations = SecurityCheckManager.runAllChecks(this, debuggable);

        if (violations.isEmpty()) {
            dismissSecurityDialog();
        } else {
            showSecurityBlockingDialog(violations);
        }
    }

    private void showSecurityBlockingDialog(List<SecurityCheckManager.Violation> violations) {
        if (securityDialog != null && securityDialog.isShowing()) {
            updateDialogContent(violations);
            return;
        }

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_security_violation, null);

        TextView messageView = dialogView.findViewById(R.id.tvViolationMessage);
        MaterialButton btnSettings = dialogView.findViewById(R.id.btnOpenSettings);
        MaterialButton btnExit = dialogView.findViewById(R.id.btnExitApp);

        messageView.setText(buildViolationMessage(violations));

        securityDialog = new AlertDialog.Builder(this).setView(dialogView).setCancelable(false).create();

        if (securityDialog.getWindow() != null) {
            securityDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            securityDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
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
            sb.append(index++).append(". ").append(v.title).append("\n   → ").append(v.message).append("\n\n");
        }
        sb.append("Please correct these settings and return to the app.");
        return sb.toString();
    }

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
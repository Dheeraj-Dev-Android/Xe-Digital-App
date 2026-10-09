package app.xedigital.ai.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;

import app.xedigital.ai.AdminMainActivity;
import app.xedigital.ai.R;
import app.xedigital.ai.utills.RoleAccessManager;
import app.xedigital.ai.utills.RoleGroup;
import app.xedigital.ai.utills.SecurePrefManager;

public class LoginSelectionActivity extends AppCompatActivity {

    private CardView cvAdminLogin, cvEmployeeLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        // ✅ Auto-login if active session exists
        if (routeIfActiveSession()) {
            return;
        }

        setContentView(R.layout.activity_login_selection);

        cvAdminLogin = findViewById(R.id.cvAdminLogin);
        cvEmployeeLogin = findViewById(R.id.cvEmployeeLogin);

        cvAdminLogin.setOnClickListener(view -> {
            Intent intent = new Intent(this, AdminLoginActivity.class);
            intent.putExtra("isEmployee", false);
            startActivity(intent);
        });

        cvEmployeeLogin.setOnClickListener(view -> {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.putExtra("isEmployee", true);
            startActivity(intent);
        });
    }

    /**
     * Checks whether a saved session exists and routes directly to the right home activity.
     * Returns true if the user was auto-routed, false if the selection screen should be shown.
     */
    private boolean routeIfActiveSession() {
        String cachedRole = RoleAccessManager.getInstance(this).getRoleName();
        SecurePrefManager securePrefs = SecurePrefManager.getInstance(this);

        // 1️⃣ Admin session check (SecurePrefManager)
        String adminToken = securePrefs.getString("authToken", "");
        String adminUserId = securePrefs.getString("userId", "");

        if (!adminToken.isEmpty() && !adminUserId.isEmpty() && RoleGroup.isAdminTier(cachedRole)) {
            Intent intent = new Intent(this, AdminMainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return true;
        }

        // 2️⃣ Employee session check (SecurePrefManager)
        String empToken = securePrefs.getString("authToken", null);
        String empUserId = securePrefs.getString("userId", null);

        if (empToken != null && empUserId != null && RoleGroup.isEmployeeTier(cachedRole)) {
            // Route to LoginActivity which handles silent token refresh + FaceLogin
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return true;
        }

        return false;
    }
}
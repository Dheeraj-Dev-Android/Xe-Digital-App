package app.xedigital.ai.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;

import app.xedigital.ai.R;


public class LoginSelectionActivity extends AppCompatActivity {

    private CardView cvAdminLogin, cvEmployeeLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_login_selection);

        cvAdminLogin = findViewById(R.id.cvAdminLogin);
        cvEmployeeLogin = findViewById(R.id.cvEmployeeLogin);

        cvAdminLogin.setOnClickListener(view -> {
            Intent intent = new Intent(LoginSelectionActivity.this, AdminLoginActivity.class);
            intent.putExtra("isEmployee", false); // 👈 Pass boolean false for admin
            startActivity(intent);
        });

        cvEmployeeLogin.setOnClickListener(view -> {
            Intent intent = new Intent(LoginSelectionActivity.this, LoginActivity.class);
            intent.putExtra("isEmployee", true); // 👈 Pass boolean true for employee
            startActivity(intent);
        });
    }
}

package app.xedigital.ai.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;

import app.xedigital.ai.NetworkChangeReceiver;
import app.xedigital.ai.R;
import app.xedigital.ai.utills.BioMetric;
import app.xedigital.ai.utills.NetworkUtils;

public class SplashActivity extends AppCompatActivity {
    private final NetworkChangeReceiver networkChangeReceiver = new NetworkChangeReceiver();
    private final boolean isShowingNoInternetDialog = false;
    private final boolean isShowingSlowNetworkDialog = false;
    private AlertDialog noInternetDialog;
    private LottieAnimationView noInternetAnimation;
    private TextView tvSpeed;
    private BiometricManager biometricManager;
    private BioMetric bioMetric;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        noInternetAnimation = findViewById(R.id.noInternetAnimation);

        Glide.with(this).load(R.mipmap.ic_launcher).apply(RequestOptions.bitmapTransform(new CircleCrop())).into((android.widget.ImageView) findViewById(R.id.iv_app_logo));

        noInternetDialog = new AlertDialog.Builder(this).setTitle("No Internet Connection").setMessage("Please check your internet connection and try again.").setPositiveButton("OK", (dialog, which) -> finish()).setCancelable(false).create();

        // Create the slow network alert dialog
        AlertDialog slowNetworkDialog = new AlertDialog.Builder(this).setTitle("Slow Network Connection").setMessage("Your network connection is slow. Some features might be affected.").setPositiveButton("OK", null).setCancelable(true).create();
        FrameLayout slowInternetContainer = findViewById(R.id.slowInternetContainer);
        View slowInternetLayout = findViewById(R.id.slowInternetLayout);
        tvSpeed = findViewById(R.id.tvSpeed);
        ImageButton dismissButton = findViewById(R.id.btnDismiss);
        if (dismissButton != null) {
            dismissButton.setOnClickListener(v -> {
                slowInternetLayout.setVisibility(View.GONE);
                Toast.makeText(SplashActivity.this, "Dismiss button clicked", Toast.LENGTH_SHORT).show();
            });
        }
//
//        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
//        biometricManager = BiometricManager.from(this);
        new Handler().postDelayed(() -> {
            // Check network status before starting LoginActivity
            if (NetworkUtils.isNetworkAvailable(this)) {
                Intent mainIntent = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(mainIntent);
                finish();
//                attemptBiometricLogin();
            } else {
                showNoInternetLayout();
            }
        }, 3000);

        Button btnGetStarted = findViewById(R.id.btn_get_started);
        btnGetStarted.setOnClickListener(v -> {
            Intent mainIntent = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(mainIntent);
            finish();
//            attemptBiometricLogin();
        });
    }

//    private void attemptBiometricLogin() {
//        boolean isBiometricSupported = isBiometricSupported();
//        boolean isLoggedIn = isLoggedIn();
//        if (isLoggedIn && isBiometricSupported) {
//            initiateBiometricAuthentication();
//        } else {
//            navigateToLoginActivity();
//        }
//    }

//    private boolean isBiometricSupported() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            return biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL | BiometricManager.Authenticators.BIOMETRIC_WEAK) == BiometricManager.BIOMETRIC_SUCCESS;
//        } else {
//            return biometricManager.canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS;
//        }
//    }

//    private boolean isLoggedIn() {
////        return sharedPreferences.getBoolean("isLoggedIn", false);
//        String authToken = sharedPreferences.getString("authToken", null);
//        return authToken != null;
//    }
//
//    private void initiateBiometricAuthentication() {
//        bioMetric = new BioMetric(this, this, new BioMetric.BiometricAuthListener() {
//            @Override
//            public void onAuthenticationSucceeded() {
//                navigateToMainActivity();
//            }
//
//            @Override
//            public void onAuthenticationError(int errorCode, CharSequence errString) {
//                navigateToLoginActivity();
//            }
//
//            @Override
//            public void onAuthenticationFailed() {
//                navigateToLoginActivity();
//            }
//        });
//        bioMetric.authenticate(true);
//    }

//    private void navigateToMainActivity() {
//        Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
//        startActivity(mainIntent);
//        finish();
//    }
//
//    private void navigateToLoginActivity() {
//        Intent loginIntent = new Intent(SplashActivity.this, LoginActivity.class);
//        startActivity(loginIntent);
//        finish();
//    }

    public void showNoInternetLayout() {
        findViewById(R.id.noInternetLayout).setVisibility(View.VISIBLE);
        noInternetAnimation.playAnimation();
    }

    public void hideNoInternetLayout() {
        findViewById(R.id.noInternetLayout).setVisibility(View.GONE);
        noInternetAnimation.pauseAnimation();
    }

    public void showSlowInternetLayout(double speed) {
        findViewById(R.id.slowInternetLayout).setVisibility(View.VISIBLE);
        String speedText = String.format("Current speed: %.2f Mbps", speed / 1000);
        tvSpeed.setText(speedText);
        noInternetAnimation.playAnimation();
    }

    public void hideSlowInternetLayout() {
        findViewById(R.id.slowInternetLayout).setVisibility(View.GONE);
        noInternetAnimation.pauseAnimation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(networkChangeReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));

        if (!NetworkUtils.isNetworkAvailable(this)) {
            showNoInternetLayout();
        } else {
            hideNoInternetLayout();
            NetworkUtils.NetworkSpeed networkSpeed = NetworkUtils.getNetworkSpeed(this);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && networkSpeed.downSpeedKbps < 80) {
                showSlowInternetLayout(networkSpeed.downSpeedKbps);
            } else {
                hideSlowInternetLayout();
            }
        }
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (bioMetric != null) {
//            // Pass the 'forLogin' flag here (true for login)
//            bioMetric.handlePermissionResult(requestCode, permissions, grantResults, true);
//        }
//    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(networkChangeReceiver);

    }

    public void onRetryButtonClicked(View view) {
        // Code to retry network operations
        if (NetworkUtils.isNetworkAvailable(this)) {
            hideNoInternetLayout();

        } else {
            // Potentially show a toast or message if the network is still not available
            Toast.makeText(this, "Still no internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    public void onOpenSettingsButtonClicked(View view) {
        startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
    }
}
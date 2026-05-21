package app.xedigital.ai.activity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;

import app.xedigital.ai.R;
import app.xedigital.ai.utills.NetworkUtils;

public class SplashActivity extends AppCompatActivity {
    private static final String TAG = "SplashActivity";
    private final Handler splashHandler = new Handler(Looper.getMainLooper());
    private Runnable splashRunnable;

    private LottieAnimationView noInternetAnimation;
    private FrameLayout slowInternetContainer;
    private TextView tvSpeed;
    private TextView tvAppVersion;

    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_splash);

        noInternetAnimation = findViewById(R.id.noInternetAnimation);
        Glide.with(this)
                .load(R.mipmap.ic_launcher)
                .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                .into((android.widget.ImageView) findViewById(R.id.iv_app_logo));

        tvSpeed = findViewById(R.id.tvSpeed);
        slowInternetContainer = findViewById(R.id.slowInternetContainer);
        tvAppVersion = findViewById(R.id.tv_app_version);

        if (tvAppVersion != null) {
            try {
                String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
                int versionCode = Build.VERSION.SDK_INT >= 28
                        ? (int) getPackageManager().getPackageInfo(getPackageName(), 0).getLongVersionCode()
                        : getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;

                tvAppVersion.setText("App Version : " + versionCode + "." + versionName);
            } catch (Exception e) {
                Log.e(TAG, "Version error", e);
            }
        }

        ImageButton dismissButton = findViewById(R.id.btnDismiss);
        if (dismissButton != null) {
            dismissButton.setOnClickListener(v -> hideSlowInternetLayout());
        }

        ImageButton btnGetStarted = findViewById(R.id.btn_get_started);
        if (btnGetStarted != null) {
            btnGetStarted.setOnClickListener(v -> navigateToLogin());
        }

        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        setupNetworkCallback();

        splashRunnable = this::checkNetworkAndNavigate;
        splashHandler.postDelayed(splashRunnable, 2500);
    }

    private void setupNetworkCallback() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            networkCallback = new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(@NonNull Network network) {
                    runOnUiThread(() -> {
                        hideNoInternetLayout();
                        // Clear slow internet banners if full network health changes
                        if (NetworkUtils.isNetworkSpeedGood(SplashActivity.this, 80)) {
                            hideSlowInternetLayout();
                        }
                    });
                }

                @Override
                public void onLost(@NonNull Network network) {
                    runOnUiThread(() -> showNoInternetLayout());
                }
            };
        }
    }

    private void checkNetworkAndNavigate() {
        if (isFinishing() || isDestroyed()) return;

        if (NetworkUtils.isNetworkAvailable(this)) {
            NetworkUtils.NetworkSpeed networkSpeed = NetworkUtils.getNetworkSpeed(this);
            Log.d(TAG, "Download Speed: " + networkSpeed.downSpeedKbps + " Kbps");
            if (networkSpeed.downSpeedKbps < 80) {
                showSlowInternetLayout(networkSpeed.downSpeedKbps);
            } else {
                navigateToLogin();
            }
        } else {
            showNoInternetLayout();
        }
    }

    private void navigateToLogin() {
        splashHandler.removeCallbacksAndMessages(null);
        Intent mainIntent = new Intent(SplashActivity.this, LoginActivity.class);
        startActivity(mainIntent);
        finish();
    }

    public void showNoInternetLayout() {
        View layout = findViewById(R.id.noInternetLayout);
        if (layout != null) layout.setVisibility(View.VISIBLE);
        if (noInternetAnimation != null) noInternetAnimation.playAnimation();
    }

    public void hideNoInternetLayout() {
        View layout = findViewById(R.id.noInternetLayout);
        if (layout != null) layout.setVisibility(View.GONE);
        if (noInternetAnimation != null) noInternetAnimation.pauseAnimation();
    }

    public void showSlowInternetLayout(double speed) {
        runOnUiThread(() -> {
            if (slowInternetContainer != null) {
                slowInternetContainer.setVisibility(View.VISIBLE);
                String speedText = String.format("Current speed: %.2f Mbps", speed / 1000);
                if (tvSpeed != null) tvSpeed.setText(speedText);
            }
        });
    }

    // Fix: Re-implemented method to clear target error across receiver bindings safely
    public void hideSlowInternetLayout() {
        runOnUiThread(() -> {
            if (slowInternetContainer != null) {
                slowInternetContainer.setVisibility(View.GONE);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && connectivityManager != null && networkCallback != null) {
            connectivityManager.registerDefaultNetworkCallback(networkCallback);
        }

        if (!NetworkUtils.isNetworkAvailable(this)) {
            showNoInternetLayout();
        } else {
            hideNoInternetLayout();
            if (NetworkUtils.isNetworkSpeedGood(this, 80)) {
                hideSlowInternetLayout();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && connectivityManager != null && networkCallback != null) {
            try {
                connectivityManager.unregisterNetworkCallback(networkCallback);
            } catch (Exception e) {
                Log.w(TAG, "Network callback was not registered", e);
            }
        }
    }

    @Override
    protected void onDestroy() {
        splashHandler.removeCallbacks(splashRunnable);
        super.onDestroy();
    }

    public void onRetryButtonClicked(View view) {
        if (NetworkUtils.isNetworkAvailable(this)) {
            hideNoInternetLayout();
            if (NetworkUtils.isNetworkSpeedGood(this, 80)) {
                hideSlowInternetLayout();
                navigateToLogin();
            } else {
                NetworkUtils.NetworkSpeed networkSpeed = NetworkUtils.getNetworkSpeed(this);
                showSlowInternetLayout(networkSpeed.downSpeedKbps);
            }
        } else {
            Toast.makeText(this, "Still no internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    public void onOpenSettingsButtonClicked(View view) {
        startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
    }
}
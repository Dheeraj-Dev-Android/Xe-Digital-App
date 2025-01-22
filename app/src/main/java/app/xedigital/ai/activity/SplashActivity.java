package app.xedigital.ai.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;

import app.xedigital.ai.NetworkChangeReceiver;
import app.xedigital.ai.R;
import app.xedigital.ai.utills.NetworkUtils;

public class SplashActivity extends AppCompatActivity {
    private final NetworkChangeReceiver networkChangeReceiver = new NetworkChangeReceiver();
    private AlertDialog noInternetDialog;
    private AlertDialog slowNetworkDialog;
    private LottieAnimationView noInternetAnimation;
    private FrameLayout slowInternetContainer;
    private TextView tvSpeed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Initialize UI components
        noInternetAnimation = findViewById(R.id.noInternetAnimation);
        Glide.with(this).load(R.mipmap.ic_launcher).apply(RequestOptions.bitmapTransform(new CircleCrop())).into((android.widget.ImageView) findViewById(R.id.iv_app_logo));

        tvSpeed = findViewById(R.id.tvSpeed);
        slowInternetContainer = findViewById(R.id.slowInternetContainer);

        // Dismiss button for slow internet layout
        ImageButton dismissButton = findViewById(R.id.btnDismiss);
        if (dismissButton != null) {
            dismissButton.setOnClickListener(v -> {
                slowInternetContainer.setVisibility(View.GONE);
                Toast.makeText(SplashActivity.this, "Slow internet message dismissed", Toast.LENGTH_SHORT).show();
            });
        }

        // Handle "Get Started" button
        Button btnGetStarted = findViewById(R.id.btn_get_started);
        btnGetStarted.setOnClickListener(v -> navigateToLogin());

        // No internet dialog
        noInternetDialog = new AlertDialog.Builder(this).setTitle("No Internet Connection").setMessage("Please check your internet connection and try again.").setPositiveButton("OK", (dialog, which) -> finish()).setCancelable(false).create();

        // Slow network dialog
        slowNetworkDialog = new AlertDialog.Builder(this).setTitle("Slow Network Connection").setMessage("Your network connection is slow. Some features might be affected.").setPositiveButton("OK", null).setCancelable(true).create();

        // Simulate splash screen delay and check network status
        new Handler().postDelayed(this::checkNetworkAndNavigate, 3000);
    }

    private void checkNetworkAndNavigate() {
        if (NetworkUtils.isNetworkAvailable(this)) {
            // Check for slow internet
            NetworkUtils.NetworkSpeed networkSpeed = NetworkUtils.getNetworkSpeed(this);
            Log.d("SplashActivity", "Download Speed: " + networkSpeed.downSpeedKbps + " Kbps");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && networkSpeed.downSpeedKbps < 80) {
                showSlowInternetLayout(networkSpeed.downSpeedKbps);
            } else {
                navigateToLogin();
            }
        } else {
            showNoInternetLayout();
        }
    }

    private void navigateToLogin() {
        Intent mainIntent = new Intent(SplashActivity.this, LoginActivity.class);
        startActivity(mainIntent);
        finish();
    }

    public void showNoInternetLayout() {
        findViewById(R.id.noInternetLayout).setVisibility(View.VISIBLE);
        noInternetAnimation.playAnimation();
    }

    public void hideNoInternetLayout() {
        findViewById(R.id.noInternetLayout).setVisibility(View.GONE);
        noInternetAnimation.pauseAnimation();
    }

    public void showSlowInternetLayout(double speed) {
        runOnUiThread(() -> {
            if (slowInternetContainer != null) {
                slowInternetContainer.setVisibility(View.VISIBLE);
                String speedText = String.format("Current speed: %.2f Mbps", speed / 1000);
                tvSpeed.setText(speedText);
            }
        });
    }

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
        registerReceiver(networkChangeReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));

        if (!NetworkUtils.isNetworkAvailable(this)) {
            showNoInternetLayout();
        } else {
            hideNoInternetLayout();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(networkChangeReceiver);
    }

    public void onRetryButtonClicked(View view) {
        if (NetworkUtils.isNetworkAvailable(this)) {
            hideNoInternetLayout();
        } else {
            Toast.makeText(this, "Still no internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    public void onOpenSettingsButtonClicked(View view) {
        startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
    }
}

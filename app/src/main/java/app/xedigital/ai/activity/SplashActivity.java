package app.xedigital.ai.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
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
    private final boolean isShowingNoInternetDialog = false;
    private final boolean isShowingSlowNetworkDialog = false;
    private AlertDialog noInternetDialog;
    private AlertDialog slowNetworkDialog;
    private LottieAnimationView noInternetAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        noInternetAnimation = findViewById(R.id.noInternetAnimation);

        Glide.with(this).load(R.mipmap.ic_launcher).apply(RequestOptions.bitmapTransform(new CircleCrop())).into((android.widget.ImageView) findViewById(R.id.iv_app_logo));

        noInternetDialog = new AlertDialog.Builder(this).setTitle("No Internet Connection").setMessage("Please check your internet connection and try again.").setPositiveButton("OK", (dialog, which) -> finish()).setCancelable(false).create();

        // Create the slow network alert dialog
        slowNetworkDialog = new AlertDialog.Builder(this).setTitle("Slow Network Connection").setMessage("Your network connection is slow. Some features might be affected.").setPositiveButton("OK", null).setCancelable(true).create();

        new Handler().postDelayed(() -> {
            // Check network status before starting LoginActivity
            if (NetworkUtils.isNetworkAvailable(this)) {
                Intent mainIntent = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(mainIntent);
                finish();
            } else {
                showNoInternetLayout();
            }
        }, 3000);

        Button btnGetStarted = findViewById(R.id.btn_get_started);
        btnGetStarted.setOnClickListener(v -> {
            Intent mainIntent = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(mainIntent);
            finish();
        });
    }

    public void showNoInternetLayout() {
        findViewById(R.id.noInternetLayout).setVisibility(View.VISIBLE);
        noInternetAnimation.playAnimation();
    }

    public void hideNoInternetLayout() {
        findViewById(R.id.noInternetLayout).setVisibility(View.GONE);
        noInternetAnimation.pauseAnimation();
    }

    public void showSlowInternetLayout() {
        findViewById(R.id.slowInternetLayout).setVisibility(View.VISIBLE);
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
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !NetworkUtils.isNetworkSpeedGood(this)) {
            showSlowInternetLayout();
        } else {
            hideNoInternetLayout();
            hideSlowInternetLayout();

        }
    }

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
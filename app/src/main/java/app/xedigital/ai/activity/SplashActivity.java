package app.xedigital.ai.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;

import app.xedigital.ai.NetworkChangeReceiver;
import app.xedigital.ai.R;
import app.xedigital.ai.utills.NetworkUtils;

public class SplashActivity extends AppCompatActivity {
    private NetworkChangeReceiver networkChangeReceiver = new NetworkChangeReceiver();

    private AlertDialog noInternetDialog;
    private boolean isShowingNoInternetDialog = false;
    private AlertDialog slowNetworkDialog;
    private boolean isShowingSlowNetworkDialog = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Glide.with(this)
                .load(R.mipmap.ic_launcher)
                .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                .into((android.widget.ImageView) findViewById(R.id.iv_app_logo));

        noInternetDialog = new AlertDialog.Builder(this)
                .setTitle("No Internet Connection")
                .setMessage("Please check your internet connection and try again.")
                .setPositiveButton("OK", (dialog, which) -> finish())
                .setCancelable(false)
                .create();

        // Create the slow network alert dialog
        slowNetworkDialog = new AlertDialog.Builder(this)
                .setTitle("Slow Network Connection")
                .setMessage("Your network connection is slow. Some features might be affected.")
                .setPositiveButton("OK", null)
                .setCancelable(true)
                .create();

        new Handler().postDelayed(() -> {
            // Check network status before starting LoginActivity
            if (NetworkUtils.isNetworkAvailable(this)) {
                Intent mainIntent = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(mainIntent);
                finish();
            } else {
                showNoInternetAlert();
            }
        }, 3000);

        Button btnGetStarted = findViewById(R.id.btn_get_started);
        btnGetStarted.setOnClickListener(v -> {
            Intent mainIntent = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(mainIntent);
            finish();
        });
    }

    public void showNoInternetAlert() {
        if (!isShowingNoInternetDialog) {
            noInternetDialog.show();
            isShowingNoInternetDialog = true;
        }
    }

    public void hideNoInternetAlert() {
        if (noInternetDialog != null && noInternetDialog.isShowing()) {
            noInternetDialog.dismiss();
            isShowingNoInternetDialog = false;
        }
    }

    public void showSlowNetworkAlert() {
        if (!isShowingSlowNetworkDialog) {
            slowNetworkDialog.show();
            isShowingSlowNetworkDialog = true;
        }
    }

    public void hideSlowNetworkAlert() {
        if (slowNetworkDialog != null && slowNetworkDialog.isShowing()) {
            slowNetworkDialog.dismiss();
            isShowingSlowNetworkDialog = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(networkChangeReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
        if (!NetworkUtils.isNetworkAvailable(this) && !isShowingNoInternetDialog) {
            noInternetDialog.show();
            isShowingNoInternetDialog = true;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(networkChangeReceiver);
        if (noInternetDialog != null && noInternetDialog.isShowing()) {
            noInternetDialog.dismiss();
            isShowingNoInternetDialog = false;
        }
    }
}
package app.xedigital.ai;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;

import app.xedigital.ai.activity.SplashActivity;
import app.xedigital.ai.utills.NetworkUtils;

public class NetworkChangeReceiver extends BroadcastReceiver {

    public static final String EXTRA_SPEED = "extra_speed";
    private static final int SLOW_NETWORK_THRESHOLD_KBPS = 80;
    private static final int MINIMUM_SPEED_KBPS = 80;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            Log.e("NetworkChangeReceiver", "Received unexpected intent action: " + intent.getAction());
            return;
        }

        // Check if the network is available and if the network speed is above the slow network threshold
        boolean isNetworkAvailable = NetworkUtils.isNetworkAvailable(context);
        NetworkUtils.NetworkSpeed networkSpeed = NetworkUtils.getNetworkSpeed(context);
        boolean isSpeedGood = isNetworkAvailable && networkSpeed.downSpeedKbps >= SLOW_NETWORK_THRESHOLD_KBPS;

        // Handle the network state change based on the availability and speed
        handleNetworkStateChange(context, isNetworkAvailable, isSpeedGood, networkSpeed.downSpeedKbps);

        // If network is available and the speed is below the minimum speed threshold, show slow internet UI
        if (isNetworkAvailable && networkSpeed.downSpeedKbps < MINIMUM_SPEED_KBPS) {
            showSlowInternetLayout(context, networkSpeed.downSpeedKbps);
        } else if (isNetworkAvailable) {
            // If network is available and speed is good, hide slow internet UI
            hideSlowInternetLayout(context);
        }
    }

    /**
     * Shows slow internet layout based on the context (either MainActivity or SplashActivity).
     *
     * @param context the application context
     */
    private void showSlowInternetLayout(Context context, double speed) {
        if (context instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) context;
            mainActivity.showSlowInternetLayout(speed);
        } else if (context instanceof SplashActivity) {
            SplashActivity splashActivity = (SplashActivity) context;
            splashActivity.showSlowInternetLayout(speed);
        }
    }

    /**
     * Hides slow internet layout based on the context (either MainActivity or SplashActivity).
     *
     * @param context the application context
     */
    private void hideSlowInternetLayout(Context context) {
        if (context instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) context;
            mainActivity.hideSlowInternetLayout();
        } else if (context instanceof SplashActivity) {
            SplashActivity splashActivity = (SplashActivity) context;
            splashActivity.hideSlowInternetLayout();
        }
    }

    /**
     * Handles the network state change and updates UI based on the network status.
     *
     * @param context            the application context
     * @param isNetworkAvailable indicates whether the network is available
     * @param isSpeedGood        indicates whether the network speed is good
     */
    private void handleNetworkStateChange(Context context, boolean isNetworkAvailable, boolean isSpeedGood, double speed) {
        if (context instanceof SplashActivity) {
            SplashActivity splashActivity = (SplashActivity) context;
            if (!isNetworkAvailable) {
                splashActivity.showNoInternetLayout();
            } else {
                splashActivity.hideNoInternetLayout();
                if (!isSpeedGood) {
                    splashActivity.showSlowInternetLayout(speed);
                } else {
                    splashActivity.hideSlowInternetLayout();
                }
            }
        } else if (context instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) context;
            if (!isNetworkAvailable) {
                mainActivity.showNoInternetLayout();
            } else {
                mainActivity.hideNoInternetLayout();
                if (!isSpeedGood) {
                    mainActivity.showSlowInternetLayout(speed);
                } else {
                    mainActivity.hideSlowInternetLayout();
                }
            }
        }
    }
}

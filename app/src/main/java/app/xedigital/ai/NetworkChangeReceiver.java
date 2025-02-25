package app.xedigital.ai;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;

import app.xedigital.ai.activity.SplashActivity;
import app.xedigital.ai.utills.NetworkUtils;

public class NetworkChangeReceiver extends BroadcastReceiver {
    private static final String TAG = "NetworkChangeReceiver";
    private static final int SLOW_NETWORK_THRESHOLD_KBPS = 80;
    private static final int MINIMUM_SPEED_KBPS = 50;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            Log.e(TAG, "Received unexpected intent action: " + intent.getAction());
            return;
        }
        // Check if network is available and retrieve network speed
        boolean isNetworkAvailable = NetworkUtils.isNetworkAvailable(context);
        NetworkUtils.NetworkSpeed networkSpeed = NetworkUtils.getNetworkSpeed(context);
        // Determine if the network speed is above the slow network threshold
        boolean isSpeedGood = isNetworkAvailable && networkSpeed.downSpeedKbps >= SLOW_NETWORK_THRESHOLD_KBPS;
        Log.d(TAG, "Network available: " + isNetworkAvailable + ", Speed: " + networkSpeed.downSpeedKbps + " Kbps, Is speed good: " + isSpeedGood);
        // Handle network state change
        handleNetworkStateChange(context, isNetworkAvailable, isSpeedGood, networkSpeed.downSpeedKbps);
    }

    /**
     * Handles the network state change and updates the UI accordingly.
     *
     * @param context            the application context
     * @param isNetworkAvailable indicates whether the network is available
     * @param isSpeedGood        indicates whether the network speed is good
     * @param speed              the current network speed in Kbps
     */
    private void handleNetworkStateChange(Context context, boolean isNetworkAvailable, boolean isSpeedGood, int speed) {
        if (context instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) context;
            updateMainActivityUI(mainActivity, isNetworkAvailable, isSpeedGood, speed);
        } else if (context instanceof SplashActivity) {
            SplashActivity splashActivity = (SplashActivity) context;
            updateSplashActivityUI(splashActivity, isNetworkAvailable, isSpeedGood, speed);
        } else {
            Log.w(TAG, "Context is not an instance of MainActivity or SplashActivity.");
        }
    }

    /**
     * Updates the UI in MainActivity based on the network state.
     *
     * @param mainActivity       the MainActivity instance
     * @param isNetworkAvailable indicates whether the network is available
     * @param isSpeedGood        indicates whether the network speed is good
     * @param speed              the current network speed in Kbps
     */
    private void updateMainActivityUI(MainActivity mainActivity, boolean isNetworkAvailable, boolean isSpeedGood, int speed) {
        if (!isNetworkAvailable && !isSpeedGood) {
            mainActivity.showNoInternetLayout();
        } else {
            mainActivity.hideNoInternetLayout();
            if (speed < MINIMUM_SPEED_KBPS && speed > 5) {
                mainActivity.showSlowInternetLayout(speed);
            } else {
                mainActivity.hideSlowInternetLayout();
            }
        }
    }

    /**
     * Updates the UI in SplashActivity based on the network state.
     *
     * @param splashActivity     the SplashActivity instance
     * @param isNetworkAvailable indicates whether the network is available
     * @param isSpeedGood        indicates whether the network speed is good
     * @param speed              the current network speed in Kbps
     */
    private void updateSplashActivityUI(SplashActivity splashActivity, boolean isNetworkAvailable, boolean isSpeedGood, int speed) {
        if (!isNetworkAvailable && !isSpeedGood) {
            splashActivity.showNoInternetLayout();
        } else {
            splashActivity.hideNoInternetLayout();
            if (speed < MINIMUM_SPEED_KBPS && speed > 5) {
                splashActivity.showSlowInternetLayout(speed);
            } else {
                splashActivity.hideSlowInternetLayout();
            }
        }
    }
}

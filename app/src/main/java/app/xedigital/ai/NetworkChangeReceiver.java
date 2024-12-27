package app.xedigital.ai;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import app.xedigital.ai.activity.SplashActivity;
import app.xedigital.ai.utills.NetworkUtils;

public class NetworkChangeReceiver extends BroadcastReceiver {


    private static final int SLOW_NETWORK_THRESHOLD_KBPS = 10;
    private static final int MINIMUM_SPEED_KBPS = 10;

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean isNetworkAvailable = NetworkUtils.isNetworkAvailable(context);
        boolean isSpeedGood = isNetworkAvailable && NetworkUtils.getNetworkSpeed(context).downSpeed >= SLOW_NETWORK_THRESHOLD_KBPS;

        handleNetworkStateChange(context, isNetworkAvailable, isSpeedGood);


        if (isNetworkAvailable && NetworkUtils.getNetworkSpeed(context).downSpeed < MINIMUM_SPEED_KBPS) {

            if (context instanceof MainActivity) {
                MainActivity mainActivity = (MainActivity) context;
                mainActivity.showSlowInternetLayout();
            } else if (context instanceof SplashActivity) {
                SplashActivity splashActivity = (SplashActivity) context;
                splashActivity.showSlowInternetLayout();
            }
        } else if (isNetworkAvailable) {
            if (context instanceof MainActivity) {
                MainActivity mainActivity = (MainActivity) context;
                mainActivity.hideSlowInternetLayout();
            } else if (context instanceof SplashActivity) {
                SplashActivity splashActivity = (SplashActivity) context;
                splashActivity.hideSlowInternetLayout();
            }
        }

    }


    private void handleNetworkStateChange(Context context, boolean isNetworkAvailable, boolean isSpeedGood) {
        if (context instanceof SplashActivity) {
            SplashActivity splashActivity = (SplashActivity) context;
            if (!isNetworkAvailable) {
                splashActivity.showNoInternetLayout();
            } else {
                splashActivity.hideNoInternetLayout();
                if (!isSpeedGood) {
                    splashActivity.showSlowInternetLayout();
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
                    mainActivity.showSlowInternetLayout();
                } else {
                    mainActivity.hideSlowInternetLayout();
                }
            }
        }
    }
}

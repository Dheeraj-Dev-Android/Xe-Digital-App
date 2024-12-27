package app.xedigital.ai;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import app.xedigital.ai.activity.SplashActivity;
import app.xedigital.ai.utills.NetworkUtils;

public class NetworkChangeReceiver1 extends BroadcastReceiver {

    private static final int SLOW_NETWORK_THRESHOLD_KBPS = 10;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            handleNetworkStateChange(context, false, false);
        } else {
            boolean isSpeedGood = Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                    NetworkUtils.getNetworkSpeed(context).downSpeed >= SLOW_NETWORK_THRESHOLD_KBPS;
            handleNetworkStateChange(context, true, isSpeedGood);
        }
    }

    private void handleNetworkStateChange(Context context, boolean isNetworkAvailable, boolean isSpeedGood) {
        if (context instanceof SplashActivity) {
            SplashActivity splashActivity = (SplashActivity) context;
            if (!isNetworkAvailable) {
                splashActivity.showNoInternetAlert();
            } else {
                splashActivity.hideNoInternetAlert();
                if (!isSpeedGood) {
                    splashActivity.showSlowNetworkAlert();
                } else {
                    splashActivity.hideSlowNetworkAlert();
                }
            }
        } else if (context instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) context;
            if (!isNetworkAvailable) {
                mainActivity.showNoInternetAlert();
            } else {
                mainActivity.hideNoInternetAlert();
                if (!isSpeedGood) {
                    mainActivity.showSlowNetworkAlert();
                } else {
                    mainActivity.hideSlowNetworkAlert();
                }
            }
        }
    }
}

//    private void showNoInternetSnackbar(Context context) {
//        Snackbar snackbar = Snackbar.make(((MainActivity) context).findViewById(android.R.id.content),
//                "No Internet Connection. Please check your network.", Snackbar.LENGTH_INDEFINITE);
//        snackbar.setAction("OK", view -> ((MainActivity) context).finish());
//        snackbar.show();
//    }
//
//    private void showNoInternetAlert(Context context) {
//        Activity activity = (Activity) context;
//
//        // Check if the Activity is not null before proceeding
//        if (activity != null && !activity.isFinishing()) {
//            new AlertDialog.Builder(activity)
//                    .setTitle("No Internet Connection")
//                    .setMessage("Please check your internet connection and try again.")
//                    .setPositiveButton("OK", (dialog, which) -> activity.finish())
//                    .setCancelable(false)
//                    .show();
//        }
//    }

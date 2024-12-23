package app.xedigital.ai;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import app.xedigital.ai.utills.NetworkUtils;

public class NetworkChangeReceiver extends BroadcastReceiver {
    //    @Override
//    public void onReceive(Context context, Intent intent) {
//        if (!NetworkUtils.isNetworkAvailable(context)) {
////            showNoInternetSnackbar(context);
//            showNoInternetAlert(context);
//        }
//    }

    private static final int SLOW_NETWORK_THRESHOLD_KBPS = 10;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            ((MainActivity) context).showNoInternetAlert();
        } else {
            ((MainActivity) context).hideNoInternetAlert(); // Hide the alert if internet is restored

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                NetworkUtils.NetworkSpeed speed = NetworkUtils.getNetworkSpeed(context);
                if (speed.downSpeed < SLOW_NETWORK_THRESHOLD_KBPS) {
                    ((MainActivity) context).showSlowNetworkAlert();
                } else {
                    ((MainActivity) context).hideSlowNetworkAlert();
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
}

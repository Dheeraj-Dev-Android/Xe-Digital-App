package app.xedigital.ai.utills;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

public class NetworkUtils {

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return false;
        }

        Network activeNetwork = connectivityManager.getActiveNetwork();
        if (activeNetwork == null) {
            return false;
        }

        NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork);
        if (networkCapabilities == null) {
            return false;
        }

        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static NetworkSpeed getNetworkSpeed(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());

        if (networkCapabilities != null) {
            int downSpeed = networkCapabilities.getLinkDownstreamBandwidthKbps();
            int upSpeed = networkCapabilities.getLinkUpstreamBandwidthKbps();
            Log.d("NetworkSpeed", "Down Speed: " + downSpeed + " Mbps, Up Speed: " + upSpeed + " Mbps");
            return new NetworkSpeed(downSpeed, upSpeed);

        } else {
            Log.d("NetworkSpeed", "Network capabilities are null");
            return new NetworkSpeed(0, 0);

        }
    }

    public static class NetworkSpeed {
        public int downSpeed;
        public int upSpeed;

        public NetworkSpeed(int downSpeed, int upSpeed) {
            this.downSpeed = downSpeed;
            this.upSpeed = upSpeed;
        }
    }
}
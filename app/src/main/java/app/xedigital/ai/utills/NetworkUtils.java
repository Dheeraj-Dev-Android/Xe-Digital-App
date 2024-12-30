package app.xedigital.ai.utills;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

public class NetworkUtils {

    private static final String TAG = "NetworkUtils";

    /**
     * Checks if network is available and has internet connectivity.
     *
     * @param context the application context.
     * @return true if network is available, false otherwise.
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            Log.e(TAG, "ConnectivityManager is null.");
            return false;
        }

        Network activeNetwork = connectivityManager.getActiveNetwork();
        if (activeNetwork == null) {
            Log.e(TAG, "No active network.");
            return false;
        }

        NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork);
        if (networkCapabilities == null) {
            Log.e(TAG, "Network capabilities are null.");
            return false;
        }

        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
    }

    /**
     * Returns the network speed (downstream and upstream) in Kbps.
     *
     * @param context the application context.
     * @return a NetworkSpeed object containing downstream and upstream speeds in Kbps.
     */
    public static NetworkSpeed getNetworkSpeed(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            Log.e(TAG, "ConnectivityManager is null.");
            return new NetworkSpeed(0, 0);
        }

        NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
        if (networkCapabilities != null) {
            int downSpeedKbps = networkCapabilities.getLinkDownstreamBandwidthKbps();
            int upSpeedKbps = networkCapabilities.getLinkUpstreamBandwidthKbps();
            Log.d(TAG, "Down Speed: " + downSpeedKbps + " Kbps, Up Speed: " + upSpeedKbps + " Kbps");
            return new NetworkSpeed(downSpeedKbps, upSpeedKbps);
        } else {
            Log.d(TAG, "Network capabilities are null");
            return new NetworkSpeed(0, 0);
        }
    }

    /**
     * Checks if the current network speed is good.
     * A network is considered good if its download speed is greater than the defined threshold.
     *
     * @param context the application context.
     * @return true if the network speed is good, false otherwise.
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static boolean isNetworkSpeedGood(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            Log.e(TAG, "ConnectivityManager is null.");
            return false;
        }

        NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
        if (networkCapabilities != null) {
            int downSpeedKbps = networkCapabilities.getLinkDownstreamBandwidthKbps();
            // Define your threshold for good network speed (in Kbps)
            int goodSpeedThresholdKbps = 1000; // Example: 1 Mbps = 1000 Kbps

            boolean isGoodSpeed = downSpeedKbps >= goodSpeedThresholdKbps;
            if (!isGoodSpeed) {
                Log.d(TAG, "Network speed too slow: " + downSpeedKbps + " Kbps");
            }
            return isGoodSpeed;
        } else {
            Log.e(TAG, "Network capabilities are null.");
            return false; // Assume slow network if capabilities are null
        }
    }

    /**
     * Converts the speed from Kbps to Mbps.
     *
     * @param kbps the speed in Kbps.
     * @return the speed in Mbps.
     */
    private static double convertKbpsToMbps(int kbps) {
        return kbps / 1024.0;  // Convert Kbps to Mbps
    }

    /**
     * NetworkSpeed class to represent download and upload speeds.
     */
    public static class NetworkSpeed {
        public int downSpeedKbps;  // Download speed in Kbps
        public int upSpeedKbps;    // Upload speed in Kbps

        public NetworkSpeed(int downSpeedKbps, int upSpeedKbps) {
            this.downSpeedKbps = downSpeedKbps;
            this.upSpeedKbps = upSpeedKbps;
        }

        /**
         * Convert the speeds to Mbps for easier display.
         */
        public String getDownSpeedInMbps() {
            return String.format("%.2f Mbps", convertKbpsToMbps(downSpeedKbps));
        }

        public String getUpSpeedInMbps() {
            return String.format("%.2f Mbps", convertKbpsToMbps(upSpeedKbps));
        }
    }
}

package app.xedigital.ai.utills;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

public class NetworkUtils {

    private static final String TAG = "NetworkUtils";

    /**
     * Checks if network is available and has internet connectivity.
     *
     * @param context the application context.
     * @return true if network is available and validated, false otherwise.
     */
    public static boolean isNetworkAvailable(@NonNull Context context) {
        ConnectivityManager connectivityManager = ContextCompat.getSystemService(context, ConnectivityManager.class);
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

        // Simplified network validation using hasTransport
        return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET);
    }

    /**
     * Returns the network speed (downstream and upstream) in Kbps.
     *
     * @param context the application context.
     * @return a NetworkSpeed object containing downstream and upstream speeds in Kbps.
     */
    public static NetworkSpeed getNetworkSpeed(@NonNull Context context) {
        ConnectivityManager connectivityManager = ContextCompat.getSystemService(context, ConnectivityManager.class);
        if (connectivityManager == null) {
            Log.e(TAG, "ConnectivityManager is null.");
            return new NetworkSpeed(0, 0);
        }

        Network activeNetwork = connectivityManager.getActiveNetwork();
        if (activeNetwork == null) {
            Log.e(TAG, "No active network.");
            return new NetworkSpeed(0, 0);
        }

        NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork);
        if (networkCapabilities != null) {
            int downSpeedKbps = networkCapabilities.getLinkDownstreamBandwidthKbps();
            int upSpeedKbps = networkCapabilities.getLinkUpstreamBandwidthKbps();

            Log.d(TAG, "Down Speed: " + downSpeedKbps + " Kbps, Up Speed: " + upSpeedKbps + " Kbps");
            return new NetworkSpeed(downSpeedKbps, upSpeedKbps);
        } else {
            Log.e(TAG, "Network capabilities are null.");
            return new NetworkSpeed(0, 0);
        }
    }

    /**
     * Checks if the current network speed meets the threshold for being considered "good."
     *
     * @param context       the application context.
     * @param thresholdKbps the minimum speed threshold in Kbps (default: 80 Kbps).
     * @return true if the network speed is good, false otherwise.
     */
    public static boolean isNetworkSpeedGood(Context context, int thresholdKbps) {
        NetworkSpeed networkSpeed = getNetworkSpeed(context);

        boolean isGoodSpeed = networkSpeed.downSpeedKbps >= thresholdKbps;
        if (!isGoodSpeed) {
            Log.w(TAG, "Network speed is below threshold: " + networkSpeed.downSpeedKbps + " Kbps (Threshold: " + thresholdKbps + " Kbps)");
        }
        return isGoodSpeed;
    }

    /**
     * Converts the speed from Kbps to Mbps.
     *
     * @param kbps the speed in Kbps.
     * @return the speed in Mbps.
     */
    private static double convertKbpsToMbps(int kbps) {
        return kbps / 1024.0;
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
         * Converts the download speed to Mbps for easier display.
         *
         * @return download speed in Mbps as a formatted string.
         */
        public String getDownSpeedInMbps() {
            return String.format("%.2f Mbps", convertKbpsToMbps(downSpeedKbps));
        }

        /**
         * Converts the upload speed to Mbps for easier display.
         *
         * @return upload speed in Mbps as a formatted string.
         */
        public String getUpSpeedInMbps() {
            return String.format("%.2f Mbps", convertKbpsToMbps(upSpeedKbps));
        }
    }
}

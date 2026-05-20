package app.xedigital.ai;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import app.xedigital.ai.utills.ShiftCheckHelper;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || !Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            return;
        }

        Log.d(TAG, "Hardware Boot Broadcast Received. Activating Shift Tracking Initialization Blueprint...");

        SharedPreferences prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String authToken = prefs.getString("authToken", null);

        if (authToken != null) {
            ShiftCheckHelper.startShiftTracking(context);
            Log.d(TAG, "Unique background tasks registry parsed and initialized from Boot.");
        } else {
            Log.w(TAG, "No valid authorization credentials detected during boot configuration. Aborting initialization.");
        }
    }
}
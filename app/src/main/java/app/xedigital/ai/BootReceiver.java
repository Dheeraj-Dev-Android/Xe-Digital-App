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

        Log.d(TAG, "Device Boot Completed. Initializing Shift Tracking Safety Net...");

        SharedPreferences prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String authToken = prefs.getString("authToken", null);

        // DO NOT start Foreground Service directly here.
        // Instead, schedule the WorkManager task.
        if (authToken != null) {
            ShiftCheckHelper.startShiftTracking(context);
            Log.d(TAG, "WorkManager scheduled successfully from Boot.");
        }
    }
}
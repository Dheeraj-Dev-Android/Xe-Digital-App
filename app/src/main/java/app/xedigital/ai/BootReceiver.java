package app.xedigital.ai;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

import app.xedigital.ai.utills.ShiftTrackingWorker;

public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || !Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            return;
        }

        Log.d(TAG, "Boot completed received. Checking if shift tracking should restart...");

        // Only restart tracking if user was previously logged in / punched in
        SharedPreferences prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String userId = prefs.getString("userId", null);
        String authToken = prefs.getString("authToken", null);

        if (userId == null || authToken == null) {
            Log.d(TAG, "No credentials found. Skipping worker restart.");
            return;
        }

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest trackingRequest =
                new PeriodicWorkRequest.Builder(ShiftTrackingWorker.class, 15, TimeUnit.MINUTES)
                        .setConstraints(constraints)
                        .addTag("SHIFT_WORK_TAG")
                        .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "EmployeeTracking",
                ExistingPeriodicWorkPolicy.KEEP,
                trackingRequest
        );

        Log.d(TAG, "Shift tracking worker restarted successfully after boot.");
    }
}
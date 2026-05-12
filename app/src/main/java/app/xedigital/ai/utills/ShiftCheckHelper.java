package app.xedigital.ai.utills;

import android.content.Context;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class ShiftCheckHelper {
    public static final String WORK_NAME = "EmployeeTracking";

    public static void startShiftTracking(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        // Android enforces a minimum of 15 minutes for PeriodicWork.
        PeriodicWorkRequest trackingRequest =
                new PeriodicWorkRequest.Builder(ShiftTrackingWorker.class, 15, TimeUnit.MINUTES)
                        .setConstraints(constraints)
                        .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                trackingRequest
        );
    }
}
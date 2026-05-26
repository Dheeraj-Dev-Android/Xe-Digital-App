//package app.xedigital.ai.utills;
//
//import android.content.Context;
//import android.util.Log;
//
//import androidx.work.BackoffPolicy;
//import androidx.work.Constraints;
//import androidx.work.ExistingPeriodicWorkPolicy;
//import androidx.work.NetworkType;
//import androidx.work.PeriodicWorkRequest;
//import androidx.work.WorkManager;
//
//import java.util.concurrent.TimeUnit;
//
//public class ShiftCheckHelper {
//    public static final String WORK_NAME = "EmployeeTracking";
//
//    public static void startShiftTracking(Context context) {
//        Constraints constraints = new Constraints.Builder()
//                .setRequiredNetworkType(NetworkType.CONNECTED)
//                .build();
//
//        // Establishes a 15-minute recurring job interval
//        PeriodicWorkRequest trackingRequest =
//                new PeriodicWorkRequest.Builder(ShiftTrackingWorker.class, 15, TimeUnit.MINUTES)
//                        .setConstraints(constraints)
//                        .setBackoffCriteria(
//                                BackoffPolicy.EXPONENTIAL,
//                                PeriodicWorkRequest.MIN_BACKOFF_MILLIS, // Initial backoff starts at 10 seconds if Result.retry() is called
//                                TimeUnit.MILLISECONDS)
//                        .build();
//
//        // CRITICAL REPAIR: Policy.KEEP checks if the workspace ID already exists.
//        // If it exists, it leaves it alone, keeping its schedule intact and stopping unexpected immediate runs.
////        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
////                WORK_NAME,
////                ExistingPeriodicWorkPolicy.KEEP,
////                trackingRequest
////        );
//        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
//                WORK_NAME,
//                ExistingPeriodicWorkPolicy.UPDATE, // Safely modifies parameters without losing track of execution
//                trackingRequest
//        );
//
//        Log.d("ShiftCheckHelper", "WorkManager schedule configuration securely established/verified.");
//    }
//}
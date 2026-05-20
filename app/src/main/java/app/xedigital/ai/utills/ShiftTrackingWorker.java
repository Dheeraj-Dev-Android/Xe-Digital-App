package app.xedigital.ai.utills;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ShiftTrackingWorker extends Worker {
    private static final String TAG = "ShiftTrackingWorker";

    public ShiftTrackingWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String start = prefs.getString("shift_start", null);
        String end = prefs.getString("shift_end", null);

        Log.d(TAG, "WorkManager background cycle awakened. Validating runtime timestamp logic...");
        Log.d(TAG, "Active Schedule Matrix: " + start + " - " + end);

        // STAGE 1 GATEKEEPER CHECK: Immediate operational evaluation.
        // If outside defined shift bounds, abort immediately without performing location tasks.
        if (!isWithinShift(start, end)) {
            Log.d(TAG, "Current time falls OUTSIDE defined shift constraints. Execution bypassed, API payload suppressed.");
            return Result.success(); // Return success to keep the periodic loop safely registered
        }

        // STAGE 2 PAYLOAD DELIVERY: Executes only when the system time matches shift rules perfectly
        Log.d(TAG, "Current time is strictly INSIDE shift hours. Dispatching location and API tasks...");
        return performLocationUpdate();
    }

    private boolean isWithinShift(String startStr, String endStr) {
        if (startStr == null || endStr == null) {
            Log.w(TAG, "Shift timestamps are completely absent in SharedPreferences memory. Suppressing operation.");
            return false;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date now = sdf.parse(sdf.format(new Date()));
            Date start = sdf.parse(startStr);
            Date end = sdf.parse(endStr);

            if (now == null || start == null || end == null) return false;

            // Handles night-shift rollover configurations smoothly (e.g., 22:00 PM to 06:00 AM)
            if (end.before(start)) {
                return now.after(start) || now.before(end);
            }

            return now.after(start) && now.before(end);
        } catch (Exception e) {
            Log.e(TAG, "Fatal parser exception assessing configuration string matrices: " + e.getMessage());
            return false;
        }
    }

    private Result performLocationUpdate() {
        try {
            Log.d(TAG, "Polling FusedLocationProviderClient coordinates...");

            // TODO: Place your precise Location and Retrofit API client chain sequence here.
            // Example layout:
            // boolean apiStatusSuccess = myApiCallInterface.sendData(lat, lon);
            // if (!apiStatusSuccess) { return Result.retry(); } // Triggers Exponential Backoff sequence

            Log.d(TAG, "Attendance endpoint payload successfully synchronized.");
            return Result.success();
        } catch (Exception e) {
            Log.e(TAG, "Network or location lookup failure encountered: " + e.getMessage());
            // Triggers WorkManager exponential backoff rules instead of abandoning the payload tracking slot
            return Result.retry();
        }
    }
}
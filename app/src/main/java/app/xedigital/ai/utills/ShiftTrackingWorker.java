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

        // Logic Change: If outside shift, return success but do nothing.
        // This keeps the periodic timer alive for the next check.
        if (!isWithinShift(start, end)) {
            Log.d(TAG, "Outside shift hours. Worker idling...");
            return Result.success();
        }

        Log.d(TAG, "Inside shift hours. Fetching location...");
        // Call your existing location fetching logic here
        return performLocationUpdate();
    }

    private boolean isWithinShift(String startStr, String endStr) {
        if (startStr == null || endStr == null) return true;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date now = sdf.parse(sdf.format(new Date()));
            Date start = sdf.parse(startStr);
            Date end = sdf.parse(endStr);
            if (end.before(start)) return now.after(start) || now.before(end);
            return now.after(start) && now.before(end);
        } catch (Exception e) {
            return true;
        }
    }

    private Result performLocationUpdate() {
        // Implement your FusedLocationProvider and Retrofit logic here as per previous versions
        return Result.success();
    }
}
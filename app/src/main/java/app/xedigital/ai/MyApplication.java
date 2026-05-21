package app.xedigital.ai;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Configuration;

import app.xedigital.ai.utills.ShiftCheckHelper;

public class MyApplication extends Application implements Configuration.Provider {

    private static final String TAG = "MyApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Application runtime environment online. Custom configuration structure compiled.");

        // FIX: This guarantees that WorkManager registers your worker on fresh install/app launch
        try {
            ShiftCheckHelper.startShiftTracking(this);
            Log.d(TAG, "WorkManager Shift tracking blueprint successfully initialized on app launch.");
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize WorkManager tracking on launch: " + e.getMessage());
        }
    }

    @NonNull
    @Override
    public Configuration getWorkManagerConfiguration() {
        return new Configuration.Builder()
                .setMinimumLoggingLevel(Log.DEBUG)
                .build();
    }
}
package app.xedigital.ai;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Configuration;

public class MyApplication extends Application implements Configuration.Provider {

    private static final String TAG = "MyApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Application runtime environment online. Custom configuration structure compiled.");
    }

    @NonNull
    @Override
    public Configuration getWorkManagerConfiguration() {
        // Enforces customized initialization parameter scopes and expands framework error logging details
        return new Configuration.Builder()
                .setMinimumLoggingLevel(Log.DEBUG)
                .build();
    }
}
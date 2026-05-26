//package app.xedigital.ai.utills;
//
//import android.content.Context;
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.os.Build;
//import android.util.Log;
//
//import androidx.annotation.NonNull;
//import androidx.work.Worker;
//import androidx.work.WorkerParameters;
//
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.Locale;
//
//import app.xedigital.ai.api.APIClient;
//import app.xedigital.ai.api.APIInterface;
//import app.xedigital.ai.model.profile.UserProfileResponse;
//import retrofit2.Response;
//
//public class ShiftTrackingWorker extends Worker {
//    private static final String TAG = "ShiftTrackingWorker";
//
//    public ShiftTrackingWorker(@NonNull Context context, @NonNull WorkerParameters params) {
//        super(context, params);
//    }
//
//    @NonNull
//    @Override
//    public Result doWork() {
//        Context context = getApplicationContext();
//        if (context == null) {
//            return Result.failure();
//        }
//
//        SharedPreferences prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
//        if (prefs == null) {
//            return Result.failure();
//        }
//
//        String userId = prefs.getString("userId", null);
//        String authToken = prefs.getString("authToken", null);
//
//        // 1. DYNAMIC SYNC PHASE: Fetch real-time shift modifications from your API
//        if (userId != null && authToken != null) {
//            try {
//                APIInterface apiInterface = APIClient.getInstance().getApi();
//                if (apiInterface != null) {
//                    String authHeaderValue = "jwt " + authToken;
//
//                    // Execute synchronously since WorkManager already isolates this execution inside a worker thread
//                    Response<UserProfileResponse> response = apiInterface.getUserProfile(userId, authHeaderValue).execute();
//
//                    if (response.isSuccessful() && response.body() != null) {
//                        UserProfileResponse profile = response.body();
//
//                        // Safety multi-layered structural null check on API response schema
//                        if (profile.getData() != null) {
//                            String freshStart = profile.getData().getEmployee().getShift().getStartTime(); // e.g. "09:00"
//                            String freshEnd = profile.getData().getEmployee().getShift().getEndTime();     // e.g. "17:30"
//
//                            if (freshStart != null && freshEnd != null) {
//                                prefs.edit()
//                                        .putString("shift_start", freshStart)
//                                        .putString("shift_end", freshEnd)
//                                        .apply();
//                                Log.d(TAG, "Shift hours dynamically refreshed from API: " + freshStart + " - " + freshEnd);
//                            }
//                        }
//                    } else {
//                        Log.w(TAG, "Profile API dynamic check returned an error code: " + response.code());
//                    }
//                }
//            } catch (Exception e) {
//                // Fail-safe tracking preservation: if network fails or times out, proceed seamlessly using existing cached bounds
//                Log.e(TAG, "Network connection unavailable for dynamic shift checking. Defaulting to local cache. Error: " + e.getMessage());
//            }
//        }
//
//        // 2. EXTRACTION PHASE: Fetch finalized parameters from SharedPreferences
//        String start = prefs.getString("shift_start", null);
//        String end = prefs.getString("shift_end", null);
//
//        Log.d(TAG, "Evaluating shift metrics constraints: " + start + " - " + end);
//
//        Intent serviceIntent = new Intent(context, LocationService.class);
//
//        // 3. BOUNDARY VALVE MANAGEMENT: Assert timeline compliance
//        if (!isWithinShift(start, end)) {
//            Log.d(TAG, "Clock is OUTSIDE allowed parameters. Demolishing active location engine instance.");
//            try {
//                context.stopService(serviceIntent);
//            } catch (Exception e) {
//                Log.e(TAG, "Error stopping service: " + e.getMessage());
//            }
//            return Result.success(); // Bypasses operation execution safely while staying scheduled
//        }
//
//        // Locate this section in ShiftTrackingWorker.java inside doWork():
//        Log.d(TAG, "Clock is INSIDE parameters. Re-verifying active status of Location Engine.");
//        try {
//            // Double check runtime location permissions before trying to invoke FGS from background
//            boolean hasCoarse = androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED;
//            boolean hasFine = androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED;
//
//            if (!hasCoarse && !hasFine) {
//                Log.e(TAG, "Aborting FGS Start: User has not given location permissions to this application.");
//                return Result.failure();
//            }
//
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                context.startForegroundService(serviceIntent);
//            } else {
//                context.startService(serviceIntent);
//            }
//        } catch (Exception e) {
//            // This catches ForegroundServiceStartNotAllowedException on Android 14 gracefully
//            Log.e(TAG, "Android OS blocked background FGS launch. Retrying on next window or when app opens: " + e.getMessage());
//            return Result.retry(); // Tells WorkManager to try again later when execution states change
//        }
//
//        return Result.success();
//    }
//
//    private boolean isWithinShift(String startStr, String endStr) {
//        if (startStr == null || endStr == null || startStr.isEmpty() || endStr.isEmpty()) {
//            return false;
//        }
//        try {
//            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
//            String currentTimeStr = sdf.format(new Date());
//
//            Date now = sdf.parse(currentTimeStr);
//            Date start = sdf.parse(startStr);
//            Date end = sdf.parse(endStr);
//
//            if (now == null || start == null || end == null) return false;
//
//            // Accommodates overnight tracking windows seamlessly (e.g., 9 PM to 5 AM scheduling)
//            if (end.before(start)) {
//                return now.after(start) || now.before(end);
//            }
//
//            return now.after(start) && now.before(end);
//        } catch (Exception e) {
//            Log.e(TAG, "Parser failed to assess timestamp compliance variables formatting constraints: " + e.getMessage());
//            return false;
//        }
//    }
//}
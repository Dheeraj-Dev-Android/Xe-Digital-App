package app.xedigital.ai.utills;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.List;
import java.util.concurrent.TimeUnit;

import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.api.APIInterface;
import app.xedigital.ai.model.attendance.EmployeeAttendanceResponse;
import app.xedigital.ai.model.attendance.EmployeePunchDataItem;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShiftCheckHelper {

    public static final String WORK_NAME = "EmployeeTracking";
    private static final String TAG = "ShiftCheckHelper";

    public static void checkAndStartTracking(Context context) {
        // 1. Check Permissions first
        if (!hasBackgroundLocationPermission(context)) {
            Log.w(TAG, "Background location permission missing. Cannot start tracking.");
            navigateToLocationSettings(context);
            return;
        }

        SharedPreferences prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String authToken = prefs.getString("authToken", null);

        if (authToken == null) return;

        String authHeader = authToken.startsWith("jwt ") ? authToken : "jwt " + authToken;
        APIInterface service = APIClient.getInstance().getAttendance();

        service.getAttendance(authHeader, null, null, null, null, null, null, null, null)
                .enqueue(new Callback<EmployeeAttendanceResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<EmployeeAttendanceResponse> call, @NonNull Response<EmployeeAttendanceResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            EmployeeAttendanceResponse attendanceResp = response.body();
                            if (attendanceResp.isSuccess() && attendanceResp.getData() != null) {
                                List<EmployeePunchDataItem> punchList = attendanceResp.getData().getEmployeePunchData();
                                if (punchList != null && !punchList.isEmpty()) {
                                    String punchInTime = punchList.get(0).getPunchIn();
                                    if (isValidPunch(punchInTime)) {
                                        startShiftTracking(context);
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<EmployeeAttendanceResponse> call, @NonNull Throwable t) {
                        Log.e(TAG, "API failure: " + t.getMessage());
                    }
                });
    }

    /**
     * Checks if the app has 'Allow all the time' location permission.
     */
    public static boolean hasBackgroundLocationPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return ContextCompat.checkSelfPermission(context,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED;
        }
        // For Android 9 and below, Fine location is sufficient
        return ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Navigates the user directly to the App Info settings page.
     * The user must then click 'Permissions' -> 'Location' -> 'Allow all the time'.
     */
    public static void navigateToLocationSettings(Context context) {
        Toast.makeText(context, "Please set Location to 'Allow all the time’ to ensure smooth and accurate attendance tracking without any errors.", Toast.LENGTH_LONG).show();

        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", context.getPackageName(), null);
        intent.setData(uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private static boolean isValidPunch(String punchTime) {
        return punchTime != null && !punchTime.trim().isEmpty() && !punchTime.equalsIgnoreCase("null");
    }

    private static void startShiftTracking(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest trackingRequest =
                new PeriodicWorkRequest.Builder(ShiftTrackingWorker.class, 5, TimeUnit.MINUTES)
                        .setConstraints(constraints)
                        .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                trackingRequest
        );
    }
}
package app.xedigital.ai.utills;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.Tasks;

import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.api.APIInterface;
import app.xedigital.ai.model.profile.UserProfileResponse;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Response;

public class ShiftTrackingWorker extends Worker {

    private static final String TAG = "ShiftTrackingWorker";

    // Required constructor — must be public and exact signature for WorkManager reflection
    public ShiftTrackingWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        SharedPreferences prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

        // ── Step 1: Credentials Check ─────────────────────────────────────────
        String userId = prefs.getString("userId", null);
        String authToken = prefs.getString("authToken", null);

        if (userId == null || authToken == null) {
            Log.e(TAG, "No credentials found in SharedPreferences. Stopping worker.");
            // Return failure to stop retrying — no point without credentials
            return Result.failure();
        }

        String authHeader = "jwt " + authToken;

        try {
            // ── Step 2: Shift Timing Check ────────────────────────────────────
            APIInterface apiInterface = APIClient.getInstance().getLogin();
            Response<UserProfileResponse> profileResponse =
                    apiInterface.getUserProfile(userId, authHeader).execute();

            if (profileResponse.isSuccessful() && profileResponse.body() != null) {
                UserProfileResponse res = profileResponse.body();

                // Safe null-traversal through nested response
                if (res.getData() != null
                        && res.getData().getEmployee() != null
                        && res.getData().getEmployee().getShift() != null) {

                    String startTime = res.getData().getEmployee().getShift().getStartTime();
                    String endTime = res.getData().getEmployee().getShift().getEndTime();

                    if (!isWithinShift(startTime, endTime)) {
                        Log.d(TAG, "Shift window closed (" + endTime + "). Cancelling worker.");
                        WorkManager.getInstance(context).cancelUniqueWork("EmployeeTracking");
                        return Result.success();
                    }
                }
            } else {
                Log.w(TAG, "Profile API failed or returned empty body. Code: " + profileResponse.code());
                // Don't stop — retry next cycle in case it's a transient network issue
            }

            // ── Step 3: Location Permission Check ────────────────────────────
            boolean hasFineLocation = ActivityCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED;

            boolean hasBackgroundLocation = true; // Default true for Android < Q
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                hasBackgroundLocation = ActivityCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                        == PackageManager.PERMISSION_GRANTED;
            }

            if (!hasFineLocation || !hasBackgroundLocation) {
                Log.e(TAG, "Location permissions missing. Fine=" + hasFineLocation
                        + " Background=" + hasBackgroundLocation);
                // Retry — user may grant permission later
                return Result.retry();
            }

            // ── Step 4: Fetch Location (30s timeout) ─────────────────────────
            // Timeout prevents the worker from hanging and draining battery
            // when GPS is weak (e.g. indoors)
            Location location = null;
            try {
                location = Tasks.await(
                        LocationServices.getFusedLocationProviderClient(context)
                                .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null),
                        30, TimeUnit.SECONDS
                );
            } catch (Exception timeoutException) {
                Log.e(TAG, "Location fetch timed out or failed: " + timeoutException.getMessage());
            }

            if (location != null) {
                // ── Step 5: Send Attendance ───────────────────────────────────
                return sendAttendance(context, authHeader, userId, location);
            } else {
                Log.w(TAG, "Location is null after timeout. Will retry next cycle.");
                return Result.retry();
            }

        } catch (Exception e) {
            Log.e(TAG, "Critical worker error: " + e.getMessage(), e);
            return Result.retry();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Sends attendance punch to the server
    // ─────────────────────────────────────────────────────────────────────────
    private Result sendAttendance(Context context, String authHeader, String userId, Location loc)
            throws Exception {

        String preciseAddress = getAddressFromLocation(context, loc.getLatitude(), loc.getLongitude());

        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        fmt.setTimeZone(TimeZone.getTimeZone("UTC"));
        String currentTime = fmt.format(new Date(loc.getTime()));

        JSONObject body = new JSONObject();
        body.put("employee", userId);
        body.put("address", preciseAddress);
        body.put("punchTime", currentTime);
        // Raw coordinates as fallback for server-side verification
        body.put("latitude", loc.getLatitude());
        body.put("longitude", loc.getLongitude());

        Log.d(TAG, "Sending attendance: " + currentTime + " | " + preciseAddress);

        RequestBody requestBody = RequestBody.create(
                MediaType.parse("application/json"), body.toString());

        APIInterface service = APIClient.getInstance().getAttendance();
        Response<ResponseBody> response = service.AttendanceApi(authHeader, requestBody).execute();

        if (response.isSuccessful()) {
            Log.d(TAG, "Attendance sent successfully.");
            return Result.success();
        } else {
            Log.e(TAG, "Attendance API failed. Code: " + response.code());
            return Result.retry();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Reverse geocode coordinates to a human-readable address
    // ─────────────────────────────────────────────────────────────────────────
    private String getAddressFromLocation(Context context, double lat, double lon) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);
            if (addresses != null && !addresses.isEmpty()) {
                return addresses.get(0).getAddressLine(0);
            }
        } catch (IOException e) {
            Log.e(TAG, "Geocoder failed: " + e.getMessage());
        }
        // Fallback to raw coordinates if geocoding fails
        return "Lat: " + lat + ", Lon: " + lon;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Checks if current time is within the employee's shift window
    // Handles overnight shifts (e.g. 22:00 – 06:00)
    // ─────────────────────────────────────────────────────────────────────────
    private boolean isWithinShift(String startStr, String endStr) {
        if (startStr == null || endStr == null) {
            // If shift data is unavailable, allow tracking to continue
            return true;
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date now = sdf.parse(sdf.format(new Date()));
            Date start = sdf.parse(startStr);
            Date end = sdf.parse(endStr);

            if (now == null || start == null || end == null) return true;

            if (end.before(start)) {
                // Overnight shift: e.g. 22:00 → 06:00
                return now.after(start) || now.before(end);
            }
            return now.after(start) && now.before(end);

        } catch (ParseException e) {
            Log.e(TAG, "Shift time parse error: " + e.getMessage());
            // Default to allowing tracking if parsing fails
            return true;
        }
    }
}
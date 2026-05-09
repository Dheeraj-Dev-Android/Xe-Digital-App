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

    public ShiftTrackingWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    //    @NonNull
//    @Override
//    public Result doWork() {
//        Context context = getApplicationContext();
//        SharedPreferences prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
//
//        String userId = prefs.getString("userId", null);
//        String authToken = prefs.getString("authToken", null);
//
//        if (userId == null || authToken == null) {
//            Log.e(TAG, "Worker failed: Missing credentials.");
//            return Result.failure();
//        }
//
//        String authHeader = "jwt " + authToken;
//
//        try {
//            // 1. Fetch User Profile with Null Safety
//            APIInterface apiInterface = APIClient.getInstance().getLogin();
//            Response<UserProfileResponse> profileResponse = apiInterface.getUserProfile(userId, authHeader).execute();
//
//            if (profileResponse.isSuccessful() && profileResponse.body() != null) {
//                UserProfileResponse res = profileResponse.body();
//
//                // Deep null-check for nested shift data
//                if (res.getData() != null &&
//                        res.getData().getEmployee() != null &&
//                        res.getData().getEmployee().getShift() != null) {
//
//                    String startTime = res.getData().getEmployee().getShift().getStartTime();
//                    String endTime = res.getData().getEmployee().getShift().getEndTime();
//
//                    if (!isWithinShift(startTime, endTime)) {
//                        Log.d(TAG, "Shift ended. Terminating worker.");
//                        WorkManager.getInstance(context).cancelUniqueWork("EmployeeTracking");
//                        return Result.success();
//                    }
//                }
//            }
//
//            // 2. Permission Check
//            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                Log.e(TAG, "Location permission missing.");
//                return Result.failure();
//            }
//
//            // 3. Get Location with a 30-second timeout to prevent hanging in production
//            Location location = Tasks.await(
//                    LocationServices.getFusedLocationProviderClient(context)
//                            .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null),
//                    30, TimeUnit.SECONDS);
//
//            if (location != null) {
//                return sendAttendance(context, authHeader, userId, location);
//            }
//
//        } catch (Exception e) {
//            Log.e(TAG, "Worker execution failed: " + e.getMessage());
//            return Result.retry();
//        }
//
//        return Result.retry();
//    }
    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        SharedPreferences prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

        // 1. Credentials Check
        String userId = prefs.getString("userId", null);
        String authToken = prefs.getString("authToken", null);

        if (userId == null || authToken == null) {
            Log.e(TAG, "Worker failed: No credentials found. Stopping.");
            return Result.failure();
        }

        String authHeader = "jwt " + authToken;

        try {
            // 2. Shift Timing Check (API Call with Null Safety)
            APIInterface apiInterface = APIClient.getInstance().getLogin();
            Response<UserProfileResponse> profileResponse = apiInterface.getUserProfile(userId, authHeader).execute();

            if (profileResponse.isSuccessful() && profileResponse.body() != null) {
                UserProfileResponse res = profileResponse.body();

                // Safe navigation through the nested JSON structure
                if (res.getData() != null &&
                        res.getData().getEmployee() != null &&
                        res.getData().getEmployee().getShift() != null) {

                    String startTime = res.getData().getEmployee().getShift().getStartTime();
                    String endTime = res.getData().getEmployee().getShift().getEndTime();

                    // If shift has ended, stop the worker permanently until next manual punch
                    if (!isWithinShift(startTime, endTime)) {
                        Log.d(TAG, "Shift window closed (" + endTime + "). Terminating worker.");
                        WorkManager.getInstance(context).cancelUniqueWork("EmployeeTracking");
                        return Result.success();
                    }
                }
            }

            // 3. Silent Permission Check (Crucial for Production)
            boolean hasFineLocation = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
            boolean hasBackgroundLocation = true; // Default true for older Android versions

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                hasBackgroundLocation = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED;
            }

            if (!hasFineLocation || !hasBackgroundLocation) {
                Log.e(TAG, "Permissions missing (Fine: " + hasFineLocation + ", Back: " + hasBackgroundLocation + ")");
                // We retry in case the user grants permission later, rather than failing permanently
                return Result.retry();
            }

            // 4. Fetch Location with 30-Second Timeout
            // This prevents the worker from hanging and draining battery if GPS signal is weak (e.g. indoors)
            Location location = null;
            try {
                location = Tasks.await(
                        LocationServices.getFusedLocationProviderClient(context)
                                .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null),
                        30, TimeUnit.SECONDS);
            } catch (Exception timeoutException) {
                Log.e(TAG, "Location fetch timed out after 30s");
            }

            if (location != null) {
                // 5. Send Attendance to Server
                return sendAttendance(context, authHeader, userId, location);
            } else {
                Log.w(TAG, "Location is null after timeout. Retrying next cycle.");
                return Result.retry();
            }

        } catch (Exception e) {
            Log.e(TAG, "Critical Worker Error: " + e.getMessage());
            return Result.retry();
        }
    }

    private Result sendAttendance(Context context, String authHeader, String userId, Location loc) throws Exception {
        String preciseAddress = getAddressFromLocation(context, loc.getLatitude(), loc.getLongitude());

        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        fmt.setTimeZone(TimeZone.getTimeZone("UTC"));
        String currentTime = fmt.format(new Date(loc.getTime()));

        JSONObject body = new JSONObject();
        body.put("employee", userId);
        body.put("address", preciseAddress);
        body.put("punchTime", currentTime);
        // Fallback: Add raw coordinates for server-side verification if address fails
        body.put("latitude", loc.getLatitude());
        body.put("longitude", loc.getLongitude());

        RequestBody requestBody = RequestBody.create(
                MediaType.parse("application/json"), body.toString());

        APIInterface service = APIClient.getInstance().getAttendance();
        Response<ResponseBody> response = service.AttendanceApi(authHeader, requestBody).execute();

        return response.isSuccessful() ? Result.success() : Result.retry();
    }

    private String getAddressFromLocation(Context context, double lat, double lon) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);
            if (addresses != null && !addresses.isEmpty()) {
                return addresses.get(0).getAddressLine(0);
            }
        } catch (IOException e) {
            Log.e(TAG, "Geocoder failed");
        }
        return "Lat: " + lat + ", Lon: " + lon; // Fallback to coordinates
    }

    private boolean isWithinShift(String startStr, String endStr) {
        if (startStr == null || endStr == null) return true;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date now = sdf.parse(sdf.format(new Date()));
            Date start = sdf.parse(startStr);
            Date end = sdf.parse(endStr);

            if (now == null || start == null || end == null) return true;

            if (end.before(start)) { // Overnight shift check
                return now.after(start) || now.before(end);
            }
            return now.after(start) && now.before(end);
        } catch (ParseException e) {
            return true;
        }
    }
}
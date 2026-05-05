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

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        SharedPreferences prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

        // Retrieve credentials saved during the PunchActivity success event
        String userId = prefs.getString("userId", null);
        String authToken = prefs.getString("authToken", null);

        if (userId == null || authToken == null) {
            Log.e(TAG, "Worker failed: Missing credentials. Stopping worker.");
            return Result.failure();
        }

        String authHeader = "jwt " + authToken;

        try {
            // 1. Fetch User Profile to check current Shift timings
            APIInterface apiInterface = APIClient.getInstance().getLogin();
            Response<UserProfileResponse> profileResponse = apiInterface.getUserProfile(userId, authHeader).execute();

            if (profileResponse.isSuccessful() && profileResponse.body() != null) {
                String startTime = profileResponse.body().getData().getEmployee().getShift().getStartTime();
                String endTime = profileResponse.body().getData().getEmployee().getShift().getEndTime();

                // 2. SELF-TERMINATION LOGIC: Check if current time is outside shift window
                if (!isWithinShift(startTime, endTime)) {
                    Log.d(TAG, "Shift ended at " + endTime + ". Terminating background worker.");

                    // This cancels the unique periodic work so it won't run again until next Punch In
                    WorkManager.getInstance(context).cancelUniqueWork("EmployeeTracking");

                    return Result.success();
                }
            }

            // 3. If within shift, check location permissions[cite: 3]
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "Location permission missing. Background tracking paused.");
                return Result.failure();
            }

            // 4. Extract location and sync with server[cite: 3]
            Location location = Tasks.await(LocationServices.getFusedLocationProviderClient(context)
                    .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null));

            if (location != null) {
                return sendAttendance(context, authHeader, userId, location);
            }

        } catch (Exception e) {
            Log.e(TAG, "Worker execution failed: " + e.getMessage());
            return Result.retry(); // Reschedules for another attempt within this 15m window
        }

        return Result.retry();
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

        RequestBody requestBody = RequestBody.create(
                MediaType.parse("application/json"), body.toString());

        APIInterface service = APIClient.getInstance().getAttendance();
        Response<ResponseBody> response = service.AttendanceApi(authHeader, requestBody).execute();

        if (response.isSuccessful()) {
            Log.d(TAG, "Tracking update successful: " + preciseAddress);
            return Result.success();
        } else {
            return Result.retry();
        }
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
        return "Location Not Found (" + lat + ", " + lon + ")";
    }

    private boolean isWithinShift(String startStr, String endStr) {
        if (startStr == null || endStr == null) return true;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            String currentTimeStr = sdf.format(new Date());
            Date now = sdf.parse(currentTimeStr);
            Date start = sdf.parse(startStr);
            Date end = sdf.parse(endStr);

            if (now == null || start == null || end == null) return true;

            // Handle shifts that cross midnight
            if (end.before(start)) {
                return now.after(start) || now.before(end);
            }
            return now.after(start) && now.before(end);
        } catch (ParseException e) {
            return true;
        }
    }
}
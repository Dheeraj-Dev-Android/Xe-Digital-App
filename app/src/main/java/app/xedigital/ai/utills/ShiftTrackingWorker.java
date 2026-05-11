package app.xedigital.ai.utills;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
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
import java.net.UnknownHostException;
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

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        SharedPreferences prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

        String userId = prefs.getString("userId", null);
        String authToken = prefs.getString("authToken", null);

        if (userId == null || authToken == null) {
            Log.e(TAG, "No credentials found. Failure.");
            return Result.failure();
        }

        String authHeader = authToken.startsWith("jwt ") ? authToken : "jwt " + authToken;

        try {
            // ── Step 1: Permission Check ──
            boolean hasFine = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
            boolean hasBack = true;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                hasBack = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED;
            }

            if (!hasFine || !hasBack) {
                Log.e(TAG, "Permissions missing: Fine=" + hasFine + " Background=" + hasBack);
                return Result.retry();
            }

            // ── Step 2: Shift Check ──
            APIInterface api = APIClient.getInstance().getLogin();
            Response<UserProfileResponse> profileResponse = api.getUserProfile(userId, authHeader).execute();

            if (profileResponse.isSuccessful() && profileResponse.body() != null) {
                UserProfileResponse res = profileResponse.body();
                if (res.getData() != null && res.getData().getEmployee() != null && res.getData().getEmployee().getShift() != null) {
                    if (!isWithinShift(res.getData().getEmployee().getShift().getStartTime(),
                            res.getData().getEmployee().getShift().getEndTime())) {
                        Log.d(TAG, "Shift window closed. Stopping.");
                        WorkManager.getInstance(context).cancelUniqueWork("EmployeeTracking");
                        return Result.success();
                    }
                }
            }

            // ── Step 3: Fetch Location ──
            @SuppressLint("MissingPermission")
            Location location = Tasks.await(
                    LocationServices.getFusedLocationProviderClient(context)
                            .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null),
                    20, TimeUnit.SECONDS
            );

            if (location != null) {
                return sendAttendance(context, authHeader, userId, location);
            } else {
                return Result.retry();
            }

        } catch (UnknownHostException e) {
            Log.e(TAG, "DNS Error: app.xedigital.ai not found. Check network.");
            return Result.retry();
        } catch (IOException e) {
            Log.e(TAG, "I/O Error: Retrying next cycle.");
            return Result.retry();
        } catch (Exception e) {
            Log.e(TAG, "Worker Error: " + e.getMessage());
            return Result.retry();
        }
    }

    private Result sendAttendance(Context context, String auth, String uid, Location loc) throws Exception {
        String address = getAddressFromLocation(context, loc.getLatitude(), loc.getLongitude());

        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        fmt.setTimeZone(TimeZone.getTimeZone("UTC"));

        JSONObject body = new JSONObject();
        body.put("employee", uid);
        body.put("address", address);
        body.put("punchTime", fmt.format(new Date(loc.getTime())));
        body.put("latitude", loc.getLatitude());
        body.put("longitude", loc.getLongitude());

        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), body.toString());
        Response<ResponseBody> response = APIClient.getInstance().getAttendance().AttendanceApi(auth, requestBody).execute();

        return response.isSuccessful() ? Result.success() : Result.retry();
    }

    private String getAddressFromLocation(Context context, double lat, double lon) {
        if (!Geocoder.isPresent()) return "Lat: " + lat + ", Lon: " + lon;
        try {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);
            if (addresses != null && !addresses.isEmpty())
                return addresses.get(0).getAddressLine(0);
        } catch (Exception ignored) {
        }
        return "Lat: " + lat + ", Lon: " + lon;
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
}
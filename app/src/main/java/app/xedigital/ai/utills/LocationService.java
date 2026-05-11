package app.xedigital.ai.utills;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import app.xedigital.ai.R;
import app.xedigital.ai.api.APIClient;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Response;

public class LocationService extends Service {
    private static final String CHANNEL_ID = "LocationServiceChannel";
    private static final String TAG = "LocationService";
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private PowerManager.WakeLock wakeLock;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Keep CPU awake even if screen is off
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "XEDigital:TrackingWakeLock");
        wakeLock.acquire(10 * 60 * 1000L /*10 minutes maximum if not refreshed*/);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) return;
                for (Location location : locationResult.getLocations()) {
                    Log.d(TAG, "New Location: " + location.getLatitude() + ", " + location.getLongitude());
                    sendLocationToServer(location);
                }
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Shift Tracking Active")
                .setContentText("ConsultEdge is tracking your location in the background.")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOngoing(true)
                .build();

        startForeground(101, notification);
        startLocationUpdates();
        return START_STICKY;
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        // Reduced interval to 5 minutes for better reliability while testing
        LocationRequest locationRequest = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, TimeUnit.MINUTES.toMillis(5))
                .setMinUpdateIntervalMillis(TimeUnit.MINUTES.toMillis(3))
                // CRITICAL: Set to false so it doesn't get stuck waiting for GPS inside buildings
                .setWaitForAccurateLocation(false)
                .build();

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private void sendLocationToServer(Location loc) {
        new Thread(() -> {
            try {
                SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                String userId = prefs.getString("userId", null);
                String authToken = prefs.getString("authToken", null);
                if (userId == null || authToken == null) return;

                String address = getAddressFromLocation(loc.getLatitude(), loc.getLongitude());

                // Format time as expected by most punch APIs
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                String currentTime = sdf.format(new Date());

                JSONObject body = new JSONObject();
                body.put("employee", userId);
                body.put("latitude", loc.getLatitude());
                body.put("longitude", loc.getLongitude());
                body.put("address", address);
                body.put("punchTime", currentTime);

                RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), body.toString());
                String authHeader = authToken.startsWith("jwt ") ? authToken : "jwt " + authToken;

                Response<ResponseBody> response = APIClient.getInstance().getAttendance().AttendanceApi(authHeader, requestBody).execute();
                if (response.isSuccessful()) {
                    Log.d(TAG, "Background Ping Success at " + currentTime);
                } else {
                    Log.e(TAG, "API Error: " + response.code());
                }
            } catch (Exception e) {
                Log.e(TAG, "Network failed: " + e.getMessage());
            }
        }).start();
    }

    private String getAddressFromLocation(double lat, double lon) {
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);
            if (addresses != null && !addresses.isEmpty()) {
                return addresses.get(0).getAddressLine(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Geocoder error: " + e.getMessage());
        }
        return "Lat: " + lat + ", Lon: " + lon;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Shift Tracking", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        if (wakeLock != null && wakeLock.isHeld()) wakeLock.release();
        fusedLocationClient.removeLocationUpdates(locationCallback);
        super.onDestroy();
    }
}
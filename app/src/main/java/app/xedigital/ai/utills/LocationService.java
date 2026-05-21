package app.xedigital.ai.utills;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ServiceInfo;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.NonNull;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import app.xedigital.ai.api.APIClient;

public class LocationService extends Service {
    private static final String TAG = "LocationService";
    private static final String CHANNEL_ID = "ShiftTrackingChannel";
    private static final int NOTIFICATION_ID = 1001;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private PowerManager.WakeLock wakeLock;
    private ExecutorService geocoderExecutor;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForegroundServiceWithNotification();

        // Thread pool to handle Geocoder requests asynchronously without blocking or dropping lookups
        geocoderExecutor = Executors.newSingleThreadExecutor();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Standard setup for high accuracy background updates
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 300000) // 5 minutes
                .setMinUpdateIntervalMillis(60000) // 1 minute shortest bridge window
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                SharedPreferences prefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                if (prefs != null) {
                    String start = prefs.getString("shift_start", null);
                    String end = prefs.getString("shift_end", null);

                    // 1. Shift Boundary Check: If past tracking hours, immediately stop everything
                    if (start != null && end != null) {
                        if (!isServiceWithinShiftCheck(start, end)) {
                            Log.w(TAG, "Shift hours expired. Destroying background tracking instantly.");
                            shutdownService();
                            return;
                        }
                    }
                }

                // 2. Extract and resolve coordinates asynchronously
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        resolvePreciseAddressAndSend(location);
                    }
                }
            }
        };

        requestLocationUpdates(locationRequest);
    }

    @SuppressLint("MissingPermission")
    private void requestLocationUpdates(LocationRequest locationRequest) {
        try {
            if (fusedLocationClient != null && locationCallback != null) {
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);

                PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                if (pm != null) {
                    wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "XEDigital::TrackingWakeLock");
                    wakeLock.acquire(10 * 60 * 1000L); // Hold for safety sequence window
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to start tracking loop updates: " + e.getMessage());
        }
    }

    /**
     * Resolves the precise address string on a background worker thread pool
     * and then passes everything cleanly to the API pipeline.
     */
    private void resolvePreciseAddressAndSend(final Location location) {
        if (geocoderExecutor == null || geocoderExecutor.isShutdown() || location == null) return;

        geocoderExecutor.execute(() -> {
            // Fallback placeholder address string
            final String fallbackAddress = "Lat: " + location.getLatitude() + ", Lon: " + location.getLongitude();

            try {
                Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1, new Geocoder.GeocodeListener() {
                        @Override
                        public void onGeocode(@NonNull List<Address> addresses) {
                            String resolved = fallbackAddress;
                            if (!addresses.isEmpty()) {
                                String resolvedLine = addresses.get(0).getAddressLine(0);
                                if (resolvedLine != null && !resolvedLine.isEmpty()) {
                                    resolved = resolvedLine;
                                }
                            }
                            // Execute the API update using the resolved address from the callback
                            sendLocationToServer(location, resolved);
                        }

                        @Override
                        public void onError(@Nullable String errorMessage) {
                            Log.e(TAG, "Geocoder async error: " + errorMessage);
                            sendLocationToServer(location, fallbackAddress);
                        }
                    });

                } else {
                    // Legacy synchronous code path for devices running beneath Android 13
                    String preciseAddress = fallbackAddress;
                    try {
                        List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                        if (addresses != null && !addresses.isEmpty()) {
                            String resolved = addresses.get(0).getAddressLine(0);
                            if (resolved != null && !resolved.isEmpty()) {
                                preciseAddress = resolved;
                            }
                        }
                    } catch (Exception oldEx) {
                        Log.e(TAG, "Legacy Geocoder lookup failed: " + oldEx.getMessage());
                    }

                    sendLocationToServer(location, preciseAddress);
                }
            } catch (Exception e) {
                Log.e(TAG, "Reverse-geocoding processing execution aborted: " + e.getMessage());
                sendLocationToServer(location, fallbackAddress);
            }
        });
    }

    /**
     * Fires the structured network tracking payload down to your API endpoint.
     * (Runs safely on your background thread pool executor).
     */
    private void sendLocationToServer(Location loc, String resolvedAddress) {
        try {
            SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            if (prefs == null) return;

            String userId = prefs.getString("userId", null);
            String authToken = prefs.getString("authToken", null);
            if (userId == null || authToken == null) {
                Log.w(TAG, "Aborting API dispatch: User credentials missing from shared preferences cache.");
                return;
            }

            // Generate clean UTC timestamp parameters required for backend logging
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            String currentTime = sdf.format(new Date());

            // Construct JSON Payload Model
            JSONObject body = new JSONObject();
            body.put("employee", userId);
            body.put("latitude", loc.getLatitude());
            body.put("longitude", loc.getLongitude());
            body.put("address", resolvedAddress);
            body.put("punchTime", currentTime);

            okhttp3.RequestBody requestBody = okhttp3.RequestBody.create(
                    okhttp3.MediaType.parse("application/json"),
                    body.toString()
            );

            String authHeader = authToken.startsWith("jwt ") ? authToken : "jwt " + authToken;

            Log.d(TAG, "Attempting Background Ping. payload: " + body);

            // Execute network call synchronously since this is already running inside geocoderExecutor thread space
            retrofit2.Response<okhttp3.ResponseBody> response = APIClient.getInstance()
                    .getAttendance()
                    .AttendanceApi(authHeader, requestBody)
                    .execute();

            if (response.isSuccessful()) {
                Log.d(TAG, "🎉 Background Ping Success! Location logged on dashboard.");
            } else {
                Log.w(TAG, "Server rejected background coordinate package status code: " + response.code());
            }

        } catch (Exception e) {
            Log.e(TAG, "Network failed during background telemetry dispatch: " + e.getMessage());
        }
    }

    private boolean isServiceWithinShiftCheck(String startStr, String endStr) {
        if (startStr == null || endStr == null || startStr.isEmpty() || endStr.isEmpty()) {
            return false;
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            String currentTimeStr = sdf.format(new Date());

            Date now = sdf.parse(currentTimeStr);
            Date start = sdf.parse(startStr);
            Date end = sdf.parse(endStr);

            if (now == null || start == null || end == null) return false;

            if (end.before(start)) {
                return now.after(start) || now.before(end);
            }
            return now.after(start) && now.before(end);
        } catch (Exception e) {
            Log.e(TAG, "Shift checking verification parser crash: " + e.getMessage());
            return false;
        }
    }

    private void startForegroundServiceWithNotification() {
        // 1. Double check runtime permissions before calling startForeground on Android 14
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            boolean hasCoarse = androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED;
            boolean hasFine = androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED;

            if (!hasCoarse && !hasFine) {
                Log.e(TAG, "Cannot start Foreground Service: Location permissions are missing at runtime.");
                stopSelf();
                return;
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Duty Tracking Active")
                .setContentText("Securing background workspace parameters updates...")
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOngoing(true);

        Notification notification = builder.build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
        } else {
            startForeground(NOTIFICATION_ID, notification);
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Shift Tracking System", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private void shutdownService() {
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
        if (geocoderExecutor != null && !geocoderExecutor.isShutdown()) {
            geocoderExecutor.shutdown();
        }
        stopForeground(true);
        stopSelf();
    }

    @Override
    public void onDestroy() {
        shutdownService();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
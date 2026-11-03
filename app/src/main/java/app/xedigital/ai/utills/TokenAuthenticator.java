package app.xedigital.ai.utills;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import app.xedigital.ai.activity.LoginSelectionActivity;
import okhttp3.Authenticator;
import okhttp3.CookieJar;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Route;

public class TokenAuthenticator implements Authenticator {

    private static final String TAG = "TokenAuthenticator";
    private static final int MAX_RETRIES = 1;

    // Single static lock shared across all instances (APIClient + AdminAPIClient)
    private static final Lock lock = new ReentrantLock();

    private final Context context;
    private final CookieJar cookieJar;

    public TokenAuthenticator(Context context, CookieJar cookieJar) {
        this.context = context.getApplicationContext();
        this.cookieJar = cookieJar;
    }

    @Nullable
    @Override
    public Request authenticate(@Nullable Route route, @NonNull Response response) throws IOException {

        // ── FIX #1: Count ONLY 401 responses, not redirects or other codes ──
        if (responseCount(response) > MAX_RETRIES) {
            Log.w(TAG, "Max 401 retries reached. Logging out.");
            logoutUser();
            return null;
        }

        // ── FIX #2: Exact path match instead of contains() ──
        String path = response.request().url().encodedPath();
        if (path.equals("/api/v1/authentication/refresh")) {
            Log.w(TAG, "Refresh endpoint itself returned 401. Logging out.");
            logoutUser();
            return null;
        }

        lock.lock();
        try {
            // Double-check: another thread may have already refreshed the token
            SecurePrefManager prefs = SecurePrefManager.getInstance(context);
            String currentToken = prefs.getString("authToken", null);
            String failedToken = response.request().header("Authorization");

            if (failedToken != null && !failedToken.equals("jwt " + currentToken)) {
                Log.d(TAG, "Token already refreshed by another thread. Retrying.");
                return response.request().newBuilder()
                        .header("Authorization", "jwt " + currentToken)
                        .build();
            }

            // Perform the actual refresh
            String newToken = performRefresh();
            if (newToken != null && !newToken.isEmpty()) {
                Log.d(TAG, "Token refreshed successfully. Retrying original request.");
                return response.request().newBuilder()
                        .header("Authorization", "jwt " + newToken)
                        .build();
            } else {
                Log.w(TAG, "Token refresh failed. Logging out.");
                logoutUser();
                return null;
            }
        } finally {
            lock.unlock();
        }
    }

    private String performRefresh() {
        try {
            SecurePrefManager prefs = SecurePrefManager.getInstance(context);
            String refreshToken = prefs.getString("refreshToken", null);

            if (refreshToken == null || refreshToken.isEmpty()) {
                Log.e(TAG, "No refresh token stored. Cannot refresh.");
                return null;
            }

            // ── CRITICAL FIX: Send refreshToken in the POST body ──
            // Server expects {"refreshToken": "..."} for platform "android"
            JSONObject json = new JSONObject();
            json.put("refreshToken", refreshToken);

            RequestBody body = RequestBody.create(
                    json.toString(),
                    MediaType.parse("application/json; charset=utf-8")
            );

            Request refreshRequest = new Request.Builder()
                    .url("https://app.xedigital.ai/api/v1/authentication/refresh")
                    .post(body)
                    .build();

            // ── FIX #6: Explicit timeouts on the refresh client ──
            // No AuthInterceptor, no Authenticator — avoids circular dependency
            OkHttpClient refreshClient = new OkHttpClient.Builder()
                    .connectTimeout(5, TimeUnit.SECONDS)
                    .readTimeout(5, TimeUnit.SECONDS)
                    .writeTimeout(5, TimeUnit.SECONDS)
                    .cookieJar(cookieJar)
                    .build();

            Response refreshResponse = refreshClient.newCall(refreshRequest).execute();

            try {
                if (refreshResponse.isSuccessful() && refreshResponse.body() != null) {
                    String responseBody = refreshResponse.body().string();
                    JSONObject responseJson = new JSONObject(responseBody);

                    if (responseJson.has("data")) {
                        JSONObject data = responseJson.getJSONObject("data");
                        if (data.has("token")) {
                            String newToken = data.getString("token");
                            prefs.putString("authToken", newToken);

                            // Update refresh token if server rotates it
                            if (data.has("refreshToken")) {
                                String newRefreshToken = data.getString("refreshToken");
                                if (!newRefreshToken.isEmpty()) {
                                    prefs.putString("refreshToken", newRefreshToken);
                                }
                            }

                            Log.d(TAG, "Refresh successful. New token stored.");
                            return newToken;
                        }
                    }
                    Log.e(TAG, "Refresh response missing data.token: " + responseBody);
                } else {
                    Log.e(TAG, "Refresh failed with HTTP " + refreshResponse.code());
                }
            } finally {
                refreshResponse.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception during token refresh", e);
        }
        return null;
    }

    // ── FIX #1: Only count 401 responses in the chain ──
    private int responseCount(Response response) {
        int result = 0;
        while (response != null) {
            if (response.code() == 401) {
                result++;
            }
            response = response.priorResponse();
        }
        return result;
    }

    private void logoutUser() {
        // Use clearSession() to preserve device-binding keys (installation_id, etc.)
        SecurePrefManager.getInstance(context).clearSession();
        new Handler(Looper.getMainLooper()).post(() -> {
            Intent intent = new Intent(context, LoginSelectionActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
        });
    }
}
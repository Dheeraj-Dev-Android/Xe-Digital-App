package app.xedigital.ai.utills;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Authenticator;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Route;

public class TokenAuthenticator implements Authenticator {

    private static final String TAG = "TokenAuthenticator";
    private static final int MAX_RETRY_COUNT = 2;

    private final Context context;
    private final PersistentCookieJar cookieJar;
    private final OkHttpClient refreshClient;
    private final Object lock = new Object();

    public TokenAuthenticator(Context context, PersistentCookieJar cookieJar) {
        this.context = context.getApplicationContext();
        this.cookieJar = cookieJar;
        // Separate client: has CookieJar (to send refresh cookie) but NO Authenticator (prevents infinite loop)
        this.refreshClient = new OkHttpClient.Builder()
                .cookieJar(cookieJar)
                .build();
    }

    @Override
    public Request authenticate(Route route, Response response) throws IOException {

        // 1. Prevent infinite retry loops
        if (responseCount(response) >= MAX_RETRY_COUNT) {
            Log.w(TAG, "Max retry count reached, giving up");
            handleSessionExpired();
            return null;
        }

        // 2. If the refresh endpoint itself returned 401, the session is dead
        if (response.request().url().toString().contains("/authentication/refresh")) {
            Log.w(TAG, "Refresh endpoint returned 401 — session expired or revoked");
            handleSessionExpired();
            return null;
        }

        // 3. Synchronized block: only ONE thread refreshes, others wait and reuse
        synchronized (lock) {
            SecurePrefManager prefManager = SecurePrefManager.getInstance(context);

            // Check if another thread already refreshed while we were waiting for the lock
            String currentToken = prefManager.getString("authToken", null);
            String failedToken = extractTokenFromRequest(response.request());

            if (currentToken != null && !currentToken.equals(failedToken)) {
                Log.d(TAG, "Token already refreshed by another thread, retrying");
                return response.request().newBuilder()
                        .header("Authorization", "jwt " + currentToken)
                        .build();
            }

            // 4. Dynamically build refresh URL based on host/scheme of the failed request
            HttpUrl refreshUrl = response.request().url().newBuilder()
                    .encodedPath("/api/v1/authentication/refresh")
                    .query(null)
                    .build();

            // 5. Attempt synchronous refresh
            String newToken = performRefresh(refreshUrl);

            if (newToken != null) {
                Log.d(TAG, "Token refreshed successfully, retrying original request");
                return response.request().newBuilder()
                        .header("Authorization", "jwt " + newToken)
                        .build();
            } else {
                Log.w(TAG, "Token refresh failed — session expired");
                handleSessionExpired();
                return null;
            }
        }
    }

    private String performRefresh(HttpUrl refreshUrl) {
        try {
            RequestBody emptyBody = RequestBody.create("{}",
                    MediaType.parse("application/json"));

            Request refreshRequest = new Request.Builder()
                    .url(refreshUrl)
                    .post(emptyBody)
                    .build();

            Response refreshResponse = refreshClient.newCall(refreshRequest).execute();

            if (refreshResponse.isSuccessful() && refreshResponse.body() != null) {
                String responseBody = refreshResponse.body().string();
                JSONObject json = new JSONObject(responseBody);

                if (json.optBoolean("success", false) && json.has("data")) {
                    JSONObject data = json.getJSONObject("data");
                    String newToken = data.optString("token", null);

                    if (newToken != null && !newToken.isEmpty()) {
                        SecurePrefManager.getInstance(context).putString("authToken", newToken);
                        return newToken;
                    }
                }
            }

            Log.e(TAG, "Refresh failed with HTTP " + refreshResponse.code());
            return null;

        } catch (Exception e) {
            Log.e(TAG, "Refresh call exception: " + e.getMessage());
            return null;
        }
    }

    private void handleSessionExpired() {
        SecurePrefManager prefManager = SecurePrefManager.getInstance(context);
        prefManager.clearSession();
        cookieJar.clearAll();

        // Broadcast to all visible SecureBaseActivity instances
        Intent intent = new Intent("ACTION_SESSION_EXPIRED");
        intent.setPackage(context.getPackageName());
        context.sendBroadcast(intent);
    }

    private String extractTokenFromRequest(Request request) {
        String authHeader = request.header("Authorization");
        if (authHeader != null && authHeader.startsWith("jwt ")) {
            return authHeader.substring(4);
        }
        return null;
    }

    private int responseCount(Response response) {
        int count = 1;
        while ((response = response.priorResponse()) != null) {
            count++;
        }
        return count;
    }
}
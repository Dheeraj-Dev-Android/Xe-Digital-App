package app.xedigital.ai.utills;

import android.content.Context;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * FIX #8: Global interceptor that attaches the JWT to every outgoing request.
 * Skips if the Authorization header is already present (e.g., set manually
 * via @Header in a Retrofit interface, or by TokenAuthenticator after refresh).
 */
public class AuthInterceptor implements Interceptor {

    private static final String TAG = "AuthInterceptor";
    private final Context context;

    public AuthInterceptor(Context context) {
        this.context = context.getApplicationContext();
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request originalRequest = chain.request();

        // Don't overwrite an existing Authorization header
        if (originalRequest.header("Authorization") != null) {
            return chain.proceed(originalRequest);
        }

        String token = SecurePrefManager.getInstance(context).getString("authToken", null);
        if (token != null && !token.isEmpty()) {
            Request authenticatedRequest = originalRequest.newBuilder()
                    .header("Authorization", "jwt " + token)
                    .build();
            return chain.proceed(authenticatedRequest);
        }

        // No token available (user not logged in yet) — proceed as-is
        return chain.proceed(originalRequest);
    }
}
package app.xedigital.ai.utills;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

public class PersistentCookieJar implements CookieJar {

    private static final String TAG = "PersistentCookieJar";
    private static final String COOKIES_KEY = "http_cookies";
    private final SecurePrefManager prefManager;
    private final Gson gson = new Gson();
    private List<SerializableCookie> cookieStore = new ArrayList<>();

    public PersistentCookieJar(Context context) {
        this.prefManager = SecurePrefManager.getInstance(context);
        loadFromStorage();
    }

    @Override
    public synchronized void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        if (cookies != null && !cookies.isEmpty()) {
            Log.d(TAG, "🍪 [COOKIE_JAR] Saving " + cookies.size() + " cookie(s) from response URL: " + url);
        }

        for (Cookie newCookie : cookies) {
            Log.d(TAG, "   └─ Captured Cookie: " + newCookie.name()
                    + " | Value: " + (newCookie.value().length() > 15 ? newCookie.value().substring(0, 15) + "..." : newCookie.value())
                    + " | Domain: " + newCookie.domain()
                    + " | Path: " + newCookie.path()
                    + " | ExpiresAt: " + newCookie.expiresAt());

            // Remove existing cookie with same name, domain, and path
            cookieStore.removeIf(sc ->
                    sc.name.equals(newCookie.name()) &&
                            sc.domain.equals(newCookie.domain()) &&
                            sc.path.equals(newCookie.path()));

            // Only store if not expired
            if (newCookie.expiresAt() > System.currentTimeMillis()) {
                cookieStore.add(new SerializableCookie(newCookie));
            } else {
                Log.w(TAG, "   └─ Cookie " + newCookie.name() + " already expired, skipping storage.");
            }
        }
        saveToStorage();
    }

    @Override
    public synchronized List<Cookie> loadForRequest(HttpUrl url) {
        List<Cookie> matching = new ArrayList<>();
        long now = System.currentTimeMillis();
        List<SerializableCookie> expired = new ArrayList<>();

        for (SerializableCookie sc : cookieStore) {
            if (sc.expiresAt < now) {
                expired.add(sc);
                continue;
            }
            Cookie cookie = sc.toCookie();
            if (cookie != null && cookie.matches(url)) {
                matching.add(cookie);
            }
        }

        if (!matching.isEmpty()) {
            Log.d(TAG, "🍪 [COOKIE_JAR] Attaching " + matching.size() + " stored cookie(s) to outgoing request: " + url.encodedPath());
            for (Cookie c : matching) {
                Log.d(TAG, "   └─ Attached Header: Cookie: " + c.name() + "=" + (c.value().length() > 15 ? c.value().substring(0, 15) + "..." : c.value()));
            }
        }

        // Clean up expired cookies
        if (!expired.isEmpty()) {
            Log.w(TAG, "🧹 [COOKIE_JAR] Cleaned up " + expired.size() + " expired cookie(s) from memory.");
            cookieStore.removeAll(expired);
            saveToStorage();
        }

        return matching;
    }

    public synchronized void clearAll() {
        Log.w(TAG, "🧹 [COOKIE_JAR] Clearing all cookies from memory and EncryptedSharedPreferences.");
        cookieStore.clear();
        prefManager.remove(COOKIES_KEY);
    }

    private void saveToStorage() {
        prefManager.putString(COOKIES_KEY, gson.toJson(cookieStore));
    }

    private void loadFromStorage() {
        String json = prefManager.getString(COOKIES_KEY, null);
        if (json != null && !json.isEmpty()) {
            try {
                Type type = new TypeToken<List<SerializableCookie>>() {
                }.getType();
                List<SerializableCookie> loaded = gson.fromJson(json, type);
                if (loaded != null) {
                    cookieStore = loaded;
                    Log.d(TAG, "📦 [COOKIE_JAR] Loaded " + cookieStore.size() + " stored cookie(s) from EncryptedSharedPreferences.");
                }
            } catch (Exception e) {
                Log.e(TAG, "❌ [COOKIE_JAR] Failed to parse stored cookies: " + e.getMessage());
                cookieStore.clear();
            }
        } else {
            Log.d(TAG, "📦 [COOKIE_JAR] No stored cookies found in EncryptedSharedPreferences.");
        }
    }

    /**
     * Serializable wrapper for OkHttp Cookie (which has no public constructor).
     */
    private static class SerializableCookie {
        String name;
        String value;
        String domain;
        String path;
        long expiresAt;
        boolean secure;
        boolean httpOnly;
        boolean hostOnly;
        boolean persistent;

        SerializableCookie(Cookie cookie) {
            this.name = cookie.name();
            this.value = cookie.value();
            this.domain = cookie.domain();
            this.path = cookie.path();
            this.expiresAt = cookie.expiresAt();
            this.secure = cookie.secure();
            this.httpOnly = cookie.httpOnly();
            this.hostOnly = cookie.hostOnly();
            this.persistent = cookie.persistent();
        }

        Cookie toCookie() {
            try {
                Cookie.Builder builder = new Cookie.Builder()
                        .name(name)
                        .value(value)
                        .path(path);

                if (hostOnly) {
                    builder.hostOnlyDomain(domain);
                } else {
                    builder.domain(domain);
                }

                if (persistent) {
                    builder.expiresAt(expiresAt);
                }

                if (secure) builder.secure();
                if (httpOnly) builder.httpOnly();

                return builder.build();
            } catch (Exception e) {
                return null;
            }
        }
    }
}
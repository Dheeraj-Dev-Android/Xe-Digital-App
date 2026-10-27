package app.xedigital.ai.utills;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

public class PersistentCookieJar implements CookieJar {

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
        for (Cookie newCookie : cookies) {
            // Remove existing cookie with same name, domain, and path
            cookieStore.removeIf(sc ->
                    sc.name.equals(newCookie.name()) &&
                            sc.domain.equals(newCookie.domain()) &&
                            sc.path.equals(newCookie.path()));

            // Only store if not expired
            if (newCookie.expiresAt() > System.currentTimeMillis()) {
                cookieStore.add(new SerializableCookie(newCookie));
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

        // Clean up expired cookies
        if (!expired.isEmpty()) {
            cookieStore.removeAll(expired);
            saveToStorage();
        }

        return matching;
    }

    public synchronized void clearAll() {
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
                }
            } catch (Exception e) {
                cookieStore.clear();
            }
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
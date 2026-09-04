package app.xedigital.ai.utills;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class SecurePrefManager {
    private static final String PREF_FILE_NAME = "secure_prefs";
    private static SecurePrefManager instance;
    private SharedPreferences sharedPreferences;

    private SecurePrefManager(Context context) {
        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);

            sharedPreferences = EncryptedSharedPreferences.create(PREF_FILE_NAME, masterKeyAlias, context.getApplicationContext(), EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            sharedPreferences = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        }
    }

    public static synchronized SecurePrefManager getInstance(Context context) {
        if (instance == null) {
            instance = new SecurePrefManager(context);
        }
        return instance;
    }

    public void putString(String key, String value) {
        sharedPreferences.edit().putString(key, value).apply();
    }

    public String getString(String key, String defaultValue) {
        return sharedPreferences.getString(key, defaultValue);
    }

    public void putBoolean(String key, boolean value) {
        sharedPreferences.edit().putBoolean(key, value).apply();
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return sharedPreferences.getBoolean(key, defaultValue);
    }

    public void remove(String key) {
        sharedPreferences.edit().remove(key).apply();
    }

    public boolean contains(String key) {
        return sharedPreferences.contains(key);
    }

    public void clearAll() {
        sharedPreferences.edit().clear().apply();
    }

    public void clearSession() {
        String installId = getString("installation_id", null);
        String boundUserId = getString("bound_user_id", null);
        String boundEmail = getString("bound_user_email", null);

        sharedPreferences.edit().clear().apply();

        // Restore hardware & device-binding identity
        if (installId != null) putString("installation_id", installId);
        if (boundUserId != null) putString("bound_user_id", boundUserId);
        if (boundEmail != null) putString("bound_user_email", boundEmail);
    }
}
package app.xedigital.ai.utills;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import java.util.concurrent.Executor;

public class BioMetric {

    private static final int BIOMETRIC_PERMISSION_REQUEST_CODE = 100;
    private final Context context;
    private final FragmentActivity activity;
    private final BiometricAuthListener biometricAuthListener;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    public BioMetric(Context context, FragmentActivity activity, BiometricAuthListener listener) {
        this.context = context;
        this.activity = activity;
        this.biometricAuthListener = listener;
        initialize();
    }

    private void initialize() {
        Executor executor = ContextCompat.getMainExecutor(context);

        biometricPrompt = new BiometricPrompt(activity, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Log.e("Biometric", "Error: " + errString + ", code: " + errorCode);
                if (biometricAuthListener != null) {
                    biometricAuthListener.onAuthenticationError(errorCode, errString);
                }
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Log.d("Biometric", "Success!");
                if (biometricAuthListener != null) {
                    biometricAuthListener.onAuthenticationSucceeded();
                }
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Log.e("Biometric", "Failed");
                if (biometricAuthListener != null) {
                    biometricAuthListener.onAuthenticationFailed();
                }
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder().setTitle("Biometric").setSubtitle("Use Your Biometric Credentials to Punch Attendance").setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL).build();
    }

    public void authenticate() {
        if (!hasBiometricPermission()) {
            requestBiometricPermission();
            return;
        }
        BiometricManager biometricManager = BiometricManager.from(context);
        int result = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL | BiometricManager.Authenticators.BIOMETRIC_WEAK);

        if (result == BiometricManager.BIOMETRIC_SUCCESS) {
            Log.d("MY_APP_TAG", "App can authenticate using biometrics.");
            biometricPrompt.authenticate(promptInfo);
        } else if (result == BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE) {
            Log.e("MY_APP_TAG", "No biometric features available on this device.");
            Toast.makeText(context, "No biometric features available on this device.", Toast.LENGTH_LONG).show();
        } else if (result == BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE) {
            Log.e("MY_APP_TAG", "Biometric features are currently unavailable.");
            Toast.makeText(context, "Biometric features are currently unavailable.", Toast.LENGTH_LONG).show();
        } else if (result == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED) {
            Log.e("MY_APP_TAG", "The user hasn't associated any biometric credentials with their account.");
            Toast.makeText(context, "The user hasn't associated any biometric credentials with their account.", Toast.LENGTH_LONG).show();
        } else if (result == BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED) {
            Log.e("MY_APP_TAG", "A security update is required.");
            Toast.makeText(context, "A security update is required.", Toast.LENGTH_LONG).show();
        } else if (result == BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED) {
            Log.e("MY_APP_TAG", "The device does not support the type of biometrics requested.");
            Toast.makeText(context, "The device does not support the type of biometrics requested.", Toast.LENGTH_LONG).show();
        } else {
            Log.e("MY_APP_TAG", "Unhandled Biometric Error.");
            Toast.makeText(context, "An Unhandled Biometric Error.", Toast.LENGTH_LONG).show();
        }
    }

    private boolean hasBiometricPermission() {
        return ContextCompat.checkSelfPermission(context, android.Manifest.permission.USE_BIOMETRIC) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestBiometricPermission() {
        ActivityCompat.requestPermissions(activity, new String[]{android.Manifest.permission.USE_BIOMETRIC}, BIOMETRIC_PERMISSION_REQUEST_CODE);
    }

    public void handlePermissionResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == BIOMETRIC_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Biometric permission granted, proceed with authentication
                authenticate();
            } else {
                // Biometric permission denied, handle accordingly
                Log.e("Biometric", "Biometric permission denied");
                Toast.makeText(context, "Biometric permission denied", Toast.LENGTH_SHORT).show();
            }
        }

    }

    public interface BiometricAuthListener {
        void onAuthenticationSucceeded();

        void onAuthenticationError(int errorCode, CharSequence errString);

        void onAuthenticationFailed();
    }
}
package app.xedigital.ai.utills;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
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
    private BiometricPrompt.PromptInfo promptInfoLogin;
    private BiometricPrompt.PromptInfo promptInfoAttendance;

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

        promptInfoLogin = createPromptInfo("Security Authentication", "Use your biometric or device PIN/Pattern to log in");
        promptInfoAttendance = createPromptInfo("Punch Attendance", "Use your biometric or device PIN/Pattern to punch attendance");
    }

    private BiometricPrompt.PromptInfo createPromptInfo(String title, String subtitle) {
        BiometricPrompt.PromptInfo.Builder builder = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setSubtitle(subtitle);

        // Include both strong biometrics and device credentials (PIN/Pattern/Password)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            builder.setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL);
        } else {
            builder.setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.BIOMETRIC_WEAK | BiometricManager.Authenticators.DEVICE_CREDENTIAL);
        }

        // Note: Do NOT set negative button text when DEVICE_CREDENTIAL is supported.
        return builder.build();
    }

    private int getAuthenticators() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL;
        } else {
            return BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.BIOMETRIC_WEAK | BiometricManager.Authenticators.DEVICE_CREDENTIAL;
        }
    }

    public void authenticate(boolean forLogin) {
        if (!hasBiometricPermission()) {
            Log.e("Biometric", "Biometric permission not granted");
            requestBiometricPermission();
            return;
        }

        BiometricManager biometricManager = BiometricManager.from(context);
        int result = biometricManager.canAuthenticate(getAuthenticators());

        if (result == BiometricManager.BIOMETRIC_SUCCESS) {
            Log.d("Biometric", "Device can authenticate using biometrics or PIN/Pattern");
            biometricPrompt.authenticate(forLogin ? promptInfoLogin : promptInfoAttendance);
        } else {
            String errorMsg = getErrorMessage(result);
            Log.e("Biometric", errorMsg);
            Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show();

            if (biometricAuthListener != null) {
                biometricAuthListener.onAuthenticationError(result, errorMsg);
            }
        }
    }

    public boolean isDeviceSecurityAvailable() {
        BiometricManager biometricManager = BiometricManager.from(context);
        int result = biometricManager.canAuthenticate(getAuthenticators());
        return result == BiometricManager.BIOMETRIC_SUCCESS;
    }

    public boolean isBiometricAvailable() {
        return isDeviceSecurityAvailable();
    }

    private String getErrorMessage(int result) {
        switch (result) {
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                return "No biometric features or security lock available on this device";
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                return "Biometric features are currently unavailable";
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                return "No biometrics or PIN/Pattern locks enrolled on this device";
            case BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED:
                return "Security update required";
            case BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED:
                return "Biometric authentication is unsupported";
            default:
                return "Authentication error or lock not configured";
        }
    }

    private boolean hasBiometricPermission() {
        return ContextCompat.checkSelfPermission(context, android.Manifest.permission.USE_BIOMETRIC) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestBiometricPermission() {
        ActivityCompat.requestPermissions(activity, new String[]{android.Manifest.permission.USE_BIOMETRIC}, BIOMETRIC_PERMISSION_REQUEST_CODE);
    }

    public void handlePermissionResult(int requestCode, String[] permissions, int[] grantResults, boolean forLogin) {
        if (requestCode == BIOMETRIC_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                authenticateAfterPermission(forLogin);
            } else {
                Log.e("Biometric", "Biometric permission denied");
                Toast.makeText(context, "Biometric permission denied", Toast.LENGTH_LONG).show();
                if (biometricAuthListener != null) {
                    biometricAuthListener.onAuthenticationError(-1, "Permission Denied");
                }
            }
        }
    }

    private void authenticateAfterPermission(boolean forLogin) {
        BiometricManager biometricManager = BiometricManager.from(context);
        int result = biometricManager.canAuthenticate(getAuthenticators());

        if (result == BiometricManager.BIOMETRIC_SUCCESS) {
            biometricPrompt.authenticate(forLogin ? promptInfoLogin : promptInfoAttendance);
        } else {
            Log.e("Biometric", "Authentication unavailable");
            Toast.makeText(context, "Authentication unavailable", Toast.LENGTH_LONG).show();
            if (biometricAuthListener != null) {
                biometricAuthListener.onAuthenticationError(result, "Authentication Unavailable");
            }
        }
    }

    public interface BiometricAuthListener {
        void onAuthenticationSucceeded();

        void onAuthenticationError(int errorCode, CharSequence errString);

        void onAuthenticationFailed();
    }
}
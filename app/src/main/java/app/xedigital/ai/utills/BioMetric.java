package app.xedigital.ai.utills;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

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
            public void onAuthenticationError(int errorCode, @androidx.annotation.NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Log.e("Biometric", "Error: " + errString + ", code: " + errorCode);
                if (biometricAuthListener != null) {
                    biometricAuthListener.onAuthenticationError(errorCode, errString);
                }
            }

            @Override
            public void onAuthenticationSucceeded(@androidx.annotation.NonNull BiometricPrompt.AuthenticationResult result) {
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
//        promptInfoLogin = new BiometricPrompt.PromptInfo.Builder().setTitle("Biometric Login").setSubtitle("Use your biometric credentials to login").setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL).build();
//
//        promptInfoAttendance = new BiometricPrompt.PromptInfo.Builder().setTitle("Punch Attendance").setSubtitle("Use your biometric credentials to punch attendance").setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL).build();
        // Initialize PromptInfo with proper authenticator settings
        promptInfoLogin = createPromptInfo("Biometric Login", "Use your biometric credentials to login");
        promptInfoAttendance = createPromptInfo("Punch Attendance", "Use your biometric credentials to punch attendance");

    }

    private BiometricPrompt.PromptInfo createPromptInfo(String title, String subtitle) {
        BiometricPrompt.PromptInfo.Builder builder = new BiometricPrompt.PromptInfo.Builder().setTitle(title).setSubtitle(subtitle);

        // Set authenticators based on API level
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // API 30+ supports BIOMETRIC_STRONG | DEVICE_CREDENTIAL
            builder.setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL);
        } else {
            // Before API 30, only device credential or biometric can be used, not both at the same time
            builder.setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.BIOMETRIC_WEAK | BiometricManager.Authenticators.DEVICE_CREDENTIAL);
            builder.setDeviceCredentialAllowed(true)

        }
        return builder.build();
    }

    public void authenticate(boolean forLogin) {
//        if (!hasBiometricPermission()) {
//            Log.e("Biometric", "Biometric permission not granted");
//            requestBiometricPermission();
//        }
//        BiometricManager biometricManager = BiometricManager.from(context);
//        int result = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL | BiometricManager.Authenticators.BIOMETRIC_WEAK);

        if (!hasBiometricPermission()) {
            Log.e("Biometric", "Biometric permission not granted");
            requestBiometricPermission();
            return;
        }

        BiometricManager biometricManager = BiometricManager.from(context);
        int result;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            result = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL);
        } else {
            result = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.BIOMETRIC_WEAK);
        }
        if (result == BiometricManager.BIOMETRIC_SUCCESS) {
            Log.d("MY_APP_TAG", "App can authenticate using biometrics");
//            if(forLogin){
//                biometricPrompt.authenticate(promptInfoLogin);
//            }else{
//                biometricPrompt.authenticate(promptInfoAttendance);
//            }
            biometricPrompt.authenticate(forLogin ? promptInfoLogin : promptInfoAttendance);
        } else if (result == BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE) {
            Log.e("MY_APP_TAG", "No biometric features available on this device");
            Toast.makeText(context, "No biometric features available on this device", Toast.LENGTH_LONG).show();
        } else if (result == BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE) {
            Log.d("MY_APP_TAG", "Biometric features are currently unavailable");
            Toast.makeText(context, "Biometric features are currently unavailable", Toast.LENGTH_LONG).show();
        } else if (result == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED) {
            Log.d("MY_APP_TAG", "Biometric features are not enrolled");
            Toast.makeText(context, "Biometric features are not enrolled", Toast.LENGTH_LONG).show();
        } else if (result == BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED) {
            Log.d("MY_APP_TAG", "Security update required");
            Toast.makeText(context, "Security update required", Toast.LENGTH_LONG).show();
        } else if (result == BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED) {
            Log.d("MY_APP_TAG", "Biometric features are not supported");
            Toast.makeText(context, "Biometric features are not supported", Toast.LENGTH_LONG).show();
        } else {
            Log.e("MY_APP_TAG", "Unhandled Biometric Error.");
            Toast.makeText(context, "An Unhandled Biometric Error.", Toast.LENGTH_LONG).show();
        }
    }

    private boolean hasBiometricPermission() {
        return ContextCompat.checkSelfPermission(context, android.Manifest.permission.USE_BIOMETRIC) == android.content.pm.PackageManager.PERMISSION_GRANTED;
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
            }
        }
    }

//    private void authenticateAfterPermission(boolean forLogin) {
//        BiometricManager biometricManager = BiometricManager.from(context);
//        int result = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL | BiometricManager.Authenticators.BIOMETRIC_WEAK);
//        if (result == BiometricManager.BIOMETRIC_SUCCESS) {
//            biometricPrompt.authenticate(forLogin ? promptInfoLogin : promptInfoAttendance);
//        } else {
//            Log.e("Biometric", "Biometric authentication not available");
//            Toast.makeText(context, "Biometric authentication not available", Toast.LENGTH_LONG).show();
//        }
//    }

    private void authenticateAfterPermission(boolean forLogin) {
        BiometricManager biometricManager = BiometricManager.from(context);
        int result;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            result = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL);
        } else {
            result = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.BIOMETRIC_WEAK);
        }

        if (result == BiometricManager.BIOMETRIC_SUCCESS) {
            biometricPrompt.authenticate(forLogin ? promptInfoLogin : promptInfoAttendance);
        } else {
            Log.e("Biometric", "Biometric authentication not available");
            Toast.makeText(context, "Biometric authentication not available", Toast.LENGTH_LONG).show();
        }
    }


    public interface BiometricAuthListener {
        void onAuthenticationSucceeded();

        void onAuthenticationError(int errorCode, CharSequence errString);

        void onAuthenticationFailed();
    }
}

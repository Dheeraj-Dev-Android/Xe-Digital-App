package app.xedigital.ai.adminActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;

import app.xedigital.ai.R;
import app.xedigital.ai.activity.AdminDashboardActivity;

public class AdminPunchActivity extends AppCompatActivity {

    private final Handler handler = new Handler();
    private PreviewView previewView;
    private TextView captureOverlay;
    private ProcessCameraProvider cameraProvider;
    private final Runnable captureRunnable = this::captureAfterDelayLogic;
    private Camera camera;
    private final ActivityResultLauncher<String> cameraPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
        if (isGranted) {
            startCamera();
        } else {
            showPermissionDeniedDialog();
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_punch_activity);

        previewView = findViewById(R.id.previewView);
        captureOverlay = findViewById(R.id.capture_overlay);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_FRONT).build();

                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                cameraProvider.unbindAll();
                camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview);

                showCapturingOverlay();
                captureImageAfterDelay();

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void showCapturingOverlay() {
        runOnUiThread(() -> {
            captureOverlay.setVisibility(TextView.VISIBLE);

            Animation fadeAnim = new AlphaAnimation(0.2f, 1.0f);
            fadeAnim.setDuration(800);
            fadeAnim.setRepeatMode(Animation.REVERSE);
            fadeAnim.setRepeatCount(Animation.INFINITE);
            captureOverlay.startAnimation(fadeAnim);
        });
    }

    private void hideCapturingOverlay() {
        runOnUiThread(() -> {
            captureOverlay.clearAnimation();
            captureOverlay.setVisibility(TextView.GONE);
        });
    }

    private void captureImageAfterDelay() {
        handler.postDelayed(captureRunnable, 3000);
    }

    private void captureAfterDelayLogic() {
        hideCapturingOverlay();
        showAlert("Image Captured", "Image was captured after 3 seconds.");
    }

    private void showAlert(String title, String message) {
        runOnUiThread(() -> {
            // Prevent dialog leak if activity is finishing or destroyed
            if (isFinishing() || isDestroyed()) return;

            if (cameraProvider != null) {
                cameraProvider.unbindAll();
            }

            new AlertDialog.Builder(AdminPunchActivity.this).setTitle(title).setMessage(message).setCancelable(false).setPositiveButton("OK", (dialog, which) -> {
                dialog.dismiss();
                navigateToDashboard();
            }).show();
        });
    }

    private void navigateToDashboard() {
        Intent intent = new Intent(AdminPunchActivity.this, AdminDashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private void showPermissionDeniedDialog() {
        if (isFinishing() || isDestroyed()) return;

        new AlertDialog.Builder(this).setTitle("Camera Permission Needed").setMessage("Camera permission is required to use this feature.").setPositiveButton("OK", (dialog, which) -> finish()).setCancelable(false).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
        handler.removeCallbacks(captureRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
        handler.removeCallbacks(captureRunnable); // cancel to prevent leak
    }
}

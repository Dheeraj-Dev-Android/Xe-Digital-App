package app.xedigital.ai.admin.adminUI.employeeDetails;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import app.xedigital.ai.R;
import app.xedigital.ai.admin.adminModal.EmployeeDetails.EmployeesItem;
import app.xedigital.ai.utills.CustomDialogHelper;
import app.xedigital.ai.utills.FaceOverlayView;
import app.xedigital.ai.utills.PermissionGuard;
import app.xedigital.ai.utills.PermissionManager;
import app.xedigital.ai.utills.SecurePrefManager;

@OptIn(markerClass = ExperimentalGetImage.class)
public class AddFaceFragment extends Fragment {

    private static final String TAG = "AddFaceFragment";
    private static final String ARG_EMPLOYEE = "selected_employee";

    private static final int[] AVATAR_COLORS = {0xFF1E88E5, 0xFF43A047, 0xFFE53935, 0xFF8E24AA, 0xFFFB8C00, 0xFF00ACC1, 0xFF3949AB, 0xFFD81B60, 0xFF00897B, 0xFF6D4C41};

    private EmployeesItem employee;

    private LinearLayout cameraStateLayout;
    private PreviewView previewView;
    private FaceOverlayView faceOverlay;
    private TextView tvInstruction, tvFaceStatus, tvLighting;
    private ImageView btnCapture, btnCloseCamera, btnFlipCamera;

    private NestedScrollView previewStateLayout;
    private ImageView ivCapturedPhoto, btnClosePreview;
    private TextView tvEmployeeInfo, tvEmployeeSubInfo, tvEmployeeAvatar;
    private LinearLayout btnAddFace, btnAddProfile;
    private MaterialButton btnRetake, btnAddBoth;

    private ProcessCameraProvider cameraProvider;
    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;
    private FaceDetector faceDetector;
    private boolean isCaptureEnabled = false;
    private boolean isFrontCamera = true;
    private final ActivityResultLauncher<String> permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
        PermissionGuard.verify(requireContext(), PermissionManager.TAG_CAMERA, isGranted, this::startCamera, () -> PermissionGuard.showRationaleDialog(requireActivity(), PermissionManager.TAG_CAMERA, () -> PermissionGuard.openAppSettings(requireContext())));
    });
    private AddFaceViewModel viewModel;
    private AlertDialog loadingDialog;
    private File capturedImageFile;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            employee = (EmployeesItem) getArguments().getSerializable(ARG_EMPLOYEE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_face, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(AddFaceViewModel.class);

        cameraStateLayout = view.findViewById(R.id.cameraStateLayout);
        previewView = view.findViewById(R.id.previewView);
        faceOverlay = view.findViewById(R.id.faceOverlay);
        tvInstruction = view.findViewById(R.id.tvInstruction);
        tvFaceStatus = view.findViewById(R.id.tvFaceStatus);
        tvLighting = view.findViewById(R.id.tvLighting);
        btnCapture = view.findViewById(R.id.btnCapture);
        btnCloseCamera = view.findViewById(R.id.btnCloseCamera);
        btnFlipCamera = view.findViewById(R.id.btnFlipCamera);

        previewStateLayout = view.findViewById(R.id.previewStateLayout);
        ivCapturedPhoto = view.findViewById(R.id.ivCapturedPhoto);
        btnClosePreview = view.findViewById(R.id.btnClosePreview);
        tvEmployeeInfo = view.findViewById(R.id.tvEmployeeInfo);
        tvEmployeeSubInfo = view.findViewById(R.id.tvEmployeeSubInfo);
        tvEmployeeAvatar = view.findViewById(R.id.tvEmployeeAvatar);
        btnRetake = view.findViewById(R.id.btnRetake);
        btnAddFace = view.findViewById(R.id.btnAddFace);
        btnAddProfile = view.findViewById(R.id.btnAddProfile);
        btnAddBoth = view.findViewById(R.id.btnAddBoth);

        populateEmployeeInfo();

        cameraExecutor = Executors.newSingleThreadExecutor();

        FaceDetectorOptions options = new FaceDetectorOptions.Builder().setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST).setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE).setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE).setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE).build();
        faceDetector = FaceDetection.getClient(options);

        btnCloseCamera.setOnClickListener(v -> navigateBack());
        btnClosePreview.setOnClickListener(v -> navigateBack());
        btnCapture.setOnClickListener(v -> capturePhoto());
        btnRetake.setOnClickListener(v -> retakePhoto());
        btnFlipCamera.setOnClickListener(v -> flipCamera());

        btnAddFace.setOnClickListener(v -> triggerAddFace());
        btnAddProfile.setOnClickListener(v -> triggerAddProfile());
        btnAddBoth.setOnClickListener(v -> triggerAddBoth());

        observeViewModel();

        PermissionGuard.require(this, PermissionManager.TAG_CAMERA, permissionLauncher, this::startCamera);
    }

    private void triggerAddFace() {
        if (!validateCaptureAndEmployee()) return;
        SecurePrefManager prefs = SecurePrefManager.getInstance(requireContext());
        viewModel.uploadFace(capturedImageFile, prefs.getString("collectionName", ""), prefs.getString("authToken", ""), employee);
    }

    private void triggerAddProfile() {
        if (!validateCaptureAndEmployee()) return;
        SecurePrefManager prefs = SecurePrefManager.getInstance(requireContext());
        viewModel.uploadProfileImage(capturedImageFile, prefs.getString("authToken", ""), employee);
    }

    private void triggerAddBoth() {
        if (!validateCaptureAndEmployee()) return;
        SecurePrefManager prefs = SecurePrefManager.getInstance(requireContext());
        viewModel.uploadFaceAndProfile(capturedImageFile, prefs.getString("collectionName", ""), prefs.getString("authToken", ""), employee);
    }

    private boolean validateCaptureAndEmployee() {
        if (capturedImageFile == null) {
            CustomDialogHelper.showErrorDialog(requireContext(), "No Photo", "No photo captured. Please retake.", null);
            return false;
        }
        if (employee == null) {
            CustomDialogHelper.showErrorDialog(requireContext(), "Error", "Employee data is missing.", null);
            return false;
        }
        return true;
    }

    private void observeViewModel() {
        viewModel.getUiState().observe(getViewLifecycleOwner(), state -> {
            if (state == null) return;
            switch (state.status) {
                case IDLE:
                    break;
                case LOADING:
                    showLoading();
                    setUploadButtonsEnabled(false);
                    break;
                case SUCCESS:
                    hideLoading();
                    handleSuccess();
                    break;
                case ERROR:
                    hideLoading();
                    setUploadButtonsEnabled(true);
                    handleError(state.errorMessage, state.isRetryable);
                    break;
            }
        });
    }

    private void handleSuccess() {
        if (capturedImageFile != null && capturedImageFile.exists()) {
            capturedImageFile.delete();
            capturedImageFile = null;
        }
        CustomDialogHelper.showSuccessDialog(requireContext(), "Success", "Data saved successfully for " + getDisplayName() + ".", "Okay", () -> {
            viewModel.resetState();
            navigateBack();
        });
    }

    private void handleError(String message, boolean isRetryable) {
        if (isRetryable) {
            CustomDialogHelper.showWarningDialog(requireContext(), "Upload Failed", message, "Retake", "Cancel", new CustomDialogHelper.OnWarningActionListener() {
                @Override
                public void onConfirm() {
                    viewModel.resetState();
                    retakePhoto();
                }

                @Override
                public void onCancel() {
                    viewModel.resetState();
                    navigateBack();
                }
            });
        } else {
            CustomDialogHelper.showErrorDialog(requireContext(), "Error", message, "Okay", () -> {
                viewModel.resetState();
                navigateBack();
            });
        }
    }

    private void showLoading() {
        if (loadingDialog == null || !loadingDialog.isShowing()) {
            loadingDialog = CustomDialogHelper.showLoadingDialog(requireContext(), "Uploading...");
        }
    }

    private void hideLoading() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            CustomDialogHelper.dismissWithAnimation(requireContext(), loadingDialog);
            loadingDialog = null;
        }
    }

    private void setUploadButtonsEnabled(boolean enabled) {
        if (btnAddFace != null) btnAddFace.setEnabled(enabled);
        if (btnAddProfile != null) btnAddProfile.setEnabled(enabled);
        if (btnAddBoth != null) btnAddBoth.setEnabled(enabled);
        if (btnRetake != null) btnRetake.setEnabled(enabled);
        if (btnClosePreview != null) btnClosePreview.setEnabled(enabled);
    }

    private String getDisplayName() {
        if (employee == null) return "this employee";
        String fn = employee.getFirstname() != null ? employee.getFirstname() : "";
        String ln = employee.getLastname() != null ? employee.getLastname() : "";
        String name = (fn + " " + ln).trim();
        return name.isEmpty() ? "this employee" : name;
    }

    private void populateEmployeeInfo() {
        if (employee == null) {
            tvEmployeeInfo.setText("N/A");
            tvEmployeeSubInfo.setText("");
            tvEmployeeAvatar.setText("?");
            return;
        }
        String firstName = employee.getFirstname() != null ? employee.getFirstname() : "";
        String lastName = employee.getLastname() != null ? employee.getLastname() : "";
        String fullName = (firstName + " " + lastName).trim();
        if (fullName.isEmpty()) fullName = "N/A";

        tvEmployeeInfo.setText(fullName);

        StringBuilder subInfo = new StringBuilder();
        String empCode = employee.getEmployeeCode();
        String designation = employee.getDesignation();
        if (empCode != null && !empCode.trim().isEmpty()) subInfo.append(empCode);
        if (designation != null && !designation.trim().isEmpty()) {
            if (subInfo.length() > 0) subInfo.append(" • ");
            subInfo.append(designation);
        }
        tvEmployeeSubInfo.setText(subInfo.length() > 0 ? subInfo.toString() : "Employee");

        String initials = getInitials(fullName);
        int avatarColor = getAvatarColor(fullName);
        tvEmployeeAvatar.setText(initials);
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);
        bg.setColor(avatarColor);
        tvEmployeeAvatar.setBackground(bg);
    }


    private String getInitials(String name) {
        if (name == null || name.trim().isEmpty() || name.equalsIgnoreCase("N/A")) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1)
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        return ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
    }

    private int getAvatarColor(String name) {
        if (name == null || name.trim().isEmpty()) return AVATAR_COLORS[0];
        return AVATAR_COLORS[Math.abs(name.trim().hashCode()) % AVATAR_COLORS.length];
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> f = ProcessCameraProvider.getInstance(requireContext());
        f.addListener(() -> {
            try {
                cameraProvider = f.get();
                bindCameraUseCases();
            } catch (Exception e) {
                Log.e(TAG, "Camera start failed", e);
                Toast.makeText(requireContext(), "Failed to start camera", Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void bindCameraUseCases() {
        if (cameraProvider == null) return;
        try {
            Preview preview = new Preview.Builder().build();
            preview.setSurfaceProvider(previewView.getSurfaceProvider());
            imageCapture = new ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).setTargetResolution(new Size(1280, 720)).build();
            ImageAnalysis imageAnalysis = new ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();
            imageAnalysis.setAnalyzer(cameraExecutor, this::analyzeFrame);
            CameraSelector selector = isFrontCamera ? CameraSelector.DEFAULT_FRONT_CAMERA : CameraSelector.DEFAULT_BACK_CAMERA;
            cameraProvider.unbindAll();
            cameraProvider.bindToLifecycle(getViewLifecycleOwner(), selector, preview, imageCapture, imageAnalysis);
        } catch (Exception e) {
            Log.e(TAG, "Camera bind failed", e);
        }
    }

    private void flipCamera() {
        isFrontCamera = !isFrontCamera;
        setCaptureEnabled(false);
        faceOverlay.setFaceDetected(false);
        tvFaceStatus.setText("Switching camera...");
        tvFaceStatus.setTextColor(0xFFFFD54F);
        bindCameraUseCases();
    }

    private void analyzeFrame(@NonNull ImageProxy imageProxy) {
        if (imageProxy.getImage() == null) {
            imageProxy.close();
            return;
        }
        InputImage inputImage = InputImage.fromMediaImage(imageProxy.getImage(), imageProxy.getImageInfo().getRotationDegrees());
        faceDetector.process(inputImage).addOnSuccessListener(faces -> {
            if (isAdded()) requireActivity().runOnUiThread(() -> updateFaceStatus(faces));
        }).addOnFailureListener(e -> {
            if (isAdded()) requireActivity().runOnUiThread(() -> {
                tvFaceStatus.setText("Detection error");
                tvFaceStatus.setTextColor(0xFFFF5252);
                faceOverlay.setFaceDetected(false);
                setCaptureEnabled(false);
            });
        }).addOnCompleteListener(task -> imageProxy.close());
    }

    private void updateFaceStatus(List<Face> faces) {
        int c = faces.size();
        if (c == 0) {
            tvFaceStatus.setText("No face detected");
            tvFaceStatus.setTextColor(0xFFFF5252);
            faceOverlay.setFaceDetected(false);
            setCaptureEnabled(false);
        } else if (c == 1) {
            tvFaceStatus.setText("✓ Face detected — Hold still");
            tvFaceStatus.setTextColor(0xFF4CAF50);
            faceOverlay.setFaceDetected(true);
            setCaptureEnabled(true);
        } else {
            tvFaceStatus.setText("Multiple faces — only one allowed");
            tvFaceStatus.setTextColor(0xFFFF9800);
            faceOverlay.setFaceDetected(false);
            setCaptureEnabled(false);
        }
    }

    private void setCaptureEnabled(boolean enabled) {
        isCaptureEnabled = enabled;
        btnCapture.setEnabled(enabled);
        btnCapture.setBackgroundResource(enabled ? R.drawable.bg_capture_button : R.drawable.bg_capture_button_disabled);
    }

    private void capturePhoto() {
        if (imageCapture == null || !isCaptureEnabled) return;
        btnCapture.setEnabled(false);
        tvFaceStatus.setText("Capturing...");
        capturedImageFile = new File(requireContext().getCacheDir(), "face_" + System.currentTimeMillis() + ".jpg");
        imageCapture.takePicture(new ImageCapture.OutputFileOptions.Builder(capturedImageFile).build(), ContextCompat.getMainExecutor(requireContext()), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults r) {
                if (isAdded()) showPreview();
            }

            @Override
            public void onError(@NonNull ImageCaptureException e) {
                if (isAdded()) {
                    setCaptureEnabled(true);
                    Toast.makeText(requireContext(), "Capture failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showPreview() {
        if (capturedImageFile != null && capturedImageFile.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(capturedImageFile.getAbsolutePath());
            if (bitmap != null) {
                if (isFrontCamera) {
                    Matrix m = new Matrix();
                    m.preScale(-1f, 1f);
                    ivCapturedPhoto.setImageBitmap(Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true));
                } else {
                    ivCapturedPhoto.setImageBitmap(bitmap);
                }
            }
        }
        cameraStateLayout.setVisibility(View.GONE);
        previewStateLayout.setVisibility(View.VISIBLE);
    }

    private void retakePhoto() {
        if (capturedImageFile != null && capturedImageFile.exists()) {
            capturedImageFile.delete();
            capturedImageFile = null;
        }
        ivCapturedPhoto.setImageBitmap(null);
        previewStateLayout.setVisibility(View.GONE);
        cameraStateLayout.setVisibility(View.VISIBLE);
        setCaptureEnabled(false);
        faceOverlay.setFaceDetected(false);
        tvFaceStatus.setText("Detecting face...");
        tvFaceStatus.setTextColor(0xFFFFD54F);
        setUploadButtonsEnabled(true);
    }

    private void navigateBack() {
        hideLoading();
        if (cameraExecutor != null && !cameraExecutor.isShutdown()) cameraExecutor.shutdown();
        if (faceDetector != null) faceDetector.close();
        if (getView() != null) Navigation.findNavController(getView()).popBackStack();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        hideLoading();
        if (cameraExecutor != null && !cameraExecutor.isShutdown()) cameraExecutor.shutdown();
        if (faceDetector != null) faceDetector.close();
    }
}
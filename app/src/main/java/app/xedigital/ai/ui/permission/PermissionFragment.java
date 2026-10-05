package app.xedigital.ai.ui.permission;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import app.xedigital.ai.R;
import app.xedigital.ai.utills.BioMetric;
import app.xedigital.ai.utills.PermissionGuard;
import app.xedigital.ai.utills.PermissionManager;

public class PermissionFragment extends Fragment {

    private PermissionViewModel mViewModel;
    private RecyclerView recyclerView;
    private BioMetric bioMetric;
    private PermissionManager permissionManager;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                // After ANY system permission dialog, re-sync everything
                permissionManager.syncAllPermissions();
                mViewModel.refreshAll();
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_permission, container, false);
        recyclerView = view.findViewById(R.id.permissionRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        permissionManager = PermissionManager.getInstance(requireContext());
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(PermissionViewModel.class);

        mViewModel.getPermissions().observe(getViewLifecycleOwner(), items -> {
            PermissionAdapter adapter = new PermissionAdapter(items, this::handlePermissionClick);
            recyclerView.setAdapter(adapter);
        });
    }

    private void handlePermissionClick(PermissionItem item) {
        String tag = item.getTag();

        // ── BIOMETRIC (App-level toggle) ──
        if (PermissionManager.TAG_BIOMETRIC.equals(tag)) {
            if (item.isGranted()) {
                showDisableBiometricDialog();
            } else {
                triggerBiometricActivation();
            }
            return;
        }

        // ── BACKGROUND LOCATION (Special flow) ──
        if (PermissionManager.TAG_BACKGROUND_LOCATION.equals(tag)) {
            if (!item.isGranted()) {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Background Location Access")
                        .setMessage("To track attendance accurately when the app is closed, " +
                                "please set location access to 'Allow all the time' in the next screen.")
                        .setPositiveButton("Go to Settings", (d, w) -> PermissionGuard.openAppSettings(requireContext()))
                        .setNegativeButton("Cancel", null)
                        .show();
            } else {
                PermissionGuard.openAppSettings(requireContext());
            }
            return;
        }

        // ── INTERNET (Informational — no action) ──
        if (PermissionManager.TAG_INTERNET.equals(tag)) {
            Toast.makeText(getContext(), "Internet is always available.", Toast.LENGTH_SHORT).show();
            return;
        }

        // ── ALL OTHER SYSTEM PERMISSIONS ──
        if (item.getManifestPermission() == null) return;

        if (!item.isGranted()) {
            // Request via the centralized launcher
            requestPermissionLauncher.launch(item.getManifestPermission());
        } else {
            // Already granted → open settings if user wants to revoke
            PermissionGuard.openAppSettings(requireContext());
        }
    }

    private void triggerBiometricActivation() {
        bioMetric = new BioMetric(requireContext(), requireActivity(),
                new BioMetric.BiometricAuthListener() {
                    @Override
                    public void onAuthenticationSucceeded() {
                        if (isAdded()) {
                            requireActivity().runOnUiThread(() -> {
                                mViewModel.toggleAppPermission(PermissionManager.TAG_BIOMETRIC, true);
                                Toast.makeText(getContext(), "Biometric Login Enabled!", Toast.LENGTH_SHORT).show();
                            });
                        }
                    }

                    @Override
                    public void onAuthenticationError(int errorCode, CharSequence errString) {
                        if (isAdded()) {
                            requireActivity().runOnUiThread(() ->
                                    Toast.makeText(getContext(), "Error: " + errString, Toast.LENGTH_SHORT).show());
                        }
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        if (isAdded()) {
                            requireActivity().runOnUiThread(() ->
                                    Toast.makeText(getContext(), "Authentication Failed", Toast.LENGTH_SHORT).show());
                        }
                    }
                });
        bioMetric.authenticate(true);
    }

    private void showDisableBiometricDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Disable Biometric Login?")
                .setMessage("You will need to enter your password manually next time.")
                .setPositiveButton("Disable", (d, w) -> {
                    mViewModel.toggleAppPermission(PermissionManager.TAG_BIOMETRIC, false);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Always re-sync when user comes back (e.g., from Settings)
        if (mViewModel != null) {
            mViewModel.refreshAll();
        }
    }
}
package app.xedigital.ai.ui.permission;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import app.xedigital.ai.R;
import app.xedigital.ai.utills.BioMetric;

public class PermissionFragment extends Fragment {

    private PermissionViewModel mViewModel;
    private RecyclerView recyclerView;
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                refreshPermissionStatus();
            });
    private BioMetric bioMetric;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_permission, container, false);
        recyclerView = view.findViewById(R.id.permissionRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(PermissionViewModel.class);

        mViewModel.getPermissions().observe(getViewLifecycleOwner(), items -> {
            PermissionAdapter adapter = new PermissionAdapter(items, this::handlePermissionClick);
            recyclerView.setAdapter(adapter);
            refreshPermissionStatus();
        });
    }

    private void handlePermissionClick(PermissionItem item) {
        if ("BIOMETRIC".equals(item.getTag())) {
            if (item.isGranted()) {
                // If already on, show the dialog to disable it
                showDisableBiometricDialog();
            } else {
                // If off, trigger the scan to enable it right here!
                triggerBiometricActivation();
            }
            return;
        }

        // Standard logic for Camera/Location remains same
        if (item.getManifestPermission() == null) return;
        if (!item.isGranted()) {
            requestPermissionLauncher.launch(item.getManifestPermission());
        } else {
            openAppSettings();
        }
    }

    private void triggerBiometricActivation() {
        bioMetric = new BioMetric(requireContext(), requireActivity(), new BioMetric.BiometricAuthListener() {
            @Override
            public void onAuthenticationSucceeded() {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        SharedPreferences prefs = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                        prefs.edit().putBoolean("isBioEnabled", true).apply();

                        refreshPermissionStatus();
                        Toast.makeText(getContext(), "Biometric Login Enabled!", Toast.LENGTH_SHORT).show();
                    });
                }
            }

            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Error: " + errString, Toast.LENGTH_SHORT).show()
                    );
                }
            }

            @Override
            public void onAuthenticationFailed() {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Authentication Failed", Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });

        bioMetric.authenticate(true);
    }

    private void showDisableBiometricDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Disable Biometric Login?")
                .setMessage("You will need to enter your password manually next time you log in.")
                .setPositiveButton("Disable", (dialog, which) -> {
                    SharedPreferences prefs = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                    prefs.edit().putBoolean("isBioEnabled", false).apply();
                    refreshPermissionStatus();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void refreshPermissionStatus() {
        List<PermissionItem> currentList = mViewModel.getPermissions().getValue();
        SharedPreferences prefs = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        boolean isBioEnabled = prefs.getBoolean("isBioEnabled", false);

        if (currentList != null) {
            for (PermissionItem item : currentList) {
                // UI Sync for Biometric item
                if ("BIOMETRIC".equals(item.getTag())) {
                    item.setGranted(isBioEnabled);
                }
                // UI Sync for System items
                else if (item.getManifestPermission() != null) {
                    int status = ContextCompat.checkSelfPermission(requireContext(), item.getManifestPermission());
                    item.setGranted(status == PackageManager.PERMISSION_GRANTED);
                } else {
                    item.setGranted(true);
                }
            }
            if (recyclerView.getAdapter() != null) {
                recyclerView.getAdapter().notifyDataSetChanged();
            }
        }
    }

    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", requireContext().getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshPermissionStatus();
    }
}
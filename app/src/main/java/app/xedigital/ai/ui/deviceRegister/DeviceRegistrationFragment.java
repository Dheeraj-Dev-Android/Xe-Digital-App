package app.xedigital.ai.ui.deviceRegister;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import app.xedigital.ai.R;

public class DeviceRegistrationFragment extends Fragment {

    private static final String TAG = "DeviceRegFragment";

    private DeviceRegistrationViewModel viewModel;
    private ProgressBar progressBar;
    private TextView tvStatus;

    public static DeviceRegistrationFragment newInstance() {
        return new DeviceRegistrationFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.v(TAG, "🟢 onCreateView() - Inflating Fragment Layout");
        return inflater.inflate(R.layout.fragment_device_registration, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated() - Binding Views & Initializing ViewModel");

        progressBar = view.findViewById(R.id.progressBar);
        tvStatus = view.findViewById(R.id.tvStatus);

        viewModel = new ViewModelProvider(this).get(DeviceRegistrationViewModel.class);

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
            Log.d(TAG, "🔄 [UI Observation] Loading state changed to: " + loading);
            if (progressBar != null) {
                progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
            }
            if (tvStatus != null) {
                tvStatus.setText(loading ? "Verifying authorized device binding..." : "");
            }
        });

        viewModel.getSuccessMessage().observe(getViewLifecycleOwner(), message -> {
            if (message == null) return;
            Log.i(TAG, "🎉 [UI Observation] SUCCESS SIGNAL: " + message);
            if (tvStatus != null) {
                tvStatus.setText(message);
            }
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), message -> {
            if (message == null) return;
            Log.e(TAG, "💥 [UI Observation] ERROR SIGNAL: " + message);
            if (tvStatus != null) {
                tvStatus.setText(message);
            }
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
        });

        viewModel.getIsDeviceBlocked().observe(getViewLifecycleOwner(), isBlocked -> {
            if (Boolean.TRUE.equals(isBlocked)) {
                String reason = viewModel.getBlockedReason().getValue();
                Log.w(TAG, "🚨 [UI Observation] Lockout Triggered: " + reason);

                if (tvStatus != null) {
                    tvStatus.setText("Access Denied.\n\n" + (reason != null ? reason : ""));
                    tvStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                }
            }
        });

        Log.d(TAG, "Triggering validateAndRegisterDevice() on ViewModel...");
        viewModel.validateAndRegisterDevice();
    }
}
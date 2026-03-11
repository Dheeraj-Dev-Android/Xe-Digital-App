package app.xedigital.ai.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.material.card.MaterialCardView;

import app.xedigital.ai.R;

public class SettingsFragment extends Fragment {

    private SettingsViewModel mViewModel;

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize ViewModel
        mViewModel = new ViewModelProvider(this).get(SettingsViewModel.class);

        // Find the CardViews by ID
        MaterialCardView cardPermissions = view.findViewById(R.id.card_permissions);
        MaterialCardView cardAppInfo = view.findViewById(R.id.card_app_info);

        // Set Click Listener for Permissions
        cardPermissions.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_nav_settings_to_nav_permission);
        });

        // Set Click Listener for About Us/App Info
        cardAppInfo.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_nav_settings_to_nav_about_us);
        });
    }
}
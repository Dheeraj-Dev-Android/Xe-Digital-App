package app.xedigital.ai.ui.about_application;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import app.xedigital.ai.R;

public class AboutApplicationFragment extends Fragment {

    private TextView appName;
    private TextView companyName;
    private TextView appVersion;
    private TextView lastUpdated;
    private TextView appDescription;
    private TextView appTagline;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_about_application, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        appName = view.findViewById(R.id.app_name);
        companyName = view.findViewById(R.id.company_name);
        appVersion = view.findViewById(R.id.app_version);
        lastUpdated = view.findViewById(R.id.last_updated);
        appDescription = view.findViewById(R.id.app_description);
        appTagline = view.findViewById(R.id.app_tagline);

        // Populate data
        populateAppInfo();
    }

    private void populateAppInfo() {
        try {
            PackageManager packageManager = requireContext().getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(requireContext().getPackageName(), 0);

            // Set app name
            appName.setText(getString(R.string.app_name));

            // Set company name
            companyName.setText("ConsultEdge.Global");

            // Set tagline
            appTagline.setText("Transforming Ideas into Possibilities");

            // Set version
//            appVersion.setText(packageInfo.versionName);
            String versionName = packageInfo.versionName;

            // Set build number
            long buildNumber;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                buildNumber = packageInfo.getLongVersionCode();
            } else {
                // Suppress the deprecation warning for older APIs
                @SuppressWarnings("deprecation")
                long legacyVersionCode = packageInfo.versionCode;
                buildNumber = legacyVersionCode;
            }

            String fullVersionInfo = "v" + buildNumber + "." + versionName;
            appVersion.setText(fullVersionInfo);
            // Set last updated date
            long lastUpdateTime = packageInfo.lastUpdateTime;
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
            lastUpdated.setText(dateFormat.format(new Date(lastUpdateTime)));

            // Set description
            appDescription.setText("XeDigital.ai: Your All-in-One AI-Powered HRMS & VMS Streamline your workplace with XeDigital.ai. From AI-driven recruitment and touchless visitor management to automated payroll and selfie-based attendance, we provide a unified platform to manage your entire workforce and guest identity with ease.");

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }
}
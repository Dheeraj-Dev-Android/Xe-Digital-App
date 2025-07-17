package app.xedigital.ai;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.analytics.FirebaseAnalytics;

import app.xedigital.ai.activity.LoginSelectionActivity;
import app.xedigital.ai.databinding.ActivityAdminMainBinding;
import app.xedigital.ai.databinding.NoInternetConnectionBinding;
import app.xedigital.ai.databinding.SlowInternetConnectionBinding;
import app.xedigital.ai.utills.NetworkUtils;

public class AdminMainActivity extends AppCompatActivity {

    private static final String TAG = "AdminMainActivity";
    private final NetworkChangeReceiver networkChangeReceiver = new NetworkChangeReceiver();
    private ActivityAdminMainBinding binding;
    private AppBarConfiguration mAppBarConfiguration;
    private NavController navController;
    private FirebaseAnalytics mFirebaseAnalytics;
    private boolean isNetworkChangeReceiverRegistered = false;
    private boolean isVisitorSubMenuVisible = false;

    private SlowInternetConnectionBinding slowInternetBinding;
    private NoInternetConnectionBinding noInternetBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        // Inflate main layout
        binding = ActivityAdminMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Bind included layouts
        slowInternetBinding = SlowInternetConnectionBinding.bind(binding.getRoot().findViewById(R.id.slowInternetLayout));
        noInternetBinding = NoInternetConnectionBinding.bind(binding.getRoot().findViewById(R.id.noInternetLayout));

        // Firebase Analytics
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        // Setup toolbar
        setSupportActionBar(binding.adminAppBarMain.adminToolbar);

        // Setup navigation
        DrawerLayout drawer = binding.adminDrawerLayout;
        NavigationView navigationView = binding.adminNavView;
        navController = Navigation.findNavController(this, R.id.admin_nav_host_fragment_content_main);

//        mAppBarConfiguration = new AppBarConfiguration.Builder(R.id.nav_admin_dashboard, R.id.nav_visitorCheckInFragment, R.id.nav_logout).setOpenableLayout(drawer).build();

        mAppBarConfiguration = new AppBarConfiguration.Builder(R.id.nav_admin_dashboard, R.id.nav_visitorCheckInFragment, R.id.nav_employees, R.id.nav_partners, R.id.nav_users, R.id.nav_logout).setOpenableLayout(drawer).build();

        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // Handle dismiss slow internet layout
        slowInternetBinding.btnDismiss.setOnClickListener(v -> slowInternetBinding.slowInternetContainer.setVisibility(View.GONE));

        // Menu item listeners
        navigationView.getMenu().findItem(R.id.nav_logout).setOnMenuItemClickListener(item -> {
            handleLogout();
            return true;
        });

        // Digital Identity main toggle item
        MenuItem digitalIdentityGroup = navigationView.getMenu().findItem(R.id.nav_digital_identity);
        digitalIdentityGroup.setOnMenuItemClickListener(item -> {
            toggleVisitorSubMenuVisibility(navigationView.getMenu());
            return true;
        });
        // Set all sub-items initially hidden (if needed)
        setVisitorSubMenuVisibility(navigationView.getMenu(), false);

        navigationView.getMenu().findItem(R.id.nav_visitorCheckInFragment).setOnMenuItemClickListener(item -> {
            navController.navigate(R.id.nav_visitorCheckInFragment);
            return true;
        });

        navigationView.getMenu().findItem(R.id.nav_employees).setOnMenuItemClickListener(item -> {
            navController.navigate(R.id.nav_employees);
            return true;
        });
        navigationView.getMenu().findItem(R.id.nav_admin_dashboard).setOnMenuItemClickListener(item -> {
            navController.navigate(R.id.nav_admin_dashboard);

            return true;
        });

        navigationView.getMenu().findItem(R.id.nav_partners).setOnMenuItemClickListener(item -> {
            navController.navigate(R.id.nav_partners);
            return true;
        });

        navigationView.getMenu().findItem(R.id.nav_users).setOnMenuItemClickListener(item -> {
            navController.navigate(R.id.nav_users);
            return true;
        });

        if (navigationView != null) {
            fetchUserProfileData();
        } else {
            Log.e(TAG, "Navigation view is null, cannot fetch user profile");
        }
    }

    private void setVisitorSubMenuVisibility(Menu menu, boolean visible) {
        menu.findItem(R.id.nav_visitorCheckInFragment).setVisible(visible);
        menu.findItem(R.id.nav_employees).setVisible(visible);
        menu.findItem(R.id.nav_partners).setVisible(visible);
        menu.findItem(R.id.nav_users).setVisible(visible);

        MenuItem parentItem = menu.findItem(R.id.nav_digital_identity);
        parentItem.setIcon(visible ? R.drawable.ic_dropdown_adaptive_fore : R.drawable.ic_dropdown_adaptive_back);
        isVisitorSubMenuVisible = visible;
    }


    // Internet UI Handlers
    public void showNoInternetLayout() {
        noInternetBinding.getRoot().setVisibility(View.VISIBLE);
    }

    public void hideNoInternetLayout() {
        noInternetBinding.getRoot().setVisibility(View.GONE);
    }

    public void hideSlowInternetLayout() {
        slowInternetBinding.slowInternetContainer.setVisibility(View.GONE);
    }


    public void showSlowInternetLayout(double speed) {
        slowInternetBinding.slowInternetContainer.setVisibility(View.VISIBLE);
        String speedText = String.format("Current Speed: %.2f Mbps", speed / 1000);
        slowInternetBinding.tvSpeed.setText(speedText);
    }

//    private void toggleVisitorSubMenuVisibility(Menu menu) {
//        MenuItem visitorItem = menu.findItem(R.id.visitor_submenu);
//        boolean newVisibility = !isVisitorSubMenuVisible;
//        menu.findItem(R.id.nav_check_in_visitor).setVisible(newVisibility);
//
//        visitorItem.setIcon(newVisibility ? R.drawable.ic_dropdown_adaptive_fore : R.drawable.ic_dropdown_adaptive_back);
//        binding.adminNavView.invalidate();
//        isVisitorSubMenuVisible = newVisibility;
//    }

    private void toggleVisitorSubMenuVisibility(Menu menu) {
        boolean newVisibility = !isVisitorSubMenuVisible;
        setVisitorSubMenuVisibility(menu, newVisibility);
        binding.adminNavView.invalidate();
    }

    private void handleLogout() {
        SharedPreferences sharedPreferences = getSharedPreferences("AdminCred", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("authToken");
        editor.apply();

        Intent intent = new Intent(this, LoginSelectionActivity.class);
        startActivity(intent);
        finish();
    }

    private void fetchUserProfileData() {
        // TODO: Add real data fetching logic
    }

    public void onRetryButtonClicked(View view) {
        if (NetworkUtils.isNetworkAvailable(this)) {
            hideNoInternetLayout();
        } else {
            Toast.makeText(this, "Still no internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    public void onOpenSettingsButtonClicked(View view) {
        startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isNetworkChangeReceiverRegistered) {
            IntentFilter filter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
            registerReceiver(networkChangeReceiver, filter);
            isNetworkChangeReceiverRegistered = true;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterNetworkReceiver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterNetworkReceiver();
    }

    private void unregisterNetworkReceiver() {
        if (isNetworkChangeReceiverRegistered) {
            try {
                unregisterReceiver(networkChangeReceiver);
                isNetworkChangeReceiverRegistered = false;
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Error unregistering receiver: " + e.getMessage());
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, mAppBarConfiguration) || super.onSupportNavigateUp();
    }
}

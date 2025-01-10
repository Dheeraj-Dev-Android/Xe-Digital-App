package app.xedigital.ai;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
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

import app.xedigital.ai.activity.LoginActivity;
import app.xedigital.ai.activity.PunchActivity;
import app.xedigital.ai.databinding.ActivityMainBinding;
import app.xedigital.ai.utills.NetworkUtils;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_PUNCH_ACTIVITY = 1;
    private final NetworkChangeReceiver networkChangeReceiver = new NetworkChangeReceiver();
    private final boolean isShowingNoInternetDialog = false;
    private final boolean isShowingSlowNetworkDialog = false;
    private AppBarConfiguration mAppBarConfiguration;
    private NavController navController;
    private AlertDialog noInternetDialog;
    private AlertDialog slowNetworkDialog;
    private boolean isAttendanceSubmenuVisible = false;
    private boolean isLeavesSubmenuVisible = false;
    private boolean isDcrSubmenuVisible = false;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        noInternetDialog = new AlertDialog.Builder(this).setTitle("No Internet Connection").setMessage("Please check your internet connection and try again.").setPositiveButton("OK", (dialog, which) -> finish()).setCancelable(false).create();

        // Create the slow network alert dialog
        slowNetworkDialog = new AlertDialog.Builder(this).setTitle("Slow Network Connection").setMessage("Your network connection is slow. Some features might be affected.").setPositiveButton("OK", null).setCancelable(true).create();

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.appBarMain.toolbar);

        DrawerLayout drawer = binding.drawerLayout;
        navigationView = binding.navView;

        mAppBarConfiguration = new AppBarConfiguration.Builder(R.id.nav_dashboard, R.id.nav_profile, R.id.nav_attendance, R.id.nav_addAttendanceFragment, R.id.nav_regularizeAppliedFragment, R.id.nav_claim_management, R.id.nav_dcr, R.id.nav_documents, R.id.nav_holidays, R.id.nav_leaves, R.id.nav_applied_leaves, R.id.nav_payroll, R.id.nav_policy, R.id.nav_shifts, R.id.nav_vms, R.id.nav_logout).setOpenableLayout(drawer).build();

        navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        navigationView.getMenu().findItem(R.id.nav_logout).setOnMenuItemClickListener(MenuItem -> {
            handleLogout();
            return true;
        });

        // Set click listener for attendance menu item
        MenuItem attendanceItem = navigationView.getMenu().findItem(R.id.nav_attendance_menu);
        attendanceItem.setOnMenuItemClickListener(item -> {
            toggleAttendanceVisibility(navigationView.getMenu());
            return true;
        });

        MenuItem leavesItem = navigationView.getMenu().findItem(R.id.nav_leaves_menu);
        leavesItem.setOnMenuItemClickListener(item -> {
            toggleLeavesVisibility(navigationView.getMenu());
            return true;
        });

        MenuItem dcrItem = navigationView.getMenu().findItem(R.id.nav_dcr_menu);
        dcrItem.setOnMenuItemClickListener(item -> {
            toggleDcrVisibility(navigationView.getMenu());
            return true;
        });
    }

    public void showNoInternetLayout() {
        findViewById(R.id.noInternetLayout).setVisibility(View.VISIBLE);
    }

    public void hideNoInternetLayout() {
        findViewById(R.id.noInternetLayout).setVisibility(View.GONE);
    }

    public void showSlowInternetLayout() {
        findViewById(R.id.slowInternetLayout).setVisibility(View.VISIBLE);
    }

    public void hideSlowInternetLayout() {
        findViewById(R.id.slowInternetLayout).setVisibility(View.GONE);
    }

    private void toggleAttendanceVisibility(Menu menu) {
        MenuItem attendanceItem = menu.findItem(R.id.nav_attendance_menu);

        // Toggle visibility of submenu items
        boolean newVisibility = !isAttendanceSubmenuVisible;
        menu.findItem(R.id.nav_attendance).setVisible(newVisibility);
        menu.findItem(R.id.nav_addAttendanceFragment).setVisible(newVisibility);
        menu.findItem(R.id.nav_regularizeAppliedFragment).setVisible(newVisibility);
        menu.findItem(R.id.nav_pendingApprovalFragment).setVisible(newVisibility);

        // Change icon based on visibility
        attendanceItem.setIcon(newVisibility ? R.drawable.ic_dropdown_up_adaptive_fore : R.drawable.ic_dropdown_adaptive_fore);
        isAttendanceSubmenuVisible = newVisibility;

        // Refresh the navigation menu to reflect the changes
        navigationView.invalidate();
    }

    private void toggleLeavesVisibility(Menu menu) {
        MenuItem leavesItem = menu.findItem(R.id.nav_leaves_menu);

        // Toggle visibility of submenu items
        boolean newVisibility = !isLeavesSubmenuVisible;
        menu.findItem(R.id.nav_leaves).setVisible(newVisibility);
        menu.findItem(R.id.nav_leaves_data).setVisible(newVisibility);
        menu.findItem(R.id.nav_applied_leaves).setVisible(newVisibility);
        menu.findItem(R.id.nav_approve_leaves).setVisible(newVisibility);

        // Change icon based on visibility
        leavesItem.setIcon(newVisibility ? R.drawable.ic_dropdown_up_adaptive_fore : R.drawable.ic_dropdown_adaptive_fore);
        isLeavesSubmenuVisible = newVisibility;

        // Refresh the navigation menu to reflect the changes
        navigationView.invalidate();
    }

    private void toggleDcrVisibility(Menu menu) {
        MenuItem dcrItem = menu.findItem(R.id.nav_dcr_menu);

        // Toggle visibility of submenu items
        boolean newVisibility = !isDcrSubmenuVisible;
        menu.findItem(R.id.nav_dcr).setVisible(newVisibility);
        menu.findItem(R.id.nav_dcr_form).setVisible(newVisibility);

        // Change icon based on visibility
        dcrItem.setIcon(newVisibility ? R.drawable.ic_dropdown_up_adaptive_fore : R.drawable.ic_dropdown_adaptive_fore);
        isDcrSubmenuVisible = newVisibility;

        // Refresh the navigation menu to reflect the changes
        navigationView.invalidate();
    }



    private void handleLogout() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("authToken");
//        editor.putBoolean("isLoggedIn", false);
        editor.apply();

        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.main, menu);
//        return true;
//    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration) || super.onSupportNavigateUp();
    }

    public void startPunchActivity() {
        Intent intent = new Intent(this, PunchActivity.class);
        startActivityForResult(intent, REQUEST_CODE_PUNCH_ACTIVITY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PUNCH_ACTIVITY) {
            if (resultCode == Activity.RESULT_OK) {
                // Handle successful punch, navigate to a different fragment
                NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
                navController.navigate(R.id.nav_dashboard);
            } else {
                // You might want to check for specific result codes for different errors
                Toast.makeText(this, "Punch failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void onRetryButtonClicked(View view) {
        // Code to retry network operations
        if (NetworkUtils.isNetworkAvailable(this)) {
            hideNoInternetLayout();

        } else {

            Toast.makeText(this, "Still no internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    public void onOpenSettingsButtonClicked(View view) {
        // Code to open network settings
        startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(networkChangeReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));

        if (!NetworkUtils.isNetworkAvailable(this)) {
            showNoInternetLayout();
        } else if (!NetworkUtils.isNetworkSpeedGood(this)) {
            showSlowInternetLayout();
        } else {
            hideNoInternetLayout();
            hideSlowInternetLayout();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(networkChangeReceiver);
        hideNoInternetLayout();
        hideSlowInternetLayout();
    }
}
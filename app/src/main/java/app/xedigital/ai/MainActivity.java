package app.xedigital.ai;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
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

public class MainActivity extends AppCompatActivity {
    private AppBarConfiguration mAppBarConfiguration;
    private NavController navController;
    private static final int REQUEST_CODE_PUNCH_ACTIVITY = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.appBarMain.toolbar);

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        mAppBarConfiguration = new AppBarConfiguration.Builder(R.id.nav_dashboard, R.id.nav_profile, R.id.nav_attendance, R.id.nav_viewAttendanceFragment, R.id.nav_addAttendanceFragment, R.id.nav_regularizeAppliedFragment,
                R.id.nav_claim_management, R.id.nav_dcr, R.id.nav_documents, R.id.nav_holidays, R.id.nav_leaves,R.id.nav_applied_leaves, R.id.nav_payroll, R.id.nav_policy, R.id.nav_shifts, R.id.nav_logout).setOpenableLayout(drawer).build();

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

    private void toggleDcrVisibility(Menu menu) {
        MenuItem dcrItem = menu.findItem(R.id.nav_dcr_menu);
        boolean isDcrVisible = menu.findItem(R.id.nav_dcr).isVisible();
        // Toggle visibility of submenu items
        menu.findItem(R.id.nav_dcr).setVisible(!isDcrVisible);
        menu.findItem(R.id.nav_dcr_form).setVisible(!isDcrVisible);
        // Change icon based on visibility
        if (isDcrVisible) {
            dcrItem.setIcon(R.drawable.ic_dropdown_adaptive_fore);
        } else {
            dcrItem.setIcon(R.drawable.ic_dropdown_up_adaptive_fore);
        }

    }

    private void toggleLeavesVisibility(Menu menu) {
        MenuItem leavesItem = menu.findItem(R.id.nav_leaves_menu);
        boolean isLeavesVisible = menu.findItem(R.id.nav_leaves).isVisible();
        // Toggle visibility of submenu items
        menu.findItem(R.id.nav_leaves).setVisible(!isLeavesVisible);
        menu.findItem(R.id.nav_leaves_data).setVisible(!isLeavesVisible);
        menu.findItem(R.id.nav_applied_leaves).setVisible(!isLeavesVisible);
        // Change icon based on visibility
        if (isLeavesVisible) {
            leavesItem.setIcon(R.drawable.ic_dropdown_adaptive_fore);
        } else {
            leavesItem.setIcon(R.drawable.ic_dropdown_up_adaptive_fore);
        }
    }

    private void toggleAttendanceVisibility(Menu menu) {
        MenuItem attendanceItem = menu.findItem(R.id.nav_attendance_menu);
        boolean isAddAttendanceVisible = menu.findItem(R.id.nav_addAttendanceFragment).isVisible();
        // Toggle visibility of submenu items
        menu.findItem(R.id.nav_attendance).setVisible(!isAddAttendanceVisible);
        menu.findItem(R.id.nav_addAttendanceFragment).setVisible(!isAddAttendanceVisible);
        menu.findItem(R.id.nav_regularizeAppliedFragment).setVisible(!isAddAttendanceVisible);
        menu.findItem(R.id.nav_pendingApprovalFragment).setVisible(!isAddAttendanceVisible);
        // Change icon based on visibility
        if (isAddAttendanceVisible) {
            attendanceItem.setIcon(R.drawable.ic_dropdown_adaptive_fore);
        } else {
            attendanceItem.setIcon(R.drawable.ic_dropdown_up_adaptive_fore);
        }
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
                navController.navigate(R.id.nav_dashboard); // Replace with your target fragment ID
            } else {
                // Handle punch failure (e.g., show a message)
                // You might want to check for specific result codes for different errors
                Toast.makeText(this, "Punch failed", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
package app.xedigital.ai;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.provider.Settings;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.navigation.NavigationView;

import app.xedigital.ai.activity.LoginActivity;
import app.xedigital.ai.activity.PunchActivity;
import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.databinding.ActivityMainBinding;
import app.xedigital.ai.model.profile.UserProfileResponse;
import app.xedigital.ai.utills.NetworkUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_PUNCH_ACTIVITY = 1;
    private static final String TAG = "MainActivity";
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
    private boolean isNetworkChangeReceiverRegistered = false;
    private FrameLayout slowInternetContainer;
    private TextView tvSpeed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        noInternetDialog = new AlertDialog.Builder(this).setTitle("No Internet Connection").setMessage("Please check your internet connection and try again.").setPositiveButton("OK", (dialog, which) -> finish()).setCancelable(false).create();

        // Create the slow network alert dialog
        slowNetworkDialog = new AlertDialog.Builder(this).setTitle("Slow Network Connection").setMessage("Your network connection is slow. Some features might be affected.").setPositiveButton("OK", null).setCancelable(true).create();
        slowInternetContainer = findViewById(R.id.slowInternetContainer);
        View slowInternetLayout = findViewById(R.id.slowInternetLayout);
        tvSpeed = findViewById(R.id.tvSpeed);
        ImageButton dismissButton = findViewById(R.id.btnDismiss);
        if (dismissButton != null) {
            dismissButton.setOnClickListener(v -> {
                slowInternetLayout.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "Dismiss button clicked", Toast.LENGTH_SHORT).show();
            });
        }

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
        if (navigationView != null) {
            fetchUserProfileData();
        } else {
            Log.e(TAG, "Navigation view is null, cannot fetch user profile");
        }
    }

    public void showNoInternetLayout() {
        findViewById(R.id.noInternetLayout).setVisibility(View.VISIBLE);
    }

    public void hideNoInternetLayout() {
        findViewById(R.id.noInternetLayout).setVisibility(View.GONE);
    }

    public void showSlowInternetLayout(double speed) {
        slowInternetContainer.setVisibility(View.VISIBLE);
        String speedText = String.format("Current speed: %.2f Mbps", speed / 1000);
        tvSpeed.setText(speedText);
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

    private void fetchUserProfileData() {
        // Code to fetch user profile data
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId", "");
        String authToken = sharedPreferences.getString("authToken", "");
        // Make API call to fetch user profile data using userId and authToken
        // Update the UI with the fetched data
        View headerView = navigationView.getHeaderView(0);
        ImageView profileImage = findViewById(R.id.imageView);
        TextView profileName = findViewById(R.id.textView);
        TextView profileEmail = findViewById(R.id.subtitleText);
        fetchUserProfile(userId, authToken, profileImage, profileName, profileEmail);
    }

    private void fetchUserProfile(String userId, String authToken, ImageView profileImage, TextView profileName, TextView profileEmail) {
        String authHeaderValue = "jwt " + authToken;

        Call<UserProfileResponse> call = APIClient.getInstance().getUser().getUserProfile(userId, authHeaderValue);
        call.enqueue(new Callback<UserProfileResponse>() {
            @Override
            public void onResponse(@NonNull Call<UserProfileResponse> call, @NonNull Response<UserProfileResponse> response) {
                if (navigationView == null) {
                    Log.e(TAG, "Navigation view is null, cannot update user profile");
                    return;
                }
                View headerView = navigationView.getHeaderView(0);

                if (headerView == null) {
                    Log.e(TAG, "Header view is null, cannot update user profile");
                    return;
                }

                ImageView profileImage = headerView.findViewById(R.id.imageView);
                TextView profileName = headerView.findViewById(R.id.textView);
                TextView profileEmail = headerView.findViewById(R.id.subtitleText);
                if (response.isSuccessful() && response.body() != null) {
                    UserProfileResponse userProfileResponse = response.body();
                    if (userProfileResponse.isSuccess() && userProfileResponse.getData() != null && userProfileResponse.getData().getEmployee() != null) {
                        String firstName = userProfileResponse.getData().getEmployee().getFirstname();
                        String lastName = userProfileResponse.getData().getEmployee().getLastname();

                        String fullName = firstName + " " + lastName;
                        // Create a SpannableString
                        SpannableString spannableString = new SpannableString(fullName);

                        // Apply color to the first letter of the first name
                        if (!firstName.isEmpty()) {
                            spannableString.setSpan(new ForegroundColorSpan(Color.rgb(255, 165, 0)), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            spannableString.setSpan(new StyleSpan(Typeface.BOLD), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            spannableString.setSpan(new RelativeSizeSpan(1.3f), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }

                        // Apply color to the first letter of the last name
                        if (!lastName.isEmpty()) {
                            int lastNameStartIndex = firstName.length() + 1;
                            spannableString.setSpan(new ForegroundColorSpan(Color.rgb(255, 165, 0)), lastNameStartIndex, lastNameStartIndex + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            spannableString.setSpan(new StyleSpan(Typeface.BOLD), lastNameStartIndex, lastNameStartIndex + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            spannableString.setSpan(new RelativeSizeSpan(1.3f), lastNameStartIndex, lastNameStartIndex + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                        profileName.setText(spannableString);

//                        profileName.setText(userProfileResponse.getData().getEmployee().getFirstname() + " " + userProfileResponse.getData().getEmployee().getLastname());
                        profileEmail.setText(userProfileResponse.getData().getEmployee().getEmail());
                        if (userProfileResponse.getData().getEmployee().getProfileImageUrl() != null && !userProfileResponse.getData().getEmployee().getProfileImageUrl().isEmpty()) {
                            Glide.with(MainActivity.this).load(userProfileResponse.getData().getEmployee().getProfileImageUrl()).apply(RequestOptions.bitmapTransform(new CircleCrop())).into(profileImage);
                        } else {
                            Glide.with(MainActivity.this).load(R.mipmap.ic_launcher).apply(RequestOptions.bitmapTransform(new CircleCrop())).into(profileImage);
                        }

                    } else {
                        Toast.makeText(MainActivity.this, "Profile fetch failed: " + userProfileResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        profileName.setText(getString(R.string.guest_name));
                        profileEmail.setText(getString(R.string.guest_email));
                        Glide.with(MainActivity.this).load(R.mipmap.ic_launcher).apply(RequestOptions.bitmapTransform(new CircleCrop())).into(profileImage);
                    }

                } else {
                    Toast.makeText(MainActivity.this, "Profile fetch failed: " + response.code(), Toast.LENGTH_SHORT).show();
                    profileName.setText(getString(R.string.guest_name));
                    profileEmail.setText(getString(R.string.guest_email));
                    Glide.with(MainActivity.this).load(R.mipmap.ic_launcher).apply(RequestOptions.bitmapTransform(new CircleCrop())).into(profileImage);
                }

            }

            @Override
            public void onFailure(@NonNull Call<UserProfileResponse> call, @NonNull Throwable t) {
                Toast.makeText(MainActivity.this, "Profile fetch failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                profileName.setText(getString(R.string.guest_name));
                profileEmail.setText(getString(R.string.guest_email));
                Glide.with(MainActivity.this).load(R.mipmap.ic_launcher).apply(RequestOptions.bitmapTransform(new CircleCrop())).into(profileImage);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(networkChangeReceiver, filter);
        isNetworkChangeReceiverRegistered = true;
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
}
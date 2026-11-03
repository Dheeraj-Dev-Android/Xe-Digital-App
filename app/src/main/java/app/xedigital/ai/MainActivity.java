package app.xedigital.ai;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.analytics.FirebaseAnalytics;

import app.xedigital.ai.activity.PunchActivity;
import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.databinding.ActivityMainBinding;
import app.xedigital.ai.model.profile.UserProfileResponse;
import app.xedigital.ai.model.user.UserModelResponse;
import app.xedigital.ai.ui.deviceRegister.DeviceRegistrationViewModel;
import app.xedigital.ai.utills.CustomDialogHelper;
import app.xedigital.ai.utills.NetworkUtils;
import app.xedigital.ai.utills.PermissionManager;
import app.xedigital.ai.utills.RoleAccessManager;
import app.xedigital.ai.utills.SecureBaseActivity;
import app.xedigital.ai.utills.SecurePrefManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends SecureBaseActivity {

    private static final int REQUEST_CODE_PUNCH_ACTIVITY = 1;
    private static final String TAG = "MainActivity";

    private static final int[] INITIALS_COLOR_PALETTE = new int[]{
            Color.rgb(51, 206, 255), Color.rgb(125, 206, 160), Color.rgb(255, 165, 0),
            Color.rgb(199, 0, 57), Color.rgb(161, 16, 76), Color.rgb(102, 0, 102),
            Color.rgb(16, 89, 178), Color.rgb(76, 175, 80), Color.rgb(255, 87, 82),
            Color.rgb(255, 183, 77), Color.rgb(159, 168, 218), Color.rgb(38, 166, 154)
    };

    private final NetworkChangeReceiver networkChangeReceiver = new NetworkChangeReceiver();

    private ActivityMainBinding binding;
    private FirebaseAnalytics mFirebaseAnalytics;
    private AppBarConfiguration mAppBarConfiguration;
    private NavController navController;

    private boolean isAttendanceSubmenuVisible = false;
    private boolean isLeavesSubmenuVisible = false;
    private boolean isTeamSubVisible = false;
    private boolean isDcrSubmenuVisible = false;
    private boolean isOnboardingVisible = false;
    private boolean isNetworkChangeReceiverRegistered = false;

    private NavigationView navigationView;
    private TextView tvSpeed;
    private View slowInternetLayout;
    private ImageView profileImage;
    private TextView profileName;
    private TextView profileEmail;
    private ImageView clientLogo;

    private Call<UserProfileResponse> profileCall;
    private Call<UserModelResponse> userCall;

    private boolean isNoInternetDialogShowing = false;
    private DeviceRegistrationViewModel deviceRegistrationViewModel;
    private PermissionManager permissionManager;

    private int profileFetchAttempt = 0;
    private AlertDialog profileLoadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.appBarMain.toolbar);

        permissionManager = PermissionManager.getInstance(this);

        slowInternetLayout = findViewById(R.id.slowInternetLayout);
        tvSpeed = findViewById(R.id.tvSpeed);

        ImageButton dismissButton = findViewById(R.id.btnDismiss);
        if (dismissButton != null) {
            dismissButton.setOnClickListener(v -> {
                if (slowInternetLayout != null) {
                    slowInternetLayout.setVisibility(View.GONE);
                }
            });
        }

        String role = RoleAccessManager.getInstance(this).getRoleName();
        if (app.xedigital.ai.utills.RoleGroup.isAdminTier(role)) {
            Intent intent = new Intent(this, AdminMainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        DrawerLayout drawer = binding.drawerLayout;
        navigationView = binding.navView;
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_dashboard, R.id.nav_profile, R.id.nav_vms, R.id.nav_holidays,
                R.id.nav_policy, R.id.nav_settings, R.id.nav_payroll, R.id.nav_documents,
                R.id.nav_claim_management, R.id.nav_meeting_room, R.id.nav_shifts,
                R.id.nav_attendance, R.id.nav_addAttendanceFragment, R.id.nav_regularizeAppliedFragment,
                R.id.nav_pendingApprovalFragment, R.id.nav_leaves, R.id.nav_leaves_data,
                R.id.nav_applied_leaves, R.id.nav_approve_leaves, R.id.navTeam_member,
                R.id.navManager_attendance_menu, R.id.nav_team_member_leave,
                R.id.nav_team_member_timesheet, R.id.nav_dcr, R.id.nav_dcr_form
        ).setOpenableLayout(drawer).build();

        navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            boolean handled = false;

            if (id == R.id.nav_dashboard) {
                navigateToDashboard();
                handled = true;
            } else if (id == R.id.nav_attendance_menu) {
                toggleAttendanceVisibility(navigationView.getMenu());
                return true;
            } else if (id == R.id.nav_leaves_menu) {
                toggleLeavesVisibility(navigationView.getMenu());
                return true;
            } else if (id == R.id.nav_dcr_menu) {
                toggleDcrVisibility(navigationView.getMenu());
                return true;
            } else if (id == R.id.nav_onboarding_menu) {
                toggleOnboardingVisibility(navigationView.getMenu());
                return true;
            } else if (id == R.id.nav_team_member) {
                toggleTeamMemberVisibility(navigationView.getMenu());
                return true;
            } else if (id == R.id.nav_logout) {
                showLogoutConfirmationDialog();
                return true;
            } else {
                handled = navigateToDestination(id);
            }

            if (handled) {
                drawer.closeDrawer(GravityCompat.START);
            }

            return handled;
        });

        View headerView = navigationView.getHeaderView(0);
        if (headerView != null) {
            profileImage = headerView.findViewById(R.id.imageView);
            profileName = headerView.findViewById(R.id.textView);
            profileEmail = headerView.findViewById(R.id.subtitleText);
            clientLogo = headerView.findViewById(R.id.clientLogo);
        }

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            int destId = destination.getId();
            navigationView.setCheckedItem(destId);
            if (destId == R.id.nav_dashboard) {
                collapseAllSubmenus();
            } else {
                syncSubmenuVisibilityForDestination(destId);
            }
        });
        if (navController.getCurrentDestination() != null) {
            syncSubmenuVisibilityForDestination(navController.getCurrentDestination().getId());
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START);
                    return;
                }

                if (navController.getCurrentDestination() != null && navController.getCurrentDestination().getId() != R.id.nav_dashboard) {
                    navigateToDashboard();
                    return;
                }

                if (!isFinishing() && !isDestroyed()) {
                    CustomDialogHelper.showWarningDialog(MainActivity.this, "Exit App", "Are you sure you want to exit the app?", "Yes, Exit", "Cancel", new CustomDialogHelper.OnWarningActionListener() {
                        @Override
                        public void onConfirm() {
                            finish();
                        }

                        @Override
                        public void onCancel() {
                            // Stay in app
                        }
                    });
                }
            }
        });

        fetchUserProfileData();
    }

    private void navigateToDashboard() {
        NavOptions navOptions = new NavOptions.Builder().setPopUpTo(R.id.nav_dashboard, true).setLaunchSingleTop(true).build();
        try {
            navController.navigate(R.id.nav_dashboard, null, navOptions);
        } catch (Exception e) {
            Log.e(TAG, "Navigation to dashboard failed: " + e.getMessage());
        }
    }

    private boolean navigateToDestination(int destinationId) {
        try {
            NavOptions navOptions = new NavOptions.Builder().setLaunchSingleTop(true).build();
            navController.navigate(destinationId, null, navOptions);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Navigation failed for id: " + destinationId + " -> " + e.getMessage());
            return false;
        }
    }

    private void syncSubmenuVisibilityForDestination(int destId) {
        if (destId == R.id.nav_attendance || destId == R.id.nav_addAttendanceFragment || destId == R.id.nav_regularizeAppliedFragment || destId == R.id.nav_pendingApprovalFragment) {
            if (!isAttendanceSubmenuVisible) toggleAttendanceVisibility(navigationView.getMenu());
        } else if (destId == R.id.nav_leaves || destId == R.id.nav_leaves_data || destId == R.id.nav_applied_leaves || destId == R.id.nav_approve_leaves) {
            if (!isLeavesSubmenuVisible) toggleLeavesVisibility(navigationView.getMenu());
        } else if (destId == R.id.navTeam_member || destId == R.id.navManager_attendance_menu || destId == R.id.nav_team_member_leave || destId == R.id.nav_team_member_timesheet) {
            if (!isTeamSubVisible) toggleTeamMemberVisibility(navigationView.getMenu());
        } else if (destId == R.id.nav_dcr || destId == R.id.nav_dcr_form) {
            if (!isDcrSubmenuVisible) toggleDcrVisibility(navigationView.getMenu());
        } else if (destId == R.id.nav_onboarding || destId == R.id.nav_onboarding_details) {
            if (!isOnboardingVisible) toggleOnboardingVisibility(navigationView.getMenu());
        }
    }

    private void collapseAllSubmenus() {
        Menu menu = navigationView.getMenu();
        if (isAttendanceSubmenuVisible) toggleAttendanceVisibility(menu);
        if (isLeavesSubmenuVisible) toggleLeavesVisibility(menu);
        if (isTeamSubVisible) toggleTeamMemberVisibility(menu);
        if (isDcrSubmenuVisible) toggleDcrVisibility(menu);
        if (isOnboardingVisible) toggleOnboardingVisibility(menu);
    }

    public void showNoInternetLayout() {
        View noInternetView = findViewById(R.id.noInternetLayout);
        if (noInternetView != null) {
            noInternetView.setVisibility(View.VISIBLE);
        }

        if (!isNoInternetDialogShowing && !isFinishing() && !isDestroyed()) {
            isNoInternetDialogShowing = true;
            CustomDialogHelper.showErrorDialog(this, "No Internet Connection", "Please check your internet connection and try again.\n\nMake sure Wi-Fi or mobile data is turned on.", () -> {
                isNoInternetDialogShowing = false;
                finish();
            });
        }
    }

    public void hideNoInternetLayout() {
        View noInternetView = findViewById(R.id.noInternetLayout);
        if (noInternetView != null) {
            noInternetView.setVisibility(View.GONE);
        }
        isNoInternetDialogShowing = false;
    }

    public void showSlowInternetLayout(double speed) {
        if (slowInternetLayout != null) {
            slowInternetLayout.setVisibility(View.VISIBLE);
            String speedText = String.format("Current speed: %.2f Mbps", speed / 1000);
            if (tvSpeed != null) {
                tvSpeed.setText(speedText);
            }
        }

        if (!isFinishing() && !isDestroyed()) {
            CustomDialogHelper.showInfoDialog(this, "Slow Network Detected", "Your network connection is slow.\n\nCurrent speed: " + String.format("%.2f Mbps", speed / 1000) + "\n\nSome features might load slowly or be temporarily unavailable.");
        }
    }

    public void hideSlowInternetLayout() {
        if (slowInternetLayout != null) {
            slowInternetLayout.setVisibility(View.GONE);
        }
    }

    private void showLogoutConfirmationDialog() {
        if (isFinishing() || isDestroyed()) return;

        CustomDialogHelper.showWarningDialog(this, "Logout", "Are you sure you want to logout?\n\nYou will need to login again to access the app.", "Yes, Logout", "Cancel", new CustomDialogHelper.OnWarningActionListener() {
            @Override
            public void onConfirm() {
                handleLogout();
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "Logout cancelled by user");
            }
        });
    }

    private void handleLogout() {
        SecurePrefManager.getInstance(MainActivity.this).clearSession();
        RoleAccessManager.getInstance(this).clear();

        getSharedPreferences("AdminCred", MODE_PRIVATE).edit().clear().apply();
        if (APIClient.getSharedCookieJar() != null) {
            APIClient.getSharedCookieJar().clearAll();
        }

        Intent intent = new Intent(MainActivity.this, app.xedigital.ai.activity.LoginSelectionActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void refreshNavigationMenu() {
        if (navigationView == null) return;
        navigationView.post(() -> {
            for (int i = 0; i < navigationView.getChildCount(); i++) {
                View child = navigationView.getChildAt(i);
                child.requestLayout();
                child.invalidate();
            }
            navigationView.requestLayout();
            navigationView.invalidate();
        });
    }

    private void toggleAttendanceVisibility(Menu menu) {
        boolean newVisibility = !isAttendanceSubmenuVisible;
        if (menu.findItem(R.id.nav_attendance) != null)
            menu.findItem(R.id.nav_attendance).setVisible(newVisibility);
        if (menu.findItem(R.id.nav_addAttendanceFragment) != null)
            menu.findItem(R.id.nav_addAttendanceFragment).setVisible(newVisibility);
        if (menu.findItem(R.id.nav_regularizeAppliedFragment) != null)
            menu.findItem(R.id.nav_regularizeAppliedFragment).setVisible(newVisibility);
        if (menu.findItem(R.id.nav_pendingApprovalFragment) != null)
            menu.findItem(R.id.nav_pendingApprovalFragment).setVisible(newVisibility);
        isAttendanceSubmenuVisible = newVisibility;
        refreshNavigationMenu();
    }

    private void toggleLeavesVisibility(Menu menu) {
        boolean newVisibility = !isLeavesSubmenuVisible;
        if (menu.findItem(R.id.nav_leaves) != null)
            menu.findItem(R.id.nav_leaves).setVisible(newVisibility);
        if (menu.findItem(R.id.nav_leaves_data) != null)
            menu.findItem(R.id.nav_leaves_data).setVisible(newVisibility);
        if (menu.findItem(R.id.nav_applied_leaves) != null)
            menu.findItem(R.id.nav_applied_leaves).setVisible(newVisibility);
        if (menu.findItem(R.id.nav_approve_leaves) != null)
            menu.findItem(R.id.nav_approve_leaves).setVisible(newVisibility);
        isLeavesSubmenuVisible = newVisibility;
        refreshNavigationMenu();
    }

    private void toggleTeamMemberVisibility(Menu menu) {
        boolean newVisibility = !isTeamSubVisible;
        if (menu.findItem(R.id.navTeam_member) != null)
            menu.findItem(R.id.navTeam_member).setVisible(newVisibility);
        if (menu.findItem(R.id.navManager_attendance_menu) != null)
            menu.findItem(R.id.navManager_attendance_menu).setVisible(newVisibility);
        if (menu.findItem(R.id.nav_team_member_leave) != null)
            menu.findItem(R.id.nav_team_member_leave).setVisible(newVisibility);
        if (menu.findItem(R.id.nav_team_member_timesheet) != null)
            menu.findItem(R.id.nav_team_member_timesheet).setVisible(newVisibility);
        isTeamSubVisible = newVisibility;
        refreshNavigationMenu();
    }

    private void toggleDcrVisibility(Menu menu) {
        boolean newVisibility = !isDcrSubmenuVisible;
        if (menu.findItem(R.id.nav_dcr) != null)
            menu.findItem(R.id.nav_dcr).setVisible(newVisibility);
        if (menu.findItem(R.id.nav_dcr_form) != null)
            menu.findItem(R.id.nav_dcr_form).setVisible(newVisibility);
        isDcrSubmenuVisible = newVisibility;
        refreshNavigationMenu();
    }

    private void toggleOnboardingVisibility(Menu menu) {
        boolean newVisibility = !isOnboardingVisible;
        if (menu.findItem(R.id.nav_onboarding) != null)
            menu.findItem(R.id.nav_onboarding).setVisible(newVisibility);
        if (menu.findItem(R.id.nav_onboarding_details) != null)
            menu.findItem(R.id.nav_onboarding_details).setVisible(newVisibility);
        isOnboardingVisible = newVisibility;
        refreshNavigationMenu();
    }

    @Override
    public boolean onSupportNavigateUp() {
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
                navigateToDashboard();
            } else {
                if (!isFinishing() && !isDestroyed()) {
                    CustomDialogHelper.showErrorDialog(this, "Punch Failed", "Attendance punch could not be recorded.\nPlease try again.");
                }
            }
        }
    }

    public void onRetryButtonClicked(View view) {
        if (NetworkUtils.isNetworkAvailable(this)) {
            hideNoInternetLayout();
        } else {
            if (!isFinishing() && !isDestroyed()) {
                CustomDialogHelper.showInfoDialog(this, "Still Offline", "No internet connection detected.\nPlease enable Wi-Fi or mobile data and try again.");
            }
        }
    }

    public void onOpenSettingsButtonClicked(View view) {
        startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
    }

    private void fetchUserProfileData() {
        SecurePrefManager prefManager = SecurePrefManager.getInstance(MainActivity.this);
        String userId = prefManager.getString("userId", null);
        String authToken = prefManager.getString("authToken", null);

        if (userId == null || authToken == null) {
            handleProfileFetchError();
            return;
        }

        fetchUserProfile(userId, authToken, profileImage, profileName, profileEmail);
        fetchUserData(userId, authToken, clientLogo);
    }

    private void fetchUserProfile(String userId, String authToken, ImageView profileImage, TextView profileName, TextView profileEmail) {
        String authHeaderValue = "jwt " + authToken;
        profileCall = APIClient.getInstance().getUser().getUserProfile(userId, authHeaderValue);

        profileCall.enqueue(new Callback<UserProfileResponse>() {
            @Override
            public void onResponse(@NonNull Call<UserProfileResponse> call, @NonNull Response<UserProfileResponse> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    handleProfileFetchError();
                    return;
                }

                UserProfileResponse userProfileResponse = response.body();
                if (!userProfileResponse.isSuccess() || userProfileResponse.getData() == null || userProfileResponse.getData().getEmployee() == null) {
                    handleProfileFetchError();
                    return;
                }

                String firstName = userProfileResponse.getData().getEmployee().getFirstname();
                String lastName = userProfileResponse.getData().getEmployee().getLastname();
                String profileImageUrl = userProfileResponse.getData().getEmployee().getProfileImageUrl();
                String email = userProfileResponse.getData().getEmployee().getEmail();

                String fullName = (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
                SpannableString spannableString = new SpannableString(fullName);

                if (firstName != null && !firstName.isEmpty()) {
                    spannableString.setSpan(new ForegroundColorSpan(Color.rgb(255, 165, 0)), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    spannableString.setSpan(new StyleSpan(Typeface.BOLD), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    spannableString.setSpan(new RelativeSizeSpan(1.3f), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                if (lastName != null && !lastName.isEmpty()) {
                    int lastNameStartIndex = (firstName != null ? firstName.length() : 0) + 1;
                    if (lastNameStartIndex < spannableString.length()) {
                        spannableString.setSpan(new ForegroundColorSpan(Color.rgb(255, 165, 0)), lastNameStartIndex, lastNameStartIndex + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        spannableString.setSpan(new StyleSpan(Typeface.BOLD), lastNameStartIndex, lastNameStartIndex + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        spannableString.setSpan(new RelativeSizeSpan(1.3f), lastNameStartIndex, lastNameStartIndex + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }

                if (profileName != null) profileName.setText(spannableString);
                if (profileEmail != null) profileEmail.setText(email);

                if (!MainActivity.this.isFinishing() && !MainActivity.this.isDestroyed() && profileImage != null) {
                    Drawable initialsDrawable = getInitialsDrawable(firstName, lastName);

                    if (profileImageUrl != null && !profileImageUrl.trim().isEmpty() && !"null".equalsIgnoreCase(profileImageUrl.trim())) {
                        Glide.with(MainActivity.this).load(profileImageUrl).apply(RequestOptions.bitmapTransform(new CircleCrop())).placeholder(initialsDrawable).error(initialsDrawable).into(profileImage);
                    } else {
                        profileImage.setImageDrawable(initialsDrawable);
                    }
                }

                dismissProfileLoadingDialog();
                profileFetchAttempt = 0;
            }

            @Override
            public void onFailure(@NonNull Call<UserProfileResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Profile fetch failure: " + t.getMessage());
                handleProfileFetchError();
            }
        });
    }

    private void fetchUserData(String userId, String authToken, ImageView clientLogo) {
        String authHeaderValue = "jwt " + authToken;
        userCall = APIClient.getInstance().getUser().getUserData(userId, authHeaderValue);

        userCall.enqueue(new Callback<UserModelResponse>() {
            @Override
            public void onResponse(@NonNull Call<UserModelResponse> call, @NonNull Response<UserModelResponse> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Log.e(TAG, "User data fetch failed");
                    return;
                }

                UserModelResponse userDataResponse = response.body();
                if (!userDataResponse.isSuccess() || userDataResponse.getData() == null || userDataResponse.getData().getCompany() == null) {
                    Log.e(TAG, "User data fetch failed: " + userDataResponse.getMessage());
                    return;
                }

                String clientLogoUrl = userDataResponse.getData().getCompany().getLogo();
                if (!MainActivity.this.isFinishing() && !MainActivity.this.isDestroyed() && clientLogo != null) {
                    if (clientLogoUrl != null && !clientLogoUrl.isEmpty()) {
                        Glide.with(MainActivity.this).load(clientLogoUrl).into(clientLogo);
                    } else {
                        Glide.with(MainActivity.this).load(R.mipmap.ic_launcher).apply(RequestOptions.bitmapTransform(new CircleCrop())).into(clientLogo);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserModelResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "User data fetch failed: " + t.getMessage());
            }
        });
    }

    private void handleProfileFetchFailure(String message, ImageView profileImage, TextView profileName, TextView profileEmail, ImageView clientLogo) {
        if (isFinishing() || isDestroyed()) return;

        Log.e(TAG, "handleProfileFetchFailure: " + message);

        if (profileName != null) profileName.setText(getString(R.string.guest_name));
        if (profileEmail != null) profileEmail.setText(getString(R.string.guest_email));

        if (profileImage != null) {
            profileImage.setImageDrawable(getInitialsDrawable(null, null));
        }

        if (clientLogo != null) {
            Glide.with(getApplicationContext()).load(R.mipmap.ic_launcher).apply(RequestOptions.bitmapTransform(new CircleCrop())).into(clientLogo);
        }
    }

    private void handleProfileFetchError() {
        if (isFinishing() || isDestroyed()) return;

        dismissProfileLoadingDialog();
        handleProfileFetchFailure("Profile fetch failed", profileImage, profileName, profileEmail, clientLogo);

        profileFetchAttempt++;

        if (profileFetchAttempt == 1) {
            CustomDialogHelper.showErrorDialog(this, "Heads Up", "We couldn't load your profile details right now. Please try again.", "Retry", () -> {
                profileLoadingDialog = CustomDialogHelper.showLoadingDialog(this, "Loading your profile...");
                fetchUserProfileData();
            });
        } else {
            CustomDialogHelper.showErrorDialog(this, "Unable to Load Profile", "We're still unable to load your details. Please log in again to continue.", "Go to Login", () -> handleLogout());
        }
    }

    private void dismissProfileLoadingDialog() {
        if (profileLoadingDialog != null && profileLoadingDialog.isShowing()) {
            CustomDialogHelper.dismissWithAnimation(this, profileLoadingDialog);
            profileLoadingDialog = null;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerNetworkReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (permissionManager != null) {
            permissionManager.syncAllPermissions();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterNetworkReceiver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (profileCall != null) profileCall.cancel();
        if (userCall != null) userCall.cancel();
    }

    private void registerNetworkReceiver() {
        if (!isNetworkChangeReceiverRegistered) {
            try {
                IntentFilter filter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
                registerReceiver(networkChangeReceiver, filter);
                isNetworkChangeReceiverRegistered = true;
            } catch (Exception e) {
                Log.e(TAG, "Error registering NetworkChangeReceiver", e);
            }
        }
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

    private Drawable getInitialsDrawable(String firstName, String lastName) {
        final String initials = getInitials(firstName, lastName);
        final int bgColor = getInitialsColor(firstName, lastName);

        return new Drawable() {
            private final Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            private final Rect textBounds = new Rect();

            {
                bgPaint.setColor(bgColor);
                bgPaint.setStyle(Paint.Style.FILL);

                textPaint.setColor(Color.WHITE);
                textPaint.setTextAlign(Paint.Align.CENTER);
                textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            }

            @Override
            public void draw(@NonNull Canvas canvas) {
                Rect b = getBounds();
                if (b.width() <= 0 || b.height() <= 0) return;

                float cx = b.exactCenterX();
                float cy = b.exactCenterY();
                float radius = Math.min(b.width(), b.height()) / 2f;

                canvas.drawCircle(cx, cy, radius, bgPaint);

                textPaint.setTextSize(radius * 0.8f);
                textPaint.getTextBounds(initials, 0, initials.length(), textBounds);
                float textY = cy - textBounds.exactCenterY();
                canvas.drawText(initials, cx, textY, textPaint);
            }

            @Override
            public void setAlpha(int alpha) {
                bgPaint.setAlpha(alpha);
                textPaint.setAlpha(alpha);
                invalidateSelf();
            }

            @Override
            public void setColorFilter(@Nullable ColorFilter colorFilter) {
                bgPaint.setColorFilter(colorFilter);
                textPaint.setColorFilter(colorFilter);
                invalidateSelf();
            }

            @Override
            public int getOpacity() {
                return PixelFormat.TRANSLUCENT;
            }
        };
    }

    private String getInitials(String firstName, String lastName) {
        String first = firstName != null ? firstName.trim() : "";
        String last = lastName != null ? lastName.trim() : "";

        StringBuilder sb = new StringBuilder();
        if (!first.isEmpty()) {
            sb.append(Character.toUpperCase(first.charAt(0)));
        }
        if (!last.isEmpty()) {
            sb.append(Character.toUpperCase(last.charAt(0)));
        }

        if (sb.length() == 0) {
            return "?";
        }
        return sb.toString();
    }

    private int getInitialsColor(String firstName, String lastName) {
        String key = ((firstName != null ? firstName : "") + "|" + (lastName != null ? lastName : "")).trim();
        if (key.isEmpty() || "|".equals(key)) {
            return INITIALS_COLOR_PALETTE[0];
        }
        int hash = key.hashCode();
        int index = Math.abs(hash) % INITIALS_COLOR_PALETTE.length;
        return INITIALS_COLOR_PALETTE[index];
    }
}
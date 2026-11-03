package app.xedigital.ai;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.HashMap;
import java.util.Map;

import app.xedigital.ai.activity.LoginSelectionActivity;
import app.xedigital.ai.admin.adminAPI.AdminAPIClient;
import app.xedigital.ai.admin.adminAPI.AdminAPIInterface;
import app.xedigital.ai.admin.adminModal.UserDetails.UserDetailsResponse;
import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.databinding.ActivityAdminMainBinding;
import app.xedigital.ai.databinding.NoInternetConnectionBinding;
import app.xedigital.ai.databinding.SlowInternetConnectionBinding;
import app.xedigital.ai.utills.CustomDialogHelper;
import app.xedigital.ai.utills.NetworkUtils;
import app.xedigital.ai.utills.SecureBaseActivity;
import app.xedigital.ai.utills.SecurePrefManager;
import app.xedigital.ai.utills.UserViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminMainActivity extends SecureBaseActivity {

    private static final String TAG = "AdminMainActivity";

    private final NetworkChangeReceiver networkChangeReceiver = new NetworkChangeReceiver();

    private final Map<Integer, Boolean> expandedGroups = new HashMap<>();

    private final Map<Integer, int[]> groupChildMap = new HashMap<Integer, int[]>() {{
        put(R.id.nav_digital_identity_menu, new int[]{
                R.id.nav_visitorDetailsFragment,
                R.id.nav_employees,
                R.id.nav_partners,
                R.id.nav_allUsers
        });
        put(R.id.nav_workforce_menu, new int[]{
                R.id.nav_wf_attendance,
                R.id.nav_employee_leaves,
                R.id.nav_timesheet,
                R.id.nav_shifts,
                R.id.nav_departments,
                R.id.nav_business_unit,
                R.id.nav_holidays,
                R.id.nav_leave_types,
                R.id.nav_policy,
                R.id.nav_claim_management_wf
        });
        put(R.id.nav_talent_menu, new int[]{
                R.id.nav_job_posting,
                R.id.nav_sourcing,
                R.id.nav_assessment,
                R.id.nav_interviews,
                R.id.nav_feedback_review,
                R.id.nav_offer_letter
        });
        put(R.id.nav_bgv_menu, new int[]{
                R.id.nav_candidates_verification,
                R.id.nav_employment_verification
        });
        put(R.id.nav_payroll_menu, new int[]{
                R.id.nav_payroll_employees,
                R.id.nav_salary_component,
                R.id.nav_upload_payroll,
                R.id.nav_generate_payroll,
                R.id.nav_payroll_config
        });
        put(R.id.nav_meeting_menu, new int[]{
                R.id.nav_meeting_rooms,
                R.id.nav_book_meeting
        });
        put(R.id.nav_claim_menu, new int[]{
                R.id.nav_claim_managements,
                R.id.nav_travel_expenses,
                R.id.nav_food_expenses,
                R.id.nav_accommodation_expenses,
                R.id.nav_miscellaneous_expenses
        });
        put(R.id.nav_config_menu, new int[]{
                R.id.nav_visitor_categories,
                R.id.nav_leave_config,
                R.id.nav_notification
        });
    }};

    private ActivityAdminMainBinding binding;
    private AppBarConfiguration mAppBarConfiguration;
    private NavController navController;
    private UserViewModel userViewModel;
    private FirebaseAnalytics mFirebaseAnalytics;
    private boolean isNetworkChangeReceiverRegistered = false;
    private SlowInternetConnectionBinding slowInternetBinding;
    private NoInternetConnectionBinding noInternetBinding;

    private int profileFetchAttempt = 0;
    private AlertDialog profileLoadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        binding = ActivityAdminMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        slowInternetBinding = SlowInternetConnectionBinding.bind(binding.getRoot().findViewById(R.id.slowInternetLayout));
        noInternetBinding = NoInternetConnectionBinding.bind(binding.getRoot().findViewById(R.id.noInternetLayout));

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        setSupportActionBar(binding.adminAppBarMain.adminToolbar);

        DrawerLayout drawer = binding.adminDrawerLayout;
        NavigationView navigationView = binding.adminNavView;

        navController = Navigation.findNavController(this, R.id.admin_nav_host_fragment_content_main);

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_admin_dashboard,
                R.id.nav_visitorDetailsFragment,
                R.id.nav_employees,
                R.id.nav_partners,
                R.id.nav_allUsers,
                R.id.nav_wf_attendance,
                R.id.nav_employee_leaves,
                R.id.nav_timesheet,
                R.id.nav_shifts,
                R.id.nav_departments,
                R.id.nav_business_unit,
                R.id.nav_holidays,
                R.id.nav_leave_types,
                R.id.nav_policy,
                R.id.nav_claim_management_wf,
                R.id.nav_job_posting,
                R.id.nav_sourcing,
                R.id.nav_assessment,
                R.id.nav_interviews,
                R.id.nav_feedback_review,
                R.id.nav_offer_letter,
                R.id.nav_candidates_verification,
                R.id.nav_employment_verification,
                R.id.nav_payroll_employees,
                R.id.nav_salary_component,
                R.id.nav_upload_payroll,
                R.id.nav_generate_payroll,
                R.id.nav_payroll_config,
                R.id.nav_meeting_rooms,
                R.id.nav_book_meeting,
                R.id.nav_claim_managements,
                R.id.nav_travel_expenses,
                R.id.nav_food_expenses,
                R.id.nav_accommodation_expenses,
                R.id.nav_miscellaneous_expenses,
                R.id.nav_productivity,
                R.id.nav_access_management,
                R.id.nav_new_features,
                R.id.nav_visitor_categories,
                R.id.nav_leave_config,
                R.id.nav_notification,
                R.id.nav_visitorCheckInFragment
        ).setOpenableLayout(drawer).build();

        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);

        setupNavigationDrawer(navigationView, drawer);

        slowInternetBinding.btnDismiss.setOnClickListener(v ->
                slowInternetBinding.slowInternetContainer.setVisibility(View.GONE));

        if (navigationView != null) {
            fetchUserProfileData();
        } else {
            Log.e(TAG, "Navigation view is null");
        }

        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        if (navController.getCurrentDestination() != null) {
            syncSubmenuVisibilityForDestination(navController.getCurrentDestination().getId());
        }
        setupBackPressedHandling(drawer);
    }

    private void setupNavigationDrawer(NavigationView navigationView, DrawerLayout drawer) {
        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_logout) {
                drawer.closeDrawer(GravityCompat.START);
                handleLogout();
                return true;
            }

            if (groupChildMap.containsKey(itemId)) {
                toggleSubMenu(navigationView, itemId);
                return true;
            }

            boolean handled = NavigationUI.onNavDestinationSelected(item, navController);
            if (handled) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                Toast.makeText(this, item.getTitle() + " — Coming Soon", Toast.LENGTH_SHORT).show();
                drawer.closeDrawer(GravityCompat.START);
            }
            return true;
        });
    }

    private void toggleSubMenu(NavigationView navigationView, int parentId) {
        boolean isCurrentlyExpanded = Boolean.TRUE.equals(expandedGroups.get(parentId));

        for (Map.Entry<Integer, int[]> entry : groupChildMap.entrySet()) {
            if (entry.getKey() != parentId) {
                setChildrenVisibility(navigationView, entry.getValue(), false);
                expandedGroups.put(entry.getKey(), false);
            }
        }

        boolean newState = !isCurrentlyExpanded;
        setChildrenVisibility(navigationView, groupChildMap.get(parentId), newState);
        expandedGroups.put(parentId, newState);
    }

    private void refreshNavigationMenu() {
        NavigationView navigationView = binding.adminNavView;
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

    private void setChildrenVisibility(NavigationView navigationView, int[] childIds, boolean visible) {
        if (childIds == null) return;
        for (int childId : childIds) {
            MenuItem childItem = navigationView.getMenu().findItem(childId);
            if (childItem != null) {
                childItem.setVisible(visible);
            }
        }
        refreshNavigationMenu();
    }

    private void setupBackPressedHandling(DrawerLayout drawer) {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                    return;
                }

                if (navController.navigateUp()) {
                    return;
                }

                finish();
            }
        });
    }

    private void syncSubmenuVisibilityForDestination(int destId) {
        NavigationView navigationView = binding.adminNavView;
        for (Map.Entry<Integer, int[]> entry : groupChildMap.entrySet()) {
            for (int childId : entry.getValue()) {
                if (childId == destId) {
                    if (!Boolean.TRUE.equals(expandedGroups.get(entry.getKey()))) {
                        toggleSubMenu(navigationView, entry.getKey());
                    }
                    return;
                }
            }
        }
    }

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

    private void handleLogout() {
        SecurePrefManager.getInstance(this).clearAll();
        if (APIClient.getSharedCookieJar() != null) {
            APIClient.getSharedCookieJar().clearAll();
        }
        Intent intent = new Intent(this, LoginSelectionActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void handleProfileFetchError() {
        if (isFinishing() || isDestroyed()) return;

        dismissProfileLoadingDialog();

        profileFetchAttempt++;

        if (profileFetchAttempt == 1) {
            CustomDialogHelper.showErrorDialog(this, "Heads Up",
                    "We couldn't load your profile details right now. Please try again.",
                    "Retry",
                    () -> {
                        profileLoadingDialog = CustomDialogHelper.showLoadingDialog(this, "Loading your profile...");
                        fetchUserProfileData();
                    });
        } else {
            CustomDialogHelper.showErrorDialog(this, "Unable to Load Profile",
                    "We're still unable to load your details. Please log in again to continue.",
                    "Go to Login",
                    () -> handleLogout());
        }
    }

    private void dismissProfileLoadingDialog() {
        if (profileLoadingDialog != null && profileLoadingDialog.isShowing()) {
            CustomDialogHelper.dismissWithAnimation(this, profileLoadingDialog);
            profileLoadingDialog = null;
        }
    }

    private void fetchUserProfileData() {
        SecurePrefManager prefManager = SecurePrefManager.getInstance(this);
        String authToken = prefManager.getString("authToken", "");
        String userId = prefManager.getString("userId", "");

        if (authToken.isEmpty() || userId.isEmpty()) {
            Log.e(TAG, "Missing auth token or user ID");
            handleProfileFetchError();
            return;
        }

        AdminAPIInterface apiService = AdminAPIClient.getInstance().getBase2();
        Call<UserDetailsResponse> call = apiService.getUser("jwt " + authToken, userId);

        call.enqueue(new Callback<UserDetailsResponse>() {
            @Override
            public void onResponse(@NonNull Call<UserDetailsResponse> call,
                                   @NonNull Response<UserDetailsResponse> response) {
                if (response.isSuccessful()
                        && response.body() != null
                        && response.body().getData() != null
                        && response.body().getData().getUser() != null) {
                    UserDetailsResponse userDetails = response.body();
                    userViewModel.setUserDetails(userDetails);
                    bindUserToDrawer(userDetails);
                    dismissProfileLoadingDialog();
                    profileFetchAttempt = 0;
                } else {
                    Log.e(TAG, "Failed to fetch profile: " + response.message());
                    handleProfileFetchError();
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserDetailsResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "API failure: " + t.getMessage());
                handleProfileFetchError();
            }
        });
    }

    private void bindUserToDrawer(UserDetailsResponse userDetails) {
        SecurePrefManager preferences = SecurePrefManager.getInstance(this);
        NavigationView navigationView = binding.adminNavView;
        View headerView = navigationView.getHeaderView(0);

        TextView nameText = headerView.findViewById(R.id.textView);
        TextView subtitleText = headerView.findViewById(R.id.subtitleText);
        ImageView profileImage = headerView.findViewById(R.id.imageView);

        String firstName = userDetails.getData().getUser().getFirstname();
        String lastName = userDetails.getData().getUser().getLastname();

        nameText.setText(firstName + " " + lastName);
        subtitleText.setText(userDetails.getData().getUser().getEmail());
        preferences.putString("company_logo", userDetails.getData().getCompany().getLogo());

        if (userDetails.getData() != null) {
            Glide.with(this)
                    .load(userDetails.getData().getCompany().getLogo())
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .into(profileImage);
        }
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
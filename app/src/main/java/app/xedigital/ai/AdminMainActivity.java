package app.xedigital.ai;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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
import androidx.appcompat.app.AppCompatActivity;
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

import app.xedigital.ai.activity.AdminLoginActivity;
import app.xedigital.ai.adminApi.AdminAPIClient;
import app.xedigital.ai.adminApi.AdminAPIInterface;
import app.xedigital.ai.databinding.ActivityAdminMainBinding;
import app.xedigital.ai.databinding.NoInternetConnectionBinding;
import app.xedigital.ai.databinding.SlowInternetConnectionBinding;
import app.xedigital.ai.model.Admin.UserDetails.UserDetailsResponse;
import app.xedigital.ai.utills.NetworkUtils;
import app.xedigital.ai.utills.UserViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminMainActivity extends AppCompatActivity {

    private static final String TAG = "AdminMainActivity";
    private final NetworkChangeReceiver networkChangeReceiver = new NetworkChangeReceiver();
    private ActivityAdminMainBinding binding;
    private AppBarConfiguration mAppBarConfiguration;
    private NavController navController;
    private UserViewModel userViewModel;
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

        mAppBarConfiguration = new AppBarConfiguration.Builder(R.id.nav_admin_dashboard, R.id.nav_visitorCheckInFragment, R.id.nav_visitorDetailsFragment, R.id.nav_employees, R.id.nav_partners, R.id.nav_allUsers, R.id.nav_logout).setOpenableLayout(drawer).build();

        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // Handle dismiss slow internet layout
        slowInternetBinding.btnDismiss.setOnClickListener(v -> slowInternetBinding.slowInternetContainer.setVisibility(View.GONE));


        if (navigationView != null) {
            fetchUserProfileData();
        } else {
            Log.e(TAG, "Navigation view is null, cannot fetch user profile");
        }

        MenuItem logout = navigationView.getMenu().findItem(R.id.nav_logout);
        logout.setOnMenuItemClickListener(item -> {
            handleLogout();
            return true;
        });
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // If the Navigation Drawer is open, close it first
                if (binding.adminDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.adminDrawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    // If we are on the Dashboard, this will exit the app
                    // rather than going back to a previous screen/activity.
                    finishAffinity();
                }
            }
        });
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

    private void handleLogout() {
        // Access the same SharedPreferences file used in Login
        SharedPreferences sharedPreferences = getSharedPreferences("AdminCred", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        // Redirect to Login and clear the activity stack
        Intent intent = new Intent(this, AdminLoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void fetchUserProfileData() {
        SharedPreferences sharedPreferences = getSharedPreferences("AdminCred", MODE_PRIVATE);
        String authToken = sharedPreferences.getString("authToken", "");
        String userId = sharedPreferences.getString("userId", "");

        if (authToken.isEmpty() || userId.isEmpty()) {
            Log.e(TAG, "Missing auth token or user ID");
            return;
        }

        AdminAPIInterface apiService = AdminAPIClient.getInstance().getBase2();
        Call<UserDetailsResponse> call = apiService.getUser("jwt " + authToken, userId);

        call.enqueue(new Callback<UserDetailsResponse>() {
            @Override
            public void onResponse(@NonNull Call<UserDetailsResponse> call, @NonNull Response<UserDetailsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserDetailsResponse userDetails = response.body();
                    userViewModel.setUserDetails(userDetails);
                    bindUserToDrawer(userDetails);
                } else {
                    Log.e(TAG, "Failed to fetch profile: " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserDetailsResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "API failure: " + t.getMessage());
            }
        });
    }

    private void bindUserToDrawer(UserDetailsResponse userDetails) {
        NavigationView navigationView = binding.adminNavView;
        View headerView = navigationView.getHeaderView(0);

        TextView nameText = headerView.findViewById(R.id.textView);
        TextView subtitleText = headerView.findViewById(R.id.subtitleText);
        ImageView profileImage = headerView.findViewById(R.id.imageView);
        String FirstName = userDetails.getData().getUser().getFirstname();
        String LastName = userDetails.getData().getUser().getLastname();

        nameText.setText(FirstName + LastName);
        subtitleText.setText(userDetails.getData().getUser().getEmail());

        // Load profile image if exists (use Glide or Picasso)
        if (userDetails.getData() != null) {
            Glide.with(this).load(userDetails.getData().getCompany().getLogo()).placeholder(R.drawable.ic_profile_placeholder).into(profileImage);

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

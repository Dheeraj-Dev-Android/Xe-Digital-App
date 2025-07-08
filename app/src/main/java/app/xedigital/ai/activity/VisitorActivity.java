package app.xedigital.ai.activity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import app.xedigital.ai.R;
import app.xedigital.ai.adminActivity.AdminCheckOutActivity;
import app.xedigital.ai.adminActivity.AdminManualCheckIn;
import app.xedigital.ai.adminActivity.AdminPunchActivity;

public class VisitorActivity extends AppCompatActivity {

    private Handler handler;
    private View loadingAnimation;
    private View mainContentCard;
    private View punchInButton;
    private View logoutButton;
    private View punchOutButton;
    private View manualCheckInButton;
    private View buttonsContainer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.visitor_activity);

        Log.d("VISITOR_ACTIVITY", "VisitorActivity opened");
        Toast.makeText(this, "Visitor Activity Loaded", Toast.LENGTH_SHORT).show();

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        handler = new Handler(Looper.getMainLooper());

        // Initialize views with updated IDs from responsive layout
        initializeViews();

        // Setup initial visibility states
        setupInitialVisibility();

        // Setup click listeners
        setupClickListeners();

        // Setup back press handling
        setupBackPressHandling();

        // Start loading sequence
        startLoadingSequence();
    }

    private void initializeViews() {
        loadingAnimation = findViewById(R.id.loading_animation);
        mainContentCard = findViewById(R.id.main_content_card);
        punchInButton = findViewById(R.id.punch_in_button);
        manualCheckInButton = findViewById(R.id.manual_checkIn_button);
        punchOutButton = findViewById(R.id.punch_out_button);
        logoutButton = findViewById(R.id.logout_button);
        buttonsContainer = findViewById(R.id.buttons_container);
    }

    private void setupInitialVisibility() {
        // Hide main content initially
        mainContentCard.setVisibility(View.INVISIBLE);

        // Hide individual buttons for staggered animation
        punchInButton.setVisibility(View.INVISIBLE);
        manualCheckInButton.setVisibility(View.INVISIBLE);
        punchOutButton.setVisibility(View.INVISIBLE);
        logoutButton.setVisibility(View.INVISIBLE);

        // Show loading animation
        loadingAnimation.setVisibility(View.VISIBLE);
    }

    private void setupClickListeners() {
        // Logout button click listener
        logoutButton.setOnClickListener(v -> handleLogout());

        // Punch In button click listener
        punchInButton.setOnClickListener(v -> navigateToPunchIn());

        // Punch Out button click listener
        punchOutButton.setOnClickListener(v -> navigateToPunchOut());

        // Manual Check In button click listener
        manualCheckInButton.setOnClickListener(v -> handleManualCheckIn());
    }

    private void setupBackPressHandling() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Toast.makeText(VisitorActivity.this, "Back pressed! : Logged Out", Toast.LENGTH_SHORT).show();
                finishAffinity(); // Closes all activities and exits app
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    private void startLoadingSequence() {
        // Simulate loading delay
        handler.postDelayed(this::startDashboardAnimations, 2000);
    }

    private void startDashboardAnimations() {
        // Fade out loading animation
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(loadingAnimation, "alpha", 1f, 0f);
        fadeOut.setDuration(500);
        fadeOut.start();

        // Hide loading and start content animations
        handler.postDelayed(() -> {
            loadingAnimation.setVisibility(View.GONE);
            animateMainContent();
        }, 500);
    }

    private void animateMainContent() {
        // Show main content card
        mainContentCard.setVisibility(View.VISIBLE);
        mainContentCard.setAlpha(0f);
        mainContentCard.setTranslationY(-100f);

        // Animate main content card entrance
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(mainContentCard, "alpha", 0f, 1f);
        ObjectAnimator translateY = ObjectAnimator.ofFloat(mainContentCard, "translationY", -100f, 0f);

        AnimatorSet contentSet = new AnimatorSet();
        contentSet.playTogether(fadeIn, translateY);
        contentSet.setDuration(600);
        contentSet.setInterpolator(new DecelerateInterpolator());
        contentSet.start();

        // Start button animations after content is visible
        handler.postDelayed(this::animateButtons, 400);
    }

    private void animateButtons() {
        // Animate buttons in sequence with staggered delays
        animateButton(punchOutButton, 0);
        animateButton(punchInButton, 100);
        animateButton(manualCheckInButton, 200);
        animateButton(logoutButton, 300);
    }

    private void animateButton(View button, long delay) {
        button.setVisibility(View.VISIBLE);
        button.setAlpha(0f);
        button.setScaleX(0.5f);
        button.setScaleY(0.5f);

        // Create bounce-in animation
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(button, "alpha", 0f, 1f);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(button, "scaleX", 0.5f, 1.1f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(button, "scaleY", 0.5f, 1.1f, 1f);

        AnimatorSet buttonSet = new AnimatorSet();
        buttonSet.playTogether(fadeIn, scaleX, scaleY);
        buttonSet.setStartDelay(delay);
        buttonSet.setDuration(500);
        buttonSet.setInterpolator(new DecelerateInterpolator());
        buttonSet.start();
    }

    // Click handler methods
    private void handleLogout() {
        // Add subtle animation feedback
        animateButtonPress(logoutButton);

        // Clear stored login data
        getSharedPreferences("AdminCred", MODE_PRIVATE).edit().clear().apply();

        // Navigate back to login screen
        Intent intent = new Intent(VisitorActivity.this, LoginSelectionActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToPunchIn() {
        // Add button press animation
        animateButtonPress(punchInButton);

        // Navigate to punch in screen
        Intent intent = new Intent(VisitorActivity.this, AdminPunchActivity.class);
        startActivity(intent);
    }

    private void navigateToPunchOut() {
        // Add button press animation
        animateButtonPress(punchOutButton);

        // Navigate to punch out screen
        Intent intent = new Intent(VisitorActivity.this, AdminCheckOutActivity.class);
        startActivity(intent);
    }

    private void handleManualCheckIn() {
        animateButtonPress(manualCheckInButton);
        Toast.makeText(this, "Manual Check-In clicked", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(VisitorActivity.this, AdminManualCheckIn.class);
        startActivity(intent);
    }

    private void animateButtonPress(View button) {
        // Create a subtle press animation for user feedback
        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(button, "scaleX", 1f, 0.95f);
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(button, "scaleY", 1f, 0.95f);
        ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(button, "scaleX", 0.95f, 1f);
        ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(button, "scaleY", 0.95f, 1f);

        AnimatorSet pressDown = new AnimatorSet();
        pressDown.playTogether(scaleDownX, scaleDownY);
        pressDown.setDuration(100);

        AnimatorSet pressUp = new AnimatorSet();
        pressUp.playTogether(scaleUpX, scaleUpY);
        pressUp.setDuration(100);

        AnimatorSet pressAnimation = new AnimatorSet();
        pressAnimation.playSequentially(pressDown, pressUp);
        pressAnimation.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reset any button states that might have been changed during press animations
        resetButtonStates();
    }

    private void resetButtonStates() {
        if (punchInButton != null) {
            punchInButton.setScaleX(1f);
            punchInButton.setScaleY(1f);
        }
        if (punchOutButton != null) {
            punchOutButton.setScaleX(1f);
            punchOutButton.setScaleY(1f);
        }
        if (manualCheckInButton != null) {
            manualCheckInButton.setScaleX(1f);
            manualCheckInButton.setScaleY(1f);
        }
        if (logoutButton != null) {
            logoutButton.setScaleX(1f);
            logoutButton.setScaleY(1f);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up handler to prevent memory leaks
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }
}
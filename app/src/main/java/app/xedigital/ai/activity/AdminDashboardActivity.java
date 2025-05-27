package app.xedigital.ai.activity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import app.xedigital.ai.R;
import app.xedigital.ai.adminActivity.AdminCheckOutActivity;
import app.xedigital.ai.adminActivity.AdminPunchActivity;

public class AdminDashboardActivity extends AppCompatActivity {

    private Handler handler;
    private View loadingAnimation;
    private View headerCard;
    private View punchInButton;
    private View logoutButton;
    private View checkOutButton;
    private View ManualCheckInButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_dasboard_activity);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        handler = new Handler(Looper.getMainLooper());

        // Initialize views
        loadingAnimation = findViewById(R.id.loading_animation);
        headerCard = findViewById(R.id.header_card);
        punchInButton = findViewById(R.id.punch_in_button);
        ManualCheckInButton = findViewById(R.id.manual_checkIn_button);
        checkOutButton = findViewById(R.id.punch_out_button);
        logoutButton = findViewById(R.id.logout_button);

        // Hide content initially
        headerCard.setVisibility(View.INVISIBLE);
        punchInButton.setVisibility(View.INVISIBLE);
        ManualCheckInButton.setVisibility(View.INVISIBLE);
        checkOutButton.setVisibility(View.INVISIBLE);
        logoutButton.setVisibility(View.INVISIBLE);

        // Show loading animation
        loadingAnimation.setVisibility(View.VISIBLE);

        // Simulate loading delay
        handler.postDelayed(this::startDashboardAnimations, 2000);

        logoutButton.setOnClickListener(v -> {
            // Clear stored login data
            getSharedPreferences("AdminCred", MODE_PRIVATE).edit().clear().apply();

            // Navigate back to login screen
            Intent intent = new Intent(AdminDashboardActivity.this, LoginSelectionActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
        punchInButton.setOnClickListener(v -> {
            // Navigate to punch in screen
            Intent intent = new Intent(AdminDashboardActivity.this, AdminPunchActivity.class);
            startActivity(intent);
        });
        checkOutButton.setOnClickListener(v -> {
            // Navigate to punch in screen
            Intent intent = new Intent(AdminDashboardActivity.this, AdminCheckOutActivity.class);
            startActivity(intent);
        });


        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Toast.makeText(AdminDashboardActivity.this, "Back pressed! : Logged Out", Toast.LENGTH_SHORT).show();
                finishAffinity(); // Closes all activities and exits app
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);

    }

    private void startDashboardAnimations() {
        // Fade out loading
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(loadingAnimation, "alpha", 1f, 0f);
        fadeOut.setDuration(500);
        fadeOut.start();

        handler.postDelayed(() -> {
            loadingAnimation.setVisibility(View.GONE);
            animateHeaderCard();
        }, 500);
    }

    private void animateHeaderCard() {
        headerCard.setVisibility(View.VISIBLE);
        headerCard.setAlpha(0f);
        headerCard.setTranslationY(-100f);

        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(headerCard, "alpha", 0f, 1f);
        ObjectAnimator translateY = ObjectAnimator.ofFloat(headerCard, "translationY", -100f, 0f);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(fadeIn, translateY);
        set.setDuration(600);
        set.setInterpolator(new DecelerateInterpolator());
        set.start();

        handler.postDelayed(this::animateButtons, 400);
    }

    private void animateButtons() {
        animateButton(checkOutButton, 0);
        animateButton(punchInButton, 50);
        animateButton(ManualCheckInButton, 100);
        animateButton(logoutButton, 150);
    }

    private void animateButton(View button, long delay) {
        button.setVisibility(View.VISIBLE);
        button.setAlpha(0f);
        button.setScaleX(0.8f);
        button.setScaleY(0.8f);

        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(button, "alpha", 0f, 1f);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(button, "scaleX", 0.8f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(button, "scaleY", 0.8f, 1f);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(fadeIn, scaleX, scaleY);
        set.setStartDelay(delay);
        set.setDuration(400);
        set.setInterpolator(new DecelerateInterpolator());
        set.start();
    }
//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//        finishAffinity();
//    }
}

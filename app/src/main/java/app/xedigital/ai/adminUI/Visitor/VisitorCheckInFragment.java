package app.xedigital.ai.adminUI.Visitor;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import app.xedigital.ai.R;
import app.xedigital.ai.adminActivity.AdminCheckOutActivity;
import app.xedigital.ai.adminActivity.AdminManualCheckIn;
import app.xedigital.ai.adminActivity.AdminPunchActivity;

public class VisitorCheckInFragment extends Fragment {

    private Handler handler;
    private View loadingAnimation;
    private View mainContentCard;
    private View punchInButton;
    private View punchOutButton;
    private View manualCheckInButton;

    public static VisitorCheckInFragment newInstance() {
        return new VisitorCheckInFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_visitor_check_in, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        handler = new Handler(Looper.getMainLooper());

        initializeViews(view);
        setupInitialVisibility();
        setupClickListeners();
        startLoadingSequence();
    }

    private void initializeViews(View root) {
        loadingAnimation = root.findViewById(R.id.loading_animation);
        mainContentCard = root.findViewById(R.id.main_content_card);
        punchInButton = root.findViewById(R.id.punch_in_button);
        manualCheckInButton = root.findViewById(R.id.manual_checkIn_button);
        punchOutButton = root.findViewById(R.id.punch_out_button);
    }

    private void setupInitialVisibility() {
        mainContentCard.setVisibility(View.INVISIBLE);
        punchInButton.setVisibility(View.INVISIBLE);
        manualCheckInButton.setVisibility(View.INVISIBLE);
        punchOutButton.setVisibility(View.INVISIBLE);
        loadingAnimation.setVisibility(View.VISIBLE);
    }

    private void setupClickListeners() {
        punchInButton.setOnClickListener(v -> navigateTo(AdminPunchActivity.class));
        punchOutButton.setOnClickListener(v -> navigateTo(AdminCheckOutActivity.class));
        manualCheckInButton.setOnClickListener(v -> {
            animateButtonPress(manualCheckInButton);
            startActivity(new Intent(requireContext(), AdminManualCheckIn.class));
        });
    }

    private void startLoadingSequence() {
        handler.postDelayed(this::startDashboardAnimations, 2000);
    }

    private void startDashboardAnimations() {
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(loadingAnimation, "alpha", 1f, 0f);
        fadeOut.setDuration(500);
        fadeOut.start();

        handler.postDelayed(() -> {
            loadingAnimation.setVisibility(View.GONE);
            animateMainContent();
        }, 500);
    }

    private void animateMainContent() {
        mainContentCard.setVisibility(View.VISIBLE);
        mainContentCard.setAlpha(0f);
        mainContentCard.setTranslationY(-100f);

        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(mainContentCard, "alpha", 0f, 1f);
        ObjectAnimator translateY = ObjectAnimator.ofFloat(mainContentCard, "translationY", -100f, 0f);

        AnimatorSet contentSet = new AnimatorSet();
        contentSet.playTogether(fadeIn, translateY);
        contentSet.setDuration(600);
        contentSet.setInterpolator(new DecelerateInterpolator());
        contentSet.start();

        handler.postDelayed(this::animateButtons, 400);
    }

    private void animateButtons() {
        animateButton(punchOutButton, 0);
        animateButton(punchInButton, 100);
        animateButton(manualCheckInButton, 200);
    }

    private void animateButton(View button, long delay) {
        button.setVisibility(View.VISIBLE);
        button.setAlpha(0f);
        button.setScaleX(0.5f);
        button.setScaleY(0.5f);

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

    private void navigateTo(Class<?> destination) {
        animateButtonPress(requireView().findFocus());
        startActivity(new Intent(requireContext(), destination));
    }

    private void animateButtonPress(View button) {
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
    public void onResume() {
        super.onResume();
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
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }
}

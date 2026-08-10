package app.xedigital.ai.ui.policy;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

import app.xedigital.ai.R;
import app.xedigital.ai.adapter.PolicyAdapter;
import app.xedigital.ai.databinding.FragmentPolicyBinding;
import app.xedigital.ai.model.policy.PoliciesItem;
import app.xedigital.ai.utills.SecurePrefManager;

public class PolicyFragment extends Fragment {

    private static final String TAG = "PolicyFragment";

    private FragmentPolicyBinding binding;
    private PolicyAdapter policyAdapter;
    private PolicyViewModel policyViewModel;
    private String authTokenHeader;
    private Animator shimmerAnimator;

    // ─────────────────────────────────────────────
    // Lifecycle
    // ─────────────────────────────────────────────

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPolicyBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        policyViewModel = new ViewModelProvider(this).get(PolicyViewModel.class);

        setupRecyclerView();
        setupSwipeRefresh();
        setupRetryButton();
        setupObservers();

        // Fetch initial data
        authTokenHeader = "jwt " + SecurePrefManager.getInstance(requireContext()).getString("authToken", "");

        loadPolicies();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopShimmer();
        binding = null;
    }

    // ─────────────────────────────────────────────
    // Setup
    // ─────────────────────────────────────────────

    private void setupRecyclerView() {
        policyAdapter = new PolicyAdapter(new ArrayList<>(), requireContext());
        binding.policyRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.policyRecyclerView.setAdapter(policyAdapter);
        binding.policyRecyclerView.setItemAnimator(new androidx.recyclerview.widget.DefaultItemAnimator());
    }

    private void setupSwipeRefresh() {
        binding.swipeRefreshLayout.setColorSchemeResources(R.color.policy_active, R.color.policy_inactive);
        binding.swipeRefreshLayout.setOnRefreshListener(this::loadPolicies);
    }

    private void setupRetryButton() {
        binding.btnRetry.setOnClickListener(v -> loadPolicies());
    }

    private void setupObservers() {
        policyViewModel.getPolicyData().observe(getViewLifecycleOwner(), policyResponse -> {

            stopShimmer();
            binding.progressBar.setVisibility(View.GONE);
            binding.swipeRefreshLayout.setRefreshing(false);

            if (policyResponse == null || policyResponse.getData() == null || policyResponse.getData().getPolicies() == null || policyResponse.getData().getPolicies().isEmpty()) {
                showEmptyState();
            } else {
                showContent(policyResponse.getData().getPolicies());
            }
        });
    }

    // ─────────────────────────────────────────────
    // State machine
    // ─────────────────────────────────────────────

    private void showLoading() {
        binding.swipeRefreshLayout.setVisibility(View.GONE);
        binding.emptyStateView.setVisibility(View.GONE);
        binding.progressBar.setVisibility(View.GONE);
        binding.loadingContainer.setVisibility(View.VISIBLE);
        startShimmer();
    }

    private void showContent(List<PoliciesItem> policies) {
        binding.loadingContainer.setVisibility(View.GONE);
        binding.emptyStateView.setVisibility(View.GONE);
        binding.swipeRefreshLayout.setVisibility(View.VISIBLE);
        binding.policyRecyclerView.setVisibility(View.VISIBLE);

        policyAdapter.updatePolicies(policies);

        // Subtle fade-in
        binding.policyRecyclerView.setAlpha(0f);
        binding.policyRecyclerView.animate().alpha(1f).setDuration(300).start();
    }

    private void showEmptyState() {
        binding.loadingContainer.setVisibility(View.GONE);
        binding.swipeRefreshLayout.setVisibility(View.GONE);
        binding.emptyStateView.setVisibility(View.VISIBLE);

        // Bounce-in the empty state
        binding.emptyStateView.setScaleX(0.85f);
        binding.emptyStateView.setScaleY(0.85f);
        binding.emptyStateView.setAlpha(0f);
        binding.emptyStateView.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(350).setInterpolator(new android.view.animation.OvershootInterpolator(1.2f)).start();
    }

    // ─────────────────────────────────────────────
    // Shimmer helpers
    // ─────────────────────────────────────────────

    private void startShimmer() {
        try {
            shimmerAnimator = AnimatorInflater.loadAnimator(requireContext(), R.animator.shimmer_anim);
            shimmerAnimator.setTarget(binding.loadingContainer);
            shimmerAnimator.start();
        } catch (Exception e) {
            Log.w(TAG, "Shimmer animator not found, skipping", e);
        }
    }

    private void stopShimmer() {
        if (shimmerAnimator != null && shimmerAnimator.isRunning()) {
            shimmerAnimator.cancel();
            shimmerAnimator = null;
        }
        if (binding != null) {
            binding.loadingContainer.setAlpha(1f);
        }
    }

    // ─────────────────────────────────────────────
    // Data loading
    // ─────────────────────────────────────────────

    private void loadPolicies() {
        if (!binding.swipeRefreshLayout.isRefreshing()) {
            showLoading();
        }
        policyViewModel.fetchPolicies(authTokenHeader);
    }
}
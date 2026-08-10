package app.xedigital.ai.ui.vms;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import app.xedigital.ai.R;
import app.xedigital.ai.adapter.VisitorClickListener;
import app.xedigital.ai.adapter.VisitorsAdapter;
import app.xedigital.ai.databinding.FragmentVMSBinding;
import app.xedigital.ai.model.vms.VisitorsItem;
import app.xedigital.ai.utills.SecurePrefManager;

public class VmsFragment extends Fragment implements VisitorClickListener {

    private static final String TAG = "VmsFragment";

    private FragmentVMSBinding binding;
    private VmsViewModel mViewModel;
    private VisitorsAdapter visitorsAdapter;

    public static VmsFragment newInstance() {
        return new VmsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        binding = FragmentVMSBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mViewModel = new ViewModelProvider(this).get(VmsViewModel.class);

        setupRecyclerView();
        setupChips();
        setupSwipeRefresh();
        setupObservers();
        loadVisitors();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // ─────────────────────────────────────────────
    // Setup
    // ─────────────────────────────────────────────

    private void setupRecyclerView() {
        visitorsAdapter = new VisitorsAdapter(null, this);
        binding.VisitorsListRecyclerView.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );
        binding.VisitorsListRecyclerView.setAdapter(visitorsAdapter);
        binding.VisitorsListRecyclerView.setItemAnimator(
                new androidx.recyclerview.widget.DefaultItemAnimator()
        );
    }

    private void setupChips() {
        // Pre-Approved Visitors chip
        binding.preApprovedVisitorChip.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_nav_vms_to_nav_preApproved_visitors);
            Toast.makeText(
                    requireContext(),
                    "Pre-Approved Visitors",
                    Toast.LENGTH_SHORT
            ).show();
        });

        // Check In Visitors chip — opens external URL
        binding.checkVisitorsChip.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Check Visitors", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://app.xedigital.ai/checkin/home")
            );
            startActivity(intent);
        });
    }

    private void setupSwipeRefresh() {
        binding.swipeRefreshLayout.setColorSchemeResources(
                R.color.colorPrimary,
                R.color.RestrictedHoliday,
                R.color.NationalHoliday
        );
        binding.swipeRefreshLayout.setOnRefreshListener(this::loadVisitors);
    }

    private void setupObservers() {
        // Visitors list observer
        mViewModel.getVisitors().observe(getViewLifecycleOwner(), visitorsItems -> {
            if (visitorsAdapter != null) {
                visitorsAdapter.updateVisitors(visitorsItems);
            }
            updateUIState(
                    visitorsItems == null || visitorsItems.isEmpty(),
                    false
            );
        });

        // Loading state observer
        mViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            updateUIState(false, isLoading);
        });

        // Error observer
        mViewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Log.e(TAG, "Error: " + error);
                Toast.makeText(
                        requireContext(),
                        "Error loading visitors",
                        Toast.LENGTH_SHORT
                ).show();
                updateUIState(true, false);
                binding.swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    // ─────────────────────────────────────────────
    // State management
    // ─────────────────────────────────────────────

    private void updateUIState(boolean isEmpty, boolean isLoading) {
        if (isLoading) {
            // Show loading state
            binding.loadingProgress.setVisibility(View.VISIBLE);
            binding.emptyStateContainer.setVisibility(View.GONE);
            binding.VisitorsListRecyclerView.setVisibility(View.GONE);
            binding.swipeRefreshLayout.setRefreshing(true);
        } else if (isEmpty) {
            // Show empty state
            binding.loadingProgress.setVisibility(View.GONE);
            binding.emptyStateContainer.setVisibility(View.VISIBLE);
            binding.VisitorsListRecyclerView.setVisibility(View.GONE);
            binding.swipeRefreshLayout.setRefreshing(false);
        } else {
            // Show content
            binding.loadingProgress.setVisibility(View.GONE);
            binding.emptyStateContainer.setVisibility(View.GONE);
            binding.VisitorsListRecyclerView.setVisibility(View.VISIBLE);
            binding.swipeRefreshLayout.setRefreshing(false);

            // Fade-in animation for list
            binding.VisitorsListRecyclerView.setAlpha(0f);
            binding.VisitorsListRecyclerView
                    .animate()
                    .alpha(1f)
                    .setDuration(300)
                    .start();
        }
    }

    // ─────────────────────────────────────────────
    // Data loading
    // ─────────────────────────────────────────────

    private void loadVisitors() {
        String authToken = SecurePrefManager
                .getInstance(requireContext())
                .getString("authToken", "");
        mViewModel.fetchVisitors(authToken);
    }

    // ─────────────────────────────────────────────
    // VisitorClickListener implementation
    // ─────────────────────────────────────────────

    @Override
    public void onVisitorClicked(VisitorsItem visitor) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("visitor", visitor);

        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(R.id.action_nav_vms_to_nav_visitor_details, bundle);
    }
}
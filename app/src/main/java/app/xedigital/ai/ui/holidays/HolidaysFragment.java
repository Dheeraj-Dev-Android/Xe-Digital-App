package app.xedigital.ai.ui.holidays;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import app.xedigital.ai.R;
import app.xedigital.ai.adapter.HolidayAdapter;
import app.xedigital.ai.databinding.FragmentHolidaysBinding;
import app.xedigital.ai.model.holiday.HolidaysItem;
import app.xedigital.ai.utills.SecurePrefManager;

public class HolidaysFragment extends Fragment {

    private static final String TAG = "HolidaysFragment";

    private FragmentHolidaysBinding binding;
    private HolidayAdapter holidayAdapter;
    private HolidaysViewModel holidaysViewModel;
    private String authToken;
    private Animator shimmerAnimator;

    // ─────────────────────────────────────────────
    // Lifecycle
    // ─────────────────────────────────────────────

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHolidaysBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        holidaysViewModel = new ViewModelProvider(this).get(HolidaysViewModel.class);
        setupRecyclerView();
        setupSwipeRefresh();
        setupRetryButton();
        setupObservers();

        // Load data
        authToken = SecurePrefManager.getInstance(requireContext()).getString("authToken", "");

        loadHolidays();
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
        holidayAdapter = new HolidayAdapter();
        binding.recyclerViewHolidays.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewHolidays.setAdapter(holidayAdapter);

        // Smooth scroll-in animation for each item as it appears
        binding.recyclerViewHolidays.setItemAnimator(new androidx.recyclerview.widget.DefaultItemAnimator());
    }

    private void setupSwipeRefresh() {
        // Match your brand colors
        binding.swipeRefreshLayout.setColorSchemeResources(R.color.RestrictedHoliday, R.color.NationalHoliday);
        binding.swipeRefreshLayout.setOnRefreshListener(this::loadHolidays);
    }

    private void setupRetryButton() {
        binding.btnRetry.setOnClickListener(v -> loadHolidays());
    }

    private void setupObservers() {
        holidaysViewModel.getHolidaysList().observe(getViewLifecycleOwner(), holidays -> {

            stopShimmer();
            binding.progressBar.setVisibility(View.GONE);
            binding.swipeRefreshLayout.setRefreshing(false);

            if (holidays == null || holidays.isEmpty()) {
                showEmptyState();
            } else {
                showContent(holidays);
            }
        });
    }

    // ─────────────────────────────────────────────
    // State management
    // ─────────────────────────────────────────────

    /**
     * Show skeleton shimmer while loading.
     * Falls back to plain ProgressBar if shimmer container
     * somehow isn't available.
     */
    private void showLoading() {
        binding.swipeRefreshLayout.setVisibility(View.GONE);
        binding.emptyStateView.setVisibility(View.GONE);
        binding.progressBar.setVisibility(View.GONE);
        binding.loadingContainer.setVisibility(View.VISIBLE);
        startShimmer();
    }

    private void showContent(List<HolidaysItem> holidays) {
        binding.loadingContainer.setVisibility(View.GONE);
        binding.emptyStateView.setVisibility(View.GONE);
        binding.swipeRefreshLayout.setVisibility(View.VISIBLE);
        binding.recyclerViewHolidays.setVisibility(View.VISIBLE);

        List<HolidaysItem> sorted = sortHolidaysByDate(holidays);
        holidayAdapter.updateHolidays(sorted);

        // Subtle fade-in for the list
        binding.recyclerViewHolidays.setAlpha(0f);
        binding.recyclerViewHolidays.animate().alpha(1f).setDuration(300).start();
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

    private void loadHolidays() {
        // Only show full shimmer on first load;
        // on swipe-refresh the pull indicator is enough
        if (!binding.swipeRefreshLayout.isRefreshing()) {
            showLoading();
        }
        holidaysViewModel.loadHolidays(authToken);
    }

    // ─────────────────────────────────────────────
    // Sorting
    // ─────────────────────────────────────────────

    private List<HolidaysItem> sortHolidaysByDate(List<HolidaysItem> holidays) {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());

        List<Pair<Date, HolidaysItem>> paired = new ArrayList<>();
        for (HolidaysItem holiday : holidays) {
            try {
                Date date = fmt.parse(holiday.getHolidayDate());
                if (date != null) {
                    paired.add(new Pair<>(date, holiday));
                }
            } catch (ParseException e) {
                Log.e(TAG, "Error parsing date for: " + holiday.getHolidayName(), e);
            }
        }

        paired.sort((a, b) -> {
            Calendar calA = Calendar.getInstance();
            calA.setTime(a.first);

            Calendar calB = Calendar.getInstance();
            calB.setTime(b.first);

            int monthCompare = Integer.compare(calA.get(Calendar.MONTH), calB.get(Calendar.MONTH));
            if (monthCompare != 0) return monthCompare;

            return Integer.compare(calA.get(Calendar.DAY_OF_MONTH), calB.get(Calendar.DAY_OF_MONTH));
        });

        List<HolidaysItem> sorted = new ArrayList<>();
        for (Pair<Date, HolidaysItem> pair : paired) {
            sorted.add(pair.second);
        }
        return sorted;
    }
}
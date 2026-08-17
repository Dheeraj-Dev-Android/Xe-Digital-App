package app.xedigital.ai.adminUI.adminDashboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import app.xedigital.ai.R;
import app.xedigital.ai.model.Admin.LeaveGraph.Data;

public class AdminDashboardFragment extends Fragment {

    private static final String TAG = "AdminDashboardFragment";
    private static final int SCROLL_INTERVAL = 3000;
    private static final String FALLBACK_SHORT = "N/A";

    private final Handler scrollHandler = new Handler(Looper.getMainLooper());
    private AdminDashboardViewModel mViewModel;
    private BirthdayEmployeesAdapter birthdayAdapter;
    private String token;
    private TextView totalSignin, totalSignout, totalEmployees, totalBranches;
    private TextView birthdayCount;
    private RecyclerView birthdayRecyclerView;
    private LinearLayout emptyBirthdayState;
    private PieChart leavesBarChart;
    private int currentPosition = 0;
    private boolean shouldAutoScroll = false;
    private final Runnable scrollRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isAdded() || birthdayAdapter == null || birthdayRecyclerView == null) {
                return;
            }
            int itemCount = birthdayAdapter.getItemCount();
            if (itemCount > 1) {
                currentPosition = (currentPosition + 1) % itemCount;
                birthdayRecyclerView.smoothScrollToPosition(currentPosition);
                scrollHandler.postDelayed(this, SCROLL_INTERVAL);
            } else {
                // Stop auto-scroll silently if items dropped to 1 or 0
                shouldAutoScroll = false;
            }
        }
    };
    private MaterialCardView btnPunchAttendance;

    public static AdminDashboardFragment newInstance() {
        return new AdminDashboardFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getContext() != null) {
            SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("AdminCred", Context.MODE_PRIVATE);
            token = sharedPreferences.getString("authToken", "");
        }
        return inflater.inflate(R.layout.fragment_admin_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        setupRecyclerView();
        setupViewModel();

        if (token != null && !token.isEmpty()) {
            String authToken = "jwt " + token;
            mViewModel.fetchDashboardData(authToken);
            mViewModel.fetchBirthdayData(authToken);
            mViewModel.fetchLeavesGraph(authToken);
        } else {
            setDashboardDefaultTexts();
        }
        btnPunchAttendance.setOnClickListener(v -> {
            try {
                NavController navController = Navigation.findNavController(v);
                // Use action ID — not raw destination ID
                navController.navigate(R.id.action_dashboard_to_visitorCheckIn);
            } catch (IllegalStateException e) {
                Log.e(TAG, "NavController not found: " + e.getMessage());
            }
        });
    }

    private void initializeViews(View view) {
        totalSignin = view.findViewById(R.id.signinCount);
        totalSignout = view.findViewById(R.id.signoutCount);
        totalEmployees = view.findViewById(R.id.employeesCount);
        totalBranches = view.findViewById(R.id.branchesCount);
        birthdayCount = view.findViewById(R.id.birthdayCount);
        birthdayRecyclerView = view.findViewById(R.id.birthdayRecyclerView);
        emptyBirthdayState = view.findViewById(R.id.emptyBirthdayState);
        leavesBarChart = view.findViewById(R.id.pieChart);
        btnPunchAttendance = view.findViewById(R.id.btnPunchAttendance);
    }

    private void setupRecyclerView() {
        birthdayAdapter = new BirthdayEmployeesAdapter();
        BirthdayEmployeesAdapter.setupHorizontalScrolling(birthdayRecyclerView);
        birthdayRecyclerView.setAdapter(birthdayAdapter);
        birthdayRecyclerView.setNestedScrollingEnabled(false);

        PagerSnapHelper snapHelper = new PagerSnapHelper();
        birthdayRecyclerView.setOnFlingListener(null);
        snapHelper.attachToRecyclerView(birthdayRecyclerView);
    }

    private void setupViewModel() {
        mViewModel = new ViewModelProvider(this).get(AdminDashboardViewModel.class);

        mViewModel.getDashboardData().observe(getViewLifecycleOwner(), data -> {
            if (data != null) {
                String totalVisitors = String.valueOf(data.getTotalVisitors());
                String checkInVisitors = String.valueOf(data.getTotalSigninVisitors());
                String checkOutVisitors = String.valueOf(data.getTotalSignoutVisitors());

                totalSignin.setText(String.format("%s / %s", checkInVisitors, totalVisitors));
                totalSignout.setText(String.format("%s / %s", checkOutVisitors, totalVisitors));
                totalEmployees.setText(String.valueOf(data.getTotalEmployees()));
                totalBranches.setText(String.valueOf(data.getTotalBranches()));
            } else {
                setDashboardDefaultTexts();
            }
        });

        mViewModel.getBirthdayData().observe(getViewLifecycleOwner(), birthdayEmployees -> {
            scrollHandler.removeCallbacks(scrollRunnable);

            if (birthdayEmployees != null && !birthdayEmployees.isEmpty()) {
                birthdayCount.setText(String.valueOf(birthdayEmployees.size()));
                birthdayAdapter.updateBirthdayEmployees(birthdayEmployees);

                birthdayRecyclerView.setVisibility(View.VISIBLE);
                emptyBirthdayState.setVisibility(View.GONE);

                currentPosition = 0;

                // FIX: Only enable auto-scroll when there are multiple items.
                // Scrolling a single-item list is wasteful and looks broken.
                if (birthdayEmployees.size() > 1) {
                    shouldAutoScroll = true;
                    scrollHandler.postDelayed(scrollRunnable, SCROLL_INTERVAL);
                } else {
                    shouldAutoScroll = false;
                }
            } else {
                birthdayCount.setText("0");
                birthdayAdapter.updateBirthdayEmployees(new ArrayList<>());
                birthdayRecyclerView.setVisibility(View.GONE);
                emptyBirthdayState.setVisibility(View.VISIBLE);
                shouldAutoScroll = false;
            }
        });

        mViewModel.getLeavesGraphData().observe(getViewLifecycleOwner(), response -> {
            if (response != null && response.getData() != null) {
                updatePieChart(response.getData());
            } else {
                showChartEmptyState();
            }
        });

        mViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (getContext() != null && errorMessage != null && !errorMessage.trim().isEmpty()) {
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setDashboardDefaultTexts() {
        if (totalSignin != null) totalSignin.setText(FALLBACK_SHORT);
        if (totalSignout != null) totalSignout.setText(FALLBACK_SHORT);
        if (totalEmployees != null) totalEmployees.setText(FALLBACK_SHORT);
        if (totalBranches != null) totalBranches.setText(FALLBACK_SHORT);
    }

    private void showChartEmptyState() {
        if (leavesBarChart != null) {
            leavesBarChart.clear();
            leavesBarChart.setNoDataText("No Data Available");
            leavesBarChart.setNoDataTextColor(Color.GRAY);
            leavesBarChart.invalidate();
        }
    }

    private void updatePieChart(Data leaveGraphData) {
        if (leavesBarChart == null) return;

        if (leaveGraphData == null || leaveGraphData.getGraphData() == null || leaveGraphData.getGraphLabels() == null) {
            showChartEmptyState();
            return;
        }

        List<Integer> values = leaveGraphData.getGraphData();
        List<String> labels = leaveGraphData.getGraphLabels();
        List<String> colors = leaveGraphData.getGraphBackground();

        if (values.isEmpty() || labels.isEmpty() || values.size() != labels.size()) {
            showChartEmptyState();
            return;
        }

        List<PieEntry> entries = new ArrayList<>();
        for (int i = 0; i < values.size(); i++) {
            Integer val = values.get(i);
            String label = labels.get(i);
            entries.add(new PieEntry(val != null ? val : 0f, label != null ? label : "Data Not Available"));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setSliceSpace(1f);
        dataSet.setSelectionShift(10f);

        List<Integer> colorInts = new ArrayList<>();
        if (colors != null) {
            for (String colorName : colors) {
                try {
                    if (colorName != null && !colorName.trim().isEmpty()) {
                        colorInts.add(Color.parseColor(colorName));
                    } else {
                        colorInts.add(Color.LTGRAY);
                    }
                } catch (Exception e) {
                    colorInts.add(Color.LTGRAY);
                }
            }
        }
        if (colorInts.isEmpty()) colorInts.add(Color.GRAY);
        dataSet.setColors(colorInts);

        PieData data = new PieData(dataSet);
        data.setDrawValues(false);

        leavesBarChart.getDescription().setEnabled(false);
        leavesBarChart.setDrawHoleEnabled(false);
        leavesBarChart.setDrawEntryLabels(false);

        Legend legend = leavesBarChart.getLegend();
        if (legend != null) legend.setEnabled(false);

        leavesBarChart.setData(data);

        // FIX: Pass pre-summed total to OptimizedMarkerView
        // so it doesn't recalculate on every highlight tap
        OptimizedMarkerView mv = new OptimizedMarkerView(leavesBarChart.getContext(), R.layout.marker_view, values);
        mv.setChartView(leavesBarChart);
        leavesBarChart.setMarker(mv);

        leavesBarChart.animateY(1400, Easing.EaseInOutQuad);
        leavesBarChart.invalidate();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Restart scroll only if conditions still valid after resume
        if (shouldAutoScroll && birthdayAdapter != null && birthdayAdapter.getItemCount() > 1) {
            scrollHandler.removeCallbacks(scrollRunnable);
            scrollHandler.postDelayed(scrollRunnable, SCROLL_INTERVAL);
        }
    }

    @Override
    public void onPause() {
        scrollHandler.removeCallbacks(scrollRunnable);
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        scrollHandler.removeCallbacks(scrollRunnable);

        // Null out all view references to prevent memory leaks
        // when fragment stays on back stack but view is destroyed
        totalSignin = null;
        totalSignout = null;
        totalEmployees = null;
        totalBranches = null;
        birthdayCount = null;
        birthdayRecyclerView = null;
        emptyBirthdayState = null;
        leavesBarChart = null;
        birthdayAdapter = null;
        btnPunchAttendance = null;

        super.onDestroyView();
    }

    // ─────────────────────────────────────────────────────────────
    // OptimizedMarkerView
    // FIX: Total is computed ONCE in constructor, not on every tap
    // ─────────────────────────────────────────────────────────────
    private static class OptimizedMarkerView extends MarkerView {

        private final TextView markerTextView;
        private final float precomputedTotal; // computed once

        public OptimizedMarkerView(Context context, int layoutResource, List<Integer> values) {
            super(context, layoutResource);

            // Pre-calculate total here instead of in refreshContent
            float sum = 0f;
            if (values != null) {
                for (Integer val : values) {
                    if (val != null) sum += val;
                }
            }
            this.precomputedTotal = sum;
            this.markerTextView = findViewById(R.id.marker_text);
        }

        @Override
        public void refreshContent(Entry e, Highlight highlight) {
            if (e instanceof PieEntry && markerTextView != null) {
                PieEntry pieEntry = (PieEntry) e;

                // FIX: Use pre-computed total — O(1) instead of O(n) per tap
                float percentage = (precomputedTotal > 0) ? (pieEntry.getValue() / precomputedTotal) * 100f : 0f;

                String label = pieEntry.getLabel() != null ? pieEntry.getLabel() : "Data Not Available";

                markerTextView.setText(String.format(Locale.US, "%s\n%.1f%%", label, percentage));
            }
            super.refreshContent(e, highlight);
        }

        @Override
        public MPPointF getOffset() {
            return new MPPointF(-(getWidth() / 2f), -getHeight());
        }
    }
}
package app.xedigital.ai.adminUI.adminDashboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import app.xedigital.ai.R;
import app.xedigital.ai.model.Admin.LeaveGraph.Data;

public class AdminDashboardFragment extends Fragment {

    private final int SCROLL_INTERVAL = 3000;
    private final android.os.Handler scrollHandler = new android.os.Handler();
    PieChart leavesBarChart;
    private AdminDashboardViewModel mViewModel;
    private BirthdayEmployeesAdapter birthdayAdapter;
    private String token;
    private TextView totalSignin, totalSignout, totalEmployees, totalBranches;
    private TextView birthdayCount;
    private RecyclerView birthdayRecyclerView;
    private LinearLayout emptyBirthdayState;
    private int currentPosition = 0;
    private final Runnable scrollRunnable = new Runnable() {
        @Override
        public void run() {
            if (birthdayAdapter != null && birthdayAdapter.getItemCount() > 0) {
                currentPosition = (currentPosition + 1) % birthdayAdapter.getItemCount();
                birthdayRecyclerView.smoothScrollToPosition(currentPosition);
                scrollHandler.postDelayed(this, SCROLL_INTERVAL);
            }
        }
    };


    public static AdminDashboardFragment newInstance() {
        return new AdminDashboardFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("AdminCred", Context.MODE_PRIVATE);
        token = sharedPreferences.getString("authToken", "");

        return inflater.inflate(R.layout.fragment_admin_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        setupRecyclerView();
        setupViewModel();

        String authToken = "jwt " + token;
        mViewModel.fetchDashboardData(authToken);
        mViewModel.fetchBirthdayData(authToken);
        mViewModel.fetchLeavesGraph(authToken);
    }

    private void initializeViews(View view) {
        // Dashboard counters
        totalSignin = view.findViewById(R.id.signinCount);
        totalSignout = view.findViewById(R.id.signoutCount);
        totalEmployees = view.findViewById(R.id.employeesCount);
        totalBranches = view.findViewById(R.id.branchesCount);

        // Birthday components
        birthdayCount = view.findViewById(R.id.birthdayCount);
        birthdayRecyclerView = view.findViewById(R.id.birthdayRecyclerView);
        emptyBirthdayState = view.findViewById(R.id.emptyBirthdayState);

        leavesBarChart = view.findViewById(R.id.pieChart);
    }

    private void setupRecyclerView() {
        birthdayAdapter = new BirthdayEmployeesAdapter();
        BirthdayEmployeesAdapter.setupHorizontalScrolling(birthdayRecyclerView);
        birthdayRecyclerView.setAdapter(birthdayAdapter);
        birthdayRecyclerView.setNestedScrollingEnabled(false);
        androidx.recyclerview.widget.PagerSnapHelper snapHelper = new androidx.recyclerview.widget.PagerSnapHelper();
        snapHelper.attachToRecyclerView(birthdayRecyclerView);
    }

    private void setupViewModel() {
        mViewModel = new ViewModelProvider(this).get(AdminDashboardViewModel.class);

        // Observe dashboard data
        mViewModel.getDashboardData().observe(getViewLifecycleOwner(), data -> {
            if (data != null) {
                String totalVisitors = String.valueOf(data.getTotalVisitors());
                String checkInVisitors = String.valueOf(data.getTotalSigninVisitors());
                String checkOutVisitors = String.valueOf(data.getTotalSignoutVisitors());

                totalSignin.setText(checkInVisitors + " / " + totalVisitors);
                totalSignout.setText(checkOutVisitors + " / " + totalVisitors);
                totalEmployees.setText(String.valueOf(data.getTotalEmployees()));
                totalBranches.setText(String.valueOf(data.getTotalBranches()));
            } else {
                totalSignin.setText("N/A");
                totalSignout.setText("N/A");
                totalEmployees.setText("N/A");
                totalBranches.setText("N/A");
            }
        });

        // Observe birthday data
        mViewModel.getBirthdayData().observe(getViewLifecycleOwner(), birthdayEmployees -> {
            if (birthdayEmployees != null && !birthdayEmployees.isEmpty()) {
                // Show birthday data
                birthdayCount.setText(String.valueOf(birthdayEmployees.size()));
                birthdayAdapter.updateBirthdayEmployees(birthdayEmployees);

                // Show RecyclerView and hide empty state
                birthdayRecyclerView.setVisibility(View.VISIBLE);
                emptyBirthdayState.setVisibility(View.GONE);

                // 👉 Start auto-scroll
                scrollHandler.removeCallbacks(scrollRunnable);
                scrollHandler.postDelayed(scrollRunnable, SCROLL_INTERVAL);
            } else {
                // Show empty state
                birthdayCount.setText("0");
                birthdayAdapter.updateBirthdayEmployees(null);

                // Hide RecyclerView and show empty state
                birthdayRecyclerView.setVisibility(View.GONE);
                emptyBirthdayState.setVisibility(View.VISIBLE);

                // 👉 Stop auto-scroll
                scrollHandler.removeCallbacks(scrollRunnable);
            }
        });
//        mViewModel.getLeavesGraphData().observe(getViewLifecycleOwner(), leaveGraphResponse -> {
//            if (leaveGraphResponse != null) {
//                updatePieChart(leaveGraphResponse);
//            } else {
//                Toast.makeText(getContext(), "Failed to fetch leave graph data", Toast.LENGTH_SHORT).show();
//            }
//        });
        mViewModel.getLeavesGraphData().observe(getViewLifecycleOwner(), response -> {
            if (response != null && response.getData() != null) {
                updatePieChart(response.getData());
            }
        });

        // Observe loading state
        mViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            // You can show/hide a progress bar here if needed
            // For now, we'll just handle it silently
        });

        // Observe error messages
        mViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updatePieChart(Data leaveGraphData) {
        PieChart pieChart = leavesBarChart;

        List<Integer> values = leaveGraphData.getGraphData();
        List<String> labels = leaveGraphData.getGraphLabels();
        List<String> colors = leaveGraphData.getGraphBackground();

        // Prepare entries
        List<PieEntry> entries = new ArrayList<>();
        for (int i = 0; i < values.size(); i++) {
            entries.add(new PieEntry(values.get(i), labels.get(i)));
        }

        // Dataset
        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setSliceSpace(1f);
        dataSet.setSelectionShift(10f);

        // Set colors
        List<Integer> colorInts = new ArrayList<>();
        for (String colorName : colors) {
            try {
                colorInts.add(Color.parseColor(colorName));
            } catch (IllegalArgumentException e) {
                colorInts.add(Color.rgb((int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255)));
            }
        }
        dataSet.setColors(colorInts);

        // Hide default slice values
//        PieData data = new PieData(dataSet);
//        data.setDrawValues(false); // ✅ hides % on chart initially
//
//        pieChart.setData(data);
//        pieChart.setUsePercentValues(true);
//        pieChart.setDrawHoleEnabled(true);
//        pieChart.setHoleRadius(0f);
//        pieChart.setHoleColor(Color.TRANSPARENT);
//        pieChart.setTransparentCircleAlpha(0);
//        pieChart.setCenterText("");
//        pieChart.setEntryLabelColor(Color.TRANSPARENT);
//        pieChart.setEntryLabelTextSize(0f);
//        pieChart.getDescription().setEnabled(false);

        PieData data = new PieData(dataSet);
        data.setDrawValues(false);
        pieChart.setData(data);

        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawHoleEnabled(false);
        pieChart.setHoleRadius(25f);
        pieChart.setCenterText("");
        pieChart.setTransparentCircleRadius(45f);
        pieChart.animateXY(1000, 1000);
        pieChart.setDrawEntryLabels(false);
        pieChart.invalidate();

        // Legend setup
        Legend legend = pieChart.getLegend();
        legend.setEnabled(false);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setDrawInside(false);
        legend.setWordWrapEnabled(true);
        legend.setTextSize(8f);
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setXEntrySpace(8f);
        legend.setYEntrySpace(8f);

        // Custom MarkerView to show selected info on chart
        MarkerView mv = new MarkerView(pieChart.getContext(), R.layout.marker_view) {
            @Override
            public void refreshContent(Entry e, Highlight highlight) {
                if (e instanceof PieEntry) {
                    PieEntry pieEntry = (PieEntry) e;
                    float total = 0f;
                    for (int val : values) total += val;
                    float percentage = (pieEntry.getValue() / total) * 100f;

                    TextView tv = findViewById(R.id.marker_text);
                    tv.setText(pieEntry.getLabel() + "\n" + String.format(Locale.US, "%.1f", percentage) + "%");
                }
                super.refreshContent(e, highlight);
            }

            @Override
            public MPPointF getOffset() {
                return new MPPointF(-(getWidth() / 2f), -getHeight());
            }
        };
        pieChart.setMarker(mv);

        pieChart.animateY(1400, Easing.EaseInOutQuad);
        pieChart.invalidate();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Stop auto-scroll when view is destroyed
        scrollHandler.removeCallbacks(scrollRunnable);
    }

}
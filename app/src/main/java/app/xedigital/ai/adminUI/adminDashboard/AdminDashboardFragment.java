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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import app.xedigital.ai.R;
import app.xedigital.ai.model.Admin.LeaveGraph.Data;

public class AdminDashboardFragment extends Fragment {

    private final int SCROLL_INTERVAL = 3000;
    private final android.os.Handler scrollHandler = new android.os.Handler();
    private final Runnable scrollRunnable = new Runnable() {
        @Override
        public void run() {
            if (isAdded() && birthdayAdapter != null && birthdayRecyclerView != null) {
                int itemCount = birthdayAdapter.getItemCount();
                if (itemCount > 0) {
                    currentPosition = (currentPosition + 1) % itemCount;
                    birthdayRecyclerView.smoothScrollToPosition(currentPosition);
                    scrollHandler.postDelayed(this, SCROLL_INTERVAL);
                }
            }
        }
    };
    private AdminDashboardViewModel mViewModel;
    private BirthdayEmployeesAdapter birthdayAdapter;
    private String token;
    private TextView totalSignin, totalSignout, totalEmployees, totalBranches;
    private TextView birthdayCount;
    private RecyclerView birthdayRecyclerView;
    private LinearLayout emptyBirthdayState;
    private int currentPosition = 0;
    private PieChart leavesBarChart;

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
                // Wrap conversions defensively to confirm fields exist safely
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
                scrollHandler.postDelayed(scrollRunnable, SCROLL_INTERVAL);
            } else {
                birthdayCount.setText("0");
                birthdayAdapter.updateBirthdayEmployees(new ArrayList<>());
                birthdayRecyclerView.setVisibility(View.GONE);
                emptyBirthdayState.setVisibility(View.VISIBLE);
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
        String missingLabel = "Data Not Available";
        if (totalSignin != null) totalSignin.setText(missingLabel);
        if (totalSignout != null) totalSignout.setText(missingLabel);
        if (totalEmployees != null) totalEmployees.setText(missingLabel);
        if (totalBranches != null) totalBranches.setText(missingLabel);
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

        OptimizedMarkerView mv = new OptimizedMarkerView(leavesBarChart.getContext(), R.layout.marker_view, values);
        mv.setChartView(leavesBarChart);
        leavesBarChart.setMarker(mv);

        leavesBarChart.animateY(1400, Easing.EaseInOutQuad);
        leavesBarChart.invalidate();
    }

    @Override
    public void onPause() {
        scrollHandler.removeCallbacks(scrollRunnable);
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        scrollHandler.removeCallbacks(scrollRunnable);
        super.onDestroyView();
    }

    private static class OptimizedMarkerView extends MarkerView {
        private final TextView markerTextView;
        private final List<Integer> valueList;

        public OptimizedMarkerView(Context context, int layoutResource, List<Integer> values) {
            super(context, layoutResource);
            this.valueList = values != null ? values : new ArrayList<>();
            this.markerTextView = findViewById(R.id.marker_text);
        }

        @Override
        public void refreshContent(Entry e, Highlight highlight) {
            if (e instanceof PieEntry && markerTextView != null) {
                PieEntry pieEntry = (PieEntry) e;
                float total = 0f;
                for (Integer val : valueList) {
                    if (val != null) total += val;
                }
                float percentage = (total > 0) ? (pieEntry.getValue() / total) * 100f : 0f;
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
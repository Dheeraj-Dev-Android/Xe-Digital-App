package app.xedigital.ai.ui.leaves;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import app.xedigital.ai.R;
import app.xedigital.ai.adapter.LeaveAdapter;
import app.xedigital.ai.model.leaves.LeavesItem;

public class LeavesDataFragment extends Fragment {

    private final List<LeavesItem> leaveList = new ArrayList<>();
    private LeavesViewModel leavesViewModel;
    private LeaveAdapter leaveAdapter;
    private RecyclerView recyclerViewLeaves;
    private TextView emptyStateText;
    private ProgressBar loadingProgress;
    private PieChart leavePieChart;

    public LeavesDataFragment() {
        // Required empty public constructor
    }

    public PieChart getLeavePieChart() {
        return leavePieChart;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_leaves_data, container, false);

        recyclerViewLeaves = view.findViewById(R.id.recyclerViewLeaves);
        leavePieChart = view.findViewById(R.id.leavePieChart);
        emptyStateText = view.findViewById(R.id.emptyStateText);
        loadingProgress = view.findViewById(R.id.loadingProgress);

        leaveAdapter = new LeaveAdapter(leaveList);
        recyclerViewLeaves.setAdapter(leaveAdapter);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        leavesViewModel = new ViewModelProvider(this).get(LeavesViewModel.class);
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String authToken = sharedPreferences.getString("authToken", "");

        leavesViewModel.setUserId(authToken);
        leavesViewModel.fetchLeavesData();

        leavesViewModel.leavesData.observe(getViewLifecycleOwner(), leavesData -> {
            loadingProgress.setVisibility(View.GONE);
            if (leavesData != null && leavesData.getData() != null) {
                List<LeavesItem> leaves = leavesData.getData().getLeaves();
                if (leaves.isEmpty()) {
                    emptyStateText.setVisibility(View.VISIBLE);
                    recyclerViewLeaves.setVisibility(View.GONE);
                    leavePieChart.setVisibility(View.GONE);
                } else {
                    emptyStateText.setVisibility(View.GONE);
                    recyclerViewLeaves.setVisibility(View.VISIBLE);
                    leavePieChart.setVisibility(View.VISIBLE);

                    leaveList.clear();
                    leaveList.addAll(leaves);
                    leaveAdapter.notifyDataSetChanged();
                    // Update PieChart data
                    updatePieChartData(leaves);
                }
            } else {

                emptyStateText.setVisibility(View.VISIBLE);
                emptyStateText.setText("Error loading data");
                recyclerViewLeaves.setVisibility(View.GONE);
                leavePieChart.setVisibility(View.GONE);
            }
        });

        loadingProgress.setVisibility(View.VISIBLE);
    }

    public void updatePieChartData(List<LeavesItem> leaves) {
        Map<String, Float> leaveData = new HashMap<>();
        float totalBalanceLeaves = 0;
        float totalLeaves = 0;

        for (LeavesItem leave : leaves) {
            String leaveType = leave.getLeavetype();
            if (leaveType != null) {
                float creditedLeaves = leave.getCreditLeave();
                float usedLeaves = leave.getUsedLeave();
                float debitedLeaves = leave.getDebitLeave();

                // Calculate balance leaves
                float balanceLeaves = creditedLeaves - usedLeaves - debitedLeaves;

                leaveData.put(leaveType, balanceLeaves);
                totalBalanceLeaves += balanceLeaves;
                totalLeaves += creditedLeaves;
            }
        }

        List<PieEntry> entries = new ArrayList<>();

        // Create PieEntry for each leave type with balance leaves value
        for (Map.Entry<String, Float> entry : leaveData.entrySet()) {
            if (entry.getValue() > 0) {
                entries.add(new PieEntry(entry.getValue(), entry.getKey()));
            }
        }
        if (entries.isEmpty()) {
            leavePieChart.setVisibility(View.VISIBLE);
            emptyStateText.setVisibility(View.VISIBLE);
            emptyStateText.setText("No balance leaves data available");
            return;
        }

        // Create a color map for leave types
        Map<String, Integer> leaveTypeColors = new HashMap<>();
        leaveTypeColors.put("Casual Leave", Color.BLUE);
        leaveTypeColors.put("Sick Leave", Color.RED);
        leaveTypeColors.put("Privilege Leave", Color.rgb(255, 165, 0));

        // Create a list of colors for the pie chart slices
        List<Integer> colors = new ArrayList<>();
        for (PieEntry entry : entries) {
            String leaveType = entry.getLabel();
            int color = leaveTypeColors.getOrDefault(leaveType, Color.GRAY);
            colors.add(color);
        }

        PieDataSet dataSet = new PieDataSet(entries, "Leave Types");
        dataSet.setColors(colors);
        dataSet.setValueFormatter(new PercentFormatter(leavePieChart));
        dataSet.setValueTextSize(16f);
        dataSet.setValueTextColor(Color.WHITE);

        PieData data = new PieData(dataSet);
        leavePieChart.setData(data);
        leavePieChart.getDescription().setEnabled(false);
        leavePieChart.setDrawHoleEnabled(true);
        leavePieChart.setHoleRadius(35f);
        leavePieChart.setTransparentCircleRadius(45f);
        leavePieChart.animateXY(1000, 1000);
        leavePieChart.setDrawEntryLabels(false);

//        leavePieChart.setCenterText("Balance Leaves: " + totalBalanceLeaves);
//        leavePieChart.setCenterTextSize(14f);

        leavePieChart.invalidate();
    }
}
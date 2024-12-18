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
import com.github.mikephil.charting.utils.ColorTemplate;

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
    private PieChart pieChart;
    private TextView emptyStateText;
    private ProgressBar loadingProgress;

    public LeavesDataFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_leaves_data, container, false);

        recyclerViewLeaves = view.findViewById(R.id.recyclerViewLeaves);
        pieChart = view.findViewById(R.id.pieChart);
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
                    pieChart.setVisibility(View.GONE);
                } else {
                    emptyStateText.setVisibility(View.GONE);
                    recyclerViewLeaves.setVisibility(View.VISIBLE);
                    pieChart.setVisibility(View.VISIBLE);

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
                pieChart.setVisibility(View.GONE);
            }
        });

        loadingProgress.setVisibility(View.VISIBLE);
    }

    private void updatePieChartData(List<LeavesItem> leaves) {
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
            pieChart.setVisibility(View.GONE);
            emptyStateText.setVisibility(View.VISIBLE);
            emptyStateText.setText("No balance leaves data available");
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, "Leave Types");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueFormatter(new PercentFormatter(pieChart));
        dataSet.setValueTextSize(16f);
        dataSet.setValueTextColor(Color.WHITE);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(35f);
        pieChart.setTransparentCircleRadius(45f);
        pieChart.animateXY(1000, 1000);
        pieChart.setDrawEntryLabels(false);

//        pieChart.setCenterText("Balance Leaves: " + totalBalanceLeaves);
//        pieChart.setCenterTextSize(14f);

        pieChart.invalidate();
    }
}
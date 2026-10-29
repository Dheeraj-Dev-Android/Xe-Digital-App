package app.xedigital.ai.ui.leaves;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import app.xedigital.ai.R;
import app.xedigital.ai.adapter.LeaveAdapter;
import app.xedigital.ai.model.leaves.LeavesItem;
import app.xedigital.ai.utills.CustomDialogHelper;
import app.xedigital.ai.utills.SecurePrefManager;

public class LeavesDataFragment extends Fragment {

    private final List<LeavesItem> leaveList = new ArrayList<>();
    private LeavesViewModel leavesViewModel;
    private LeaveAdapter leaveAdapter;
    private RecyclerView recyclerViewLeaves;
    private LinearLayout emptyStateContainer;
    private TextView emptyStateText;
    private CircularProgressIndicator loadingProgress;
    private PieChart leavePieChart;
    private View chartCard;

    public LeavesDataFragment() {
        // Required empty public constructor
    }

    public PieChart getLeavePieChart() {
        return leavePieChart;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_leaves_data, container, false);

        chartCard = view.findViewById(R.id.chartCard);
        recyclerViewLeaves = view.findViewById(R.id.recyclerViewLeaves);
        leavePieChart = view.findViewById(R.id.leavePieChart);
        emptyStateContainer = view.findViewById(R.id.emptyStateContainer);
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
        SecurePrefManager prefManager = SecurePrefManager.getInstance(requireContext());
        String authToken = prefManager.getString("authToken", "");

        leavesViewModel.setUserId(authToken);

        loadingProgress.setVisibility(View.VISIBLE);
        leavesViewModel.fetchLeavesData();

        leavesViewModel.leavesData.observe(getViewLifecycleOwner(), leavesData -> {
            if (!isAdded() || getView() == null) return;
            loadingProgress.setVisibility(View.GONE);

            if (leavesData != null && leavesData.getData() != null) {
                List<LeavesItem> leaves = leavesData.getData().getLeaves();
                if (leaves == null || leaves.isEmpty()) {
                    showEmptyState("No leaves data available");
                } else {
                    hideEmptyState();
                    leaveList.clear();
                    leaveList.addAll(leaves);
                    leaveAdapter.notifyDataSetChanged();
                    updatePieChartData(leaves);
                }
            } else {
                showEmptyState("No leaves data available");
                if (isAdded()) {
                    CustomDialogHelper.showErrorDialog(requireContext(), "Error", "Failed to load leave data. Please try again.");
                }
            }
        });
    }

    private void showEmptyState(String message) {
        emptyStateContainer.setVisibility(View.VISIBLE);
        emptyStateText.setText(message);
        recyclerViewLeaves.setVisibility(View.GONE);
        chartCard.setVisibility(View.GONE);
    }

    private void hideEmptyState() {
        emptyStateContainer.setVisibility(View.GONE);
        recyclerViewLeaves.setVisibility(View.VISIBLE);
        chartCard.setVisibility(View.VISIBLE);
    }

    public void updatePieChartData(List<LeavesItem> leaves) {
        if (leavePieChart == null || leaves == null || leaves.isEmpty()) return;

        Map<String, Float> leaveData = new HashMap<>();

        for (LeavesItem leave : leaves) {
            if (leave == null) continue;
            String leaveType = leave.getLeavetype();
            if (leaveType == null) continue;

            float balanceLeaves = leave.getCreditLeave() - leave.getUsedLeave() - leave.getDebitLeave();
            leaveData.put(leaveType, balanceLeaves);
        }

        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Float> entry : leaveData.entrySet()) {
            if (entry.getValue() > 0) {
                entries.add(new PieEntry(entry.getValue(), entry.getKey()));
            }
        }

        if (entries.isEmpty()) {
//            showEmptyState("No balance leaves data available");
            leavePieChart.setVisibility(View.GONE);
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, "Leave Types");
        dataSet.setValueFormatter(new PercentFormatter(leavePieChart));

        // ── Predefined color map (case-insensitive) ──
        Map<String, Integer> leaveTypeColors = new HashMap<>();
        leaveTypeColors.put("casual leave", Color.rgb(51, 206, 255));
        leaveTypeColors.put("sick leave", Color.rgb(125, 206, 160));
        leaveTypeColors.put("privilege leave", Color.rgb(255, 165, 0));
        leaveTypeColors.put("restricted holidays", Color.rgb(199, 0, 57));
        leaveTypeColors.put("earned leave", Color.rgb(156, 39, 176));
        leaveTypeColors.put("compensatory off", Color.rgb(0, 150, 136));
        leaveTypeColors.put("maternity leave", Color.rgb(233, 30, 99));
        leaveTypeColors.put("paternity leave", Color.rgb(63, 81, 181));
        leaveTypeColors.put("cl", Color.rgb(51, 206, 255));
        leaveTypeColors.put("sl", Color.rgb(125, 206, 160));
        leaveTypeColors.put("pl", Color.rgb(255, 165, 0));
        leaveTypeColors.put("rh", Color.rgb(199, 0, 57));

        // Deterministic random seeded by leave type name → same color every time for same label
        List<Integer> colors = new ArrayList<>();
        for (PieEntry entry : entries) {
            String label = entry.getLabel() != null ? entry.getLabel().toLowerCase().trim() : "";
            Integer predefined = leaveTypeColors.get(label);
            if (predefined != null) {
                colors.add(predefined);
            } else {
                colors.add(generateRandomColor(label));
            }
        }

        dataSet.setColors(colors);
        dataSet.setValueTextSize(16f);
        dataSet.setValueTextColor(Color.WHITE);

        Legend legend = leavePieChart.getLegend();
        legend.setEnabled(true);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setWordWrapEnabled(true);
        legend.setTextSize(8f);
        legend.setTextColor(Color.WHITE);

        PieData data = new PieData(dataSet);
        leavePieChart.setData(data);
        leavePieChart.getDescription().setEnabled(false);
        leavePieChart.setDrawHoleEnabled(true);
        leavePieChart.setHoleRadius(35f);
        leavePieChart.setTransparentCircleRadius(45f);
        leavePieChart.animateXY(1000, 1000);
        leavePieChart.setDrawEntryLabels(false);
        leavePieChart.invalidate();
    }

    /**
     * Generates a visually pleasant random color seeded by the leave type name.
     * Same name → same color every time (deterministic), so the pie chart stays
     * consistent across refreshes. Uses HSV with high saturation + brightness
     * to avoid washed-out or dark colors.
     */
    private int generateRandomColor(String seed) {
        Random random = new Random(seed.hashCode());
        float hue = random.nextFloat() * 360f;
        float saturation = 0.65f + random.nextFloat() * 0.25f;   // 0.65 – 0.90
        float brightness = 0.75f + random.nextFloat() * 0.20f;   // 0.75 – 0.95
        return Color.HSVToColor(new float[]{hue, saturation, brightness});
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        chartCard = null;
        recyclerViewLeaves = null;
        leavePieChart = null;
        emptyStateContainer = null;
        emptyStateText = null;
        loadingProgress = null;
        leaveAdapter = null;
    }
}
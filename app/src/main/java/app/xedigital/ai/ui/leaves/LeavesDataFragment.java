package app.xedigital.ai.ui.leaves;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import app.xedigital.ai.R;
import app.xedigital.ai.adapter.LeaveAdapter;
import app.xedigital.ai.model.leaves.LeavesItem;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LeavesDataFragment extends Fragment {

    private final List<LeavesItem> leaveList = new ArrayList<>();

    public LeavesDataFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_leaves_data, container, false);

        RecyclerView recyclerViewLeaves = view.findViewById(R.id.recyclerViewLeaves);
        LeaveAdapter leaveAdapter = new LeaveAdapter(leaveList);
        recyclerViewLeaves.setAdapter(leaveAdapter);

        LeavesViewModel leavesViewModel = new ViewModelProvider(this).get(LeavesViewModel.class);
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String authToken = sharedPreferences.getString("authToken", "");

        leavesViewModel.setUserId(authToken);
        leavesViewModel.fetchLeavesData();

        leavesViewModel.leavesData.observe(getViewLifecycleOwner(), leavesData -> {
            if (leavesData != null && leavesData.getData() != null) {
                List<LeavesItem> leaves = leavesData.getData().getLeaves();
                Log.d("LeavesDataFragment", "Leaves Data: " + leaves);
                leaveList.clear();
                leaveList.addAll(leaves);
                leaveAdapter.notifyDataSetChanged();


                PieChart pieChart = view.findViewById(R.id.pieChart);
                Set<String> leaveTypes = new HashSet<>();
                Map<String, Integer> leaveCounts = new HashMap<>();

                for (LeavesItem leave : leaves) {
                    if (leave.getLeavetype() != null) {
                        leaveTypes.add(leave.getLeavetype());
                        leaveCounts.put(leave.getLeavetype(), leaveCounts.getOrDefault(leave.getLeavetype(), 0)+1);
                    }
                }

                List<PieEntry> entries = new ArrayList<>();
                for (String leaveType : leaveTypes) {
                    entries.add(new PieEntry(leaveCounts.get(leaveType), leaveType));
                }

                PieDataSet dataSet = new PieDataSet(entries, "Leave Types");
                dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
                PieData pieData = new PieData(dataSet);
                pieChart.setData(pieData);
                pieChart.getDescription().setEnabled(false);
                pieChart.setCenterText("Leave Distribution");
                pieChart.animateY(1000);
                pieChart.invalidate();


                CombinedChart combinedChart = view.findViewById(R.id.combinedChart);

                List<BarEntry> barEntries = new ArrayList<>();
                List<Entry> lineEntries = new ArrayList<>();

                for (int i = 0; i < leaves.size(); i++) {
                    LeavesItem leave = leaves.get(i);
                    barEntries.add(new BarEntry(i, leave.getUsedLeave()));
                    lineEntries.add(new Entry(i, leave.getCreditLeave()));
                }

                BarDataSet barDataSet = new BarDataSet(barEntries, "Used Leaves");
                barDataSet.setColors(ColorTemplate.MATERIAL_COLORS);

                LineDataSet lineDataSet = new LineDataSet(lineEntries, "Credit Leaves");
                lineDataSet.setColor(Color.RED);
                lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);

                BarData barData = new BarData(barDataSet);
                LineData lineData = new LineData(lineDataSet);

                CombinedData data = new CombinedData();
                data.setData(barData);
                data.setData(lineData);

                combinedChart.setData(data);
                combinedChart.getDescription().setEnabled(false);

                combinedChart.animateY(1000);
                combinedChart.invalidate();
            }
        });

        return view;
    }
}
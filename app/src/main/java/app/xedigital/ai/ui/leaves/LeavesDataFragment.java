package app.xedigital.ai.ui.leaves;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import app.xedigital.ai.R;
import app.xedigital.ai.adapter.LeaveAdapter;
import app.xedigital.ai.model.leaves.LeavesItem;

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

                Set<String> leaveTypes = new HashSet<>();
                Map<String, Integer> leaveCounts = new HashMap<>();

                for (LeavesItem leave : leaves) {
                    if (leave.getLeavetype() != null) {
                        leaveTypes.add(leave.getLeavetype());
                        leaveCounts.put(leave.getLeavetype(), leaveCounts.getOrDefault(leave.getLeavetype(), 0) + 1);
                    }
                }
            }
        });

        return view;
    }
}
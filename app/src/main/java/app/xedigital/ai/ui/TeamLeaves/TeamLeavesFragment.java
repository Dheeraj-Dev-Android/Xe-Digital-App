package app.xedigital.ai.ui.TeamLeaves;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import app.xedigital.ai.R;
import app.xedigital.ai.adapter.TeamLeavesAdapter;

public class TeamLeavesFragment extends Fragment {

    private TeamLeavesViewModel mViewModel;

    private RecyclerView rvEmployees;
    private ProgressBar progressBar;
    private LinearLayout layoutEmpty;
    private TeamLeavesAdapter adapter;

    public static TeamLeavesFragment newInstance() {
        return new TeamLeavesFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_team_leaves, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvEmployees = view.findViewById(R.id.rv_employees);
        progressBar = view.findViewById(R.id.progress_bar);
        layoutEmpty = view.findViewById(R.id.layout_empty);

        // Setup RecyclerView once with an empty list
        adapter = new TeamLeavesAdapter(requireContext(), new ArrayList<>());
        rvEmployees.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvEmployees.setAdapter(adapter);

        mViewModel = new ViewModelProvider(this).get(TeamLeavesViewModel.class);

        SharedPreferences prefs = requireContext()
                .getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String authToken = prefs.getString("authToken", null);
        String employeeId = prefs.getString("userId", null);

        showLoading(true);
        mViewModel.fetchTeamLeaves("jwt " + authToken, employeeId);

        // Observe successful data updates
        mViewModel.getEmployeesLiveData().observe(getViewLifecycleOwner(), employees -> {
            showLoading(false);

            if (employees == null || employees.isEmpty()) {
                layoutEmpty.setVisibility(View.VISIBLE);
                rvEmployees.setVisibility(View.GONE);
            } else {
                layoutEmpty.setVisibility(View.GONE);
                rvEmployees.setVisibility(View.VISIBLE);

                // Use a single adapter instance and update its list data internally
                adapter.updateList(employees);
            }
        });

        // Observe errors to prevent infinite progress spinning
        mViewModel.getErrorLiveData().observe(getViewLifecycleOwner(), errorMessage -> {
            showLoading(false);
            layoutEmpty.setVisibility(View.VISIBLE);
            rvEmployees.setVisibility(View.GONE);

            // Optional alert to notify the user
            Toast.makeText(requireContext(), "Failed to load team leaves", Toast.LENGTH_SHORT).show();
        });
    }

    private void showLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        // Toggle empty state visibility safely alongside the recycler layout
        if (loading) {
            rvEmployees.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.GONE);
        }
    }
}
package app.xedigital.ai.ui.TeamLeaves;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import app.xedigital.ai.R;
import app.xedigital.ai.adapter.TeamLeavesAdapter;
import app.xedigital.ai.model.TeamLeave.EmployeesItem;
import app.xedigital.ai.utills.CustomDialogHelper;
import app.xedigital.ai.utills.SecurePrefManager;

public class TeamLeavesFragment extends Fragment {

    private TeamLeavesViewModel mViewModel;

    private RecyclerView rvEmployees;
    private CircularProgressIndicator progressBar;
    private LinearLayout layoutEmpty;
    private EditText etSearchName;
    private TeamLeavesAdapter adapter;

    private List<EmployeesItem> fullEmployeeList = new ArrayList<>();

    public static TeamLeavesFragment newInstance() {
        return new TeamLeavesFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_team_leaves, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvEmployees = view.findViewById(R.id.rv_employees);
        progressBar = view.findViewById(R.id.progress_bar);
        layoutEmpty = view.findViewById(R.id.layout_empty);
        etSearchName = view.findViewById(R.id.etSearchName);

        adapter = new TeamLeavesAdapter(requireContext(), new ArrayList<>());
        rvEmployees.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvEmployees.setAdapter(adapter);

        mViewModel = new ViewModelProvider(this).get(TeamLeavesViewModel.class);

        SecurePrefManager prefManager = SecurePrefManager.getInstance(requireContext());
        String authToken = prefManager.getString("authToken", null);
        String employeeId = prefManager.getString("userId", null);

        showLoading(true);
        mViewModel.fetchTeamLeaves("jwt " + authToken, employeeId);

        etSearchName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                filterList(s.toString());
            }
        });

        mViewModel.getEmployeesLiveData().observe(getViewLifecycleOwner(), employees -> {
            if (!isAdded() || getView() == null) return;
            showLoading(false);

            if (employees == null || employees.isEmpty()) {
                fullEmployeeList = new ArrayList<>();
                layoutEmpty.setVisibility(View.VISIBLE);
                rvEmployees.setVisibility(View.GONE);
                etSearchName.setVisibility(View.GONE);
            } else {
                fullEmployeeList = new ArrayList<>(employees);
                layoutEmpty.setVisibility(View.GONE);
                rvEmployees.setVisibility(View.VISIBLE);
                etSearchName.setVisibility(View.VISIBLE);
                filterList(etSearchName.getText().toString());
            }
        });

        mViewModel.getErrorLiveData().observe(getViewLifecycleOwner(), errorMessage -> {
            if (!isAdded() || getView() == null) return;
            showLoading(false);
            fullEmployeeList = new ArrayList<>();
            layoutEmpty.setVisibility(View.VISIBLE);
            rvEmployees.setVisibility(View.GONE);
            etSearchName.setVisibility(View.GONE);

            CustomDialogHelper.showErrorDialog(requireContext(), "Error", "Failed to load team leaves. Please try again.");
        });
    }

    private void filterList(String query) {
        if (query == null || query.trim().isEmpty()) {
            adapter.updateList(fullEmployeeList);
            return;
        }

        String lowerQuery = query.trim().toLowerCase(Locale.ROOT);
        List<EmployeesItem> filtered = new ArrayList<>();

        for (EmployeesItem employee : fullEmployeeList) {
            String name = employee.getEmployeeName();
            if (name != null && name.toLowerCase(Locale.ROOT).contains(lowerQuery)) {
                filtered.add(employee);
            }
        }
        adapter.updateList(filtered);
    }

    private void showLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        if (loading) {
            rvEmployees.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.GONE);
            etSearchName.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        rvEmployees = null;
        progressBar = null;
        layoutEmpty = null;
        etSearchName = null;
        adapter = null;
    }
}
package app.xedigital.ai.ui.TeamMember;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import app.xedigital.ai.R;
import app.xedigital.ai.model.TeamMember.AllEmployeesItem;
import app.xedigital.ai.model.TeamMember.EmployeesItem;

public class TeamMemberFragment extends Fragment {

    private TeamMemberViewModel mViewModel;
    private TextView meAvatar, meName, meRole;
    private TextView statDirect, statCross, statTotal;
    private TextView labelDirect, labelCross;
    private RecyclerView recyclerDirect, recyclerCross;
    private TeamTreeAdapter directAdapter, crossAdapter;
    private CircularProgressIndicator loadingProgress;
    private TextView emptyStateText;
    private String myUserId = "";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_team_member, container, false);

        // UI Binding
        meAvatar = root.findViewById(R.id.me_avatar);
        meName = root.findViewById(R.id.me_name);
        meRole = root.findViewById(R.id.me_role);
        statDirect = root.findViewById(R.id.stat_direct_count);
        statCross = root.findViewById(R.id.stat_cross_count);
        statTotal = root.findViewById(R.id.stat_total_count);
        labelDirect = root.findViewById(R.id.label_direct);
        labelCross = root.findViewById(R.id.label_cross);
        loadingProgress = root.findViewById(R.id.loadingProgress);
        emptyStateText = root.findViewById(R.id.emptyStateText);
        recyclerDirect = root.findViewById(R.id.recyclerViewDirect);
        recyclerCross = root.findViewById(R.id.recyclerViewCross);

        // Adapter Setup
        directAdapter = new TeamTreeAdapter();
        crossAdapter = new TeamTreeAdapter();

        // CLICK LISTENERS
        directAdapter.setOnMemberClickListener(this::showMemberDetailPopup);
        crossAdapter.setOnMemberClickListener(this::showMemberDetailPopup);

        recyclerDirect.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerCross.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerDirect.setAdapter(directAdapter);
        recyclerCross.setAdapter(crossAdapter);

        return root;
    }

    private void showMemberDetailPopup(TeamTreeAdapter.TreeNode node) {
        View v = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_member_info, null);

        // Header info
        ((TextView) v.findViewById(R.id.popup_avatar_initials)).setText(getInitials(node.name));
        ((TextView) v.findViewById(R.id.popup_employee_name)).setText(node.name);
        ((TextView) v.findViewById(R.id.popup_employee_role)).setText(node.role);

        // Grid fields - Use IDs from your include or direct IDs
        ((TextView) v.findViewById(R.id.tv_emp_id)).setText(node.id);
        ((TextView) v.findViewById(R.id.tv_email)).setText(node.email);
        ((TextView) v.findViewById(R.id.tv_contact)).setText(node.contact);
        ((TextView) v.findViewById(R.id.tv_dept)).setText(node.department); // Now showing Dept
        ((TextView) v.findViewById(R.id.tv_shift)).setText(node.shiftTiming); // New Shift view

        // Manager Footer
        ((TextView) v.findViewById(R.id.tv_reporting_manager)).setText(node.reportingManager);
        ((TextView) v.findViewById(R.id.tv_cross_manager)).setText(node.crossManager);

        new MaterialAlertDialogBuilder(requireContext())
                .setView(v)
                .setBackground(requireContext().getDrawable(R.drawable.bg_dialog_white_rounded))
                .show();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(TeamMemberViewModel.class);
        SharedPreferences prefs = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String authToken = prefs.getString("authToken", "");
        myUserId = prefs.getString("userId", "");

        setupObservers();
        if (!authToken.isEmpty()) mViewModel.fetchTeamMembers(authToken, myUserId);
    }

    private void setupObservers() {
        mViewModel.getIsLoading().observe(getViewLifecycleOwner(), loading ->
                loadingProgress.setVisibility(loading ? View.VISIBLE : View.GONE));

        mViewModel.getTeamMemberData().observe(getViewLifecycleOwner(), response -> {
            if (response == null || response.getData() == null) return;

            // Handle "Me" Card
            List<EmployeesItem> employees = response.getData().getEmployees();
            if (employees != null) {
                for (EmployeesItem emp : employees) {
                    if (myUserId.equals(emp.getId())) {
                        populateMeCard(emp);
                        break;
                    }
                }
            }

            // Filtering logic (Direct vs Cross)
            Set<String> addedIds = new HashSet<>();
            List<EmployeesItem> dEmp = new ArrayList<>(), cEmp = new ArrayList<>();
            List<AllEmployeesItem> dAll = new ArrayList<>(), cAll = new ArrayList<>();

            if (employees != null) {
                for (EmployeesItem emp : employees) {
                    if (emp == null || myUserId.equals(emp.getId())) continue;
                    if (emp.getReportingManager() != null && myUserId.equals(emp.getReportingManager().getId())) {
                        dEmp.add(emp);
                        addedIds.add(emp.getId());
                    } else if (emp.getCrossmanager() != null && myUserId.equals(emp.getCrossmanager().getId())) {
                        cEmp.add(emp);
                        addedIds.add(emp.getId());
                    }
                }
            }

            List<AllEmployeesItem> all = response.getData().getAllEmployees();
            if (all != null) {
                for (AllEmployeesItem emp : all) {
                    if (emp == null || myUserId.equals(emp.getId()) || addedIds.contains(emp.getId()))
                        continue;
                    if (emp.getReportingManager() != null && myUserId.equals(emp.getReportingManager().getId())) {
                        dAll.add(emp);
                    } else if (emp.getCrossmanager() != null && myUserId.equals(emp.getCrossmanager().getId())) {
                        cAll.add(emp);
                    }
                }
            }

            // Update Adapters
            directAdapter.setMergedData(dEmp, dAll, TeamTreeAdapter.RelationType.DIRECT);
            crossAdapter.setMergedData(cEmp, cAll, TeamTreeAdapter.RelationType.CROSS);

            // Update Stats
            int tD = dEmp.size() + dAll.size();
            int tC = cEmp.size() + cAll.size();
            statDirect.setText(String.valueOf(tD));
            statCross.setText(String.valueOf(tC));
            statTotal.setText(String.valueOf(tD + tC));

            labelDirect.setVisibility(tD > 0 ? View.VISIBLE : View.GONE);
            labelCross.setVisibility(tC > 0 ? View.VISIBLE : View.GONE);
            emptyStateText.setVisibility((tD + tC) == 0 ? View.VISIBLE : View.GONE);
        });
    }

    private void populateMeCard(EmployeesItem me) {
        String name = trim(me.getFirstname()) + " " + trim(me.getLastname());
        meName.setText(name);
        meRole.setText(trim(me.getDesignation()));
        meAvatar.setText(getInitials(name));
    }

    private String getInitials(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) return "?";
        String[] parts = fullName.trim().split(" ");
        return (parts.length >= 2) ? ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase() : ("" + parts[0].charAt(0)).toUpperCase();
    }

    private String trim(String s) {
        return s != null ? s.trim() : "";
    }
}
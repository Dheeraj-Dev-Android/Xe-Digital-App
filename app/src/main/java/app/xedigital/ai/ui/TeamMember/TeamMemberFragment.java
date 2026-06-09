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
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
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
import app.xedigital.ai.model.TeamMember.TeamMemberResponse;

public class TeamMemberFragment extends Fragment {

    private TeamMemberViewModel mViewModel;
    private TextView meAvatar, meName, meRole;
    private TextView statDirect, statCross, statTotal;
    private View labelDirect, labelCross;
    private RecyclerView recyclerDirect, recyclerCross;
    private TeamTreeAdapter directAdapter, crossAdapter;
    private CircularProgressIndicator loadingProgress;
    private TextView emptyStateText;
    private String myUserId = "";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_team_member, container, false);

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

        directAdapter = new TeamTreeAdapter();
        crossAdapter = new TeamTreeAdapter();
        directAdapter.setOnMemberClickListener(this::showMemberDetailPopup);
        crossAdapter.setOnMemberClickListener(this::showMemberDetailPopup);

        recyclerDirect.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerCross.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerDirect.setAdapter(directAdapter);
        recyclerCross.setAdapter(crossAdapter);

        return root;
    }

    private void showMemberDetailPopup(TeamTreeAdapter.TreeNode node) {
        Context context = getContext();
        if (context == null || !isAdded()) return;

        View v = LayoutInflater.from(context).inflate(R.layout.dialog_member_info, null);

        ((TextView) v.findViewById(R.id.popup_avatar_initials)).setText(getInitials(node.name));
        ((TextView) v.findViewById(R.id.popup_employee_name)).setText(node.name);
        ((TextView) v.findViewById(R.id.popup_employee_role)).setText(node.role);
        ((TextView) v.findViewById(R.id.tv_emp_id)).setText(node.empId);
        ((TextView) v.findViewById(R.id.tv_email)).setText(node.email);
        ((TextView) v.findViewById(R.id.tv_contact)).setText(node.contact);
        ((TextView) v.findViewById(R.id.tv_dept)).setText(node.department);
        ((TextView) v.findViewById(R.id.tv_shift)).setText(node.shiftTiming);
        ((TextView) v.findViewById(R.id.tv_reporting_manager)).setText(node.reportingManager);
        ((TextView) v.findViewById(R.id.tv_cross_manager)).setText(node.crossManager);

        TextView popupSubteamTitle = v.findViewById(R.id.popup_subteam_title);
        View subLoading = v.findViewById(R.id.popup_sub_loading);
        View pLabelDirect = v.findViewById(R.id.popup_label_direct);
        View pLabelCross = v.findViewById(R.id.popup_label_cross);
        RecyclerView pRecyclerDirect = v.findViewById(R.id.popup_recycler_direct);
        RecyclerView pRecyclerCross = v.findViewById(R.id.popup_recycler_cross);

        TeamTreeAdapter subDirectAdapter = new TeamTreeAdapter();
        TeamTreeAdapter subCrossAdapter = new TeamTreeAdapter();

        subDirectAdapter.setOnMemberClickListener(this::showMemberDetailPopup);
        subCrossAdapter.setOnMemberClickListener(this::showMemberDetailPopup);

        pRecyclerDirect.setLayoutManager(new LinearLayoutManager(context));
        pRecyclerCross.setLayoutManager(new LinearLayoutManager(context));
        pRecyclerDirect.setAdapter(subDirectAdapter);
        pRecyclerCross.setAdapter(subCrossAdapter);

        AlertDialog dialog = new MaterialAlertDialogBuilder(context).setView(v).setBackground(context.getDrawable(R.drawable.bg_dialog_white_rounded)).show();

        SharedPreferences prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String authToken = prefs.getString("authToken", "");

        mViewModel.clearSubTeamData();
        Observer<Boolean> loadingObserver = loading -> {
            if (subLoading != null) {
                subLoading.setVisibility(Boolean.TRUE.equals(loading) ? View.VISIBLE : View.GONE);
            }
        };
        Observer<TeamMemberResponse> subTeamDataObserver = response -> {
            if (!isAdded() || getContext() == null) return;
            if (response == null || response.getData() == null) return;

            String targetMemberId = node.id;
            List<EmployeesItem> employees = response.getData().getEmployees();
            Set<String> addedIds = new HashSet<>();
            List<EmployeesItem> dEmp = new ArrayList<>();
            List<EmployeesItem> cEmp = new ArrayList<>();
            List<AllEmployeesItem> dAll = new ArrayList<>();
            List<AllEmployeesItem> cAll = new ArrayList<>();

            if (employees != null) {
                for (EmployeesItem emp : employees) {
                    if (emp == null || targetMemberId.equals(emp.getId())) continue;

                    if (emp.getReportingManager() != null && targetMemberId.equals(emp.getReportingManager().getId())) {
                        dEmp.add(emp);
                        addedIds.add(emp.getId());
                    } else if (emp.getCrossmanager() != null && targetMemberId.equals(emp.getCrossmanager().getId())) {
                        cEmp.add(emp);
                        addedIds.add(emp.getId());
                    }
                }
            }

            List<AllEmployeesItem> all = response.getData().getAllEmployees();
            if (all != null) {
                for (AllEmployeesItem emp : all) {
                    if (emp == null || targetMemberId.equals(emp.getId()) || addedIds.contains(emp.getId()))
                        continue;

                    if (emp.getReportingManager() != null && targetMemberId.equals(emp.getReportingManager().getId())) {
                        dAll.add(emp);
                    } else if (emp.getCrossmanager() != null && targetMemberId.equals(emp.getCrossmanager().getId())) {
                        cAll.add(emp);
                    }
                }
            }

            subDirectAdapter.setMergedData(dEmp, dAll, TeamTreeAdapter.RelationType.DIRECT);
            subCrossAdapter.setMergedData(cEmp, cAll, TeamTreeAdapter.RelationType.CROSS);

            int totalSubReports = dEmp.size() + dAll.size() + cEmp.size() + cAll.size();

            if (totalSubReports > 0) {
                if (popupSubteamTitle != null) popupSubteamTitle.setVisibility(View.VISIBLE);

                int directCount = dEmp.size() + dAll.size();
                if (pLabelDirect != null)
                    pLabelDirect.setVisibility(directCount > 0 ? View.VISIBLE : View.GONE);
                if (pRecyclerDirect != null)
                    pRecyclerDirect.setVisibility(directCount > 0 ? View.VISIBLE : View.GONE);

                int crossCount = cEmp.size() + cAll.size();
                if (pLabelCross != null)
                    pLabelCross.setVisibility(crossCount > 0 ? View.VISIBLE : View.GONE);
                if (pRecyclerCross != null)
                    pRecyclerCross.setVisibility(crossCount > 0 ? View.VISIBLE : View.GONE);
            }
        };

        mViewModel.getIsSubTeamLoading().observe(getViewLifecycleOwner(), loadingObserver);
        mViewModel.getSubTeamData().observe(getViewLifecycleOwner(), subTeamDataObserver);

        dialog.setOnDismissListener(dialogInterface -> {
            mViewModel.getIsSubTeamLoading().removeObserver(loadingObserver);
            mViewModel.getSubTeamData().removeObserver(subTeamDataObserver);
        });

        if (!authToken.isEmpty()) {
            mViewModel.fetchSubTeamMembers(authToken, node.id);
        }
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
        mViewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
            if (loadingProgress != null) {
                loadingProgress.setVisibility(Boolean.TRUE.equals(loading) ? View.VISIBLE : View.GONE);
            }
        });

        mViewModel.getTeamMemberData().observe(getViewLifecycleOwner(), response -> {
            if (response == null || response.getData() == null) return;

            List<EmployeesItem> employees = response.getData().getEmployees();
            if (employees != null) {
                for (EmployeesItem emp : employees) {
                    if (emp != null && myUserId.equals(emp.getId())) {
                        populateMeCard(emp);
                        break;
                    }
                }
            }

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

            directAdapter.setMergedData(dEmp, dAll, TeamTreeAdapter.RelationType.DIRECT);
            crossAdapter.setMergedData(cEmp, cAll, TeamTreeAdapter.RelationType.CROSS);

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
        String[] parts = fullName.trim().split("\\s+");
        return (parts.length >= 2) ? ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase() : ("" + parts[0].charAt(0)).toUpperCase();
    }

    private String trim(String s) {
        return s != null ? s.trim() : "";
    }
}
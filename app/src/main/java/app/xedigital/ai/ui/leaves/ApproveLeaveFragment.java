package app.xedigital.ai.ui.leaves;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

import app.xedigital.ai.R;
import app.xedigital.ai.adapter.LeaveApprovalAdapter;
import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.api.APIInterface;
import app.xedigital.ai.model.leaveApprovalPending.AppliedLeavesApproveItem;
import app.xedigital.ai.model.leaveApprovalPending.LeavePendingApprovalResponse;
import app.xedigital.ai.model.regularizeList.AttendanceRegularizeAppliedItem;
import app.xedigital.ai.utills.FilterBottomSheetDialogFragment;
import app.xedigital.ai.utills.SecurePrefManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ApproveLeaveFragment extends Fragment implements FilterLeaveApprovalListener {

    private final String currentStatusFilter = "All";
    private final List<AppliedLeavesApproveItem> originalLeaveList = new ArrayList<>();
    private final List<AppliedLeavesApproveItem> filteredDataByStatus = new ArrayList<>();
    private final List<AppliedLeavesApproveItem> filteredDataByDate = new ArrayList<>();
    private String authTokenHeader;
    private String userId;
    private LeaveApprovalAdapter approvalAdapter;
    private PendingLeaveApproveFragment pendingLeaveApproveFragment;
    private LeavePendingApprovalResponse leavePendingApprovalResponse;
    private APIInterface apiInterface;
    private ProgressBar loadingProgress;
    private LinearLayout emptyStateContainer;
    private View view;
    private String currentStartDate = "";
    private String currentEndDate = "";
    private ChipGroup chipGroup;
    private boolean isDataLoaded = false;
    private ApproveLeaveViewModal approveLeaveViewModal;

    public static ApproveLeaveFragment newInstance() {
        return new ApproveLeaveFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        approveLeaveViewModal = new ViewModelProvider(this).get(ApproveLeaveViewModal.class);
    }

    public void onFilterApplied(String startDate, String endDate) {
        currentStartDate = startDate;
        currentEndDate = endDate;
        filterLeavesByDate(startDate, endDate);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_approve_leave, container, false);
        setHasOptionsMenu(true);

        RecyclerView approvalRecyclerView = view.findViewById(R.id.leaveApprovalRecyclerView);
        loadingProgress = view.findViewById(R.id.loadingProgress);
        emptyStateContainer = view.findViewById(R.id.emptyStateContainer);
        chipGroup = view.findViewById(R.id.statusChipGroup);
        approvalRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        approvalAdapter = new LeaveApprovalAdapter(new ArrayList<>(), authTokenHeader, userId, ApproveLeaveFragment.this, getContext());
        approvalRecyclerView.setAdapter(approvalAdapter);

        apiInterface = APIClient.getInstance().getPendingApprovalLeaves();
        SecurePrefManager prefManager = SecurePrefManager.getInstance(requireContext());
        String authToken = prefManager.getString("authToken", "");
        userId = prefManager.getString("userId", "");
        authTokenHeader = "jwt " + authToken;

        chipGroup.setSingleSelection(true);

        if (!isDataLoaded) {
            loadingProgress.setVisibility(View.VISIBLE);
            loadLeaves();
            isDataLoaded = true;
        } else {
            if (leavePendingApprovalResponse != null) {
                filterLeavesByDate(currentStartDate, currentEndDate);
                filterLeaves(currentStatusFilter);
            }
        }

        if (currentStatusFilter.equals("Approved")) {
            chipGroup.check(R.id.chipApproved);
        } else if (currentStatusFilter.equals("Unapproved")) {
            chipGroup.check(R.id.chipUnapproved);
        } else if (currentStatusFilter.equals("Rejected")) {
            chipGroup.check(R.id.chipRejected);
        } else if (currentStatusFilter.equals("Cancelled")) {
            chipGroup.check(R.id.chipCancelled);
        } else {
            chipGroup.check(R.id.chipAll);
        }

        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.contains(R.id.chipAll)) {
                filterLeaves("All");
            } else if (checkedIds.contains(R.id.chipApproved)) {
                filterLeaves("Approved");
            } else if (checkedIds.contains(R.id.chipUnapproved)) {
                filterLeaves("Unapproved");
            } else if (checkedIds.contains(R.id.chipRejected)) {
                filterLeaves("Rejected");
            } else if (checkedIds.contains(R.id.chipCancelled)) {
                filterLeaves("Cancelled");
            } else {
                filterLeaves("All");
            }
        });

        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            View child = chipGroup.getChildAt(i);
            if (child instanceof Chip) {
                child.setOnClickListener(this::onChipClicked);
            }
        }
        return view;
    }

    public void onChipClicked(View view) {
        ChipGroup chipGroup = requireView().findViewById(R.id.statusChipGroup);
        Chip clickedChip = (Chip) view;
        chipGroup.check(clickedChip.getId());
    }

    private void filterLeaves(String status) {
        List<AppliedLeavesApproveItem> masterList = approveLeaveViewModal.leaveList.getValue();
        List<AppliedLeavesApproveItem> listToFilter;

        if (!filteredDataByDate.isEmpty()) {
            listToFilter = filteredDataByDate;
        } else if (masterList != null && !masterList.isEmpty()) {
            listToFilter = masterList;
        } else {
            listToFilter = new ArrayList<>();
        }

        List<AppliedLeavesApproveItem> filteredList = new ArrayList<>();
        if (status.equals("All")) {
            filteredList.addAll(listToFilter);
        } else {
            for (AppliedLeavesApproveItem item : listToFilter) {
                if (item.getStatus() != null && item.getStatus().equalsIgnoreCase(status)) {
                    filteredList.add(item);
                }
            }
        }

        filteredDataByStatus.clear();
        filteredDataByStatus.addAll(filteredList);
        if (approvalAdapter != null) {
            approvalAdapter.updateList(filteredList);
        }
    }

    private void getLeaveApproval() {
        Call<LeavePendingApprovalResponse> call = apiInterface.getPendingApprovalLeaves(authTokenHeader, userId);
        call.enqueue(new Callback<LeavePendingApprovalResponse>() {
            @Override
            public void onResponse(@NonNull Call<LeavePendingApprovalResponse> call, @NonNull Response<LeavePendingApprovalResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    loadingProgress.setVisibility(View.GONE);
                    leavePendingApprovalResponse = response.body();
                    List<AppliedLeavesApproveItem> items = leavePendingApprovalResponse.getData().getAppliedLeaves();

                    approveLeaveViewModal.leaveList.setValue(items);
                    if (items == null || items.isEmpty()) {
                        emptyStateContainer.setVisibility(View.VISIBLE);
                        new AlertDialog.Builder(getContext()).setTitle("Approve Leaves").setMessage("No Records Found").setPositiveButton("OK", null).show();
                    } else {
                        emptyStateContainer.setVisibility(View.GONE);
                        List<AppliedLeavesApproveItem> filteredItems = new ArrayList<>(items);
                        filterLeavesByDate(currentStartDate, currentEndDate, filteredItems);
                        chipGroup.check(R.id.chipAll);
                        filterLeaves(currentStatusFilter);
                    }
                } else {
                    Toast.makeText(getContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<LeavePendingApprovalResponse> call, @NonNull Throwable t) {
                loadingProgress.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadLeaves() {
        getLeaveApproval();
    }

    private void filterLeavesByDate(String fromDate, String toDate) {
        List<AppliedLeavesApproveItem> listToFilter;

        if (!originalLeaveList.isEmpty()) {
            listToFilter = originalLeaveList;
        } else if (leavePendingApprovalResponse != null && leavePendingApprovalResponse.getData() != null && leavePendingApprovalResponse.getData().getAppliedLeaves() != null) {
            listToFilter = leavePendingApprovalResponse.getData().getAppliedLeaves();
        } else {
            listToFilter = new ArrayList<>();
        }

        if (listToFilter != null) {
            List<AppliedLeavesApproveItem> filteredList = new ArrayList<>();
            for (AppliedLeavesApproveItem item : listToFilter) {
                String leaveDate = item.getAppliedDate();
                if (leaveDate != null) {
                    if (leaveDate.compareTo(fromDate) >= 0 && leaveDate.compareTo(toDate) <= 0) {
                        filteredList.add(item);
                    }
                }
            }
            filteredDataByDate.clear();
            filteredDataByDate.addAll(filteredList);
            if (approvalAdapter != null) {
                approvalAdapter.updateList(filteredList);
            }
        }
    }

    private void filterLeavesByDate(String fromDate, String toDate, List<AppliedLeavesApproveItem> listToFilter) {
        if (listToFilter != null) {
            List<AppliedLeavesApproveItem> filteredList = new ArrayList<>();
            for (AppliedLeavesApproveItem item : listToFilter) {
                String leaveDate = item.getAppliedDate();
                if (leaveDate != null) {
                    if (leaveDate.compareTo(fromDate) >= 0 && leaveDate.compareTo(toDate) <= 0) {
                        filteredList.add(item);
                    }
                }
            }
            filteredDataByDate.clear();
            filteredDataByDate.addAll(filteredList);
            if (approvalAdapter != null) {
                approvalAdapter.updateList(filteredList);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        view = null;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_approve_leave_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (super.onOptionsItemSelected(item)) {
            return true;
        }
        if (item.getItemId() == R.id.action_filter) {
            FilterBottomSheetDialogFragment filterBottomSheetDialogFragment = new FilterBottomSheetDialogFragment();
            filterBottomSheetDialogFragment.setFilterLeaveApprovalListener(this);
            filterBottomSheetDialogFragment.show(getParentFragmentManager(), filterBottomSheetDialogFragment.getTag());
            return true;

        } else if (item.getItemId() == R.id.action_cross_manager) {
            NavController navController = Navigation.findNavController(view);
            navController.navigate(R.id.action_nav_approve_leaves_to_nav_cross_approval_leave);
            Toast.makeText(getContext(), "Cross Approval", Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    public void onApprove(AttendanceRegularizeAppliedItem item) {
        if (pendingLeaveApproveFragment != null) {
            pendingLeaveApproveFragment.handleApprove(item.getId());
        }
        getLeaveApproval();
    }

    public void onReject(AttendanceRegularizeAppliedItem item) {
        getLeaveApproval();
    }

    public void onCancel(AttendanceRegularizeAppliedItem item) {
        getLeaveApproval();
    }
}
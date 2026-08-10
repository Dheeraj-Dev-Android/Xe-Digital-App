package app.xedigital.ai.ui.leaves;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
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

import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

import app.xedigital.ai.R;
import app.xedigital.ai.adapter.LeaveApprovalAdapter;
import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.api.APIInterface;
import app.xedigital.ai.model.leaveApprovalPending.AppliedLeavesItem;
import app.xedigital.ai.model.leaveApprovalPending.LeavePendingApprovalResponse;
import app.xedigital.ai.model.regularizeList.AttendanceRegularizeAppliedItem;
import app.xedigital.ai.utills.FilterBottomSheetDialogFragment;
import app.xedigital.ai.utills.SecurePrefManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ApproveLeaveFragment extends Fragment implements FilterLeaveApprovalListener {

    private static final String TAG = "ApproveLeaveDebug";

    private final List<AppliedLeavesItem> masterLeaveList = new ArrayList<>();

    private String authTokenHeader;
    private String userId;
    private LeaveApprovalAdapter approvalAdapter;
    private PendingLeaveApproveFragment pendingLeaveApproveFragment;
    private APIInterface apiInterface;
    private ProgressBar loadingProgress;
    private LinearLayout emptyStateContainer;
    private View view;
    private ChipGroup chipGroup;
    private ApproveLeaveViewModal approveLeaveViewModal;

    private String currentStatusFilter = "All";
    private String currentStartDate = "";
    private String currentEndDate = "";

    public static ApproveLeaveFragment newInstance() {
        return new ApproveLeaveFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        approveLeaveViewModal = new ViewModelProvider(this).get(ApproveLeaveViewModal.class);
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

        SecurePrefManager prefManager = SecurePrefManager.getInstance(requireContext());
        String authToken = prefManager.getString("authToken", "");
        userId = prefManager.getString("userId", "");
        authTokenHeader = "jwt " + authToken;

        approvalAdapter = new LeaveApprovalAdapter(new ArrayList<>(), authTokenHeader, userId, ApproveLeaveFragment.this, getContext());
        approvalRecyclerView.setAdapter(approvalAdapter);

        apiInterface = APIClient.getInstance().getPendingApprovalLeaves();

        chipGroup.setSingleSelection(true);
        setupChipListeners();

        loadingProgress.setVisibility(View.VISIBLE);
        getLeaveApproval();

        return view;
    }

    private void setupChipListeners() {
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.contains(R.id.chipApproved)) {
                currentStatusFilter = "approved";
            } else if (checkedIds.contains(R.id.chipUnapproved)) {
                currentStatusFilter = "unapproved";
            } else if (checkedIds.contains(R.id.chipRejected)) {
                currentStatusFilter = "rejected";
            } else if (checkedIds.contains(R.id.chipCancelled)) {
                currentStatusFilter = "cancelled";
            } else {
                currentStatusFilter = "All";
            }
            applyFilters();
        });
    }

    private void getLeaveApproval() {
        Call<LeavePendingApprovalResponse> call = apiInterface.getPendingApprovalLeaves(authTokenHeader, userId);
        call.enqueue(new Callback<LeavePendingApprovalResponse>() {
            @Override
            public void onResponse(@NonNull Call<LeavePendingApprovalResponse> call, @NonNull Response<LeavePendingApprovalResponse> response) {
                if (!isAdded()) {
                    Log.w(TAG, "onResponse: Fragment not attached to host context.");
                    return;
                }

                loadingProgress.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    LeavePendingApprovalResponse body = response.body();

                    if (body.getData() != null && body.getData().getAppliedLeaves() != null) {
                        List<AppliedLeavesItem> items = body.getData().getAppliedLeaves();

                        if (items.isEmpty()) {
                            showEmptyState();
                        } else {
                            emptyStateContainer.setVisibility(View.GONE);
                            masterLeaveList.clear();
                            masterLeaveList.addAll(items);
                            approveLeaveViewModal.leaveList.setValue(items);

                            applyFilters();
                        }
                    } else {

                        showEmptyState();
                    }
                } else {

                    showEmptyState();
                    Toast.makeText(getContext(), "Failed to retrieve leave records", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<LeavePendingApprovalResponse> call, @NonNull Throwable t) {
                if (!isAdded()) return;

                loadingProgress.setVisibility(View.GONE);
                Log.e(TAG, "Network failure in getLeaveApproval: " + t.getMessage(), t);
                Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEmptyState() {
        masterLeaveList.clear();
        emptyStateContainer.setVisibility(View.VISIBLE);
        if (approvalAdapter != null) {
            approvalAdapter.updateList(new ArrayList<>());
        }
        if (isAdded() && getContext() != null) {
            new AlertDialog.Builder(getContext()).setTitle("Approve Leaves").setMessage("No Records Found").setPositiveButton("OK", null).show();
        }
    }

    @Override
    public void onFilterApplied(String startDate, String endDate) {
        currentStartDate = startDate != null ? startDate : "";
        currentEndDate = endDate != null ? endDate : "";
        applyFilters();
    }

    private void applyFilters() {
        List<AppliedLeavesItem> dateFilteredList = filterByDate(masterLeaveList, currentStartDate, currentEndDate);

        List<AppliedLeavesItem> finalFilteredList = filterByStatus(dateFilteredList, currentStatusFilter);

        if (finalFilteredList.isEmpty()) {
            emptyStateContainer.setVisibility(View.VISIBLE);
        } else {
            emptyStateContainer.setVisibility(View.GONE);
        }

        if (approvalAdapter != null) {
            approvalAdapter.updateList(finalFilteredList);
        }
    }

    private List<AppliedLeavesItem> filterByDate(List<AppliedLeavesItem> sourceList, String fromDate, String toDate) {
        if (fromDate.isEmpty() || toDate.isEmpty()) {
            return new ArrayList<>(sourceList);
        }

        List<AppliedLeavesItem> result = new ArrayList<>();
        for (AppliedLeavesItem item : sourceList) {
            if (item != null) {
                String leaveDate = cleanDate(item.getAppliedDate());
                if (!leaveDate.isEmpty()) {
                    if (leaveDate.compareTo(fromDate) >= 0 && leaveDate.compareTo(toDate) <= 0) {
                        result.add(item);
                    }
                }
            }
        }
        return result;
    }

    private List<AppliedLeavesItem> filterByStatus(List<AppliedLeavesItem> sourceList, String status) {
        if (status == null || status.trim().equalsIgnoreCase("All")) {
            return new ArrayList<>(sourceList);
        }

        List<AppliedLeavesItem> result = new ArrayList<>();
        String targetStatus = status.trim();

        for (AppliedLeavesItem item : sourceList) {
            if (item != null && item.getStatus() != null) {
                String itemStatus = item.getStatus().trim();

                if (targetStatus.equalsIgnoreCase("unapproved")) {
                    if (itemStatus.equalsIgnoreCase("unapproved") || itemStatus.equalsIgnoreCase("pending")) {
                        result.add(item);
                    }
                } else if (itemStatus.equalsIgnoreCase(targetStatus)) {
                    result.add(item);
                }
            }
        }
        return result;
    }

    private String cleanDate(String rawDate) {
        if (rawDate != null && rawDate.contains("T")) {
            return rawDate.split("T")[0];
        }
        return rawDate != null ? rawDate : "";
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
        if (item.getItemId() == R.id.action_filter) {
            FilterBottomSheetDialogFragment filterDialogFragment = new FilterBottomSheetDialogFragment();
            filterDialogFragment.setFilterLeaveApprovalListener(this);
            filterDialogFragment.show(getParentFragmentManager(), filterDialogFragment.getTag());
            return true;
        } else if (item.getItemId() == R.id.action_cross_manager) {
            if (view != null) {
                NavController navController = Navigation.findNavController(view);
                navController.navigate(R.id.action_nav_approve_leaves_to_nav_cross_approval_leave);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
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
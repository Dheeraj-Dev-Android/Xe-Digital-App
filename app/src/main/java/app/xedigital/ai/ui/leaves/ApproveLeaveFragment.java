package app.xedigital.ai.ui.leaves;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
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
import androidx.fragment.app.FragmentTransaction;
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
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ApproveLeaveFragment extends Fragment implements FilterLeaveApprovalListener {

    private String authTokenHeader;
    private String userId;
    private LeaveApprovalAdapter approvalAdapter;
    private RecyclerView approvalRecyclerView;
    private PendingLeaveApproveFragment pendingLeaveApproveFragment;
    private LeavePendingApprovalResponse leavePendingApprovalResponse;
    private APIInterface apiInterface;
    private ProgressBar loadingProgress;
    private LinearLayout emptyStateContainer;
    private View view;

    public static ApproveLeaveFragment newInstance() {
        return new ApproveLeaveFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void onFilterApplied(String startDate, String endDate) {
        filterLeavesByDate(startDate, endDate);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_approve_leave, container, false);
        setHasOptionsMenu(true);

        approvalRecyclerView = view.findViewById(R.id.leaveApprovalRecyclerView);
        loadingProgress = view.findViewById(R.id.loadingProgress);
        emptyStateContainer = view.findViewById(R.id.emptyStateContainer);
        approvalRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        loadingProgress.setVisibility(View.VISIBLE);

        approvalAdapter = new LeaveApprovalAdapter(new ArrayList<>(), authTokenHeader, userId, ApproveLeaveFragment.this, getContext()); // Initialize with an empty list
        approvalRecyclerView.setAdapter(approvalAdapter);

        apiInterface = APIClient.getInstance().getPendingApprovalLeaves();
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String authToken = sharedPreferences.getString("authToken", "");
        userId = sharedPreferences.getString("userId", "");
        authTokenHeader = "jwt " + authToken;
        getLeaveApproval();

        ChipGroup chipGroup = view.findViewById(R.id.statusChipGroup);
        chipGroup.setSingleSelection(true);

        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            // Handle chip selection
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
        // Attach click listeners to chips
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            View child = chipGroup.getChildAt(i);
            if (child instanceof Chip) {
                child.setOnClickListener(this::onChipClicked);
            }
        }
        return view;
    }

    public void onChipClicked(View view) {
        // Get the ChipGroup and the clicked chip
        ChipGroup chipGroup = getView().findViewById(R.id.statusChipGroup);
        Chip clickedChip = (Chip) view;

        chipGroup.check(clickedChip.getId());
    }

    private void filterLeaves(String status) {
        if (leavePendingApprovalResponse != null && leavePendingApprovalResponse.getData() != null && leavePendingApprovalResponse.getData().getAppliedLeaves() != null) {
            List<AppliedLeavesApproveItem> originalList = leavePendingApprovalResponse.getData().getAppliedLeaves();
            List<AppliedLeavesApproveItem> filteredList = new ArrayList<>();

            if (status.equals("All")) {
                filteredList.addAll(originalList);
            } else {
                for (AppliedLeavesApproveItem item : originalList) {
                    if (item.getStatus().equalsIgnoreCase(status)) {
                        filteredList.add(item);
                    }
                }
            }
            if (approvalAdapter != null) {
                approvalAdapter.updateList(filteredList);
            } else {
                Log.e("ApproveLeaveFragment", "approvalAdapter is null in filterLeaves");
            }
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
                    if (items.isEmpty()) {
                        emptyStateContainer.setVisibility(View.VISIBLE);
                        Toast.makeText(getContext(), "No Data Found", Toast.LENGTH_SHORT).show();
                        new AlertDialog.Builder(getContext()).setTitle("Approve Leaves").setMessage("No Records Found").setPositiveButton("OK", null).show();
                    } else {
                        emptyStateContainer.setVisibility(View.GONE);
                        approvalAdapter = new LeaveApprovalAdapter(items, authTokenHeader, userId, ApproveLeaveFragment.this, getContext());
                        approvalRecyclerView.setAdapter(approvalAdapter);
                    }
                } else {
                    Toast.makeText(getContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                    Log.e("API Error", "Response not successful: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<LeavePendingApprovalResponse> call, @NonNull Throwable t) {
                loadingProgress.setVisibility(View.GONE);
                Log.e("Approval pending List", "Error: " + t.getMessage());
                Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void filterLeavesByDate(String fromDate, String toDate) {
        if (leavePendingApprovalResponse != null && leavePendingApprovalResponse.getData() != null && leavePendingApprovalResponse.getData().getAppliedLeaves() != null) {
            List<AppliedLeavesApproveItem> originalList = leavePendingApprovalResponse.getData().getAppliedLeaves();
            List<AppliedLeavesApproveItem> filteredList = new ArrayList<>();

            for (AppliedLeavesApproveItem item : originalList) {
                String leaveDate = item.getAppliedDate();

                // Check if leaveDate is within the specified range
                if (leaveDate.compareTo(fromDate) >= 0 && leaveDate.compareTo(toDate) <= 0) {
                    filteredList.add(item);
                }
            }
            approvalAdapter.updateList(filteredList);
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

        FragmentTransaction ft = requireActivity().getSupportFragmentManager().beginTransaction();
        ft.detach(pendingLeaveApproveFragment).attach(pendingLeaveApproveFragment).commit();
        getLeaveApproval();
    }

    public void onReject(AttendanceRegularizeAppliedItem item) {
        FragmentTransaction ft = requireActivity().getSupportFragmentManager().beginTransaction();
        ft.detach(pendingLeaveApproveFragment).attach(pendingLeaveApproveFragment).commit();
        if (pendingLeaveApproveFragment != null) {
            pendingLeaveApproveFragment.handleReject(item.getId(), "");
        }
        getLeaveApproval();
    }

    public void onCancel(AttendanceRegularizeAppliedItem item) {
        FragmentTransaction ft = requireActivity().getSupportFragmentManager().beginTransaction();
        ft.detach(pendingLeaveApproveFragment).attach(pendingLeaveApproveFragment).commit();
        if (pendingLeaveApproveFragment != null) {
            pendingLeaveApproveFragment.handleCancel(item.getId(), "");
        }
    }

}
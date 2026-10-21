package app.xedigital.ai.ui.regularize_attendance;

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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.ChipGroup;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

import app.xedigital.ai.R;
import app.xedigital.ai.adapter.RegularizeApprovalAdapter;
import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.api.APIInterface;
import app.xedigital.ai.model.regularizeList.AttendanceRegularizeAppliedItem;
import app.xedigital.ai.model.regularizeList.RegularizeApprovalResponse;
import app.xedigital.ai.utills.CustomDialogHelper;
import app.xedigital.ai.utills.SecurePrefManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PendingApprovalAttendance extends Fragment implements PendingApprovalViewFragment.OnRegularizeApprovalActionListener {
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private String authToken;
    private String userId;
    private RecyclerView approvalRecyclerView;
    private RegularizeApprovalAdapter adapter;
    private APIInterface apiInterface;
    private ProgressBar loadingProgress;
    private TextView emptyStateText;
    private LinearLayout emptyStateContainer;
    private RegularizeApprovalResponse regularizeApprovalResponse;
    private SecurePrefManager prefManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pending_approval_attendance, container, false);
        setHasOptionsMenu(true);

        approvalRecyclerView = view.findViewById(R.id.approval_recycler_view);
        loadingProgress = view.findViewById(R.id.loadingProgress);
        emptyStateContainer = view.findViewById(R.id.emptyStateContainer);
        emptyStateText = view.findViewById(R.id.emptyStateText);
        ChipGroup chipGroup = view.findViewById(R.id.statusChipGroup);

        prefManager = SecurePrefManager.getInstance(requireContext());
        authToken = prefManager.getString("authToken", "");
        userId = prefManager.getString("userId", "");

        adapter = new RegularizeApprovalAdapter(new ArrayList<>(), authToken, userId, this, getContext());
        approvalRecyclerView.setAdapter(adapter);
        approvalRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        loadingProgress.setVisibility(View.VISIBLE);
        apiInterface = APIClient.getInstance().getRegularizeListApproval();
        getRegularizeApproval();

        chipGroup.setSingleSelection(true);
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

        return view;
    }

    private void filterLeaves(String status) {
        if (adapter == null || regularizeApprovalResponse == null
                || regularizeApprovalResponse.getData() == null
                || regularizeApprovalResponse.getData().getAttendanceRegularizeApplied() == null) {
            return;
        }

        List<AttendanceRegularizeAppliedItem> originalList = regularizeApprovalResponse.getData().getAttendanceRegularizeApplied();
        List<AttendanceRegularizeAppliedItem> filteredList = new ArrayList<>();

        if ("All".equalsIgnoreCase(status)) {
            filteredList.addAll(originalList);
        } else {
            for (AttendanceRegularizeAppliedItem item : originalList) {
                if (item != null && item.getStatus() != null && item.getStatus().equalsIgnoreCase(status)) {
                    filteredList.add(item);
                }
            }
        }
        adapter.updateList(filteredList);
    }

    private void getRegularizeApproval() {
        Call<RegularizeApprovalResponse> call = apiInterface.getRegularizeApproval("jwt " + authToken);
        call.enqueue(new Callback<RegularizeApprovalResponse>() {
            @Override
            public void onResponse(@NonNull Call<RegularizeApprovalResponse> call, @NonNull Response<RegularizeApprovalResponse> response) {
                if (!isAdded() || getView() == null) return;

                loadingProgress.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    regularizeApprovalResponse = response.body();
                    List<AttendanceRegularizeAppliedItem> items = regularizeApprovalResponse.getData().getAttendanceRegularizeApplied();

                    if (items == null || items.isEmpty()) {
                        emptyStateContainer.setVisibility(View.VISIBLE);
                        approvalRecyclerView.setVisibility(View.GONE);
                        CustomDialogHelper.showInfoDialog(requireContext(),
                                "Attendance Regularization Approval List",
                                "No Records Found.");
                    } else {
                        emptyStateContainer.setVisibility(View.GONE);
                        approvalRecyclerView.setVisibility(View.VISIBLE);
                        adapter.updateList(items);
                    }
                } else {
                    emptyStateContainer.setVisibility(View.VISIBLE);
                    approvalRecyclerView.setVisibility(View.GONE);
                    emptyStateText.setText("Failed to load data. Please try again.");
                    CustomDialogHelper.showErrorDialog(requireContext(),
                            "Error",
                            "Failed to load data. Please try again.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<RegularizeApprovalResponse> call, @NonNull Throwable t) {
                if (!isAdded() || getView() == null) return;

                loadingProgress.setVisibility(View.GONE);
                emptyStateContainer.setVisibility(View.VISIBLE);
                approvalRecyclerView.setVisibility(View.GONE);
                emptyStateText.setText("Failed to load data. Please try again.");
                Log.e("Approval pending List", "Error: " + t.getMessage());
                CustomDialogHelper.showErrorDialog(requireContext(),
                        "Network Error",
                        "Unable to connect. Please check your internet connection.");
            }
        });
    }

    public void onApprove(AttendanceRegularizeAppliedItem item) {
        // Refresh the list after adapter handles the approve API call
        getRegularizeApproval();
    }

    public void onReject(AttendanceRegularizeAppliedItem item) {
        // Refresh the list after adapter handles the reject API call
        getRegularizeApproval();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_regularize_attendance, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_cross_manager_attendance) {
            NavHostFragment navHostFragment = (NavHostFragment) requireActivity().getSupportFragmentManager()
                    .findFragmentById(R.id.nav_host_fragment_content_main);
            if (navHostFragment != null) {
                NavController navController = navHostFragment.getNavController();
                navController.navigate(R.id.action_nav_pendingApprovalFragment_to_nav_cross_approval_attendance);
                Toast.makeText(getContext(), "Cross Approval", Toast.LENGTH_SHORT).show();
                return true;
            }
        }
        return false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        approvalRecyclerView = null;
        loadingProgress = null;
        emptyStateText = null;
        emptyStateContainer = null;
    }
}
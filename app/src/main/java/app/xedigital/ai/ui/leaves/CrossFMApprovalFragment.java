package app.xedigital.ai.ui.leaves;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import app.xedigital.ai.R;
import app.xedigital.ai.adapter.CrossManagerLeaveApprovalAdapter;
import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.api.APIInterface;
import app.xedigital.ai.model.cmLeaveApprovalPending.AppliedLeavesItem;
import app.xedigital.ai.model.cmLeaveApprovalPending.CMLeavePendingResponse;
import app.xedigital.ai.utills.SecurePrefManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class CrossFMApprovalFragment extends Fragment {

    private static final String TAG = "CrossFMApproval";

    private String authToken;
    private String userId;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private CrossManagerLeaveApprovalAdapter crossManagerLeaveApprovalAdapter;
    private CMLeavePendingResponse cmLeavePendingApprovalResponse;
    private LinearLayout emptyStateView;
    private Button retryButton;

    // Track current chip filter so it survives swipe-refresh
    private String currentFilter = "All";


    public CrossFMApprovalFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cross_f_m_approval, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        progressBar = view.findViewById(R.id.progressBar);
        emptyStateView = view.findViewById(R.id.emptyStateView);
        retryButton = view.findViewById(R.id.retryButton);

        SecurePrefManager prefManager = SecurePrefManager.getInstance(requireContext());
        authToken = prefManager.getString("authToken", "");
        userId = prefManager.getString("userId", "");

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        swipeRefreshLayout.setOnRefreshListener(this::fetchPendingLeaves);
        retryButton.setOnClickListener(v -> fetchPendingLeaves());

        ChipGroup chipGroup = view.findViewById(R.id.statusChipGroup);
        chipGroup.setSingleSelection(true);

        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.contains(R.id.chipAll)) {
                currentFilter = "All";
            } else if (checkedIds.contains(R.id.chipApproved)) {
                currentFilter = "Approved";
            } else if (checkedIds.contains(R.id.chipUnapproved)) {
                currentFilter = "Unapproved";
            } else if (checkedIds.contains(R.id.chipRejected)) {
                currentFilter = "Rejected";
            } else if (checkedIds.contains(R.id.chipCancelled)) {
                currentFilter = "Cancelled";
            } else {
                currentFilter = "All";
            }
            filterLeaves(currentFilter);
        });

        fetchPendingLeaves();

        return view;
    }

    // ======================== FILTER ========================

    private void filterLeaves(String status) {
        if (cmLeavePendingApprovalResponse == null
                || cmLeavePendingApprovalResponse.getData() == null
                || cmLeavePendingApprovalResponse.getData().getAppliedLeaves() == null) {
            return;
        }

        List<AppliedLeavesItem> originalList = cmLeavePendingApprovalResponse.getData().getAppliedLeaves();
        List<AppliedLeavesItem> filteredList;

        if ("All".equals(status)) {
            filteredList = new ArrayList<>(originalList);
        } else {
            filteredList = originalList.stream()
                    .filter(item -> item.getStatus() != null
                            && item.getStatus().equalsIgnoreCase(status))
                    .collect(Collectors.toList());
        }

        if (crossManagerLeaveApprovalAdapter != null) {
            crossManagerLeaveApprovalAdapter.updateList(filteredList);
            toggleEmptyState(filteredList.isEmpty());
        } else {
            Log.e(TAG, "Adapter is null during filter");
        }
    }

    // ======================== FETCH DATA ========================

    private void fetchPendingLeaves() {
        progressBar.setVisibility(View.VISIBLE);
        emptyStateView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);

        String authHeader = "jwt " + authToken;
        APIInterface apiService = APIClient.getInstance().getPendingApprovalLeaves();

        Call<CMLeavePendingResponse> call = apiService.getPendingApprovalLeavesCR(authHeader, userId);
        call.enqueue(new Callback<CMLeavePendingResponse>() {
            @Override
            public void onResponse(@NonNull Call<CMLeavePendingResponse> call,
                                   @NonNull Response<CMLeavePendingResponse> response) {
                if (!isAdded()) return;

                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null) {
                    cmLeavePendingApprovalResponse = response.body();

                    List<AppliedLeavesItem> appliedLeaves = null;
                    if (cmLeavePendingApprovalResponse.getData() != null) {
                        appliedLeaves = cmLeavePendingApprovalResponse.getData().getAppliedLeaves();
                    }

                    if (appliedLeaves != null && !appliedLeaves.isEmpty()) {
                        // Reuse existing adapter if possible; create new only on first load
                        if (crossManagerLeaveApprovalAdapter == null) {
                            crossManagerLeaveApprovalAdapter =
                                    new CrossManagerLeaveApprovalAdapter(appliedLeaves, requireContext());
                            recyclerView.setAdapter(crossManagerLeaveApprovalAdapter);
                        } else {
                            crossManagerLeaveApprovalAdapter.updateList(appliedLeaves);
                        }

                        // Re-apply the current chip filter after refresh
                        filterLeaves(currentFilter);
                        recyclerView.setVisibility(View.VISIBLE);
                    } else {
                        toggleEmptyState(true);
                    }
                } else {
                    toggleEmptyState(true);
                    String errorMsg = "Failed to load leaves";
                    try {
                        if (response.errorBody() != null) {
                            errorMsg = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing error body", e);
                    }
                    Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<CMLeavePendingResponse> call,
                                  @NonNull Throwable throwable) {
                if (!isAdded()) return;

                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);

                String message = throwable.getMessage() != null ? throwable.getMessage() : "Unknown error";
                Log.e(TAG, "Fetch failed: " + message);
                toggleEmptyState(true);
                Toast.makeText(requireContext(), "Network Error: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ======================== HELPERS ========================

    private void toggleEmptyState(boolean isEmpty) {
        if (isEmpty) {
            emptyStateView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyStateView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
}
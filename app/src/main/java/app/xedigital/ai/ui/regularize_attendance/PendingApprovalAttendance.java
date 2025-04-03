package app.xedigital.ai.ui.regularize_attendance;

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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
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
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PendingApprovalAttendance extends Fragment implements PendingApprovalViewFragment.OnRegularizeApprovalActionListener {
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private String authToken;
    private String userId;
    private RecyclerView approvalRecyclerView;
    private RegularizeApprovalAdapter adapter;
    private PendingApprovalViewFragment pendingApprovalViewFragment;
    private APIInterface apiInterface;
    private ProgressBar loadingProgress;
    private TextView emptyStateText;
    private LinearLayout emptyStateContainer;
    private RegularizeApprovalResponse regularizeApprovalResponse;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pending_approval_attendance, container, false);
        setHasOptionsMenu(true);
        approvalRecyclerView = view.findViewById(R.id.approval_recycler_view);
        loadingProgress = view.findViewById(R.id.loadingProgress);
        emptyStateContainer = view.findViewById(R.id.emptyStateContainer);
        approvalRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        loadingProgress.setVisibility(View.VISIBLE);
        apiInterface = APIClient.getInstance().getRegularizeListApproval();
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        authToken = sharedPreferences.getString("authToken", "");
        userId = sharedPreferences.getString("userId", "");
        getRegularizeApproval();
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
        ChipGroup chipGroup = requireView().findViewById(R.id.statusChipGroup);
        Chip clickedChip = (Chip) view;

        // Check the clicked chip
        chipGroup.check(clickedChip.getId());
    }

    private void filterLeaves(String status) {
        if (regularizeApprovalResponse != null && regularizeApprovalResponse.getData() != null && regularizeApprovalResponse.getData().getAttendanceRegularizeApplied() != null) {
            List<AttendanceRegularizeAppliedItem> originalList = regularizeApprovalResponse.getData().getAttendanceRegularizeApplied();
            List<AttendanceRegularizeAppliedItem> filteredList = new ArrayList<>();

            if (status.equals("All")) {
                filteredList.addAll(originalList);
            } else {
                for (AttendanceRegularizeAppliedItem item : originalList) {
                    if (item.getStatus().equalsIgnoreCase(status)) {
                        filteredList.add(item);
                    }
                }
            }
            adapter.updateList(filteredList);
        }
    }

    private void getRegularizeApproval() {
        Call<RegularizeApprovalResponse> call = apiInterface.getRegularizeApproval("jwt " + authToken);
        call.enqueue(new Callback<RegularizeApprovalResponse>() {
            @Override
            public void onResponse(@NonNull Call<RegularizeApprovalResponse> call, @NonNull Response<RegularizeApprovalResponse> response) {
                loadingProgress.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    regularizeApprovalResponse = response.body();
                    List<AttendanceRegularizeAppliedItem> items = regularizeApprovalResponse.getData().getAttendanceRegularizeApplied();

                    if (items.isEmpty()) {
                        emptyStateContainer.setVisibility(View.VISIBLE);
                        Toast.makeText(getContext(), "No data found", Toast.LENGTH_SHORT).show();
                        new AlertDialog.Builder(getContext()).setTitle("Attendance Regularization Approval List").setMessage("No Records Found.").setPositiveButton("OK", null).show();
                    } else {
                        emptyStateContainer.setVisibility(View.GONE);
                        adapter = new RegularizeApprovalAdapter(items, authToken, userId, PendingApprovalAttendance.this, getContext());
                        approvalRecyclerView.setAdapter(adapter);
//                        Log.d("Approval pending List", gson.toJson(response.body()));
//                        adapter.setOnItemClickListener(item -> {
//                            pendingApprovalViewFragment = PendingApprovalViewFragment.newInstance(item);
//                            pendingApprovalViewFragment.setListener(PendingApprovalAttendance.this);
//                            requireActivity().getSupportFragmentManager().beginTransaction()
//                                    .replace(R.id.action_nav_pendingApprovalFragment_to_nav_pendingApprovalViewFragment, pendingApprovalViewFragment) // Assuming 'fragment_container' is the ID of your fragment container
//                                    .addToBackStack(null)
//                                    .commit();
//                        });
                    }
                } else {
                    Log.d("Approval pending List", "Failed");
                    Toast.makeText(getContext(), "Failed to load data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<RegularizeApprovalResponse> call, @NonNull Throwable t) {
                loadingProgress.setVisibility(View.GONE);
                Log.e("Approval pending List", "Error: " + t.getMessage());
                Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onApprove(AttendanceRegularizeAppliedItem item) {
        if (pendingApprovalViewFragment != null) {
            pendingApprovalViewFragment.handleApprove(item.getId());
        }

        FragmentTransaction ft = requireActivity().getSupportFragmentManager().beginTransaction();
        ft.detach(pendingApprovalViewFragment).attach(pendingApprovalViewFragment).commit();
        getRegularizeApproval();
    }

    public void onReject(AttendanceRegularizeAppliedItem item) {
        FragmentTransaction ft = requireActivity().getSupportFragmentManager().beginTransaction();
        ft.detach(pendingApprovalViewFragment).attach(pendingApprovalViewFragment).commit();
        if (pendingApprovalViewFragment != null) {
            pendingApprovalViewFragment.handleReject(item.getId());
        }
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
            //Correct way to get the navController in a Fragment.
            NavHostFragment navHostFragment = (NavHostFragment) requireActivity().getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main);
            if (navHostFragment != null) {
                NavController navController = navHostFragment.getNavController();
                navController.navigate(R.id.action_nav_pendingApprovalFragment_to_nav_cross_approval_attendance);
                Toast.makeText(getContext(), "Cross Approval", Toast.LENGTH_SHORT).show();
                return true;
            }
        }
        return false;
    }
}
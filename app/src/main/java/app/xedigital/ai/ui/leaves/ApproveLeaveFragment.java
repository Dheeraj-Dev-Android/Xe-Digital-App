package app.xedigital.ai.ui.leaves;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import app.xedigital.ai.R;
import app.xedigital.ai.adapter.LeaveApprovalAdapter;
import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.api.APIInterface;
import app.xedigital.ai.databinding.FragmentApproveLeaveBinding;
import app.xedigital.ai.model.leaveApprovalPending.AppliedLeavesApproveItem;
import app.xedigital.ai.model.leaveApprovalPending.LeavePendingApprovalResponse;
import app.xedigital.ai.model.regularizeList.AttendanceRegularizeAppliedItem;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ApproveLeaveFragment extends Fragment {

    private String authToken;
    private String authTokenHeader;
    private String userId;
    private LeaveApprovalAdapter approvalAdapter;
    private RecyclerView approvalRecyclerView;
    private PendingLeaveApproveFragment pendingLeaveApproveFragment;
    private APIInterface apiInterface;
    private FragmentApproveLeaveBinding binding;

    public static ApproveLeaveFragment newInstance() {
        return new ApproveLeaveFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_approve_leave, container, false);

        approvalRecyclerView = view.findViewById(R.id.leaveApprovalRecyclerView);
//        RecyclerView leaveApprovalRecyclerView = binding.leaveApprovalRecyclerView;
        approvalRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        apiInterface = APIClient.getInstance().getPendingApprovalLeaves();
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        authToken = sharedPreferences.getString("authToken", "");
        userId = sharedPreferences.getString("userId", "");
        authTokenHeader = "jwt " + authToken;
        getLeaveApproval();

//        viewModel.getLeaveApprovalList().observe(getViewLifecycleOwner(), items -> {
//            if (approvalAdapter == null) {
//                approvalAdapter = new LeaveApprovalAdapter(items, authTokenHeader, userId, ApproveLeaveFragment.this, getContext());
//                binding.leaveApprovalRecyclerView.setAdapter(approvalAdapter);
//            } else {
////                approvalAdapter.updateItems(items);
//                approvalAdapter.notifyDataSetChanged();
//            }
//        });
//
//        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
//            if (errorMessage != null) {
//                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        viewModel.getPendingApprovalLeaves(authTokenHeader, userId);

        return view;
    }

    private void getLeaveApproval() {
        Call<LeavePendingApprovalResponse> call = apiInterface.getPendingApprovalLeaves(authTokenHeader, userId);
        call.enqueue(new Callback<LeavePendingApprovalResponse>() {
            @Override
            public void onResponse(@NonNull Call<LeavePendingApprovalResponse> call, @NonNull Response<LeavePendingApprovalResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LeavePendingApprovalResponse leavePendingApprovalResponse = response.body();
                    List<AppliedLeavesApproveItem> items = leavePendingApprovalResponse.getData().getAppliedLeaves();
                    if (items.isEmpty()) {
                        Toast.makeText(getContext(), "No Data Found", Toast.LENGTH_SHORT).show();
                        new AlertDialog.Builder(getContext())
                                .setTitle("No Data Found")
                                .setMessage("No Data Found")
                                .setPositiveButton("OK", null)
                                .show();
                    } else {
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
                Log.e("Approval pending List", "Error: " + t.getMessage());
                Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
            }
        });


    }
//    @Override
//    public void onResume() {
//        super.onResume();
//        updateLeaveApprovalList();
//    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

//    public void updateLeaveApprovalList() {
//        viewModel.getPendingApprovalLeaves(authTokenHeader, userId);
//    }
//
//    public void onApprove(AttendanceRegularizeAppliedItem item) {
//        viewModel.getPendingApprovalLeaves(authTokenHeader, userId);
//    }

//    public void onReject(AttendanceRegularizeAppliedItem item) {
//
//        viewModel.getPendingApprovalLeaves(authTokenHeader, userId);
//    }

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
            pendingLeaveApproveFragment.handleReject(item.getId());
        }
        getLeaveApproval();
    }

    public void onCancel(AttendanceRegularizeAppliedItem item) {
        FragmentTransaction ft = requireActivity().getSupportFragmentManager().beginTransaction();
        ft.detach(pendingLeaveApproveFragment).attach(pendingLeaveApproveFragment).commit();
        if (pendingLeaveApproveFragment != null) {
            pendingLeaveApproveFragment.handleCancel(item.getId());
        }
    }

}
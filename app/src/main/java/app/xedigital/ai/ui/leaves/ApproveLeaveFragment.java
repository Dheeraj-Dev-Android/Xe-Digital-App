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
import app.xedigital.ai.model.leaveApprovalPending.AppliedLeavesItem;
import app.xedigital.ai.model.leaveApprovalPending.LeavePendingApprovalResponse;
import app.xedigital.ai.model.regularizeList.AttendanceRegularizeAppliedItem;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ApproveLeaveFragment extends Fragment {

    private RecyclerView leaveApprovalRecyclerView;
    private APIInterface apiInterface;
    private String authToken;
    private String userId;
    private LeaveApprovalAdapter approvalAdapter;
    private PendingLeaveApproveFragment pendingApprovalLeaveFragment;
    ;

    public static ApproveLeaveFragment newInstance() {
        return new ApproveLeaveFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_approve_leave, container, false);

        leaveApprovalRecyclerView = view.findViewById(R.id.leave_approval_recycler_view);
        leaveApprovalRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        apiInterface = APIClient.getInstance().getPendingApprovalLeaves();
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        authToken = sharedPreferences.getString("authToken", "");
        userId = sharedPreferences.getString("userId", "");
        getPendingApprovalLeaves();

        return view;
    }

    public void getPendingApprovalLeaves() {
        Call<LeavePendingApprovalResponse> call = apiInterface.getPendingApprovalLeaves(authToken, userId);
        call.enqueue(new Callback<LeavePendingApprovalResponse>() {
            @Override
            public void onResponse(@NonNull Call<LeavePendingApprovalResponse> call, @NonNull Response<LeavePendingApprovalResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LeavePendingApprovalResponse leavePendingApprovalResponse = response.body();
                    List<AppliedLeavesItem> items = leavePendingApprovalResponse.getData().getAppliedLeaves();

                    if (items.isEmpty()) {
                        Toast.makeText(requireContext(), "No Data Found", Toast.LENGTH_SHORT).show();
                        new AlertDialog.Builder(getContext()).setTitle("Leave Pending Approval List").setMessage("No data found in the list.").setPositiveButton("OK", null).show();
                    } else {
                        approvalAdapter = new LeaveApprovalAdapter(items, authToken, userId, ApproveLeaveFragment.this, getContext());
                        leaveApprovalRecyclerView.setAdapter(approvalAdapter);
                    }

                } else {
                    Toast.makeText(requireContext(), "Something Went Wrong", Toast.LENGTH_SHORT).show();
                    Log.d("Approval Pending list", "onResponse: " + response.code());
                }

            }

            @Override
            public void onFailure(@NonNull Call<LeavePendingApprovalResponse> call, @NonNull Throwable t) {
                Log.e("Approval pending List", "Error: " + t.getMessage());
                Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
            }

        });
    }

    public void updateLeaveApprovalList() {
        getPendingApprovalLeaves();
    }

    public void onApprove(AttendanceRegularizeAppliedItem item) {
        if (pendingApprovalLeaveFragment != null) {
            pendingApprovalLeaveFragment.handleApprove(item.getId());
        }

        FragmentTransaction ft = requireActivity().getSupportFragmentManager().beginTransaction();
        ft.detach(pendingApprovalLeaveFragment).attach(pendingApprovalLeaveFragment).commit();
        getPendingApprovalLeaves();
    }

    public void onReject(AttendanceRegularizeAppliedItem item) {
        FragmentTransaction ft = requireActivity().getSupportFragmentManager().beginTransaction();
        ft.detach(pendingApprovalLeaveFragment).attach(pendingApprovalLeaveFragment).commit();
        if (pendingApprovalLeaveFragment != null) {
            pendingApprovalLeaveFragment.handleReject(item.getId());
        }
        getPendingApprovalLeaves();
    }


}
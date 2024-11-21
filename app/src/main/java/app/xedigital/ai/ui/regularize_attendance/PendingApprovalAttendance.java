package app.xedigital.ai.ui.regularize_attendance;

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
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pending_approval_attendance, container, false);

        approvalRecyclerView = view.findViewById(R.id.approval_recycler_view);
        approvalRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        apiInterface = APIClient.getInstance().getRegularizeListApproval();
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        authToken = sharedPreferences.getString("authToken", "");
        userId = sharedPreferences.getString("userId", "");
        getRegularizeApproval();

        return view;
    }

    private void getRegularizeApproval() {
        Call<RegularizeApprovalResponse> call = apiInterface.getRegularizeApproval("jwt " + authToken);
        call.enqueue(new Callback<RegularizeApprovalResponse>() {
            @Override
            public void onResponse(@NonNull Call<RegularizeApprovalResponse> call, @NonNull Response<RegularizeApprovalResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    RegularizeApprovalResponse apiResponse = response.body();
                    List<AttendanceRegularizeAppliedItem> items = apiResponse.getData().getAttendanceRegularizeApplied();

                    if (items.isEmpty()) {
                        Toast.makeText(getContext(), "No data found", Toast.LENGTH_SHORT).show();
                        new AlertDialog.Builder(getContext())
                                .setTitle("Attendance Regularization Approval List")
                                .setMessage("No data found in the list.")
                                .setPositiveButton("OK", null)
                                .show();
                    } else {
                        adapter = new RegularizeApprovalAdapter(items, authToken,userId, PendingApprovalAttendance.this, getContext());
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
}
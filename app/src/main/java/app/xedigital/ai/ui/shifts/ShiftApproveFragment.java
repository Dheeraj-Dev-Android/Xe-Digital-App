package app.xedigital.ai.ui.shifts;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import app.xedigital.ai.R;
import app.xedigital.ai.adapter.ShiftApprovalListAdapter;
import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.api.APIInterface;
import app.xedigital.ai.model.shiftApprovalList.EmployeeApproveShiftdataItem;
import app.xedigital.ai.model.shiftApprovalList.ShiftApproveListResponse;
import app.xedigital.ai.ui.profile.ProfileViewModel;
import app.xedigital.ai.utills.SecurePrefManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShiftApproveFragment extends Fragment {

    private RecyclerView ShiftApproveRecyclerView;
    private ProgressBar loadingProgress;
    private TextView emptyStateText;
    private View emptyStateContainer;
    private ProfileViewModel profileViewModel;
    private SecurePrefManager prefManager;

    public ShiftApproveFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_shift_approve, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        ShiftApproveRecyclerView = view.findViewById(R.id.ShiftApproveRecyclerView);
        loadingProgress = view.findViewById(R.id.loadingProgress);
        emptyStateText = view.findViewById(R.id.emptyStateText);
        emptyStateContainer = view.findViewById(R.id.emptyStateContainer);
        ShiftApproveRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

//        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        prefManager = SecurePrefManager.getInstance(requireContext());
        String token = prefManager.getString("authToken", "");

        APIInterface shiftApprovalList = APIClient.getInstance().getShiftTypes();
        String authToken = "jwt " + token;

        Call<ShiftApproveListResponse> call = shiftApprovalList.getShiftApprovalList(authToken);
        call.enqueue(new Callback<ShiftApproveListResponse>() {
            @Override
            public void onResponse(@NonNull Call<ShiftApproveListResponse> call, @NonNull Response<ShiftApproveListResponse> response) {
                loadingProgress.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    ShiftApproveListResponse shiftList = response.body();
                    if (shiftList != null && shiftList.getData() != null && shiftList.getData().getEmployeeShiftdata() != null && !shiftList.getData().getEmployeeShiftdata().isEmpty()) {
                        List<EmployeeApproveShiftdataItem> shiftApprovalDataList = shiftList.getData().getEmployeeShiftdata();
                        ShiftApprovalListAdapter adapter = new ShiftApprovalListAdapter(getContext(), shiftApprovalDataList, getViewLifecycleOwner(), profileViewModel);
                        ShiftApproveRecyclerView.setAdapter(adapter);
                    } else {
                        emptyStateContainer.setVisibility(View.VISIBLE);
                    }
                } else {
                    emptyStateContainer.setVisibility(View.VISIBLE);
                    Log.e("ShiftApproveFragment", "API Error: " + response.code() + " - " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ShiftApproveListResponse> call, @NonNull Throwable t) {
                loadingProgress.setVisibility(View.GONE);
                emptyStateContainer.setVisibility(View.VISIBLE);
                Log.e("ShiftApproveFragment", "Network Error: " + t.getMessage());
            }
        });
    }
}
package app.xedigital.ai.ui.shifts;

import android.content.Context;
import android.content.SharedPreferences;
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
import app.xedigital.ai.model.shiftApprovalList.EmployeeShiftdataItem;
import app.xedigital.ai.model.shiftApprovalList.ShiftApproveListResponse;
import app.xedigital.ai.ui.profile.ProfileViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShiftApproveFragment extends Fragment {

    private RecyclerView ShiftApproveRecyclerView;
    private ProgressBar loadingProgress;
    private TextView emptyStateText;
    private ProfileViewModel profileViewModel;

    public ShiftApproveFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_shift_approve, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        ShiftApproveRecyclerView = view.findViewById(R.id.ShiftApproveRecyclerView);
        loadingProgress = view.findViewById(R.id.loadingProgress);
        emptyStateText = view.findViewById(R.id.emptyStateText);

        ShiftApproveRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("authToken", "");

        APIInterface shiftApprovalList = APIClient.getInstance().getShiftTypes();
        String authToken = "jwt " + token;

        Call<ShiftApproveListResponse> call = shiftApprovalList.getShiftApprovalList(authToken);
        call.enqueue(new Callback<ShiftApproveListResponse>() {
            @Override
            public void onResponse(@NonNull Call<ShiftApproveListResponse> call, @NonNull Response<ShiftApproveListResponse> response) {
                loadingProgress.setVisibility(View.GONE);

                if (response.isSuccessful()) {
                    ShiftApproveListResponse shiftList = response.body();
                    // Access data using the correct structure:
                    if (shiftList != null && shiftList.getData() != null && shiftList.getData().getEmployeeShiftdata() != null && !shiftList.getData().getEmployeeShiftdata().isEmpty()) {
                        List<EmployeeShiftdataItem> shiftApprovalDataList = shiftList.getData().getEmployeeShiftdata();
                        ShiftApprovalListAdapter adapter = new ShiftApprovalListAdapter(getContext(), shiftApprovalDataList, getViewLifecycleOwner(), profileViewModel);
                        ShiftApproveRecyclerView.setAdapter(adapter);
                    } else {
                        emptyStateText.setVisibility(View.VISIBLE);
                    }
                } else {
                    // Handle API error, potentially show an error message
                    Log.e("ShiftApproveFragment", "API Error: " + response.code() + " - " + response.message());
                    // You might want to display an error message to the user here
                }
            }

            @Override
            public void onFailure(@NonNull Call<ShiftApproveListResponse> call, @NonNull Throwable t) {
                loadingProgress.setVisibility(View.GONE);
                // Handle network error, potentially show an error message
                Log.e("ShiftApproveFragment", "Network Error: " + t.getMessage());
                // You might want to display an error message to the user here
            }
        });
    }
}
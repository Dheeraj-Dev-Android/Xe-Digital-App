package app.xedigital.ai.ui.shifts;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.List;

import app.xedigital.ai.adapter.ShiftAppliedAdapter;
import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.api.APIInterface;
import app.xedigital.ai.databinding.FragmentShiftAppliedBinding;
import app.xedigital.ai.model.shiftApplied.EmployeeShiftdataItem;
import app.xedigital.ai.model.shiftApplied.ShiftAppliedResponse;
import app.xedigital.ai.utills.SecurePrefManager;
import retrofit2.Call;
import retrofit2.Response;

public class ShiftAppliedFragment extends Fragment {
    private String authToken, userId;
    private ShiftAppliedAdapter shiftAdapter;
    private FragmentShiftAppliedBinding binding;
    private SecurePrefManager prefManager;

    public ShiftAppliedFragment() {
        // Required empty public constructor
    }

    public static ShiftAppliedFragment newInstance(String param1, String param2) {
        return new ShiftAppliedFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentShiftAppliedBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
//        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        prefManager = SecurePrefManager.getInstance(requireContext());
        authToken = prefManager.getString("authToken", "");
        userId = prefManager.getString("userId", "");
        getShiftApplied(authToken);

        return view;
    }

    private void getShiftApplied(String authToken) {
        binding.loadingProgress.setVisibility(View.VISIBLE);
        String token = "jwt " + authToken;
        APIInterface shiftApplied = APIClient.getInstance().getShiftTypes();
        Call<ShiftAppliedResponse> call = shiftApplied.getShiftApplied(token);
        call.enqueue(new retrofit2.Callback<ShiftAppliedResponse>() {
            @Override
            public void onResponse(@NonNull Call<ShiftAppliedResponse> call, @NonNull Response<ShiftAppliedResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    binding.loadingProgress.setVisibility(View.GONE);
                    List<EmployeeShiftdataItem> shiftList = response.body().getData().getEmployeeShiftdata();
                    if (shiftList != null && !shiftList.isEmpty()) {
                        shiftAdapter = new ShiftAppliedAdapter(shiftList);
                        binding.ShiftAppliedRecyclerView.setAdapter(shiftAdapter);
                        binding.ShiftAppliedRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                    } else {
                        binding.emptyStateContainer.setVisibility(View.VISIBLE);
                    }
                } else {
                    binding.loadingProgress.setVisibility(View.GONE);
                    binding.emptyStateContainer.setVisibility(View.VISIBLE);
                    Log.e("Error", "Response not successful");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ShiftAppliedResponse> call, @NonNull Throwable throwable) {
                Log.e("Error", "API call failed: " + throwable.getMessage());
                binding.emptyStateContainer.setVisibility(View.VISIBLE);
            }
        });
    }
}
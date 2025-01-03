package app.xedigital.ai.ui.timesheet;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import app.xedigital.ai.R;
import app.xedigital.ai.adapter.DcrAdapter;
import app.xedigital.ai.databinding.FragmentDcrBinding;
import app.xedigital.ai.model.dcrData.Data;
import app.xedigital.ai.model.dcrData.DcrDataResponse;
import app.xedigital.ai.model.dcrData.EmployeesDcrDataItem;
import app.xedigital.ai.utills.FilterBottomSheetDialogFragment;

public class DcrFragment extends Fragment implements FilterAppliedListener {
    private FragmentDcrBinding binding;
    private TimesheetViewModel dcrViewModel;
    private RecyclerView recyclerViewDcr;
    private DcrAdapter dcrAdapter;

    private ProgressBar loadingProgress;
    private LinearLayout emptyStateContainer;
    public void onFilterApplied(String startDate, String endDate) {
        dcrViewModel.fetchEmployeeDcr(startDate, endDate);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        dcrViewModel = new ViewModelProvider(this).get(TimesheetViewModel.class);
        binding = FragmentDcrBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);

        loadingProgress = binding.loadingProgress;
        emptyStateContainer = binding.emptyStateContainer;

        loadingProgress.setVisibility(View.VISIBLE);
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String authToken = sharedPreferences.getString("authToken", null);
        String userId = sharedPreferences.getString("userId", null);

        dcrViewModel.storeLoginData(authToken);

        String startDate = "";
        String endDate = "";
        dcrViewModel.fetchEmployeeDcr(startDate, endDate);
        dcrViewModel.callUserApi(userId, authToken);

        recyclerViewDcr = binding.recyclerViewDcr;
        recyclerViewDcr.setLayoutManager(new LinearLayoutManager(requireContext()));

        dcrAdapter = new DcrAdapter(new ArrayList<>(), this);
        recyclerViewDcr.setAdapter(dcrAdapter);

        dcrViewModel.getDcrData().observe(getViewLifecycleOwner(), dcrDataList -> {
            loadingProgress.setVisibility(View.GONE);

            if (dcrDataList != null) {
                List<EmployeesDcrDataItem> dcr = parseDcrData(dcrDataList);

                if (dcr.isEmpty()) {
                    emptyStateContainer.setVisibility(View.VISIBLE);
                } else {
                    emptyStateContainer.setVisibility(View.GONE);
                    dcrAdapter.updateData(dcr);
                    recyclerViewDcr.setAdapter(dcrAdapter);
                }
            } else {
                emptyStateContainer.setVisibility(View.VISIBLE);
            }
        });
    }

    private List<EmployeesDcrDataItem> parseDcrData(DcrDataResponse dcrResponse) {
        List<EmployeesDcrDataItem> dcrDataList = new ArrayList<>();

        if (dcrResponse != null && dcrResponse.getData() != null) {
            Data data = dcrResponse.getData();
            if (data.getEmployeesDcrData() != null) {
                return data.getEmployeesDcrData();
            }
        }
        return new ArrayList<>();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_dcr_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (super.onOptionsItemSelected(item)) {
            return true;
        }

        int id = item.getItemId();
        if (id == R.id.action_filter) {
            FilterBottomSheetDialogFragment filterBottomSheetDialogFragment = new FilterBottomSheetDialogFragment();
            filterBottomSheetDialogFragment.setFilterAppliedListener(this);
            filterBottomSheetDialogFragment.show(getParentFragmentManager(), filterBottomSheetDialogFragment.getTag());
            return true;
        }
        return false;
    }
}
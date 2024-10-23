package app.xedigital.ai.ui.timesheet;

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
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import app.xedigital.ai.R;
import app.xedigital.ai.adapter.DcrAdapter;
import app.xedigital.ai.databinding.FragmentDcrBinding;
import app.xedigital.ai.model.dcrData.Data;
import app.xedigital.ai.model.dcrData.DcrDataResponse;
import app.xedigital.ai.model.dcrData.EmployeesDcrDataItem;
import app.xedigital.ai.utills.FilterBottomSheetDialogFragment;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

public class DcrFragment extends Fragment implements FilterAppliedListener {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private FragmentDcrBinding binding;
    private TimesheetViewModel dcrViewModel;
    private RecyclerView recyclerViewDcr;
    private DcrAdapter dcrAdapter;

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
            if (dcrDataList != null) {
                String prettyJson = gson.toJson(dcrDataList);
                Log.d("DcrFragment", "Pretty JSON:\n" + prettyJson);
                List<EmployeesDcrDataItem> dcr = parseDcrData(dcrDataList);
                dcrAdapter.updateData(dcr);
//                DcrAdapter dcrAdapter = new DcrAdapter(dcr, this);
                recyclerViewDcr.setAdapter(dcrAdapter);
            }

        });
//        Button filterButton = view.findViewById(R.id.filterButton);
//        filterButton.setOnClickListener(v -> {
//            FilterBottomSheetDialogFragment filterBottomSheetDialogFragment = new FilterBottomSheetDialogFragment();
//            filterBottomSheetDialogFragment.setFilterAppliedListener(this);
//            filterBottomSheetDialogFragment.show(getParentFragmentManager(), filterBottomSheetDialogFragment.getTag());
//        });

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
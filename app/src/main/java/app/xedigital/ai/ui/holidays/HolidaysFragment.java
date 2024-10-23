package app.xedigital.ai.ui.holidays;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import app.xedigital.ai.adapter.HolidayAdapter;
import app.xedigital.ai.databinding.FragmentHolidaysBinding;

import java.util.Collections;

public class HolidaysFragment extends Fragment {

    private FragmentHolidaysBinding binding;
    private HolidayAdapter holidayAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HolidaysViewModel holidaysViewModel = new ViewModelProvider(this).get(HolidaysViewModel.class);

        binding = FragmentHolidaysBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize RecyclerView and adapter ONCE
        holidayAdapter = new HolidayAdapter(Collections.emptyList());
        RecyclerView recyclerView = binding.recyclerViewHolidays;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(holidayAdapter);

        // Observe ViewModel data and UPDATE adapter
        holidaysViewModel.getHolidaysList().observe(getViewLifecycleOwner(), holidays -> holidayAdapter.updateHolidays(holidays));

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
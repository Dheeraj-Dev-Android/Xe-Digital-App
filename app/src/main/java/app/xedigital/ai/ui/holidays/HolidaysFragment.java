package app.xedigital.ai.ui.holidays;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import app.xedigital.ai.adapter.HolidayAdapter;
import app.xedigital.ai.databinding.FragmentHolidaysBinding;

public class HolidaysFragment extends Fragment {

    private FragmentHolidaysBinding binding;
    private HolidayAdapter holidayAdapter;
    private ProgressBar progressBar;
    private LinearLayout textViewEmpty;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        HolidaysViewModel holidaysViewModel = new ViewModelProvider(this).get(HolidaysViewModel.class);

        binding = FragmentHolidaysBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        progressBar = binding.progressBar;
        textViewEmpty = binding.textViewEmpty;

        // Initialize RecyclerView and adapter
        holidayAdapter = new HolidayAdapter();
        binding.recyclerViewHolidays.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewHolidays.setAdapter(holidayAdapter);

        // Observe ViewModel data
        holidaysViewModel.getHolidaysList().observe(getViewLifecycleOwner(), holidays -> {
            if (holidays == null || holidays.isEmpty()) {
                binding.recyclerViewHolidays.setVisibility(View.GONE);
                binding.textViewEmpty.setVisibility(View.VISIBLE);
            } else {
                binding.recyclerViewHolidays.setVisibility(View.VISIBLE);
                binding.textViewEmpty.setVisibility(View.GONE);
                holidayAdapter.updateHolidays(holidays);
            }

            binding.progressBar.setVisibility(View.GONE);
        });
        binding.progressBar.setVisibility(View.VISIBLE);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
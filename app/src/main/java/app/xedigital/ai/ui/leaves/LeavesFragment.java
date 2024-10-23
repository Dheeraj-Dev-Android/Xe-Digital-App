package app.xedigital.ai.ui.leaves;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import app.xedigital.ai.R;
import app.xedigital.ai.databinding.FragmentLeavesBinding;

public class LeavesFragment extends Fragment {

    private FragmentLeavesBinding binding;
    private EditText etFromDate, etToDate;
    private SimpleDateFormat dateFormat;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LeavesViewModel leavesViewModel = new ViewModelProvider(this).get(LeavesViewModel.class);
        binding = FragmentLeavesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();


//        etFromDate = binding.etFromDate;
//        etToDate = binding.etToDate;
        Calendar calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

//        etFromDate.setOnClickListener(view -> showDatePicker(etFromDate));
//        etToDate.setOnClickListener(view -> showDatePicker(etToDate));

//        AutoCompleteTextView leaveTypeSpinner = binding.spinnerLeaveType;
//        AutoCompleteTextView leaveCategorySpinnerfrom = binding.spinnerLeaveCategoryfrom;
//        AutoCompleteTextView leaveCategorySpinnerto = binding.spinnerLeaveCategoryto;
//        AutoCompleteTextView leavingStationSpinner = binding.spinnerLeavingstation;
//
//        TextInputEditText leaveStationAddress = binding.etLeaveStationAddress;
//        TextInputLayout leaveStationAddressLayout = binding.tilLeaveStationAddress;

//        leaveStationAddressLayout.setVisibility(View.GONE);
//
//        leavingStationSpinner.setOnItemClickListener((adapterView, view, position, id) -> {
//            String selectedOption = (String) adapterView.getItemAtPosition(position);
//            if ("Yes".equalsIgnoreCase(selectedOption)) {
//                leaveStationAddressLayout.setVisibility(View.VISIBLE);
//            } else {
//                leaveStationAddressLayout.setVisibility(View.GONE);
//                leaveStationAddress.setText("");
//            }
//        });

//        ArrayAdapter<String> leaveTypeAdapter = new ArrayAdapter<>(requireContext(), R.layout.dropdown_menu_popup_item, getResources().getStringArray(R.array.leave_types));
//        leaveTypeSpinner.setAdapter(leaveTypeAdapter);
//
//        ArrayAdapter<String> leaveCategoriesAdapter = new ArrayAdapter<>(requireContext(), R.layout.dropdown_menu_popup_item, getResources().getStringArray(R.array.leave_categories));
//        leaveCategorySpinnerfrom.setAdapter(leaveCategoriesAdapter);
//        leaveCategorySpinnerto.setAdapter(leaveCategoriesAdapter);
//
//        ArrayAdapter<String> leavingStationsAdapter = new ArrayAdapter<>(requireContext(), R.layout.dropdown_menu_popup_item, getResources().getStringArray(R.array.leaving_stations));
//        leavingStationSpinner.setAdapter(leavingStationsAdapter);

        return root;
    }

    private void showDatePicker(EditText editText) {
        MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
        builder.setTitleText("Select date");
        builder.setSelection(MaterialDatePicker.todayInUtcMilliseconds());
        MaterialDatePicker<Long> picker = builder.build();

        picker.addOnPositiveButtonClickListener(selection -> {
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            calendar.setTimeInMillis(selection);
            String selectedDate = dateFormat.format(calendar.getTime());
            editText.setText(selectedDate);
        });

        picker.show(requireActivity().getSupportFragmentManager(), "datePicker");
    }

}


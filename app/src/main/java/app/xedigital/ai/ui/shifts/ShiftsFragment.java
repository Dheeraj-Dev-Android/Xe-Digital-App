package app.xedigital.ai.ui.shifts;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import app.xedigital.ai.databinding.FragmentShiftsBinding;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShiftsFragment extends Fragment {

    private FragmentShiftsBinding binding;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ShiftsViewModel shiftsViewModel = new ViewModelProvider(this).get(ShiftsViewModel.class);

        binding = FragmentShiftsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();


        TextInputEditText firstNameEditText = binding.firstNameEditText;
        TextInputEditText lastNameEditText = binding.lastNameEditText;
        TextInputEditText contactEditText = binding.contactEditText;
        TextInputEditText emailEditText = binding.emailEditText;
        TextInputEditText hrEmailEditText = binding.hrEmailEditText;
        hrEmailEditText.setText("hr@consultedge.global");
        hrEmailEditText.setEnabled(false);

        AutoCompleteTextView shiftTypeSpinner = binding.shiftTypeSpinner;
        AutoCompleteTextView shiftSpinner = binding.shiftSpinner;

        Button clearButton = binding.clearButton;
        Button submitButton = binding.submitButton;

        String[] shiftTypes = {"General Shift", "24*7 Shift"};
        ArrayAdapter<String> shiftTypeAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, shiftTypes);
        shiftTypeSpinner.setAdapter(shiftTypeAdapter);

        // Define shift options for each shift type
        final Map<String, List<String>> shiftOptions = new HashMap<>();
        shiftOptions.put("General Shift", Arrays.asList("Morning(9:00 AM - 6:00 PM)", "Morning(9:30 AM - 6:30 PM)", "Morning(10:00 AM - 7:00 PM)", "Morning(10:30 AM - 7:30 PM)", "Morning(11:00 AM - 8:00 PM)"));
        shiftOptions.put("24*7 Shift", Arrays.asList("Morning(7:30 AM - 4:30 PM)", "Morning(8:00 AM - 5:00 PM)", "Morning(8:30 AM - 5:30 PM)", "Morning(9:00 AM - 6:00 PM)", "Morning(9:30 AM - 6:30 PM)", "Morning(10:00 AM - 7:00 PM)", "Morning(10:30 AM - 7:30 PM)", "Morning(11:00 AM - 8:00 PM)", "Morning(11:30 AM - 8:30 PM)", "Afternoon(12:00 PM - 9:00 PM)", "Afternoon(12:30 PM - 9:30 PM)", "Afternoon(1:00 PM - 10:00 PM)", "Afternoon(1:30 PM - 10:30 PM)", "Afternoon(2:00 PM - 11:00 PM)", "Afternoon(2:30 PM - 11:30 PM)", "Afternoon(3:00 PM - 12:00 AM)", "Afternoon(3:30 PM - 12:30 AM)", "Afternoon(4:00 PM - 1:00 AM)", "Afternoon(4:30 PM - 1:30 AM)", "Afternoon(5:00 PM - 2:00 AM)", "Afternoon(5:30 PM - 2:30 AM)", "Evening(6:00 PM - 3:00 AM)", "Evening(6:30 PM - 3:30 AM)", "Evening(7:00 PM - 4:00 AM)", "Evening(7:30 PM - 4:30 AM)", "Evening(8:00 PM - 5:00 AM)", "Evening(8:30 PM - 5:30 AM)", "Evening(9:00 PM - 6:00 AM)", "Evening(9:30 PM - 6:30 AM)", "Evening(10:00 PM - 7:00 AM)", "Evening(10:30 PM - 7:30 AM)", "Evening(11:00 PM - 8:00 AM)", "Evening(11:30 PM - 8:30 AM)", "Night(12:00 AM - 9:00 AM)", "Night(12:30 AM - 9:30 AM)", "Night(1:00 AM - 10:00 AM)", "Night(1:30 AM - 10:30 AM)", "Night(2:00 AM - 11:00 AM)", "Night(2:30 AM - 11:30 AM)", "Night(3:00 AM - 12:00 PM)", "Night(3:30 AM - 12:30 PM)", "Night(4:00 AM - 1:00 PM)", "Night(4:30 AM - 1:30 PM)", "Night(5:00 AM - 2:00 PM)", "Night(5:30 AM - 2:30 PM)"));

        // Initial setup for shiftSpinner (using default or empty list)
        ArrayAdapter<String> initialShiftAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, new ArrayList<>());
        shiftSpinner.setAdapter(initialShiftAdapter);

        // Listener for shiftTypeSpinner selection
        shiftTypeSpinner.setOnItemClickListener((parent, view, position, id) -> {
            String selectedShiftType = (String) parent.getItemAtPosition(position);
            List<String> shiftsForSelectedType = shiftOptions.getOrDefault(selectedShiftType, new ArrayList<>());

            ArrayAdapter<String> shiftAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, shiftsForSelectedType);
            shiftSpinner.setAdapter(shiftAdapter);
        });

        clearButton.setOnClickListener(view -> {
            firstNameEditText.setText("");
            lastNameEditText.setText("");
            emailEditText.setText("");
            contactEditText.setText("");
            shiftTypeSpinner.setText("");
            shiftSpinner.setText("");
            Toast.makeText(requireContext(), "Cleared", Toast.LENGTH_SHORT).show();
        });

        submitButton.setOnClickListener(view -> {
            String firstName = firstNameEditText.getText().toString();
            String lastName = lastNameEditText.getText().toString();
            String email = emailEditText.getText().toString();
            String contact = contactEditText.getText().toString();
            String shiftType = shiftTypeSpinner.getText().toString();
            String shift = shiftSpinner.getText().toString();

            shiftsViewModel.processShiftData(firstName, lastName, email, contact, shiftType, shift);
            Toast.makeText(requireContext(), "Submitted", Toast.LENGTH_SHORT).show();
        });


        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


}
package app.xedigital.ai.ui.payroll;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import app.xedigital.ai.databinding.FragmentPayrollBinding;

public class PayrollFragment extends Fragment {

    private FragmentPayrollBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        PayrollViewModel payrollViewModel = new ViewModelProvider(this).get(PayrollViewModel.class);

        binding = FragmentPayrollBinding.inflate(inflater, container, false);

//        final TextView textView = binding.textPayroll;
//        payrollViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;


    }
}
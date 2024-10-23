package app.xedigital.ai.ui.policy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import app.xedigital.ai.databinding.FragmentPolicyBinding;

public class PolicyFragment extends Fragment {

    private FragmentPolicyBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        PolicyViewModel policyViewModel =
                new ViewModelProvider(this).get(PolicyViewModel.class);

        binding = FragmentPolicyBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

//        final TextView textView = binding.textPolicy;
//        policyViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


}
package app.xedigital.ai.ui.claim_management;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import app.xedigital.ai.databinding.FragmentClaimManagementBinding;

public class ClaimManagementFragment extends Fragment {

    private FragmentClaimManagementBinding binding;

    public static ClaimManagementFragment newInstance() {
        return new ClaimManagementFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        ClaimManagementViewModel claimManagementViewModel = new ViewModelProvider(this).get(ClaimManagementViewModel.class);

        binding = FragmentClaimManagementBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

//        final TextView textView = binding.textClaim;
//        claimManagementViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


}
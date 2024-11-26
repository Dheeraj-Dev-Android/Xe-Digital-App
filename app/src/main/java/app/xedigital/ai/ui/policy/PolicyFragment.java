package app.xedigital.ai.ui.policy;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.List;

import app.xedigital.ai.adapter.PolicyAdapter;
import app.xedigital.ai.databinding.FragmentPolicyBinding;
import app.xedigital.ai.model.policy.PoliciesItem;
import app.xedigital.ai.model.policy.PolicyResponse;

public class PolicyFragment extends Fragment {

    private FragmentPolicyBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        PolicyViewModel policyViewModel = new ViewModelProvider(this).get(PolicyViewModel.class);
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String authToken = sharedPreferences.getString("authToken", "");
        String authTokenHeader = "jwt " + authToken;
        policyViewModel.fetchPolicies(authTokenHeader);

        policyViewModel.getPolicyData().observe(getViewLifecycleOwner(), new Observer<PolicyResponse>() {
            @Override
            public void onChanged(PolicyResponse policyResponse) {
                List<PoliciesItem> policies = policyResponse.getData().getPolicies();
                PolicyAdapter adapter = new PolicyAdapter(policies);
                binding.policyRecyclerView.setAdapter(adapter);
                binding.policyRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            }
        });

        binding = FragmentPolicyBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        return view;

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


}
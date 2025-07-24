package app.xedigital.ai.adminUI.allUsers;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

import app.xedigital.ai.R;
import app.xedigital.ai.adminAdapter.AllUsersAdapter;
import app.xedigital.ai.model.Admin.AdminUsers.UsersItem;

public class AllUsersFragment extends Fragment {

    private AllUsersViewModel mViewModel;

    private List<UsersItem> allUsers = new ArrayList<>();
    private List<UsersItem> filteredUsers = new ArrayList<>();
    private String selectedStatus = "All";

    private void applyFilters(String searchText, String status) {
        filteredUsers.clear();
        for (UsersItem user : allUsers) {
            boolean matchesSearch = user.getFirstname().toLowerCase().contains(searchText.toLowerCase()) ||
                    user.getLastname().toLowerCase().contains(searchText.toLowerCase());

            boolean matchesStatus = status.equals("All") ||
                    (status.equals("Active") && user.isActive()) ||
                    (status.equals("Inactive") && !user.isActive());

            if (matchesSearch && matchesStatus) {
                filteredUsers.add(user);
            }
        }

        // Notify adapter
        RecyclerView recyclerView = requireView().findViewById(R.id.allUserRecyclerView);
        ((AllUsersAdapter) recyclerView.getAdapter()).updateData(filteredUsers);
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_all_users, container, false);
    }

//    @Override
//    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//
//        mViewModel = new ViewModelProvider(this).get(AllUsersViewModel.class);
//        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("AdminCred", Context.MODE_PRIVATE);
//        String token = sharedPreferences.getString("authToken", "");
//        String authToken = "jwt " + token;
//
//        mViewModel.fetchAllUsers(authToken);
//
//        RecyclerView recyclerView = view.findViewById(R.id.allUserRecyclerView);
//        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
//
//        AllUsersAdapter adapter = new AllUsersAdapter(new ArrayList<>(), user -> {
//            // Handle user click
//            Toast.makeText(getContext(), "Clicked: " + user.getEmail(), Toast.LENGTH_SHORT).show();
//        });
//
//        recyclerView.setAdapter(adapter);
//
//        mViewModel.getUserResponse().observe(getViewLifecycleOwner(), response -> {
//            if (response != null && response.getData() != null) {
//                adapter.updateData(response.getData().getUsers());
//            }
//        });
//
//
//        mViewModel.getLoadingStatus().observe(getViewLifecycleOwner(), isLoading -> {
//            // Show/hide loading spinner
//        });
//
//        mViewModel.getError().observe(getViewLifecycleOwner(), errorMsg -> {
//            // Show error message
//            Toast.makeText(getContext(), errorMsg, Toast.LENGTH_SHORT).show();
//        });
//    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mViewModel = new ViewModelProvider(this).get(AllUsersViewModel.class);
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("AdminCred", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("authToken", "");
        String authToken = "jwt " + token;

        RecyclerView recyclerView = view.findViewById(R.id.allUserRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        AllUsersAdapter adapter = new AllUsersAdapter(new ArrayList<>(), user -> {
            Toast.makeText(getContext(), "Clicked: " + user.getEmail(), Toast.LENGTH_SHORT).show();
        });
        recyclerView.setAdapter(adapter);

        // Observe user data
        mViewModel.getUserResponse().observe(getViewLifecycleOwner(), response -> {
            if (response != null && response.getData() != null) {
                allUsers = response.getData().getUsers(); // keep all data here
                filteredUsers = new ArrayList<>(allUsers); // initial copy
                adapter.updateData(filteredUsers);
            }
        });

        // Fetch from API
        mViewModel.fetchAllUsers(authToken);

        // Search Box
        EditText searchEditText = view.findViewById(R.id.etSearchEmployee);
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters(searchEditText.getText().toString(), selectedStatus);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // ChipGroup filter
        ChipGroup chipGroup = view.findViewById(R.id.chipGroupFilter);
        chipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipAll) {
                selectedStatus = "All";
            } else if (checkedId == R.id.chipActive) {
                selectedStatus = "Active";
            } else if (checkedId == R.id.chipInactive) {
                selectedStatus = "Inactive";
            }
            applyFilters(searchEditText.getText().toString(), selectedStatus);
        });
    }

}

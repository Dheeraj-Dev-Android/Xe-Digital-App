package app.xedigital.ai.adminUI.partners;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import app.xedigital.ai.R;
import app.xedigital.ai.adminAdapter.PartnersAdapter;
import app.xedigital.ai.adminApi.AdminAPIClient;
import app.xedigital.ai.adminApi.AdminAPIInterface;
import app.xedigital.ai.model.Admin.partners.PartnersItem;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PartnersFragment extends Fragment {

    private PartnersViewModel partnersViewModel;
    private RecyclerView recyclerView;
    private PartnersAdapter adapter;
    private String authToken;
    private View emptyStateContainer;
    private SwipeRefreshLayout swipeRefreshLayout;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_partners, container, false);
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("AdminCred", Context.MODE_PRIVATE);
        authToken = sharedPreferences.getString("authToken", "");
        recyclerView = view.findViewById(R.id.partnersRecyclerView);
        emptyStateContainer = view.findViewById(R.id.emptyStateContainer);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            String token = "jwt " + authToken;
            partnersViewModel.fetchPartners(token);
        });


        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);

        partnersViewModel = new ViewModelProvider(this).get(PartnersViewModel.class);

        partnersViewModel.getPartnersList().observe(getViewLifecycleOwner(), this::updateUI);

        String token = "jwt " + authToken;
        partnersViewModel.fetchPartners(token);
    }


    private void updateUI(List<PartnersItem> partners) {
        swipeRefreshLayout.setRefreshing(false);

        if (partners != null && !partners.isEmpty()) {
            emptyStateContainer.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            if (adapter == null) {
                adapter = new PartnersAdapter(getContext(), partners, () -> {
                    String token = "jwt " + authToken;
                    partnersViewModel.fetchPartners(token);
                });
                recyclerView.setAdapter(adapter);
            } else {
                adapter.setData(partners);
            }


        } else {
            recyclerView.setVisibility(View.GONE);
            emptyStateContainer.setVisibility(View.VISIBLE);
        }
    }

    private void showAddPartnerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = LayoutInflater.from(requireContext());

        // Inflate the full form layout
        View dialogView = inflater.inflate(R.layout.edit_partner_form, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.show();
        TextView title = dialogView.findViewById(R.id.titleEditPartner);
        title.setText("Add New Partner");

        // Reference form fields
        EditText nameInput = dialogView.findViewById(R.id.editName);
        EditText emailInput = dialogView.findViewById(R.id.editEmail);
        EditText contactInput = dialogView.findViewById(R.id.editContact);
        EditText addressInput = dialogView.findViewById(R.id.editAddress);
        EditText cityInput = dialogView.findViewById(R.id.editCity);
        EditText stateInput = dialogView.findViewById(R.id.editState);
        EditText zipInput = dialogView.findViewById(R.id.editZip);
        EditText websiteInput = dialogView.findViewById(R.id.editWebsite);
        Spinner statusSpinner = dialogView.findViewById(R.id.statusSpinner);

        Button submitButton = dialogView.findViewById(R.id.btnSubmit);
        Button clearButton = dialogView.findViewById(R.id.btnClear);

        TextView nameLabel = dialogView.findViewById(R.id.nameLabel);
        TextView emailLabel = dialogView.findViewById(R.id.emailLabel);
        TextView contactLabel = dialogView.findViewById(R.id.contactLabel);

        // Set labels with red asterisk
        nameLabel.setText(Html.fromHtml("Name <font color='#FF0000'>*</font>"));
        emailLabel.setText(Html.fromHtml("Email <font color='#FF0000'>*</font>"));
        contactLabel.setText(Html.fromHtml("Contact <font color='#FF0000'>*</font>"));

        // Submit Button Click
        submitButton.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String contact = contactInput.getText().toString().trim();
            String address = addressInput.getText().toString().trim();
            String city = cityInput.getText().toString().trim();
            String state = stateInput.getText().toString().trim();
            String zip = zipInput.getText().toString().trim();
            String website = websiteInput.getText().toString().trim();
            String status = statusSpinner.getSelectedItem().toString();


            // === FORM VALIDATION ===
            if (name.isEmpty()) {
                nameInput.setError("Name is required");
                nameInput.requestFocus();
                return;
            }

            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailInput.setError("Valid email required");
                emailInput.requestFocus();
                return;
            }

            if (contact.isEmpty() || contact.length() < 10) {
                contactInput.setError("Valid contact number required");
                contactInput.requestFocus();
                return;
            }

            if (address.isEmpty()) {
                addressInput.setError("Address is required");
                addressInput.requestFocus();
                return;
            }

            if (city.isEmpty()) {
                cityInput.setError("City is required");
                cityInput.requestFocus();
                return;
            }

            if (state.isEmpty()) {
                stateInput.setError("State is required");
                stateInput.requestFocus();
                return;
            }

            if (zip.isEmpty() || zip.length() != 6) {
                zipInput.setError("Valid 6-digit zip code required");
                zipInput.requestFocus();
                return;
            }

            if (!website.isEmpty() && !Patterns.WEB_URL.matcher(website).matches()) {
                websiteInput.setError("Enter a valid website URL");
                websiteInput.requestFocus();
                return;
            }

            // === All validations passed ===
            Toast.makeText(requireContext(), "Partner added: " + name, Toast.LENGTH_SHORT).show();

            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("name", name);
                jsonObject.put("email", email);
                jsonObject.put("contact", contact);
                jsonObject.put("address", address);
                jsonObject.put("city", city);
                jsonObject.put("state", state);
                jsonObject.put("zip", zip);
                jsonObject.put("website", website.isEmpty() ? JSONObject.NULL : website);
                jsonObject.put("company", JSONObject.NULL);
                jsonObject.put("active", status.equalsIgnoreCase("Active") ? "true" : "false");

                RequestBody requestBody = RequestBody.create(jsonObject.toString(), okhttp3.MediaType.parse("application/json; charset=utf-8"));

                String token = "jwt " + authToken;


                AdminAPIInterface apiService = AdminAPIClient.getInstance().getBase2();
                Call<ResponseBody> call = apiService.addPartner(token, requestBody);

                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(requireContext(), "Partner added successfully!", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();

                            // Refresh list
                            partnersViewModel.fetchPartners(token);
                        } else {
                            Toast.makeText(requireContext(), "Failed to add partner", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                        Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(requireContext(), "JSON error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            dialog.dismiss();
        });

        // Clear Button Click
        clearButton.setOnClickListener(v -> {
            nameInput.setText("");
            emailInput.setText("");
            contactInput.setText("");
            addressInput.setText("");
            cityInput.setText("");
            stateInput.setText("");
            zipInput.setText("");
            websiteInput.setText("");
            statusSpinner.setSelection(0);
        });
    }


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_add_partner, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_add_newPartner) {
            showAddPartnerDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}

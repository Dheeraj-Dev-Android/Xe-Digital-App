package app.xedigital.ai.adminAdapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.imageview.ShapeableImageView;

import org.json.JSONObject;

import java.util.List;

import app.xedigital.ai.R;
import app.xedigital.ai.adminApi.AdminAPIClient;
import app.xedigital.ai.adminApi.AdminAPIInterface;
import app.xedigital.ai.model.Admin.partners.PartnersItem;
import app.xedigital.ai.utills.DateTimeUtils;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;

public class PartnersAdapter extends RecyclerView.Adapter<PartnersAdapter.ViewHolder> {

    private final Context context;
    private final OnPartnerUpdatedListener updateListener;
    private List<PartnersItem> partnersList;

    public PartnersAdapter(Context context, List<PartnersItem> partners, OnPartnerUpdatedListener listener) {
        this.context = context;
        this.partnersList = partners;
        this.updateListener = listener;
    }

    public void setData(List<PartnersItem> newPartners) {
        this.partnersList = newPartners;
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public PartnersAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_partner_card, parent, false);
        return new ViewHolder(view);
    }

    private String getInitials(String name) {
        if (name == null || name.trim().isEmpty()) return "?";
        String[] words = name.trim().split(" ");
        if (words.length == 1) {
            return words[0].substring(0, 1).toUpperCase();
        } else {
            return (words[0].substring(0, 1) + words[1].substring(0, 1)).toUpperCase();
        }
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        PartnersItem partner = partnersList.get(position);

        holder.name.setText(partner.getName());
        holder.email.setText(partner.getEmail());
        holder.contact.setText(partner.getContact());
        holder.address.setText(partner.getAddress() + ", " + partner.getCity() + ", " + partner.getState());
        holder.website.setText(partner.getWebsite());
        holder.createdDate.setText(DateTimeUtils.getDayOfWeekAndDate(partner.getCreatedAt()));
        String name = partner.getName();
        holder.avatarText.setText(getInitials(name));


        boolean isActive = partner.isActive();
        String statusText = isActive ? "Active" : "Deactive";
        int chipBackgroundColorResourceId = isActive ? R.color.success : R.color.error;

        holder.status.setText(statusText);
        holder.status.setChipBackgroundColorResource(chipBackgroundColorResourceId);

        holder.edit.setOnClickListener(v -> {
            // Inflate the layout
            LayoutInflater inflater = LayoutInflater.from(context);
            View editView = inflater.inflate(R.layout.edit_partner_form, null);

            // Initialize views from the layout
            Spinner statusSpinner = editView.findViewById(R.id.statusSpinner);
            EditText nameEdit = editView.findViewById(R.id.editName);
            EditText emailEdit = editView.findViewById(R.id.editEmail);
            EditText contactEdit = editView.findViewById(R.id.editContact);
            EditText addressEdit = editView.findViewById(R.id.editAddress);
            EditText cityEdit = editView.findViewById(R.id.editCity);
            EditText stateEdit = editView.findViewById(R.id.editState);
            EditText zipEdit = editView.findViewById(R.id.editZip);
            EditText websiteEdit = editView.findViewById(R.id.editWebsite);
            Button submitButton = editView.findViewById(R.id.btnSubmit);
            Button clearButton = editView.findViewById(R.id.btnClear);


            // Fill data from partner
            nameEdit.setText(partner.getName());
            emailEdit.setText(partner.getEmail());
            contactEdit.setText(partner.getContact());
            addressEdit.setText(partner.getAddress());
            cityEdit.setText(partner.getCity());
            stateEdit.setText(partner.getState());
            zipEdit.setText(partner.getZip());
            websiteEdit.setText(partner.getWebsite());

            // Set status spinner value
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context, R.array.status_options, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            statusSpinner.setAdapter(adapter);
            if (partner.isActive()) {
                statusSpinner.setSelection(1);
            } else {
                statusSpinner.setSelection(2);
            }

            // Setup AlertDialog
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setView(editView);
            AlertDialog dialog = builder.create();
            dialog.show();

            // Submit button action
            submitButton.setOnClickListener(view -> {
                String updatedName = nameEdit.getText().toString();
                String updatedEmail = emailEdit.getText().toString();
                String updatedContact = contactEdit.getText().toString();
                String updatedAddress = addressEdit.getText().toString();
                String updatedCity = cityEdit.getText().toString();
                String updatedState = stateEdit.getText().toString();
                String updatedZip = zipEdit.getText().toString();
                String updatedWebsite = websiteEdit.getText().toString();
                boolean updateStatus = statusSpinner.getSelectedItem().toString().equalsIgnoreCase("Active");

                // Construct JSON
                JSONObject json = new JSONObject();
                try {
                    json.put("name", updatedName);
                    json.put("email", updatedEmail);
                    json.put("contact", updatedContact);
                    json.put("address", updatedAddress);
                    json.put("city", updatedCity);
                    json.put("state", updatedState);
                    json.put("zip", updatedZip);
                    json.put("website", updatedWebsite);
                    json.put("company", partner.getCompany());
                    json.put("active", String.valueOf(updateStatus));
                    json.put("createdAt", partner.getCreatedAt());
                } catch (Exception e) {
                    Toast.makeText(context, "Failed to build request: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }

                // Convert to RequestBody
                RequestBody requestBody = RequestBody.create(json.toString(), okhttp3.MediaType.parse("application/json"));

                SharedPreferences sharedPreferences = context.getSharedPreferences("AdminCred", Context.MODE_PRIVATE);
                String token = sharedPreferences.getString("authToken", "");
                String authToken = "jwt " + token;
                String partnerId = partner.getId();

                AdminAPIInterface apiService = AdminAPIClient.getInstance().getBase2();
                Call<ResponseBody> call = apiService.updatePartner(authToken, partnerId, requestBody);

                call.enqueue(new retrofit2.Callback<ResponseBody>() {
                    @Override
                    public void onResponse(@NonNull Call<ResponseBody> call, @NonNull retrofit2.Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(context, "Partner updated successfully!", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();

                            if (updateListener != null) {
                                updateListener.onPartnerUpdated();
                            }
                        } else {
                            Toast.makeText(context, "Update failed: " + response.code(), Toast.LENGTH_LONG).show();
                        }

                    }

                    @Override
                    public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                        Toast.makeText(context, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            });


            clearButton.setOnClickListener(view -> {
                nameEdit.setText("");
                emailEdit.setText("");
                contactEdit.setText("");
                addressEdit.setText("");
                cityEdit.setText("");
                stateEdit.setText("");
                zipEdit.setText("");
                websiteEdit.setText("");
                statusSpinner.setSelection(0);
            });
        });

    }

    @Override
    public int getItemCount() {
        return partnersList.size();
    }

    public interface OnPartnerUpdatedListener {
        void onPartnerUpdated();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, email;
        TextView contact, address, website, createdDate;
        Chip status;
        CardView cardView;
        ShapeableImageView edit;
        TextView avatarText;


        public ViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.partnerName);
            email = view.findViewById(R.id.partnerEmail);
            contact = view.findViewById(R.id.partnerContact);
            address = view.findViewById(R.id.partnerAddress);
            website = view.findViewById(R.id.partnerWebsite);
            status = view.findViewById(R.id.partnerStatus);
            createdDate = view.findViewById(R.id.partnerCreatedDate);
            edit = view.findViewById(R.id.btnEdit);
            avatarText = view.findViewById(R.id.avatarText);


            cardView = view.findViewById(R.id.partnerCard);
        }
    }
}

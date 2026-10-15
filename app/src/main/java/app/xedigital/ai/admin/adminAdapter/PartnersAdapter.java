package app.xedigital.ai.admin.adminAdapter;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
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
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.imageview.ShapeableImageView;

import org.json.JSONObject;

import java.util.List;

import app.xedigital.ai.R;
import app.xedigital.ai.admin.adminAPI.AdminAPIClient;
import app.xedigital.ai.admin.adminAPI.AdminAPIInterface;
import app.xedigital.ai.admin.adminModal.partners.PartnersItem;
import app.xedigital.ai.utills.DateTimeUtils;
import app.xedigital.ai.utills.SecurePrefManager;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;

public class PartnersAdapter extends RecyclerView.Adapter<PartnersAdapter.ViewHolder> {

    private static final int[] AVATAR_COLORS = {
            0xFF1E88E5, // Blue
            0xFF43A047, // Green
            0xFFE53935, // Red
            0xFF8E24AA, // Purple
            0xFFFB8C00, // Orange
            0xFF00ACC1, // Cyan
            0xFF3949AB, // Indigo
            0xFFD81B60, // Pink
            0xFF00897B, // Teal
            0xFF6D4C41  // Brown
    };

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
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_partner_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        PartnersItem partner = partnersList.get(position);

        // Safe Name Resolution
        String name = partner.getName();
        String displayName = (name != null && !name.trim().isEmpty()) ? name.trim() : "N/A";
        holder.name.setText(displayName);

        // Safe Email Resolution
        String email = partner.getEmail();
        holder.email.setText((email != null && !email.trim().isEmpty()) ? email : "N/A");

        // Safe Contact Resolution
        String contact = partner.getContact();
        holder.contact.setText((contact != null && !contact.trim().isEmpty()) ? contact : "N/A");

        // Safe Address Resolution (combine address, city, state safely)
        String addressText = buildAddress(partner.getAddress(), partner.getCity(), partner.getState());
        holder.address.setText(addressText);

        // Safe Website Resolution
        String website = partner.getWebsite();
        holder.website.setText((website != null && !website.trim().isEmpty()) ? website : "N/A");

        // Safe Created Date Resolution
        String createdAt = partner.getCreatedAt();
        if (createdAt != null && !createdAt.trim().isEmpty()) {
            holder.createdDate.setText(DateTimeUtils.getDayOfWeekAndDate(createdAt));
        } else {
            holder.createdDate.setText("N/A");
        }

        // Dynamic Initials with Colored Background
        String initials = getInitials(displayName);
        int avatarColor = getAvatarColor(displayName);
        holder.avatarText.setText(initials);

        GradientDrawable circularBg = new GradientDrawable();
        circularBg.setShape(GradientDrawable.OVAL);
        circularBg.setColor(avatarColor);
        holder.avatarText.setBackground(circularBg);

        // Safe Status Resolution
        boolean isActive = partner.isActive();
        String statusText = isActive ? "Active" : "Deactive";
        int chipBackgroundColorResourceId = isActive ? R.color.success : R.color.error;
        holder.status.setText(statusText);
        holder.status.setChipBackgroundColorResource(chipBackgroundColorResourceId);

        // Edit Button Listener (existing logic preserved)
        holder.edit.setOnClickListener(v -> {
            LayoutInflater inflater = LayoutInflater.from(context);
            View editView = inflater.inflate(R.layout.edit_partner_form, null);

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

            nameEdit.setText(partner.getName());
            emailEdit.setText(partner.getEmail());
            contactEdit.setText(partner.getContact());
            addressEdit.setText(partner.getAddress());
            cityEdit.setText(partner.getCity());
            stateEdit.setText(partner.getState());
            zipEdit.setText(partner.getZip());
            websiteEdit.setText(partner.getWebsite());

            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context, R.array.status_options, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            statusSpinner.setAdapter(adapter);
            if (partner.isActive()) {
                statusSpinner.setSelection(1);
            } else {
                statusSpinner.setSelection(2);
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setView(editView);
            AlertDialog dialog = builder.create();
            dialog.show();

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

                RequestBody requestBody = RequestBody.create(json.toString(), okhttp3.MediaType.parse("application/json"));

                SecurePrefManager sharedPreferences = SecurePrefManager.getInstance(context);
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
        return partnersList != null ? partnersList.size() : 0;
    }

    // Helper: Build safe address string from parts
    private String buildAddress(String address, String city, String state) {
        StringBuilder sb = new StringBuilder();
        if (address != null && !address.trim().isEmpty()) {
            sb.append(address.trim());
        }
        if (city != null && !city.trim().isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(city.trim());
        }
        if (state != null && !state.trim().isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(state.trim());
        }
        return sb.length() > 0 ? sb.toString() : "N/A";
    }

    // Helper: Extract 1 or 2 uppercase initials from a name
    private String getInitials(String name) {
        if (name == null || name.trim().isEmpty() || name.equalsIgnoreCase("N/A")) {
            return "?";
        }
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }
        return String.valueOf((parts[0].charAt(0) + parts[parts.length - 1].charAt(0)));
    }

    // Helper: Returns a consistent color from the palette based on the name's hash
    private int getAvatarColor(String name) {
        if (name == null || name.trim().isEmpty()) {
            return AVATAR_COLORS[0];
        }
        int hash = Math.abs(name.trim().hashCode());
        return AVATAR_COLORS[hash % AVATAR_COLORS.length];
    }

    public interface OnPartnerUpdatedListener {
        void onPartnerUpdated();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, email, contact, address, website, createdDate, avatarText;
        Chip status;
        ShapeableImageView edit;

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
        }
    }
}
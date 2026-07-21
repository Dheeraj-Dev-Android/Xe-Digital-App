package app.xedigital.ai.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import app.xedigital.ai.R;
import app.xedigital.ai.model.employeeClaim.EmployeeClaimdataItem;

public class ClaimsAdapter extends RecyclerView.Adapter<ClaimsAdapter.ClaimViewHolder> {

    private List<EmployeeClaimdataItem> claimList;
    private OnClaimClickListener listener;

    public ClaimsAdapter(List<EmployeeClaimdataItem> claimList) {
        this.claimList = claimList;
    }

    @NonNull
    @Override
    public ClaimViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.claim_item, parent, false);
        return new ClaimViewHolder(view, parent.getContext());
    }

    @Override
    public void onBindViewHolder(@NonNull ClaimViewHolder holder, int position) {
        EmployeeClaimdataItem claim = claimList.get(position);
        holder.bind(claim, listener);
    }

    @Override
    public int getItemCount() {
        int count = claimList != null ? claimList.size() : 0;
        Log.d("ADAPTER_DEBUG", "getItemCount called: " + count);
        return count;
    }
    public void updateData(List<EmployeeClaimdataItem> filteredList) {
        this.claimList = filteredList;
        notifyDataSetChanged();
    }

    public void setOnClaimClickListener(OnClaimClickListener listener) {
        this.listener = listener;
    }

    public interface OnClaimClickListener {
        void onClaimClick(EmployeeClaimdataItem claim);
    }

    static class ClaimViewHolder extends RecyclerView.ViewHolder {
        private final Context context;
        private final TextView txtClaimId, txtProjectName, txtMeetingType, txtPurposeOfMeeting, txtTotalAmount, txtAppliedDate;
        private final TextView statusText;
        private final ShapeableImageView viewDetailsButton;

        public ClaimViewHolder(@NonNull View itemView, Context context) {
            super(itemView);
            this.context = context;

            txtClaimId = itemView.findViewById(R.id.claimIdText);
            txtProjectName = itemView.findViewById(R.id.projectNameText);
            txtMeetingType = itemView.findViewById(R.id.meetingTypeText);
            txtPurposeOfMeeting = itemView.findViewById(R.id.purposeText);
            txtTotalAmount = itemView.findViewById(R.id.amountText);
            txtAppliedDate = itemView.findViewById(R.id.claimDateText);
            viewDetailsButton = itemView.findViewById(R.id.viewDetailsButton);
            statusText = itemView.findViewById(R.id.statusText);
        }

        public void bind(final EmployeeClaimdataItem claim, final OnClaimClickListener listener) {
            if (claim == null) return;
            java.util.function.Function<String, String> sanitize = value ->
                    (value == null || value.trim().isEmpty() || value.equalsIgnoreCase("null")) ? "N/A" : value.trim();

            String claimId = sanitize.apply(claim.getClaimId());
            txtClaimId.setText("Claim ID: " + claimId);
            txtProjectName.setText(sanitize.apply(claim.getProject()));
            txtMeetingType.setText(sanitize.apply(claim.getMeeting()));
            txtPurposeOfMeeting.setText(sanitize.apply(claim.getPerposeofmeet()));
            String currency = claim.getCurrency() != null && !claim.getCurrency().equalsIgnoreCase("null") ? claim.getCurrency().trim() : "";
            double rawAmount = claim.getTotalamount();

            if (rawAmount <= 0) {
                txtTotalAmount.setText("N/A");
            } else {
                String amountStr = String.valueOf(rawAmount);
                txtTotalAmount.setText(currency.isEmpty() ? amountStr : currency + " " + amountStr);
            }

            // 6. Claim Date formatter
            String rawDate = claim.getClaimDate();
            if (rawDate == null || rawDate.trim().isEmpty() || rawDate.equalsIgnoreCase("null")) {
                txtAppliedDate.setText("N/A");
            } else {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                try {
                    Date date = inputFormat.parse(rawDate);
                    txtAppliedDate.setText(outputFormat.format(date));
                } catch (Exception e) {
                    txtAppliedDate.setText(sanitize.apply(rawDate));
                }
            }

            // 7. Complete Status Text Parsing Flow
            String status = claim.getStatusRm() != null ? claim.getStatusRm() : (claim.getStatusHr() != null ? claim.getStatusHr() : "Pending");
            status = sanitize.apply(status);
            statusText.setText(status);

            // Dynamically manage status background tinting
            Drawable background = statusText.getBackground();
            if (background instanceof GradientDrawable) {
                GradientDrawable gradientDrawable = (GradientDrawable) background;
                int backgroundColor;
                if (status.equalsIgnoreCase("Unapproved") || status.equalsIgnoreCase("Pending") || status.equalsIgnoreCase("N/A")) {
                    backgroundColor = ContextCompat.getColor(context, R.color.pending_status_color);
                } else if (status.equalsIgnoreCase("Approved")) {
                    backgroundColor = ContextCompat.getColor(context, R.color.approved_status_color);
                } else if (status.equalsIgnoreCase("Rejected") || status.equalsIgnoreCase("Cancelled")) {
                    backgroundColor = ContextCompat.getColor(context, R.color.rejected_status_color);
                } else {
                    backgroundColor = ContextCompat.getColor(context, R.color.default_status_color);
                }
                gradientDrawable.setColor(backgroundColor);
            }

            // 9. View Details Event Callback Click Binding
            viewDetailsButton.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onClaimClick(claim);
                }
            });
        }
    }
}
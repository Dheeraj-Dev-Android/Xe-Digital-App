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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import app.xedigital.ai.R;
import app.xedigital.ai.model.employeeClaim.EmployeeClaimdataItem;

public class ClaimsAdapter extends RecyclerView.Adapter<ClaimsAdapter.ClaimViewHolder> {

    private static List<EmployeeClaimdataItem> claimList;
    private OnClaimClickListener listener;

    public ClaimsAdapter(List<EmployeeClaimdataItem> claimList) {
        this.claimList = claimList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ClaimViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.claim_item, parent, false);
        Context context = parent.getContext();
        return new ClaimViewHolder(view, context, listener, claimList);
    }

    @Override
    public void onBindViewHolder(@NonNull ClaimViewHolder holder, int position) {
        EmployeeClaimdataItem claim = claimList.get(position);
        holder.bind(claim, listener);
    }

    @Override
    public int getItemCount() {
        return claimList.size();
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

    class ClaimViewHolder extends RecyclerView.ViewHolder {
        TextView txtClaimId, txtProjectName, txtMeetingType, txtPurposeOfMeeting, txtTotalAmount, txtAppliedDate;
        TextView viewDetailsButton, actionButton;
        private Context context;
        private OnClaimClickListener listener;
        private List<EmployeeClaimdataItem> claimList;

        public ClaimViewHolder(@NonNull View itemView, Context context, OnClaimClickListener listener, List<EmployeeClaimdataItem> claimList) {
            super(itemView);
            this.context = context;
            this.listener = listener;
            this.claimList = claimList;

            txtClaimId = itemView.findViewById(R.id.claimIdText);
            txtProjectName = itemView.findViewById(R.id.projectNameText);
            txtMeetingType = itemView.findViewById(R.id.meetingTypeText);
            txtPurposeOfMeeting = itemView.findViewById(R.id.purposeText);
            txtTotalAmount = itemView.findViewById(R.id.amountText);
            txtAppliedDate = itemView.findViewById(R.id.claimDateText);
            viewDetailsButton = itemView.findViewById(R.id.viewDetailsButton);
            actionButton = itemView.findViewById(R.id.actionButton);
            actionButton.setVisibility(View.GONE);

            viewDetailsButton.setOnClickListener(v -> {
                Log.d("ClaimViewHolder", "View Details button clicked");
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onClaimClick(claimList.get(position));
                }
            });
        }


        public void bind(EmployeeClaimdataItem claim, OnClaimClickListener listener) {
            txtClaimId.setText("Claim ID : " + claim.getClaimId());
            txtProjectName.setText("Project Name : " + claim.getProject());
            txtMeetingType.setText("Meeting Type : " + claim.getMeeting());
            txtPurposeOfMeeting.setText("Purpose : " + claim.getPerposeofmeet());
            txtTotalAmount.setText("Amount : " + claim.getCurrency() + " " + claim.getTotalamount());
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            try {
                Date date = inputFormat.parse(claim.getClaimDate());
                txtAppliedDate.setText("Claim Date : " + outputFormat.format(date));
            } catch (Exception e) {
                // Handle parsing error
                txtAppliedDate.setText("Claim date : " + claim.getClaimDate());
            }

            TextView statusText = itemView.findViewById(R.id.statusText);
            String status = claim.getStatusRm() != null ? claim.getStatusRm() : (claim.getStatusHr() != null ? claim.getStatusHr() : "Pending");
            statusText.setText(status);

            Drawable background = statusText.getBackground();
            if (background instanceof GradientDrawable) {
                GradientDrawable gradientDrawable = (GradientDrawable) background;
                int backgroundColor;
                if (status.equalsIgnoreCase("Unapproved")) {
                    backgroundColor = ContextCompat.getColor(context, R.color.pending_status_color);
                } else if (status.equalsIgnoreCase("Pending")) {
                    backgroundColor = ContextCompat.getColor(context, R.color.pending_status_color);
                } else if (status.equalsIgnoreCase("Approved")) {
                    backgroundColor = ContextCompat.getColor(context, R.color.approved_status_color);
                } else if (status.equalsIgnoreCase("Rejected")) {
                    backgroundColor = ContextCompat.getColor(context, R.color.rejected_status_color);
                } else if (status.equalsIgnoreCase("Cancelled")) {
                    backgroundColor = ContextCompat.getColor(context, R.color.rejected_status_color);
                } else {
                    backgroundColor = ContextCompat.getColor(context, R.color.default_status_color);
                }

                gradientDrawable.setColor(backgroundColor);
            }

            actionButton.setVisibility(View.GONE);

            viewDetailsButton.setOnClickListener(v -> {
                listener.onClaimClick(claim);
            });
        }

    }
}

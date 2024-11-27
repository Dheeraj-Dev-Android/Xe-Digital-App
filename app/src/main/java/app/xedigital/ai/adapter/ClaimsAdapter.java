package app.xedigital.ai.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import app.xedigital.ai.R;
import app.xedigital.ai.model.employeeClaim.EmployeeClaimdataItem;

public class ClaimsAdapter extends RecyclerView.Adapter<ClaimsAdapter.ClaimViewHolder> {

    private List<EmployeeClaimdataItem> claimList;

    public ClaimsAdapter(List<EmployeeClaimdataItem> claimList) {
        this.claimList = claimList;
    }

    @NonNull
    @Override
    public ClaimViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.claim_item_layout, parent, false);
        return new ClaimViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClaimViewHolder holder, int position) {
        EmployeeClaimdataItem claim = claimList.get(position);
        holder.txtClaimId.setText("Claim ID: " + claim.getClaimId());
        holder.txtProjectName.setText("Project Name: " + claim.getProject());
        holder.txtMeetingType.setText("Meeting Type: " + claim.getProject());
    }

    @Override
    public int getItemCount() {
        return claimList.size();
    }

    public void updateData(List<EmployeeClaimdataItem> filteredList) {
        this.claimList = filteredList;
        notifyDataSetChanged();
    }

    static class ClaimViewHolder extends RecyclerView.ViewHolder {
        TextView txtClaimId, txtProjectName, txtMeetingType, txtPurposeOfMeeting,
                txtTravelCategory, txtModeOfTransport, txtFromTo, txtDistance,
                txtTotalAmount, txtComment, txtAppliedDate, txtStatus, txtStatusDetails;


        public ClaimViewHolder(@NonNull View itemView) {
            super(itemView);
            txtClaimId = itemView.findViewById(R.id.txtClaimId);
            txtProjectName = itemView.findViewById(R.id.txtProjectName);
            txtMeetingType = itemView.findViewById(R.id.txtMeetingType);
            txtPurposeOfMeeting = itemView.findViewById(R.id.txtPurposeOfMeeting);
            txtTravelCategory = itemView.findViewById(R.id.txtTravelCategory);
            txtModeOfTransport = itemView.findViewById(R.id.txtModeOfTransport);
            txtFromTo = itemView.findViewById(R.id.txtFromTo);
            txtDistance = itemView.findViewById(R.id.txtDistance);
            txtTotalAmount = itemView.findViewById(R.id.txtTotalAmount);
            txtComment = itemView.findViewById(R.id.txtComment);
            txtAppliedDate = itemView.findViewById(R.id.txtAppliedDate);
            txtStatus = itemView.findViewById(R.id.txtStatus);
            txtStatusDetails = itemView.findViewById(R.id.txtStatusDetails);


        }
    }
}
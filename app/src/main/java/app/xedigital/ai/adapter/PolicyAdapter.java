package app.xedigital.ai.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

import app.xedigital.ai.R;
import app.xedigital.ai.model.policy.PoliciesItem;

public class PolicyAdapter extends RecyclerView.Adapter<PolicyAdapter.PolicyViewHolder> {

    private List<PoliciesItem> policies;

    public PolicyAdapter(List<PoliciesItem> policies) {
        this.policies = policies;
    }

    @NonNull
    @Override
    public PolicyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.policy_item, parent, false);
        return new PolicyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PolicyViewHolder holder, int position) {
        PoliciesItem policy = policies.get(position);
        holder.policyName.setText(policy.getName());
        holder.policyDate.setText(policy.getUpdatedAt());

        // Convert boolean to String
//        String status = policy.isActive() ? "Active" : "Inactive";
//        holder.policyStatus.setText(status);
        if (policy.isActive()) {
            holder.policyStatus.setText("Active");
            holder.policyStatus.setChipBackgroundColorResource(R.color._000000);
        } else {
            holder.policyStatus.setText("Inactive");
            holder.policyStatus.setChipBackgroundColorResource(R.color.status_pending);
        }

        holder.policyViewIcon.setOnClickListener(v -> {

        });
    }

    @Override
    public int getItemCount() {
        return policies.size();
    }

    public static class PolicyViewHolder extends RecyclerView.ViewHolder {
        public ShapeableImageView policyViewIcon;
        public TextView policyName;
        public TextView policyDate;
        public Chip policyStatus;


        public PolicyViewHolder(@NonNull View itemView) {
            super(itemView);
            policyViewIcon = itemView.findViewById(R.id.btn_policyIcon);
            policyName = itemView.findViewById(R.id.tv_policy_name);
            policyDate = itemView.findViewById(R.id.tv_policy_date);
            policyStatus = itemView.findViewById(R.id.tv_policy_status);

        }
    }
}
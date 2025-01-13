package app.xedigital.ai.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

import app.xedigital.ai.R;
import app.xedigital.ai.activity.PDFViewActivity;
import app.xedigital.ai.model.policy.PoliciesItem;
import app.xedigital.ai.utills.DateTimeUtils;

public class PolicyAdapter extends RecyclerView.Adapter<PolicyAdapter.PolicyViewHolder> {

    private final List<PoliciesItem> policies;
    private final Context context;

    public PolicyAdapter(List<PoliciesItem> policies, Context context) {
        this.policies = policies;
        this.context = context;
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
        holder.policyDate.setText("Policy Date : " + DateTimeUtils.getDayOfWeekAndDate(policy.getUpdatedAt()));

        holder.policyViewIcon.setOnClickListener(v -> {
            PoliciesItem policy1 = policies.get(holder.getBindingAdapterPosition());
            String pdfUrl = policy1.getPolicyFileURL();

            Intent intent = new Intent(context, PDFViewActivity.class);
            intent.putExtra("pdfUrl", pdfUrl);
            context.startActivity(intent);
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
//        public Chip policyStatus;


        public PolicyViewHolder(@NonNull View itemView) {
            super(itemView);
            policyViewIcon = itemView.findViewById(R.id.btn_policyIcon);
            policyName = itemView.findViewById(R.id.tv_policy_name);
            policyDate = itemView.findViewById(R.id.tv_policy_date);
//            policyStatus = itemView.findViewById(R.id.tv_policy_status);

        }
    }
}
package app.xedigital.ai.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.List;

import app.xedigital.ai.R;
import app.xedigital.ai.activity.PDFViewActivity;
import app.xedigital.ai.model.policy.PoliciesItem;
import app.xedigital.ai.utills.DateTimeUtils;

public class PolicyAdapter extends RecyclerView.Adapter<PolicyAdapter.PolicyViewHolder> {

    private final Context context;
    private List<PoliciesItem> policies;

    // ─────────────────────────────────────────────
    // Constructor
    // ─────────────────────────────────────────────

    public PolicyAdapter(List<PoliciesItem> policies, Context context) {
        this.policies = policies != null ? policies : new ArrayList<>();
        this.context = context;
    }

    // ─────────────────────────────────────────────
    // RecyclerView overrides
    // ─────────────────────────────────────────────

    @NonNull
    @Override
    public PolicyViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.policy_item, parent, false);
        return new PolicyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull PolicyViewHolder holder,
            int position
    ) {
        PoliciesItem policy = policies.get(position);

        // ── Policy name ───────────────────────────
        holder.policyName.setText(
                policy.getName() != null ? policy.getName() : "—"
        );

        // ── Updated date ──────────────────────────
        String formattedDate = DateTimeUtils
                .getDayOfWeekAndDate(policy.getUpdatedAt());
        holder.policyDate.setText(
                context.getString(R.string.policy_date_prefix, formattedDate)
        );

        // ── Active / Inactive accent bar ──────────
        // Uses the real `active` boolean from the model
        // Active   → green  (policy is live)
        // Inactive → red    (policy is disabled/archived)
        int accentColorRes = policy.isActive()
                ? R.color.policy_active
                : R.color.policy_inactive;

        holder.policyTypeIndicator.setBackgroundTintList(
                ColorStateList.valueOf(
                        ContextCompat.getColor(context, accentColorRes)
                )
        );

        // ── Card alpha — dim inactive policies ────
        // Gives an immediate visual cue that an
        // inactive policy is not currently in effect
        holder.policyCard.setAlpha(policy.isActive() ? 1.0f : 0.6f);

        // ── Click — open PDF ──────────────────────
        View.OnClickListener openPdf = v -> openPdf(
                policies.get(holder.getBindingAdapterPosition())
        );
        holder.policyCard.setOnClickListener(openPdf);
        holder.policyViewIcon.setOnClickListener(openPdf);
    }

    @Override
    public int getItemCount() {
        return policies != null ? policies.size() : 0;
    }

    // ─────────────────────────────────────────────
    // Public API
    // ─────────────────────────────────────────────

    /**
     * Swap full dataset and refresh.
     * Call from your Fragment/Activity observer.
     */
    public void updatePolicies(List<PoliciesItem> newPolicies) {
        this.policies = newPolicies != null ? newPolicies : new ArrayList<>();
        notifyDataSetChanged();
    }

    /**
     * Returns only the active policies from the current list.
     * Handy if you want to offer a "Show active only" filter toggle.
     */
    public List<PoliciesItem> getActivePolicies() {
        List<PoliciesItem> active = new ArrayList<>();
        for (PoliciesItem policy : policies) {
            if (policy.isActive()) active.add(policy);
        }
        return active;
    }

    // ─────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────

    /**
     * Opens PDFViewActivity with the policy's file URL.
     * Silently ignores taps if the URL is missing.
     */
    private void openPdf(PoliciesItem policy) {
        String pdfUrl = policy.getPolicyFileURL();
        if (pdfUrl == null || pdfUrl.trim().isEmpty()) return;

        Intent intent = new Intent(context, PDFViewActivity.class);
        intent.putExtra("pdfUrl", pdfUrl);

        // Pass policy name so PDFViewActivity can show it
        // in its toolbar — useful UX detail
        intent.putExtra("policyName", policy.getName());

        context.startActivity(intent);
    }

    // ─────────────────────────────────────────────
    // ViewHolder
    // ─────────────────────────────────────────────

    public static class PolicyViewHolder extends RecyclerView.ViewHolder {

        final MaterialCardView policyCard;
        final ShapeableImageView policyViewIcon;
        final MaterialTextView policyName;
        final MaterialTextView policyDate;
        final View policyTypeIndicator;

        public PolicyViewHolder(@NonNull View itemView) {
            super(itemView);
            policyCard = itemView.findViewById(R.id.policyCard);
            policyViewIcon = itemView.findViewById(R.id.btn_policyIcon);
            policyName = itemView.findViewById(R.id.tv_policy_name);
            policyDate = itemView.findViewById(R.id.tv_policy_date);
            policyTypeIndicator = itemView.findViewById(R.id.policy_type_indicator);
        }
    }
}
package app.xedigital.ai.ui.permission;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.List;

import app.xedigital.ai.R;

public class PermissionAdapter extends RecyclerView.Adapter<PermissionAdapter.ViewHolder> {

    private final List<PermissionItem> items;
    private final OnPermissionClickListener listener;

    public PermissionAdapter(List<PermissionItem> items, OnPermissionClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_permission, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PermissionItem item = items.get(position);
        holder.title.setText(item.getTitle());
        holder.desc.setText(item.getDescription());
        holder.switchPermission.setChecked(item.isGranted());
        if (item.isGranted()) {
            holder.statusLabel.setText("Active");
            holder.statusLabel.setTextColor(Color.parseColor("#2E7D32"));
        } else {
            holder.statusLabel.setText("Inactive");
            holder.statusLabel.setTextColor(Color.parseColor("#757575"));
        }
        holder.tag.setVisibility(item.isMandatory() ? View.VISIBLE : View.GONE);
        if (item.getManifestPermission() == null && !"BIOMETRIC".equals(item.getTag())) {
            holder.switchPermission.setVisibility(View.INVISIBLE);
            holder.statusLabel.setText("Active");
        } else {
            holder.switchPermission.setVisibility(View.VISIBLE);
        }
        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public interface OnPermissionClickListener {
        void onItemClick(PermissionItem item);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, desc, statusLabel;
        View tag;
        SwitchMaterial switchPermission;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.textTitle);
            desc = itemView.findViewById(R.id.textDescription);
            tag = itemView.findViewById(R.id.textMandatoryTag);
            statusLabel = itemView.findViewById(R.id.textStatusLabel);
            switchPermission = itemView.findViewById(R.id.switchPermission);
        }
    }
}
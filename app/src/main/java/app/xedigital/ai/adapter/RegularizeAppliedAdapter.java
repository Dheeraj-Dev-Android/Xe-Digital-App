package app.xedigital.ai.adapter;

import static app.xedigital.ai.ui.regularize_attendance.RegularizeViewFragment.ARG_REGULARIZE_APPLIED_ITEM;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import app.xedigital.ai.R;
import app.xedigital.ai.model.regularizeApplied.AttendanceRegularizeAppliedItem;
import app.xedigital.ai.utills.DateTimeUtils;

public class RegularizeAppliedAdapter extends RecyclerView.Adapter<RegularizeAppliedAdapter.ViewHolder> {

    private final List<AttendanceRegularizeAppliedItem> items;

    public RegularizeAppliedAdapter(List<AttendanceRegularizeAppliedItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.regularize_applied, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AttendanceRegularizeAppliedItem item = items.get(position);

        String formattedPunchDate = DateTimeUtils.getDayOfWeekAndDate(item.getPunchDate());
        holder.empPunchDate.setText("Punch Date : " + formattedPunchDate);
        String formattedAppliedDate = DateTimeUtils.getDayOfWeekAndDate(item.getAppliedDate());
        holder.appliedDate.setText("Applied Date : " + formattedAppliedDate);
        holder.appliedStatus.setText("Applied Status : " + item.getStatus());


        holder.btnViewRegularize.setOnClickListener(v -> {
            if (position != RecyclerView.NO_POSITION) {
                AttendanceRegularizeAppliedItem regularizeAppliedItem = items.get(position);
                if (regularizeAppliedItem != null && regularizeAppliedItem.getId() != null) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(ARG_REGULARIZE_APPLIED_ITEM, regularizeAppliedItem);
                    Navigation.findNavController(v).navigate(R.id.action_nav_regularizeAppliedFragment_to_nav_regularizeViewFragment, bundle);
                } else {
                    Toast.makeText(v.getContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(v.getContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView empPunchDate;
        public TextView appliedDate;
        public TextView appliedStatus;

        public ImageButton btnViewRegularize;

        public ViewHolder(View itemView) {
            super(itemView);
            empPunchDate = itemView.findViewById(R.id.empPunchDate);
            appliedDate = itemView.findViewById(R.id.appliedDate);
            appliedStatus = itemView.findViewById(R.id.appliedStatus);
            btnViewRegularize = itemView.findViewById(R.id.btn_viewRegularize);
        }

    }
}
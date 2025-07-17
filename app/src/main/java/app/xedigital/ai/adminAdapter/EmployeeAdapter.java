package app.xedigital.ai.adminAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;

import app.xedigital.ai.R;
import app.xedigital.ai.model.Admin.EmployeeDetails.EmployeesItem;

public class EmployeeAdapter extends RecyclerView.Adapter<EmployeeAdapter.EmployeeViewHolder> {

    private final List<EmployeesItem> employeeList;
    private final Context context;

    public EmployeeAdapter(Context context, List<EmployeesItem> employeeList) {
        this.context = context;
        this.employeeList = employeeList;
    }

    @NonNull
    @Override
    public EmployeeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_employee_card, parent, false);
        return new EmployeeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EmployeeViewHolder holder, int position) {
        EmployeesItem employee = employeeList.get(position);

        holder.tvName.setText(employee.getFirstname() + " " + employee.getLastname());
        holder.tvEmail.setText(employee.getEmail());
        holder.tvDesignation.setText(employee.getDesignation());
//        holder.tvDepartment.setText(employee.getDepartment().getName());
//        holder.tvShift.setText(employee.getShift().getName() + " : " + employee.getShift().getStartTime() + "-" + employee.getShift().getEndTime());
        holder.chipStatus.setText(employee.isActive() ? "Active" : "Inactive");

        if (employee.getDepartment() != null && employee.getDepartment().getName() != null) {
            holder.tvDepartment.setText(employee.getDepartment().getName());
        } else {
            holder.tvDepartment.setText("N/A"); // or use empty string or placeholder
        }
        if (employee.getShift() != null &&
                employee.getShift().getName() != null &&
                employee.getShift().getStartTime() != null &&
                employee.getShift().getEndTime() != null) {

            String shiftText = employee.getShift().getName() + " ( " +
                    employee.getShift().getStartTime() + " - " +
                    employee.getShift().getEndTime() + ")";
            holder.tvShift.setText(shiftText);
        } else {
            holder.tvShift.setText("Shift not assigned");
        }

        if (employee.isActive()) {
            holder.chipStatus.setText("Active");
            holder.chipStatus.setChipBackgroundColorResource(R.color.active_green);
        } else {
            holder.chipStatus.setText("Inactive");
            holder.chipStatus.setChipBackgroundColorResource(R.color.inactive_gray);
        }

        // Load profile image using Glide (fallback to placeholder)
        if (employee.getProfileImageUrl() != null) {
            Glide.with(context).load(employee.getProfileImageUrl()).placeholder(R.drawable.ic_user_placeholder).into(holder.ivProfile);
        } else {
            holder.ivProfile.setImageResource(R.drawable.ic_user_placeholder);
        }

        holder.btnViewMore.setOnClickListener(v -> {
            Toast.makeText(context, "View more for " + employee.getFirstname(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return employeeList.size();
    }

    static class EmployeeViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView ivProfile;
        MaterialTextView tvName, tvEmail, tvDesignation, tvDepartment, tvShift;
        Chip chipStatus;
        ShapeableImageView btnViewMore;

        public EmployeeViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfile = itemView.findViewById(R.id.emplProfile);
            tvName = itemView.findViewById(R.id.emplName);
            tvEmail = itemView.findViewById(R.id.emplEmail);
            tvDesignation = itemView.findViewById(R.id.emplDesignation);
            tvDepartment = itemView.findViewById(R.id.tvDepartment);
            tvShift = itemView.findViewById(R.id.emplShift);
            chipStatus = itemView.findViewById(R.id.emplChipStatus);
            btnViewMore = itemView.findViewById(R.id.btnViewMore);
        }
    }
}

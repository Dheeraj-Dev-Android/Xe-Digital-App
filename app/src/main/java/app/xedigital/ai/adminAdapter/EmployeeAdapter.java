package app.xedigital.ai.adminAdapter;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;

import app.xedigital.ai.R;
import app.xedigital.ai.adminUI.employeeDetails.EditEmployeeFragment;
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
        holder.chipStatus.setText(employee.isActive() ? "Active" : "Inactive");

        if (employee.getDepartment() != null && employee.getDepartment().getName() != null) {
            holder.tvDepartment.setText(employee.getDepartment().getName());
        } else {
            holder.tvDepartment.setText("N/A");
        }
        if (employee.getShift() != null && employee.getShift().getName() != null && employee.getShift().getStartTime() != null && employee.getShift().getEndTime() != null) {

            String shiftText = employee.getShift().getName() + " ( " + employee.getShift().getStartTime() + " - " + employee.getShift().getEndTime() + ")";
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
            Glide.with(context).load(employee.getProfileImageUrl()).placeholder(R.drawable.ic_profile_placeholder).into(holder.ivProfile);
        } else {
            holder.ivProfile.setImageResource(R.drawable.ic_profile_placeholder);
        }

        holder.btnViewMore.setOnClickListener(v -> {
            Toast.makeText(context, "View more for " + employee.getFirstname(), Toast.LENGTH_SHORT).show();
        });
        holder.empEditBtn.setOnClickListener(v -> {
            Toast.makeText(context, "Edit for " + employee.getFirstname(), Toast.LENGTH_SHORT).show();
            EmployeesItem selectedItem = employeeList.get(position);
            String userId = selectedItem.getId();
            if (userId != null) {
//                EditEmployeeFragment fragment = new EditEmployeeFragment();
                Bundle bundle = new Bundle();
                bundle.putSerializable(EditEmployeeFragment.ARG_SELECTED_ITEM, selectedItem);
//                fragment.setArguments(bundle);
                Navigation.findNavController(v).navigate(R.id.nav_employees_to_nav_editEmployee, bundle);
            } else {
                Toast.makeText(v.getContext(), "Selected Employee or EmployeeId is null", Toast.LENGTH_SHORT).show();
            }
        });
        holder.empAddFace.setOnClickListener(v -> {
            Toast.makeText(context, "Add Face for " + employee.getFirstname(), Toast.LENGTH_SHORT).show();
        });

//        holder.empAddLeave.setOnClickListener(v -> {
//            Toast.makeText(context, "Add Leave for " + employee.getFirstname(), Toast.LENGTH_SHORT).show();
//        });
        holder.empAddLeave.setOnClickListener(v -> {
            // Inflate the custom layout
            LayoutInflater inflater = LayoutInflater.from(context);
            View dialogView = inflater.inflate(R.layout.item_add_leave, null);

            // Optional: If you want to access views from dialogView
            TextView tvTitle = dialogView.findViewById(R.id.tvTitle);
            tvTitle.setText("Leave Assign - " + employee.getFirstname() + " " + employee.getLastname());

            MaterialButton btnClose = dialogView.findViewById(R.id.btnClose);
            MaterialButton btnSubmit = dialogView.findViewById(R.id.btnSubmit);

            // Build the dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog);
            builder.setView(dialogView);
            AlertDialog dialog = builder.create();

            // Close button action
            btnClose.setOnClickListener(view -> dialog.dismiss());

            // Submit button action
            btnSubmit.setOnClickListener(view -> {
                // Handle submission logic here
                Toast.makeText(context, "Leave submitted for " + employee.getFirstname(), Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });

            // Show the dialog
            dialog.show();
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
        ShapeableImageView btnViewMore, empEditBtn, empAddFace, empAddLeave;

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
            empEditBtn = itemView.findViewById(R.id.empEditBtn);
            empAddFace = itemView.findViewById(R.id.empAddFace);
            empAddLeave = itemView.findViewById(R.id.empAddLeave);
        }
    }
}

package app.xedigital.ai.adminAdapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import app.xedigital.ai.R;
import app.xedigital.ai.adminApi.AdminAPIClient;
import app.xedigital.ai.adminApi.AdminAPIInterface;
import app.xedigital.ai.adminUI.employeeDetails.EditEmployeeFragment;
import app.xedigital.ai.model.Admin.EmployeeDetails.EmployeesItem;
import app.xedigital.ai.model.Admin.assignLeave.AssignLeaveRequest;
import app.xedigital.ai.model.Admin.leaveBalance.LeaveBalanceResponse;
import app.xedigital.ai.model.Admin.leaveType.LeaveTypeResponse;
import app.xedigital.ai.model.Admin.leaveType.LeavetypesItem;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EmployeeAdapter extends RecyclerView.Adapter<EmployeeAdapter.EmployeeViewHolder> {

    private final List<EmployeesItem> employeeList;
    private final Context context;
    private String selectedEmployeeId;
    private String selectedId;

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

        holder.empAddLeave.setOnClickListener(v -> {
            // Inflate the custom layout
            LayoutInflater inflater = LayoutInflater.from(context);
            View dialogView = inflater.inflate(R.layout.item_add_leave, null);

            // Optional: If you want to access views from dialogView
            TextView tvTitle = dialogView.findViewById(R.id.tvTitle);
            tvTitle.setText("Leave Assign - " + employee.getFirstname() + " " + employee.getLastname());
            TextView tvLeaveBalance = dialogView.findViewById(R.id.tvLeaveBalance);

            MaterialButton btnClose = dialogView.findViewById(R.id.btnClose);
            MaterialButton btnSubmit = dialogView.findViewById(R.id.btnSubmit);

            // Build the dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog);
            builder.setView(dialogView);
            AlertDialog dialog = builder.create();

            AutoCompleteTextView actCalculationType = dialogView.findViewById(R.id.actvCalculationType);
            String[] calculationTypes = new String[]{"Please select", "Opening", "Credit", "Debit", "Used"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, calculationTypes);
            actCalculationType.setAdapter(adapter);
            actCalculationType.setText(calculationTypes[0], false);

            AutoCompleteTextView actLeaveType = dialogView.findViewById(R.id.actLeaveType);
            SharedPreferences sharedPreferences = context.getSharedPreferences("AdminCred", Context.MODE_PRIVATE);
            String token = sharedPreferences.getString("authToken", "");
            AdminAPIInterface apiService = AdminAPIClient.getInstance().getBase2();
            Call<LeaveTypeResponse> call = apiService.getLeaveTypes("jwt " + token);

            call.enqueue(new Callback<LeaveTypeResponse>() {
                @Override
                public void onResponse(@NonNull Call<LeaveTypeResponse> call, @NonNull Response<LeaveTypeResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                        List<LeavetypesItem> leaveTypes = response.body().getData().getLeavetypes();

                        List<String> leaveTypeNames = new ArrayList<>();
                        leaveTypeNames.add("Please select");

                        for (LeavetypesItem item : leaveTypes) {
                            leaveTypeNames.add(item.getLeavetypeName());
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, leaveTypeNames);
                        actLeaveType.setAdapter(adapter);
                        actLeaveType.setText(leaveTypeNames.get(0), false);

                        Map<String, String> leaveTypeNameToIdMap = new HashMap<>();
                        for (LeavetypesItem item : leaveTypes) {
                            leaveTypeNameToIdMap.put(item.getLeavetypeName(), item.getId());
                        }

                        actLeaveType.setOnItemClickListener((parent, view1, position, id1) -> {
                            String selectedName = (String) parent.getItemAtPosition(position);
                            selectedId = leaveTypeNameToIdMap.get(selectedName);

                            if (selectedId != null && employee.getId() != null) {
                                Call<LeaveBalanceResponse> balanceCall = apiService.getLeaves("jwt " + token, selectedId, employee.getId());

                                balanceCall.enqueue(new Callback<LeaveBalanceResponse>() {
                                    @Override
                                    public void onResponse(@NonNull Call<LeaveBalanceResponse> call, @NonNull Response<LeaveBalanceResponse> response) {
                                        if (response.isSuccessful() && response.body() != null) {
                                            LeaveBalanceResponse leaveBalance = response.body();
                                            tvLeaveBalance.setText(leaveBalance.getData().getCreditLeave() + " Leaves");

                                        } else {
                                            Toast.makeText(context, "Failed to fetch leave balance", Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onFailure(@NonNull Call<LeaveBalanceResponse> call, @NonNull Throwable t) {
                                        Toast.makeText(context, "API Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });

                    } else {
                        Toast.makeText(context, "Failed to load leave types", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<LeaveTypeResponse> call, @NonNull Throwable t) {
                    Toast.makeText(context, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
            EditText etAssignedLeave = dialogView.findViewById(R.id.etAssignedLeave);
            EditText etTotalLeave = dialogView.findViewById(R.id.etTotalLeave);
            TextView etRemarks = dialogView.findViewById(R.id.etRemarks);

            // Create a single update method
            TextWatcher watcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    calculateAndSetTotalLeave(etAssignedLeave.getText().toString(), tvLeaveBalance.getText().toString(), actCalculationType.getText().toString(), etTotalLeave);
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            };

            etAssignedLeave.addTextChangedListener(watcher);
            actCalculationType.setOnItemClickListener((parent, view, pos, id) -> {
                calculateAndSetTotalLeave(etAssignedLeave.getText().toString(), tvLeaveBalance.getText().toString(), actCalculationType.getText().toString(), etTotalLeave);
            });


            // Close button action
            btnClose.setOnClickListener(view -> dialog.dismiss());

            // Submit button action

            btnSubmit.setOnClickListener(view -> {
                // Get data from views
                String leavetypeId = selectedId;
                String calculationType = actCalculationType.getText().toString();
//                int assignedLeave = Integer.parseInt(etAssignedLeave.getText().toString().trim());
                String input = etAssignedLeave.getText().toString().trim();
                double assignedLeave = Double.parseDouble(input);

                String remarks = etRemarks.getText().toString().trim();
                String total = etTotalLeave.getText().toString().trim();
                double totalLeave = Double.parseDouble(total);
                String employeeId = employee.getId();
                String assignType = "manual";

                // Create request body
                AssignLeaveRequest request = new AssignLeaveRequest();
                request.setLeavetype(leavetypeId);
                request.setLeaveCalculationType(calculationType.toLowerCase());
                request.setAssignedLeave(assignedLeave);
                request.setRemarks(remarks);
                request.setTotalLeave(totalLeave);
                request.setEmployee(employeeId);
                request.setAssignType(assignType);

                // API Call
                Call<ResponseBody> assignLeave = apiService.assignLeave("jwt " + token, request);
                assignLeave.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(context, "Leave assigned successfully", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        } else {
                            Toast.makeText(context, "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                        Toast.makeText(context, "API call failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            });


            // Show the dialog
            dialog.show();
        });

    }


    private void calculateAndSetTotalLeave(String assignedLeaveText, String balanceText, String calculationType, EditText etTotalLeave) {
        try {
            String assignedStr = assignedLeaveText.replaceAll("[^\\d.]+", "");
            String balanceStr = balanceText.replaceAll("[^\\d.]+", "");

            double assignedLeave = Double.parseDouble(assignedStr.isEmpty() ? "0" : assignedStr);
            double balanceLeave = Double.parseDouble(balanceStr.isEmpty() ? "0" : balanceStr);
            double totalLeave;

            // Normalize calculation type
            String type = calculationType.toLowerCase().trim();

            if (type.equals("credit") || type.equals("opening")) {
                totalLeave = assignedLeave + balanceLeave;
            } else if (type.equals("debit") || type.equals("used")) {
                totalLeave = balanceLeave - assignedLeave;
            } else {
                totalLeave = assignedLeave; // Fallback
            }

            etTotalLeave.setText(String.valueOf(totalLeave));
        } catch (NumberFormatException e) {
            e.printStackTrace();
            etTotalLeave.setText("0");
        }
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

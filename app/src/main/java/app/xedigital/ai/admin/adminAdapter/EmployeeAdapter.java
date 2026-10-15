package app.xedigital.ai.admin.adminAdapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import app.xedigital.ai.R;
import app.xedigital.ai.admin.adminAPI.AdminAPIClient;
import app.xedigital.ai.admin.adminAPI.AdminAPIInterface;
import app.xedigital.ai.admin.adminModal.EmployeeDetails.EmployeesItem;
import app.xedigital.ai.admin.adminModal.assignLeave.AssignLeaveRequest;
import app.xedigital.ai.admin.adminModal.leaveBalance.LeaveBalanceResponse;
import app.xedigital.ai.admin.adminModal.leaveType.LeaveTypeResponse;
import app.xedigital.ai.admin.adminModal.leaveType.LeavetypesItem;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EmployeeAdapter extends RecyclerView.Adapter<EmployeeAdapter.EmployeeViewHolder> {

    private static final int[] AVATAR_COLORS = {
            0xFF1E88E5, 0xFF43A047, 0xFFE53935, 0xFF8E24AA, 0xFFFB8C00,
            0xFF00ACC1, 0xFF3949AB, 0xFFD81B60, 0xFF00897B, 0xFF6D4C41
    };

    private final List<EmployeesItem> employeeList;
    private final Context context;
    private final OnEmployeeActionListener listener;
    private String selectedId;

    public EmployeeAdapter(Context context, List<EmployeesItem> employeeList, OnEmployeeActionListener listener) {
        this.context = context;
        this.employeeList = employeeList;
        this.listener = listener;
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

        // Safe Full Name
        String firstName = employee.getFirstname();
        String lastName = employee.getLastname();
        StringBuilder nameBuilder = new StringBuilder();
        if (firstName != null && !firstName.trim().isEmpty()) nameBuilder.append(firstName.trim());
        if (lastName != null && !lastName.trim().isEmpty()) {
            if (nameBuilder.length() > 0) nameBuilder.append(" ");
            nameBuilder.append(lastName.trim());
        }
        String displayName = nameBuilder.length() > 0 ? nameBuilder.toString() : "N/A";
        holder.tvName.setText(displayName);

        // Safe Email
        String email = employee.getEmail();
        holder.tvEmail.setText((email != null && !email.trim().isEmpty()) ? email : "N/A");

        // Safe Designation
        String designation = employee.getDesignation();
        holder.tvDesignation.setText((designation != null && !designation.trim().isEmpty()) ? designation : "N/A");

        // Safe Department
        if (employee.getDepartment() != null && employee.getDepartment().getName() != null
                && !employee.getDepartment().getName().trim().isEmpty()) {
            holder.tvDepartment.setText(employee.getDepartment().getName());
        } else {
            holder.tvDepartment.setText("N/A");
        }

        // Safe Shift
        if (employee.getShift() != null
                && employee.getShift().getName() != null && !employee.getShift().getName().trim().isEmpty()
                && employee.getShift().getStartTime() != null && !employee.getShift().getStartTime().trim().isEmpty()
                && employee.getShift().getEndTime() != null && !employee.getShift().getEndTime().trim().isEmpty()) {
            holder.tvShift.setText(employee.getShift().getName() + " ( " + employee.getShift().getStartTime() + " - " + employee.getShift().getEndTime() + " )");
        } else {
            holder.tvShift.setText("N/A");
        }

        // Status
        if (employee.isActive()) {
            holder.chipStatus.setText("Active");
            holder.chipStatus.setChipBackgroundColorResource(R.color.active_green);
        } else {
            holder.chipStatus.setText("Inactive");
            holder.chipStatus.setChipBackgroundColorResource(R.color.inactive_gray);
        }

        // Profile Image / Initials
        String profileUrl = (String) employee.getProfileImageUrl();
        if (profileUrl != null && !profileUrl.trim().isEmpty()) {
            holder.ivProfile.setVisibility(View.VISIBLE);
            holder.tvProfileInitials.setVisibility(View.GONE);
            Glide.with(context).load(profileUrl).placeholder(R.drawable.ic_profile_placeholder).into(holder.ivProfile);
        } else {
            holder.ivProfile.setVisibility(View.GONE);
            holder.tvProfileInitials.setVisibility(View.VISIBLE);
            holder.tvProfileInitials.setText(getInitials(displayName));
            GradientDrawable bg = new GradientDrawable();
            bg.setShape(GradientDrawable.OVAL);
            bg.setColor(getAvatarColor(displayName));
            holder.tvProfileInitials.setBackground(bg);
        }

        // Three-Dot Menu
        final String finalDisplayName = displayName;
        holder.btnEmployeeMenu.setOnClickListener(v -> {
            BottomSheetDialog bottomSheet = new BottomSheetDialog(context);
            View sheetView = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_employee_actions, null);
            bottomSheet.setContentView(sheetView);

            // CHANGED: Now lookups return the full row LinearLayout (as View) instead of inner icon
            View sheetBtnViewMore = sheetView.findViewById(R.id.btnViewMore);
            View sheetEmpEditBtn = sheetView.findViewById(R.id.empEditBtn);
            View sheetEmpAddFace = sheetView.findViewById(R.id.empAddFace);
            View sheetEmpAddLeave = sheetView.findViewById(R.id.empAddLeave);
            View sheetEmpLeaveDetails = sheetView.findViewById(R.id.empLeaveDetails);

            // View More
            sheetBtnViewMore.setOnClickListener(btnView -> {
                bottomSheet.dismiss();
                Toast.makeText(context, "View more for " + finalDisplayName, Toast.LENGTH_SHORT).show();
            });

            // Edit Employee — DELEGATE TO FRAGMENT
            sheetEmpEditBtn.setOnClickListener(btnView -> {
                Log.d("EmployeeAdapter", "Edit clicked for: " + finalDisplayName);
                bottomSheet.dismiss();
                int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onEditEmployee(employeeList.get(pos));
                }
            });

            // Add Face — DELEGATE TO FRAGMENT
            sheetEmpAddFace.setOnClickListener(btnView -> {
                Log.d("EmployeeAdapter", "Add Face clicked for: " + finalDisplayName);
                bottomSheet.dismiss();
                int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onAddFace(employeeList.get(pos));
                }
            });

            // Add Leave
            sheetEmpAddLeave.setOnClickListener(btnView -> {
                bottomSheet.dismiss();

                View dialogView = LayoutInflater.from(context).inflate(R.layout.item_add_leave, null);
                TextView tvTitle = dialogView.findViewById(R.id.tvTitle);
                tvTitle.setText("Leave Assign - " + finalDisplayName);
                TextView tvLeaveBalance = dialogView.findViewById(R.id.tvLeaveBalance);
                MaterialButton btnClose = dialogView.findViewById(R.id.btnClose);
                MaterialButton btnSubmit = dialogView.findViewById(R.id.btnSubmit);

                AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog);
                builder.setView(dialogView);
                AlertDialog dialog = builder.create();

                AutoCompleteTextView actCalculationType = dialogView.findViewById(R.id.actvCalculationType);
                String[] calculationTypes = new String[]{"Please select", "Opening", "Credit", "Debit", "Used"};
                ArrayAdapter<String> calcAdapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, calculationTypes);
                actCalculationType.setAdapter(calcAdapter);
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
                            for (LeavetypesItem item : leaveTypes)
                                leaveTypeNames.add(item.getLeavetypeName());

                            ArrayAdapter<String> leaveAdapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, leaveTypeNames);
                            actLeaveType.setAdapter(leaveAdapter);
                            actLeaveType.setText(leaveTypeNames.get(0), false);

                            Map<String, String> leaveTypeNameToIdMap = new HashMap<>();
                            for (LeavetypesItem item : leaveTypes)
                                leaveTypeNameToIdMap.put(item.getLeavetypeName(), item.getId());

                            actLeaveType.setOnItemClickListener((adapterView, view1, pos, id1) -> {
                                String selectedName = (String) adapterView.getItemAtPosition(pos);
                                selectedId = leaveTypeNameToIdMap.get(selectedName);
                                if (selectedId != null && employee.getId() != null) {
                                    apiService.getLeaves("jwt " + token, selectedId, employee.getId()).enqueue(new Callback<LeaveBalanceResponse>() {
                                        @Override
                                        public void onResponse(@NonNull Call<LeaveBalanceResponse> c, @NonNull Response<LeaveBalanceResponse> r) {
                                            if (r.isSuccessful() && r.body() != null) {
                                                tvLeaveBalance.setText(r.body().getData().getCreditLeave() + " Leaves");
                                            } else {
                                                Toast.makeText(context, "Failed to fetch leave balance", Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                        @Override
                                        public void onFailure(@NonNull Call<LeaveBalanceResponse> c, @NonNull Throwable t) {
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
                actCalculationType.setOnItemClickListener((adapterView, view, pos, id) -> {
                    calculateAndSetTotalLeave(etAssignedLeave.getText().toString(), tvLeaveBalance.getText().toString(), actCalculationType.getText().toString(), etTotalLeave);
                });

                btnClose.setOnClickListener(view -> dialog.dismiss());
                btnSubmit.setOnClickListener(view -> {
                    String input = etAssignedLeave.getText().toString().trim();
                    String total = etTotalLeave.getText().toString().trim();
                    AssignLeaveRequest request = new AssignLeaveRequest();
                    request.setLeavetype(selectedId);
                    request.setLeaveCalculationType(actCalculationType.getText().toString().toLowerCase());
                    request.setAssignedLeave(Double.parseDouble(input));
                    request.setRemarks(etRemarks.getText().toString().trim());
                    request.setTotalLeave(Double.parseDouble(total));
                    request.setEmployee(employee.getId());
                    request.setAssignType("manual");

                    apiService.assignLeave("jwt " + token, request).enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(@NonNull Call<ResponseBody> c, @NonNull Response<ResponseBody> r) {
                            if (r.isSuccessful()) {
                                Toast.makeText(context, "Leave assigned successfully", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            } else {
                                Toast.makeText(context, "Error: " + r.code(), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<ResponseBody> c, @NonNull Throwable t) {
                            Toast.makeText(context, "API call failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                });
                dialog.show();
            });

            // Leave Details
            sheetEmpLeaveDetails.setOnClickListener(btnView -> {
                bottomSheet.dismiss();
                Toast.makeText(context, "Leave Details for " + finalDisplayName, Toast.LENGTH_SHORT).show();
            });

            bottomSheet.show();
        });
    }

    private void calculateAndSetTotalLeave(String assignedLeaveText, String balanceText, String calculationType, EditText etTotalLeave) {
        try {
            String assignedStr = assignedLeaveText.replaceAll("[^\\d.]+", "");
            String balanceStr = balanceText.replaceAll("[^\\d.]+", "");
            double assignedLeave = Double.parseDouble(assignedStr.isEmpty() ? "0" : assignedStr);
            double balanceLeave = Double.parseDouble(balanceStr.isEmpty() ? "0" : balanceStr);
            double totalLeave;
            String type = calculationType.toLowerCase().trim();
            if (type.equals("credit") || type.equals("opening"))
                totalLeave = assignedLeave + balanceLeave;
            else if (type.equals("debit") || type.equals("used"))
                totalLeave = balanceLeave - assignedLeave;
            else totalLeave = assignedLeave;
            etTotalLeave.setText(String.valueOf(totalLeave));
        } catch (NumberFormatException e) {
            etTotalLeave.setText("0");
        }
    }

    @Override
    public int getItemCount() {
        return employeeList != null ? employeeList.size() : 0;
    }

    private String getInitials(String name) {
        if (name == null || name.trim().isEmpty() || name.equalsIgnoreCase("N/A")) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1)
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        return (parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
    }

    private int getAvatarColor(String name) {
        if (name == null || name.trim().isEmpty()) return AVATAR_COLORS[0];
        return AVATAR_COLORS[Math.abs(name.trim().hashCode()) % AVATAR_COLORS.length];
    }

    public interface OnEmployeeActionListener {
        void onEditEmployee(EmployeesItem employee);

        void onAddFace(EmployeesItem employee);
    }

    static class EmployeeViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView ivProfile, btnEmployeeMenu;
        MaterialTextView tvName, tvEmail, tvDesignation, tvDepartment, tvShift;
        Chip chipStatus;
        TextView tvProfileInitials;

        public EmployeeViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfile = itemView.findViewById(R.id.emplProfile);
            tvProfileInitials = itemView.findViewById(R.id.tvProfileInitials);
            btnEmployeeMenu = itemView.findViewById(R.id.btnEmployeeMenu);
            tvName = itemView.findViewById(R.id.emplName);
            tvEmail = itemView.findViewById(R.id.emplEmail);
            tvDesignation = itemView.findViewById(R.id.emplDesignation);
            tvDepartment = itemView.findViewById(R.id.tvDepartment);
            tvShift = itemView.findViewById(R.id.emplShift);
            chipStatus = itemView.findViewById(R.id.emplChipStatus);
        }
    }
}
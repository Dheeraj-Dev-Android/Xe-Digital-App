package app.xedigital.ai.admin.adminAdapter;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;

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
import app.xedigital.ai.admin.adminModal.EmployeeLeaves.EmployeeLeaveResponse;
import app.xedigital.ai.admin.adminModal.EmployeeLeaves.LeavesItem;
import app.xedigital.ai.admin.adminModal.assignLeave.AssignLeaveRequest;
import app.xedigital.ai.admin.adminModal.empTimesheet.EmployeeDcrItem;
import app.xedigital.ai.admin.adminModal.empTimesheet.EmployeeTimesheetResponse;
import app.xedigital.ai.admin.adminModal.employeeAppliedLeave.AppliedLeavesItem;
import app.xedigital.ai.admin.adminModal.employeeAppliedLeave.EmployeeAppliedLeaveResponse;
import app.xedigital.ai.admin.adminModal.leaveBalance.LeaveBalanceResponse;
import app.xedigital.ai.admin.adminModal.leaveType.LeaveTypeResponse;
import app.xedigital.ai.admin.adminModal.leaveType.LeavetypesItem;
import app.xedigital.ai.utills.CustomAlertDialog;
import app.xedigital.ai.utills.DateTimeUtils;
import app.xedigital.ai.utills.SecurePrefManager;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EmployeeAdapter extends RecyclerView.Adapter<EmployeeAdapter.EmployeeViewHolder> {

    private static final int[] AVATAR_COLORS = {0xFF1E88E5, 0xFF43A047, 0xFFE53935, 0xFF8E24AA, 0xFFFB8C00, 0xFF00ACC1, 0xFF3949AB, 0xFFD81B60, 0xFF00897B, 0xFF6D4C41};

    private static final String SHORT_LEAVE_ID = "6ab39a2abbe29c32b2003ed1";
    private static final String SHORT_LEAVE_NAME = "Short Leave";

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
        if (employee.getDepartment() != null && employee.getDepartment().getName() != null && !employee.getDepartment().getName().trim().isEmpty()) {
            holder.tvDepartment.setText(employee.getDepartment().getName());
        } else {
            holder.tvDepartment.setText("N/A");
        }

        // Safe Shift
        if (employee.getShift() != null && employee.getShift().getName() != null && !employee.getShift().getName().trim().isEmpty() && employee.getShift().getStartTime() != null && !employee.getShift().getStartTime().trim().isEmpty() && employee.getShift().getEndTime() != null && !employee.getShift().getEndTime().trim().isEmpty()) {
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
            Glide.with(context).load(profileUrl).placeholder(R.drawable.ic_person).into(holder.ivProfile);
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

            View sheetBtnViewMore = sheetView.findViewById(R.id.btnViewMore);
            View sheetEmpEditBtn = sheetView.findViewById(R.id.empEditBtn);
            View sheetEmpAddFace = sheetView.findViewById(R.id.empAddFace);
            View sheetEmpAddLeave = sheetView.findViewById(R.id.empAddLeave);
            View sheetEmpLeaveDetails = sheetView.findViewById(R.id.empLeaveDetails);
            View sheetEmpAppliedLeave = sheetView.findViewById(R.id.empAppliedLeave);
            View sheetEmpTimesheet = sheetView.findViewById(R.id.empTimesheet);

            // View More — shows employee details bottom sheet
            sheetBtnViewMore.setOnClickListener(btnView -> {
                bottomSheet.dismiss();
                showEmployeeDetailsBottomSheet(employee, finalDisplayName);
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

                selectedId = null;

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

                SecurePrefManager prefManager = SecurePrefManager.getInstance(context);
                String token = prefManager.getString("authToken", "");

                AdminAPIInterface apiService = AdminAPIClient.getInstance().getBase2();
                Call<LeaveTypeResponse> call = apiService.getLeaveTypes("jwt " + token);

                call.enqueue(new Callback<LeaveTypeResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<LeaveTypeResponse> call, @NonNull Response<LeaveTypeResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            List<LeavetypesItem> leaveTypes = response.body().getData().getLeavetypes();

                            List<LeavetypesItem> filteredLeaveTypes = new ArrayList<>();
                            if (leaveTypes != null) {
                                for (LeavetypesItem item : leaveTypes) {
                                    if (item == null) continue;
                                    String id = item.getId();
                                    String name = item.getLeavetypeName();
                                    boolean isShortLeave =
                                            (SHORT_LEAVE_ID.equalsIgnoreCase(id))
                                                    || (name != null && SHORT_LEAVE_NAME.equalsIgnoreCase(name.trim()));
                                    if (!isShortLeave) {
                                        filteredLeaveTypes.add(item);
                                    }
                                }
                            }

                            List<String> leaveTypeNames = new ArrayList<>();
                            leaveTypeNames.add("Please select");
                            for (LeavetypesItem item : filteredLeaveTypes)
                                leaveTypeNames.add(item.getLeavetypeName());

                            ArrayAdapter<String> leaveAdapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, leaveTypeNames);
                            actLeaveType.setAdapter(leaveAdapter);
                            actLeaveType.setText(leaveTypeNames.get(0), false);

                            Map<String, String> leaveTypeNameToIdMap = new HashMap<>();
                            for (LeavetypesItem item : filteredLeaveTypes)
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
                                                new CustomAlertDialog(context)
                                                        .setType(CustomAlertDialog.Type.ERROR)
                                                        .setTitle("Leave Balance Error")
                                                        .setMessage("Failed to fetch leave balance.")
                                                        .setPositiveButton("OK", null)
                                                        .show();
                                            }
                                        }

                                        @Override
                                        public void onFailure(@NonNull Call<LeaveBalanceResponse> c, @NonNull Throwable t) {
                                            new CustomAlertDialog(context)
                                                    .setType(CustomAlertDialog.Type.ERROR)
                                                    .setTitle("API Error")
                                                    .setMessage("Failed to fetch leave balance: " + t.getMessage())
                                                    .setPositiveButton("OK", null)
                                                    .show();
                                        }
                                    });
                                }
                            });
                        } else {
                            new CustomAlertDialog(context)
                                    .setType(CustomAlertDialog.Type.ERROR)
                                    .setTitle("Error")
                                    .setMessage("Failed to load leave types.")
                                    .setPositiveButton("OK", null)
                                    .show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<LeaveTypeResponse> call, @NonNull Throwable t) {
                        new CustomAlertDialog(context)
                                .setType(CustomAlertDialog.Type.ERROR)
                                .setTitle("Network Error")
                                .setMessage("Could not load leave types: " + t.getMessage())
                                .setPositiveButton("OK", null)
                                .show();
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
                    // ===== VALIDATION =====

                    String leaveTypeText = actLeaveType.getText() != null ? actLeaveType.getText().toString().trim() : "";
                    if (TextUtils.isEmpty(leaveTypeText)
                            || "Please select".equalsIgnoreCase(leaveTypeText)
                            || TextUtils.isEmpty(selectedId)) {
                        new CustomAlertDialog(context)
                                .setType(CustomAlertDialog.Type.WARNING)
                                .setTitle("Validation")
                                .setMessage("Please select a leave type.")
                                .setPositiveButton("OK", null)
                                .show();
                        return;
                    }

                    String calcTypeText = actCalculationType.getText() != null ? actCalculationType.getText().toString().trim() : "";
                    if (TextUtils.isEmpty(calcTypeText) || "Please select".equalsIgnoreCase(calcTypeText)) {
                        new CustomAlertDialog(context)
                                .setType(CustomAlertDialog.Type.WARNING)
                                .setTitle("Validation")
                                .setMessage("Please select a calculation type.")
                                .setPositiveButton("OK", null)
                                .show();
                        return;
                    }

                    String input = etAssignedLeave.getText() != null ? etAssignedLeave.getText().toString().trim() : "";
                    if (TextUtils.isEmpty(input)) {
                        new CustomAlertDialog(context)
                                .setType(CustomAlertDialog.Type.WARNING)
                                .setTitle("Validation")
                                .setMessage("Please enter the number of assigned leave.")
                                .setPositiveButton("OK", null)
                                .show();
                        return;
                    }
                    double assignedLeaveVal;
                    try {
                        assignedLeaveVal = Double.parseDouble(input);
                    } catch (NumberFormatException e) {
                        new CustomAlertDialog(context)
                                .setType(CustomAlertDialog.Type.WARNING)
                                .setTitle("Validation")
                                .setMessage("Please enter a valid number for assigned leave.")
                                .setPositiveButton("OK", null)
                                .show();
                        return;
                    }

                    String total = etTotalLeave.getText() != null ? etTotalLeave.getText().toString().trim() : "";
                    if (TextUtils.isEmpty(total)) {
                        new CustomAlertDialog(context)
                                .setType(CustomAlertDialog.Type.WARNING)
                                .setTitle("Validation")
                                .setMessage("Total leave calculation failed. Please re-check inputs.")
                                .setPositiveButton("OK", null)
                                .show();
                        return;
                    }
                    double totalLeaveVal;
                    try {
                        totalLeaveVal = Double.parseDouble(total);
                    } catch (NumberFormatException e) {
                        new CustomAlertDialog(context)
                                .setType(CustomAlertDialog.Type.WARNING)
                                .setTitle("Validation")
                                .setMessage("Total leave calculation failed. Please re-check inputs.")
                                .setPositiveButton("OK", null)
                                .show();
                        return;
                    }

                    if (employee.getId() == null || employee.getId().trim().isEmpty()) {
                        new CustomAlertDialog(context)
                                .setType(CustomAlertDialog.Type.ERROR)
                                .setTitle("Error")
                                .setMessage("Employee ID is missing.")
                                .setPositiveButton("OK", null)
                                .show();
                        return;
                    }

                    // ===== BUILD REQUEST =====
                    AssignLeaveRequest request = new AssignLeaveRequest();
                    request.setLeavetype(selectedId);
                    request.setLeaveCalculationType(calcTypeText.toLowerCase());
                    request.setAssignedLeave(assignedLeaveVal);
                    request.setRemarks(etRemarks.getText() != null ? etRemarks.getText().toString().trim() : "");
                    request.setTotalLeave(totalLeaveVal);
                    request.setEmployee(employee.getId());
                    request.setAssignType("manual");

                    btnSubmit.setEnabled(false);
                    btnClose.setEnabled(false);

                    apiService.assignLeave("jwt " + token, request).enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(@NonNull Call<ResponseBody> c, @NonNull Response<ResponseBody> r) {
                            btnSubmit.setEnabled(true);
                            btnClose.setEnabled(true);
                            if (r.isSuccessful()) {
                                new CustomAlertDialog(context)
                                        .setType(CustomAlertDialog.Type.SUCCESS)
                                        .setTitle("Success")
                                        .setMessage("Leave assigned successfully.")
                                        .setPositiveButton("OK", () -> dialog.dismiss())
                                        .show();
                            } else {
                                new CustomAlertDialog(context)
                                        .setType(CustomAlertDialog.Type.ERROR)
                                        .setTitle("Error")
                                        .setMessage("Failed to assign leave. Server returned code: " + r.code())
                                        .setPositiveButton("OK", null)
                                        .show();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<ResponseBody> c, @NonNull Throwable t) {
                            btnSubmit.setEnabled(true);
                            btnClose.setEnabled(true);
                            new CustomAlertDialog(context)
                                    .setType(CustomAlertDialog.Type.ERROR)
                                    .setTitle("Network Error")
                                    .setMessage("API call failed: " + t.getMessage())
                                    .setPositiveButton("OK", null)
                                    .show();
                        }
                    });
                });
                dialog.show();
            });

            // Leave Details
            sheetEmpLeaveDetails.setOnClickListener(btnView -> {
                bottomSheet.dismiss();
                showLeaveDetailsBottomSheet(employee, finalDisplayName);
            });

            // View Applied Leave [NEW]
            if (sheetEmpAppliedLeave != null) {
                sheetEmpAppliedLeave.setOnClickListener(btnView -> {
                    bottomSheet.dismiss();
                    fetchAndShowAppliedLeaves(employee, finalDisplayName);
                });
            }

            // View Timesheet [NEW]
            if (sheetEmpTimesheet != null) {
                sheetEmpTimesheet.setOnClickListener(btnView -> {
                    bottomSheet.dismiss();
                    fetchAndShowTimesheet(employee, finalDisplayName);
                });
            }

            bottomSheet.show();
        });
    }

    private void showEmployeeDetailsBottomSheet(EmployeesItem employee, String displayName) {
        BottomSheetDialog detailsSheet = new BottomSheetDialog(context);
        View sheetView = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_employee_details, null);
        detailsSheet.setContentView(sheetView);

        // ===== HEADER =====
        ShapeableImageView profileImage = sheetView.findViewById(R.id.detailsProfileImage);
        TextView profileInitials = sheetView.findViewById(R.id.detailsProfileInitials);
        MaterialTextView tvFullName = sheetView.findViewById(R.id.detailsFullName);
        MaterialTextView tvDesignationHeader = sheetView.findViewById(R.id.detailsDesignation);
        Chip chipStatus = sheetView.findViewById(R.id.detailsStatusChip);
        MaterialTextView tvEmpCodeBadge = sheetView.findViewById(R.id.detailsEmployeeCodeBadge);

        tvFullName.setText(displayName);
        tvDesignationHeader.setText(safe(employee.getDesignation()));

        String empCode = employee.getEmployeeCode();
        tvEmpCodeBadge.setText((empCode != null && !empCode.trim().isEmpty()) ? empCode : "N/A");

        if (employee.isActive()) {
            chipStatus.setText("Active");
            chipStatus.setChipBackgroundColorResource(R.color.active_green);
        } else {
            chipStatus.setText("Inactive");
            chipStatus.setChipBackgroundColorResource(R.color.inactive_gray);
        }

        String profileUrl = (String) employee.getProfileImageUrl();
        if (profileUrl != null && !profileUrl.trim().isEmpty()) {
            profileImage.setVisibility(View.VISIBLE);
            profileInitials.setVisibility(View.GONE);
            Glide.with(context)
                    .load(profileUrl)
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                        @Override
                        public boolean onLoadFailed(@androidx.annotation.Nullable com.bumptech.glide.load.engine.GlideException e, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, boolean isFirstResource) {
                            profileImage.setVisibility(View.GONE);
                            profileInitials.setVisibility(View.VISIBLE);
                            profileInitials.setText(getInitials(displayName));
                            GradientDrawable bg = new GradientDrawable();
                            bg.setShape(GradientDrawable.OVAL);
                            bg.setColor(getAvatarColor(displayName));
                            profileInitials.setBackground(bg);
                            return true;
                        }

                        @Override
                        public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                            return false;
                        }
                    })
                    .into(profileImage);
        } else {
            profileImage.setVisibility(View.GONE);
            profileInitials.setVisibility(View.VISIBLE);
            profileInitials.setText(getInitials(displayName));
            GradientDrawable bg = new GradientDrawable();
            bg.setShape(GradientDrawable.OVAL);
            bg.setColor(getAvatarColor(displayName));
            profileInitials.setBackground(bg);
        }

        ((MaterialTextView) sheetView.findViewById(R.id.detailsField_fullName)).setText(displayName);
        ((MaterialTextView) sheetView.findViewById(R.id.detailsField_fatherName)).setText(safe(employee.getFatherName()));
        ((MaterialTextView) sheetView.findViewById(R.id.detailsField_dob)).setText(safeDate(employee.getDateOfBirth()));
        ((MaterialTextView) sheetView.findViewById(R.id.detailsField_contact)).setText(safe(employee.getContact()));
        ((MaterialTextView) sheetView.findViewById(R.id.detailsField_email)).setText(safe(employee.getEmail()));
        ((MaterialTextView) sheetView.findViewById(R.id.detailsField_address)).setText(safe(employee.getAddress()));
        ((MaterialTextView) sheetView.findViewById(R.id.detailsField_state)).setText(safe(employee.getState()));
        ((MaterialTextView) sheetView.findViewById(R.id.detailsField_pincode)).setText(safe(employee.getPincode()));

        ((MaterialTextView) sheetView.findViewById(R.id.detailsField_empCode)).setText(safe(employee.getEmployeeCode()));
        ((MaterialTextView) sheetView.findViewById(R.id.detailsField_designation)).setText(safe(employee.getDesignation()));

        String deptName = (employee.getDepartment() != null) ? employee.getDepartment().getName() : null;
        ((MaterialTextView) sheetView.findViewById(R.id.detailsField_department)).setText(safe(deptName));

        ((MaterialTextView) sheetView.findViewById(R.id.detailsField_level)).setText(safe(employee.getLevel()));
        ((MaterialTextView) sheetView.findViewById(R.id.detailsField_grade)).setText(safe(employee.getGrade()));
        ((MaterialTextView) sheetView.findViewById(R.id.detailsField_empType)).setText(safe(employee.getEmployeeType()));
        ((MaterialTextView) sheetView.findViewById(R.id.detailsField_joiningType)).setText(safe(employee.getJoiningType()));
        ((MaterialTextView) sheetView.findViewById(R.id.detailsField_joiningDate)).setText(safeDate(employee.getJoiningDate()));

        String shiftText = "N/A";
        if (employee.getShift() != null
                && employee.getShift().getName() != null && !employee.getShift().getName().trim().isEmpty()) {
            String sName = employee.getShift().getName().trim();
            String sStart = employee.getShift().getStartTime();
            String sEnd = employee.getShift().getEndTime();
            if (sStart != null && !sStart.trim().isEmpty() && sEnd != null && !sEnd.trim().isEmpty()) {
                shiftText = sName + " (" + sStart.trim() + " - " + sEnd.trim() + ")";
            } else {
                shiftText = sName;
            }
        }
        ((MaterialTextView) sheetView.findViewById(R.id.detailsField_shift)).setText(shiftText);

        ((MaterialTextView) sheetView.findViewById(R.id.detailsField_status)).setText(employee.isActive() ? "Active" : "Inactive");

        String reportingMgr = "N/A";
        if (employee.getReportingManager() != null) {
            String rFirst = null, rLast = null;
            try {
                rFirst = (String) employee.getReportingManager().getClass().getMethod("getFirstname").invoke(employee.getReportingManager());
                rLast = (String) employee.getReportingManager().getClass().getMethod("getLastname").invoke(employee.getReportingManager());
            } catch (Exception ignored) {
            }
            StringBuilder rb = new StringBuilder();
            if (rFirst != null && !rFirst.trim().isEmpty()) rb.append(rFirst.trim());
            if (rLast != null && !rLast.trim().isEmpty()) {
                if (rb.length() > 0) rb.append(" ");
                rb.append(rLast.trim());
            }
            if (rb.length() > 0) reportingMgr = rb.toString();
        }
        ((MaterialTextView) sheetView.findViewById(R.id.detailsField_reportingManager)).setText(reportingMgr);

        String crossMgr = "N/A";
        if (employee.getCrossmanager() != null) {
            String cFirst = null, cLast = null;
            try {
                cFirst = (String) employee.getCrossmanager().getClass().getMethod("getFirstname").invoke(employee.getCrossmanager());
                cLast = (String) employee.getCrossmanager().getClass().getMethod("getLastname").invoke(employee.getCrossmanager());
            } catch (Exception ignored) {
            }
            StringBuilder cb = new StringBuilder();
            if (cFirst != null && !cFirst.trim().isEmpty()) cb.append(cFirst.trim());
            if (cLast != null && !cLast.trim().isEmpty()) {
                if (cb.length() > 0) cb.append(" ");
                cb.append(cLast.trim());
            }
            if (cb.length() > 0) crossMgr = cb.toString();
        }
        ((MaterialTextView) sheetView.findViewById(R.id.detailsField_crossManager)).setText(crossMgr);

        ((MaterialTextView) sheetView.findViewById(R.id.detailsField_pan)).setText(safe(employee.getPanNo()));
        ((MaterialTextView) sheetView.findViewById(R.id.detailsField_aadhar)).setText(safe(employee.getAdharNo()));
        ((MaterialTextView) sheetView.findViewById(R.id.detailsField_uan)).setText(safe(employee.getUanno()));
        ((MaterialTextView) sheetView.findViewById(R.id.detailsField_pf)).setText(safe(employee.getPfAccountNo()));
        ((MaterialTextView) sheetView.findViewById(R.id.detailsField_epf)).setText(employee.isEpf() ? "Yes" : "No");
        ((MaterialTextView) sheetView.findViewById(R.id.detailsField_esi)).setText(employee.isEsi() ? "Yes" : "No");
        ((MaterialTextView) sheetView.findViewById(R.id.detailsField_differentlyAbled)).setText(safe(employee.getDifferentlyAbled()));
        ((MaterialTextView) sheetView.findViewById(R.id.detailsField_verified)).setText(employee.isIsVerified() ? "Yes" : "No");
        ((MaterialTextView) sheetView.findViewById(R.id.detailsField_hrAdmin)).setText(employee.isIsHROrAdmin() ? "Yes" : "No");

        ((MaterialTextView) sheetView.findViewById(R.id.detailsField_createdAt)).setText(safeDateTime(employee.getCreatedAt()));
        ((MaterialTextView) sheetView.findViewById(R.id.detailsField_updatedAt)).setText(safeDateTime(employee.getUpdatedAt()));

        detailsSheet.show();
    }

    private void showLeaveDetailsBottomSheet(EmployeesItem employee, String displayName) {
        if (employee == null || employee.getId() == null || employee.getId().trim().isEmpty()) {
            new CustomAlertDialog(context)
                    .setType(CustomAlertDialog.Type.ERROR)
                    .setTitle("Error")
                    .setMessage("Employee ID is missing.")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        SecurePrefManager prefManager = SecurePrefManager.getInstance(context);
        String token = prefManager.getString("authToken", "");

        AdminAPIInterface apiService = AdminAPIClient.getInstance().getBase2();
        Call<EmployeeLeaveResponse> call = apiService.getLeavesEmployee("jwt " + token, employee.getId());

        call.enqueue(new Callback<EmployeeLeaveResponse>() {
            @Override
            public void onResponse(@NonNull Call<EmployeeLeaveResponse> call, @NonNull Response<EmployeeLeaveResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<LeavesItem> leaves = null;
                    if (response.body().getData() != null) {
                        leaves = response.body().getData().getLeaves();
                    }
                    renderLeaveDetailsSheet(employee, displayName, leaves);
                } else {
                    new CustomAlertDialog(context)
                            .setType(CustomAlertDialog.Type.ERROR)
                            .setTitle("Error")
                            .setMessage("Failed to load leave details. Server returned code: " + response.code())
                            .setPositiveButton("OK", null)
                            .show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<EmployeeLeaveResponse> call, @NonNull Throwable t) {
                new CustomAlertDialog(context)
                        .setType(CustomAlertDialog.Type.ERROR)
                        .setTitle("Network Error")
                        .setMessage("Could not load leave details: " + t.getMessage())
                        .setPositiveButton("OK", null)
                        .show();
            }
        });
    }

    private void renderLeaveDetailsSheet(EmployeesItem employee, String displayName, List<LeavesItem> leaves) {
        BottomSheetDialog sheet = new BottomSheetDialog(context);
        View sheetView = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_leave_details, null);
        sheet.setContentView(sheetView);

        MaterialTextView tvEmpName = sheetView.findViewById(R.id.leaveDetailsEmployeeName);
        MaterialTextView tvEmpCodeBadge = sheetView.findViewById(R.id.leaveDetailsEmployeeCodeBadge);
        View loadingView = sheetView.findViewById(R.id.leaveDetailsLoadingView);
        View emptyView = sheetView.findViewById(R.id.leaveDetailsEmptyView);
        ViewGroup container = sheetView.findViewById(R.id.leaveDetailsContainer);

        tvEmpName.setText(displayName);
        String empCode = employee.getEmployeeCode();
        tvEmpCodeBadge.setText((empCode != null && !empCode.trim().isEmpty()) ? empCode : "N/A");

        // Hide loading immediately since data is already fetched before showing sheet
        loadingView.setVisibility(View.GONE);

        if (leaves == null || leaves.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            container.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            container.setVisibility(View.VISIBLE);
            container.removeAllViews();

            LayoutInflater inflater = LayoutInflater.from(context);
            for (LeavesItem item : leaves) {
                if (item == null) continue;

                View card = inflater.inflate(R.layout.item_leave_detail_card, container, false);

                View accentStrip = card.findViewById(R.id.leaveAccentStrip);
                MaterialTextView tvLeaveType = card.findViewById(R.id.leaveTypeName);
                MaterialTextView tvAssignDate = card.findViewById(R.id.leaveAssignDate);
                MaterialTextView tvBalance = card.findViewById(R.id.leaveBalance);
                MaterialTextView tvOpening = card.findViewById(R.id.leaveOpening);
                MaterialTextView tvCredit = card.findViewById(R.id.leaveCredit);
                MaterialTextView tvUsed = card.findViewById(R.id.leaveUsed);
                MaterialTextView tvDebit = card.findViewById(R.id.leaveDebit);
                MaterialTextView tvApplied = card.findViewById(R.id.leaveApplied);

                String leaveTypeName = item.getLeavetype() != null ? item.getLeavetype().trim() : "N/A";
                tvLeaveType.setText(leaveTypeName);

                int accentColor = getLeaveTypeColor(leaveTypeName);
                accentStrip.setBackgroundColor(accentColor);
                tvLeaveType.setTextColor(accentColor);

                // Assign date
                tvAssignDate.setText(safeDate(item.getAssignDate()));

                // Stats
                double opening = item.getOpeningLeave();
                double credit = item.getCreditLeave();
                double debit = item.getDebitLeave();
                double used = item.getUsedLeave();
                double applied = item.getAppliedLeave();

                tvOpening.setText(String.valueOf(opening));
                tvCredit.setText(String.valueOf(credit));
                tvUsed.setText(String.valueOf(used));
                tvDebit.setText(String.valueOf(debit));
                tvApplied.setText(String.valueOf(applied));

                // Balance
                double balance = (opening + credit) - (debit + used + applied);
                if (balance == Math.floor(balance)) {
                    tvBalance.setText(String.valueOf((long) balance));
                } else {
                    tvBalance.setText(String.valueOf(balance));
                }
                tvBalance.setTextColor(getBalanceColor(balance));

                container.addView(card);
            }
        }

        sheet.show();
    }

    // ========================= NEW: View Applied Leaves =========================

    private void fetchAndShowAppliedLeaves(EmployeesItem employee, String displayName) {
        if (employee == null || employee.getId() == null || employee.getId().trim().isEmpty()) {
            new CustomAlertDialog(context)
                    .setType(CustomAlertDialog.Type.ERROR)
                    .setTitle("Error")
                    .setMessage("Employee ID is missing.")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        SecurePrefManager prefManager = SecurePrefManager.getInstance(context);
        String token = prefManager.getString("authToken", "");

        AdminAPIInterface apiService = AdminAPIClient.getInstance().getBase2();
        Call<EmployeeAppliedLeaveResponse> call = apiService.getLeavesApplied("jwt " + token, employee.getId());

        call.enqueue(new Callback<EmployeeAppliedLeaveResponse>() {
            @Override
            public void onResponse(@NonNull Call<EmployeeAppliedLeaveResponse> call, @NonNull Response<EmployeeAppliedLeaveResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<AppliedLeavesItem> appliedLeaves = null;
                    if (response.body().getData() != null) {
                        appliedLeaves = response.body().getData().getAppliedLeaves();
                    }
                    renderAppliedLeavesSheet(employee, displayName, appliedLeaves);
                } else {
                    new CustomAlertDialog(context)
                            .setType(CustomAlertDialog.Type.ERROR)
                            .setTitle("Error")
                            .setMessage("Failed to load applied leaves. Server returned code: " + response.code())
                            .setPositiveButton("OK", null)
                            .show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<EmployeeAppliedLeaveResponse> call, @NonNull Throwable t) {
                new CustomAlertDialog(context)
                        .setType(CustomAlertDialog.Type.ERROR)
                        .setTitle("Network Error")
                        .setMessage("Could not load applied leaves: " + t.getMessage())
                        .setPositiveButton("OK", null)
                        .show();
            }
        });
    }

    private void renderAppliedLeavesSheet(EmployeesItem employee, String displayName, List<AppliedLeavesItem> appliedLeaves) {
        BottomSheetDialog sheet = new BottomSheetDialog(context);
        View sheetView = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_applied_leave, null);
        sheet.setContentView(sheetView);

        MaterialTextView tvEmpName = sheetView.findViewById(R.id.appliedLeaveEmployeeName);
        MaterialTextView tvEmpCodeBadge = sheetView.findViewById(R.id.appliedLeaveEmployeeCodeBadge);
        View emptyView = sheetView.findViewById(R.id.appliedLeaveEmptyView);
        ViewGroup container = sheetView.findViewById(R.id.appliedLeaveContainer);

        tvEmpName.setText(displayName != null && !displayName.trim().isEmpty() ? displayName : "N/A");
        String empCode = (employee != null) ? employee.getEmployeeCode() : null;
        tvEmpCodeBadge.setText((empCode != null && !empCode.trim().isEmpty()) ? empCode : "N/A");

        if (appliedLeaves == null || appliedLeaves.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            container.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            container.setVisibility(View.VISIBLE);
            container.removeAllViews();

            LayoutInflater inflater = LayoutInflater.from(context);
            for (AppliedLeavesItem item : appliedLeaves) {
                if (item == null) continue;

                View card = inflater.inflate(R.layout.item_applied_leave_card, container, false);

                View accentStrip = card.findViewById(R.id.appliedLeaveAccentStrip);
                MaterialTextView tvLeaveType = card.findViewById(R.id.appliedLeaveTypeName);
                MaterialTextView tvStatus = card.findViewById(R.id.appliedLeaveStatusBadge);
                MaterialTextView tvPlanned = card.findViewById(R.id.appliedLeavePlannedBadge);
                MaterialTextView tvFromDate = card.findViewById(R.id.appliedLeaveFromDate);
                MaterialTextView tvToDate = card.findViewById(R.id.appliedLeaveToDate);
                View shortLeaveSection = card.findViewById(R.id.appliedLeaveShortLeaveSection);
                MaterialTextView tvShortStart = card.findViewById(R.id.appliedLeaveShortStart);
                MaterialTextView tvShortEnd = card.findViewById(R.id.appliedLeaveShortEnd);
                View reasonSection = card.findViewById(R.id.appliedLeaveReasonSection);
                MaterialTextView tvReason = card.findViewById(R.id.appliedLeaveReason);
                MaterialTextView tvAppliedDate = card.findViewById(R.id.appliedLeaveAppliedDate);
                MaterialTextView tvReportingMgr = card.findViewById(R.id.appliedLeaveReportingManager);
                MaterialTextView tvCrossMgr = card.findViewById(R.id.appliedLeaveCrossManager);
                View approvalSection = card.findViewById(R.id.appliedLeaveApprovalSection);
                MaterialTextView tvApprovedBy = card.findViewById(R.id.appliedLeaveApprovedBy);
                MaterialTextView tvApprovedDate = card.findViewById(R.id.appliedLeaveApprovedDate);
                View commentSection = card.findViewById(R.id.appliedLeaveCommentSection);
                MaterialTextView tvComment = card.findViewById(R.id.appliedLeaveComment);

                // Leave type name
                String leaveName = item.getLeaveName();
                if (leaveName == null || leaveName.trim().isEmpty()) {
                    if (item.getLeavetype() != null && item.getLeavetype().getLeavetypeName() != null
                            && !item.getLeavetype().getLeavetypeName().trim().isEmpty()) {
                        leaveName = item.getLeavetype().getLeavetypeName().trim();
                    } else {
                        leaveName = "N/A";
                    }
                }
                tvLeaveType.setText(leaveName);

                // Status badge
                String status = item.getStatus();
                if (status == null || status.trim().isEmpty()) {
                    status = "Pending";
                }
                tvStatus.setText(capitalizeFirst(status));
                int statusColor = getStatusColor(status);
                accentStrip.setBackgroundColor(statusColor);
                if (tvStatus.getBackground() instanceof GradientDrawable) {
                    ((GradientDrawable) tvStatus.getBackground().mutate()).setColor(statusColor);
                } else {
                    GradientDrawable bg = new GradientDrawable();
                    bg.setShape(GradientDrawable.RECTANGLE);
                    bg.setCornerRadius(dpToPx(12));
                    bg.setColor(statusColor);
                    tvStatus.setBackground(bg);
                }

                // Planned badge
                String planned = item.getLeavePlanned();
                if (planned != null && !planned.trim().isEmpty()) {
                    tvPlanned.setVisibility(View.VISIBLE);
                    tvPlanned.setText(planned.trim().toUpperCase());
                } else {
                    tvPlanned.setVisibility(View.GONE);
                }

                // From date
                tvFromDate.setText(safeDate(item.getFromDate()));

                // To date (may be Object or null)
                String toDateStr = null;
                Object toDateObj = item.getToDate();
                if (toDateObj instanceof String) {
                    toDateStr = (String) toDateObj;
                }
                if (toDateStr != null && !toDateStr.trim().isEmpty()) {
                    tvToDate.setText(safeDate(toDateStr));
                } else {
                    tvToDate.setText(safeDate(item.getFromDate()));
                }

                // Short leave times
                String shortStart = item.getShortLeaveStartTime();
                String shortEnd = item.getShortLeaveEndTime();
                boolean hasShortTimes = (shortStart != null && !shortStart.trim().isEmpty())
                        || (shortEnd != null && !shortEnd.trim().isEmpty());
                if (hasShortTimes) {
                    shortLeaveSection.setVisibility(View.VISIBLE);
                    tvShortStart.setText((shortStart != null && !shortStart.trim().isEmpty()) ? shortStart.trim() : "N/A");
                    tvShortEnd.setText((shortEnd != null && !shortEnd.trim().isEmpty()) ? shortEnd.trim() : "N/A");
                } else {
                    shortLeaveSection.setVisibility(View.GONE);
                }

                // Reason
                String reason = item.getReason();
                if (reason != null && !reason.trim().isEmpty()) {
                    reasonSection.setVisibility(View.VISIBLE);
                    tvReason.setText(reason.trim());
                } else {
                    reasonSection.setVisibility(View.GONE);
                }

                // Applied date
                tvAppliedDate.setText(safeDateTime(item.getAppliedDate()));

                // Reporting manager
                String rName = item.getReportingManagerName();
                String rLast = item.getReportingManagerLastName();
                StringBuilder rb = new StringBuilder();
                if (rName != null && !rName.trim().isEmpty()) rb.append(rName.trim());
                if (rLast != null && !rLast.trim().isEmpty()) {
                    if (rb.length() > 0) rb.append(" ");
                    rb.append(rLast.trim());
                }
                tvReportingMgr.setText(rb.length() > 0 ? rb.toString() : "N/A");

                // Cross manager
                String cName = item.getCrossManagerName();
                tvCrossMgr.setText((cName != null && !cName.trim().isEmpty()) ? cName.trim() : "N/A");

                // Approval section
                String approvedBy = item.getApprovedByName();
                String approvedDateRaw = item.getApprovedDate();
                boolean validApprovedDate = approvedDateRaw != null
                        && !approvedDateRaw.trim().isEmpty()
                        && !approvedDateRaw.startsWith("1900");
                boolean hasApprover = approvedBy != null && !approvedBy.trim().isEmpty();

                if (hasApprover || validApprovedDate) {
                    approvalSection.setVisibility(View.VISIBLE);
                    tvApprovedBy.setText(hasApprover ? approvedBy.trim() : "N/A");
                    tvApprovedDate.setText(validApprovedDate ? safeDate(approvedDateRaw) : "N/A");

                    String comment = item.getComment();
                    if (comment != null && !comment.trim().isEmpty()) {
                        commentSection.setVisibility(View.VISIBLE);
                        tvComment.setText(comment.trim());
                    } else {
                        commentSection.setVisibility(View.GONE);
                    }
                } else {
                    approvalSection.setVisibility(View.GONE);
                }

                container.addView(card);
            }
        }

        sheet.show();
    }

    // ========================= NEW: View Timesheet =========================

    private void fetchAndShowTimesheet(EmployeesItem employee, String displayName) {
        if (employee == null || employee.getId() == null || employee.getId().trim().isEmpty()) {
            new CustomAlertDialog(context)
                    .setType(CustomAlertDialog.Type.ERROR)
                    .setTitle("Error")
                    .setMessage("Employee ID is missing.")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        SecurePrefManager prefManager = SecurePrefManager.getInstance(context);
        String token = prefManager.getString("authToken", "");

        AdminAPIInterface apiService = AdminAPIClient.getInstance().getBase2();
        Call<EmployeeTimesheetResponse> call = apiService.getTimesheetData("jwt " + token, employee.getId());

        call.enqueue(new Callback<EmployeeTimesheetResponse>() {
            @Override
            public void onResponse(@NonNull Call<EmployeeTimesheetResponse> call, @NonNull Response<EmployeeTimesheetResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<EmployeeDcrItem> dcrList = null;
                    if (response.body().getData() != null) {
                        dcrList = response.body().getData().getEmployeeDcr();
                    }
                    renderTimesheetSheet(employee, displayName, dcrList);
                } else {
                    new CustomAlertDialog(context)
                            .setType(CustomAlertDialog.Type.ERROR)
                            .setTitle("Error")
                            .setMessage("Failed to load timesheet. Server returned code: " + response.code())
                            .setPositiveButton("OK", null)
                            .show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<EmployeeTimesheetResponse> call, @NonNull Throwable t) {
                new CustomAlertDialog(context)
                        .setType(CustomAlertDialog.Type.ERROR)
                        .setTitle("Network Error")
                        .setMessage("Could not load timesheet: " + t.getMessage())
                        .setPositiveButton("OK", null)
                        .show();
            }
        });
    }

    private void renderTimesheetSheet(EmployeesItem employee, String displayName, List<EmployeeDcrItem> dcrList) {
        BottomSheetDialog sheet = new BottomSheetDialog(context);
        View sheetView = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_timesheet, null);
        sheet.setContentView(sheetView);

        MaterialTextView tvEmpName = sheetView.findViewById(R.id.timesheetEmployeeName);
        MaterialTextView tvEmpCodeBadge = sheetView.findViewById(R.id.timesheetEmployeeCodeBadge);
        View emptyView = sheetView.findViewById(R.id.timesheetEmptyView);
        ViewGroup container = sheetView.findViewById(R.id.timesheetContainer);

        tvEmpName.setText(displayName != null && !displayName.trim().isEmpty() ? displayName : "N/A");
        String empCode = (employee != null) ? employee.getEmployeeCode() : null;
        tvEmpCodeBadge.setText((empCode != null && !empCode.trim().isEmpty()) ? empCode : "N/A");

        if (dcrList == null || dcrList.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            container.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            container.setVisibility(View.VISIBLE);
            container.removeAllViews();

            LayoutInflater inflater = LayoutInflater.from(context);
            for (EmployeeDcrItem item : dcrList) {
                if (item == null) continue;

                View card = inflater.inflate(R.layout.item_timesheet_card, container, false);

                MaterialTextView tvDate = card.findViewById(R.id.timesheetDate);
                MaterialTextView tvDurationBadge = card.findViewById(R.id.timesheetDurationBadge);
                MaterialTextView tvInTime = card.findViewById(R.id.timesheetInTime);
                MaterialTextView tvOutTime = card.findViewById(R.id.timesheetOutTime);
                View reportSection = card.findViewById(R.id.timesheetReportSection);
                MaterialTextView tvTodayReport = card.findViewById(R.id.timesheetTodayReport);
                View outcomeSection = card.findViewById(R.id.timesheetOutcomeSection);
                MaterialTextView tvOutcome = card.findViewById(R.id.timesheetOutcome);
                View feelingSection = card.findViewById(R.id.timesheetFeelingSection);
                MaterialTextView tvFeeling = card.findViewById(R.id.timesheetFeeling);
                View planSection = card.findViewById(R.id.timesheetPlanSection);
                MaterialTextView tvPlan = card.findViewById(R.id.timesheetTomorrowPlan);
                MaterialTextView tvReportingMgr = card.findViewById(R.id.timesheetReportingManager);

                // DCR date
                tvDate.setText(safeDate(item.getDcrDate()));

                // In/Out time (ISO datetime -> extract time)
                String inRaw = item.getInTime();
                String outRaw = item.getOutTime();
                String inTimeStr = extractTimeSafe(inRaw);
                String outTimeStr = extractTimeSafe(outRaw);
                tvInTime.setText(inTimeStr);
                tvOutTime.setText(outTimeStr);

                // Duration badge (best-effort; if both are present and parseable)
                String duration = computeDuration(inRaw, outRaw);
                if (duration != null && !duration.isEmpty()) {
                    tvDurationBadge.setVisibility(View.VISIBLE);
                    tvDurationBadge.setText(duration);
                } else {
                    tvDurationBadge.setVisibility(View.GONE);
                }

                // Today's report (may contain HTML)
                String todayReport = item.getTodayReport();
                if (todayReport != null && !todayReport.trim().isEmpty()) {
                    reportSection.setVisibility(View.VISIBLE);
                    tvTodayReport.setText(safeHtml(todayReport));
                } else {
                    reportSection.setVisibility(View.GONE);
                }

                // Outcome (may contain HTML)
                String outcome = item.getOutcome();
                if (outcome != null && !outcome.trim().isEmpty()) {
                    outcomeSection.setVisibility(View.VISIBLE);
                    tvOutcome.setText(safeHtml(outcome));
                } else {
                    outcomeSection.setVisibility(View.GONE);
                }

                // Today's feeling
                String feeling = item.getTodayFeeling();
                if (feeling != null && !feeling.trim().isEmpty()) {
                    feelingSection.setVisibility(View.VISIBLE);
                    tvFeeling.setText(safeHtml(feeling));
                } else {
                    feelingSection.setVisibility(View.GONE);
                }

                // Tomorrow's plan (may contain HTML)
                String plan = item.getTommarowPlan();
                if (plan != null && !plan.trim().isEmpty()) {
                    planSection.setVisibility(View.VISIBLE);
                    tvPlan.setText(safeHtml(plan));
                } else {
                    planSection.setVisibility(View.GONE);
                }

                // Reporting manager
                String rFirst = item.getReportingManagerFirstName();
                String rLast = item.getReportingManagerLastName();
                StringBuilder rb = new StringBuilder();
                if (rFirst != null && !rFirst.trim().isEmpty() && !"NA".equalsIgnoreCase(rFirst.trim())) {
                    rb.append(rFirst.trim());
                }
                if (rLast != null && !rLast.trim().isEmpty() && !"NA".equalsIgnoreCase(rLast.trim())) {
                    if (rb.length() > 0) rb.append(" ");
                    rb.append(rLast.trim());
                }
                tvReportingMgr.setText(rb.length() > 0 ? rb.toString() : "N/A");

                container.addView(card);
            }
        }

        sheet.show();
    }

    // ========================= Helpers (new) =========================

    private int getStatusColor(String status) {
        int colorRes;
        if (status == null) {
            colorRes = R.color.default_status_color;
        } else {
            String s = status.trim().toLowerCase();
            if (s.equals("approved")) {
                colorRes = R.color.status_approved;
            } else if (s.equals("rejected")) {
                colorRes = R.color.status_rejected;
            } else if (s.equals("cancelled") || s.equals("canceled")) {
                colorRes = R.color.default_status_color;
            } else if (s.equals("unapproved") || s.equals("pending")) {
                colorRes = R.color.status_pending;
            } else {
                colorRes = R.color.default_status_color;
            }
        }
        return androidx.core.content.ContextCompat.getColor(context, colorRes);
    }

    private CharSequence safeHtml(String html) {
        if (html == null) return "";
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                return Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT).toString().trim();
            } else {
                return Html.fromHtml(html).toString().trim();
            }
        } catch (Exception e) {
            return html;
        }
    }

    private String extractTimeSafe(String isoString) {
        if (isoString == null || isoString.trim().isEmpty()) return "N/A";
        try {
            String t = DateTimeUtils.extractTime(isoString.trim());
            if (t == null || t.trim().isEmpty()) return "N/A";
            return t;
        } catch (Exception e) {
            return "N/A";
        }
    }

    private String computeDuration(String inIso, String outIso) {
        if (inIso == null || outIso == null) return null;
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US);
            sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
            java.util.Date inDate = sdf.parse(inIso);
            java.util.Date outDate = sdf.parse(outIso);
            if (inDate == null || outDate == null) return null;
            long diffMs = outDate.getTime() - inDate.getTime();
            if (diffMs <= 0) return null;
            long totalMinutes = diffMs / (60 * 1000);
            long hours = totalMinutes / 60;
            long mins = totalMinutes % 60;
            return hours + "h " + String.format(java.util.Locale.US, "%02d", mins) + "m";
        } catch (Exception e) {
            return null;
        }
    }

    private String capitalizeFirst(String s) {
        if (s == null || s.trim().isEmpty()) return "";
        String t = s.trim();
        return Character.toUpperCase(t.charAt(0)) + t.substring(1).toLowerCase();
    }

    private int dpToPx(int dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density);
    }

    // ========================= Existing helpers (unchanged) =========================

    private int getLeaveTypeColor(String leaveTypeName) {
        if (leaveTypeName == null) {
            return androidx.core.content.ContextCompat.getColor(context, R.color.accent_employee);
        }
        String n = leaveTypeName.trim().toLowerCase();
        int colorRes;
        if (n.contains("casual")) {
            colorRes = R.color.CasualLeave;
        } else if (n.contains("sick")) {
            colorRes = R.color.SickLeave;
        } else if (n.contains("privilege")) {
            colorRes = R.color.PrivilegeLeave;
        } else if (n.contains("occasional")) {
            colorRes = R.color.OccasionalLeave;
        } else if (n.contains("restricted")) {
            colorRes = R.color.RestrictedHoliday;
        } else if (n.contains("national")) {
            colorRes = R.color.NationalHoliday;
        } else if (n.contains("loss of pay") || n.contains("lossofpay")) {
            colorRes = R.color.LossOfPay;
        } else if (n.contains("leave without pay") || n.contains("leavewithoutpay")) {
            colorRes = R.color.LeaveWithoutPay;
        } else {
            colorRes = R.color.accent_employee;
        }
        return androidx.core.content.ContextCompat.getColor(context, colorRes);
    }

    private int getBalanceColor(double balance) {
        int colorRes;
        if (balance > 0) {
            colorRes = R.color.leave_balance_positive;
        } else if (balance == 0) {
            colorRes = R.color.leave_balance_zero;
        } else {
            colorRes = R.color.error_color;
        }
        return androidx.core.content.ContextCompat.getColor(context, colorRes);
    }

    private String safe(String value) {
        if (value == null || value.trim().isEmpty()) return "N/A";
        return value.trim();
    }

    private String safeDate(String isoString) {
        if (isoString == null || isoString.trim().isEmpty()) return "N/A";
        try {
            String formatted = DateTimeUtils.formatToReadableDate(isoString.trim());
            if (formatted == null || formatted.trim().isEmpty()) return "N/A";
            return formatted;
        } catch (Exception e) {
            return "N/A";
        }
    }

    private String safeDateTime(String isoString) {
        if (isoString == null || isoString.trim().isEmpty()) return "N/A";
        try {
            String datePart = DateTimeUtils.formatToReadableDate(isoString.trim());
            String timePart = DateTimeUtils.extractTime(isoString.trim());
            boolean hasDate = datePart != null && !datePart.trim().isEmpty();
            boolean hasTime = timePart != null && !timePart.trim().isEmpty() && !timePart.equalsIgnoreCase("N/A");
            if (hasDate && hasTime) return datePart + ", " + timePart;
            if (hasDate) return datePart;
            if (hasTime) return timePart;
            return "N/A";
        } catch (Exception e) {
            return "N/A";
        }
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
        return ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
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
package app.xedigital.ai.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewTreeLifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

import app.xedigital.ai.R;
import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.api.APIInterface;
import app.xedigital.ai.model.approveClaim.EmployeeClaimdataItem;
import app.xedigital.ai.model.updateAccommodationClaim.UpdateAccommodationRequest;
import app.xedigital.ai.model.updateFoodClaim.UpdateFoodRequest;
import app.xedigital.ai.model.updateMiscClaim.UpdateMiscRequest;
import app.xedigital.ai.model.updateTravelClaim.UpdateTravelRequest;
import app.xedigital.ai.ui.profile.ProfileViewModel;
import app.xedigital.ai.utills.CustomDialogHelper;
import app.xedigital.ai.utills.DateTimeUtils;
import app.xedigital.ai.utills.SecurePrefManager;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;

public class ApproveClaimAdapter extends RecyclerView.Adapter<ApproveClaimAdapter.ClaimViewHolder> {
    private List<EmployeeClaimdataItem> claimList;
    private OnClaimClickListener listener;

    public ApproveClaimAdapter(List<EmployeeClaimdataItem> claimList) {
        this.claimList = claimList;
    }

    @NonNull
    @Override
    public ClaimViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.approval_claim_item, parent, false);
        Context context = parent.getContext();
        return new ClaimViewHolder(view, context, listener, claimList);
    }

    @Override
    public void onBindViewHolder(@NonNull ClaimViewHolder holder, int position) {
        if (claimList != null && position < claimList.size()) {
            EmployeeClaimdataItem currentClaim = claimList.get(position);
            holder.bind(currentClaim, listener);
        }
    }

    @Override
    public int getItemCount() {
        return claimList != null ? claimList.size() : 0;
    }

    public void updateData(List<EmployeeClaimdataItem> filteredList) {
        this.claimList = filteredList;
        notifyDataSetChanged();
    }

    public void setOnClaimClickListener(OnClaimClickListener listener) {
        this.listener = listener;
    }

    public interface OnClaimClickListener {
        void onClaimClick(EmployeeClaimdataItem currentClaim);

        void onClaimStatusUpdated();
    }

    public class ClaimViewHolder extends RecyclerView.ViewHolder {
        private final Context context;
        private final OnClaimClickListener listener;
        private final List<EmployeeClaimdataItem> claimList;
        TextView txtClaimId, expenseTypeText, txtMeetingType, txtPurposeOfMeeting, txtTotalAmount, txtAppliedDate;
        TextView viewDetailsButton, actionButton;

        public ClaimViewHolder(@NonNull View itemView, Context context, OnClaimClickListener listener, List<EmployeeClaimdataItem> claimList) {
            super(itemView);
            this.context = context;
            this.listener = listener;
            this.claimList = claimList;
            txtClaimId = itemView.findViewById(R.id.claimIdText);
            expenseTypeText = itemView.findViewById(R.id.expenseTypeText);
            txtMeetingType = itemView.findViewById(R.id.meetingTypeText);
            txtPurposeOfMeeting = itemView.findViewById(R.id.purposeText);
            txtTotalAmount = itemView.findViewById(R.id.amountText);
            txtAppliedDate = itemView.findViewById(R.id.claimDateText);
            viewDetailsButton = itemView.findViewById(R.id.viewDetailsButton);
            actionButton = itemView.findViewById(R.id.actionButton);

            actionButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && claimList != null && position < claimList.size()) {
                    EmployeeClaimdataItem claim = claimList.get(position);
                    if (claim == null) return;
                    showActionDialog(claim);
                }
            });
        }

        // ════════════════════════════════════════════════════════
        //  ACTION DIALOG (Upgraded UI)
        // ════════════════════════════════════════════════════════

        private void showActionDialog(EmployeeClaimdataItem claim) {
            View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_claim_action, null);

            // Header
            TextView txtDialogClaimId = dialogView.findViewById(R.id.txtDialogClaimId);
            String claimIdText = claim.getClaimId() != null ? claim.getClaimId() : "N/A";
            txtDialogClaimId.setText("Claim ID: " + claimIdText);

            // Comment input
            TextInputLayout tilComment = dialogView.findViewById(R.id.tilComment);
            TextInputEditText etComment = dialogView.findViewById(R.id.etComment);

            // Buttons
            MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancel);
            MaterialButton btnReject = dialogView.findViewById(R.id.btnReject);
            MaterialButton btnApprove = dialogView.findViewById(R.id.btnApprove);

            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog);
            builder.setView(dialogView);
            builder.setCancelable(true);

            AlertDialog dialog = builder.create();
            dialogView.findViewById(R.id.btnCloseDialog).setOnClickListener(v -> dialog.dismiss());

            // ── Approve: no comment required ──
            btnApprove.setOnClickListener(v -> {
                tilComment.setError(null);
                String comment = etComment.getText() != null ? etComment.getText().toString().trim() : "";
                dialog.dismiss();
                updateClaimStatus(claim, "approved", comment);
            });

            // ── Reject: comment required ──
            btnReject.setOnClickListener(v -> {
                String comment = etComment.getText() != null ? etComment.getText().toString().trim() : "";
                if (comment.isEmpty()) {
                    tilComment.setError("Comment is required to reject");
                    etComment.requestFocus();
                    return;
                }
                tilComment.setError(null);
                dialog.dismiss();
                updateClaimStatus(claim, "rejected", comment);
            });

            // ── Cancel action: comment required ──
            btnCancel.setOnClickListener(v -> {
                String comment = etComment.getText() != null ? etComment.getText().toString().trim() : "";
                if (comment.isEmpty()) {
                    tilComment.setError("Comment is required to cancel");
                    etComment.requestFocus();
                    return;
                }
                tilComment.setError(null);
                dialog.dismiss();
                updateClaimStatus(claim, "cancelled", comment);
            });

            // ── Clear error when user starts typing ──
            etComment.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    boolean hasText = s != null && !s.toString().trim().isEmpty();
                    if (hasText) {
                        tilComment.setError(null);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });

            dialog.show();
        }
        // ════════════════════════════════════════════════════════
        //  BIND
        // ════════════════════════════════════════════════════════

        public void bind(EmployeeClaimdataItem currentClaim, OnClaimClickListener listener) {
            if (currentClaim == null) return;

            String claimId = currentClaim.getClaimId();
            txtClaimId.setText("Claim ID : " + (!TextUtils.isEmpty(claimId) ? claimId : "N/A"));

            String expenseType = currentClaim.getExpenseType();
            expenseTypeText.setText(!TextUtils.isEmpty(expenseType) ? expenseType : "N/A");

            String meetingType = currentClaim.getMeeting();
            txtMeetingType.setText(!TextUtils.isEmpty(meetingType) ? meetingType : "N/A");

            String purpose = currentClaim.getPerposeofmeet();
            txtPurposeOfMeeting.setText(!TextUtils.isEmpty(purpose) ? purpose : "N/A");

            String currency = currentClaim.getCurrency();
            String amount = String.valueOf(currentClaim.getTotalamount());
            if (!TextUtils.isEmpty(currency) && !TextUtils.isEmpty(amount)) {
                txtTotalAmount.setText(currency + " " + amount);
            } else if (!TextUtils.isEmpty(amount)) {
                txtTotalAmount.setText(amount);
            } else {
                txtTotalAmount.setText("N/A");
            }

            // Date formatting using DateTimeUtils
            String claimDateStr = currentClaim.getClaimDate();
            if (!TextUtils.isEmpty(claimDateStr)) {
                String formatted = DateTimeUtils.formatToReadableDate(claimDateStr);
                txtAppliedDate.setText(!TextUtils.isEmpty(formatted) ? formatted : claimDateStr);
            } else {
                txtAppliedDate.setText("N/A");
            }

            TextView statusText = itemView.findViewById(R.id.statusText);
            String status = currentClaim.getStatusRm() != null ? currentClaim.getStatusRm() : (currentClaim.getStatusHr() != null ? currentClaim.getStatusHr() : "Pending");
            if (statusText != null) {
                statusText.setText(status);
                Drawable background = statusText.getBackground();
                if (background instanceof GradientDrawable) {
                    GradientDrawable gradientDrawable = (GradientDrawable) background;
                    int backgroundColor;
                    if (status.equalsIgnoreCase("Unapproved") || status.equalsIgnoreCase("Pending")) {
                        backgroundColor = ContextCompat.getColor(context, R.color.pending_status_color);
                    } else if (status.equalsIgnoreCase("Approved")) {
                        backgroundColor = ContextCompat.getColor(context, R.color.approved_status_color);
                    } else if (status.equalsIgnoreCase("Rejected") || status.equalsIgnoreCase("Cancelled")) {
                        backgroundColor = ContextCompat.getColor(context, R.color.rejected_status_color);
                    } else {
                        backgroundColor = ContextCompat.getColor(context, R.color.default_status_color);
                    }
                    gradientDrawable.setColor(backgroundColor);
                }
            }

            if (actionButton != null) {
                if (status.equalsIgnoreCase("Unapproved")) {
                    actionButton.setVisibility(View.VISIBLE);
                } else {
                    actionButton.setVisibility(View.GONE);
                }
            }

            viewDetailsButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onClaimClick(currentClaim);
                }
            });
        }

        // ════════════════════════════════════════════════════════
        //  API CALL
        // ════════════════════════════════════════════════════════

        private void updateClaimStatus(EmployeeClaimdataItem claim, String status, String comment) {
            SecurePrefManager prefManager = SecurePrefManager.getInstance(context);
            String authToken = prefManager.getString("authToken", "");
            String userId = prefManager.getString("userId", "");
            String claimId = claim.getId();

            ProfileViewModel profileViewModel = new ViewModelProvider((FragmentActivity) context).get(ProfileViewModel.class);
            profileViewModel.storeLoginData(userId, authToken);
            profileViewModel.fetchUserProfile();

            final boolean[] processed = {false};
            profileViewModel.userProfile.observe(Objects.requireNonNull(ViewTreeLifecycleOwner.get(itemView)), userProfile -> {
                if (processed[0]) return;
                if (userProfile != null && userProfile.getData() != null && userProfile.getData().getEmployee() != null) {
                    processed[0] = true;
                    String reportingManagerFirstName = userProfile.getData().getEmployee().getFirstname();
                    String reportingManagerLastName = userProfile.getData().getEmployee().getLastname();
                    String reportingManagerId = userProfile.getData().getEmployee().getId();
                    if (reportingManagerFirstName != null && reportingManagerLastName != null && reportingManagerId != null) {
                        String reportingManager = reportingManagerFirstName + " " + reportingManagerLastName;
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                        String currentDate = dateFormat.format(new Date());

                        String expenseType = claim.getExpenseType();
                        Gson gson = new Gson();
                        String claimJson = gson.toJson(claim);

                        APIInterface apiInterface = APIClient.getInstance().ClaimUpdateStatus();
                        Call<ResponseBody> call = null;

                        if ("travel".equals(expenseType)) {
                            UpdateTravelRequest request = gson.fromJson(claimJson, UpdateTravelRequest.class);
                            request.setStatus(status);
                            request.setStatusRm(status);
                            request.setComment(comment);
                            request.setApprovedByName(reportingManager);
                            request.setApprovedDate(currentDate);
                            call = apiInterface.updateTravelClaimStatus("jwt " + authToken, claimId, request);

                        } else if ("accommodation".equals(expenseType)) {
                            UpdateAccommodationRequest request = gson.fromJson(claimJson, UpdateAccommodationRequest.class);
                            request.setStatus(status);
                            request.setStatusRm(status);
                            request.setComment(comment);
                            request.setApprovedByName(reportingManager);
                            request.setApprovedDate(currentDate);
                            call = apiInterface.updateAccommodationClaimStatus("jwt " + authToken, claimId, request);

                        } else if ("food".equals(expenseType)) {
                            UpdateFoodRequest request = gson.fromJson(claimJson, UpdateFoodRequest.class);
                            request.setStatus(status);
                            request.setStatusRm(status);
                            request.setComment(comment);
                            request.setApprovedByName(reportingManager);
                            request.setApprovedDate(currentDate);
                            call = apiInterface.updateFoodClaimStatus("jwt " + authToken, claimId, request);

                        } else if ("miscellaneous".equals(expenseType)) {
                            UpdateMiscRequest request = gson.fromJson(claimJson, UpdateMiscRequest.class);
                            request.setStatus(status);
                            request.setStatusRm(status);
                            request.setComment(comment);
                            request.setApprovedByName(reportingManager);
                            request.setApprovedDate(currentDate);
                            call = apiInterface.updateMiscClaimStatus("jwt " + authToken, claimId, request);

                        } else {
                            Log.e("ClaimViewHolder", "Unknown expense type: " + expenseType);
                            CustomDialogHelper.showErrorDialog(context, "Error", "Unknown expense type: " + expenseType);
                            return;
                        }

                        call.enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull retrofit2.Response<ResponseBody> response) {
                                if (response.isSuccessful()) {
                                    claim.setStatusRm(status);
                                    int pos = getAdapterPosition();
                                    if (pos != RecyclerView.NO_POSITION) {
                                        notifyItemChanged(pos);
                                    }
                                    CustomDialogHelper.showSuccessDialog(context, "Success", "Claim " + status + " successfully");
                                    if (listener != null) {
                                        listener.onClaimStatusUpdated();
                                    }
                                } else {
                                    Log.e("ClaimViewHolder", "Error updating claim status: " + response.message());
                                    CustomDialogHelper.showErrorDialog(context, "Error", "Failed to update claim status");
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                                Log.e("ClaimViewHolder", "Error updating claim status: " + t.getMessage());
                                CustomDialogHelper.showErrorDialog(context, "Network Error", t.getMessage());
                            }
                        });
                    } else {
                        Log.w("ClaimViewHolder", "Reporting manager information not available");
                    }
                } else {
                    Log.w("ClaimViewHolder", "User profile or employee data is null");
                }
            });
        }
    }
}
package app.xedigital.ai.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewTreeLifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import app.xedigital.ai.R;
import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.api.APIInterface;
import app.xedigital.ai.model.approveClaim.EmployeeClaimdataItem;
import app.xedigital.ai.ui.profile.ProfileViewModel;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;

public class ApproveClaimAdapter extends RecyclerView.Adapter<ApproveClaimAdapter.ClaimViewHolder> {
    private List<EmployeeClaimdataItem> claimList;
    private OnClaimClickListener listener;

    public ApproveClaimAdapter(List<EmployeeClaimdataItem> claimList) {
        this.claimList = claimList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ClaimViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.claim_item, parent, false);
        Context context = parent.getContext();
        return new ClaimViewHolder(view, context, listener, claimList);
    }

    @Override
    public void onBindViewHolder(@NonNull ClaimViewHolder holder, int position) {
        EmployeeClaimdataItem currentClaim = claimList.get(position);
        holder.bind(currentClaim, listener);
    }

    @Override
    public int getItemCount() {
        return claimList.size();
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
    }

    public class ClaimViewHolder extends RecyclerView.ViewHolder {
        TextView txtClaimId, txtProjectName, txtMeetingType, txtPurposeOfMeeting, txtTotalAmount, txtAppliedDate;
        TextView viewDetailsButton, actionButton;
        private Context context;
        private OnClaimClickListener listener;
        private List<EmployeeClaimdataItem> claimList;

        public ClaimViewHolder(@NonNull View itemView, Context context, OnClaimClickListener listener, List<EmployeeClaimdataItem> claimList) {
            super(itemView);
            this.context = context;
            this.listener = listener;
            this.claimList = claimList;
            txtClaimId = itemView.findViewById(R.id.claimIdText);
            txtProjectName = itemView.findViewById(R.id.projectNameText);
            txtMeetingType = itemView.findViewById(R.id.meetingTypeText);
            txtPurposeOfMeeting = itemView.findViewById(R.id.purposeText);
            txtTotalAmount = itemView.findViewById(R.id.amountText);
            txtAppliedDate = itemView.findViewById(R.id.claimDateText);
            viewDetailsButton = itemView.findViewById(R.id.viewDetailsButton);
            actionButton = itemView.findViewById(R.id.actionButton);

            viewDetailsButton.setOnClickListener(v -> {
                Log.d("ClaimViewHolder", "View Details button clicked");
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onClaimClick(claimList.get(position));
                }
            });
//            actionButton.setOnClickListener(v -> {
//                int position = getAdapterPosition();
//                if (position != RecyclerView.NO_POSITION) {
//                    EmployeeClaimdataItem claim = claimList.get(position);
//
//                    // Create a MaterialAlertDialogBuilder
//                    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog);
//                    builder.setTitle("Claim Action");
//                    String message = "Choose an action for this claim:\nClaim ID: " + claim.getClaimId();
//                    builder.setMessage(message);
//
//                    builder.setPositiveButton("Approve", (dialog, which) -> {
//                        // Call API to approve the claim
//                        updateClaimStatus(claim, "Approved");
//                    });
//
//                    builder.setNeutralButton("Cancel", (dialog, which) -> {
//                        // Dismiss the dialog (no action needed)
//                        updateClaimStatus(claim, "Cancelled");
//                    });
//
//                    builder.setNegativeButton("Reject", (dialog, which) -> {
//                        // Call API to reject the claim
//                        updateClaimStatus(claim, "Rejected");
//                    });
//
//                    AlertDialog dialog = builder.create();
//
//                    // Customize button colors (optional)
//                    dialog.setOnShowListener(dialogInterface -> {
//                        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
//                        positiveButton.setTextColor(ContextCompat.getColor(context, R.color.status_approved));
//
//                        Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
//                        negativeButton.setTextColor(ContextCompat.getColor(context, R.color.status_rejected));
//
//                        Button neutralButton = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
//                        neutralButton.setTextColor(ContextCompat.getColor(context, R.color.status_rejected));
//                    });
//
//                    dialog.show();
//                }
//            });
            actionButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    EmployeeClaimdataItem claim = claimList.get(position);

                    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog);
                    builder.setTitle("Claim Action");
                    String message = "Choose an action for this claim:\nClaim ID: " + claim.getClaimId();
                    builder.setMessage(message);

                    builder.setPositiveButton("Approve", (dialog, which) -> {
                        updateClaimStatus(claim, "Approved", "");
                    });

                    builder.setNeutralButton("Cancel", null);
                    builder.setNegativeButton("Reject", null);

                    final EditText commentInput = new EditText(context);
                    commentInput.setHint("Enter comment");
                    builder.setView(commentInput);

                    AlertDialog dialog = builder.create();

                    dialog.setOnShowListener(dialogInterface -> {
                        TextView errorTextView = new TextView(context);
                        errorTextView.setTextColor(Color.RED); // Set error text color
                        errorTextView.setVisibility(View.GONE);
                        ViewGroup layout = (ViewGroup) commentInput.getParent();
                        layout.addView(errorTextView, layout.indexOfChild(commentInput) + 1);

                        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                        positiveButton.setTextColor(ContextCompat.getColor(context, R.color.status_approved));

                        Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                        negativeButton.setTextColor(ContextCompat.getColor(context, R.color.status_rejected));
                        negativeButton.setEnabled(false);
                        negativeButton.setOnClickListener(view -> {
                            String comment = commentInput.getText().toString().trim();
                            if (comment.isEmpty()) {
                                errorTextView.setText("Fill the comment");
                                errorTextView.setVisibility(View.VISIBLE);
                            } else {
                                errorTextView.setVisibility(View.GONE);
                                updateClaimStatus(claim, "Rejected", comment);
                                dialog.dismiss();
                            }
                        });

                        Button neutralButton = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
                        neutralButton.setTextColor(ContextCompat.getColor(context, R.color.status_rejected));
                        neutralButton.setEnabled(false);
                        neutralButton.setOnClickListener(view -> {
                            String comment = commentInput.getText().toString().trim();
                            if (comment.isEmpty()) {
                                errorTextView.setText("Fill the comment");
                                errorTextView.setVisibility(View.VISIBLE);
                            } else {
                                errorTextView.setVisibility(View.GONE);
                                updateClaimStatus(claim, "Cancelled", comment);
                                dialog.dismiss();
                            }
                        });

                        commentInput.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {
                                boolean hasComment = !s.toString().trim().isEmpty();
                                negativeButton.setEnabled(hasComment);
                                neutralButton.setEnabled(hasComment);
                            }

                            @Override
                            public void afterTextChanged(Editable s) {
                            }
                        });
                    });

                    dialog.show();
                }
            });
        }


        public void bind(EmployeeClaimdataItem currentClaim, OnClaimClickListener listener) {
            txtClaimId.setText("Claim ID : " + currentClaim.getClaimId());
            txtProjectName.setText("Project Name : " + currentClaim.getProject());
            txtMeetingType.setText("Meeting Type : " + currentClaim.getMeeting());
            txtPurposeOfMeeting.setText("Purpose : " + currentClaim.getPerposeofmeet());
            txtTotalAmount.setText("Amount : " + currentClaim.getCurrency() + " " + currentClaim.getTotalamount());
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            try {
                Date date = inputFormat.parse(currentClaim.getClaimDate());
                txtAppliedDate.setText("Claim Date : " + outputFormat.format(date));
            } catch (Exception e) {
                // Handle parsing error
                txtAppliedDate.setText("Claim date : " + currentClaim.getClaimDate());
            }

            TextView statusText = itemView.findViewById(R.id.statusText);
            String status = currentClaim.getStatusRm() != null ? currentClaim.getStatusRm() : (currentClaim.getStatusHr() != null ? currentClaim.getStatusHr() : "Pending");
            statusText.setText(status);

            Drawable background = statusText.getBackground();
            if (background instanceof GradientDrawable) {
                GradientDrawable gradientDrawable = (GradientDrawable) background;
                int backgroundColor;
                if (status.equalsIgnoreCase("Unapproved")) {
                    backgroundColor = ContextCompat.getColor(context, R.color.pending_status_color);
                } else if (status.equalsIgnoreCase("Pending")) {
                    backgroundColor = ContextCompat.getColor(context, R.color.pending_status_color);
                } else if (status.equalsIgnoreCase("Approved")) {
                    backgroundColor = ContextCompat.getColor(context, R.color.approved_status_color);
                } else if (status.equalsIgnoreCase("Rejected")) {
                    backgroundColor = ContextCompat.getColor(context, R.color.rejected_status_color);
                } else if (status.equalsIgnoreCase("Cancelled")) {
                    backgroundColor = ContextCompat.getColor(context, R.color.rejected_status_color);
                } else {
                    backgroundColor = ContextCompat.getColor(context, R.color.default_status_color);
                }

                gradientDrawable.setColor(backgroundColor);
            }
            // Conditionally show/hide the action button
            if (status.equalsIgnoreCase("Unapproved")) {
                actionButton.setVisibility(View.VISIBLE);
            } else {
                actionButton.setVisibility(View.GONE);
            }
            viewDetailsButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onClaimClick(currentClaim);
                }
            });
        }

        private void updateClaimStatus(EmployeeClaimdataItem claim, String status, String comment) {
            SharedPreferences sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
            String authToken = sharedPreferences.getString("authToken", "");
            String userId = sharedPreferences.getString("userId", "");

            ProfileViewModel profileViewModel = new ViewModelProvider((FragmentActivity) context).get(ProfileViewModel.class);
            profileViewModel.storeLoginData(userId, authToken);
            profileViewModel.fetchUserProfile();

            profileViewModel.userProfile.observe(ViewTreeLifecycleOwner.get(itemView), userProfile -> {
                if (userProfile != null && userProfile.getData() != null && userProfile.getData().getEmployee() != null && userProfile.getData().getEmployee() != null) {
                    String reportingManagerFirstName = userProfile.getData().getEmployee().getFirstname();
                    String reportingManagerLastName = userProfile.getData().getEmployee().getLastname();
                    String reportingManagerId = userProfile.getData().getEmployee().getId();

                    if (reportingManagerFirstName != null && reportingManagerLastName != null && reportingManagerId != null) {
                        String reportingManager = reportingManagerFirstName + " " + reportingManagerLastName;
                        String reportingManagerEmail = userProfile.getData().getEmployee().getEmail();

                        try {
                            JSONObject requestBody = new JSONObject(new Gson().toJson(claim));
                            requestBody.put("statusRm", status);
                            requestBody.put("comment", comment);
                            requestBody.put("approvedBy", reportingManagerId);
                            requestBody.put("email", reportingManagerEmail);
                            requestBody.put("approvedByName", reportingManager);
                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                            String currentDate = dateFormat.format(new Date());
                            requestBody.put("approvedDate", currentDate);

                            RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), requestBody.toString());

                            // Get API instance and make the call
                            APIInterface apiInterface = APIClient.getInstance().ClaimUpdateStatus();
                            retrofit2.Call<ResponseBody> call = apiInterface.claimStatus("jwt " + authToken, body);

                            call.enqueue(new Callback<ResponseBody>() {
                                @Override
                                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull retrofit2.Response<ResponseBody> response) {
                                    if (response.isSuccessful()) {
                                        // Handle success
                                        claim.setStatusRm(status);
                                        notifyItemChanged(getAdapterPosition());
                                    } else {
                                        // Handle error
                                        Log.e("ClaimViewHolder", "Error updating claim status: " + response.message());

                                    }
                                }

                                @Override
                                public void onFailure(@NonNull retrofit2.Call<ResponseBody> call, @NonNull Throwable t) {
                                    Log.e("ClaimViewHolder", "Error updating claim status: " + t.getMessage());
                                }
                            });
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        Log.w("ClaimViewHolder", "Reporting manager information not available");
                    }
                }
            });
        }
    }
}
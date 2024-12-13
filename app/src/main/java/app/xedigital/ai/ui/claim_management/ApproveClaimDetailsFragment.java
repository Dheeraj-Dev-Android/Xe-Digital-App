package app.xedigital.ai.ui.claim_management;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import app.xedigital.ai.R;
import app.xedigital.ai.adapter.ImageAdapter;
import app.xedigital.ai.model.approveClaim.EmployeeClaimdataItem;


public class ApproveClaimDetailsFragment extends Fragment {
    private TextView txtClaimId, txtProjectName, txtMeetingType, txtPurposeOfMeeting, txtComment, txtTravelCategory, txtModeOfTransport, txtFromTo, txtDistance, txtTotalAmount, txtAppliedDate, txtStatus, txtStatusDetails;

    private ImageButton btnDocumentView;
    private EmployeeClaimdataItem claimData;

    public ApproveClaimDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_approve_claim_details, container, false);
        txtClaimId = view.findViewById(R.id.txtClaimId);
        txtProjectName = view.findViewById(R.id.txtProjectName);
        txtMeetingType = view.findViewById(R.id.txtMeetingType);
        txtPurposeOfMeeting = view.findViewById(R.id.txtPurposeOfMeeting);
        txtComment = view.findViewById(R.id.txtComment);
        txtTravelCategory = view.findViewById(R.id.txtTravelCategory);
        txtModeOfTransport = view.findViewById(R.id.txtModeOfTransport);
        txtFromTo = view.findViewById(R.id.txtFromTo);
        txtDistance = view.findViewById(R.id.txtDistance);
        txtTotalAmount = view.findViewById(R.id.txtTotalAmount);
        txtAppliedDate = view.findViewById(R.id.txtAppliedDate);
        txtStatus = view.findViewById(R.id.txtStatus);
        txtStatusDetails = view.findViewById(R.id.txtStatusDetails);
        btnDocumentView = view.findViewById(R.id.btnDocumentView);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            claimData = (EmployeeClaimdataItem) getArguments().getSerializable("claimData");

            txtClaimId.setText("Claim ID: " + claimData.getClaimId());
            txtProjectName.setText("Project Name: " + claimData.getProject());
            txtMeetingType.setText("Meeting Type: " + claimData.getMeeting());
            txtPurposeOfMeeting.setText("Purpose: " + claimData.getPerposeofmeet());
            txtComment.setText("Comment: " + claimData.getComment());
            txtTravelCategory.setText("Travel Category: " + claimData.getTravelCategory());
            txtModeOfTransport.setText("Mode of Transport: " + claimData.getModeoftransport());
            txtFromTo.setText("Route: " + claimData.getFromaddress() + " to " + claimData.getToaddress());
            txtDistance.setText("Distance: " + claimData.getDistance() + " Km");
            txtTotalAmount.setText("Total Amount: " + claimData.getCurrency() + " " + claimData.getTotalamount());
            txtStatus.setText("Status: " + claimData.getStatusRm());
            txtStatusDetails.setText("Status RM: " + claimData.getStatusRm() + " \nStatus HR: " + claimData.getStatusHr());

            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            try {
                Date date = inputFormat.parse(claimData.getClaimDate());
                txtAppliedDate.setText("Claim Date : " + outputFormat.format(date));
            } catch (Exception e) {
                // Handle parsing error
                txtAppliedDate.setText("Claim date : " + claimData.getClaimDate());
            }
        }
        btnDocumentView.setOnClickListener(v -> {
            Log.d("btnDocumentView", "btnDocumentView clicked");
            if (claimData != null && claimData.getDocumentUrls() != null) {
                List<String> documentUrls = claimData.getDocumentUrls();
                if (!documentUrls.isEmpty()) {
                    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
                    View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_image_viewer, null);
                    RecyclerView imageRecyclerView = dialogView.findViewById(R.id.imageRecyclerView);

                    imageRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
                    ImageAdapter imageAdapter = new ImageAdapter(requireContext(), documentUrls);
                    imageRecyclerView.setAdapter(imageAdapter);

                    Dialog dialog = builder.setView(dialogView)
                            .setTitle("Uploaded Documents")
                            .setPositiveButton("Close", (dialog1, which) -> dialog1.dismiss())
                            .create();

                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
                    dialog.show();
                }
            }
        });
    }
}
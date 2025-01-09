package app.xedigital.ai.ui.vms;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;

import app.xedigital.ai.R;
import app.xedigital.ai.databinding.FragmentVisitorsDetailBinding;
import app.xedigital.ai.model.vms.VisitorsItem;
import app.xedigital.ai.utills.DateTimeUtils;

public class VisitorsDetailFragment extends Fragment {
    private static final String ARG_VISITOR = "visitor";
    private FragmentVisitorsDetailBinding binding;
    private VisitorsItem visitor;

    public VisitorsDetailFragment() {
        // Required empty public constructor
    }

    public static VisitorsDetailFragment newInstance(VisitorsItem visitor) {
        VisitorsDetailFragment fragment = new VisitorsDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_VISITOR, visitor);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            visitor = (VisitorsItem) getArguments().getSerializable(ARG_VISITOR);
        }

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentVisitorsDetailBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        if (visitor != null) {
            binding.tvVisitorName.setText(visitor.getName() != null ? visitor.getName() : "N/A");
            binding.tvSerialNumber.setText("Serial Number: " + (visitor.getSerialNumber() != null ? visitor.getSerialNumber() : "N/A"));
            binding.tvVisitorCategory.setText("Category: " + (visitor.getVisitorCategory() != null ? visitor.getVisitorCategory() : "N/A"));
            binding.tvEmail.setText("Email: " + (visitor.getEmail() != null ? visitor.getEmail() : "N/A"));
            binding.tvContact.setText("Contact: " + (visitor.getContact() != null ? visitor.getContact() : "N/A"));
            binding.tvCompanyFrom.setText("Company: " + (visitor.getCompanyFrom() != null ? visitor.getCompanyFrom() : "N/A"));
            if (visitor.getWhomToMeet() != null) {
                String firstName = visitor.getWhomToMeet().getFirstname() != null ? visitor.getWhomToMeet().getFirstname() : "";
                String lastName = visitor.getWhomToMeet().getLastname() != null ? visitor.getWhomToMeet().getLastname() : "";
                binding.tvWhomToMeet.setText("Meeting With: " + firstName + " " + lastName);
            } else {
                binding.tvWhomToMeet.setText("Meeting With: N/A");
            }
            binding.tvPurposeOfMeeting.setText("Meeting Purpose: " + (visitor.getPurposeOfmeeting() != null ? visitor.getPurposeOfmeeting() : "N/A"));
            binding.tvMeetingOverStatus.setText("Meeting Status: " + (visitor.getMeetingOverStatus() != null ? visitor.getMeetingOverStatus() : "N/A"));

            binding.tvCheckinDateTime.setText("Check-in: " + (visitor.getSignIn() != null ? DateTimeUtils.getDayOfWeekAndDate(visitor.getSignIn()) : "N/A"));
            binding.tvCheckoutDateTime.setText("Check-out: " + (visitor.getSignOut() != null ? DateTimeUtils.getDayOfWeekAndDate(visitor.getSignOut()) : "N/A"));
            binding.tvMeetingOverDateTime.setText("Meeting Over: " + (visitor.getMeetingOverDate() != null ? DateTimeUtils.getDayOfWeekAndDate(visitor.getMeetingOverDate()) : "N/A"));

            binding.tvPreApproved.setText("Pre-approved: " + (visitor.isIsPreApproved() ? "Yes" : "No"));
            binding.tvPreApprovedDate.setText("Pre-approval Date: " + DateTimeUtils.getDayOfWeekAndDate(visitor.getPreApprovedDate()));
            binding.tvVisitorVisited.setText("Visitor Visited: " + (visitor.isIsVisitorVisited() ? "Yes" : "No"));

            String profileImagePath = visitor.getProfileImagePath();
            if (profileImagePath != null && !profileImagePath.isEmpty()) {
                Glide.with(requireContext()).load(profileImagePath).placeholder(R.drawable.ic_profile_placeholder).error(R.drawable.ic_profile_placeholder).circleCrop().into(binding.ivVisitorProfile);
            } else {
                // Optional: Load a default placeholder or clear the ImageView if no image is available
                Glide.with(requireContext()).load(R.drawable.ic_profile_placeholder).circleCrop().into(binding.ivVisitorProfile);
                Log.w("VisitorsAdapter", "Profile image path is null or empty for visitor: " + visitor.getName());
            }

            String status = visitor.getApprovalStatus();

            if (status == null || status.isEmpty()) {
                status = "Pending";
            }

            // Set chip text and background color based on status
            binding.chipApprovalStatus.setText(status);
            if (status.equalsIgnoreCase("Approved")) {
                binding.chipApprovalStatus.setChipBackgroundColorResource(R.color.status_approved);
            } else if (status.equalsIgnoreCase("Pending")) {
                binding.chipApprovalStatus.setChipBackgroundColorResource(R.color.pending_status_color);
            } else if (status.equalsIgnoreCase("Rejected")) {
                binding.chipApprovalStatus.setChipBackgroundColorResource(R.color.status_rejected);
            } else {
                // Handle other status values or set a default color
                binding.chipApprovalStatus.setChipBackgroundColorResource(R.color.icon_tint);
            }

        }

        return view;
    }
}
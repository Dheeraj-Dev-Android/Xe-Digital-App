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
        binding = FragmentVisitorsDetailBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        if (visitor != null) {
            // Apply helper method for null and blank string checks
            binding.tvVisitorName.setText(getDisplayValue(visitor.getName()));
            binding.tvSerialNumber.setText(getDisplayValue(visitor.getSerialNumber()));
            binding.tvVisitorCategory.setText(getDisplayValue(visitor.getVisitorCategory()));
            binding.tvEmail.setText(getDisplayValue(visitor.getEmail()));
            binding.tvContact.setText(getDisplayValue(visitor.getContact()));
            binding.tvCompanyFrom.setText(getDisplayValue(visitor.getCompanyFrom()));

            // Safe handling of whomToMeet to avoid blank names or solitary spaces
            if (visitor.getWhomToMeet() != null) {
                String firstName = getDisplayValue(visitor.getWhomToMeet().getFirstname());
                String lastName = getDisplayValue(visitor.getWhomToMeet().getLastname());

                if (firstName.equals("N/A") && lastName.equals("N/A")) {
                    binding.tvWhomToMeet.setText("N/A");
                } else if (firstName.equals("N/A")) {
                    binding.tvWhomToMeet.setText(lastName);
                } else if (lastName.equals("N/A")) {
                    binding.tvWhomToMeet.setText(firstName);
                } else {
                    binding.tvWhomToMeet.setText(firstName + " " + lastName);
                }
            } else {
                binding.tvWhomToMeet.setText("N/A");
            }

            binding.tvPurposeOfMeeting.setText(getDisplayValue(visitor.getPurposeOfmeeting()));
            binding.tvMeetingOverStatus.setText(getDisplayValue(visitor.getMeetingOverStatus()));

            // Dates: Safe formatting to prevent NPEs inside DateTimeUtils
            binding.tvCheckinDateTime.setText(formatDateSafely(visitor.getSignIn()));
            binding.tvCheckoutDateTime.setText(formatDateSafely(visitor.getSignOut()));
            binding.tvMeetingOverDateTime.setText(formatDateSafely(visitor.getMeetingOverDate()));

            // Fixed NPE risk here: Added safety wrapper around getPreApprovedDate
            binding.tvPreApprovedDate.setText(formatDateSafely(visitor.getPreApprovedDate()));

            binding.tvPreApproved.setText((visitor.isIsPreApproved() ? "Yes" : "No"));
            binding.tvVisitorVisited.setText((visitor.isIsVisitorVisited() ? "Yes" : "No"));

            String profileImagePath = visitor.getProfileImagePath();

            // Safe Context check for Glide to prevent IllegalStateException if Fragment is detached
            if (getContext() != null) {
                if (profileImagePath != null && !profileImagePath.trim().isEmpty()) {
                    Glide.with(getContext())
                            .load(profileImagePath)
                            .placeholder(R.drawable.ic_profile_placeholder)
                            .error(R.drawable.ic_profile_placeholder)
                            .circleCrop()
                            .into(binding.ivVisitorProfile);
                } else {
                    Glide.with(getContext())
                            .load(R.drawable.ic_profile_placeholder)
                            .circleCrop()
                            .into(binding.ivVisitorProfile);
                    Log.w("VisitorsAdapter", "Profile image path is null or empty for visitor: " + visitor.getName());
                }
            }

            String status = visitor.getApprovalStatus();
            if (status == null || status.trim().isEmpty()) {
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
                binding.chipApprovalStatus.setChipBackgroundColorResource(R.color.icon_tint);
            }
        }

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Prevents memory leaks with View Binding in Fragments
        binding = null;
    }

    /**
     * Helper method to return "N/A" if string is null or completely blank.
     */
    private String getDisplayValue(String value) {
        return (value == null || value.trim().isEmpty()) ? "N/A" : value;
    }

    /**
     * Helper method to handle date formatting safely to prevent NullPointerExceptions.
     * Adjust parameter type (String, Long, Object) if your date getter returns a different format.
     */
    private String formatDateSafely(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return "N/A";
        }
        try {
            String formatted = DateTimeUtils.getDayOfWeekAndDate(dateStr);
            return (formatted == null || formatted.trim().isEmpty()) ? "N/A" : formatted;
        } catch (Exception e) {
            Log.e("VisitorsDetailFragment", "Date formatting error", e);
            return "N/A";
        }
    }
}
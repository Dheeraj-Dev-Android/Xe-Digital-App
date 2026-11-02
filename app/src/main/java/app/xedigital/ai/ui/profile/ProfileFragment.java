package app.xedigital.ai.ui.profile;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;

import app.xedigital.ai.R;
import app.xedigital.ai.databinding.FragmentProfileBinding;
import app.xedigital.ai.model.profile.Employee;
import app.xedigital.ai.utills.SecurePrefManager;

public class ProfileFragment extends Fragment {
    private static final String TAG = "ProfileFragment";

    // Palette used for the initials-circle background color (deterministic per user).
    private static final int[] INITIALS_COLOR_PALETTE = new int[]{
            Color.rgb(51, 206, 255),
            Color.rgb(125, 206, 160),
            Color.rgb(255, 165, 0),
            Color.rgb(199, 0, 57),
            Color.rgb(161, 16, 76),
            Color.rgb(102, 0, 102),
            Color.rgb(16, 89, 178),
            Color.rgb(76, 175, 80),
            Color.rgb(255, 87, 82),
            Color.rgb(255, 183, 77),
            Color.rgb(159, 168, 218),
            Color.rgb(38, 166, 154)
    };

    private FragmentProfileBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ProfileViewModel profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        setHasOptionsMenu(true);
        binding.profileLoader.setVisibility(View.VISIBLE);
        binding.emptyStateText.setVisibility(View.GONE);
        binding.profileCard.setVisibility(View.GONE);

        SecurePrefManager prefManager = SecurePrefManager.getInstance(requireContext());
        String userId = prefManager.getString("userId", null);
        String authToken = prefManager.getString("authToken", null);

        profileViewModel.storeLoginData(userId, authToken);
        profileViewModel.fetchUserProfile();

        profileViewModel.userProfile.observe(getViewLifecycleOwner(), userProfile -> {
            binding.profileLoader.setVisibility(View.GONE);

            if (userProfile == null || userProfile.getData() == null || userProfile.getData().getEmployee() == null) {
                binding.profileCard.setVisibility(View.GONE);
                binding.emptyStateText.setVisibility(View.VISIBLE);
                Toast.makeText(requireContext(), "Failed to fetch profile. Please check your network connection.", Toast.LENGTH_SHORT).show();
                return;
            }

            Employee employee = userProfile.getData().getEmployee();

            if (employee != null) {
                binding.profileCard.setVisibility(View.VISIBLE);
                binding.emptyStateText.setVisibility(View.GONE);

                // ---- INITIALS-BASED PROFILE IMAGE ----
                String firstName = employee.getFirstname() != null ? employee.getFirstname() : "";
                String lastName = employee.getLastname() != null ? employee.getLastname() : "";
                Object profileImageUrlObj = employee.getProfileImageUrl();
                String profileImageUrl = profileImageUrlObj != null ? String.valueOf(profileImageUrlObj) : null;
                ImageView profileImage = binding.profileImage;
                Drawable initialsDrawable = getInitialsDrawable(firstName, lastName);

                if (profileImageUrl != null
                        && !profileImageUrl.trim().isEmpty()
                        && !"null".equalsIgnoreCase(profileImageUrl.trim())) {
                    Glide.with(requireContext())
                            .load(profileImageUrl)
                            .placeholder(initialsDrawable)
                            .error(initialsDrawable)
                            .into(profileImage);
                } else {
                    profileImage.setImageDrawable(initialsDrawable);
                }

                binding.employeeCodeTitle.setText("Emp Code ");
                binding.employeeCodeValue.setText(employee.getEmployeeCode() != null ? employee.getEmployeeCode() : "N/A");
                binding.nameTitle.setText("Name ");
                String fullName = (employee.getFirstname() != null ? employee.getFirstname() : "") + " " + (employee.getLastname() != null ? employee.getLastname() : "");
                binding.nameValue.setText(!fullName.trim().isEmpty() ? fullName : "N/A");
                binding.emailTitle.setText("Email ");
                binding.emailValue.setText(employee.getEmail() != null ? employee.getEmail() : "N/A");
                binding.contactTitle.setText("Contact ");
                binding.contactValue.setText(employee.getContact() != null ? employee.getContact() : "N/A");

                if (employee.getDepartment() != null && employee.getDesignation() != null) {
                    binding.departmentTitle.setText("Department ");
                    binding.departmentValue.setText(employee.getDepartment().getName() != null ? employee.getDepartment().getName() : "N/A");
                    binding.designationTitle.setText("Designation ");
                    binding.designationValue.setText(employee.getDesignation() != null ? employee.getDesignation() : "N/A");
                } else {
                    binding.departmentTitle.setText("Department ");
                    binding.departmentValue.setText("N/A");
                    binding.designationTitle.setText("Designation ");
                    binding.designationValue.setText("N/A");
                }

                binding.levelGradeTitle.setText("Level / Grade ");
                binding.levelGradeValue.setText((employee.getLevel() != null ? employee.getLevel() : "N/A") + " / " + (employee.getGrade() != null ? employee.getGrade() : "N/A"));

                if (employee.getShift() != null) {
                    binding.shiftTitle.setText("Shift ");
                    binding.shiftValue.setText(employee.getShift().getName() != null ? employee.getShift().getName() : "N/A");
                    binding.shiftTimingTitle.setText("Timing ");
                    binding.shiftTimingValue.setText((employee.getShift().getStartTime() != null ? employee.getShift().getStartTime() : "N/A") + " - " + (employee.getShift().getEndTime() != null ? employee.getShift().getEndTime() : "N/A"));
                } else {
                    binding.shiftTitle.setText("Shift ");
                    binding.shiftValue.setText("N/A");
                    binding.shiftTimingTitle.setText("Timing ");
                    binding.shiftTimingValue.setText("N/A");
                }

                if (employee.getReportingManager() != null) {
                    binding.reportingManagerTitle.setText("Name");
                    String reportingManagerFullName = (employee.getReportingManager().getFirstname() != null ? employee.getReportingManager().getFirstname() : "") + " " + (employee.getReportingManager().getLastname() != null ? employee.getReportingManager().getLastname() : "");
                    binding.reportingManagerName.setText(!reportingManagerFullName.trim().isEmpty() ? reportingManagerFullName : "N/A");
                    binding.reportingManagerEmail.setText("Email");
                    binding.reportingManagerEmailValue.setText(employee.getReportingManager().getEmail() != null ? employee.getReportingManager().getEmail() : "N/A");
                } else {
                    binding.reportingManagerName.setText("N/A");
                    binding.reportingManagerEmailValue.setText("N/A");
                }

                if (employee.getCrossmanager() != null) {
                    binding.crossManagerTitle.setText("Name");
                    String crossManagerFullName = (employee.getCrossmanager().getFirstname() != null ? employee.getCrossmanager().getFirstname() : "") + " " + (employee.getCrossmanager().getLastname() != null ? employee.getCrossmanager().getLastname() : "");
                    binding.crossManagerName.setText(!crossManagerFullName.trim().isEmpty() ? crossManagerFullName : "N/A");
                    binding.crossManagerEmail.setText("Email");
                    binding.crossManagerEmailValue.setText(employee.getCrossmanager().getEmail() != null ? employee.getCrossmanager().getEmail() : "N/A");
                } else {
                    binding.crossManagerName.setText("N/A");
                    binding.crossManagerEmailValue.setText("N/A");
                }

            } else {
                Toast.makeText(requireContext(), "Failed to fetch profile. Please check your network connection.", Toast.LENGTH_SHORT).show();
            }

        });
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.edit_profile, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (super.onOptionsItemSelected(item)) {
            return true;
        }
        if (item.getItemId() == R.id.action_edit_profile) {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_nav_profile_to_nav_edit_profile);
            Toast.makeText(requireContext(), "Edit Profile", Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    // =========================================================================
    //  INITIALS AVATAR HELPERS
    // =========================================================================

    /**
     * Builds a self-scaling Drawable that renders a colored circle with the
     * user's initials in white. Used as the fallback whenever a profile image
     * is missing OR fails to load.
     */
    private Drawable getInitialsDrawable(String firstName, String lastName) {
        final String initials = getInitials(firstName, lastName);
        final int bgColor = getInitialsColor(firstName, lastName);

        return new Drawable() {
            private final Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            private final Rect textBounds = new Rect();

            {
                bgPaint.setColor(bgColor);
                bgPaint.setStyle(Paint.Style.FILL);

                textPaint.setColor(Color.WHITE);
                textPaint.setTextAlign(Paint.Align.CENTER);
                textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            }

            @Override
            public void draw(@NonNull Canvas canvas) {
                Rect b = getBounds();
                if (b.width() <= 0 || b.height() <= 0) return;

                float cx = b.exactCenterX();
                float cy = b.exactCenterY();
                float radius = Math.min(b.width(), b.height()) / 2f;

                canvas.drawCircle(cx, cy, radius, bgPaint);

                // Scale text to roughly 40% of the circle diameter.
                textPaint.setTextSize(radius * 0.8f);
                textPaint.getTextBounds(initials, 0, initials.length(), textBounds);
                float textY = cy - textBounds.exactCenterY();
                canvas.drawText(initials, cx, textY, textPaint);
            }

            @Override
            public void setAlpha(int alpha) {
                bgPaint.setAlpha(alpha);
                textPaint.setAlpha(alpha);
                invalidateSelf();
            }

            @Override
            public void setColorFilter(@Nullable ColorFilter colorFilter) {
                bgPaint.setColorFilter(colorFilter);
                textPaint.setColorFilter(colorFilter);
                invalidateSelf();
            }

            @Override
            public int getOpacity() {
                return PixelFormat.TRANSLUCENT;
            }
        };
    }

    /**
     * Extracts up to 2 uppercase initials from the given first/last name.
     * Falls back to "?" if nothing usable is available.
     */
    private String getInitials(String firstName, String lastName) {
        String first = firstName != null ? firstName.trim() : "";
        String last = lastName != null ? lastName.trim() : "";

        StringBuilder sb = new StringBuilder();
        if (!first.isEmpty()) {
            sb.append(Character.toUpperCase(first.charAt(0)));
        }
        if (!last.isEmpty()) {
            sb.append(Character.toUpperCase(last.charAt(0)));
        }

        if (sb.length() == 0) {
            return "?";
        }
        return sb.toString();
    }

    /**
     * Deterministically picks a palette color for the given name so the
     * same user always sees the same background color.
     */
    private int getInitialsColor(String firstName, String lastName) {
        String key = ((firstName != null ? firstName : "") + "|" + (lastName != null ? lastName : "")).trim();
        if (key.isEmpty() || "|".equals(key)) {
            return INITIALS_COLOR_PALETTE[0];
        }
        int hash = key.hashCode();
        int index = Math.abs(hash) % INITIALS_COLOR_PALETTE.length;
        return INITIALS_COLOR_PALETTE[index];
    }
}
package app.xedigital.ai.admin.adminUI.Visitor;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.print.PrintHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

import java.util.ArrayList;
import java.util.List;

import app.xedigital.ai.R;
import app.xedigital.ai.admin.adminAdapter.VisitorAdapter;
import app.xedigital.ai.admin.adminModal.VisitorsAdminDetails.VisitorsItem;
import app.xedigital.ai.admin.adminModal.VisitorsAdminDetails.WhomToMeet;
import app.xedigital.ai.utills.DateTimeUtils;
import app.xedigital.ai.utills.SecurePrefManager;

public class VisitorsDetailsFragment extends Fragment {

    private final List<VisitorsItem> fullVisitorList = new ArrayList<>();
    private final List<VisitorsItem> filteredVisitorList = new ArrayList<>();
    private VisitorsDetailsViewModel mViewModel;
    private VisitorAdapter adapter;
    private RecyclerView rvVisitors;
    private EditText vsSearchEmployee;
    private View emptyStateView;
    private ProgressBar progressBar;
    private AlertDialog loadingDialog;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_visitors_details, container, false);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvVisitors = view.findViewById(R.id.rvVisitors);
        vsSearchEmployee = view.findViewById(R.id.vsSearchEmployee);
        emptyStateView = view.findViewById(R.id.emptyStateView);
        progressBar = view.findViewById(R.id.progressBar);

        rvVisitors.setLayoutManager(new LinearLayoutManager(requireContext()));

        mViewModel = new ViewModelProvider(this).get(VisitorsDetailsViewModel.class);

        adapter = new VisitorAdapter(requireContext(), filteredVisitorList, new VisitorAdapter.OnVisitorActionListener() {
            @Override
            public void onCheckInClick(VisitorsItem visitor, int position) {
                handleCheckIn(visitor, position);
            }

            @Override
            public void onCheckOutClick(VisitorsItem visitor, int position) {
                handleCheckOut(visitor, position);
            }

            @Override
            public void onPrintClick(VisitorsItem visitor, int position) {
                showPrintPassDialog(visitor);
            }
        });
        rvVisitors.setAdapter(adapter);

        String authToken = getAuthToken();
        if (authToken != null) {
            progressBar.setVisibility(View.VISIBLE);
            mViewModel.getVisitors("jwt " + authToken).observe(getViewLifecycleOwner(), visitors -> {
                progressBar.setVisibility(View.GONE);
                if (visitors != null && !visitors.isEmpty()) {
                    fullVisitorList.clear();
                    filteredVisitorList.clear();
                    fullVisitorList.addAll(visitors);
                    filteredVisitorList.addAll(visitors);
                    adapter.notifyDataSetChanged();
                } else {
                    fullVisitorList.clear();
                    filteredVisitorList.clear();
                    adapter.notifyDataSetChanged();
                    Toast.makeText(requireContext(), "No Visitors Found", Toast.LENGTH_SHORT).show();
                }
                updateEmptyState();
            });
        } else {
            Toast.makeText(requireContext(), "Token not found", Toast.LENGTH_SHORT).show();
            updateEmptyState();
        }

        vsSearchEmployee.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterVisitorsByName(s.toString());
            }
        });
    }

    private void handleCheckIn(VisitorsItem visitor, int position) {
        String token = "jwt " + getAuthToken();
        String visitorId = visitor.getId();

        visitor.setApprovalStatus("approved");
        visitor.setApprovalDate(DateTimeUtils.getCurrentDateInISOFormat());

        showLoading(true);
        Toast.makeText(getContext(), "Processing Sign Out...", Toast.LENGTH_SHORT).show();

        mViewModel.checkInVisitor(token, visitorId, visitor).observe(getViewLifecycleOwner(), success -> {
            showLoading(false);
            if (success) {
                Toast.makeText(getContext(), "Visitor Signed Out Successfully", Toast.LENGTH_SHORT).show();
                adapter.notifyItemChanged(position);
                refreshData();
            } else {
                Toast.makeText(getContext(), "Failed to sign out visitor", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleCheckOut(VisitorsItem visitor, int position) {
        String token = "jwt " + getAuthToken();
        String visitorId = visitor.getId();

        visitor.setMeetingOverStatus("Done");
        visitor.setMeetingOverDate(DateTimeUtils.getCurrentDateInISOFormat());
        showLoading(true);

        mViewModel.checkOutVisitor(token, visitorId, visitor).observe(getViewLifecycleOwner(), success -> {
            showLoading(false);
            if (success) {
                Toast.makeText(getContext(), "Sign-out successful", Toast.LENGTH_SHORT).show();
                adapter.notifyItemChanged(position);
                refreshData();
            } else {
                Toast.makeText(getContext(), "Sign-out failed. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ==================== PRINT PASS FEATURE ====================

    private void showPrintPassDialog(VisitorsItem visitor) {
        if (getContext() == null) return;

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_visitor_pass, null);

        TextView passVisitorName = dialogView.findViewById(R.id.passVisitorName);
        TextView passVisitorContact = dialogView.findViewById(R.id.passVisitorContact);
        TextView passCompanyFrom = dialogView.findViewById(R.id.passCompanyFrom);
        TextView passHostName = dialogView.findViewById(R.id.passHostName);
        TextView passCheckInDate = dialogView.findViewById(R.id.passCheckInDate);
        TextView passCheckInTime = dialogView.findViewById(R.id.passCheckInTime);
        ImageView passQrCode = dialogView.findViewById(R.id.passQrCode);
        MaterialButton btnCancelPass = dialogView.findViewById(R.id.btnCancelPass);
        MaterialButton btnPrintPassAction = dialogView.findViewById(R.id.btnPrintPassAction);
        ImageView passCompanyLogo = dialogView.findViewById(R.id.passCompanyLogo);
        ImageView passWatermark = dialogView.findViewById(R.id.passWatermark);

        // Safe value extraction
        String vName = visitor.getName() != null ? visitor.getName() : "N/A";
        String vContact = visitor.getContact() != null ? visitor.getContact() : "N/A";
        String vCompany = visitor.getCompanyFrom() != null ? visitor.getCompanyFrom() : "N/A";

        String vHost = "N/A";
        WhomToMeet host = visitor.getWhomToMeet();
        if (host != null) {
            String firstName = host.getFirstname() != null ? host.getFirstname() : "";
            String lastName = host.getLastname() != null ? host.getLastname() : "";
            String fullName = (firstName + " " + lastName).trim();
            if (!fullName.isEmpty()) {
                vHost = fullName;
            }
        }

        String vDate = visitor.getSignIn() != null ? visitor.getSignIn() : "N/A";
        String vTime = visitor.getSignIn() != null ? DateTimeUtils.extractTime(visitor.getSignIn()) : "--";

        // Bind text values
        passVisitorName.setText(vName);
        passVisitorContact.setText(vContact);
        passCompanyFrom.setText(vCompany);
        passHostName.setText(vHost);
        passCheckInDate.setText(DateTimeUtils.getDayOfWeekAndDate(vDate));
        passCheckInTime.setText(vTime);

        // Generate QR code
        String qrTextPayload = "VISITOR PASS DETAILS\n" + "---------------------\n" + "Visitor Name: " + vName + "\n" + "Contact: " + vContact + "\n" + "Company From: " + vCompany + "\n" + "Host Name: " + vHost + "\n" + "Checked In Date: " + DateTimeUtils.getDayOfWeekAndDate(vDate) + "\n" + "Checked In Time: " + vTime;

        Bitmap qrBitmap = generateQrCode(qrTextPayload);
        if (qrBitmap != null) {
            passQrCode.setImageBitmap(qrBitmap);
        }

        // Load company logo and watermark
        SecurePrefManager securePrefManager = SecurePrefManager.getInstance(requireContext());
        String logoUrl = securePrefManager.getString("company_logo", "");

        if (logoUrl != null && !logoUrl.trim().isEmpty()) {
            Glide.with(requireContext()).load(logoUrl).placeholder(R.mipmap.ic_default_profile).error(R.mipmap.ic_launcher_round).into(passCompanyLogo);
        }

        Glide.with(requireContext()).load(R.mipmap.ic_launcher_round).error(R.mipmap.ic_launcher_round).into(passWatermark);

        // Build and show dialog
        final AlertDialog dialog = new AlertDialog.Builder(requireContext()).setView(dialogView).create();

        btnCancelPass.setOnClickListener(v -> dialog.dismiss());

        btnPrintPassAction.setOnClickListener(v -> {
            View printArea = dialogView.findViewById(R.id.printArea);
            if (printArea != null) {
                doPrintJob(printArea, vName);
            }
        });

        dialog.show();
    }

    private Bitmap generateQrCode(String data) {
        try {
            BitMatrix bitMatrix = new MultiFormatWriter().encode(data, BarcodeFormat.QR_CODE, 400, 400);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void doPrintJob(View viewToPrint, String visitorName) {
        // Hide action buttons during rendering
        View buttons = viewToPrint.findViewById(R.id.actionButtonsContainer);
        if (buttons != null) buttons.setVisibility(View.GONE);

        // Capture view as bitmap
        viewToPrint.setDrawingCacheEnabled(true);
        viewToPrint.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(viewToPrint.getDrawingCache());
        viewToPrint.setDrawingCacheEnabled(false);

        // Restore button visibility
        if (buttons != null) buttons.setVisibility(View.VISIBLE);

        // Print via system print framework
        PrintHelper printHelper = new PrintHelper(requireContext());
        printHelper.setScaleMode(PrintHelper.SCALE_MODE_FILL);
        printHelper.printBitmap("VisitorPass-" + visitorName, bitmap);
    }

    // ==================== END PRINT PASS ====================

    private void filterVisitorsByName(String query) {
        filteredVisitorList.clear();
        if (query.isEmpty()) {
            filteredVisitorList.addAll(fullVisitorList);
        } else {
            for (VisitorsItem visitor : fullVisitorList) {
                if (visitor.getName() != null && visitor.getName().toLowerCase().contains(query.toLowerCase())) {
                    filteredVisitorList.add(visitor);
                }
            }
        }

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        updateEmptyState();
    }

    private String getAuthToken() {
        SecurePrefManager sharedPreferences = SecurePrefManager.getInstance(requireContext());
        return sharedPreferences.getString("authToken", null);
    }

    private void refreshData() {
        String authToken = getAuthToken();
        if (authToken != null) {
            mViewModel.getVisitors("jwt " + authToken).observe(getViewLifecycleOwner(), visitors -> {
                if (visitors != null) {
                    fullVisitorList.clear();
                    filteredVisitorList.clear();
                    fullVisitorList.addAll(visitors);
                    filteredVisitorList.addAll(visitors);

                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }
                }
                updateEmptyState();
            });
        }
    }

    private void updateEmptyState() {
        if (filteredVisitorList.isEmpty()) {
            emptyStateView.setVisibility(View.VISIBLE);
            rvVisitors.setVisibility(View.GONE);
        } else {
            emptyStateView.setVisibility(View.GONE);
            rvVisitors.setVisibility(View.VISIBLE);
        }
    }

    private void showLoading(boolean show) {
        if (show) {
            if (loadingDialog == null) {
                android.widget.ProgressBar dialogProgressBar = new android.widget.ProgressBar(requireContext());
                dialogProgressBar.setPadding(20, 20, 20, 20);

                loadingDialog = new AlertDialog.Builder(requireContext()).setView(dialogProgressBar).setCancelable(false).create();

                if (loadingDialog.getWindow() != null) {
                    loadingDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                }
            }
            loadingDialog.show();
        } else {
            if (loadingDialog != null && loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }
        }
    }
}
package app.xedigital.ai.admin.adminUI.employeeOnboard;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;

import java.io.File;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import app.xedigital.ai.R;
import app.xedigital.ai.model.employeeOnboardDetails.DocumentsItem;
import app.xedigital.ai.model.employeeOnboardDetails.EmployeeOnBoardDetailsItem;
import app.xedigital.ai.model.employeeOnboardDetails.FamilyDetailsItem;
import app.xedigital.ai.utills.SecurePrefManager;

public class EmployeeOnboardDetailsFragment extends Fragment implements EmployeeOnboardDetailsAdapter.OnOnboardActionListener {

    private final ExecutorService docExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private EmployeeOnboardDetailsViewModel mViewModel;
    private RecyclerView rvOnboardEmployees;
    private ShimmerFrameLayout shimmerLayout;
    private View emptyStateView;
    private EditText etSearchOnboard;
    private EmployeeOnboardDetailsAdapter adapter;
    private String token;
    private Dialog docDialog;

    public static EmployeeOnboardDetailsFragment newInstance() {
        return new EmployeeOnboardDetailsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_employee_onboard_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mViewModel = new ViewModelProvider(this).get(EmployeeOnboardDetailsViewModel.class);
        SecurePrefManager prefManager = SecurePrefManager.getInstance(requireContext());
        token = prefManager.getString("authToken", "");

        initViews(view);
        setupRecyclerView();
        setupSearch();
        observeViewModel();

        showLoading(true);
        mViewModel.fetchOnboardedEmployees("jwt " + token);
    }

    private void initViews(View view) {
        rvOnboardEmployees = view.findViewById(R.id.rvOnboardEmployees);
        shimmerLayout = view.findViewById(R.id.shimmerLayout);
        emptyStateView = view.findViewById(R.id.emptyStateView);
        etSearchOnboard = view.findViewById(R.id.etSearchOnboard);
    }

    private void setupRecyclerView() {
        rvOnboardEmployees.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new EmployeeOnboardDetailsAdapter(this);
        rvOnboardEmployees.setAdapter(adapter);
    }

    private void setupSearch() {
        etSearchOnboard.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter == null) return;
                adapter.filterByName(s != null ? s.toString() : "");
                updateEmptyStateAfterFilter();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void observeViewModel() {
        mViewModel.getEmployeeList().observe(getViewLifecycleOwner(), employees -> {
            if (!isAdded()) return;

            showLoading(false);

            if (employees == null || employees.isEmpty()) {
                adapter.updateData(null);
                emptyStateView.setVisibility(View.VISIBLE);
                rvOnboardEmployees.setVisibility(View.GONE);
            } else {
                adapter.updateData(employees);

                String query = etSearchOnboard.getText() != null ? etSearchOnboard.getText().toString() : "";
                if (!query.trim().isEmpty()) {
                    adapter.filterByName(query);
                }

                updateEmptyStateAfterFilter();
            }
        });

        mViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (!isAdded() || isLoading == null) return;
            if (isLoading) {
                showLoading(true);
            }
        });

        mViewModel.getErrorMessage().observe(getViewLifecycleOwner(), message -> {
            if (!isAdded() || message == null || getContext() == null) return;
            showLoading(false);
            emptyStateView.setVisibility(View.VISIBLE);
            rvOnboardEmployees.setVisibility(View.GONE);
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        });
    }

    private void showLoading(boolean loading) {
        if (loading) {
            shimmerLayout.setVisibility(View.VISIBLE);
            shimmerLayout.startShimmer();
            rvOnboardEmployees.setVisibility(View.GONE);
            emptyStateView.setVisibility(View.GONE);
        } else {
            shimmerLayout.stopShimmer();
            shimmerLayout.setVisibility(View.GONE);
        }
    }

    private void updateEmptyStateAfterFilter() {
        if (adapter == null || !isAdded()) return;

        boolean empty = adapter.isFilteredEmpty();
        emptyStateView.setVisibility(empty ? View.VISIBLE : View.GONE);
        rvOnboardEmployees.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    // ---------------- Adapter callbacks ----------------
    // NOTE: signatures assume the adapter interface passes the item.
    // If yours differs (e.g. takes a position), adjust these two methods only.
    @Override
    public void onViewDetails(EmployeeOnBoardDetailsItem item) {
        showEmployeeOnboardDetailsBottomSheet(item);
    }

    @Override
    public void onEditDetails(EmployeeOnBoardDetailsItem item) {
        // Edit flow is not approved yet - keeping the existing stub behaviour.
        if (isAdded() && getContext() != null) {
            Toast.makeText(getContext(), "Edit coming soon", Toast.LENGTH_SHORT).show();
        }
    }

    // ---------------- Small helpers ----------------
    private void setText(View root, int id, String value) {
        TextView tv = root.findViewById(id);
        if (tv != null) tv.setText(value);
    }

    private int dp(int value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
    }

    // ---------------- Document opener ----------------
    private void openDocumentViewer(String source) {
        if (!isAdded() || getContext() == null) return;

        String value = safe(source);
        if ("N/A".equals(value)) {
            Toast.makeText(requireContext(), "Document not uploaded", Toast.LENGTH_SHORT).show();
            return;
        }

        // Base64 data URI -> in-app viewer
        if (value.startsWith("data:")) {
            showEmbeddedDocument(value);
            return;
        }

        // Normal web link -> external viewer
        String url = value.startsWith("http") ? value : "https://" + value;
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(requireContext(), "No app found to open this document", Toast.LENGTH_SHORT).show();
        }
    }

    private void showEmbeddedDocument(String dataUri) {
        int comma = dataUri.indexOf(',');
        if (comma < 0) {
            Toast.makeText(requireContext(), "Invalid document data", Toast.LENGTH_SHORT).show();
            return;
        }
        final String payload = dataUri.substring(comma + 1);
        final Context appContext = requireContext().getApplicationContext();

        // ---- Build full-screen viewer dialog in code ----
        if (docDialog != null && docDialog.isShowing()) docDialog.dismiss();
        final Dialog dialog = new Dialog(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        docDialog = dialog;

        FrameLayout root = new FrameLayout(requireContext());
        root.setBackgroundColor(Color.BLACK);

        ScrollView scroll = new ScrollView(requireContext());
        final LinearLayout pagesContainer = new LinearLayout(requireContext());
        pagesContainer.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(pagesContainer, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(scroll, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        final ProgressBar progress = new ProgressBar(requireContext());
        root.addView(progress, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER));

        ImageButton close = new ImageButton(requireContext());
        close.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        close.setBackgroundColor(Color.TRANSPARENT);
        close.setOnClickListener(v -> dialog.dismiss());
        FrameLayout.LayoutParams closeLp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.TOP | Gravity.END);
        int m = (int) (16 * getResources().getDisplayMetrics().density);
        closeLp.setMargins(m, m, m, m);
        root.addView(close, closeLp);

        dialog.setContentView(root);
        dialog.show();

        // ---- Decode off the main thread ----
        docExecutor.execute(() -> {
            try {
                byte[] bytes = Base64.decode(payload, Base64.DEFAULT);
                boolean isPdf = bytes.length > 4 && bytes[0] == '%' && bytes[1] == 'P' && bytes[2] == 'D' && bytes[3] == 'F';

                final List<Bitmap> bitmaps = new ArrayList<>();
                if (isPdf) {
                    bitmaps.addAll(renderPdfPages(appContext, bytes));
                } else {
                    Bitmap bmp = decodeSampledBitmap(bytes, 1600);
                    if (bmp != null) bitmaps.add(bmp);
                }

                mainHandler.post(() -> {
                    if (!isAdded() || !dialog.isShowing()) return;
                    progress.setVisibility(View.GONE);
                    if (bitmaps.isEmpty()) {
                        Toast.makeText(requireContext(), "Unable to display this document", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        return;
                    }
                    for (Bitmap b : bitmaps) {
                        ImageView iv = new ImageView(requireContext());
                        iv.setAdjustViewBounds(true);
                        iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        iv.setImageBitmap(b);
                        pagesContainer.addView(iv, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    }
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    if (!isAdded()) return;
                    Toast.makeText(requireContext(), "Failed to load document", Toast.LENGTH_SHORT).show();
                    if (dialog.isShowing()) dialog.dismiss();
                });
            }
        });
    }

    private Bitmap decodeSampledBitmap(byte[] bytes, int maxDim) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opts);

        int sample = 1;
        while (opts.outWidth / (sample * 2) >= maxDim || opts.outHeight / (sample * 2) >= maxDim) {
            sample *= 2;
        }
        opts.inJustDecodeBounds = false;
        opts.inSampleSize = sample;
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opts);
    }

    private List<Bitmap> renderPdfPages(Context ctx, byte[] bytes) throws Exception {
        List<Bitmap> result = new ArrayList<>();
        File temp = File.createTempFile("doc_", ".pdf", ctx.getCacheDir());
        ParcelFileDescriptor pfd = null;
        PdfRenderer renderer = null;
        try {
            try (FileOutputStream fos = new FileOutputStream(temp)) {
                fos.write(bytes);
            }
            pfd = ParcelFileDescriptor.open(temp, ParcelFileDescriptor.MODE_READ_ONLY);
            renderer = new PdfRenderer(pfd);

            int count = Math.min(renderer.getPageCount(), 10); // cap to protect memory
            for (int i = 0; i < count; i++) {
                PdfRenderer.Page page = renderer.openPage(i);
                int w = 1080;
                int h = (int) ((float) page.getHeight() / page.getWidth() * w);
                Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                bmp.eraseColor(Color.WHITE);
                page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                page.close();
                result.add(bmp);
            }
        } finally {
            if (renderer != null) renderer.close();
            if (pfd != null) pfd.close();
            temp.delete();
        }
        return result;
    }

    // Binds one of the three fixed document rows
    private void bindDocRow(View row, TextView statusTv, String url) {
        boolean has = !safe(url).equals("N/A");
        statusTv.setText(has ? "Tap to view" : "Not uploaded");
        statusTv.setTextColor(ContextCompat.getColor(requireContext(), has ? R.color.colorPrimaryVariant : R.color.text_tertiary));
        row.setAlpha(has ? 1f : 0.5f);
        row.setEnabled(has);
        if (has) {
            row.setOnClickListener(v -> openDocumentViewer(url));
        } else {
            row.setOnClickListener(null);
            row.setClickable(false);
        }
    }

    // ---------------- Bottom sheet ----------------
    public void showEmployeeOnboardDetailsBottomSheet(EmployeeOnBoardDetailsItem item) {
        if (!isAdded() || getContext() == null || item == null) return;

        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View s = LayoutInflater.from(requireContext()).inflate(R.layout.bottom_sheet_emp_onboard_details, null);
        dialog.setContentView(s);

        // Open fully expanded so all content is reachable
        dialog.setOnShowListener(d -> {
            FrameLayout sheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (sheet != null) {
                BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(sheet);
                behavior.setSkipCollapsed(true);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });

        // ----- Header -----
        String fullName = safe(item.getFullName());
        setText(s, R.id.tvSheetName, fullName);
        setText(s, R.id.tvSheetDesignationTop, safe(item.getDesignation()));
        setText(s, R.id.tvSheetEmail, safe(item.getPersonalEmail()));
        setText(s, R.id.tvSheetAvatar, fullName.equals("N/A") ? "XD" : getInitials(fullName));

        TextView tvStatus = s.findViewById(R.id.tvStatus);
        MaterialCardView cardStatus = s.findViewById(R.id.cardStatus);
        boolean active = item.isStatus();
        tvStatus.setText(active ? "Active" : "Inactive");
        tvStatus.setTextColor(ContextCompat.getColor(requireContext(), active ? R.color.status_approved : R.color.status_rejected));
        cardStatus.setCardBackgroundColor(ContextCompat.getColor(requireContext(), active ? R.color.bg_tint_green : R.color.error_bg));
        s.findViewById(R.id.cardDraft).setVisibility(item.isIsDraft() ? View.VISIBLE : View.GONE);

        // ----- Work -----
        setText(s, R.id.tvEmployeeId, safe(item.getEmployeeId()));
        setText(s, R.id.tvDepartment, safe(item.getDepartment()));
        setText(s, R.id.tvDesignation, safe(item.getDesignation()));
        setText(s, R.id.tvReportingManager, safe(item.getReportingManager()));
        setText(s, R.id.tvWorkLocation, safe(item.getWorkLocation()));
        setText(s, R.id.tvBranchName, safe(item.getBranchName()));
        setText(s, R.id.tvEmploymentType, safe(item.getEmploymentType()));
        setText(s, R.id.tvDateOfJoining, safeDate(item.getDateOfJoining()));

        // ----- Personal -----
        setText(s, R.id.tvFullName, fullName);
        setText(s, R.id.tvDob, safeDate(item.getDob()));
        setText(s, R.id.tvGender, safe(item.getGender()));
        setText(s, R.id.tvBloodGroup, safe(item.getBloodGroup()));
        setText(s, R.id.tvMaritalStatus, safe(item.getMaritalStatus()));
        setText(s, R.id.tvNationality, safe(item.getNationality()));
        setText(s, R.id.tvFatherOrHusband, safe(item.getFatherOrHusbandName()));
        setText(s, R.id.tvPassportNumber, safe(item.getPassportNumber()));
        setText(s, R.id.tvPassportExpiry, safeDate(item.getPassportExpiryDate()));

        // ----- Contact & Address -----
        setText(s, R.id.tvPersonalEmail, safe(item.getPersonalEmail()));
        setText(s, R.id.tvPersonalMobile, safe(item.getPersonalMobile()));
        setText(s, R.id.tvAlternateMobile, safe(item.getAlternateMobile()));
        setText(s, R.id.tvCurrentAddress, safe(item.getCurrentAddress()));
        setText(s, R.id.tvPermanentAddress, safe(item.getPermanentAddress()));
        setText(s, R.id.tvCity, safe(item.getCity()));
        setText(s, R.id.tvState, safe(item.getState()));
        setText(s, R.id.tvPincode, safe(item.getPincode()));
        setText(s, R.id.tvCountry, safe(item.getCountry()));

        // ----- Identity & Statutory -----
        setText(s, R.id.tvPanNumber, safe(item.getPanNumber()));
        setText(s, R.id.tvAadhaarNumber, safe(item.getAadhaarNumber()));
        setText(s, R.id.tvUanNumber, safe(item.getUanNumber()));
        setText(s, R.id.tvEsiNumber, safe(item.getEsiNumber()));

        // ----- Bank -----
        setText(s, R.id.tvAccountHolder, safe(item.getAccountHolderName()));
        setText(s, R.id.tvBankName, safe(item.getBankName()));
        setText(s, R.id.tvAccountNumber, safe(item.getAccountNumber()));
        setText(s, R.id.tvIfscCode, safe(item.getIfscCode()));
        setText(s, R.id.tvUpiId, safe(item.getUpiId()));

        // ----- Education -----
        setText(s, R.id.tvQualification, safe(item.getQualification()));
        setText(s, R.id.tvInstitution, safe(item.getInstitution()));
        setText(s, R.id.tvGrade, safe(item.getGrade()));
        setText(s, R.id.tvPercentageGrade, safe(item.getPercentageGrade()));
        setText(s, R.id.tvYearOfPassing, safe(item.getYearOfPassing()));

        // ----- Previous Employment -----
        setText(s, R.id.tvPreviousEmployer, safe(item.getPreviousEmployer()));
        setText(s, R.id.tvPreviousDesignation, safe(item.getPreviousDesignation()));
        setText(s, R.id.tvPreviousDuration, safe(item.getPreviousDuration()));
        setText(s, R.id.tvLastDrawnCtc, safe(item.getLastDrawnCtc()));
        setText(s, R.id.tvReasonForLeaving, safe(item.getReasonForLeaving()));

        // ----- Emergency -----
        setText(s, R.id.tvEmergencyName, safe(item.getEmergencyName()));
        setText(s, R.id.tvEmergencyRelationship, safe(item.getEmergencyRelationship()));
        setText(s, R.id.tvEmergencyNumber, safe(item.getEmergencyNumber()));
        setText(s, R.id.tvEmergencyAlternate, safe(item.getEmergencyAlternateNumber()));
        setText(s, R.id.tvEmergencyAddress, safe(item.getEmergencyAddress()));

        // ----- Medical -----
        setText(s, R.id.tvMedicalCondition, safe(item.getMedicalCondition()));
        setText(s, R.id.tvKnownAllergies, safe(item.getKnownAllergies()));
        setText(s, R.id.tvMedicalDetails, safe(item.getMedicalConditionDetails()));

        // ----- Nominee -----
        setText(s, R.id.tvNomineeName, safe(item.getNomineeName()));
        setText(s, R.id.tvNomineeRelationship, safe(item.getNomineeRelationship()));
        setText(s, R.id.tvNomineeDob, safeDate(item.getNomineeDob()));
        setText(s, R.id.tvSharePercentage, safe(item.getSharePercentage()));

        // ----- Remarks & record info -----
        setText(s, R.id.tvRemarks, safe(item.getRemarks()));
        setText(s, R.id.tvCreatedAt, safeDate(item.getCreatedAt()));
        setText(s, R.id.tvUpdatedAt, safeDate(item.getUpdatedAt()));

        // ----- Family list -----
        LinearLayout llFamily = s.findViewById(R.id.llFamilyContainer);
        TextView tvFamilyEmpty = s.findViewById(R.id.tvFamilyEmpty);
        List<FamilyDetailsItem> family = item.getFamilyDetails();
        if (family != null && !family.isEmpty()) {
            tvFamilyEmpty.setVisibility(View.GONE);
            for (FamilyDetailsItem f : family) {
                if (f != null) llFamily.addView(buildFamilyCard(f));
            }
        }

        // ----- Fixed documents -----
        bindDocRow(s.findViewById(R.id.rowDocAadhaarFront), s.findViewById(R.id.tvDocAadhaarFrontStatus), item.getAadhaarFrontFileURL());
        bindDocRow(s.findViewById(R.id.rowDocAadhaarBack), s.findViewById(R.id.tvDocAadhaarBackStatus), item.getAadhaarBackFileURL());
        bindDocRow(s.findViewById(R.id.rowDocPan), s.findViewById(R.id.tvDocPanStatus), item.getPanFileURL());

        // ----- Extra documents list -----
        LinearLayout llExtraDocs = s.findViewById(R.id.llExtraDocs);
        List<DocumentsItem> docs = item.getDocuments();
        if (docs != null) {
            for (DocumentsItem d : docs) {
                if (d == null) continue;
                llExtraDocs.addView(buildExtraDocRow(safe(d.getDocumentName()), d.getDocumentFileURL()));
            }
        }

        dialog.show();
    }

    // ---------------- Family card (list item) ----------------
    private View buildFamilyCard(FamilyDetailsItem f) {
        MaterialCardView card = new MaterialCardView(requireContext());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(10);
        card.setLayoutParams(lp);
        card.setRadius(dp(12));
        card.setCardElevation(0f);
        card.setStrokeWidth(dp(1));
        card.setStrokeColor(ContextCompat.getColor(requireContext(), R.color.stroke_color));
        card.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.surface_color));

        LinearLayout box = new LinearLayout(requireContext());
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(14), dp(12), dp(14), dp(12));

        box.addView(labelValue("Member Name", safe(f.getFamilyMemberName())));
        box.addView(labelValue("Relationship", safe(f.getFamilyRelationship())));
        box.addView(labelValue("Date of Birth", safeDate(f.getFamilyDob())));
        box.addView(labelValue("Mobile", safe(f.getFamilyMobile())));
        box.addView(buildExtraDocRow("Address Proof", f.getAddressProofFileURL()));

        card.addView(box);
        return card;
    }

    private View labelValue(String label, String value) {
        LinearLayout col = new LinearLayout(requireContext());
        col.setOrientation(LinearLayout.VERTICAL);
        col.setPadding(0, 0, 0, dp(10));

        TextView l = new TextView(requireContext());
        l.setText(label);
        l.setTextSize(12f);
        l.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_tertiary));

        TextView v = new TextView(requireContext());
        v.setText(value);
        v.setTextSize(14f);
        v.setTypeface(null, Typeface.BOLD);
        v.setTextIsSelectable(true);
        v.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));

        col.addView(l);
        col.addView(v);
        return col;
    }

    // ---------------- Tappable document row (same look as the fixed rows) ----------------
    private View buildExtraDocRow(String name, String url) {
        boolean has = !safe(url).equals("N/A");

        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(12), 0, dp(12));
        row.setClickable(has);
        row.setFocusable(has);
        row.setAlpha(has ? 1f : 0.5f);

        TypedValue outValue = new TypedValue();
        requireContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
        row.setBackgroundResource(outValue.resourceId);

        LinearLayout texts = new LinearLayout(requireContext());
        texts.setOrientation(LinearLayout.VERTICAL);
        texts.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView title = new TextView(requireContext());
        title.setText(name);
        title.setTextSize(14f);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));

        TextView sub = new TextView(requireContext());
        sub.setText(has ? "Tap to view" : "Not uploaded");
        sub.setTextSize(12f);
        sub.setTextColor(ContextCompat.getColor(requireContext(), has ? R.color.colorPrimaryVariant : R.color.text_tertiary));

        TextView chevron = new TextView(requireContext());
        chevron.setText("›");
        chevron.setTextSize(22f);
        chevron.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_tertiary));

        texts.addView(title);
        texts.addView(sub);
        row.addView(texts);
        row.addView(chevron);

        if (has) row.setOnClickListener(v -> openDocumentViewer(url));
        return row;
    }

    // ---------------- Null safety ----------------
    private String safe(String value) {
        if (value == null) return "N/A";
        String v = value.trim();
        if (v.isEmpty() || v.equalsIgnoreCase("null") || v.equalsIgnoreCase("undefined")) {
            return "N/A";
        }
        return v;
    }

    // "2024-05-20T00:00:00.000Z" or "2024-05-20" -> "May 20, 2024" (no timezone shift)
    private String safeDate(String rawDate) {
        String v = safe(rawDate);
        if (v.equals("N/A")) return v;
        try {
            String datePart = v.contains("T") ? v.substring(0, v.indexOf('T')) : v;
            Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(datePart);
            return date != null ? new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date) : v;
        } catch (ParseException | RuntimeException e) {
            return v;
        }
    }

    private String getInitials(String name) {
        if (name == null || name.trim().isEmpty() || name.equals("N/A")) {
            return "?";
        }
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase(Locale.getDefault());
        }
        return ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase(Locale.getDefault());
    }

    @Override
    public void onDestroyView() {
        if (shimmerLayout != null) {
            shimmerLayout.stopShimmer();
        }
        // inside onDestroyView(), next to the shimmer stop:
        if (docDialog != null && docDialog.isShowing()) docDialog.dismiss();
        docDialog = null;
        super.onDestroyView();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        docExecutor.shutdownNow();
    }
}
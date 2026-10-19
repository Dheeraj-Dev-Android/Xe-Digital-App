package app.xedigital.ai.admin.adminUI.adminDashboard;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import app.xedigital.ai.R;
import app.xedigital.ai.admin.adminModal.VisitorsAdminDetails.VisitorsItem;
import app.xedigital.ai.utills.DateTimeUtils;
import app.xedigital.ai.utills.SecurePrefManager;

public class AdminDashboardFragment extends Fragment {

    private static final String TAG = "AdminDashboardFragment";
    private static final int SCROLL_INTERVAL = 3000;
    private static final String FALLBACK_SHORT = "N/A";

    // Dynamic Material Fallback Palette for charts when backend returns identical colors
    private static final int[] SMART_DISTINCT_PALETTE = new int[]{Color.parseColor("#4285F4"), // Vibrant Blue
            Color.parseColor("#34A853"), // Vibrant Green
            Color.parseColor("#FB8C00"), // Warm Amber
            Color.parseColor("#E91E63"), // Vivid Rose
            Color.parseColor("#9C27B0"), // Purple
            Color.parseColor("#00ACC1"), // Teal
            Color.parseColor("#FF7043"), // Coral
            Color.parseColor("#3949AB")  // Indigo
    };

    private final Handler scrollHandler = new Handler(Looper.getMainLooper());
    // Client side state indicators
    private final List<VisitorsItem> allVisitorsList = new ArrayList<>();
    private final List<VisitorsItem> paginatedVisitorList = new ArrayList<>();
    private final int pageLimit = 5;
    private AdminDashboardViewModel mViewModel;
    private BirthdayEmployeesAdapter birthdayAdapter;
    private String token;
    private TextView birthdayCount;
    private RecyclerView birthdayRecyclerView;
    private LinearLayout emptyBirthdayState;
    private PieChart leavesBarChart;
    // Global UI components
    private TextView totalSignin, totalSignout, totalEmployees, totalBranches;
    private MaterialCardView btnPunchAttendance;
    // Integrated: Loading & Chart Widgets
    private CircularProgressIndicator progressBar;
    private View dashboardContentContainer;
    private PieChart signInPieChart;
    // Integrated: Paginated Visitor list UI components
    private RecyclerView rvVisitorsList;
    private LobbyVisitorAdapter visitorAdapter;
    private MaterialButton btnPreviousPage;
    private MaterialButton btnNextPage;
    private TextView tvPaginationStatus;
    private int currentPage = 1;
    private int currentPosition = 0;
    private boolean shouldAutoScroll = false;
    private final Runnable scrollRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isAdded() || birthdayAdapter == null || birthdayRecyclerView == null) {
                return;
            }
            int itemCount = birthdayAdapter.getItemCount();
            if (itemCount > 1) {
                currentPosition = (currentPosition + 1) % itemCount;
                birthdayRecyclerView.smoothScrollToPosition(currentPosition);
                scrollHandler.postDelayed(this, SCROLL_INTERVAL);
            } else {
                shouldAutoScroll = false;
            }
        }
    };
    private ShimmerFrameLayout shimmerLayout;

    public static AdminDashboardFragment newInstance() {
        return new AdminDashboardFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getContext() != null) {
            SecurePrefManager sharedPreferences = SecurePrefManager.getInstance(requireContext());
            token = sharedPreferences.getString("authToken", "");
        }
        return inflater.inflate(R.layout.fragment_admin_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        setupRecyclerView();
        setupViewModel();

        if (token != null && !token.isEmpty()) {
            String authToken = "jwt " + token;
            mViewModel.fetchDashboardData(authToken);
            mViewModel.fetchBirthdayData(authToken);
            mViewModel.fetchLeavesGraph(authToken);
            mViewModel.fetchSignInGraph(authToken);
            mViewModel.fetchVisitorsData(authToken);
        } else {
            setDashboardDefaultTexts();
        }

        btnPunchAttendance.setOnClickListener(v -> {
            try {
                NavController navController = Navigation.findNavController(v);
                navController.navigate(R.id.action_dashboard_to_visitorCheckIn);
            } catch (IllegalStateException e) {
                Log.e(TAG, "NavController not found: " + e.getMessage());
            }
        });
    }

    private void initializeViews(View view) {
        totalSignin = view.findViewById(R.id.signinCount);
        totalSignout = view.findViewById(R.id.signoutCount);
        totalEmployees = view.findViewById(R.id.employeesCount);
        totalBranches = view.findViewById(R.id.branchesCount);
        birthdayCount = view.findViewById(R.id.birthdayCount);
        birthdayRecyclerView = view.findViewById(R.id.birthdayRecyclerView);
        emptyBirthdayState = view.findViewById(R.id.emptyBirthdayState);
        leavesBarChart = view.findViewById(R.id.pieChart);
        btnPunchAttendance = view.findViewById(R.id.btnPunchAttendance);

        // Integrated views
        progressBar = view.findViewById(R.id.progressBar);
        dashboardContentContainer = view.findViewById(R.id.dashboardContentContainer);
        signInPieChart = view.findViewById(R.id.signInPieChart);
        rvVisitorsList = view.findViewById(R.id.rvVisitorsList);
        btnPreviousPage = view.findViewById(R.id.btnPreviousPage);
        btnNextPage = view.findViewById(R.id.btnNextPage);
        tvPaginationStatus = view.findViewById(R.id.tvPaginationStatus);
        shimmerLayout = view.findViewById(R.id.shimmerLayout);

        // Pagination listeners
        if (btnPreviousPage != null) {
            btnPreviousPage.setOnClickListener(v -> {
                if (currentPage > 1) {
                    currentPage--;
                    loadVisitorPageChunk();
                }
            });
        }
        if (btnNextPage != null) {
            btnNextPage.setOnClickListener(v -> {
                int maxPossibleIndex = currentPage * pageLimit;
                if (maxPossibleIndex < allVisitorsList.size()) {
                    currentPage++;
                    loadVisitorPageChunk();
                }
            });
        }
    }

    private void setupRecyclerView() {
        birthdayAdapter = new BirthdayEmployeesAdapter();
        BirthdayEmployeesAdapter.setupHorizontalScrolling(birthdayRecyclerView);
        birthdayRecyclerView.setAdapter(birthdayAdapter);
        birthdayRecyclerView.setNestedScrollingEnabled(false);

        PagerSnapHelper snapHelper = new PagerSnapHelper();
        birthdayRecyclerView.setOnFlingListener(null);
        snapHelper.attachToRecyclerView(birthdayRecyclerView);

        // Direct in-place dialog display on visitor click
        if (rvVisitorsList != null) {
            rvVisitorsList.setLayoutManager(new LinearLayoutManager(requireContext()));
            visitorAdapter = new LobbyVisitorAdapter(paginatedVisitorList, item -> {
                if (item != null) {
                    showVisitorDetailsDialog(item);
                }
            });
            rvVisitorsList.setAdapter(visitorAdapter);
        }
    }

    /**
     * Shows full visitor details directly in an in-place AlertDialog
     */
    private void showVisitorDetailsDialog(VisitorsItem visitor) {
        if (!isAdded() || getContext() == null || visitor == null) return;

        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_visitor_details, null);

        // Find dialog elements
        ShapeableImageView imgProfile = dialogView.findViewById(R.id.imgProfile);
        TextView tvFullName = dialogView.findViewById(R.id.tvFullName);
        TextView tvContact = dialogView.findViewById(R.id.tvContact);
        TextView tvEmail = dialogView.findViewById(R.id.tvEmail);
        TextView tvWhomToMeet = dialogView.findViewById(R.id.tvWhomToMeet);
        TextView tvCheckInDate = dialogView.findViewById(R.id.tvCheckInDate);
        TextView tvCheckOutDate = dialogView.findViewById(R.id.tvCheckOutDate);
        TextView tvCheckInTime = dialogView.findViewById(R.id.tvCheckInTime);
        TextView tvCheckOutTime = dialogView.findViewById(R.id.tvCheckOutTime);
        TextView tvCompanyFrom = dialogView.findViewById(R.id.tvCompanyFrom);
        TextView tvLaptopSerial = dialogView.findViewById(R.id.tvLaptopSerial);

        // Set Data
        tvFullName.setText(visitor.getName() != null ? visitor.getName() : "N/A");
        tvContact.setText(visitor.getContact() != null ? visitor.getContact() : "N/A");
        tvEmail.setText(visitor.getEmail() != null ? visitor.getEmail() : "N/A");

        if (visitor.getWhomToMeet() != null) {
            String hostFirst = visitor.getWhomToMeet().getFirstname() != null ? visitor.getWhomToMeet().getFirstname() : "";
            String hostLast = visitor.getWhomToMeet().getLastname() != null ? visitor.getWhomToMeet().getLastname() : "";
            tvWhomToMeet.setText(String.format("%s %s", hostFirst, hostLast).trim());
        } else {
            tvWhomToMeet.setText("N/A");
        }

        // Date and Time fields
        tvCheckInDate.setText(DateTimeUtils.getDayOfWeekAndDate(visitor.getSignIn()));
        tvCheckOutDate.setText(visitor.getSignOut() != null ? DateTimeUtils.getDayOfWeekAndDate(visitor.getSignOut()) : "N/A");
        tvCheckInTime.setText(visitor.getSignIn() != null ? DateTimeUtils.extractTime(visitor.getSignIn()) : "N/A");
        tvCheckOutTime.setText(visitor.getSignOut() != null ? DateTimeUtils.extractTime(visitor.getSignOut()) : "N/A");

        // Company and serial info
        tvCompanyFrom.setText(visitor.getCompanyFrom() != null ? visitor.getCompanyFrom() : "N/A");
        tvLaptopSerial.setText(visitor.getSerialNumber() != null ? visitor.getSerialNumber() : "N/A");

        // Load profile image
        if (visitor.getProfileImagePath() != null && !visitor.getProfileImagePath().trim().isEmpty()) {
            Glide.with(this).load(visitor.getProfileImagePath()).placeholder(R.drawable.ic_profile_placeholder).into(imgProfile);
        } else {
            imgProfile.setImageResource(R.drawable.ic_profile_placeholder);
        }

        // Display AlertDialog
        new AlertDialog.Builder(requireContext()).setView(dialogView).setCancelable(true).setPositiveButton("Close", (dialog, which) -> dialog.dismiss()).show();
    }

    private void setupViewModel() {
        mViewModel = new ViewModelProvider(this).get(AdminDashboardViewModel.class);

        // Sync visual spinner layout overlays
        mViewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
            if (loading != null && loading) {
                if (shimmerLayout != null) {
                    shimmerLayout.setVisibility(View.VISIBLE);
                    shimmerLayout.startShimmer();
                }
                if (dashboardContentContainer != null) {
                    dashboardContentContainer.setVisibility(View.INVISIBLE);
                }
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
            } else {
                if (shimmerLayout != null) {
                    shimmerLayout.stopShimmer();
                    shimmerLayout.setVisibility(View.GONE);
                }
                if (dashboardContentContainer != null) {
                    dashboardContentContainer.setVisibility(View.VISIBLE);
                    dashboardContentContainer.setAlpha(1.0f);
                }
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

        mViewModel.getDashboardData().observe(getViewLifecycleOwner(), data -> {
            if (data != null) {
                String totalVisitors = String.valueOf(data.getTotalVisitors());
                String checkInVisitors = String.valueOf(data.getTotalSigninVisitors());
                String checkOutVisitors = String.valueOf(data.getTotalSignoutVisitors());

                totalSignin.setText(String.format("%s / %s", checkInVisitors, totalVisitors));
                totalSignout.setText(String.format("%s / %s", checkOutVisitors, totalVisitors));
                totalEmployees.setText(String.valueOf(data.getTotalEmployees()));
                totalBranches.setText(String.valueOf(data.getTotalBranches()));
            } else {
                setDashboardDefaultTexts();
            }
        });

        mViewModel.getBirthdayData().observe(getViewLifecycleOwner(), birthdayEmployees -> {
            scrollHandler.removeCallbacks(scrollRunnable);

            if (birthdayEmployees != null && !birthdayEmployees.isEmpty()) {
                birthdayCount.setText(String.valueOf(birthdayEmployees.size()));
                birthdayAdapter.updateBirthdayEmployees(birthdayEmployees);

                birthdayRecyclerView.setVisibility(View.VISIBLE);
                emptyBirthdayState.setVisibility(View.GONE);

                currentPosition = 0;
                if (birthdayEmployees.size() > 1) {
                    shouldAutoScroll = true;
                    scrollHandler.postDelayed(scrollRunnable, SCROLL_INTERVAL);
                } else {
                    shouldAutoScroll = false;
                }
            } else {
                birthdayCount.setText("0");
                birthdayAdapter.updateBirthdayEmployees(new ArrayList<>());
                birthdayRecyclerView.setVisibility(View.GONE);
                emptyBirthdayState.setVisibility(View.VISIBLE);
                shouldAutoScroll = false;
            }
        });

        // Observation chain: Approved Leave Graph
        mViewModel.getLeavesGraphData().observe(getViewLifecycleOwner(), response -> {
            if (response != null && response.getData() != null) {
                updateLeavePieChart(leavesBarChart, response.getData());
            } else {
                showChartEmptyState(leavesBarChart);
            }
        });

        // Observation chain: Sign-In Graph
        mViewModel.getSignInGraphData().observe(getViewLifecycleOwner(), response -> {
            if (response != null && response.getData() != null && response.getData().getSignInVisitors() != null) {
                updateSignInPieChart(signInPieChart, response.getData().getSignInVisitors());
            } else {
                showChartEmptyState(signInPieChart);
            }
        });

        // Observation chain: Checked-In Visitors List
        mViewModel.getVisitorsData().observe(getViewLifecycleOwner(), visitors -> {
            if (visitors != null) {
                allVisitorsList.clear();
                allVisitorsList.addAll(visitors);
                currentPage = 1;
                loadVisitorPageChunk();
            }
        });

        mViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (getContext() != null && errorMessage != null && !errorMessage.trim().isEmpty()) {
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadVisitorPageChunk() {
        int totalRecords = allVisitorsList.size();
        if (totalRecords == 0) {
            paginatedVisitorList.clear();
            if (visitorAdapter != null) visitorAdapter.notifyDataSetChanged();
            if (tvPaginationStatus != null) tvPaginationStatus.setText("0-0 of 0");
            if (btnPreviousPage != null) btnPreviousPage.setEnabled(false);
            if (btnNextPage != null) btnNextPage.setEnabled(false);
            return;
        }

        int startIndex = (currentPage - 1) * pageLimit;
        int endIndex = Math.min(startIndex + pageLimit, totalRecords);

        List<VisitorsItem> subList = allVisitorsList.subList(startIndex, endIndex);
        paginatedVisitorList.clear();
        paginatedVisitorList.addAll(subList);

        if (visitorAdapter != null) {
            visitorAdapter.notifyDataSetChanged();
        }

        if (tvPaginationStatus != null) {
            String trackingText = String.format(Locale.getDefault(), "%d-%d of %d", (startIndex + 1), endIndex, totalRecords);
            tvPaginationStatus.setText(trackingText);
        }

        if (btnPreviousPage != null) btnPreviousPage.setEnabled(currentPage > 1);
        if (btnNextPage != null) btnNextPage.setEnabled(endIndex < totalRecords);
    }

    private void setDashboardDefaultTexts() {
        if (totalSignin != null) totalSignin.setText(FALLBACK_SHORT);
        if (totalSignout != null) totalSignout.setText(FALLBACK_SHORT);
        if (totalEmployees != null) totalEmployees.setText(FALLBACK_SHORT);
        if (totalBranches != null) totalBranches.setText(FALLBACK_SHORT);
    }

    private void showChartEmptyState(PieChart chart) {
        if (chart != null) {
            chart.clear();
            chart.setNoDataText("No Data Available");
            chart.setNoDataTextColor(Color.GRAY);
            chart.invalidate();
        }
    }

    private void updateLeavePieChart(PieChart chart, app.xedigital.ai.admin.adminModal.LeaveGraph.Data leaveGraphData) {
        if (chart == null) return;
        if (leaveGraphData == null || leaveGraphData.getGraphData() == null || leaveGraphData.getGraphLabels() == null) {
            showChartEmptyState(chart);
            return;
        }
        bindPieChartData(chart, leaveGraphData.getGraphData(), leaveGraphData.getGraphLabels(), leaveGraphData.getGraphBackground());
    }

    private void updateSignInPieChart(PieChart chart, app.xedigital.ai.admin.adminModal.SignInGraph.SignInVisitors signInVisitors) {
        if (chart == null) return;
        if (signInVisitors == null || signInVisitors.getGraphData() == null || signInVisitors.getGraphLabels() == null) {
            showChartEmptyState(chart);
            return;
        }
        bindPieChartData(chart, signInVisitors.getGraphData(), signInVisitors.getGraphLabels(), signInVisitors.getGraphBackground());
    }

    /**
     * Checks whether all colors returned by the API are identical.
     * When true, we fallback to a distinct palette so each slice is distinguishable.
     */
    private boolean areAllColorsIdentical(List<String> colors) {
        if (colors == null || colors.size() <= 1) return false;
        String first = colors.get(0) != null ? colors.get(0).replaceAll("\\s+", "").toLowerCase() : "";
        for (int i = 1; i < colors.size(); i++) {
            String current = colors.get(i) != null ? colors.get(i).replaceAll("\\s+", "").toLowerCase() : "";
            if (!first.equals(current)) {
                return false; // Found distinct color entries in API response
            }
        }
        return true; // All entries in API list are the same color
    }

    /**
     * Highly Robust color decoder. Decodes Hex codes, RGB, RGBA CSS strings, and direct common English color names.
     */
    private int parseColorHelper(String colorStr) {
        if (colorStr == null || colorStr.trim().isEmpty()) {
            return Color.LTGRAY;
        }
        String cleanStr = colorStr.replaceAll("\\s+", "").toLowerCase();

        switch (cleanStr) {
            case "orange":
                return 0xFFFFA500;
            case "pink":
                return 0xFFFFC0CB;
            case "gold":
                return 0xFFFFD700;
            case "brown":
                return 0xFFA52A2A;
            case "violet":
                return 0xFFEE82EE;
            case "indigo":
                return 0xFF4B0082;
            case "turquoise":
                return 0xFF40E0D0;
            case "plum":
                return 0xFFDDA0DD;
            case "lime":
                return 0xFF00FF00;
            case "maroon":
                return 0xFF800000;
            case "navy":
                return 0xFF000080;
            case "olive":
                return 0xFF808000;
            case "teal":
                return 0xFF008080;
            case "green":
                return 0xFF008000;
            case "blue":
                return 0xFF0000FF;
            case "red":
                return 0xFFFF0000;
            case "yellow":
                return 0xFFFFFF00;
            case "purple":
                return 0xFF800080;
            case "cyan":
                return 0xFF00FFFF;
            case "magenta":
                return 0xFFFF00FF;
            case "silver":
                return 0xFFC0C0C0;
            case "gray":
            case "grey":
                return 0xFF808080;
            case "black":
                return 0xFF000000;
            case "white":
                return 0xFFFFFFFF;
        }

        try {
            if (cleanStr.startsWith("rgba")) {
                String content = cleanStr.substring(cleanStr.indexOf("(") + 1, cleanStr.indexOf(")"));
                String[] segments = content.split(",");
                if (segments.length == 4) {
                    int r = Integer.parseInt(segments[0]);
                    int g = Integer.parseInt(segments[1]);
                    int b = Integer.parseInt(segments[2]);
                    float a = Float.parseFloat(segments[3]);
                    int alpha = Math.round(a * 255);
                    return Color.argb(alpha, r, g, b);
                }
            } else if (cleanStr.startsWith("rgb")) {
                String content = cleanStr.substring(cleanStr.indexOf("(") + 1, cleanStr.indexOf(")"));
                String[] segments = content.split(",");
                if (segments.length == 3) {
                    int r = Integer.parseInt(segments[0]);
                    int g = Integer.parseInt(segments[1]);
                    int b = Integer.parseInt(segments[2]);
                    return Color.rgb(r, g, b);
                }
            } else if (cleanStr.startsWith("#")) {
                return Color.parseColor(colorStr.trim());
            } else {
                return Color.parseColor(cleanStr);
            }
        } catch (Exception e) {
            Log.e(TAG, "Color structure matching failed: " + colorStr + " | " + e.getMessage());
        }
        return Color.LTGRAY;
    }

    private void bindPieChartData(PieChart chart, List<Integer> values, List<String> labels, List<String> colors) {
        if (values.isEmpty() || labels.isEmpty() || values.size() != labels.size()) {
            showChartEmptyState(chart);
            return;
        }

        List<PieEntry> entries = new ArrayList<>();
        for (int i = 0; i < values.size(); i++) {
            Integer val = values.get(i);
            String label = labels.get(i);
            entries.add(new PieEntry(val != null ? val : 0f, label != null ? label : "Data Not Available"));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setSliceSpace(1.2f);
        dataSet.setSelectionShift(10f);

        // Smart color resolution: If API returned identical colors or empty array, assign distinct colors!
        List<Integer> colorInts = new ArrayList<>();
        boolean useDistinctPalette = (colors == null || colors.isEmpty() || areAllColorsIdentical(colors));

        for (int i = 0; i < values.size(); i++) {
            if (useDistinctPalette) {
                colorInts.add(SMART_DISTINCT_PALETTE[i % SMART_DISTINCT_PALETTE.length]);
            } else {
                colorInts.add(parseColorHelper(colors.get(i)));
            }
        }
        dataSet.setColors(colorInts);

        PieData pieData = new PieData(dataSet);
        pieData.setDrawValues(false);

        chart.getDescription().setEnabled(false);
        chart.setDrawHoleEnabled(false);
        chart.setDrawEntryLabels(false);

        // Extra bottom offset so Legend has ample room to display
        chart.setExtraBottomOffset(10f);

        // Styling the Legend layout beautifully
        Legend legend = chart.getLegend();
        if (legend != null) {
            legend.setEnabled(true);
            legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
            legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
            legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
            legend.setDrawInside(false);
            legend.setWordWrapEnabled(true);

            // Layout padding controls
            legend.setXEntrySpace(8f);
            legend.setYEntrySpace(6f);
            legend.setYOffset(10f);

            // Typography & Colors
            legend.setTextSize(10f);
            legend.setTextColor(Color.parseColor("#4A4A4A"));
        }

        chart.setData(pieData);
        OptimizedMarkerView mv = new OptimizedMarkerView(chart.getContext(), R.layout.marker_view, values);
        mv.setChartView(chart);
        chart.setMarker(mv);

        chart.animateY(1400, Easing.EaseInOutQuad);
        chart.invalidate();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (shouldAutoScroll && birthdayAdapter != null && birthdayAdapter.getItemCount() > 1) {
            scrollHandler.removeCallbacks(scrollRunnable);
            scrollHandler.postDelayed(scrollRunnable, SCROLL_INTERVAL);
        }
    }

    @Override
    public void onPause() {
        scrollHandler.removeCallbacks(scrollRunnable);
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        scrollHandler.removeCallbacks(scrollRunnable);

        // Null out all view references
        totalSignin = null;
        totalSignout = null;
        totalEmployees = null;
        totalBranches = null;
        birthdayCount = null;
        birthdayRecyclerView = null;
        emptyBirthdayState = null;
        leavesBarChart = null;
        birthdayAdapter = null;
        btnPunchAttendance = null;

        // Nullify integrated views
        progressBar = null;
        dashboardContentContainer = null;
        signInPieChart = null;
        rvVisitorsList = null;
        btnPreviousPage = null;
        btnNextPage = null;
        tvPaginationStatus = null;
        visitorAdapter = null;
        shimmerLayout = null;

        super.onDestroyView();
    }

    // ─────────────────────────────────────────────────────────────
    // OptimizedMarkerView
    // ─────────────────────────────────────────────────────────────
    private static class OptimizedMarkerView extends MarkerView {

        private final TextView markerTextView;
        private final float precomputedTotal;

        public OptimizedMarkerView(Context context, int layoutResource, List<Integer> values) {
            super(context, layoutResource);

            float sum = 0f;
            if (values != null) {
                for (Integer val : values) {
                    if (val != null) sum += val;
                }
            }
            this.precomputedTotal = sum;
            this.markerTextView = findViewById(R.id.marker_text);
        }

        @Override
        public void refreshContent(Entry e, Highlight highlight) {
            if (e instanceof PieEntry && markerTextView != null) {
                PieEntry pieEntry = (PieEntry) e;
                float percentage = (precomputedTotal > 0) ? (pieEntry.getValue() / precomputedTotal) * 100f : 0f;
                String label = pieEntry.getLabel() != null ? pieEntry.getLabel() : "Data Not Available";
                markerTextView.setText(String.format(Locale.US, "%s\n%.1f%%", label, percentage));
            }
            super.refreshContent(e, highlight);
        }

        @Override
        public MPPointF getOffset() {
            return new MPPointF(-(getWidth() / 2f), -getHeight());
        }
    }
}
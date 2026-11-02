package app.xedigital.ai.ui.claim_management;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

import app.xedigital.ai.R;
import app.xedigital.ai.model.approveClaim.EmployeeClaimdataItem;
import app.xedigital.ai.utills.CustomDialogHelper;
import app.xedigital.ai.utills.DateTimeUtils;

public class ApproveClaimDetailsFragment extends Fragment {

    private EmployeeClaimdataItem claimData;

    // Hero
    private TextView txtClaimId, txtStatus, txtAppliedDate, txtTravelRefId;
    private TextView txtStatusRm, txtStatusHr, txtTotalAmount;
    private View rowDateTravelRef, rowStatusRmHr;

    // Meeting
    private TextView txtProjectName, txtMeetingType, txtPurposeOfMeeting, txtComment;
    private View cardMeetingDetails, rowProjectMeeting;

    // Travel
    private TextView txtTravelCategory, txtModeOfTransport, txtFromTo, txtDistance;
    private TextView txtRestaurant, txtPersons;
    private View cardTravelDetails, rowTravelCategoryMode, rowRouteDistance, rowRestaurantPersons;

    // Parking & Accommodation
    private TextView txtParkingLocation, txtParkingDate;
    private TextView txtAccommodationType, txtAccommodationName, txtCheckIn, txtCheckOut;
    private View cardParkingAccommodation, rowParkingLocationDate, rowAccommodationTypeName, boxCheckInOut;

    // Fuel & Toll
    private TextView txtFuelType, txtFuelStation, txtFuelQuantity, txtVehicleNumber;
    private TextView txtTollPlaza, txtTollLocation;
    private View cardFuelToll, rowFuelTypeStation, rowFuelQuantityVehicle, rowTollPlazaLocation;

    // Expense & Billing
    private TextView txtExpenseType, txtExpenseCategory, txtBillNumber, txtBillingPeriod, txtClaimState;
    private View cardExpenseBilling, rowExpenseTypeCategory, rowBillNumberPeriod;

    // Action
    private MaterialButton btnDocumentView;

    public ApproveClaimDetailsFragment() {
        // Required empty public constructor
    }

    public static ApproveClaimDetailsFragment newInstance(EmployeeClaimdataItem claimData) {
        ApproveClaimDetailsFragment fragment = new ApproveClaimDetailsFragment();
        Bundle args = new Bundle();
        args.putSerializable("claimData", claimData);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            claimData = (EmployeeClaimdataItem) getArguments().getSerializable("claimData");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_approve_claim_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (claimData == null) {
            CustomDialogHelper.showErrorDialog(requireContext(),
                    "Error", "No claim data available", () -> {
                        if (isAdded()) {
                            requireActivity().getSupportFragmentManager().popBackStack();
                        }
                    });
            return;
        }

        initViews(view);
        bindData();
        setupActions();
    }

    private void initViews(View view) {
        // Hero
        txtClaimId = view.findViewById(R.id.txtClaimId);
        txtStatus = view.findViewById(R.id.txtStatus);
        txtAppliedDate = view.findViewById(R.id.txtAppliedDate);
        txtTravelRefId = view.findViewById(R.id.txtTravelRefId);
        txtStatusRm = view.findViewById(R.id.txtStatusRm);
        txtStatusHr = view.findViewById(R.id.txtStatusHr);
        txtTotalAmount = view.findViewById(R.id.txtTotalAmount);
        rowDateTravelRef = view.findViewById(R.id.rowDateTravelRef);
        rowStatusRmHr = view.findViewById(R.id.rowStatusRmHr);

        // Meeting
        txtProjectName = view.findViewById(R.id.txtProjectName);
        txtMeetingType = view.findViewById(R.id.txtMeetingType);
        txtPurposeOfMeeting = view.findViewById(R.id.txtPurposeOfMeeting);
        txtComment = view.findViewById(R.id.txtComment);
        cardMeetingDetails = view.findViewById(R.id.cardMeetingDetails);
        rowProjectMeeting = view.findViewById(R.id.rowProjectMeeting);

        // Travel
        txtTravelCategory = view.findViewById(R.id.txtTravelCategory);
        txtModeOfTransport = view.findViewById(R.id.txtModeOfTransport);
        txtFromTo = view.findViewById(R.id.txtFromTo);
        txtDistance = view.findViewById(R.id.txtDistance);
        txtRestaurant = view.findViewById(R.id.txtRestaurant);
        txtPersons = view.findViewById(R.id.txtPersons);
        cardTravelDetails = view.findViewById(R.id.cardTravelDetails);
        rowTravelCategoryMode = view.findViewById(R.id.rowTravelCategoryMode);
        rowRouteDistance = view.findViewById(R.id.rowRouteDistance);
        rowRestaurantPersons = view.findViewById(R.id.rowRestaurantPersons);

        // Parking & Accommodation
        txtParkingLocation = view.findViewById(R.id.txtParkingLocation);
        txtParkingDate = view.findViewById(R.id.txtParkingDate);
        txtAccommodationType = view.findViewById(R.id.txtAccommodationType);
        txtAccommodationName = view.findViewById(R.id.txtAccommodationName);
        txtCheckIn = view.findViewById(R.id.txtCheckIn);
        txtCheckOut = view.findViewById(R.id.txtCheckOut);
        cardParkingAccommodation = view.findViewById(R.id.cardParkingAccommodation);
        rowParkingLocationDate = view.findViewById(R.id.rowParkingLocationDate);
        rowAccommodationTypeName = view.findViewById(R.id.rowAccommodationTypeName);
        boxCheckInOut = view.findViewById(R.id.boxCheckInOut);

        // Fuel & Toll
        txtFuelType = view.findViewById(R.id.txtFuelType);
        txtFuelStation = view.findViewById(R.id.txtFuelStation);
        txtFuelQuantity = view.findViewById(R.id.txtFuelQuantity);
        txtVehicleNumber = view.findViewById(R.id.txtVehicleNumber);
        txtTollPlaza = view.findViewById(R.id.txtTollPlaza);
        txtTollLocation = view.findViewById(R.id.txtTollLocation);
        cardFuelToll = view.findViewById(R.id.cardFuelToll);
        rowFuelTypeStation = view.findViewById(R.id.rowFuelTypeStation);
        rowFuelQuantityVehicle = view.findViewById(R.id.rowFuelQuantityVehicle);
        rowTollPlazaLocation = view.findViewById(R.id.rowTollPlazaLocation);

        // Expense & Billing
        txtExpenseType = view.findViewById(R.id.txtExpenseType);
        txtExpenseCategory = view.findViewById(R.id.txtExpenseCategory);
        txtBillNumber = view.findViewById(R.id.txtBillNumber);
        txtBillingPeriod = view.findViewById(R.id.txtBillingPeriod);
        txtClaimState = view.findViewById(R.id.txtClaimState);
        cardExpenseBilling = view.findViewById(R.id.cardExpenseBilling);
        rowExpenseTypeCategory = view.findViewById(R.id.rowExpenseTypeCategory);
        rowBillNumberPeriod = view.findViewById(R.id.rowBillNumberPeriod);

        // Action
        btnDocumentView = view.findViewById(R.id.btnDocumentView);
    }

    private void bindData() {
        // ===== HERO =====
        String displayId = isValid(claimData.getClaimId()) ? claimData.getClaimId() : claimData.getId();
        txtClaimId.setText(displayId != null ? displayId : "N/A");

        bindText(txtStatus, claimData.getStatus());

        boolean hasAppliedDate = bindText(txtAppliedDate,
                DateTimeUtils.formatToReadableDate(claimData.getClaimDate()));
        boolean hasTravelRef = bindText(txtTravelRefId, claimData.getTravelRefId());
        setVisibleIfAny(rowDateTravelRef, hasAppliedDate, hasTravelRef);

        boolean hasStatusRm = bindText(txtStatusRm, claimData.getStatusRm());
        boolean hasStatusHr = bindText(txtStatusHr, claimData.getStatusHr());
        setVisibleIfAny(rowStatusRmHr, hasStatusRm, hasStatusHr);

        double amount = claimData.getTotalamount();
        String currency = isValid(claimData.getCurrency()) ? claimData.getCurrency() : "₹";
        txtTotalAmount.setText(currency + amount);

        // ===== MEETING DETAILS =====
        boolean hasProject = bindText(txtProjectName, claimData.getProject());
        boolean hasMeetingType = bindText(txtMeetingType, claimData.getMeeting());
        setVisibleIfAny(rowProjectMeeting, hasProject, hasMeetingType);

        boolean hasPurpose = bindText(txtPurposeOfMeeting, claimData.getPerposeofmeet());

        String comment = claimData.getComment();
        if (!isValid(comment)) {
            comment = claimData.getDescription();
        }
        boolean hasComment = bindText(txtComment, comment);

        setVisibleIfAny(cardMeetingDetails, hasProject, hasMeetingType, hasPurpose, hasComment);

        // ===== TRAVEL DETAILS =====
        boolean hasTravelCat = bindText(txtTravelCategory, claimData.getTravelCategory());
        boolean hasMode = bindText(txtModeOfTransport, claimData.getModeoftransport());
        setVisibleIfAny(rowTravelCategoryMode, hasTravelCat, hasMode);

        String fromAddr = claimData.getFromaddress();
        String toAddr = claimData.getToaddress();
        String route = null;
        if (isValid(fromAddr) && isValid(toAddr)) {
            route = fromAddr + " → " + toAddr;
        } else if (isValid(fromAddr)) {
            route = fromAddr;
        } else if (isValid(toAddr)) {
            route = toAddr;
        } else if (isValid(claimData.getLocation())) {
            route = claimData.getLocation();
        }
        boolean hasRoute = bindText(txtFromTo, route);

        boolean hasDistance = bindText(txtDistance,
                claimData.getDistance() > 0 ? claimData.getDistance() + " Km" : null);
        setVisibleIfAny(rowRouteDistance, hasRoute, hasDistance);

        boolean hasRestaurant = bindText(txtRestaurant, claimData.getRestaurant());
        boolean hasPersons = bindText(txtPersons,
                claimData.getPersons() > 0 ? String.valueOf(claimData.getPersons()) : null);
        setVisibleIfAny(rowRestaurantPersons, hasRestaurant, hasPersons);

        setVisibleIfAny(cardTravelDetails,
                hasTravelCat, hasMode, hasRoute, hasDistance, hasRestaurant, hasPersons);

        // ===== PARKING & ACCOMMODATION =====
        boolean hasParkingLoc = bindText(txtParkingLocation, claimData.getParkinglocation());
        boolean hasParkingDate = bindText(txtParkingDate,
                DateTimeUtils.formatToReadableDate(claimData.getParkingdate()));
        setVisibleIfAny(rowParkingLocationDate, hasParkingLoc, hasParkingDate);

        boolean hasAccType = bindText(txtAccommodationType, claimData.getAccommodationtype());
        boolean hasAccName = bindText(txtAccommodationName, claimData.getAccommodationname());
        setVisibleIfAny(rowAccommodationTypeName, hasAccType, hasAccName);

        boolean hasCheckIn = bindText(txtCheckIn,
                DateTimeUtils.formatToReadableDate(claimData.getCheckin()));
        boolean hasCheckOut = bindText(txtCheckOut,
                DateTimeUtils.formatToReadableDate(claimData.getCheckout()));
        setVisibleIfAny(boxCheckInOut, hasCheckIn, hasCheckOut);

        setVisibleIfAny(cardParkingAccommodation,
                hasParkingLoc, hasParkingDate, hasAccType, hasAccName, hasCheckIn, hasCheckOut);

        // ===== FUEL & TOLL =====
        boolean hasFuelType = bindText(txtFuelType, claimData.getFueltype());
        boolean hasFuelStation = bindText(txtFuelStation, claimData.getFuelstationname());
        setVisibleIfAny(rowFuelTypeStation, hasFuelType, hasFuelStation);

        boolean hasFuelQty = bindText(txtFuelQuantity, claimData.getFuelquantity());
        boolean hasVehicle = bindText(txtVehicleNumber, claimData.getVehiclenumber());
        setVisibleIfAny(rowFuelQuantityVehicle, hasFuelQty, hasVehicle);

        boolean hasTollPlaza = bindText(txtTollPlaza, claimData.getTollplazaname());
        boolean hasTollLoc = bindText(txtTollLocation, claimData.getTolllocation());
        setVisibleIfAny(rowTollPlazaLocation, hasTollPlaza, hasTollLoc);

        setVisibleIfAny(cardFuelToll,
                hasFuelType, hasFuelStation, hasFuelQty, hasVehicle, hasTollPlaza, hasTollLoc);

        // ===== EXPENSE & BILLING =====
        boolean hasExpType = bindText(txtExpenseType, claimData.getExpenseType());
        boolean hasExpCat = bindText(txtExpenseCategory, claimData.getExpancecategory());
        setVisibleIfAny(rowExpenseTypeCategory, hasExpType, hasExpCat);

        boolean hasBillNum = bindText(txtBillNumber, claimData.getBillnumber());
        boolean hasBillPeriod = bindText(txtBillingPeriod, claimData.getBillingperiod());
        setVisibleIfAny(rowBillNumberPeriod, hasBillNum, hasBillPeriod);

        boolean hasState = bindText(txtClaimState, claimData.getState());

        setVisibleIfAny(cardExpenseBilling,
                hasExpType, hasExpCat, hasBillNum, hasBillPeriod, hasState);
    }

    private void setupActions() {
        btnDocumentView.setOnClickListener(v -> {
            String docUrl = claimData.getDocFileURL();
            String docUrlOne = claimData.getDocFileURLOne();

            String urlToOpen = null;
            if (isValid(docUrl)) {
                urlToOpen = docUrl;
            } else if (isValid(docUrlOne)) {
                urlToOpen = docUrlOne;
            }

            if (urlToOpen != null) {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlToOpen));
                    startActivity(intent);
                } catch (Exception e) {
                    CustomDialogHelper.showErrorDialog(requireContext(),
                            "Error", "Unable to open document");
                }
            } else {
                CustomDialogHelper.showInfoDialog(requireContext(),
                        "Documents", "No documents available for this claim");
            }
        });
    }

    // ==================== HELPERS ====================

    private boolean isValid(String value) {
        return value != null && !value.trim().isEmpty()
                && !value.trim().equalsIgnoreCase("null")
                && !value.trim().equalsIgnoreCase("N/A");
    }

    private boolean bindText(TextView tv, String value) {
        if (tv == null) return false;
        boolean has = isValid(value);
        View wrapper = (View) tv.getParent();
        if (has) {
            tv.setText(value);
            if (wrapper != null) wrapper.setVisibility(View.VISIBLE);
        } else {
            if (wrapper != null) wrapper.setVisibility(View.GONE);
        }
        return has;
    }

    private void setVisibleIfAny(View container, boolean... flags) {
        if (container == null) return;
        boolean any = false;
        for (boolean f : flags) {
            if (f) {
                any = true;
                break;
            }
        }
        container.setVisibility(any ? View.VISIBLE : View.GONE);
    }
}
package app.xedigital.ai.ui.claim_management;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import app.xedigital.ai.R;
import app.xedigital.ai.adapter.ImageAdapter;
import app.xedigital.ai.model.employeeClaim.EmployeeClaimdataItem;

public class ClaimDetailsFragment extends Fragment {

    // ---- Existing TextView IDs (unchanged) ----
    private TextView txtClaimId, txtProjectName, txtMeetingType, txtPurposeOfMeeting, txtComment;
    private TextView txtTravelCategory, txtModeOfTransport, txtFromTo, txtDistance, txtRestaurant, txtPersons;
    private TextView txtTotalAmount, txtAppliedDate, txtStatus, txtStatusRm, txtStatusHr, txtTravelRefId;
    private TextView txtExpenseType, txtExpenseCategory, txtBillNumber, txtBillingPeriod, txtClaimState;
    private TextView txtFuelType, txtFuelStation, txtFuelQuantity, txtVehicleNumber, txtTollPlaza, txtTollLocation;
    private TextView txtParkingLocation, txtParkingDate, txtAccommodationType, txtAccommodationName, txtCheckIn, txtCheckOut;

    // ---- New Row / Card container views for hide-if-empty logic ----
    private View rowDateTravelRef, rowStatusRmHr;
    private View cardExpenseBilling, rowExpenseTypeCategory, rowBillNumberPeriod;
    private View cardFuelToll, rowFuelTypeStation, rowFuelQuantityVehicle, rowTollPlazaLocation;
    private View cardParkingAccommodation, rowParkingLocationDate, rowAccommodationTypeName, boxCheckInOut;
    private View cardMeetingDetails, rowProjectMeeting;
    private View cardTravelDetails, rowTravelCategoryMode, rowRouteDistance, rowRestaurantPersons;

    private MaterialButton btnDocumentView, btnPrintClaim;
    private EmployeeClaimdataItem claimData;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_claim_details, container, false);

        txtClaimId = view.findViewById(R.id.txtClaimId);
        txtProjectName = view.findViewById(R.id.txtProjectName);
        txtMeetingType = view.findViewById(R.id.txtMeetingType);
        txtPurposeOfMeeting = view.findViewById(R.id.txtPurposeOfMeeting);
        txtComment = view.findViewById(R.id.txtComment);
        txtTravelCategory = view.findViewById(R.id.txtTravelCategory);
        txtModeOfTransport = view.findViewById(R.id.txtModeOfTransport);
        txtFromTo = view.findViewById(R.id.txtFromTo);
        txtDistance = view.findViewById(R.id.txtDistance);
        txtRestaurant = view.findViewById(R.id.txtRestaurant);
        txtPersons = view.findViewById(R.id.txtPersons);
        txtTotalAmount = view.findViewById(R.id.txtTotalAmount);
        txtAppliedDate = view.findViewById(R.id.txtAppliedDate);
        txtStatus = view.findViewById(R.id.txtStatus);
        txtStatusRm = view.findViewById(R.id.txtStatusRm);
        txtStatusHr = view.findViewById(R.id.txtStatusHr);
        txtTravelRefId = view.findViewById(R.id.txtTravelRefId);

        txtExpenseType = view.findViewById(R.id.txtExpenseType);
        txtExpenseCategory = view.findViewById(R.id.txtExpenseCategory);
        txtBillNumber = view.findViewById(R.id.txtBillNumber);
        txtBillingPeriod = view.findViewById(R.id.txtBillingPeriod);
        txtClaimState = view.findViewById(R.id.txtClaimState);

        txtFuelType = view.findViewById(R.id.txtFuelType);
        txtFuelStation = view.findViewById(R.id.txtFuelStation);
        txtFuelQuantity = view.findViewById(R.id.txtFuelQuantity);
        txtVehicleNumber = view.findViewById(R.id.txtVehicleNumber);
        txtTollPlaza = view.findViewById(R.id.txtTollPlaza);
        txtTollLocation = view.findViewById(R.id.txtTollLocation);

        txtParkingLocation = view.findViewById(R.id.txtParkingLocation);
        txtParkingDate = view.findViewById(R.id.txtParkingDate);
        txtAccommodationType = view.findViewById(R.id.txtAccommodationType);
        txtAccommodationName = view.findViewById(R.id.txtAccommodationName);
        txtCheckIn = view.findViewById(R.id.txtCheckIn);
        txtCheckOut = view.findViewById(R.id.txtCheckOut);

        // New row/card containers
        rowDateTravelRef = view.findViewById(R.id.rowDateTravelRef);
        rowStatusRmHr = view.findViewById(R.id.rowStatusRmHr);

        cardExpenseBilling = view.findViewById(R.id.cardExpenseBilling);
        rowExpenseTypeCategory = view.findViewById(R.id.rowExpenseTypeCategory);
        rowBillNumberPeriod = view.findViewById(R.id.rowBillNumberPeriod);

        cardFuelToll = view.findViewById(R.id.cardFuelToll);
        rowFuelTypeStation = view.findViewById(R.id.rowFuelTypeStation);
        rowFuelQuantityVehicle = view.findViewById(R.id.rowFuelQuantityVehicle);
        rowTollPlazaLocation = view.findViewById(R.id.rowTollPlazaLocation);

        cardParkingAccommodation = view.findViewById(R.id.cardParkingAccommodation);
        rowParkingLocationDate = view.findViewById(R.id.rowParkingLocationDate);
        rowAccommodationTypeName = view.findViewById(R.id.rowAccommodationTypeName);
        boxCheckInOut = view.findViewById(R.id.boxCheckInOut);

        cardMeetingDetails = view.findViewById(R.id.cardMeetingDetails);
        rowProjectMeeting = view.findViewById(R.id.rowProjectMeeting);

        cardTravelDetails = view.findViewById(R.id.cardTravelDetails);
        rowTravelCategoryMode = view.findViewById(R.id.rowTravelCategoryMode);
        rowRouteDistance = view.findViewById(R.id.rowRouteDistance);
        rowRestaurantPersons = view.findViewById(R.id.rowRestaurantPersons);

        btnDocumentView = view.findViewById(R.id.btnDocumentView);
        btnPrintClaim = view.findViewById(R.id.btnPrintClaim);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            claimData = (EmployeeClaimdataItem) getArguments().getSerializable("claimData");

            if (claimData != null) {
                bindClaimData();
            }
        }

        btnDocumentView.setOnClickListener(v -> {
            if (claimData != null && claimData.getDocFileURL() != null) {
                List<String> documentUrls = Collections.singletonList(claimData.getDocFileURL());
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
                View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_image_viewer, null);
                RecyclerView imageRecyclerView = dialogView.findViewById(R.id.imageRecyclerView);

                imageRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
                ImageAdapter imageAdapter = new ImageAdapter(requireContext(), documentUrls);
                imageRecyclerView.setAdapter(imageAdapter);

                Dialog dialog = builder.setView(dialogView).setTitle("Uploaded Documents").setPositiveButton("Close", (dialog1, which) -> dialog1.dismiss()).create();

                Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.WHITE));
                dialog.show();
            } else {
                Toast.makeText(requireContext(), "No document available.", Toast.LENGTH_SHORT).show();
            }
        });

        btnPrintClaim.setOnClickListener(v -> {
            if (claimData != null) {
                Toast.makeText(requireContext(), "This Feature coming soon.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "No claim data available to print.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ================================================================
    //  MAIN BIND METHOD
    // ================================================================
    private void bindClaimData() {

        // -------- Hero (always essential, but Travel Ref/RM/HR can hide) --------
        txtClaimId.setText(claimData.getClaimId() != null ? claimData.getClaimId() : String.valueOf(claimData.getId()));
        txtStatus.setText(isValid(claimData.getStatus()) ? claimData.getStatus() : "N/A");

        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        String appliedDateStr = null;
        try {
            Date date = inputFormat.parse(claimData.getClaimDate());
            appliedDateStr = outputFormat.format(date);
        } catch (Exception e) {
            appliedDateStr = claimData.getClaimDate();
        }
        boolean hasAppliedDate = bindText(txtAppliedDate, appliedDateStr);
        boolean hasTravelRef = bindText(txtTravelRefId, claimData.getTravelRefId());
        setVisibleIfAny(rowDateTravelRef, hasAppliedDate, hasTravelRef);

        boolean hasStatusRm = bindText(txtStatusRm, claimData.getStatusRm());
        boolean hasStatusHr = bindText(txtStatusHr, claimData.getStatusHr());
        setVisibleIfAny(rowStatusRmHr, hasStatusRm, hasStatusHr);

        // Total amount — always shown (core info)
        String currency = claimData.getCurrency() != null ? claimData.getCurrency() : "";
        txtTotalAmount.setText((currency + " " + claimData.getTotalamount()).trim());

        // -------- Expense & Billing Details --------
        boolean hasExpenseType = bindText(txtExpenseType, claimData.getExpenseType());
        boolean hasExpenseCategory = bindText(txtExpenseCategory, claimData.getExpancecategory());
        setVisibleIfAny(rowExpenseTypeCategory, hasExpenseType, hasExpenseCategory);

        boolean hasBillNumber = bindText(txtBillNumber, claimData.getBillnumber());
        boolean hasBillingPeriod = bindText(txtBillingPeriod, claimData.getBillingperiod());
        setVisibleIfAny(rowBillNumberPeriod, hasBillNumber, hasBillingPeriod);

        boolean hasState = bindText(txtClaimState, claimData.getState());

        setVisibleIfAny(cardExpenseBilling, hasExpenseType, hasExpenseCategory, hasBillNumber, hasBillingPeriod, hasState);

        // -------- Fuel & Toll Details --------
        boolean hasFuelType = bindText(txtFuelType, claimData.getFueltype());
        boolean hasFuelStation = bindText(txtFuelStation, claimData.getFuelstationname());
        setVisibleIfAny(rowFuelTypeStation, hasFuelType, hasFuelStation);

        boolean hasFuelQuantity = bindText(txtFuelQuantity, claimData.getFuelquantity());
        boolean hasVehicleNumber = bindText(txtVehicleNumber, claimData.getVehiclenumber());
        setVisibleIfAny(rowFuelQuantityVehicle, hasFuelQuantity, hasVehicleNumber);

        boolean hasTollPlaza = bindText(txtTollPlaza, claimData.getTollplazaname());
        boolean hasTollLocation = bindText(txtTollLocation, claimData.getTolllocation());
        setVisibleIfAny(rowTollPlazaLocation, hasTollPlaza, hasTollLocation);

        setVisibleIfAny(cardFuelToll, hasFuelType, hasFuelStation, hasFuelQuantity, hasVehicleNumber, hasTollPlaza, hasTollLocation);

        // -------- Parking & Accommodation --------
        boolean hasParkingLocation = bindText(txtParkingLocation, claimData.getParkinglocation());
        boolean hasParkingDate = bindText(txtParkingDate, claimData.getParkingdate());
        setVisibleIfAny(rowParkingLocationDate, hasParkingLocation, hasParkingDate);

        boolean hasAccommodationType = bindText(txtAccommodationType, claimData.getAccommodationtype());
        boolean hasAccommodationName = bindText(txtAccommodationName, claimData.getAccommodationname());
        setVisibleIfAny(rowAccommodationTypeName, hasAccommodationType, hasAccommodationName);

        boolean hasCheckIn = bindText(txtCheckIn, claimData.getCheckin());
        boolean hasCheckOut = bindText(txtCheckOut, claimData.getCheckout());
        setVisibleIfAny(boxCheckInOut, hasCheckIn, hasCheckOut);

        setVisibleIfAny(cardParkingAccommodation, hasParkingLocation, hasParkingDate,
                hasAccommodationType, hasAccommodationName, hasCheckIn, hasCheckOut);

        // -------- Meeting Details --------
        boolean hasProjectName = bindText(txtProjectName, claimData.getProject());
        boolean hasMeetingType = bindText(txtMeetingType, claimData.getMeeting());
        setVisibleIfAny(rowProjectMeeting, hasProjectName, hasMeetingType);

        boolean hasPurpose = bindText(txtPurposeOfMeeting, claimData.getPerposeofmeet());
        boolean hasComment = bindText(txtComment, claimData.getComment());

        setVisibleIfAny(cardMeetingDetails, hasProjectName, hasMeetingType, hasPurpose, hasComment);

        // -------- Travel Details --------
        boolean hasTravelCategory = bindText(txtTravelCategory, claimData.getTravelCategory());
        boolean hasModeOfTransport = bindText(txtModeOfTransport, claimData.getModeoftransport());
        setVisibleIfAny(rowTravelCategoryMode, hasTravelCategory, hasModeOfTransport);

        String from = claimData.getFromaddress();
        String to = claimData.getToaddress();
        boolean hasRoute = isValid(from) || isValid(to);
        String routeText = (isValid(from) ? from : "") + (isValid(from) && isValid(to) ? " to " : "") + (isValid(to) ? to : "");
        boolean routeBound = bindText(txtFromTo, hasRoute ? routeText : null);

        boolean hasDistance = claimData.getDistance() != null;
        if (hasDistance) {
            txtDistance.setText(claimData.getDistance() + " Km");
            ((View) txtDistance.getParent()).setVisibility(View.VISIBLE);
        } else {
            ((View) txtDistance.getParent()).setVisibility(View.GONE);
        }
        setVisibleIfAny(rowRouteDistance, routeBound, hasDistance);

        boolean hasRestaurant = bindText(txtRestaurant, claimData.getRestaurant());
        boolean hasPersons = claimData.getPersons() != null;
        if (hasPersons) {
            txtPersons.setText(String.valueOf(claimData.getPersons()));
            ((View) txtPersons.getParent()).setVisibility(View.VISIBLE);
        } else {
            ((View) txtPersons.getParent()).setVisibility(View.GONE);
        }
        setVisibleIfAny(rowRestaurantPersons, hasRestaurant, hasPersons);

        setVisibleIfAny(cardTravelDetails, hasTravelCategory, hasModeOfTransport,
                routeBound, hasDistance, hasRestaurant, hasPersons);
    }

    private boolean isValid(String value) {
        return value != null
                && !value.trim().isEmpty()
                && !value.trim().equalsIgnoreCase("null")
                && !value.trim().equalsIgnoreCase("N/A");
    }

    private boolean bindText(TextView tv, String value) {
        boolean has = isValid(value);
        View wrapper = (View) tv.getParent();
        if (has) {
            tv.setText(value);
            wrapper.setVisibility(View.VISIBLE);
        } else {
            wrapper.setVisibility(View.GONE);
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
package app.xedigital.ai.utills;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.material.card.MaterialCardView;

import app.xedigital.ai.R;

/**
 * Reusable dark-themed alert dialog matching the app's design system.
 * Supports 4 color-coded types: SUCCESS, ERROR, WARNING, INFO.
 * <p>
 * Usage:
 * new CustomAlertDialog(context)
 * .setType(CustomAlertDialog.Type.ERROR)
 * .setTitle("Attendance Failed")
 * .setMessage("Something went wrong.")
 * .setPositiveButton("Retry", () -> { ... })
 * .setNegativeButton("Cancel", () -> { ... })
 * .setCancelable(false)
 * .show();
 */
public class CustomAlertDialog {

    private final Context context;
    private Type type = Type.INFO;
    private String title = "";
    private String message = "";
    private String positiveText = "OK";
    private String negativeText = null; // null = hide negative button entirely
    private OnActionListener onPositive;
    private OnActionListener onNegative;
    private boolean cancelable = false;
    private Dialog dialog;
    public CustomAlertDialog(@NonNull Context context) {
        this.context = context;
    }

    public CustomAlertDialog setType(Type type) {
        this.type = type;
        return this;
    }

    public CustomAlertDialog setTitle(String title) {
        this.title = title;
        return this;
    }

    public CustomAlertDialog setMessage(String message) {
        this.message = message;
        return this;
    }

    public CustomAlertDialog setPositiveButton(String text, OnActionListener listener) {
        this.positiveText = text;
        this.onPositive = listener;
        return this;
    }

    public CustomAlertDialog setNegativeButton(String text, OnActionListener listener) {
        this.negativeText = text;
        this.onNegative = listener;
        return this;
    }

    public CustomAlertDialog setCancelable(boolean cancelable) {
        this.cancelable = cancelable;
        return this;
    }

    public Dialog show() {
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_admin_punch_alert, null);

        MaterialCardView iconCircle = view.findViewById(R.id.dialogIconCircle);
        TextView iconText = view.findViewById(R.id.dialogIconText);
        TextView titleView = view.findViewById(R.id.dialogTitle);
        TextView messageView = view.findViewById(R.id.dialogMessage);
        TextView positiveButton = view.findViewById(R.id.dialogPositiveButton);
        TextView negativeButton = view.findViewById(R.id.dialogNegativeButton);
        View buttonDivider = view.findViewById(R.id.dialogButtonDivider);

        int accentColor;
        String icon;
        switch (type) {
            case SUCCESS:
                accentColor = Color.parseColor("#4CAF50");
                icon = "✓";
                break;
            case ERROR:
                accentColor = Color.parseColor("#FF5252");
                icon = "✕";
                break;
            case WARNING:
                accentColor = Color.parseColor("#FFB300");
                icon = "!";
                break;
            case INFO:
            default:
                accentColor = Color.parseColor("#5B5BFF");
                icon = "i";
                break;
        }

        iconCircle.setCardBackgroundColor(applyAlpha(accentColor, 0x22));
        iconText.setText(icon);
        iconText.setTextColor(accentColor);

        titleView.setText(title);
        messageView.setText(message);

        positiveButton.setText(positiveText);
        positiveButton.setTextColor(accentColor);

        if (negativeText != null) {
            negativeButton.setText(negativeText);
            negativeButton.setVisibility(View.VISIBLE);
            buttonDivider.setVisibility(View.VISIBLE);
        } else {
            negativeButton.setVisibility(View.GONE);
            buttonDivider.setVisibility(View.GONE);
        }

        positiveButton.setOnClickListener(v -> {
            dialog.dismiss();
            if (onPositive != null) onPositive.onAction();
        });

        negativeButton.setOnClickListener(v -> {
            dialog.dismiss();
            if (onNegative != null) onNegative.onAction();
        });

        dialog.setContentView(view);
        dialog.setCancelable(cancelable);

//        if (dialog.getWindow() != null) {
//            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#CC000000")));
//        }
        if (dialog.getWindow() != null) {
            Window window = dialog.getWindow();
            window.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#CC000000")));

            // Set width programmatically to 85% of screen width
            android.util.DisplayMetrics metrics = context.getResources().getDisplayMetrics();
            int width = (int) (metrics.widthPixels * 0.85);
            window.setLayout(width, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        dialog.show();
        return dialog;
    }

    public Dialog getDialog() {
        return dialog;
    }

    private int applyAlpha(int color, int alpha) {
        return (color & 0x00FFFFFF) | (alpha << 24);
    }

    public enum Type {SUCCESS, ERROR, WARNING, INFO}

    public interface OnActionListener {
        void onAction();
    }
}
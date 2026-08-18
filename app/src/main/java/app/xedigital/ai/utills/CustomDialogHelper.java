package app.xedigital.ai.utills;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;

import app.xedigital.ai.R;

public class CustomDialogHelper {

    // ══════════════════════════════════════════════════════════════════════
    // CALLBACK INTERFACES
    // ══════════════════════════════════════════════════════════════════════

    public static void showSuccessDialog(Context context, String title, String message, OnDialogDismissListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_success, null);
        builder.setView(view);
        builder.setCancelable(false);

        AlertDialog dialog = builder.create();
        setupDialogWindow(dialog);

        TextView titleView = view.findViewById(R.id.dialogTitle);
        TextView messageView = view.findViewById(R.id.dialogMessage);
        MaterialButton btnOk = view.findViewById(R.id.btnDialogOk);

        titleView.setText(title != null ? title : "Success!");
        messageView.setText(message);

        btnOk.setOnClickListener(v -> {
            dialog.dismiss();
            if (listener != null) listener.onDismiss();
        });

        dialog.show();
        animateDialog(context, view);
    }

    public static void showSuccessDialog(Context context, String title, String message) {
        showSuccessDialog(context, title, message, null);
    }

    // ══════════════════════════════════════════════════════════════════════
    // SUCCESS DIALOG — Plain text
    // ══════════════════════════════════════════════════════════════════════

    public static void showSuccessDialogHtml(Context context, String title, String htmlMessage, OnDialogDismissListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_success, null);
        builder.setView(view);
        builder.setCancelable(false);

        AlertDialog dialog = builder.create();
        setupDialogWindow(dialog);

        TextView titleView = view.findViewById(R.id.dialogTitle);
        TextView messageView = view.findViewById(R.id.dialogMessage);
        MaterialButton btnOk = view.findViewById(R.id.btnDialogOk);

        titleView.setText(title != null ? title : "Success!");
        applyHtml(messageView, htmlMessage);

        btnOk.setOnClickListener(v -> {
            dialog.dismiss();
            if (listener != null) listener.onDismiss();
        });

        dialog.show();
        animateDialog(context, view);
    }

    public static void showErrorDialog(Context context, String title, String message, OnDialogDismissListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_error, null);
        builder.setView(view);
        builder.setCancelable(false);

        AlertDialog dialog = builder.create();
        setupDialogWindow(dialog);

        TextView titleView = view.findViewById(R.id.dialogTitle);
        TextView messageView = view.findViewById(R.id.dialogMessage);
        MaterialButton btnOk = view.findViewById(R.id.btnDialogOk);

        titleView.setText(title != null ? title : "Oops!");
        messageView.setText(message);

        btnOk.setOnClickListener(v -> {
            dialog.dismiss();
            if (listener != null) listener.onDismiss();
        });

        dialog.show();
        animateDialog(context, view);
    }

    // ══════════════════════════════════════════════════════════════════════
    // SUCCESS DIALOG — HTML formatted
    // ══════════════════════════════════════════════════════════════════════

    public static void showErrorDialog(Context context, String title, String message) {
        showErrorDialog(context, title, message, null);
    }

    // ══════════════════════════════════════════════════════════════════════
    // ERROR DIALOG — Plain text
    // ══════════════════════════════════════════════════════════════════════

    public static void showErrorDialog(Context context, String message) {
        showErrorDialog(context, null, message, null);
    }

    public static void showErrorDialogHtml(Context context, String title, String htmlMessage, OnDialogDismissListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_error, null);
        builder.setView(view);
        builder.setCancelable(false);

        AlertDialog dialog = builder.create();
        setupDialogWindow(dialog);

        TextView titleView = view.findViewById(R.id.dialogTitle);
        TextView messageView = view.findViewById(R.id.dialogMessage);
        MaterialButton btnOk = view.findViewById(R.id.btnDialogOk);

        titleView.setText(title != null ? title : "Oops!");
        applyHtml(messageView, htmlMessage);

        btnOk.setOnClickListener(v -> {
            dialog.dismiss();
            if (listener != null) listener.onDismiss();
        });

        dialog.show();
        animateDialog(context, view);
    }

    public static void showErrorDialogHtml(Context context, String title, String htmlMessage) {
        showErrorDialogHtml(context, title, htmlMessage, null);
    }

    // ══════════════════════════════════════════════════════════════════════
    // ERROR DIALOG — HTML formatted
    // ══════════════════════════════════════════════════════════════════════

    public static void showWarningDialog(Context context, String title, String message, String confirmText, String cancelText, OnWarningActionListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_warning, null);
        builder.setView(view);
        builder.setCancelable(false);

        AlertDialog dialog = builder.create();
        setupDialogWindow(dialog);

        TextView titleView = view.findViewById(R.id.dialogTitle);
        TextView messageView = view.findViewById(R.id.dialogMessage);
        MaterialButton btnConfirm = view.findViewById(R.id.btnDialogConfirm);
        MaterialButton btnCancel = view.findViewById(R.id.btnDialogCancel);

        titleView.setText(title != null ? title : "Warning");
        messageView.setText(message);
        if (confirmText != null) btnConfirm.setText(confirmText);
        if (cancelText != null) btnCancel.setText(cancelText);

        btnConfirm.setOnClickListener(v -> {
            dialog.dismiss();
            if (listener != null) listener.onConfirm();
        });

        btnCancel.setOnClickListener(v -> {
            dialog.dismiss();
            if (listener != null) listener.onCancel();
        });

        dialog.show();
        animateDialog(context, view);
    }

    public static void showWarningDialogHtml(Context context, String title, String htmlMessage, String confirmText, String cancelText, OnWarningActionListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_warning, null);
        builder.setView(view);
        builder.setCancelable(false);

        AlertDialog dialog = builder.create();
        setupDialogWindow(dialog);

        TextView titleView = view.findViewById(R.id.dialogTitle);
        TextView messageView = view.findViewById(R.id.dialogMessage);
        MaterialButton btnConfirm = view.findViewById(R.id.btnDialogConfirm);
        MaterialButton btnCancel = view.findViewById(R.id.btnDialogCancel);

        titleView.setText(title != null ? title : "Warning");
        applyHtml(messageView, htmlMessage);
        if (confirmText != null) btnConfirm.setText(confirmText);
        if (cancelText != null) btnCancel.setText(cancelText);

        btnConfirm.setOnClickListener(v -> {
            dialog.dismiss();
            if (listener != null) listener.onConfirm();
        });

        btnCancel.setOnClickListener(v -> {
            dialog.dismiss();
            if (listener != null) listener.onCancel();
        });

        dialog.show();
        animateDialog(context, view);
    }

    // ══════════════════════════════════════════════════════════════════════
    // WARNING DIALOG — Plain text
    // ══════════════════════════════════════════════════════════════════════

    public static void showInfoDialog(Context context, String title, String message, OnDialogDismissListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_info, null);
        builder.setView(view);
        builder.setCancelable(false);

        AlertDialog dialog = builder.create();
        setupDialogWindow(dialog);

        TextView titleView = view.findViewById(R.id.dialogTitle);
        TextView messageView = view.findViewById(R.id.dialogMessage);
        MaterialButton btnOk = view.findViewById(R.id.btnDialogOk);

        titleView.setText(title != null ? title : "Information");
        messageView.setText(message);

        btnOk.setOnClickListener(v -> {
            dialog.dismiss();
            if (listener != null) listener.onDismiss();
        });

        dialog.show();
        animateDialog(context, view);
    }

    // ══════════════════════════════════════════════════════════════════════
    // WARNING DIALOG — HTML formatted
    // ══════════════════════════════════════════════════════════════════════

    public static void showInfoDialog(Context context, String title, String message) {
        showInfoDialog(context, title, message, null);
    }

    // ══════════════════════════════════════════════════════════════════════
    // INFO DIALOG — Plain text
    // ══════════════════════════════════════════════════════════════════════

    public static void showInfoDialogHtml(Context context, String title, String htmlMessage, OnDialogDismissListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_info, null);
        builder.setView(view);
        builder.setCancelable(false);

        AlertDialog dialog = builder.create();
        setupDialogWindow(dialog);

        TextView titleView = view.findViewById(R.id.dialogTitle);
        TextView messageView = view.findViewById(R.id.dialogMessage);
        MaterialButton btnOk = view.findViewById(R.id.btnDialogOk);

        titleView.setText(title != null ? title : "Information");
        applyHtml(messageView, htmlMessage);

        btnOk.setOnClickListener(v -> {
            dialog.dismiss();
            if (listener != null) listener.onDismiss();
        });

        dialog.show();
        animateDialog(context, view);
    }

    public static void showInfoDialogHtml(Context context, String title, String htmlMessage) {
        showInfoDialogHtml(context, title, htmlMessage, null);
    }

    // ══════════════════════════════════════════════════════════════════════
    // INFO DIALOG — HTML formatted
    // ══════════════════════════════════════════════════════════════════════

    public static AlertDialog showLoadingDialog(Context context) {
        return showLoadingDialog(context, "Please wait...");
    }

    public static AlertDialog showLoadingDialog(Context context, String text) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_loading, null);
        builder.setView(view);
        builder.setCancelable(false);

        AlertDialog dialog = builder.create();

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setGravity(Gravity.CENTER);
            WindowManager.LayoutParams params = window.getAttributes();
            params.width = WindowManager.LayoutParams.MATCH_PARENT;
            params.height = WindowManager.LayoutParams.MATCH_PARENT;
            window.setAttributes(params);
        }

        TextView loadingText = view.findViewById(R.id.loadingText);
        if (text != null) loadingText.setText(text);

        dialog.show();
        return dialog;
    }

    // ══════════════════════════════════════════════════════════════════════
    // LOADING DIALOG
    // ══════════════════════════════════════════════════════════════════════

    private static void setupDialogWindow(AlertDialog dialog) {
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    private static void animateDialog(Context context, View view) {
        Animation anim = AnimationUtils.loadAnimation(context, R.anim.dialog_scale_in);
        view.startAnimation(anim);
    }

    // ══════════════════════════════════════════════════════════════════════
    // PRIVATE HELPERS
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Applies HTML formatting to a TextView.
     * Sets left-alignment for proper bullet point display.
     * Enables link clicking if any links are present.
     */
    private static void applyHtml(TextView textView, String htmlContent) {
        Spanned formatted = Html.fromHtml(htmlContent, Html.FROM_HTML_MODE_COMPACT);
        textView.setText(formatted);
        textView.setGravity(Gravity.START);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    public interface OnDialogDismissListener {
        void onDismiss();
    }

    public interface OnWarningActionListener {
        void onConfirm();

        void onCancel();
    }
}
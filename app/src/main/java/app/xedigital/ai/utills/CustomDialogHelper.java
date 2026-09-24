package app.xedigital.ai.utills;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.Log;
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

    private static final String TAG = "CustomDialogHelper";

    // ══════════════════════════════════════════════════════════════════════
    // SUCCESS DIALOGS
    // ══════════════════════════════════════════════════════════════════════

    public static void showSuccessDialog(Context context, String title, String message, String buttonText, OnDialogDismissListener listener) {
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
        if (buttonText != null && !buttonText.isEmpty()) {
            btnOk.setText(buttonText);
        }

        btnOk.setOnClickListener(v -> dismissWithAnimation(context, dialog, () -> {
            if (listener != null) listener.onDismiss();
        }));

        dialog.show();
        animateDialogIn(context, view);
    }

    public static void showSuccessDialog(Context context, String title, String message, OnDialogDismissListener listener) {
        showSuccessDialog(context, title, message, "Okay", listener);
    }

    public static void showSuccessDialog(Context context, String title, String message) {
        showSuccessDialog(context, title, message, "Okay", null);
    }

    public static void showSuccessDialogHtml(Context context, String title, String htmlMessage, String buttonText, OnDialogDismissListener listener) {
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
        if (buttonText != null && !buttonText.isEmpty()) {
            btnOk.setText(buttonText);
        }

        btnOk.setOnClickListener(v -> dismissWithAnimation(context, dialog, () -> {
            if (listener != null) listener.onDismiss();
        }));

        dialog.show();
        animateDialogIn(context, view);
    }

    public static void showSuccessDialogHtml(Context context, String title, String htmlMessage, OnDialogDismissListener listener) {
        showSuccessDialogHtml(context, title, htmlMessage, "Okay", listener);
    }

    public static void showSuccessDialogHtml(Context context, String title, String htmlMessage) {
        showSuccessDialogHtml(context, title, htmlMessage, "Okay", null);
    }

    // ══════════════════════════════════════════════════════════════════════
    // ERROR DIALOGS
    // ══════════════════════════════════════════════════════════════════════

    public static void showErrorDialog(Context context, String title, String message, String buttonText, OnDialogDismissListener listener) {
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
        btnOk.setText((buttonText != null && !buttonText.isEmpty()) ? buttonText : "Close");

        btnOk.setOnClickListener(v -> dismissWithAnimation(context, dialog, () -> {
            if (listener != null) listener.onDismiss();
        }));

        dialog.show();
        animateDialogIn(context, view);
    }

    public static void showErrorDialog(Context context, String title, String message, OnDialogDismissListener listener) {
        showErrorDialog(context, title, message, "Close", listener);
    }

    public static void showErrorDialog(Context context, String title, String message) {
        showErrorDialog(context, title, message, "Close", null);
    }

    public static void showErrorDialog(Context context, String message) {
        showErrorDialog(context, null, message, "Close", null);
    }

    public static void showErrorDialogHtml(Context context, String title, String htmlMessage, String buttonText, OnDialogDismissListener listener) {
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
        btnOk.setText((buttonText != null && !buttonText.isEmpty()) ? buttonText : "Close");

        btnOk.setOnClickListener(v -> dismissWithAnimation(context, dialog, () -> {
            if (listener != null) listener.onDismiss();
        }));

        dialog.show();
        animateDialogIn(context, view);
    }

    public static void showErrorDialogHtml(Context context, String title, String htmlMessage, OnDialogDismissListener listener) {
        showErrorDialogHtml(context, title, htmlMessage, "Close", listener);
    }

    public static void showErrorDialogHtml(Context context, String title, String htmlMessage, String buttonText) {
        showErrorDialogHtml(context, title, htmlMessage, buttonText, null);
    }

    public static void showErrorDialogHtml(Context context, String title, String htmlMessage) {
        showErrorDialogHtml(context, title, htmlMessage, "Close", null);
    }

    // ══════════════════════════════════════════════════════════════════════
    // INFO DIALOGS
    // ══════════════════════════════════════════════════════════════════════

    public static void showInfoDialog(Context context, String title, String message, String buttonText, OnDialogDismissListener listener) {
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
        btnOk.setText((buttonText != null && !buttonText.isEmpty()) ? buttonText : "Okay");

        btnOk.setOnClickListener(v -> dismissWithAnimation(context, dialog, () -> {
            if (listener != null) listener.onDismiss();
        }));

        dialog.show();
        animateDialogIn(context, view);
    }

    public static void showInfoDialog(Context context, String title, String message, OnDialogDismissListener listener) {
        showInfoDialog(context, title, message, "Okay", listener);
    }

    public static void showInfoDialog(Context context, String title, String message) {
        showInfoDialog(context, title, message, "Okay", null);
    }

    public static void showInfoDialogHtml(Context context, String title, String htmlMessage, String buttonText, OnDialogDismissListener listener) {
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
        btnOk.setText((buttonText != null && !buttonText.isEmpty()) ? buttonText : "Okay");

        btnOk.setOnClickListener(v -> dismissWithAnimation(context, dialog, () -> {
            if (listener != null) listener.onDismiss();
        }));

        dialog.show();
        animateDialogIn(context, view);
    }

    public static void showInfoDialogHtml(Context context, String title, String htmlMessage, OnDialogDismissListener listener) {
        showInfoDialogHtml(context, title, htmlMessage, "Okay", listener);
    }

    public static void showInfoDialogHtml(Context context, String title, String htmlMessage) {
        showInfoDialogHtml(context, title, htmlMessage, "Okay", null);
    }

    // ══════════════════════════════════════════════════════════════════════
    // WARNING DIALOGS
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
        btnConfirm.setText(confirmText != null ? confirmText : "Okay");
        btnCancel.setText(cancelText != null ? cancelText : "Close");

        btnConfirm.setOnClickListener(v -> dismissWithAnimation(context, dialog, () -> {
            if (listener != null) listener.onConfirm();
        }));

        btnCancel.setOnClickListener(v -> dismissWithAnimation(context, dialog, () -> {
            if (listener != null) listener.onCancel();
        }));

        dialog.show();
        animateDialogIn(context, view);
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
        btnConfirm.setText(confirmText != null ? confirmText : "Okay");
        btnCancel.setText(cancelText != null ? cancelText : "Close");

        btnConfirm.setOnClickListener(v -> dismissWithAnimation(context, dialog, () -> {
            if (listener != null) listener.onConfirm();
        }));

        btnCancel.setOnClickListener(v -> dismissWithAnimation(context, dialog, () -> {
            if (listener != null) listener.onCancel();
        }));

        dialog.show();
        animateDialogIn(context, view);
    }

    // ══════════════════════════════════════════════════════════════════════
    // LOADING DIALOG
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
            applyDim(window);
        }

        TextView loadingText = view.findViewById(R.id.loadingText);
        if (text != null) loadingText.setText(text);

        dialog.show();
        animateDialogIn(context, view);
        return dialog;
    }

    /**
     * Call this instead of dialog.dismiss() to animate the loading dialog out smoothly.
     * Example: CustomDialogHelper.dismissWithAnimation(context, loadingDialog);
     */
    public static void dismissWithAnimation(Context context, AlertDialog dialog) {
        dismissWithAnimation(context, dialog, null);
    }

    // ══════════════════════════════════════════════════════════════════════
    // PRIVATE HELPERS
    // ══════════════════════════════════════════════════════════════════════

    private static void setupDialogWindow(AlertDialog dialog) {
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            applyDim(window);
        }
    }

    /**
     * Applies a rich dim overlay behind the dialog for a modern, focused look.
     * <p>
     * NOTE: We intentionally do NOT use FLAG_BLUR_BEHIND. That flag can
     * disrupt window rendering / touch dispatch on certain devices and
     * Android versions (this was confirmed to cause unresponsive dialog
     * buttons in this app). A tuned dim overlay is fully reliable across
     * all devices and API levels while still looking modern.
     */
    private static void applyDim(Window window) {
        if (window == null) return;
        try {
            window.setDimAmount(0.65f);
        } catch (Exception e) {
            Log.e(TAG, "applyDim failed", e);
        }
    }

    private static void animateDialogIn(Context context, View view) {
        try {
            Animation anim = AnimationUtils.loadAnimation(context, R.anim.dialog_scale_in);
            view.startAnimation(anim);
        } catch (Exception e) {
            Log.e(TAG, "animateDialogIn failed", e);
        }
    }

    /**
     * Animates the dialog's content out (scale + fade), then dismisses it and
     * triggers the given callback. Includes a 500ms safety-net timeout so the
     * dialog is GUARANTEED to close and the callback GUARANTEED to fire, even
     * if the animation callback fails to trigger for any reason.
     */
    private static void dismissWithAnimation(Context context, AlertDialog dialog, Runnable onEnd) {
        if (dialog == null || !dialog.isShowing()) {
            runSafely(onEnd);
            return;
        }

        View decorView = null;
        try {
            Window window = dialog.getWindow();
            decorView = window != null ? window.getDecorView() : null;
        } catch (Exception e) {
            Log.e(TAG, "Error getting decorView", e);
        }

        if (decorView == null) {
            safeDismiss(dialog, onEnd);
            return;
        }

        final View finalDecorView = decorView;
        final boolean[] alreadyHandled = {false};

        Runnable finalizeDismiss = () -> {
            if (alreadyHandled[0]) return;
            alreadyHandled[0] = true;
            safeDismiss(dialog, onEnd);
        };

        try {
            Animation anim = AnimationUtils.loadAnimation(context, R.anim.dialog_scale_out);

            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    finalDecorView.post(finalizeDismiss);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });

            finalDecorView.startAnimation(anim);

            // Safety net: force-finish if onAnimationEnd never fires
            finalDecorView.postDelayed(finalizeDismiss, 500);

        } catch (Exception e) {
            Log.e(TAG, "Exception during exit animation setup", e);
            finalizeDismiss.run();
        }
    }

    private static void safeDismiss(AlertDialog dialog, Runnable onEnd) {
        try {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception during dialog.dismiss()", e);
        } finally {
            runSafely(onEnd);
        }
    }

    /**
     * Runs the caller's callback (onDismiss/onConfirm/onCancel) safely.
     * Any exception thrown inside the callback is caught and logged instead
     * of silently breaking the dismiss/animation chain.
     */
    private static void runSafely(Runnable onEnd) {
        if (onEnd == null) return;
        try {
            onEnd.run();
        } catch (Exception e) {
            Log.e(TAG, "Exception inside dialog callback", e);
        }
    }

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
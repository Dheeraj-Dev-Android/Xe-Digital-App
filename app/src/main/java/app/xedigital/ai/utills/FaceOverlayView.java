package app.xedigital.ai.utills;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class FaceOverlayView extends View {
    private final float bracketLength = 60f; // Length of the bracket arms
    private final float cornerRadius = 30f; // Roundness of the cutout
    private Paint backgroundPaint;
    private Paint eraserPaint;
    private Paint bracketPaint;
    private RectF scanRect;
    private Path bracketPath;
    private int currentColor = Color.RED;

    public FaceOverlayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // Dimmed background
        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.parseColor("#AA000000")); // 66% opacity black
        backgroundPaint.setStyle(Paint.Style.FILL);

        // Eraser for the center hole
        eraserPaint = new Paint();
        eraserPaint.setAntiAlias(true);
        eraserPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        // Paint for the brackets
        bracketPaint = new Paint();
        bracketPaint.setAntiAlias(true);
        bracketPaint.setStyle(Paint.Style.STROKE);
        bracketPaint.setStrokeWidth(12f); // Slightly thicker for emphasis
        bracketPaint.setStrokeCap(Paint.Cap.ROUND); // Makes the ends of the lines look smooth

        scanRect = new RectF();
        bracketPath = new Path();

        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Define the rectangle (80% width, 65% height)
        float width = getWidth() * 0.80f;
        float height = getHeight() * 0.65f;
        float left = (getWidth() - width) / 2;
        float top = (getHeight() - height) / 2;
        float right = left + width;
        float bottom = top + height;

        scanRect.set(left, top, right, bottom);

        // 1. Draw the dimmed background
        canvas.drawRect(0, 0, getWidth(), getHeight(), backgroundPaint);

        // 2. Cut out the scanning area
        canvas.drawRoundRect(scanRect, cornerRadius, cornerRadius, eraserPaint);

        // 3. Draw the 4 Corner Brackets
        bracketPaint.setColor(currentColor);
        drawBrackets(canvas, left, top, right, bottom);
    }

    private void drawBrackets(Canvas canvas, float l, float t, float r, float b) {
        bracketPath.reset();

        // Top-Left Corner
        bracketPath.moveTo(l, t + bracketLength);
        bracketPath.lineTo(l, t);
        bracketPath.lineTo(l + bracketLength, t);

        // Top-Right Corner
        bracketPath.moveTo(r - bracketLength, t);
        bracketPath.lineTo(r, t);
        bracketPath.lineTo(r, t + bracketLength);

        // Bottom-Right Corner
        bracketPath.moveTo(r, b - bracketLength);
        bracketPath.lineTo(r, b);
        bracketPath.lineTo(r - bracketLength, b);

        // Bottom-Left Corner
        bracketPath.moveTo(l + bracketLength, b);
        bracketPath.lineTo(l, b);
        bracketPath.lineTo(l, b - bracketLength);

        canvas.drawPath(bracketPath, bracketPaint);
    }

    public void setFaceDetected(boolean detected) {
        int targetColor = detected ? Color.GREEN : Color.RED;
        if (currentColor != targetColor) {
            currentColor = targetColor;
            postInvalidate();
        }
    }
}
package de.nitri.gauge;

/**
 * Created by helfrich on 07/01/2017.
 */

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class Gauge extends View {

    private Paint needlePaint;
    private Path needlePath;
    private Paint needleScrewPaint;

    private Matrix matrix;
    private int framePerSeconds = 100;
    private long animationDuration = 10000;
    private long startTime;
    private float canvasCenterX;
    private float canvasCenterY;
    private float canvasWidth;
    private float canvasHeight;
    private float needleTailLength;
    private float needleWidth;
    private float needleLength;
    private RectF rimRect;
    private Paint rimPaint;
    private Paint rimCirclePaint;
    private RectF faceRect;
    private Paint facePaint;
    private Paint rimShadowPaint;
    private Paint scalePaint;
    private RectF scaleRect;

    private static final int totalNicks = 120; // on a full circle
    private static final float degreesPerNick = 360.0f / totalNicks;
    private static final float valuePerNick = 10;
    private static final float minValue = 0;
    private static final float maxValue = 1000;
    private static final boolean intScale = true;

    private static final float initialValue = 0;
    private float value = 0;
    private float needleValue = 0;

    float needleStep = 3f * valuePerDegree();

    private static float centerValue;
    private float labelRadius;

    private final int majorNickInterval = 10;

    private static final String TAG = Gauge.class.getSimpleName();
    private Paint labelPaint;
    private long lastMoveTime;

    public Gauge(Context context) {
        super(context);
        matrix = new Matrix();
        this.startTime = System.currentTimeMillis();
        this.postInvalidate();
        init();
    }

    public Gauge(Context context, AttributeSet attrs) {
        super(context, attrs);
        matrix = new Matrix();
        this.startTime = System.currentTimeMillis();
        this.postInvalidate();
        init();
    }

    public Gauge(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        matrix = new Matrix();
        this.startTime = System.currentTimeMillis();
        this.postInvalidate();
        init();
    }

    private void init() {

        centerValue = (minValue + maxValue) / 2;

        // the linear gradient is a bit skewed for realism
        rimPaint = new Paint();
        rimPaint.setFlags(Paint.ANTI_ALIAS_FLAG);


        rimCirclePaint = new Paint();
        rimCirclePaint.setAntiAlias(true);
        rimCirclePaint.setStyle(Paint.Style.STROKE);
        rimCirclePaint.setColor(Color.argb(0x4f, 0x33, 0x36, 0x33));
        rimCirclePaint.setStrokeWidth(0.005f);

        facePaint = new Paint();
        facePaint.setAntiAlias(true);
        facePaint.setStyle(Paint.Style.FILL);
        facePaint.setColor(Color.argb(0xff, 0xff, 0xff, 0xff));

        rimShadowPaint = new Paint();
        rimShadowPaint.setStyle(Paint.Style.FILL);

        scalePaint = new Paint();
        scalePaint.setStyle(Paint.Style.STROKE);

        scalePaint.setAntiAlias(true);
        //scalePaint.setLinearText(true);
        scalePaint.setColor(0x9f004d0f);


        labelPaint = new Paint();
        labelPaint.setTextSize(42f);
        labelPaint.setColor(0x9f004d0f);
        labelPaint.setTypeface(Typeface.SANS_SERIF);
        labelPaint.setTextAlign(Paint.Align.CENTER);

        needlePaint = new Paint();
        needlePaint.setColor(Color.RED); // Set the color
        needlePaint.setStyle(Paint.Style.FILL_AND_STROKE); // set the border and fills the inside of needle
        needlePaint.setAntiAlias(true);
        needlePaint.setStrokeWidth(5.0f); // width of the border
        needlePaint.setShadowLayer(8.0f, 0.1f, 0.1f, Color.GRAY); // Shadow of the needle

        needlePath = new Path();

        needleScrewPaint = new Paint();
        needleScrewPaint.setColor(Color.BLACK);
        needleScrewPaint.setAntiAlias(true);
        // needleScrewPaint.setShader(new RadialGradient(130.0f, 50.0f, 10.0f,
        //        Color.DKGRAY, Color.BLACK, Shader.TileMode.CLAMP));

        needleValue = value = initialValue;

        //setValue(250);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        long elapsedTime = System.currentTimeMillis() - startTime;

        // canvas.drawColor(Color.LTGRAY);

        drawRim(canvas);

        drawFace(canvas);

        drawScale(canvas);
        drawLabels(canvas);

        //matrix.postRotate(1.0f, canvasCenterX, canvasCenterY); // rotate 10 degree every second
        //canvas.concat(matrix);
        canvas.rotate(scaleToCanvasDegrees(valueToDegrees(needleValue)), canvasCenterX, canvasCenterY);

        canvas.drawPath(needlePath, needlePaint);

        canvas.drawCircle(canvasCenterX, canvasCenterY, canvasWidth / 61f, needleScrewPaint);

        //if (elapsedTime < animationDuration) {
        //    this.postInvalidateDelayed(10000 / framePerSeconds);
        //}

        //this.postInvalidateOnAnimation();
        invalidate();

        if (needsToMove()) {
            moveNeedle();
        }
    }

    private void moveNeedle() {
        long currentTime = System.currentTimeMillis();
        long deltaTime = currentTime - lastMoveTime;

        if (deltaTime >= 5) {
            if (Math.abs(value - needleValue) <= needleStep) {
                needleValue = value;
            } else {
                if (value > needleValue) {
                    needleValue += 2 * valuePerDegree();
                } else {
                    needleValue -= 2 * valuePerDegree();
                }
            }
            lastMoveTime = System.currentTimeMillis();
        }

    }

    private void drawRim(Canvas canvas) {
        // first, draw the metallic body
        canvas.drawOval(rimRect, rimPaint);
        // now the outer rim circle
        canvas.drawOval(rimRect, rimCirclePaint);
    }

    private void drawFace(Canvas canvas) {
        canvas.drawOval(faceRect, facePaint);
        // draw the inner rim circle
        canvas.drawOval(faceRect, rimCirclePaint);
        // draw the rim shadow inside the face
        canvas.drawOval(faceRect, rimShadowPaint);
    }

    private void drawScale(Canvas canvas) {
        //canvas.drawOval(scaleRect, scalePaint);

        canvas.save(Canvas.MATRIX_SAVE_FLAG);
        for (int i = 0; i < totalNicks; ++i) {
            float y1 = scaleRect.top;
            float y2 = y1 + (0.020f * canvasHeight);
            float y3 = y1 + (0.060f * canvasHeight);
            float y4 = y1 + (0.030f * canvasHeight);

            float value = nickToValue(i);

            if (value >= minValue && value <= maxValue) {
                canvas.drawLine(0.5f * canvasWidth, y1, 0.5f * canvasWidth, y2, scalePaint);

                if (i % majorNickInterval == 0) {
                    canvas.drawLine(0.5f * canvasWidth, y1, 0.5f * canvasWidth, y3, scalePaint);
                }

                if (i % (majorNickInterval / 2) == 0) {
                    canvas.drawLine(0.5f * canvasWidth, y1, 0.5f * canvasWidth, y4, scalePaint);
                }
            }

            canvas.rotate(degreesPerNick, 0.5f * canvasWidth, 0.5f * canvasHeight);
        }
        canvas.restore();
    }

    private void drawLabels(Canvas canvas) {
        for (int i = 0; i < totalNicks; i += majorNickInterval) {
            float value = nickToValue(i);
            if (value >= minValue && value <= maxValue) {
                float scaleAngle = i * degreesPerNick;
                float scaleAngleRads = (float) Math.toRadians(scaleAngle);
                //Log.d(TAG, "i = " + i + ", angle = " + scaleAngle + ", value = " + value);
                float deltaX = labelRadius * (float) Math.sin(scaleAngleRads);
                float deltaY = labelRadius * (float) Math.cos(scaleAngleRads);
                String valueLabel;
                if (intScale) {
                    valueLabel = String.valueOf((int) value);
                } else {
                    valueLabel = String.valueOf(value);
                }
                drawTextCentered(valueLabel, canvasCenterX + deltaX, canvasCenterY - deltaY, canvas);
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        canvasWidth = (float) w;
        canvasHeight = (float) h;
        canvasCenterX = w / 2f;
        canvasCenterY = h / 2f;
        needleTailLength = canvasWidth / 12f;
        needleWidth = canvasWidth / 98f;
        needleLength = (canvasWidth / 2f) * 0.8f;
        setNeedle();

        rimRect = new RectF(canvasWidth * .05f, canvasHeight * .05f, canvasWidth * 0.95f, canvasHeight * 0.95f);
        rimPaint.setShader(new LinearGradient(canvasWidth * 0.40f, canvasHeight * 0.0f, canvasWidth * 0.60f, canvasHeight * 1.0f,
                Color.rgb(0xf0, 0xf5, 0xf0),
                Color.rgb(0x30, 0x31, 0x30),
                Shader.TileMode.CLAMP));

        float rimSize = 0.02f * canvasWidth;
        faceRect = new RectF();
        faceRect.set(rimRect.left + rimSize, rimRect.top + rimSize,
                rimRect.right - rimSize, rimRect.bottom - rimSize);

        rimShadowPaint.setShader(new RadialGradient(0.5f * canvasWidth, 0.5f * canvasHeight, faceRect.width() / 2.0f,
                new int[]{0x00000000, 0x00000500, 0x50000500},
                new float[]{0.96f, 0.96f, 0.99f},
                Shader.TileMode.MIRROR));


        scalePaint.setStrokeWidth(0.005f * canvasWidth);
        scalePaint.setTextSize(0.045f * canvasWidth);
        scalePaint.setTextScaleX(0.8f * canvasWidth);

        float scalePosition = 0.015f * canvasWidth;
        scaleRect = new RectF();
        scaleRect.set(faceRect.left + scalePosition, faceRect.top + scalePosition,
                faceRect.right - scalePosition, faceRect.bottom - scalePosition);

        labelRadius = (canvasCenterX - scaleRect.left) * 0.72f;

        super.onSizeChanged(w, h, oldw, oldh);
    }

    private void setNeedle() {
        needlePath.reset();
        needlePath.moveTo(canvasCenterX - needleTailLength, canvasCenterY);
        needlePath.lineTo(canvasCenterX, canvasCenterY - (needleWidth / 2));
        needlePath.lineTo(canvasCenterX + needleLength, canvasCenterY);
        needlePath.lineTo(canvasCenterX, canvasCenterY + (needleWidth / 2));
        needlePath.lineTo(canvasCenterX - needleTailLength, canvasCenterY);
        needlePath.addCircle(canvasCenterX, canvasCenterY, 20.0f, Path.Direction.CW);
        needlePath.close();

        needleScrewPaint.setShader(new RadialGradient(canvasCenterX, canvasCenterY, needleWidth / 2,
                Color.DKGRAY, Color.BLACK, Shader.TileMode.CLAMP));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int size;
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        int widthWithoutPadding = width - getPaddingLeft() - getPaddingRight();
        int heigthWithoutPadding = height - getPaddingTop() - getPaddingBottom();

        if (widthWithoutPadding > heigthWithoutPadding) {
            size = heigthWithoutPadding;
        } else {
            size = widthWithoutPadding;
        }

        setMeasuredDimension(size + getPaddingLeft() + getPaddingRight(), size + getPaddingTop() + getPaddingBottom());
    }

    private float nickToValue(int nick) {
        float rawValue = ((nick < totalNicks / 2) ? nick : (nick - totalNicks)) * valuePerNick;
        float shiftedValue = rawValue + centerValue;
        return shiftedValue;
    }

    private float valueToDegrees(float value) {
        // these are scale degrees, 0 is on top
        return ((value - centerValue) / valuePerNick) * degreesPerNick;
    }

    private float valuePerDegree() {
        return valuePerNick / degreesPerNick;
    }

    private float scaleToCanvasDegrees(float degrees) {
        return degrees - 90;
    }

    public void setValue(float value) {
        needleValue = this.value = value;
    }

    public void moveToValue(float value) {
        this.value = value;
    }

    private boolean needsToMove() {
        return Math.abs(needleValue - value) > 0;
    }

    private void drawTextCentered(String text, float x, float y, Canvas canvas) {
        //float xPos = x - (labelPaint.measureText(text)/2f);
        float yPos = (y - ((labelPaint.descent() + labelPaint.ascent()) / 2f));
        canvas.drawText(text, x, yPos, labelPaint);
    }
}
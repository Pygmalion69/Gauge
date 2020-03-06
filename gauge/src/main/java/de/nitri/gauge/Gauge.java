package de.nitri.gauge;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Html;
import android.text.Spanned;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * A Gauge View on Android
 *
 * @author Pygmalion69 (Serge Helfrich)
 * @version 1.x
 * @see @see <a href="https://github.com/Pygmalion69/Gauge">https://github.com/Pygmalion69/Gauge/</a>
 * @since 2017-01-07
 */
public class Gauge extends View {
    private IGaugeNick gaugeNick = new IGaugeNick() {
        @Override
        public boolean shouldDrawMajorNick(int nick, float value) {
            return (nick % majorNickInterval == 0);
        }

        @Override
        public boolean shouldDrawHalfNick(int nick, float value) {
            if (minorTicInterval > 0) {
                return nick % minorTicInterval == 0;
            }
            return false;
        }

        @Override
        public String getLabelString(int nick, float value) {
            if (shouldDrawMajorNick(nick, value)) {
                return String.valueOf(Math.round(value));
            } else {
                return null;
            }
        }
    };

    private Paint needlePaint;
    private Path needlePath;
    private Paint needleScrewPaint;

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

    private int totalNicks = 120;
    private float startAngle = 10;
    private float endAngle = 350;
    private float degreesPerNick = (endAngle - startAngle) / totalNicks;
    private float valuePerNick = 10;
    private float minValue = 0;
    private float maxValue = 1000;
    private boolean intScale = true;

    private float requestedLabelTextSize = 0;

    private float initialValue = 0;
    private float value = 0;
    private float needleValue = 0;

    private float needleStep;

    private float labelRadius;

    private int majorNickInterval = 10;
    private int minorTicInterval = -1;

    private int deltaTimeInterval = 5;
    private float needleStepFactor = 3f;

    private static final String TAG = Gauge.class.getSimpleName();
    private Paint labelPaint;
    private long lastMoveTime;
    private boolean needleShadow = true;
    private int faceColor;
    private int rimColor;
    private int scaleColor;
    private int needleColor;
    private Paint upperTextPaint;
    private Paint lowerTextPaint;

    private float requestedTextSize = 0;
    private float requestedUpperTextSize = 0;
    private float requestedLowerTextSize = 0;
    private String upperText = "";
    private String lowerText = "";

    private float textScaleFactor;

    private static final int REF_MAX_PORTRAIT_CANVAS_SIZE = 1080; // reference size, scale text accordingly

    public Gauge(Context context) {
        super(context);
        initValues();
        initPaint();
    }

    public Gauge(Context context, AttributeSet attrs) {
        super(context, attrs);
        applyAttrs(context, attrs);
        initValues();
        initPaint();
    }

    public Gauge(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        applyAttrs(context, attrs);
        initPaint();
    }

    private void applyAttrs(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Gauge, 0, 0);
        totalNicks = a.getInt(R.styleable.Gauge_totalNicks, totalNicks);
        startAngle = a.getFloat(R.styleable.Gauge_startAngle, startAngle);
        endAngle = a.getFloat(R.styleable.Gauge_endAngle, endAngle);
        majorNickInterval = a.getInt(R.styleable.Gauge_majorNickInterval, majorNickInterval);
        minValue = a.getFloat(R.styleable.Gauge_minValue, minValue);
        maxValue = a.getFloat(R.styleable.Gauge_maxValue, maxValue);
        intScale = a.getBoolean(R.styleable.Gauge_intScale, intScale);
        initialValue = a.getFloat(R.styleable.Gauge_initialValue, initialValue);
        requestedLabelTextSize = a.getFloat(R.styleable.Gauge_labelTextSize, requestedLabelTextSize);
        faceColor = a.getColor(R.styleable.Gauge_faceColor, Color.argb(0xff, 0xff, 0xff, 0xff));
        rimColor = a.getColor(R.styleable.Gauge_rimColor, Color.argb(0x4f, 0x33, 0x36, 0x33));
        scaleColor = a.getColor(R.styleable.Gauge_scaleColor, 0x9f004d0f);
        needleColor = a.getColor(R.styleable.Gauge_needleColor, Color.RED);
        needleShadow = a.getBoolean(R.styleable.Gauge_needleShadow, needleShadow);
        requestedTextSize = a.getFloat(R.styleable.Gauge_textSize, requestedTextSize);
        upperText = a.getString(R.styleable.Gauge_upperText) == null ? upperText : fromHtml(a.getString(R.styleable.Gauge_upperText)).toString();
        lowerText = a.getString(R.styleable.Gauge_lowerText) == null ? lowerText : fromHtml(a.getString(R.styleable.Gauge_lowerText)).toString();
        requestedUpperTextSize = a.getFloat(R.styleable.Gauge_upperTextSize, 0);
        requestedLowerTextSize = a.getFloat(R.styleable.Gauge_lowerTextSize, 0);
        a.recycle();

        initValues();
        validate();
    }

    private void initValues() {
        degreesPerNick = (endAngle - startAngle) / totalNicks;
        valuePerNick = (maxValue - minValue) / totalNicks;
        needleStep = needleStepFactor * valuePerDegree();
        needleValue = value = initialValue;

        if (majorNickInterval % 2 == 0) {
            minorTicInterval = majorNickInterval/2;
        } else if (majorNickInterval % 3 == 0) {
            minorTicInterval = majorNickInterval/3;
        } else if (majorNickInterval % 5 == 0) {
            minorTicInterval = majorNickInterval/5;
        }

        int widthPixels = getResources().getDisplayMetrics().widthPixels;
        textScaleFactor = (float) widthPixels / (float) REF_MAX_PORTRAIT_CANVAS_SIZE;

        if (getResources().getBoolean(R.bool.landscape)) {
            int heightPixels = getResources().getDisplayMetrics().heightPixels;
            float portraitAspectRatio = (float) heightPixels / (float) widthPixels;
            textScaleFactor = textScaleFactor * portraitAspectRatio;
        }
    }

    private void initPaint() {

        setSaveEnabled(true);

        // Rim and shadow are based on the Vintage Thermometer:
        // http://mindtherobot.com/blog/272/android-custom-ui-making-a-vintage-thermometer/

        rimPaint = new Paint();
        rimPaint.setAntiAlias(true);
        rimPaint.setColor(rimColor);

        rimCirclePaint = new Paint();
        rimCirclePaint.setAntiAlias(true);
        rimCirclePaint.setStyle(Paint.Style.STROKE);
        rimCirclePaint.setColor(rimColor);
        rimCirclePaint.setStrokeWidth(0.005f);

        facePaint = new Paint();
        facePaint.setAntiAlias(true);
        facePaint.setStyle(Paint.Style.FILL);
        facePaint.setColor(faceColor);

        rimShadowPaint = new Paint();
        rimShadowPaint.setStyle(Paint.Style.FILL);

        scalePaint = new Paint();
        scalePaint.setStyle(Paint.Style.STROKE);

        scalePaint.setAntiAlias(true);
        scalePaint.setColor(scaleColor);

        labelPaint = new Paint();
        labelPaint.setColor(scaleColor);
        labelPaint.setTypeface(Typeface.SANS_SERIF);
        labelPaint.setTextAlign(Paint.Align.CENTER);

        upperTextPaint = new Paint();
        upperTextPaint.setColor(scaleColor);
        upperTextPaint.setTypeface(Typeface.SANS_SERIF);
        upperTextPaint.setTextAlign(Paint.Align.CENTER);

        lowerTextPaint = new Paint();
        lowerTextPaint.setColor(scaleColor);
        lowerTextPaint.setTypeface(Typeface.SANS_SERIF);
        lowerTextPaint.setTextAlign(Paint.Align.CENTER);

        needlePaint = new Paint();
        needlePaint.setColor(needleColor);
        needlePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        needlePaint.setAntiAlias(true);

        needlePath = new Path();

        needleScrewPaint = new Paint();
        needleScrewPaint.setColor(Color.BLACK);
        needleScrewPaint.setAntiAlias(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawRim(canvas);
        drawFace(canvas);
        drawScale(canvas);
        drawTexts(canvas);
        canvas.rotate(scaleToCanvasDegrees(valueToDegrees(needleValue)), canvasCenterX, canvasCenterY);
        canvas.drawPath(needlePath, needlePaint);
        canvas.drawCircle(canvasCenterX, canvasCenterY, canvasWidth / 61f, needleScrewPaint);

        if (needsToMove()) {
            moveNeedle();
        }
    }

    private void moveNeedle() {
        long currentTime = System.currentTimeMillis();
        long deltaTime = currentTime - lastMoveTime;

        if (deltaTime >= deltaTimeInterval) {
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
            postInvalidateDelayed(deltaTimeInterval);
        }
    }

    private void drawRim(Canvas canvas) {
        canvas.drawOval(rimRect, rimPaint);
        canvas.drawOval(rimRect, rimCirclePaint);
    }

    private void drawFace(Canvas canvas) {
        canvas.drawOval(faceRect, facePaint);
        canvas.drawOval(faceRect, rimCirclePaint);
        canvas.drawOval(faceRect, rimShadowPaint);
    }

    private void drawScale(Canvas canvas) {
        float y1 = scaleRect.top;
        float y2 = y1 + (0.020f * canvasHeight);
        float y3 = y1 + (0.060f * canvasHeight);
        float y4 = y1 + (0.030f * canvasHeight);

        for (int i = 0; i <= totalNicks; ++i) {
            canvas.save();
            canvas.rotate(i * degreesPerNick + 180 + startAngle, 0.5f * canvasWidth, 0.5f * canvasHeight);
            canvas.drawLine(0.5f * canvasWidth, y1, 0.5f * canvasWidth, y2, scalePaint);

            if (gaugeNick.shouldDrawMajorNick(i, i * valuePerNick)) {
                canvas.drawLine(0.5f * canvasWidth, y1, 0.5f * canvasWidth, y3, scalePaint);
            }

            if (gaugeNick.shouldDrawHalfNick(i, i * valuePerNick)) {
                canvas.drawLine(0.5f * canvasWidth, y1, 0.5f * canvasWidth, y4, scalePaint);
            }
            canvas.restore();
            drawLabels(canvas, i);
        }
    }

    private void drawLabels(Canvas canvas, int i) {
        String valueLabel = gaugeNick.getLabelString(i, i * valuePerNick);
        if (valueLabel != null) {
            float scaleAngle = (i * degreesPerNick) + 180 + startAngle;
            float scaleAngleRads = (float) Math.toRadians(scaleAngle);
            //Log.d(TAG, "i = " + i + ", angle = " + scaleAngle + ", value = " + value);
            float deltaX = labelRadius * (float) Math.sin(scaleAngleRads);
            float deltaY = labelRadius * (float) Math.cos(scaleAngleRads);
            drawTextCentered(valueLabel, canvasCenterX + deltaX, canvasCenterY - deltaY, labelPaint, canvas);
        }
    }

    private void drawTexts(Canvas canvas) {
        drawTextCentered(upperText, canvasCenterX, canvasCenterY - (canvasHeight / 6.5f), upperTextPaint, canvas);
        drawTextCentered(lowerText, canvasCenterX, canvasCenterY + (canvasHeight / 6.5f), lowerTextPaint, canvas);
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

        needlePaint.setStrokeWidth(canvasWidth / 197f);

        if (needleShadow)
            needlePaint.setShadowLayer(canvasWidth / 123f, canvasWidth / 10000f, canvasWidth / 10000f, Color.GRAY);

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

        labelRadius = (canvasCenterX - scaleRect.left) * 0.70f;

        /*
        Log.d(TAG, "width = " + w);
        Log.d(TAG, "height = " + h);
        Log.d(TAG, "width pixels = " + getResources().getDisplayMetrics().widthPixels);
        Log.d(TAG, "height pixels = " + getResources().getDisplayMetrics().heightPixels);
        Log.d(TAG, "density = " + getResources().getDisplayMetrics().density);
        Log.d(TAG, "density dpi = " + getResources().getDisplayMetrics().densityDpi);
        Log.d(TAG, "scaled density = " + getResources().getDisplayMetrics().scaledDensity);
        */

        float textSize;

        if (requestedLabelTextSize > 0) {
            textSize = requestedLabelTextSize * textScaleFactor;
        } else {
            textSize = canvasWidth / 16f;
        }
        Log.d(TAG, "Label text size = " + textSize);
        labelPaint.setTextSize(textSize);

        if (requestedTextSize > 0) {
            textSize = requestedTextSize * textScaleFactor;
        } else {
            textSize = canvasWidth / 14f;
        }
        Log.d(TAG, "Default upper/lower text size = " + textSize);
        upperTextPaint.setTextSize(requestedUpperTextSize > 0 ? requestedUpperTextSize * textScaleFactor: textSize);
        lowerTextPaint.setTextSize(requestedLowerTextSize > 0 ? requestedLowerTextSize * textScaleFactor: textSize);

        super.onSizeChanged(w, h, oldw, oldh);
    }

    private void setNeedle() {
        needlePath.reset();
        needlePath.moveTo(canvasCenterX - needleTailLength, canvasCenterY);
        needlePath.lineTo(canvasCenterX, canvasCenterY - (needleWidth / 2));
        needlePath.lineTo(canvasCenterX + needleLength, canvasCenterY);
        needlePath.lineTo(canvasCenterX, canvasCenterY + (needleWidth / 2));
        needlePath.lineTo(canvasCenterX - needleTailLength, canvasCenterY);
        needlePath.addCircle(canvasCenterX, canvasCenterY, canvasWidth / 49f, Path.Direction.CW);
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
        int heightWithoutPadding = height - getPaddingTop() - getPaddingBottom();

        if (widthWithoutPadding > heightWithoutPadding) {
            size = heightWithoutPadding;
        } else {
            size = widthWithoutPadding;
        }

        setMeasuredDimension(size + getPaddingLeft() + getPaddingRight(), size + getPaddingTop() + getPaddingBottom());
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("superState", super.onSaveInstanceState());
        bundle.putFloat("value", value);
        bundle.putFloat("needleValue", needleValue);
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            value = bundle.getFloat("value");
            needleValue = bundle.getFloat("needleValue");
            super.onRestoreInstanceState(bundle.getParcelable("superState"));
        } else {
            super.onRestoreInstanceState(state);
        }
    }

    private float valueToDegrees(float value) {
        float angle = (value/valuePerNick * degreesPerNick);
        return angle <= endAngle ? angle  + 180 + startAngle : endAngle  + 180;
    }

    private float valuePerDegree() {
        return valuePerNick / degreesPerNick;
    }

    private float scaleToCanvasDegrees(float degrees) {
        return degrees - 90;
    }

    private boolean needsToMove() {
        return Math.abs(needleValue - value) > 0;
    }

    private void drawTextCentered(String text, float x, float y, Paint paint, Canvas canvas) {
        //float xPos = x - (paint.measureText(text)/2f);
        float yPos = (y - ((paint.descent() + paint.ascent()) / 2f));
        canvas.drawText(text, x, yPos, paint);
    }

    /**
     * Set gauge to value.
     *
     * @param value Value
     */
    public void setValue(float value) {
        needleValue = this.value = value;
        postInvalidate();
    }

    /**
     * Animate gauge to value.
     *
     * @param value Value
     */
    public void moveToValue(float value) {
        this.value = value;
        postInvalidate();
    }

    /**
     * Set string to display on upper gauge face.
     *
     * @param text Text
     */
    public void setUpperText(String text) {
        upperText = text;
        invalidate();
    }

    /**
     * Set string to display on lower gauge face.
     *
     * @param text Text
     */
    public void setLowerText(String text) {
        lowerText = text;
        invalidate();
    }

    /**
     * Request a text size.
     *
     * @param size Size (pixels)
     * @see Paint#setTextSize(float);
     */
    @Deprecated
    public void setRequestedTextSize(float size) {
        setTextSize(size);
    }

    /**
     * Set a text size for the upper and lower text.
     *
     * Size is in pixels at a screen width (max. canvas width/height) of 1080 and is scaled
     * accordingly at different resolutions. E.g. a value of 48 is unchanged at 1080 x 1920
     * and scaled down to 27 at 600 x 1024.
     *
     * @param size Size (relative pixels)
     * @see Paint#setTextSize(float);
     */
    public void setTextSize(float size) {
        requestedTextSize = size;
    }

    /**
     * Set or override the text size for the upper text.
     *
     * Size is in pixels at a screen width (max. canvas width/height) of 1080 and is scaled
     * accordingly at different resolutions. E.g. a value of 48 is unchanged at 1080 x 1920
     * and scaled down to 27 at 600 x 1024.
     *
     * @param size (relative pixels)
     * @see Paint#setTextSize(float);
     */
    public void setUpperTextSize(float size) {
        requestedUpperTextSize = size;
    }

    /**
     * Set or override the text size for the lower text
     *
     * Size is in pixels at a screen width (max. canvas width/height) of 1080 and is scaled
     * accordingly at different resolutions. E.g. a value of 48 is unchanged at 1080 x 1920
     * and scaled down to 27 at 600 x 1024.
     *
     * @param size (relative pixels)
     * @see Paint#setTextSize(float);
     */
    public void setLowerTextSize(float size) {
        requestedLowerTextSize = size;
    }

    /**
     * Set the delta time between movement steps during needle animation (default: 5 ms).
     *
     * @param interval Time (ms)
     */
    public void setDeltaTimeInterval(int interval) {
        deltaTimeInterval = interval;
    }

    /**
     * Set the factor that determines the step size during needle animation (default: 3f).
     * The actual step size is calulated as follows: step_size = step_factor * scale_value_per_degree.
     *
     * @param factor Step factor
     */
    public void setNeedleStepFactor(float factor) {
        needleStepFactor = factor;
    }


    /**
     * Set the minimum scale value.
     *
     * @param value minimum value
     */
    public void setMinValue(float value) {
        minValue = value;
        initValues();
        validate();
        invalidate();
    }

    /**
     * Set the maximum scale value.
     *
     * @param value maximum value
     */
    public void setMaxValue(float value) {
        maxValue = value;
        initValues();
        validate();
        invalidate();
    }

    /**
     * Set the starting angle for the values. The origin is the bottom (6h) position.
     *
     * @param value start angle
     */
    public void setStartAngle(float value) {
        startAngle = value;
        initValues();
        validate();
        invalidate();
    }

    /**
     * Set the end angle for the values. The origin is the bottom (6h) position.
     *
     * @param value end angle
     */
    public void setEndAngle(float value) {
        endAngle = value;
        initValues();
        validate();
        invalidate();
    }

    /**
     * Set the total amount of nicks on a full 360 degree scale. Should be a multiple of majorNickInterval.
     *
     * @param nicks number of nicks
     */
    public void setTotalNicks(int nicks) {
        totalNicks = nicks;
        initValues();
        validate();
        invalidate();
    }

    /**
     * Set the interval (number of nicks) between enlarged nicks.
     *
     * @param interval major nick interval
     */
    public void setMajorNickInterval(int interval) {
        majorNickInterval = interval;
        initValues();
        validate();
        invalidate();
    }

    /**
     * Set the class to handle when to draw a nick.
     *
     * @param nickHandler major nick interval
     */
    public void setNickHandler(IGaugeNick nickHandler) {
        this.gaugeNick = nickHandler;
        initValues();
        validate();
        invalidate();
    }

    private void validate() {
        boolean valid = true;
        if (totalNicks % majorNickInterval != 0) {
            valid = false;
            Log.w(TAG, getResources().getString(R.string.invalid_number_of_nicks, totalNicks, majorNickInterval));
        }
        float sum = minValue + maxValue;
        int intSum = Math.round(sum);
        if ((maxValue >= 1 && (sum != intSum || (intSum & 1) != 0)) || minValue >= maxValue) {
            valid = false;
            Log.w(TAG, getResources().getString(R.string.invalid_min_max_ratio, minValue, maxValue));
        }
        if (Math.round(sum % valuePerNick) != 0) {
            valid = false;
            Log.w(TAG, getResources().getString(R.string.invalid_min_max, minValue, maxValue, valuePerNick));
        }
        if (valid) Log.i(TAG, getResources().getString(R.string.scale_ok));
    }

    @SuppressWarnings("deprecation")
    private static Spanned fromHtml(String html) {
        Spanned result;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            result = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        } else {
            result = Html.fromHtml(html);
        }
        return result;
    }

}
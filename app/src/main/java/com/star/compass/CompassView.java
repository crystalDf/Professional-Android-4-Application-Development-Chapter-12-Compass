package com.star.compass;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;

public class CompassView extends View {

    private float mBearing;

    private Paint mMarkerPaint;
    private Paint mTextPaint;
    private Paint mCirclePaint;
    private String mNorthString;
    private String mEastString;
    private String mSouthString;
    private String mWestString;
    private int mTextHeight;

    private float mPitch;
    private float mRoll;

    private int[] mBorderGradientColors;
    private float[] mBorderGradientPositions;

    private int[] mGlassGradientColors;
    private float[] mGlassGradientPositions;

    private int mSkyHorizonColorFrom;
    private int mSkyHorizonColorTo;
    private int mGroundHorizonColorFrom;
    private int mGroundHorizonColorTo;

    private enum CompassDirection {
        N, NNE, NE, ENE,
        E, ESE, SE, SSE,
        S, SSW, SW, WSW,
        W, WNW, NW, NNW
    }

    public CompassView(Context context) {
        super(context);
        initCompassView();
    }

    public CompassView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initCompassView();
    }

    public CompassView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initCompassView();
    }

    public float getBearing() {
        return mBearing;
    }

    public void setBearing(float bearing) {
        mBearing = bearing;
        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED);
    }

    public float getPitch() {
        return mPitch;
    }

    public void setPitch(float pitch) {
        mPitch = pitch;
        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED);
    }

    public float getRoll() {
        return mRoll;
    }

    public void setRoll(float roll) {
        mRoll = roll;
        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED);
    }

    protected void initCompassView() {
        setFocusable(true);

        Resources resources = getResources();

        mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCirclePaint.setColor(resources.getColor(R.color.background_color));
        mCirclePaint.setStrokeWidth(1);
        mCirclePaint.setStyle(Paint.Style.STROKE);

        mNorthString = resources.getString(R.string.cardinal_north);
        mEastString = resources.getString(R.string.cardinal_east);
        mSouthString = resources.getString(R.string.cardinal_south);
        mWestString = resources.getString(R.string.cardinal_west);

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(resources.getColor(R.color.text_color));
        mTextPaint.setFakeBoldText(true);
        mTextPaint.setSubpixelText(true);
        mTextPaint.setTextAlign(Paint.Align.LEFT);

        mTextHeight = (int) mTextPaint.measureText("yY");

        mMarkerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mMarkerPaint.setColor(resources.getColor(R.color.marker_color));
        mMarkerPaint.setAlpha(200);
        mMarkerPaint.setStrokeWidth(1);
        mMarkerPaint.setStyle(Paint.Style.STROKE);
        mMarkerPaint.setShadowLayer(2, 1, 1, resources.getColor(R.color.shadow_color));

        mBorderGradientColors = new int[4];
        mBorderGradientPositions = new float[4];

        mBorderGradientColors[3] = resources.getColor(R.color.outer_border);
        mBorderGradientColors[2] = resources.getColor(R.color.inner_border_one);
        mBorderGradientColors[1] = resources.getColor(R.color.inner_border_two);
        mBorderGradientColors[0] = resources.getColor(R.color.inner_border);

        mBorderGradientPositions[3] = 0.0f;
        mBorderGradientPositions[2] = 1 - 0.03f;
        mBorderGradientPositions[1] = 1 - 0.06f;
        mBorderGradientPositions[0] = 1.0f;

        mGlassGradientColors = new int[5];
        mGlassGradientPositions = new float[5];

        int glassColor = 245;

        mGlassGradientColors[4] = Color.argb(65, glassColor, glassColor, glassColor);
        mGlassGradientColors[3] = Color.argb(100, glassColor, glassColor, glassColor);
        mGlassGradientColors[2] = Color.argb(50, glassColor, glassColor, glassColor);
        mGlassGradientColors[1] = Color.argb(0, glassColor, glassColor, glassColor);
        mGlassGradientColors[0] = Color.argb(0, glassColor, glassColor, glassColor);

        mGlassGradientPositions[4] = 1 - 0.0f;
        mGlassGradientPositions[3] = 1 - 0.06f;
        mGlassGradientPositions[2] = 1 - 0.10f;
        mGlassGradientPositions[1] = 1 - 0.20f;
        mGlassGradientPositions[0] = 1 - 1.0f;

        mSkyHorizonColorFrom = resources.getColor(R.color.horizon_sky_from);
        mSkyHorizonColorTo = resources.getColor(R.color.horizon_sky_to);

        mGroundHorizonColorFrom = resources.getColor(R.color.horizon_ground_from);
        mGroundHorizonColorTo = resources.getColor(R.color.horizon_ground_to);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measuredWidth = measure(widthMeasureSpec);
        int measuredHeight = measure(heightMeasureSpec);

        int d = Math.min(measuredWidth, measuredHeight);

        setMeasuredDimension(d, d);
    }

    private int measure(int measureSpec) {
        int result = 0;

        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.UNSPECIFIED) {
            result = 200;
        } else {
            result =specSize;
        }

        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        float ringWidth = mTextHeight + 4;

        int width = getMeasuredWidth();
        int height = getMeasuredHeight();

        int px = width / 2;
        int py = height / 2;

        Point center = new Point(px, py);

        int radius = Math.min(px, py) - 2;

        RectF boundingBox = new RectF(
                center.x - radius,
                center.y - radius,
                center.x + radius,
                center.y + radius
        );

        RectF innerBoundingBox = new RectF(
                center.x - radius + ringWidth,
                center.y - radius + ringWidth,
                center.x + radius - ringWidth,
                center.y + radius - ringWidth
        );

        float innerRadius = innerBoundingBox.height() / 2;

        RadialGradient borderGradient = new RadialGradient(px, py, radius,
                mBorderGradientColors, mBorderGradientPositions, Shader.TileMode.CLAMP);

        Paint pgb = new Paint();
        pgb.setShader(borderGradient);

        Path outerRingPath = new Path();
        outerRingPath.addOval(boundingBox, Path.Direction.CW);

        canvas.drawPath(outerRingPath, pgb);

        LinearGradient skyShader = new LinearGradient(
                center.x, innerBoundingBox.top, center.x, innerBoundingBox.bottom,
                mSkyHorizonColorFrom, mSkyHorizonColorTo, Shader.TileMode.CLAMP
        );

        Paint skyPaint = new Paint();
        skyPaint.setShader(skyShader);

        LinearGradient groundShader = new LinearGradient(
                center.x, innerBoundingBox.top, center.x, innerBoundingBox.bottom,
                mGroundHorizonColorFrom, mGroundHorizonColorTo, Shader.TileMode.CLAMP
        );

        Paint groundPaint = new Paint();
        groundPaint.setShader(groundShader);

        float tiltDegree = mPitch;

        while (tiltDegree > 90 || tiltDegree < -90) {
            if (tiltDegree > 90) {
                tiltDegree = -90 + (tiltDegree - 90);
            }

            if (tiltDegree < -90) {
                tiltDegree = 90 - (tiltDegree + 90);
            }
        }

        float rollDegree = mRoll;

        while (rollDegree > 180 || rollDegree < -180) {
            if (rollDegree > 180) {
                rollDegree = -180 + (rollDegree - 180);
            }

            if (rollDegree < -180) {
                rollDegree = 180 - (rollDegree + 180);
            }
        }

        Path skyPath = new Path();
        skyPath.addArc(innerBoundingBox, -tiltDegree, (180 + (2 * tiltDegree)));

        canvas.save();
        canvas.rotate(-rollDegree, px, py);
        canvas.drawOval(innerBoundingBox, groundPaint);
        canvas.drawPath(skyPath, skyPaint);
        canvas.drawPath(skyPath, mMarkerPaint);

        int markWidth = radius / 3;
        int startX = center.x - markWidth;
        int endX = center.x + markWidth;

        double h = innerRadius * Math.cos(Math.toRadians(90 - tiltDegree));
        double justTiltY = center.y - h;

        float pxPerDegree = (innerBoundingBox.height() / 2) / 45f;

        for (int i = 90; i >= -90; i -= 10) {
            double yPos = justTiltY + i * pxPerDegree;

            if (yPos < (innerBoundingBox.top + mTextHeight) ||
                    yPos > (innerBoundingBox.bottom - mTextHeight)) {
                continue;
            }
            {
                canvas.drawLine(startX, (float) yPos, endX, (float) yPos, mMarkerPaint);

                int displayPos = (int) (tiltDegree - i);

                String displayString = displayPos + "";

                float stringSizeWidth = mTextPaint.measureText(displayString);

                canvas.drawText(displayString, (int) (center.x - stringSizeWidth / 2),
                        (int) (yPos + 1), mTextPaint);
            }
        }

        mMarkerPaint.setStrokeWidth(2);

        canvas.drawLine(center.x - radius / 2, (float) justTiltY,
                center.x + radius / 2, (float) justTiltY, mMarkerPaint);

        mMarkerPaint.setStrokeWidth(1);

        Path rollArrow = new Path();
        rollArrow.moveTo(center.x - 3, innerBoundingBox.top + 14);
        rollArrow.lineTo(center.x, innerBoundingBox.top + 10);
        rollArrow.moveTo(center.x + 3, innerBoundingBox.top + 14);
        rollArrow.lineTo(center.x, innerBoundingBox.top + 10);

        canvas.drawPath(rollArrow, mMarkerPaint);

        String rollText = rollDegree + "";
        double rollTextWidth = mTextPaint.measureText(rollText);

        canvas.drawText(rollText, (float) (center.x - rollTextWidth / 2),
                innerBoundingBox.top + mTextHeight + 2, mTextPaint);

        canvas.restore();

        canvas.save();

        canvas.rotate(180, center.x, center.y);
        for (int i = -180; i < 180; i+= 10) {
            if (i % 30 == 0) {
                String rollString = i * (-1) + "";
                float rollStringWidth = mTextPaint.measureText(rollString);

                PointF rollStringCenter = new PointF(
                        center.x - rollStringWidth / 2,
                        innerBoundingBox.top + 1 + mTextHeight
                );

                canvas.drawText(rollString, rollStringCenter.x,
                        rollStringCenter.y, mTextPaint);
            } else {
                canvas.drawLine(center.x, innerBoundingBox.top,
                        center.x, innerBoundingBox.top + 5, mMarkerPaint);
            }
            canvas.rotate(10, center.x, center.y);
        }

        canvas.restore();

        canvas.save();

        canvas.rotate(mBearing * (-1), px, py);

        double increment = 22.5;

        for (double i = 0; i < 360; i += increment) {
            CompassDirection compassDirection = CompassDirection.values()[((int) (i / 22.5))];

            String headString = compassDirection + "";

            float headStringWidth = mTextPaint.measureText(headString);

            PointF headStringCenter = new PointF(
                    center.x - headStringWidth / 2,
                    boundingBox.top + 1 + mTextHeight
            );

            if (i % increment == 0) {
                canvas.drawText(headString, headStringCenter.x, headStringCenter.y, mTextPaint);
            } else {
                canvas.drawLine(center.x, boundingBox.top,
                        center.x, boundingBox.top + 3, mMarkerPaint);
            }

            canvas.rotate((int) increment, center.x, center.y);
        }

        canvas.restore();

        RadialGradient glassShader = new RadialGradient(px, py, innerRadius,
                mGlassGradientColors, mGlassGradientPositions, Shader.TileMode.CLAMP);

        Paint glassPaint = new Paint();
        glassPaint.setShader(glassShader);

        canvas.drawOval(innerBoundingBox, glassPaint);

        canvas.drawOval(boundingBox, mCirclePaint);

        mCirclePaint.setStrokeWidth(2);
        canvas.drawOval(innerBoundingBox, mCirclePaint);
    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        super.dispatchPopulateAccessibilityEvent(event);

        if (isShown()) {
            String bearingStr = mBearing + "";
            event.getText().add(bearingStr);

            return true;
        } else {
            return false;
        }
    }
}

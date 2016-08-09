package com.trevor.ultimatehue.helpers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ComposeShader;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by nemo on 9/21/15.
 */

public class ColorPickerView extends View {
    public static abstract interface OnColorChangedListener {
        public abstract void colorChanged(int color, int hue);
    }

    private static int CENTER_RADIUS = 0;
    private static int CENTER_X = 0;
    private static int CENTER_Y = 0;
    private static final float PI = 3.141593F;
    private long colorChangeRateLimit = 200000000L;
    private int hue;
    private long lastColorChange;
    private Paint mCenterPaint;
    private final int[] mColors = { -65536, -65281, -16776961, -16711681,
            -16711936, -256, -65536 };
    private boolean mHighlightCenter;
    private OnColorChangedListener mListener;
    private Paint mPaint;
    private boolean mTrackingCenter;
    private int parentHeight;

    private int parentWidth;

    public ColorPickerView(Context paramContext, AttributeSet paramAttributeSet) {
        super(paramContext, paramAttributeSet);
        SweepGradient localSweepGradient = new SweepGradient(0.0F, 0.0F,
                mColors, null);
        mPaint = new Paint(1);
        mPaint.setShader(localSweepGradient);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(32.0F);
        mCenterPaint = new Paint(1);
        mCenterPaint.setStrokeWidth(5.0F);
    }

    private int ave(int paramInt1, int paramInt2, float paramFloat) {
        return paramInt1 + Math.round(paramFloat * (paramInt2 - paramInt1));
    }

    private int floatToByte(float paramFloat) {
        return Math.round(paramFloat);
    }

    public int getColor() {
        return mCenterPaint.getColor();
    }

    public int getHue() {
        return hue;
    }

    private int interpColor(int colors[], float unit) {
        if (unit <= 0) {
            return colors[0];
        }
        if (unit >= 1) {
            return colors[colors.length - 1];
        }

        float p = unit * (colors.length - 1);
        int i = (int) p;
        p -= i;

        int c0 = colors[i];
        int c1 = colors[i + 1];
        int a = ave(Color.alpha(c0), Color.alpha(c1), p);
        int r = ave(Color.red(c0), Color.red(c1), p);
        int g = ave(Color.green(c0), Color.green(c1), p);
        int b = ave(Color.blue(c0), Color.blue(c1), p);

        return Color.argb(a, r, g, b);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float f = CENTER_X - 0.5F * mPaint.getStrokeWidth();
        canvas.translate(CENTER_X, CENTER_X);
        canvas.drawOval(new RectF(-f, -f, f, f), mPaint);
        canvas.drawCircle(0.0F, 0.0F, CENTER_RADIUS, mCenterPaint);
        if (mTrackingCenter) {
            int i = mCenterPaint.getColor();
            mCenterPaint.setStyle(android.graphics.Paint.Style.STROKE);
            if (mHighlightCenter)
                mCenterPaint.setAlpha(255);
            else
                mCenterPaint.setAlpha(128);
            canvas.drawCircle(0.0F, 0.0F,
                    CENTER_RADIUS + mCenterPaint.getStrokeWidth(),
                    mCenterPaint);
            mCenterPaint.setStyle(android.graphics.Paint.Style.FILL);
            mCenterPaint.setColor(i);
        }
    }

    @Override
    protected void onMeasure(int paramInt1, int paramInt2) {
        parentWidth = View.MeasureSpec.getSize(paramInt1);
        parentHeight = View.MeasureSpec.getSize(paramInt2);
        int i = Math.max(Math.min(parentWidth, parentHeight), 320);
        setMeasuredDimension(i, i);
        CENTER_X = i / 2;
        CENTER_Y = i / 2;
        CENTER_RADIUS = i / 6;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX() - CENTER_X;
        float y = event.getY() - CENTER_Y;
        boolean inCenter = java.lang.Math.sqrt(x * x + y * y) <= CENTER_RADIUS;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTrackingCenter = inCenter;
                if (inCenter) {
                    mHighlightCenter = true;
                    invalidate();
                    break;
                }
            case MotionEvent.ACTION_MOVE:
                if (mTrackingCenter) {
                    if (mHighlightCenter != inCenter) {
                        mHighlightCenter = inCenter;
                        invalidate();
                    }
                } else {
                    float f3 = (float) Math.atan2(y, x) / 6.283185F;
                    if (f3 < 0.0F) {
                        f3 += 1.0F;
                    }
                    hue = ((int) ((1.0F - f3) * 65535.0F));
                    mCenterPaint.setColor(interpColor(mColors, f3));
                    invalidate();
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mTrackingCenter) {
                    if (inCenter) {
                        if (mListener != null) {
                            mListener.colorChanged(mCenterPaint.getColor(), hue);
                        }
                    }
                    mTrackingCenter = false; // so we draw w/o halo
                    invalidate();
                }
                break;
        }
        return true;
    }

    private int pinToByte(int paramInt) {
        if (paramInt < 0)
            paramInt = 0;
        while (paramInt <= 255)
            return paramInt;
        return 255;
    }

    public void setInitialColor(int paramInt) {
        if (mPaint != null) {
            mCenterPaint = new Paint(1);
            mCenterPaint.setStrokeWidth(5.0F);
        }
        mCenterPaint.setColor(paramInt);
    }

    public void setOnColorChangedListener(
            OnColorChangedListener paramOnColorChangedListener) {
        mListener = paramOnColorChangedListener;
    }

}



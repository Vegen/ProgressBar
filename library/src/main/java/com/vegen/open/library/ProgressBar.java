package com.vegen.open.library;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.support.annotation.FloatRange;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Vegen
 * @creation_time 2018/12/6
 * @description 圆环流量进度条
 */
public class ProgressBar extends View {

    private int mProgressStartColor;
    private int mProgressMidColor;
    private int mProgressEndColor;
    private int mBgStartColor;
    private int mBgMidColor;
    private int mBgEndColor;
    private float mProgressPercent;
    private float mProgressWidth;
    private int mStartAngle;
    private int mSweepAngle;
    private boolean mShowDefaultAnim;
    private boolean mStrokeCap;
    private int mDefaultAllAnimDuration;

    private int mShowPattern;

    private int PATTERN_PERCENT_COVER = 0;
    private int PATTERN_PERCENT_SPLICE = 1;

    private int mMeasureHeight;
    private int mMeasureWidth;

    private Paint mBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
    private Paint mProgressPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
    private Paint mTextPaint;
    private RectF pRectF;
    private LinearGradient mLinearGradient;

    private List<Paint> splicePaintList;

    private float mUnitAngle;

    private int mCurProgress = 0;

    private ProgressBarCoverAdapter mProgressAdapter;
    private ProgressBarSpliceAdapter mSpliceAdapter;
    private List<TextWithStyle> textWithStyleList;
    private float mLineSpace;
    private boolean mShowDotFront;
    private float mDotFrontMargin;
    private int mTextGravity;
    private float mDotFrontDiameter;

    private float progressPercent;

    public ProgressBar(Context context) {
        this(context, null);
    }

    public ProgressBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ProgressBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ProgressBar);
        mShowPattern = ta.getInt(R.styleable.ProgressBar_showPattern, PATTERN_PERCENT_COVER);

        // percentCover 模式特有属性
        mProgressStartColor = ta.getColor(R.styleable.ProgressBar_cover_start_color, Color.YELLOW);
        mProgressMidColor = ta.getColor(R.styleable.ProgressBar_cover_mid_color, mProgressStartColor);
        mProgressEndColor = ta.getColor(R.styleable.ProgressBar_cover_end_color, mProgressStartColor);
        mBgStartColor = ta.getColor(R.styleable.ProgressBar_cover_bg_start_color, Color.LTGRAY);
        mBgMidColor = ta.getColor(R.styleable.ProgressBar_cover_bg_mid_color, mBgStartColor);
        mBgEndColor = ta.getColor(R.styleable.ProgressBar_cover_bg_end_color, mBgStartColor);
        mProgressPercent = ta.getFloat(R.styleable.ProgressBar_cover_percent, 0f);
        mSweepAngle = ta.getInt(R.styleable.ProgressBar_cover_sweep_angle, 360);
        mStrokeCap = ta.getBoolean(R.styleable.ProgressBar_cover_stroke_cap, false);

        // percentSplice 模式特有属性 暂无

        // 公共属性
        mProgressWidth = ta.getDimension(R.styleable.ProgressBar_progress_width, 8f);
        mStartAngle = ta.getInt(R.styleable.ProgressBar_start_angle, mShowPattern == PATTERN_PERCENT_COVER ?
                (int) (180 + (1 - mProgressPercent) * mSweepAngle * 2) : 0);
        mDefaultAllAnimDuration = 2000;
        ta.recycle();

        mUnitAngle = (float) (mSweepAngle / 100.0);

        mBgPaint.setStyle(Paint.Style.STROKE);
        if (mStrokeCap) {
            mBgPaint.setStrokeCap(Paint.Cap.ROUND);
        }
        mBgPaint.setStrokeWidth(mProgressWidth);

        mProgressPaint.setStyle(Paint.Style.STROKE);
        if (mStrokeCap) {
            mProgressPaint.setStrokeCap(Paint.Cap.ROUND);
        }
        mProgressPaint.setStrokeWidth(mProgressWidth);
        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mMeasureWidth = getMeasuredWidth();
        mMeasureHeight = getMeasuredHeight();
        if (pRectF == null) {
            float halfProgressWidth = mProgressWidth / 2;
            pRectF = new RectF(halfProgressWidth + getPaddingLeft(),
                    halfProgressWidth + getPaddingTop(),
                    mMeasureWidth - halfProgressWidth - getPaddingRight(),
                    mMeasureHeight - halfProgressWidth - getPaddingBottom());
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mShowPattern == PATTERN_PERCENT_COVER) {
            drawBg(canvas);
            drawProgress(canvas);
            drawText(canvas);
        } else {
            drawSpliceArc(canvas);
            drawText(canvas);
        }
    }

    public void setCoverAdapter(ProgressBarCoverAdapter progressAdapter) {
        mShowPattern = PATTERN_PERCENT_COVER;
        this.mProgressAdapter = progressAdapter;
        textWithStyleList = mProgressAdapter.getTextList();
                mLineSpace = dp2px(getContext(), mProgressAdapter.getLineSpace());
        mShowDotFront = mProgressAdapter.isShowDotFront();
                mDotFrontMargin = dp2px(getContext(), mProgressAdapter.getDotFrontMargin());
        mDotFrontDiameter = dp2px(getContext(), mProgressAdapter.getDotFrontDiameter());
        mProgressPercent = mProgressAdapter.getProgressPercent();
                mStartAngle = mProgressAdapter.getStartAngle();
                mTextGravity = mProgressAdapter.getTextGravity();
                mShowDefaultAnim = mProgressAdapter.isShowDefaultAnim();
                mDefaultAllAnimDuration = mProgressAdapter.getDefaultAllAnimDuration();
        if (mShowDefaultAnim) {
            post(new Runnable() {
                @Override
                public void run() {
                    showDefaultAnimator();
                }
            });
        } else {
            mCurProgress = (int) (mProgressPercent * 100);
        }
        postInvalidate();
    }

    public void setSpliceAdapter(ProgressBarSpliceAdapter progressAdapter) {
        mShowPattern = PATTERN_PERCENT_SPLICE;
        this.mSpliceAdapter = progressAdapter;

        textWithStyleList = mSpliceAdapter.getTextList();
        mLineSpace = dp2px(getContext(), mSpliceAdapter.getLineSpace());
        mShowDotFront = mSpliceAdapter.isShowDotFront();
        mDotFrontMargin = dp2px(getContext(), mSpliceAdapter.getDotFrontMargin());
        mDotFrontDiameter = dp2px(getContext(), mSpliceAdapter.getDotFrontDiameter());
        mStartAngle = mSpliceAdapter.getStartAngle();
        mTextGravity = mSpliceAdapter.getTextGravity();

        if (textWithStyleList != null) {
            splicePaintList = new ArrayList();
            for (int i = 0; i < textWithStyleList.size(); i++) {
                Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(mProgressWidth);
                splicePaintList.add(paint);
            }
        }
        postInvalidate();
    }

    private void drawSpliceArc(Canvas canvas) {
        if (textWithStyleList == null) return;

                float preDrawAngle = mStartAngle;    // 实际的绘制起点是 90
        float startFakeAngle = 180f;          // 无论是单一颜色还是渐变颜色，伪 绘制起点都是 180

        float halfProgressSweep = mSweepAngle / 2 + startFakeAngle;

        for (int j = 0; j < textWithStyleList.size(); j ++) {

            TextWithStyle textWithStyle = textWithStyleList.get(j);

            int end = (int) (textWithStyle.getPercent() * 100f * mUnitAngle);

            Paint paint = splicePaintList.get(j);

            float trueStartAngle = preDrawAngle;
            float trueEndAngle = preDrawAngle + textWithStyle.getPercent() * 100f * mUnitAngle;
            preDrawAngle = trueEndAngle;

            //Log.e("drawSpliceArc", "j=" + j + "  trueStartAngle:" + trueStartAngle + "  trueEndAngle:" + trueEndAngle);
            for (int i = 0; i <= end; i++) {

                float fraction = 0f;

                float currentAngle = trueStartAngle + i;
                if (currentAngle <= startFakeAngle && currentAngle >= trueStartAngle) {
                    // 在 实际的绘制起点 和 伪 绘制起点之间
                    currentAngle += 360f;
                }

                if (currentAngle < halfProgressSweep) {
                    fraction = (currentAngle - startFakeAngle) / (halfProgressSweep - startFakeAngle);
                    paint.setColor(getGradient(fraction, textWithStyle.getGradientColors()[0], textWithStyle.getGradientColors()[1]));
                } else {
                    fraction = (currentAngle - halfProgressSweep) / (halfProgressSweep - startFakeAngle);
                    paint.setColor(getGradient(fraction, textWithStyle.getGradientColors()[1], textWithStyle.getGradientColors()[0]));
                }

                //Log.e("drawSpliceArc", "i=" + i + "  currentAngle=" + currentAngle + "  fraction=" + fraction);

                // 判断绘制当前是否在绘制的范围内，是则绘制

                canvas.drawArc(pRectF,
                        trueStartAngle + i,
                        1f,
                        false,
                        paint);
            }
        }
    }


    private void drawText(Canvas canvas) {
        if (textWithStyleList == null || textWithStyleList.isEmpty()) return;
        // 文字行与行的间隔
        float space = mLineSpace;

        float maxLength = 0;
        float sumTextHeight = 0;
        // 循环找出文字列表的最长的宽度
        for (int i = 0; i < textWithStyleList.size(); i++) {
            String text = textWithStyleList.get(i).getText();
            if (text == null) return;

            float textSize = sp2px(getContext(), textWithStyleList.get(i).getTextSize());
            mTextPaint.setTextSize(textSize);

            Rect textBounds = new Rect();
            mTextPaint.getTextBounds(text, 0, text.length(), textBounds);
            float textWidth = textBounds.width();
            sumTextHeight += textBounds.height();
            if (i != 0) sumTextHeight += space;
            if (textWidth > maxLength) {
                maxLength = textWidth;
            }
        }

        float dx = 0;
        if (mTextGravity == ProgressBarCoverAdapter.TEXT_GRAVITY_LEFT) {
            // 计算出最长 文字 + 圆 长度居中的绘制起点
            if (mShowDotFront) {
                dx = getWidth() / 2f - (maxLength + mDotFrontMargin + mDotFrontDiameter) / 2f;
            } else {
                dx = getWidth() / 2f - maxLength / 2f;
            }
        }
        // 计算第一个矩形顶点中间 y 坐标
        float ty = getHeight() / 2f - sumTextHeight / 2f;

        // 用于保存已经正确求值的累加高度
        float preSumHeight = 0;

        for (int i = 0; i < textWithStyleList.size(); i++) {
            String text = textWithStyleList.get(i).getText();
            if (text == null) return;
            float textSize = sp2px(getContext(), textWithStyleList.get(i).getTextSize());
            int[] gradientColors = textWithStyleList.get(i).getGradientColors();
            if (gradientColors == null) gradientColors = new int[]{0xFF4FACFE, 0xFF00F2FE};

//            mTextPaint.setColor(textColor);
            mTextPaint.setTextSize(textSize);

            Rect textBounds = new Rect();
            mTextPaint.getTextBounds(text, 0, text.length(), textBounds);
            // 基线 baseLine
            Paint.FontMetricsInt fontMetrics = mTextPaint.getFontMetricsInt();
            float dy = (fontMetrics.bottom - fontMetrics.top) / 2f - fontMetrics.bottom;

            // 对应的求各个矩形的中间 y 坐标 = ty + (前累加高度 + 自身高度/2)
            float midy = ty + preSumHeight + textBounds.height() / 2f;
            float baseLine = midy + dy;

            if (i != 0) {
                baseLine += space;
                preSumHeight += space;
            }

            if (mTextGravity != ProgressBarCoverAdapter.TEXT_GRAVITY_LEFT) {
                dx = getWidth() / 2f - textBounds.width() / 2f;
            }

            float textTruedx = (mShowDotFront && mTextGravity == ProgressBarCoverAdapter.TEXT_GRAVITY_LEFT) ?
                    (dx + mDotFrontDiameter + mDotFrontMargin) : dx;

            if (mShowDotFront && mTextGravity == ProgressBarCoverAdapter.TEXT_GRAVITY_LEFT) {
                // 画圆 float cx, float cy, float radius, @NonNull Paint paint
                float cx = dx + mDotFrontDiameter / 2;
                float cy = i == 0 ? midy : midy + space;
                float radius = mDotFrontDiameter / 2;

                mLinearGradient = new LinearGradient(cx - mDotFrontDiameter / 2,
                        cy - mDotFrontDiameter / 2,
                        cx + mDotFrontDiameter / 2,
                        cy + mDotFrontDiameter / 2,
                        gradientColors,
                        null,
                        Shader.TileMode.CLAMP);
                mTextPaint.setShader(mLinearGradient);

                canvas.drawCircle(cx, cy, radius, mTextPaint);
            }

            mLinearGradient = new LinearGradient(textTruedx,
                    preSumHeight + textBounds.height() / 2,
                    textBounds.width(),
                    preSumHeight + textBounds.height() / 2,
                    gradientColors,
                    null,
                    Shader.TileMode.CLAMP);
            mTextPaint.setShader(mLinearGradient);

            canvas.drawText(text, textTruedx,
                    baseLine, mTextPaint);

            preSumHeight += textBounds.height();

        }
    }

    public Paint getBgPaint() {
        return mBgPaint;
    }

    public Paint getProgressPaint() {
        return mProgressPaint;
    }

    public void showDefaultAnimator() {
        ValueAnimator valueAnimator = ObjectAnimator.ofFloat(0, mProgressPercent * 100);
        valueAnimator.setDuration((long) (mProgressPercent * mDefaultAllAnimDuration));
        valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float currentStep = (float) animation.getAnimatedValue();
                mCurProgress = (int) currentStep;
                //Log.e("showDefaultAnimator", "mCurProgress:" + mCurProgress);
                postInvalidate();
            }
        });
        valueAnimator.start();
    }

    // 画进度之外的背景
    private void drawBg(Canvas canvas) {
        // 圆周为 mSweepAngle 颜色渐变从起点到终点，不随进度而重设颜色开端
        float halfSweep = mSweepAngle / 2;
        for (int i = mSweepAngle, st = (int) (mCurProgress * mUnitAngle); i > st; --i) {
            if (i - halfSweep > 0) {
                mBgPaint.setColor(getGradient((i - halfSweep) / halfSweep, mBgMidColor, mBgEndColor));
            } else {
                mBgPaint.setColor(getGradient((halfSweep - i) / halfSweep, mBgMidColor, mBgStartColor));
            }
            canvas.drawArc(pRectF,
                    mStartAngle + i,
                    1,
                    false,
                    mBgPaint);
        }
    }

    private void drawProgress(Canvas canvas) {
        int end = (int) (mCurProgress * mUnitAngle);
        float halfProgressSweep = end / 2;
        for (int i = 0; i <= end; i++) {

            float fraction = 0;
            if (i < halfProgressSweep) {
                fraction = i / (float) halfProgressSweep;
                mProgressPaint.setColor(getGradient(fraction, mProgressEndColor, mProgressMidColor));
            } else {
                fraction = (i - halfProgressSweep) / (float) halfProgressSweep;
                mProgressPaint.setColor(getGradient(fraction, mProgressMidColor, mProgressEndColor));
            }
            canvas.drawArc(pRectF,
                    mStartAngle + i,
                    1,
                    false,
                    mProgressPaint);
        }
    }

    public void setStartAngle(@IntRange(from = 0, to = 360) int mStartAngle) {
        this.mStartAngle = mStartAngle;
        invalidate();
    }

    public void setCurProgress(int mCurProgress) {
        this.mCurProgress = mCurProgress;
        invalidate();
    }

    public float getProgressWidth() {
        return mProgressWidth;
    }

    public void setProgressPercent(@FloatRange(from = 0f, to = 1f) float progress) {
        this.mProgressPercent = progress;
        invalidate();
    }


    public float getProgressPercent() {
        return mProgressPercent;
    }

    /**
     * dp 转 px
     *
     * @param dpValue dp 值
     * @return px 值
     */
    private int dp2px(Context context, final float dpValue) {
        final float scale = context.getApplicationContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * sp 转 px
     *
     * @param spValue sp 值
     * @return px 值
     */
    private int sp2px(Context context, final float spValue) {
        final float fontScale = context.getApplicationContext().getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    public int getGradient(float fraction, int startColor, int endColor) {
        if (fraction > 1) fraction = 1;
        int alphaStart = Color.alpha(startColor);
        int redStart = Color.red(startColor);
        int blueStart = Color.blue(startColor);
        int greenStart = Color.green(startColor);
        int alphaEnd = Color.alpha(endColor);
        int redEnd = Color.red(endColor);
        int blueEnd = Color.blue(endColor);
        int greenEnd = Color.green(endColor);
        int alphaDifference = alphaEnd - alphaStart;
        int redDifference = redEnd - redStart;
        int blueDifference = blueEnd - blueStart;
        int greenDifference = greenEnd - greenStart;
        int alphaCurrent = (int) (alphaStart + fraction * alphaDifference);
        int redCurrent = (int) (redStart + fraction * redDifference);
        int blueCurrent = (int) (blueStart + fraction * blueDifference);
        int greenCurrent = (int) (greenStart + fraction * greenDifference);
        return Color.argb(alphaCurrent, redCurrent, greenCurrent, blueCurrent);
    }

    public int getmProgressStartColor() {
        return mProgressStartColor;
    }

    public void setmProgressStartColor(int mProgressStartColor) {
        this.mProgressStartColor = mProgressStartColor;
        postInvalidate();
    }

    public int getmProgressMidColor() {
        return mProgressMidColor;
    }

    public void setmProgressMidColor(int mProgressMidColor) {
        this.mProgressMidColor = mProgressMidColor;
        postInvalidate();
    }

    public int getmProgressEndColor() {
        return mProgressEndColor;
    }

    public void setmProgressEndColor(int mProgressEndColor) {
        this.mProgressEndColor = mProgressEndColor;
        postInvalidate();
    }

    public int getmBgStartColor() {
        return mBgStartColor;
    }

    public void setmBgStartColor(int mBgStartColor) {
        this.mBgStartColor = mBgStartColor;
        postInvalidate();
    }

    public int getmBgMidColor() {
        return mBgMidColor;
    }

    public void setmBgMidColor(int mBgMidColor) {
        this.mBgMidColor = mBgMidColor;
        postInvalidate();
    }

    public int getmBgEndColor() {
        return mBgEndColor;
    }

    public void setmBgEndColor(int mBgEndColor) {
        this.mBgEndColor = mBgEndColor;
        postInvalidate();
    }

    public float getmProgressPercent() {
        return mProgressPercent;
    }

    public void setmProgressPercent(float mProgressPercent) {
        this.mProgressPercent = mProgressPercent;
        postInvalidate();
    }

    public float getmProgressWidth() {
        return mProgressWidth;
    }

    public void setmProgressWidth(float mProgressWidth) {
        this.mProgressWidth = mProgressWidth;
        postInvalidate();
    }

    public int getmStartAngle() {
        return mStartAngle;
    }

    public void setmStartAngle(int mStartAngle) {
        this.mStartAngle = mStartAngle;
        postInvalidate();
    }

    public int getmSweepAngle() {
        return mSweepAngle;
    }

    public void setmSweepAngle(int mSweepAngle) {
        this.mSweepAngle = mSweepAngle;
        postInvalidate();
    }

    public boolean ismShowDefaultAnim() {
        return mShowDefaultAnim;
    }

    public void setmShowDefaultAnim(boolean mShowDefaultAnim) {
        this.mShowDefaultAnim = mShowDefaultAnim;
        postInvalidate();
    }

    public boolean ismStrokeCap() {
        return mStrokeCap;
    }

    public void setmStrokeCap(boolean mStrokeCap) {
        this.mStrokeCap = mStrokeCap;
        postInvalidate();
    }

    public int getmDefaultAllAnimDuration() {
        return mDefaultAllAnimDuration;
    }

    public void setmDefaultAllAnimDuration(int mDefaultAllAnimDuration) {
        this.mDefaultAllAnimDuration = mDefaultAllAnimDuration;
        postInvalidate();
    }

    public int getmShowPattern() {
        return mShowPattern;
    }

    public void setmShowPattern(int mShowPattern) {
        this.mShowPattern = mShowPattern;
        postInvalidate();
    }

    public int getmCurProgress() {
        return mCurProgress;
    }

    public void setmCurProgress(int mCurProgress) {
        this.mCurProgress = mCurProgress;
        postInvalidate();
    }
}
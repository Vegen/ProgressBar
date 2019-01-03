package com.vegen.open.library;

import android.support.annotation.FloatRange;

/**
 * @author Vegen
 * @creation_time 2018/12/6
 * @description 文字效果
 */

public class TextWithStyle {
    private String text;
    private float textSize;
    private int[] gradientColors;
    private float percent;

    public TextWithStyle(){}

    public TextWithStyle(String text, float textSize, int[] gradientColors, @FloatRange(from = 0.0, to = 1.0) Float percent) {
        this.text = text;
        this.textSize = textSize;
        this.gradientColors = gradientColors;
        this.percent = percent;
    }

    public TextWithStyle(String text, float textSize, int color, @FloatRange(from = 0.0, to = 1.0) Float percent) {
        this.text = text;
        this.textSize = textSize;
        this.gradientColors = new int[]{color, color};
        this.percent = percent;
    }

    public TextWithStyle(String text, float textSize, int[] gradientColors) {
        this.text = text;
        this.textSize = textSize;
        this.gradientColors = gradientColors;
    }

    public TextWithStyle(String text, float textSize, int color) {
        this.text = text;
        this.textSize = textSize;
        this.gradientColors = new int[]{color, color};
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public float getTextSize() {
        return textSize;
    }

    public void setTextSize(float textSize) {
        this.textSize = textSize;
    }

    public void setGradientColors(int startColor, int endColor) {
        this.gradientColors = new int[]{startColor, endColor};
    }

    public void setGradientColors(int color) {
        this.gradientColors = new int[]{color, color};
    }

    public int[] getGradientColors() {
        return gradientColors;
    }

    public float getPercent() {
        return percent;
    }

    public void setPercent(float percent) {
        this.percent = percent;
    }
}

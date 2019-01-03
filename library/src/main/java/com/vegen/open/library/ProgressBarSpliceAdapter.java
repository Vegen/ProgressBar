package com.vegen.open.library;

import java.util.List;

/**
 * @author Vegen
 * @creation_time 2019/1/3
 * @description 圆环流量进度条覆盖模式适配器
 */

public abstract class ProgressBarSpliceAdapter {
    // 文字靠齐样式
    public final static int TEXT_GRAVITY_LEFT = 0;
    // 向左靠齐居中
    public final static int  TEXT_GRAVITY_CENTER = 1;  // 居中

    // 文字列表
    public abstract List<TextWithStyle> getTextList();

    // 文字行与行的距离
    public abstract float getLineSpace();

    // 是否显示左边的圆点
    public abstract boolean isShowDotFront();

    // 文字靠齐样式
    public abstract int getTextGravity();

    // 距离文字的宽度
    protected float getDotFrontMargin(){
        return 8;
    }

    // 圆的直径
    protected float getDotFrontDiameter(){
        return 6;
    }

    // 开始的角度
    public abstract int getStartAngle();
}

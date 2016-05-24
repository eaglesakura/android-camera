package com.eaglesakura.android.camera.spec;

import com.eaglesakura.util.MathUtil;

/**
 * カメラの回転角情報
 */
public class Orientation {

    private final int degree;

    private Orientation(int rotateDegree) {
        this.degree = rotateDegree;
    }

    /**
     * 回転角を取得する
     */
    public int getDegree() {
        return degree;
    }

    /**
     * 縦向きである場合はtrue
     */
    public boolean isVertical() {
        return degree == 90 || degree == 180;
    }

    /**
     * 横向きであればtrue
     */
    public boolean isHorizontal() {
        return !isVertical();
    }

    /**
     * 回転0度
     */
    public static final Orientation ROTATE_0 = new Orientation(0);

    /**
     * 回転90度
     */
    public static final Orientation ROTATE_90 = new Orientation(90);

    /**
     * 回転180度
     */
    public static final Orientation ROTATE_180 = new Orientation(180);

    /**
     * 回転270度
     */
    public static final Orientation ROTATE_270 = new Orientation(270);

    /**
     * 回転角度から取得する
     */
    public static final Orientation fromDegree(int rotate) {
        rotate = (int) MathUtil.normalizeDegree(rotate);
        rotate = (rotate / 90) * 90;    // 90度区切りに修正する
        switch (rotate) {
            case 0:
                return ROTATE_0;
            case 90:
                return ROTATE_90;
            case 180:
                return ROTATE_180;
            case 270:
                return ROTATE_270;
            default:
                throw new IllegalStateException(String.format("Rotate error(%d)", rotate));
        }
    }
}

package com.eaglesakura.android.camera.spec;

import com.eaglesakura.android.util.ContextUtil;
import com.eaglesakura.util.StringUtil;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

/**
 * ホワイトバランス設定
 */
public class FlashMode {
    private static final Map<String, FlashMode> gFlashSpecMap;

    /**
     * 自動設定
     */
    public static final FlashMode SETTING_AUTO;
    /**
     * オフ
     */
    public static final FlashMode SETTING_OFF;
    /**
     * オン
     */
    public static final FlashMode SETTING_ON;
    /**
     * 赤目補正
     */
    public static final FlashMode SETTING_RED_EYE;
    /**
     * 常時
     */
    public static final FlashMode SETTING_TORCH;

    static {
        gFlashSpecMap = new HashMap<>();

        SETTING_AUTO = fromName("auto");
        SETTING_OFF = fromName("off");
        SETTING_ON = fromName("on");
        SETTING_RED_EYE = fromName("red-eye");
        SETTING_TORCH = fromName("torch");
    }

    /**
     * API設定名
     */
    private final String mName;

    FlashMode(String apiSettingName) {
        this.mName = apiSettingName;
    }


    public String getRawName() {
        return mName;
    }

    /**
     * 設定名を取得する
     *
     * @return 日本語での設定名
     */
    public String name(Context context) {
        String result = ContextUtil.getStringFromIdName(context, String.format("Camera.FlashMode.%s", mName.replaceAll("-", "_")));
        if (StringUtil.isEmpty(result)) {
            return mName;
        } else {
            return result;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FlashMode flashMode = (FlashMode) o;

        return mName != null ? mName.equals(flashMode.mName) : flashMode.mName == null;

    }

    @Override
    public int hashCode() {
        return mName != null ? mName.hashCode() : 0;
    }

    /**
     * フラッシュ設定モードを取得する
     */
    public static FlashMode fromName(String mode) {
        FlashMode result = gFlashSpecMap.get(mode);
        if (result == null) {
            result = new FlashMode(mode);
            gFlashSpecMap.put(mode, result);
        }
        return result;
    }

}

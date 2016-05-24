package com.eaglesakura.android.camera.spec;

import com.eaglesakura.android.util.ContextUtil;
import com.eaglesakura.util.StringUtil;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

/**
 * フォーカス状態の設定を行う
 */
public class FocusMode {

    private static final Map<String, FocusMode> gFocusModeSpecMap;

    /**
     * 自動設定
     */
    public static final FocusMode SETTING_AUTO;
    /**
     * 無限遠
     */
    public static final FocusMode SETTING_INFINITY;
    /**
     * マクロ
     */
    public static final FocusMode SETTING_MACRO;
    /**
     * 写真自動
     */
    public static final FocusMode SETTING_CONTINUOUS_PICTURE;
    /**
     * ビデオ自動
     */
    public static final FocusMode SETTING_CONTINUOUS_VIDEO;

    static {
        gFocusModeSpecMap = new HashMap<>();

        SETTING_AUTO = fromName("auto");
        SETTING_INFINITY = fromName("infinity");
        SETTING_MACRO = fromName("macro");
        SETTING_CONTINUOUS_PICTURE = fromName("continuous-picture");
        SETTING_CONTINUOUS_VIDEO = fromName("continuous-video");
    }

    /**
     * API設定名
     */
    private final String mName;

    FocusMode(String apiSettingName) {
        this.mName = apiSettingName;
    }


    /**
     * 設定名を取得する
     *
     * @return 日本語での設定名
     */
    public String name(Context context) {
        String result = ContextUtil.getStringFromIdName(context, String.format("Camera.FocusMode.%s", mName.replaceAll("-", "_")));
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

        FocusMode focusMode = (FocusMode) o;

        return mName != null ? mName.equals(focusMode.mName) : focusMode.mName == null;

    }

    @Override
    public int hashCode() {
        return mName != null ? mName.hashCode() : 0;
    }

    /**
     * フォーカス設定モードを取得する
     */
    static FocusMode fromName(String mode) {
        FocusMode result = gFocusModeSpecMap.get(mode);
        if (result == null) {
            result = new FocusMode(mode);
            gFocusModeSpecMap.put(mode, result);
        }
        return result;
    }
}

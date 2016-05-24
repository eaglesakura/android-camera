package com.eaglesakura.android.camera.spec;

import com.eaglesakura.android.util.ContextUtil;
import com.eaglesakura.util.StringUtil;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ホワイトバランス設定
 */
public final class WhiteBalance {
    private static final Map<String, WhiteBalance> gWhiteBalanceSpecMap;

    /**
     * 自動設定
     */
    public static final WhiteBalance SETTING_AUTO;
    /**
     * 白熱灯
     */
    public static final WhiteBalance SETTING_INCANDESCENT;
    /**
     * 蛍光灯
     */
    public static final WhiteBalance SETTING_FLUORESCENT;
    /**
     * 晴天
     */
    public static final WhiteBalance SETTING_DAYLIGHT;
    /**
     * 曇り
     */
    public static final WhiteBalance SETTING_CLOUDY_DAYLIGHT;

    static {
        gWhiteBalanceSpecMap = new HashMap<>();

        SETTING_AUTO = fromName("auto");
        SETTING_INCANDESCENT = fromName("incandescent");
        SETTING_FLUORESCENT = fromName("fluorescent");
        SETTING_DAYLIGHT = fromName("daylight");
        SETTING_CLOUDY_DAYLIGHT = fromName("cloudy-daylight");
    }

    /**
     * API設定名
     */
    private final String mName;

    WhiteBalance(String apiSettingName) {
        this.mName = apiSettingName;
    }

    /**
     * 設定名を取得する
     *
     * @return 日本語での設定名
     */
    public String name(Context context) {
        String result = ContextUtil.getStringFromIdName(context, String.format("Camera.WhiteBalance.%s", mName.replaceAll("-", "_")));
        if (StringUtil.isEmpty(result)) {
            return mName;
        } else {
            return result;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof WhiteBalance)) {
            return false;
        }

        return ((WhiteBalance) o).mName.equals(mName);
    }

    @Override
    public int hashCode() {
        return mName.hashCode();
    }

    /**
     * ホワイトバランス設定モードを取得する
     */
    public static WhiteBalance fromName(String mode) {
        WhiteBalance result = gWhiteBalanceSpecMap.get(mode);
        if (result == null) {
            result = new WhiteBalance(mode);
            gWhiteBalanceSpecMap.put(mode, result);
        }
        return result;
    }

    /**
     * デバイス設定から取得する
     *
     * @return ホワイトバランス設定
     */
    public static List<WhiteBalance> list(List<String> deviceSettings) {
        List<WhiteBalance> result = new ArrayList<WhiteBalance>();
        if (deviceSettings == null) {
            return result;
        }

        for (String mode : deviceSettings) {
            result.add(fromName(mode));
        }

        return result;
    }
}

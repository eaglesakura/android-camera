package com.eaglesakura.android.camera.spec;

import com.eaglesakura.android.util.ContextUtil;
import com.eaglesakura.util.StringUtil;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * シーン情報
 */
public final class Scene {
    /**
     * API設定名
     */
    private final String mName;


    private static final Map<String, Scene> gSceneSpecMap;

    /**
     * モードなし
     */
    public final static Scene SETTING_OFF;

    /**
     * 自動設定
     */
    public final static Scene SETTING_AUTO;

    /**
     * 人物撮影
     * ソフトスナップ by XperiaGX
     */
    public final static Scene SETTING_PORTRAIT;

    /**
     * 風景
     */
    public final static Scene SETTING_LANDSCAPE;

    /**
     * 夜景
     */
    public final static Scene SETTING_NIGHT;

    /**
     * 夜景人物
     * 夜景＆人物 by XperiaGX
     */
    public final static Scene SETTING_NIGHT_PORTRAIT;

    /**
     * ビーチ
     * ビーチ ＆ スノー by XperiaGX
     */
    public final static Scene SETTING_BEACH;

    /**
     * 雪景色
     * ビーチ ＆ スノー by XperiaGX
     */
    public final static Scene SETTING_SNOW;

    /**
     * スポーツ
     */
    public final static Scene SETTING_SPORTS;

    /**
     * パーティ
     */
    public final static Scene SETTING_PARTY;

    /**
     * 二値化/文字認識
     */
    public final static Scene SETTING_DOCUMENT;

    public final static Scene SETTING_SUNSET;

    public final static Scene SETTING_STEADYPHOTO;

    public final static Scene SETTING_FIREWORKS;

    public final static Scene SETTING_CANDLELIGHT;

    public final static Scene SETTING_THEATRE;

    public final static Scene SETTING_ACTION;

    static {
        gSceneSpecMap = new HashMap<>();
        SETTING_OFF = fromName("off");
        SETTING_AUTO = fromName("auto");
        SETTING_PORTRAIT = fromName("portrait");
        SETTING_LANDSCAPE = fromName("landscape");
        SETTING_NIGHT = fromName("night");
        SETTING_NIGHT_PORTRAIT = fromName("night-portrait");
        SETTING_BEACH = fromName("beach");
        SETTING_SNOW = fromName("snow");
        SETTING_SPORTS = fromName("sports");
        SETTING_PARTY = fromName("party");
        SETTING_DOCUMENT = fromName("document");
        SETTING_SUNSET = fromName("sunset");
        SETTING_STEADYPHOTO = fromName("steadyphoto");
        SETTING_FIREWORKS = fromName("fireworks");
        SETTING_CANDLELIGHT = fromName("candlelight");
        SETTING_THEATRE = fromName("theatre");
        SETTING_ACTION = fromName("action");
    }

    Scene(String apiSettingName) {
        this.mName = apiSettingName;
    }

    /**
     * 設定名を取得する
     *
     * @return 日本語での設定名
     */
    public String name(Context context) {
        String result = ContextUtil.getStringFromIdName(context, String.format("Camera.Scene.%s", mName.replaceAll("-", "_")));
        if (StringUtil.isEmpty(result)) {
            return mName;
        } else {
            return result;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Scene)) {
            return false;
        }

        return ((Scene) o).mName.equals(mName);
    }

    @Override
    public int hashCode() {
        return mName.hashCode();
    }

    public String getRawName() {
        return mName;
    }

    /**
     * シーンを取得する
     */
    public static Scene fromName(String mode) {
        Scene result = gSceneSpecMap.get(mode);
        if (result == null) {
            result = new Scene(mode);
            gSceneSpecMap.put(mode, result);
        }
        return result;
    }

    /**
     * デバイス設定から取得する
     *
     * @return シーン設定
     */
    public static List<Scene> list(List<String> deviceSettings) {
        List<Scene> result = new ArrayList<Scene>();
        if (deviceSettings == null) {
            return result;
        }

        for (String mode : deviceSettings) {
            result.add(fromName(mode));
        }

        return result;
    }
}

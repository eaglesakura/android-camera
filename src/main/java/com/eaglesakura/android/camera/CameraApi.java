package com.eaglesakura.android.camera;

import android.os.Build;

import java.util.Arrays;
import java.util.List;

public enum CameraApi {
    /**
     * Android 4.4以下の古いAPI
     */
    Legacy,

    /**
     * Camera2 API
     */
    Camera2,

    /**
     * 自動で取得する
     */
    Default;

    public static List<CameraApi> listSupoorted() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            return Arrays.asList(Legacy, Camera2);
        } else {
            return Arrays.asList(Legacy);
        }
    }
}

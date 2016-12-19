package com.eaglesakura.android.camera.spec;

public enum CameraType {
    /**
     * 標準のフロントカメラ
     */
    Front,

    /**
     * 標準のバックカメラ
     */
    Back,

    /**
     * その他接続されているカメラ
     *
     * Camera2のみAPIサポートされているため、正常に選択可能。
     */
    External,

    /**
     * バック => Front => その他のカメラを順に利用する
     */
    Auto,
}

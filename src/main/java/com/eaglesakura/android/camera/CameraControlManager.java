package com.eaglesakura.android.camera;

import com.eaglesakura.android.camera.error.CameraException;
import com.eaglesakura.android.camera.preview.CameraSurface;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * 同期的なカメラ制御を提供する
 *
 * 内部はAndroid 5.0以上であればCamera2 APIを、それ以外であればCamera1 APIを使用する
 *
 * 互換性を保つため、撮影は必ずconnect > preview > takePicture の順で行わなければならない。
 * - 1. connect
 * - 2. startPreview
 * - 3. takePicture
 * - 4. stopPreview
 * - 5. disconnect
 */
public abstract class CameraControlManager {
    protected final Context mContext;

    protected final CameraConnectRequest mConnectRequest;

    public CameraControlManager(Context context, CameraConnectRequest request) {
        mContext = context.getApplicationContext();
        mConnectRequest = request;
    }

    /**
     * 撮影用の設定を指定して接続する
     *
     * @param previewSurface プレビュー用のサーフェイス
     * @param previewRequest プレビュー設定
     * @param shotRequest    撮影設定
     */
    public abstract boolean connect(@Nullable CameraSurface previewSurface, @Nullable CameraPreviewRequest previewRequest, @Nullable CameraPictureShotRequest shotRequest) throws CameraException;

    /**
     * プレビュー中であればtrue
     */
    public abstract boolean isPreviewNow();

    public abstract boolean isConnected();

    public abstract void disconnect();

    /**
     * カメラプレビューを開始する
     */
    public abstract void startPreview(@Nullable CameraEnvironmentRequest env) throws CameraException;

    /**
     * カメラプレビューを停止する
     *
     * MEMO: プレビューの停止はサーフェイスと同期して削除しなければならないため、実装的にはUIスレッド・バックグラウンドスレッドどちらでも動作できる。
     */
    public abstract void stopPreview() throws CameraException;

    /**
     * 写真撮影を行わせる
     */
    @NonNull
    public abstract PictureData takePicture(@Nullable CameraEnvironmentRequest env) throws CameraException;

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
        Default,
    }

    /**
     * カメラ制御クラスを生成する
     */
    public static CameraControlManager newInstance(Context context, CameraApi api, CameraConnectRequest request) throws CameraException {
        if (api.equals(CameraApi.Default)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                api = CameraApi.Camera2;
            } else {
                api = CameraApi.Legacy;
            }
        }

        if (api == CameraApi.Camera2) {
            // Camera2
            return new Camera2ManagerImpl(context, request);
        } else {
            // Camera1
            return new CameraLegacyManagerImpl(context, request);
        }
    }

    /**
     * デフォルトのAPIでカメラ制御クラスを生成する
     */
    public static CameraControlManager newInstance(Context context, CameraConnectRequest request) throws CameraException {
        return newInstance(context, CameraApi.Default, request);
    }
}

package com.eaglesakura.android.camera;

import com.eaglesakura.android.camera.error.CameraException;
import com.eaglesakura.android.camera.preview.CameraSurface;
import com.eaglesakura.android.thread.async.AsyncHandler;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Surface;

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

    protected final CameraConnectRequest mRequest;

    public CameraControlManager(Context context, CameraConnectRequest request) {
        mContext = context.getApplicationContext();
        mRequest = request;
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

    public static CameraControlManager newInstance(Context context, CameraConnectRequest request) throws CameraException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Camera2
            return new Camera2ManagerImpl(context, request);
        } else {
            // Camera1
            throw new IllegalStateException();
        }
    }
}

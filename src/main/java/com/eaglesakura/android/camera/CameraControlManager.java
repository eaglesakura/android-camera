package com.eaglesakura.android.camera;

import com.eaglesakura.android.camera.error.CameraException;
import com.eaglesakura.android.thread.async.AsyncHandler;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Surface;

public abstract class CameraControlManager {
    protected final Context mContext;

    protected final CameraConnectRequest mRequest;

    public CameraControlManager(Context context, CameraConnectRequest request) {
        mContext = context.getApplicationContext();
        mRequest = request;
    }

    /**
     * プレビュー用のSurfaceを取得する
     */
    public abstract Surface getPreviewSurface();

    public abstract boolean connect() throws CameraException;

    public abstract boolean isConnected();

    public abstract void disconnect();

    /**
     * 環境設定をリクエストする
     */
    public abstract void request(CameraEnvironmentRequest env) throws CameraException;

    /**
     * カメラプレビューを開始する
     *
     * MEMO: プレビューの開始はサーフェイスと同期しなければならないため、実装的にはUIスレッド・バックグラウンドスレッドどちらでも動作できる。
     */
    public abstract void startPreview(@NonNull Surface surface, @NonNull CameraPreviewRequest preview, @Nullable CameraEnvironmentRequest env) throws CameraException;

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
    public abstract PictureData takePicture(@NonNull CameraPictureShotRequest request, @Nullable CameraEnvironmentRequest env) throws CameraException;

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

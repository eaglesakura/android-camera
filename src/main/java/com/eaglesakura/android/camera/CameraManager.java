package com.eaglesakura.android.camera;

import com.eaglesakura.android.camera.error.CameraException;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Surface;

public abstract class CameraManager {
    protected final Context mContext;

    protected final CameraConnectRequest mRequest;

    public CameraManager(Context context, CameraConnectRequest request) {
        mContext = context.getApplicationContext();
        mRequest = request;
    }

    public abstract boolean connect() throws CameraException;

    public abstract boolean isConnected();

    public abstract void disconnect();

    /**
     * カメラプレビューを開始する
     */
    public abstract void startPreview(@NonNull Surface surface, @NonNull CameraPreviewRequest preview, @Nullable CameraEnvironmentRequest env) throws CameraException;

    /**
     * カメラプレビューを停止する
     */
    public abstract void stopPreview() throws CameraException;
}

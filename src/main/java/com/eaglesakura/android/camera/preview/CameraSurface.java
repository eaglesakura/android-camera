package com.eaglesakura.android.camera.preview;

import com.eaglesakura.android.camera.spec.CaptureSize;

import android.graphics.SurfaceTexture;
import android.os.Build;
import android.support.annotation.NonNull;
import android.view.Surface;

public class CameraSurface {
    private Surface mNativeSurface;

    private SurfaceTexture mSurfaceTexture;

    public CameraSurface(SurfaceTexture surfaceTexture) {
        mSurfaceTexture = surfaceTexture;
    }

    public CameraSurface(Surface nativeSurface) {
        mNativeSurface = nativeSurface;
    }

    @NonNull
    public Surface getNativeSurface(CaptureSize previewSize) {
        if (mSurfaceTexture != null) {
            if (Build.VERSION.SDK_INT >= 15) {
                mSurfaceTexture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
            }

            if (mNativeSurface == null) {
                mNativeSurface = new Surface(mSurfaceTexture);
            }
        }

        if (mNativeSurface == null) {
            throw new NullPointerException("mNativeSurface == null");
        }

        return mNativeSurface;
    }


    @NonNull
    public SurfaceTexture getSurfaceTexture(CaptureSize previewSize) {
        if (mSurfaceTexture != null) {
            if (Build.VERSION.SDK_INT >= 15) {
                mSurfaceTexture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
            }
        }
        return mSurfaceTexture;
    }
}

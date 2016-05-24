package com.eaglesakura.android.camera.preview;

import com.eaglesakura.android.glkit.egl.EGLSpecRequest;
import com.eaglesakura.android.glkit.egl.GLESVersion;
import com.eaglesakura.android.glkit.egl.IEGLDevice;
import com.eaglesakura.android.glkit.egl11.EGL11Manager;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.os.Build;

import static android.opengl.GLES20.glDeleteTextures;
import static android.opengl.GLES20.glGenTextures;

/**
 *
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class OffscreenPreviewSurface {

    final Context mContext;

    EGL11Manager mEglManager;

    IEGLDevice mEglDevice;

    SurfaceTexture mSurface;

    int mPreviewTexture;

    public OffscreenPreviewSurface(Context context) {
        this.mContext = context;
    }

    private SurfaceTexture createSurfaceTexture() {
        mEglManager = new EGL11Manager(mContext);

        // EGL初期化する
        EGLSpecRequest eglSpecRequest = new EGLSpecRequest();
        eglSpecRequest.version = GLESVersion.GLES20;
        mEglManager.initialize(eglSpecRequest);
        mEglDevice = mEglManager.newDevice(null);
        mEglDevice.createPBufferSurface(1, 1);
        if (!mEglDevice.bind()) {
            throw new IllegalStateException("EGL createSurface failed");
        }

        this.mPreviewTexture = genPreviewTexture();
        mSurface = new SurfaceTexture(mPreviewTexture);
        return mSurface;
    }

    /**
     * 初期化処理を行う
     */
    public SurfaceTexture createSurface() {
        if (mSurface != null) {
            return mSurface;
        }
        return createSurfaceTexture();
    }

    private static int genPreviewTexture() {
        int[] temp = new int[1];
        glGenTextures(1, temp, 0);
        int texture = temp[0];
        return texture;
    }

    /**
     * 開放処理を行う
     */
    public void dispose() {
        if (mSurface != null) {
            mSurface.release();
            mSurface = null;
        }

        if (mPreviewTexture != 0) {
            glDeleteTextures(1, new int[]{mPreviewTexture}, 0);
            mPreviewTexture = 0;
        }

        if (mEglDevice != null) {
            mEglDevice.unbind();
            mEglDevice.dispose();
            mEglDevice = null;
        }
    }
}

package com.eaglesakura.android.camera;

import com.eaglesakura.android.camera.spec.CaptureSize;

public final class CameraPreviewRequest {
    CaptureSize mPreviewSize;

    public CameraPreviewRequest() {
    }

    public CameraPreviewRequest size(CaptureSize size) {
        mPreviewSize = size;
        return this;
    }

    public CaptureSize getPreviewSize() {
        return mPreviewSize;
    }
}

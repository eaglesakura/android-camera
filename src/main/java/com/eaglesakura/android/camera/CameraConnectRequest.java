package com.eaglesakura.android.camera;

import com.eaglesakura.android.camera.spec.CameraType;

import android.support.annotation.NonNull;

public final class CameraConnectRequest {
    @NonNull
    final CameraType mCameraType;

    public CameraConnectRequest(@NonNull CameraType cameraType) {
        mCameraType = cameraType;
    }

    @NonNull
    public CameraType getCameraType() {
        return mCameraType;
    }
}

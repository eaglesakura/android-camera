package com.eaglesakura.android.camera;

import com.eaglesakura.android.camera.spec.CaptureFormat;
import com.eaglesakura.android.camera.spec.FlashMode;

import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public final class CameraPictureShotRequest {

    double mLatitude;

    double mLongitude;

    @NonNull
    CaptureFormat mCaptureFormat = CaptureFormat.Jpeg;

    @Nullable
    FlashMode mFlashMode;

    public CameraPictureShotRequest() {
    }

    public CameraPictureShotRequest location(double lat, double lng) {
        mLatitude = lat;
        mLongitude = lat;
        return this;
    }

    @FloatRange(from = -90, to = 90)
    public double getLatitude() {
        return mLatitude;
    }

    @FloatRange(from = -180, to = 180)
    public double getLongitude() {
        return mLongitude;
    }

    public boolean hasLocation() {
        return mLatitude != 0 && mLongitude != 0;
    }

    public CameraPictureShotRequest flash(FlashMode flashMode) {
        mFlashMode = flashMode;
        return this;
    }

    public CameraPictureShotRequest captureFormat(CaptureFormat fmt) {
        mCaptureFormat = fmt;
        return this;
    }

    @NonNull
    public CaptureFormat getCaptureFormat() {
        return mCaptureFormat;
    }

}

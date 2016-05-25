package com.eaglesakura.android.camera;

import com.eaglesakura.android.camera.spec.CaptureFormat;
import com.eaglesakura.android.camera.spec.CaptureSize;
import com.eaglesakura.android.camera.spec.FlashMode;

import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public final class CameraPictureShotRequest {

    private double mLatitude;

    private double mLongitude;

    @NonNull
    private CaptureFormat mCaptureFormat = CaptureFormat.Jpeg;

    private final CaptureSize mCaptureSize;

    public CameraPictureShotRequest(@NonNull CaptureSize size) {
        mCaptureSize = size;
    }

    public CameraPictureShotRequest location(double lat, double lng) {
        mLatitude = lat;
        mLongitude = lng;
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


    public CameraPictureShotRequest captureFormat(CaptureFormat fmt) {
        mCaptureFormat = fmt;
        return this;
    }

    public CaptureSize getCaptureSize() {
        return mCaptureSize;
    }

    @NonNull
    public CaptureFormat getCaptureFormat() {
        return mCaptureFormat;
    }

}

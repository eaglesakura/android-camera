package com.eaglesakura.android.camera;

import com.eaglesakura.android.camera.spec.CameraType;
import com.eaglesakura.android.camera.spec.CaptureFormat;
import com.eaglesakura.android.camera.spec.FlashMode;
import com.eaglesakura.android.camera.spec.FocusMode;
import com.eaglesakura.android.camera.spec.Orientation;
import com.eaglesakura.android.camera.spec.Scene;
import com.eaglesakura.android.camera.spec.WhiteBalance;

import android.location.Location;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class CameraRequest {
    @Nullable
    FlashMode mFlashMode;

    @Nullable
    FocusMode mFocusMode;

    @NonNull
    CaptureFormat mCaptureFormat = CaptureFormat.Jpeg;

    @Nullable
    Scene mScene;

    @Nullable
    WhiteBalance mWhiteBalance;

    @NonNull
    final CameraType mCameraType;

    double mLatitude;

    double mLongitude;

    public CameraRequest(@NonNull CameraType cameraType) {
        mCameraType = cameraType;
    }

    public CameraRequest flash(FlashMode flashMode) {
        mFlashMode = flashMode;
        return this;
    }

    public CameraRequest focus(FocusMode focusMode) {
        mFocusMode = focusMode;
        return this;
    }

    public CameraRequest captureFormat(CaptureFormat fmt) {
        mCaptureFormat = fmt;
        return this;
    }

    public CameraRequest scene(Scene scene) {
        mScene = scene;
        return this;
    }

    public CameraRequest whiteBalance(WhiteBalance whiteBalance) {
        mWhiteBalance = whiteBalance;
        return this;
    }

    public CameraRequest location(double lat, double lng) {
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

    @Nullable
    public FlashMode getFlashMode() {
        return mFlashMode;
    }

    @Nullable
    public FocusMode getFocusMode() {
        return mFocusMode;
    }

    @NonNull
    public CaptureFormat getCaptureFormat() {
        return mCaptureFormat;
    }

    @Nullable
    public Scene getScene() {
        return mScene;
    }

    @Nullable
    public WhiteBalance getWhiteBalance() {
        return mWhiteBalance;
    }

    @NonNull
    public CameraType getCameraType() {
        return mCameraType;
    }
}

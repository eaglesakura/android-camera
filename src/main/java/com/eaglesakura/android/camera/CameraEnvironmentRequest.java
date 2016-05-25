package com.eaglesakura.android.camera;

import com.eaglesakura.android.camera.spec.FlashMode;
import com.eaglesakura.android.camera.spec.FocusMode;
import com.eaglesakura.android.camera.spec.Scene;
import com.eaglesakura.android.camera.spec.WhiteBalance;

import android.support.annotation.Nullable;

public final class CameraEnvironmentRequest {

    @Nullable
    FocusMode mFocusMode;

    @Nullable
    Scene mScene;

    @Nullable
    WhiteBalance mWhiteBalance;

    @Nullable
    FlashMode mFlashMode;

    public CameraEnvironmentRequest() {
    }

    public CameraEnvironmentRequest focus(FocusMode focusMode) {
        mFocusMode = focusMode;
        return this;
    }

    public CameraEnvironmentRequest scene(Scene scene) {
        mScene = scene;
        return this;
    }

    public CameraEnvironmentRequest whiteBalance(WhiteBalance whiteBalance) {
        mWhiteBalance = whiteBalance;
        return this;
    }

    @Nullable
    public FlashMode getFlashMode() {
        return mFlashMode;
    }

    @Nullable
    public FocusMode getFocusMode() {
        return mFocusMode;
    }

    @Nullable
    public Scene getScene() {
        return mScene;
    }

    @Nullable
    public WhiteBalance getWhiteBalance() {
        return mWhiteBalance;
    }
}

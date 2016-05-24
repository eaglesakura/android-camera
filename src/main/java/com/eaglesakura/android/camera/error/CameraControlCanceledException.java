package com.eaglesakura.android.camera.error;

public class CameraControlCanceledException extends CameraException {
    public CameraControlCanceledException() {
    }

    public CameraControlCanceledException(String detailMessage) {
        super(detailMessage);
    }

    public CameraControlCanceledException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public CameraControlCanceledException(Throwable throwable) {
        super(throwable);
    }
}

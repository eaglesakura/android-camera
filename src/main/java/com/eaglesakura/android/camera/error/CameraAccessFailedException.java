package com.eaglesakura.android.camera.error;

public class CameraAccessFailedException extends CameraException {
    public CameraAccessFailedException() {
    }

    public CameraAccessFailedException(String detailMessage) {
        super(detailMessage);
    }

    public CameraAccessFailedException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public CameraAccessFailedException(Throwable throwable) {
        super(throwable);
    }
}

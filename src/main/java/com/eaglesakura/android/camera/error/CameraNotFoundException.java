package com.eaglesakura.android.camera.error;

public class CameraNotFoundException extends CameraException {
    public CameraNotFoundException() {
    }

    public CameraNotFoundException(String detailMessage) {
        super(detailMessage);
    }

    public CameraNotFoundException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public CameraNotFoundException(Throwable throwable) {
        super(throwable);
    }
}

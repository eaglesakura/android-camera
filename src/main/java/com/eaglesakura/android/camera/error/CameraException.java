package com.eaglesakura.android.camera.error;

public class CameraException extends Exception {
    public CameraException() {
    }

    public CameraException(String detailMessage) {
        super(detailMessage);
    }

    public CameraException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public CameraException(Throwable throwable) {
        super(throwable);
    }
}

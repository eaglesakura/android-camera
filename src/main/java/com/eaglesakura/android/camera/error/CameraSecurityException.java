package com.eaglesakura.android.camera.error;

public class CameraSecurityException extends CameraException {
    public CameraSecurityException() {
    }

    public CameraSecurityException(String detailMessage) {
        super(detailMessage);
    }

    public CameraSecurityException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public CameraSecurityException(Throwable throwable) {
        super(throwable);
    }
}

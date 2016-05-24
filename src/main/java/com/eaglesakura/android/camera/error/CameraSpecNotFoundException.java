package com.eaglesakura.android.camera.error;

public class CameraSpecNotFoundException extends CameraException {
    public CameraSpecNotFoundException() {
    }

    public CameraSpecNotFoundException(String detailMessage) {
        super(detailMessage);
    }

    public CameraSpecNotFoundException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public CameraSpecNotFoundException(Throwable throwable) {
        super(throwable);
    }
}

package com.eaglesakura.android.camera.error;

public class PictureFailedException extends CameraException {
    public PictureFailedException() {
    }

    public PictureFailedException(String detailMessage) {
        super(detailMessage);
    }

    public PictureFailedException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public PictureFailedException(Throwable throwable) {
        super(throwable);
    }
}

package com.eaglesakura.android.camera;

public class PictureData {

    /**
     * 画像幅
     */
    public final int width;

    /**
     * 画像高さ
     */
    public final int height;

    /**
     * 撮影されたJPEGやRAWバッファ
     */
    public final byte[] buffer;

    public PictureData(int width, int height, byte[] buffer) {
        this.width = width;
        this.height = height;
        this.buffer = buffer;
    }
}

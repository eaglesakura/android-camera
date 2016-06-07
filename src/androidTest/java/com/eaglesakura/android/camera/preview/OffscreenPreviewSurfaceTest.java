package com.eaglesakura.android.camera.preview;

import com.eaglesakura.android.camera.error.CameraException;
import com.eaglesakura.android.camera.spec.CaptureSize;
import com.eaglesakura.android.devicetest.DeviceTestCase;

import org.junit.Test;

import android.graphics.SurfaceTexture;

import static org.junit.Assert.*;

public class OffscreenPreviewSurfaceTest extends DeviceTestCase {

    @Test
    public void カメラ用オフスクリーンサーフェイスが生成できる() throws CameraException {
        OffscreenPreviewSurface surface = new OffscreenPreviewSurface(getContext(), new CaptureSize(1, 1));
        SurfaceTexture texture = surface.createSurface();
        assertNotNull(texture);

        surface.dispose();
    }
}

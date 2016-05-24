package com.eaglesakura.android.camera;

import com.eaglesakura.android.camera.preview.OffscreenPreviewSurface;
import com.eaglesakura.android.camera.spec.CameraType;
import com.eaglesakura.android.devicetest.DeviceTestCase;

import org.junit.Test;

import android.graphics.SurfaceTexture;
import android.view.Surface;

public class Camera2ManagerImplTest extends DeviceTestCase {

    @Test
    public void カメラプレビューを行う() throws Throwable {
        CameraRequest request = new CameraRequest(CameraType.Back);

        CameraManager cameraManager = new Camera2ManagerImpl(getContext(), request);

        cameraManager.connect();

        OffscreenPreviewSurface previewSurface = new OffscreenPreviewSurface(getContext());
        try {
            SurfaceTexture surface = previewSurface.createSurface();
            cameraManager.startPreview(new Surface(surface), null);
        } finally {
            cameraManager.disconnect();
        }
    }
}

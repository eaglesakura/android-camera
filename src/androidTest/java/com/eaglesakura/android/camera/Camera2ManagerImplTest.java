package com.eaglesakura.android.camera;

import com.eaglesakura.android.camera.preview.OffscreenPreviewSurface;
import com.eaglesakura.android.camera.spec.CameraType;
import com.eaglesakura.android.camera.spec.CaptureFormat;
import com.eaglesakura.android.camera.spec.WhiteBalance;
import com.eaglesakura.android.devicetest.DeviceTestCase;

import org.junit.Test;

import android.graphics.SurfaceTexture;
import android.view.Surface;

import static org.junit.Assert.*;

public class Camera2ManagerImplTest extends DeviceTestCase {

    @Test
    public void カメラプレビューを行う() throws Throwable {
        CameraSpec spec = CameraSpec.getSpecs(getContext(), CameraType.Back);
        CameraConnectRequest connectRequest = new CameraConnectRequest(spec.getType());
        CameraEnvironmentRequest envRequest = new CameraEnvironmentRequest().whiteBalance(WhiteBalance.SETTING_AUTO);
        CameraPreviewRequest previewRequest = new CameraPreviewRequest().size(spec.getPreviewSize(640, 480));

        CameraManager cameraManager = new Camera2ManagerImpl(getContext(), connectRequest);

        cameraManager.connect();

        OffscreenPreviewSurface previewSurface = new OffscreenPreviewSurface(getContext());
        try {
            SurfaceTexture surface = previewSurface.createSurface();
            cameraManager.startPreview(new Surface(surface), previewRequest, envRequest);

            assertTrue(cameraManager.isConnected());

        } finally {
            cameraManager.stopPreview();
            cameraManager.disconnect();

            assertFalse(cameraManager.isConnected());
        }
    }
}

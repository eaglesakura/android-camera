package com.eaglesakura.android.camera;

import com.eaglesakura.android.camera.log.CameraLog;
import com.eaglesakura.android.camera.preview.CameraSurface;
import com.eaglesakura.android.camera.preview.OffscreenPreviewSurface;
import com.eaglesakura.android.camera.spec.CameraType;
import com.eaglesakura.android.camera.spec.FlashMode;
import com.eaglesakura.android.camera.spec.WhiteBalance;
import com.eaglesakura.android.devicetest.DeviceTestCase;
import com.eaglesakura.android.util.ImageUtil;
import com.eaglesakura.util.Util;

import org.junit.Test;

import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;

import java.io.File;
import java.io.FileOutputStream;

public class CameraConnectManagerTest extends DeviceTestCase {

    @Test
    public void カメラプレビューを行う() throws Throwable {
        CameraSpec spec = CameraSpec.getSpecs(getContext(), CameraType.Back);
        CameraConnectRequest connectRequest = new CameraConnectRequest(spec.getType());
        CameraEnvironmentRequest envRequest = new CameraEnvironmentRequest().whiteBalance(spec, WhiteBalance.SETTING_AUTO).flash(spec, FlashMode.SETTING_OFF);
        CameraPreviewRequest previewRequest = new CameraPreviewRequest().size(spec.getPreviewSize(640, 480));

        CameraControlManager cameraManager = CameraControlManager.newInstance(getContext(), connectRequest);
        OffscreenPreviewSurface previewSurface = new OffscreenPreviewSurface(getContext(), spec.getMinimumPreviewSize());


        try {
            SurfaceTexture surface = previewSurface.createSurface();
            cameraManager.connect(new CameraSurface(surface), previewRequest, null);
            cameraManager.startPreview(envRequest);

            assertTrue(cameraManager.isConnected());

            Util.sleep(500);
        } finally {
            cameraManager.disconnect();
            previewSurface.dispose();

            assertFalse(cameraManager.isConnected());
        }
    }

    @Test
    public void 撮影を行う() throws Throwable {
        CameraSpec spec = CameraSpec.getSpecs(getContext(), CameraType.Front);

        CameraPreviewRequest previewRequest = new CameraPreviewRequest().size(spec.getPreviewSize(640, 480));
        CameraConnectRequest connectRequest = new CameraConnectRequest(spec.getType());
        CameraEnvironmentRequest envRequest = new CameraEnvironmentRequest().flash(FlashMode.SETTING_OFF);
        CameraPictureShotRequest shotRequest =
                new CameraPictureShotRequest(spec.getFullJpegPictureSize())
                        .location(35.658598, 139.743271);

        CameraControlManager cameraManager = CameraControlManager.newInstance(getContext(), connectRequest);
        OffscreenPreviewSurface previewSurface = new OffscreenPreviewSurface(getContext(), spec.getMinimumPreviewSize());

        try {
            SurfaceTexture surface = previewSurface.createSurface();

            cameraManager.connect(new CameraSurface(surface), previewRequest, shotRequest);
            cameraManager.startPreview(envRequest);

            assertTrue(cameraManager.isConnected());

            PictureData picture = cameraManager.takePicture(envRequest);

            assertNotNull(picture);
            assertEquals(picture.width, shotRequest.getCaptureSize().getWidth());
            assertEquals(picture.height, shotRequest.getCaptureSize().getHeight());

            File outFile = new File(getCacheDirectory(), "shot/testshot.jpg");
            outFile.getParentFile().mkdirs();
            FileOutputStream os = new FileOutputStream(outFile);
            os.write(picture.buffer);
            os.flush();
            os.close();

            CameraLog.hardware("out path[%s]", outFile.getAbsolutePath());
            {
                // デコードさせる
                Bitmap decode = ImageUtil.decode(picture.buffer);
                assertNotNull(decode);
                assertEquals(decode.getWidth() * decode.getHeight(), shotRequest.getCaptureSize().getHeight() * shotRequest.getCaptureSize().getWidth());
            }

        } finally {
            cameraManager.disconnect();
            previewSurface.dispose();

            assertFalse(cameraManager.isConnected());
        }
    }
}

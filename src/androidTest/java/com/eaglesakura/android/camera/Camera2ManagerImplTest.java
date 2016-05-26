package com.eaglesakura.android.camera;

import com.eaglesakura.android.camera.log.CameraLog;
import com.eaglesakura.android.camera.preview.OffscreenPreviewSurface;
import com.eaglesakura.android.camera.spec.CameraType;
import com.eaglesakura.android.camera.spec.FlashMode;
import com.eaglesakura.android.camera.spec.WhiteBalance;
import com.eaglesakura.android.device.external.StorageInfo;
import com.eaglesakura.android.devicetest.DeviceTestCase;
import com.eaglesakura.android.util.ImageUtil;
import com.eaglesakura.util.Util;

import org.junit.Test;

import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.view.Surface;

import java.io.File;
import java.io.FileOutputStream;

import static org.junit.Assert.*;

public class Camera2ManagerImplTest extends DeviceTestCase {

    @Test
    public void カメラプレビューを行う() throws Throwable {
        CameraSpec spec = CameraSpec.getSpecs(getContext(), CameraType.Back);
        CameraConnectRequest connectRequest = new CameraConnectRequest(spec.getType());
        CameraEnvironmentRequest envRequest = new CameraEnvironmentRequest().whiteBalance(WhiteBalance.SETTING_AUTO).flash(FlashMode.SETTING_OFF);
        CameraPreviewRequest previewRequest = new CameraPreviewRequest().size(spec.getPreviewSize(640, 480));

        CameraControlManager cameraManager = new Camera2ManagerImpl(getContext(), connectRequest);

        cameraManager.connect();

        OffscreenPreviewSurface previewSurface = new OffscreenPreviewSurface(getContext());
        try {
            SurfaceTexture surface = previewSurface.createSurface();
            cameraManager.startPreview(new Surface(surface), previewRequest, envRequest);

            assertTrue(cameraManager.isConnected());

            Util.sleep(500);

            // ホワイトバランスを更新させる
            envRequest.whiteBalance(WhiteBalance.SETTING_DAYLIGHT);
            cameraManager.request(envRequest);

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

        CameraControlManager cameraManager = new Camera2ManagerImpl(getContext(), connectRequest);

        cameraManager.connect();

        OffscreenPreviewSurface previewSurface = new OffscreenPreviewSurface(getContext());
        try {
            SurfaceTexture surface = previewSurface.createSurface();
            cameraManager.startPreview(new Surface(surface), previewRequest, envRequest);

            assertTrue(cameraManager.isConnected());

            PictureData picture = cameraManager.takePicture(shotRequest, null);

            assertNotNull(picture);
            assertEquals(picture.width, shotRequest.getCaptureSize().getWidth());
            assertEquals(picture.height, shotRequest.getCaptureSize().getHeight());

            File outFile = new File(StorageInfo.getExternalStorageRoot(getContext()), "junit/testshot.jpg");
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

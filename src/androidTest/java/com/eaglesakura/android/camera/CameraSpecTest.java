package com.eaglesakura.android.camera;

import com.eaglesakura.android.camera.log.CameraLog;
import com.eaglesakura.android.camera.spec.CameraType;
import com.eaglesakura.android.camera.spec.CaptureSize;
import com.eaglesakura.android.devicetest.DeviceTestCase;

import org.junit.Test;

public class CameraSpecTest extends DeviceTestCase {

    @Test
    public void カメラスペックを取得する() throws Throwable {
        for (CameraType type : CameraType.values()) {
            CameraSpec specs = CameraSpec.getSpecs(getContext(), type);
            assertNotNull(specs);

            CameraLog.hardware("Type[%s]", specs.getType().name());

            assertNotNull(specs.getFlashModeSpecs());
            assertNotNull(specs.getFocusModeSpecs());
            assertNotNull(specs.getJpegPictureSizes());
            assertNotNull(specs.getRawPictureSizes());
            assertNotNull(specs.getPreviewSizes());
            assertNotNull(specs.getWhiteBalanceSpecs());

            CaptureSize size = specs.getPreviewSize(796, 597);
            CameraLog.hardware("PreviewSize %dx%d", size.getWidth(), size.getHeight());

            validate(size.getWidth()).to(2000);
            validate(size.getHeight()).to(2000);

            {
                CaptureSize minimumPreviewSize = specs.getMinimumPreviewSize();
                assertTrue(minimumPreviewSize.getWidth() <= size.getWidth());
                assertTrue(minimumPreviewSize.getHeight() <= size.getHeight());
            }
        }
    }

}

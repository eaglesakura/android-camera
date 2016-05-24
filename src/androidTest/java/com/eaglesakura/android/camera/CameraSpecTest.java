package com.eaglesakura.android.camera;

import com.eaglesakura.android.camera.log.CameraLog;
import com.eaglesakura.android.camera.spec.CameraType;
import com.eaglesakura.android.devicetest.DeviceTestCase;

import org.junit.Test;

import static org.junit.Assert.*;

public class CameraSpecTest extends DeviceTestCase {

    @Test
    public void カメラスペックを取得する() throws Throwable {
        for (CameraType type : CameraType.values()) {
            CameraSpec specs = CameraSpec.getSpecs(getContext(), type);
            assertNotNull(specs);

            CameraLog.hardware("Type[%s]", specs.getType().name());

            assertNotNull(specs.getFlashModeSpecs());
            assertNotNull(specs.getFocusModeSpecs());
            assertNotNull(specs.getJpegPictureSize());
            assertNotNull(specs.getRawPictureSize());
            assertNotNull(specs.getPreviewSizes());
            assertNotNull(specs.getWhiteBalanceSpecs());
        }
    }
}

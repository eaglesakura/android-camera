package com.eaglesakura.android.camera;

import com.eaglesakura.android.camera.log.CameraLog;
import com.eaglesakura.android.camera.spec.CameraType;
import com.eaglesakura.android.camera.spec.CaptureSize;
import com.eaglesakura.android.devicetest.DeviceTestCase;
import com.eaglesakura.util.StringUtil;

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
            assertNotNull(specs.getJpegPictureSizes());
            assertNotNull(specs.getRawPictureSizes());
            assertNotNull(specs.getPreviewSizes());
            assertNotNull(specs.getWhiteBalanceSpecs());

            CaptureSize size = specs.getPreviewSize(796, 597);
            CameraLog.hardware("PreviewSize %dx%d", size.getWidth(), size.getHeight());
            assertThat(StringUtil.format("%d < 1280", size.getWidth()), size.getWidth() < 1280, isTrue());
            assertThat(StringUtil.format("%d < 1280", size.getHeight()), size.getHeight() < 1280, isTrue());
        }
    }

}

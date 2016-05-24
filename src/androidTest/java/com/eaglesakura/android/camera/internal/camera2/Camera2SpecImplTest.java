package com.eaglesakura.android.camera.internal.camera2;

import com.eaglesakura.android.camera.log.CameraLog;
import com.eaglesakura.android.camera.spec.CameraType;
import com.eaglesakura.android.camera.spec.CaptureSize;
import com.eaglesakura.android.camera.spec.FlashMode;
import com.eaglesakura.android.camera.spec.FocusMode;
import com.eaglesakura.android.camera.spec.CaptureFormat;
import com.eaglesakura.android.camera.spec.Scene;
import com.eaglesakura.android.camera.spec.WhiteBalance;
import com.eaglesakura.android.devicetest.DeviceTestCase;
import com.eaglesakura.util.StringUtil;

import org.junit.Test;

import android.hardware.camera2.CameraCharacteristics;

import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

public class Camera2SpecImplTest extends DeviceTestCase {

    @Test
    public void カメラスペックの取得が行えることを確認する() throws Throwable {
        Camera2SpecImpl impl = new Camera2SpecImpl(getContext());

        for (CameraType type : CameraType.values()) {
            CameraLog.hardware("CameraType[%s]", type.name());
            CameraCharacteristics cameraSpec = impl.getCameraSpec(type);
            assertNotNull(cameraSpec);

            // フラッシュモードは!=null
            assertNotNull(impl.getFlashModes(cameraSpec));

            // フォーカスチェック
            {
                List<FocusMode> focusModes = impl.getFocusModes(cameraSpec);
                assertNotNull(focusModes);
                assertNotEquals(focusModes.size(), 0);
                for (FocusMode mode : focusModes) {
                    CameraLog.hardware("  - FocusMode[%s]", mode.name(getContext()));
                }
            }

            // 撮影解像度チェック
            for (CaptureFormat fmt : CaptureFormat.values()) {
                List<CaptureSize> sizes = impl.getPictureSizes(cameraSpec, fmt);
                assertNotNull(sizes);
                if (fmt == CaptureFormat.Jpeg) {
                    assertNotEquals(sizes.size(), 0);
                }
                for (CaptureSize size : sizes) {
                    CameraLog.hardware("  - %s Picture size[%d x %d]", fmt, size.getWidth(), size.getHeight());
                }
            }

            // プレビュー解像度チェック
            {
                List<CaptureSize> sizes = impl.getPreviewSizes(cameraSpec);
                assertNotNull(sizes);
                assertNotEquals(sizes.size(), 0);
                for (CaptureSize size : sizes) {
                    CameraLog.hardware("  - Preview size[%d x %d]", size.getWidth(), size.getHeight());
                }
            }

            // シーンチェック
            {
                List<Scene> scenes = impl.getScenes(cameraSpec);
                assertNotNull(scenes);
                assertNotEquals(scenes.size(), 0);
                for (Scene scene : scenes) {
                    CameraLog.hardware("  - Scene[%s]", scene.name(getContext()));
                }
            }

            // ホワイトバランスチェック
            {
                List<WhiteBalance> whiteBalances = impl.getWhiteBalances(cameraSpec);
                assertNotNull(whiteBalances);
                assertNotEquals(whiteBalances.size(), 0);
                for (WhiteBalance wb : whiteBalances) {
                    CameraLog.hardware("  - White Balance[%s]", wb.name(getContext()));
                }
            }
        }
    }

    @Test
    public void リアカメラのフラッシュモードを取得する() throws Throwable {
        Camera2SpecImpl impl = new Camera2SpecImpl(getContext());

        CameraCharacteristics cameraSpec = impl.getCameraSpec(CameraType.Back);
        assertNotNull(cameraSpec);

        List<FlashMode> modes = impl.getFlashModes(cameraSpec);
        assertNotNull(modes);
        assertNotEquals(modes.size(), 0);
        for (FlashMode mode : modes) {
            CameraLog.hardware("FlashMode :: " + mode.name(getContext()));
            assertNotNull(mode);
            assertFalse(StringUtil.isEmpty(mode.name(getContext())));
        }
    }
}

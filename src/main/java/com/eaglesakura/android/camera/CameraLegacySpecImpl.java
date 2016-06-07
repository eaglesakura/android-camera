package com.eaglesakura.android.camera;

import com.eaglesakura.android.camera.error.CameraAccessFailedException;
import com.eaglesakura.android.camera.error.CameraException;
import com.eaglesakura.android.camera.error.CameraNotFoundException;
import com.eaglesakura.android.camera.spec.CameraType;
import com.eaglesakura.android.camera.spec.CaptureSize;
import com.eaglesakura.android.camera.spec.FlashMode;
import com.eaglesakura.android.camera.spec.FocusMode;
import com.eaglesakura.android.camera.spec.Scene;
import com.eaglesakura.android.camera.spec.WhiteBalance;
import com.eaglesakura.util.Util;

import android.content.Context;
import android.hardware.Camera;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 旧Camera APIのスペック表
 */
class CameraLegacySpecImpl {

    Camera mCamera;

    Camera.Parameters parameters;

    static final Map<CameraType, CameraSpec> sSpecCache = new HashMap<>();

    static {

        for (CameraType type : CameraType.values()) {
            Camera camera = null;
            try {
                int number = getCameraNumber(type);
                camera = Camera.open(number);
                Camera.Parameters params = camera.getParameters();

                CameraSpec spec = new CameraSpec(type);

                spec.mFlashModeSpecs = getFlashModes(params);
                spec.mFocusModeSpecs = getFocusModes(params);
                spec.mJpegPictureSize = getPictureSizes(params);
                spec.mRawPictureSize = new ArrayList<>();     // raw not support!
                spec.mPreviewSizes = getPreviewSizes(params);
                spec.mSceneSpecs = getScenes(params);
                spec.mWhiteBalanceSpecs = getWhiteBalances(params);

                sSpecCache.put(type, spec);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (camera != null) {
                    try {
                        camera.release();
                    } catch (Exception e) {

                    }
                }
            }
        }
    }

    public CameraLegacySpecImpl(Context context, Camera camera) throws CameraException {
        // カメラを取得する
        mCamera = camera;
        if (mCamera == null) {
            throw new CameraAccessFailedException();
        }

        parameters = mCamera.getParameters();
    }

    static int getCameraNumber(CameraType type) throws CameraException {
        int cameras = Camera.getNumberOfCameras();
        for (int i = 0; i < cameras; ++i) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);

            if (type == CameraType.Front && info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                return i;
            } else if (type == CameraType.Back && info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                return i;
            }
        }

        throw new CameraNotFoundException("Type:" + type);
    }


    /**
     * サポートしているプレビューサイズ一覧を取得する
     */
    @NonNull
    static List<CaptureSize> getPreviewSizes(Camera.Parameters parameters) throws CameraException {
        List<CaptureSize> result = new ArrayList<>();
        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
            result.add(new CaptureSize(size.width, size.height));
        }
        return result;
    }

    @NonNull
    static List<CaptureSize> getPictureSizes(Camera.Parameters parameters) throws CameraException {
        List<CaptureSize> result = new ArrayList<>();
        for (Camera.Size size : parameters.getSupportedPictureSizes()) {
            result.add(new CaptureSize(size.width, size.height));
        }
        return result;
    }

    @NonNull
    static List<Scene> getScenes(Camera.Parameters parameters) {
        List<Scene> result = new ArrayList<>();

        Util.ifPresent(parameters.getSupportedSceneModes(), it -> {
            for (String mode : it) {
                result.add(Scene.fromName(mode));
            }
        });
        return result;
    }

    @NonNull
    static List<FlashMode> getFlashModes(Camera.Parameters parameters) {
        List<FlashMode> result = new ArrayList<>();
        Util.ifPresent(parameters.getSupportedFlashModes(), it -> {
            for (String mode : it) {
                result.add(FlashMode.fromName(mode));
            }
        });
        return result;
    }

    @NonNull
    static List<FocusMode> getFocusModes(Camera.Parameters parameters) {
        List<FocusMode> result = new ArrayList<>();
        Util.ifPresent(parameters.getSupportedFocusModes(), it -> {
            for (String mode : it) {
                result.add(FocusMode.fromName(mode));
            }
        });
        return result;
    }

    @NonNull
    static List<WhiteBalance> getWhiteBalances(Camera.Parameters parameters) {
        List<WhiteBalance> result = new ArrayList<>();
        Util.ifPresent(parameters.getSupportedWhiteBalance(), it -> {
            for (String mode : it) {
                result.add(WhiteBalance.fromName(mode));
            }
        });
        return result;
    }

    static CameraSpec getSpecs(Context context, CameraType type) throws CameraException {
        CameraSpec spec = sSpecCache.get(type);
        if (spec == null) {
            throw new CameraNotFoundException("type : " + type);
        }

        return spec;
    }

}

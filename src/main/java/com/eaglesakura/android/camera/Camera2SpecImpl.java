package com.eaglesakura.android.camera;

import com.eaglesakura.android.camera.CameraSpec;
import com.eaglesakura.android.camera.error.CameraAccessFailedException;
import com.eaglesakura.android.camera.error.CameraException;
import com.eaglesakura.android.camera.error.CameraNotFoundException;
import com.eaglesakura.android.camera.error.CameraSpecNotFoundException;
import com.eaglesakura.android.camera.spec.CameraType;
import com.eaglesakura.android.camera.spec.CaptureFormat;
import com.eaglesakura.android.camera.spec.CaptureSize;
import com.eaglesakura.android.camera.spec.FlashMode;
import com.eaglesakura.android.camera.spec.FocusMode;
import com.eaglesakura.android.camera.spec.Scene;
import com.eaglesakura.android.camera.spec.WhiteBalance;
import com.eaglesakura.android.util.AndroidThreadUtil;
import com.eaglesakura.lambda.CancelCallback;
import com.eaglesakura.util.CollectionUtil;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Size;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressLint("NewApi")
public class Camera2SpecImpl {

    Context mContext;

    CameraManager mCameraManager;

    Camera2SpecImpl(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            throw new IllegalStateException();
        }

        mContext = context.getApplicationContext();
        mCameraManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
    }

    CameraCharacteristics getCameraSpec(CameraType type) throws CameraException {
        try {
            for (String id : mCameraManager.getCameraIdList()) {
                CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(id);
                int facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (type == CameraType.Front && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    return characteristics;
                } else if (type == CameraType.Back && facing == CameraCharacteristics.LENS_FACING_BACK) {
                    return characteristics;
                }
            }


            throw new CameraNotFoundException("type :: " + type);
        } catch (CameraAccessException e) {
            throw new CameraAccessFailedException(e);
        }
    }

    @NonNull
    List<FlashMode> getFlashModes(CameraCharacteristics characteristics) throws CameraException {
        if (!characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)) {
            return Collections.emptyList();
        }

        List<FlashMode> result = new ArrayList<>();
        for (int mode : characteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_MODES)) {
            switch (mode) {
                case CameraCharacteristics.CONTROL_AE_MODE_OFF:
                    result.add(FlashMode.SETTING_OFF);
                    break;
                case CameraCharacteristics.CONTROL_AE_MODE_ON:
                    result.add(FlashMode.SETTING_ON);
                    break;
                case CameraCharacteristics.CONTROL_AE_MODE_ON_ALWAYS_FLASH:
                    result.add(FlashMode.SETTING_TORCH);
                    break;
                case CameraCharacteristics.CONTROL_AE_MODE_ON_AUTO_FLASH_REDEYE:
                    result.add(FlashMode.SETTING_RED_EYE);
                    break;
                case CameraCharacteristics.CONTROL_AE_MODE_ON_AUTO_FLASH:
                    result.add(FlashMode.SETTING_AUTO);
                    break;
            }
        }

        return result;
    }

    @NonNull
    List<FocusMode> getFocusModes(CameraCharacteristics characteristics) throws CameraException {
        List<FocusMode> result = new ArrayList<>();

        for (int mode : characteristics.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES)) {
            switch (mode) {
                case CameraCharacteristics.CONTROL_AF_MODE_AUTO:
                    result.add(FocusMode.SETTING_AUTO);
                    break;
                case CameraCharacteristics.CONTROL_AF_MODE_CONTINUOUS_PICTURE:
                    result.add(FocusMode.SETTING_CONTINUOUS_PICTURE);
                    break;
                case CameraCharacteristics.CONTROL_AF_MODE_CONTINUOUS_VIDEO:
                    result.add(FocusMode.SETTING_CONTINUOUS_VIDEO);
                    break;
                case CameraCharacteristics.CONTROL_AF_MODE_MACRO:
                    result.add(FocusMode.SETTING_MACRO);
                    break;
                case CameraCharacteristics.CONTROL_AF_MODE_OFF:
                    result.add(FocusMode.SETTING_INFINITY);
                    break;
            }
        }

        return result;
    }

    static int toImageFormatInt(CaptureFormat format) {
        if (format == CaptureFormat.Jpeg) {
            return ImageFormat.JPEG;
        } else {
            return ImageFormat.RAW_SENSOR;
        }
    }

    @NonNull
    List<CaptureSize> getPictureSizes(CameraCharacteristics characteristics, CaptureFormat format) throws CameraException {
        Size[] sizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(toImageFormatInt(format));
        if (CollectionUtil.isEmpty(sizes)) {
            throw new CameraSpecNotFoundException("Format Error :: " + format);
        }
        List<CaptureSize> result = new ArrayList<>();
        for (Size size : sizes) {
            result.add(new CaptureSize(size.getWidth(), size.getHeight()));
        }
        return result;
    }

    /**
     * サポートしているプレビューサイズ一覧を取得する
     */
    @NonNull
    List<CaptureSize> getPreviewSizes(CameraCharacteristics characteristics) throws CameraException {
        Size[] sizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(SurfaceTexture.class);
        if (CollectionUtil.isEmpty(sizes)) {
            throw new CameraAccessFailedException("size error");
        }
        List<CaptureSize> result = new ArrayList<>();
        for (Size size : sizes) {
            result.add(new CaptureSize(size.getWidth(), size.getHeight()));
        }
        return result;
    }

    /**
     * サポートしている撮影シーン一覧を取得する
     */
    @NonNull
    List<Scene> getScenes(CameraCharacteristics characteristics) throws CameraException {
        List<Scene> result = new ArrayList<>();
        for (int mode : characteristics.get(CameraCharacteristics.CONTROL_AVAILABLE_SCENE_MODES)) {
            switch (mode) {
                case CameraCharacteristics.CONTROL_MODE_AUTO:
                    result.add(Scene.SETTING_AUTO);
                    break;
                case CameraCharacteristics.CONTROL_SCENE_MODE_PORTRAIT:
                    result.add(Scene.SETTING_PORTRAIT);
                    break;
                case CameraCharacteristics.CONTROL_SCENE_MODE_LANDSCAPE:
                    result.add(Scene.SETTING_LANDSCAPE);
                    break;
                case CameraCharacteristics.CONTROL_SCENE_MODE_NIGHT:
                    result.add(Scene.SETTING_NIGHT);
                    break;
                case CameraCharacteristics.CONTROL_SCENE_MODE_NIGHT_PORTRAIT:
                    result.add(Scene.SETTING_NIGHT_PORTRAIT);
                    break;
                case CameraCharacteristics.CONTROL_SCENE_MODE_BEACH:
                    result.add(Scene.SETTING_BEACH);
                    break;
                case CameraCharacteristics.CONTROL_SCENE_MODE_SNOW:
                    result.add(Scene.SETTING_SNOW);
                    break;
                case CameraCharacteristics.CONTROL_SCENE_MODE_SPORTS:
                    result.add(Scene.SETTING_SPORTS);
                    break;
                case CameraCharacteristics.CONTROL_SCENE_MODE_PARTY:
                    result.add(Scene.SETTING_PARTY);
                    break;
                case CameraCharacteristics.CONTROL_SCENE_MODE_BARCODE:
                    result.add(Scene.SETTING_DOCUMENT);
                    break;
            }
        }

        return result;
    }

    @NonNull
    List<WhiteBalance> getWhiteBalances(CameraCharacteristics characteristics) throws CameraException {
        List<WhiteBalance> result = new ArrayList<>();

        for (int mode : characteristics.get(CameraCharacteristics.CONTROL_AWB_AVAILABLE_MODES)) {
            switch (mode) {
                case CameraCharacteristics.CONTROL_AWB_MODE_AUTO:
                    result.add(WhiteBalance.SETTING_AUTO);
                    break;
                case CameraCharacteristics.CONTROL_AWB_MODE_INCANDESCENT:
                    result.add(WhiteBalance.SETTING_INCANDESCENT);
                    break;
                case CameraCharacteristics.CONTROL_AWB_MODE_FLUORESCENT:
                    result.add(WhiteBalance.SETTING_FLUORESCENT);
                    break;
                case CameraCharacteristics.CONTROL_AWB_MODE_DAYLIGHT:
                    result.add(WhiteBalance.SETTING_DAYLIGHT);
                    break;
                case CameraCharacteristics.CONTROL_AWB_MODE_CLOUDY_DAYLIGHT:
                    result.add(WhiteBalance.SETTING_CLOUDY_DAYLIGHT);
                    break;
            }
        }

        return result;
    }

    static CameraSpec getSpecs(Context context, CameraType type) throws CameraException {
        CameraSpec result = new CameraSpec(type);

        Camera2SpecImpl impl = new Camera2SpecImpl(context);
        CameraCharacteristics spec = impl.getCameraSpec(type);

        result.mFlashModeSpecs = impl.getFlashModes(spec);
        result.mFocusModeSpecs = impl.getFocusModes(spec);
        result.mJpegPictureSize = impl.getPictureSizes(spec, CaptureFormat.Jpeg);
        result.mRawPictureSize = impl.getPictureSizes(spec, CaptureFormat.Raw);
        result.mPreviewSizes = impl.getPreviewSizes(spec);
        result.mSceneSpecs = impl.getScenes(spec);
        result.mWhiteBalanceSpecs = impl.getWhiteBalances(spec);

        return result;
    }
}

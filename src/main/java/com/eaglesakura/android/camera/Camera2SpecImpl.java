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
import android.content.Intent;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressLint("NewApi")
public class Camera2SpecImpl {

    private static final Map<CaptureFormat, Integer> sCaptureFormatMap = new HashMap<>();

    private static final Map<FlashMode, Integer> sFlashModeMap = new HashMap<>();

    private static final Map<FocusMode, Integer> sFocusModeMap = new HashMap<>();

    private static final Map<Scene, Integer> sSceneModeMap = new HashMap<>();

    private static final Map<WhiteBalance, Integer> sWhiteBalanceMap = new HashMap<>();

    static {
        sCaptureFormatMap.put(CaptureFormat.Jpeg, ImageFormat.JPEG);
        sCaptureFormatMap.put(CaptureFormat.Raw, ImageFormat.RAW_SENSOR);

        sFlashModeMap.put(FlashMode.SETTING_OFF, CameraCharacteristics.CONTROL_AE_MODE_OFF);
        sFlashModeMap.put(FlashMode.SETTING_ON, CameraCharacteristics.CONTROL_AE_MODE_ON);
        sFlashModeMap.put(FlashMode.SETTING_TORCH, CameraCharacteristics.CONTROL_AE_MODE_ON_ALWAYS_FLASH);
        sFlashModeMap.put(FlashMode.SETTING_RED_EYE, CameraCharacteristics.CONTROL_AE_MODE_ON_AUTO_FLASH_REDEYE);
        sFlashModeMap.put(FlashMode.SETTING_AUTO, CameraCharacteristics.CONTROL_AE_MODE_ON_AUTO_FLASH);

        sFocusModeMap.put(FocusMode.SETTING_AUTO, CameraCharacteristics.CONTROL_AF_MODE_AUTO);
        sFocusModeMap.put(FocusMode.SETTING_CONTINUOUS_PICTURE, CameraCharacteristics.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
        sFocusModeMap.put(FocusMode.SETTING_CONTINUOUS_VIDEO, CameraCharacteristics.CONTROL_AF_MODE_CONTINUOUS_VIDEO);
        sFocusModeMap.put(FocusMode.SETTING_MACRO, CameraCharacteristics.CONTROL_AF_MODE_MACRO);
        sFocusModeMap.put(FocusMode.SETTING_INFINITY, CameraCharacteristics.CONTROL_AF_MODE_OFF);


        sSceneModeMap.put(Scene.SETTING_AUTO, CameraCharacteristics.CONTROL_MODE_AUTO);
        sSceneModeMap.put(Scene.SETTING_PORTRAIT, CameraCharacteristics.CONTROL_SCENE_MODE_PORTRAIT);
        sSceneModeMap.put(Scene.SETTING_LANDSCAPE, CameraCharacteristics.CONTROL_SCENE_MODE_LANDSCAPE);
        sSceneModeMap.put(Scene.SETTING_NIGHT, CameraCharacteristics.CONTROL_SCENE_MODE_NIGHT);
        sSceneModeMap.put(Scene.SETTING_NIGHT_PORTRAIT, CameraCharacteristics.CONTROL_SCENE_MODE_NIGHT_PORTRAIT);
        sSceneModeMap.put(Scene.SETTING_BEACH, CameraCharacteristics.CONTROL_SCENE_MODE_BEACH);
        sSceneModeMap.put(Scene.SETTING_SNOW, CameraCharacteristics.CONTROL_SCENE_MODE_SNOW);
        sSceneModeMap.put(Scene.SETTING_SPORTS, CameraCharacteristics.CONTROL_SCENE_MODE_SPORTS);
        sSceneModeMap.put(Scene.SETTING_PARTY, CameraCharacteristics.CONTROL_SCENE_MODE_PARTY);
        sSceneModeMap.put(Scene.SETTING_DOCUMENT, CameraCharacteristics.CONTROL_SCENE_MODE_BARCODE);

        sWhiteBalanceMap.put(WhiteBalance.SETTING_AUTO, CameraCharacteristics.CONTROL_AWB_MODE_AUTO);
        sWhiteBalanceMap.put(WhiteBalance.SETTING_INCANDESCENT, CameraCharacteristics.CONTROL_AWB_MODE_INCANDESCENT);
        sWhiteBalanceMap.put(WhiteBalance.SETTING_FLUORESCENT, CameraCharacteristics.CONTROL_AWB_MODE_FLUORESCENT);
        sWhiteBalanceMap.put(WhiteBalance.SETTING_DAYLIGHT, CameraCharacteristics.CONTROL_AWB_MODE_DAYLIGHT);
        sWhiteBalanceMap.put(WhiteBalance.SETTING_CLOUDY_DAYLIGHT, CameraCharacteristics.CONTROL_AWB_MODE_CLOUDY_DAYLIGHT);
    }


    private Context mContext;

    private CameraManager mCameraManager;

    private String mCameraId;

    Camera2SpecImpl(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            throw new IllegalStateException();
        }

        mContext = context.getApplicationContext();
        mCameraManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
    }

    CameraManager getCameraManager() {
        return mCameraManager;
    }

    String getCameraId() {
        return mCameraId;
    }

    CameraCharacteristics getCameraSpec(CameraType type) throws CameraException {
        try {
            for (String id : mCameraManager.getCameraIdList()) {
                CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(id);
                int facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (type == CameraType.Front && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    mCameraId = id;
                    return characteristics;
                } else if (type == CameraType.Back && facing == CameraCharacteristics.LENS_FACING_BACK) {
                    mCameraId = id;
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
            FlashMode keyFromValue = CollectionUtil.findKeyFromValue(sFlashModeMap, mode);
            if (keyFromValue != null) {
                result.add(keyFromValue);
            }
        }

        return result;
    }

    @NonNull
    List<FocusMode> getFocusModes(CameraCharacteristics characteristics) throws CameraException {
        List<FocusMode> result = new ArrayList<>();

        for (int mode : characteristics.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES)) {
            FocusMode value = CollectionUtil.findKeyFromValue(sFocusModeMap, mode);
            if (value != null) {
                result.add(value);
            }
        }

        return result;
    }

    @NonNull
    List<CaptureSize> getPictureSizes(CameraCharacteristics characteristics, CaptureFormat format) throws CameraException {
        Size[] sizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(toImageFormatInt(format));
        if (CollectionUtil.isEmpty(sizes)) {
            return new ArrayList<>();
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
            Scene value = CollectionUtil.findKeyFromValue(sSceneModeMap, mode);
            if (value != null) {
                result.add(value);
            }
        }

        return result;
    }

    @NonNull
    List<WhiteBalance> getWhiteBalances(CameraCharacteristics characteristics) throws CameraException {
        List<WhiteBalance> result = new ArrayList<>();

        for (int mode : characteristics.get(CameraCharacteristics.CONTROL_AWB_AVAILABLE_MODES)) {
            WhiteBalance value = CollectionUtil.findKeyFromValue(sWhiteBalanceMap, mode);
            if (value != null) {
                result.add(value);
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

        return result.init();
    }

    static int toSceneInt(Scene scene) {
        return sSceneModeMap.get(scene);
    }

    static int toAeModeInt(FocusMode mode) {
        return sFocusModeMap.get(mode);
    }

    static int toAwbInt(WhiteBalance mode) {
        return sWhiteBalanceMap.get(mode);
    }

    static int toFlashModeInt(FlashMode mode) {
        return sFlashModeMap.get(mode);
    }

    static int toImageFormatInt(CaptureFormat format) {
        return sCaptureFormatMap.get(format);
    }

}

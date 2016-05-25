package com.eaglesakura.android.camera;

import com.eaglesakura.android.camera.error.CameraException;
import com.eaglesakura.android.camera.error.CameraSpecNotFoundException;
import com.eaglesakura.android.camera.spec.CameraType;
import com.eaglesakura.android.camera.spec.FlashMode;
import com.eaglesakura.android.camera.spec.FocusMode;
import com.eaglesakura.android.camera.spec.CaptureSize;
import com.eaglesakura.android.camera.spec.Scene;
import com.eaglesakura.android.camera.spec.WhiteBalance;
import com.eaglesakura.lambda.Action1;
import com.eaglesakura.util.CollectionUtil;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * リアカメラ、フロントカメラごとのスペックを示したクラス
 */
public class CameraSpec {
    /**
     * カメラのプレビューサイズ
     */
    List<CaptureSize> mPreviewSizes;

    /**
     * カメラの撮影サイズ
     */
    List<CaptureSize> mJpegPictureSize;

    /**
     * カメラの撮影サイズ
     */
    List<CaptureSize> mRawPictureSize;

    /**
     * サポートしているシーン
     */
    List<Scene> mSceneSpecs;

    /**
     * ホワイトバランス設定一覧
     */
    List<WhiteBalance> mWhiteBalanceSpecs;

    /**
     * フォーカスモード一覧
     */
    List<FocusMode> mFocusModeSpecs;

    /**
     * フラッシュモード一覧
     */
    List<FlashMode> mFlashModeSpecs;

    final CameraType mType;

    CameraSpec(CameraType type) {
        mType = type;
    }

    /**
     * サイズの大きいものが若いインデックスになるように調整する
     */
    CameraSpec init() {
        Collections.sort(mJpegPictureSize, (a, b) -> -Double.compare(a.getMegaPixel(), b.getMegaPixel()));
        Collections.sort(mRawPictureSize, (a, b) -> -Double.compare(a.getMegaPixel(), b.getMegaPixel()));
        Collections.sort(mPreviewSizes, (a, b) -> -Double.compare(a.getMegaPixel(), b.getMegaPixel()));

        return this;
    }

    @NonNull
    public CameraType getType() {
        return mType;
    }

    //    @SuppressLint("NewApi")
//    public CameraSpec(CameraType type, Camera camera) {
//        this.type = type;
//
//        // スペックを取得する
//        Camera.Parameters parameters = camera.getParameters();
//        // 解像度
//        {
//            List<Camera.Size> sizeList = parameters.getSupportedPreviewSizes();
//            for (Camera.Size size : sizeList) {
//                mPreviewSizes.add(new PictureSize(size));
//            }
//        }
//        {
//            List<Camera.Size> sizeList = parameters.getSupportedPictureSizes();
//            for (Camera.Size size : sizeList) {
//                mShotSizes.add(new PictureSize(size));
//            }
//        }
//        // ビデオ関係のセットアップ
//        {
//            List<Camera.Size> sizeList;
//            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD) {
//                sizeList = parameters.getSupportedVideoSizes();
//            } else {
//                sizeList = parameters.getSupportedPreviewSizes();
//            }
//            for (Camera.Size size : sizeList) {
//                mVideoSizes.add(new PictureSize(size));
//            }
//
//            if (Build.VERSION.SDK_INT >= 15) {
//                // ビデオ手ぶれ補正
//                mVideoStabilizationSupported = parameters.isVideoStabilizationSupported();
//            }
//
//        }
//
//        mSceneSpecs = SceneSpec.list(parameters.getSupportedSceneModes());   // シーンモード
//        mWhiteBalanceSpecs = WhiteBaranceSpec.list(parameters.getSupportedWhiteBalance());   // ホワイトバランス
//        mFocusModeSpecs = FocusMode.list(parameters.getSupportedFocusModes()); // フォーカス設定
//        mFlashModeSpecs = FlashMode.list(parameters.getSupportedFlashModes()); // フラッシュモード一覧
//    }

    /**
     * プレビューサイズ一覧を取得
     */
    @NonNull
    public List<CaptureSize> getPreviewSizes() {
        return mPreviewSizes;
    }

    /**
     * 最低限のスペックを満たすプレビューサイズを取得する
     */
    @NonNull
    public CaptureSize getPreviewSize(int requireWidth, int requireHeight) {
        return chooseShotSize(mPreviewSizes, requireWidth, requireHeight, requireWidth, requireHeight);
    }

    /**
     * 指定したアスペクト比で最も大きなサイズを取得する
     */
    @NonNull
    public CaptureSize getJpegPictureSize(CaptureSize.Aspect aspect) {
        for (CaptureSize size : mJpegPictureSize) {
            if (size.getAspectType() == aspect) {
                return size;
            }
        }

        return mJpegPictureSize.get(0);
    }

    @NonNull
    public CaptureSize getFullJpegPictureSize() {
        return mJpegPictureSize.get(0);
    }

    /**
     * シーンをサポートしていたらtrue
     */
    public boolean isSupportedScene(Scene scene) {
        return mSceneSpecs.contains(scene);
    }

    @NonNull
    public List<Scene> getSceneSpecs() {
        return mSceneSpecs;
    }

    @NonNull
    public List<WhiteBalance> getWhiteBalanceSpecs() {
        return mWhiteBalanceSpecs;
    }

    @NonNull
    public List<CaptureSize> getJpegPictureSizes() {
        return mJpegPictureSize;
    }

    @NonNull
    public List<CaptureSize> getRawPictureSizes() {
        return mRawPictureSize;
    }

    @NonNull
    public List<FlashMode> getFlashModeSpecs() {
        return mFlashModeSpecs;
    }

    @NonNull
    public List<FocusMode> getFocusModeSpecs() {
        return mFocusModeSpecs;
    }

    /**
     * フラッシュモードを持っていたらtrue
     */
    public boolean hasFlash() {
        return !CollectionUtil.isEmpty(mFlashModeSpecs);
    }


    private CaptureSize chooseShotSize(List<CaptureSize> targetSizes, int width, int height, int minWidth, int minHeight) {

        final float reqLargeValue = Math.max(width, height);
        final float reqSmallValue = Math.min(width, height);
        final float lowerSizeLarge = Math.max(minWidth, minHeight);
        final float lowerSizeSmall = Math.min(minWidth, minHeight);

        final float TARGET_ASPECT = Math.max(1, reqLargeValue) / Math.max(1, reqSmallValue);

        CaptureSize target = targetSizes.get(0);
        try {
            float current_diff = 999999999;

            for (CaptureSize size : targetSizes) {
                final float checkLargeValue = Math.max(size.getWidth(), size.getHeight());
                final float checkSmallValue = Math.min(size.getWidth(), size.getHeight());

                // 最低限のサイズは保つ
                if (checkLargeValue >= lowerSizeLarge && checkSmallValue >= lowerSizeSmall) {
                    float aspect_diff = (checkLargeValue / checkSmallValue) - TARGET_ASPECT;

                    // アスペクト比の差分が小さい＝近い構成をコピーする
                    // 基本的に奥へ行くほど解像度が低いため、最低限の要求を満たせる解像度を探す
                    if (Math.abs(aspect_diff) <= current_diff) {
                        target = size;
                        current_diff = aspect_diff;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return target;
    }

    /**
     * IDからプレビューサイズを逆引きする
     */
    @NonNull
    public CaptureSize getPreviewSize(String id) throws CameraException {
        for (CaptureSize size : mPreviewSizes) {
            if (size.getId().equals(id)) {
                return size;
            }
        }
        throw new CameraSpecNotFoundException(id);
    }


    /**
     * IDから撮影サイズを逆引きする
     */
    @NonNull
    public CaptureSize getJpegPictureSize(String id) throws CameraException {
        for (CaptureSize size : mJpegPictureSize) {
            if (size.getId().equals(id)) {
                return size;
            }
        }
        throw new CameraSpecNotFoundException(id);
    }

    /**
     * IDから撮影サイズを逆引きする
     */
    @NonNull
    public CaptureSize getRawPictureSize(String id) throws CameraException {
        for (CaptureSize size : mRawPictureSize) {
            if (size.getId().equals(id)) {
                return size;
            }
        }
        throw new CameraSpecNotFoundException(id);
    }

    public static CameraSpec getSpecs(Context context, CameraType type) throws CameraException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Camera2
            return Camera2SpecImpl.getSpecs(context, type);
        } else {
            // Camera1
            throw new IllegalStateException();
        }
    }
}

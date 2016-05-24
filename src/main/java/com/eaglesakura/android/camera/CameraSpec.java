package com.eaglesakura.android.camera;

import com.eaglesakura.android.camera.spec.FlashMode;
import com.eaglesakura.android.camera.spec.FocusMode;
import com.eaglesakura.android.camera.spec.CaptureSize;
import com.eaglesakura.android.camera.spec.Scene;
import com.eaglesakura.android.camera.spec.WhiteBalance;

import java.util.ArrayList;
import java.util.List;

/**
 * リアカメラ、フロントカメラごとのスペックを示したクラス
 */
public class CameraSpec {
    /**
     * カメラのプレビューサイズ
     */
    private List<CaptureSize> mPreviewSizes = new ArrayList<>();

    /**
     * カメラの撮影サイズ
     */
    private List<CaptureSize> mShotSizes = new ArrayList<>();

    /**
     * ビデオの撮影サイズ
     */
    private List<CaptureSize> mVideoSizes = new ArrayList<>();

    /**
     * サポートしているシーン
     */
    private final List<Scene> mSceneSpecs = new ArrayList<>();

    /**
     * ホワイトバランス設定一覧
     */
    private final List<WhiteBalance> mWhiteBalanceSpecs = new ArrayList<>();

    /**
     * フォーカスモード一覧
     */
    private final List<FocusMode> mFocusModeSpecs = new ArrayList<>();

    /**
     * フラッシュモード一覧
     */
    private final List<FlashMode> mFlashModeSpecs = new ArrayList<>();

    /**
     * ビデオ手ぶれ補正
     */
    private boolean videoStabilizationSupported;

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
//                videoStabilizationSupported = parameters.isVideoStabilizationSupported();
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
    public List<CaptureSize> getPreviewSizes() {
        return mPreviewSizes;
    }

    /**
     * 撮影サイズ一覧を取得
     */
    public List<CaptureSize> getShotSizes() {
        return mShotSizes;
    }

    public List<CaptureSize> getVideoSizes() {
        return mVideoSizes;
    }

    /**
     * シーンをサポートしていたらtrue
     */
    public boolean isSupportedScene(Scene scene) {
        return mSceneSpecs.contains(scene);
    }

    public List<Scene> getSceneSpecs() {
        return mSceneSpecs;
    }

    public List<WhiteBalance> getWhiteBalanceSpecs() {
        return mWhiteBalanceSpecs;
    }

    public List<FlashMode> getFlashModeSpecs() {
        return mFlashModeSpecs;
    }

    public List<FocusMode> getFocusModeSpecs() {
        return mFocusModeSpecs;
    }

    public boolean isVideoStabilizationSupported() {
        return videoStabilizationSupported;
    }

    /**
     * フラッシュモードを持っていたらtrue
     */
    public boolean hasFlash() {
//        for (FlashMode spec : mFlashModeSpecs) {
//            if (spec.getName().equals("on")) {
//                return true;
//            }
//        }
        return false;
    }

    /**
     * IDからプレビューサイズを逆引きする
     */
    public CaptureSize getPreviewSize(String id) {
        for (CaptureSize size : mPreviewSizes) {
            if (size.getId().equals(id)) {
                return size;
            }
        }
        return null;
    }


    /**
     * IDから撮影サイズを逆引きする
     */
    public CaptureSize getShotSize(String id) {
        for (CaptureSize size : mShotSizes) {
            if (size.getId().equals(id)) {
                return size;
            }
        }
        return null;
    }

    /**
     * IDから撮影サイズを逆引きする
     */
    public CaptureSize getVideoSize(String id) {
        for (CaptureSize size : mVideoSizes) {
            if (size.getId().equals(id)) {
                return size;
            }
        }
        return null;
    }
}

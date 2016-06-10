package com.eaglesakura.android.camera;

import com.eaglesakura.android.camera.error.CameraAccessFailedException;
import com.eaglesakura.android.camera.error.CameraException;
import com.eaglesakura.android.camera.log.CameraLog;
import com.eaglesakura.android.camera.preview.CameraSurface;
import com.eaglesakura.android.util.ContextUtil;
import com.eaglesakura.thread.Holder;
import com.eaglesakura.util.Timer;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.IOException;

/**
 * Android 4.4以下の古い実装に対応したコントロールマネージャ
 */
public class LegacyCameraControlManager extends CameraControlManager {
    CameraSurface mPreviewSurface;

    CameraPreviewRequest mPreviewRequest;

    CameraPictureShotRequest mPictureShotRequest;

    Camera mCamera;

    Camera.Parameters mParameters;

    Camera.CameraInfo mCameraInfo = new Camera.CameraInfo();

    /**
     * プレビュー中である場合true
     */
    private static final int FLAG_NOW_PREVIEW = 0x01 << 0;

    int mFlags = 0x00;

    final Object lock = new Object();

    LegacyCameraControlManager(Context context, CameraConnectRequest request) {
        super(context, request);
    }

    @NonNull
    @Override
    public CameraApi getSupportApi() {
        return CameraApi.Legacy;
    }

    @Override
    public boolean connect(@Nullable CameraSurface previewSurface, @Nullable CameraPreviewRequest previewRequest, @Nullable CameraPictureShotRequest shotRequest) throws CameraException {
        synchronized (lock) {

            mPreviewRequest = previewRequest;
            mPreviewSurface = previewSurface;
            mPictureShotRequest = shotRequest;
            int number = CameraLegacySpecImpl.getCameraNumber(mConnectRequest.getCameraType());
            Camera.getCameraInfo(number, mCameraInfo);

            mCamera = Camera.open(number);
            mParameters = mCamera.getParameters();
            mFlags |= FLAG_NOW_PREVIEW;
        }
        return true;
    }

    @Override
    public boolean isPreviewNow() {
        return (mFlags & FLAG_NOW_PREVIEW) != 0;
    }

    @Override
    public boolean isConnected() {
        return mCamera != null;
    }

    @Override
    public void disconnect() {
        synchronized (lock) {
            if (!isConnected()) {
                throw new IllegalStateException("not connected");
            }

            if (isPreviewNow()) {
                try {
                    stopPreviewImpl();
                } catch (CameraException e) {
                    e.printStackTrace();
                }
            }
            try {
                mCamera.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mCamera = null;
        }
    }

    private void commitCameraParams() {
        try {
            mCamera.setParameters(mParameters);
        } catch (Exception e) {
        }

        try {
            mParameters = mCamera.getParameters();
        } catch (Exception e) {
        }
    }

    /**
     * デバイスの回転角にプレビュー角度を合わせる
     */
    private void requestPreviewRotateLinkDevice() {
        int deviceRotateDegree = ContextUtil.getDeviceRotateDegree(mContext);

        if (mCameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            deviceRotateDegree = (mCameraInfo.orientation + deviceRotateDegree) % 360;
            deviceRotateDegree = (360 - deviceRotateDegree) % 360;  // compensate the mirror
        } else {  // back-facing
            deviceRotateDegree = (mCameraInfo.orientation - deviceRotateDegree + 360) % 360;
        }
        mParameters.setRotation((deviceRotateDegree + 180) % 360);
        commitCameraParams();
        mCamera.setDisplayOrientation(deviceRotateDegree);
    }

    /**
     * 環境情報を設定する
     */
    private void updateEnvironment(CameraEnvironmentRequest env) throws CameraException {
        if (env.getWhiteBalance() != null) {
            mParameters.setWhiteBalance(env.getWhiteBalance().getRawName());
            commitCameraParams();
        }
        if (env.getScene() != null) {
            mParameters.setSceneMode(env.getScene().getRawName());
            commitCameraParams();
        }
        if (env.getFocusMode() != null) {
            mParameters.setFocusMode(env.getFocusMode().getRawName());
            commitCameraParams();
        }
        if (env.getFlashMode() != null) {
            mParameters.setFlashMode(env.getFlashMode().getRawName());
            commitCameraParams();
        }
    }

    private void startCameraPreview() throws CameraException {
        SurfaceTexture surface = mPreviewSurface.getSurfaceTexture(mPreviewRequest.getPreviewSize());
        try {
            mCamera.setPreviewTexture(surface);
            mCamera.startPreview();
        } catch (IOException e) {
            throw new CameraAccessFailedException(e);
        }
    }

    @Override
    public void startPreview(@Nullable CameraEnvironmentRequest env) throws CameraException {
        synchronized (lock) {
            if (!isConnected()) {
                throw new IllegalStateException("not connected");
            }
            mParameters.setPreviewSize(mPreviewRequest.getPreviewSize().getWidth(), mPreviewRequest.getPreviewSize().getHeight());
            commitCameraParams();

            requestPreviewRotateLinkDevice();
            updateEnvironment(env);
            startCameraPreview();
        }
    }

    private void stopPreviewImpl() throws CameraException {
        try {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mFlags &= (~FLAG_NOW_PREVIEW);
    }

    @Override
    public void stopPreview() throws CameraException {

        synchronized (lock) {
            if (!isPreviewNow()) {
                throw new IllegalStateException();
            }
            stopPreviewImpl();
        }
    }

    /**
     * Legacy実装のカメラを取得する
     */
    public Camera getCamera() {
        return mCamera;
    }

    /**
     * オートフォーカスを開始させる。
     * これは非同期で行う
     */
    public void startAutoFocus() {
        mCamera.cancelAutoFocus();
        mCamera.autoFocus(null);
    }

    /**
     * オートフォーカスを行わせる
     *
     * @return 成功した場合true
     */
    public boolean requestAutoFocus() {
        Timer timer = new Timer();
        try {
            Holder<Boolean> resultHolder = new Holder<>();
            mCamera.cancelAutoFocus();
            mCamera.autoFocus((success, camera) -> {
                resultHolder.set(success);
            });
            return resultHolder.getWithWait(1000 * 3);
        } catch (Exception e) {
            return false;
        } finally {
            CameraLog.hardware("AutoFocus %d ms", timer.end());
        }
    }

    @NonNull
    @Override
    public PictureData takePicture(@Nullable CameraEnvironmentRequest env) throws CameraException {
        if (!isConnected() || !isPreviewNow()) {
            throw new IllegalStateException("Preview not start");
        }

        try {
            synchronized (lock) {
                requestAutoFocus();

                if (mPictureShotRequest.hasLocation()) {
                    mParameters.setGpsLatitude(mPictureShotRequest.getLatitude());
                    mParameters.setGpsLongitude(mPictureShotRequest.getLongitude());
                    commitCameraParams();
                }

                // Jpeg画質指定
                mParameters.setJpegQuality(100);
                commitCameraParams();

                mParameters.setPreviewSize(mPreviewRequest.getPreviewSize().getWidth(), mPreviewRequest.getPreviewSize().getHeight());
                commitCameraParams();

                mParameters.setPictureSize(mPictureShotRequest.getCaptureSize().getWidth(), mPictureShotRequest.getCaptureSize().getHeight());
                commitCameraParams();

                updateEnvironment(env);

                Holder<byte[]> jpegBuffer = new Holder<>();
                mCamera.takePicture(null, null, (byte[] data, Camera camera) -> {
                    jpegBuffer.set(data);
                });


                PictureData data = new PictureData(mPictureShotRequest.getCaptureSize().getWidth(), mPictureShotRequest.getCaptureSize().getHeight(), jpegBuffer.getWithWait(1000 * 3));

                return data;
            }
        } finally {
            startPreview(env);
        }
//        return mTaskQueue.await(() -> {
//        });
    }
}

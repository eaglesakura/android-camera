package com.eaglesakura.android.camera;

import com.eaglesakura.android.camera.error.CameraAccessFailedException;
import com.eaglesakura.android.camera.error.CameraException;
import com.eaglesakura.android.camera.error.CameraSecurityException;
import com.eaglesakura.android.camera.log.CameraLog;
import com.eaglesakura.android.camera.spec.FocusMode;
import com.eaglesakura.android.thread.ui.UIHandler;
import com.eaglesakura.android.util.AndroidThreadUtil;
import com.eaglesakura.thread.Holder;
import com.eaglesakura.util.Util;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Surface;

import java.util.Arrays;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class Camera2ManagerImpl extends CameraManager {
    final Camera2SpecImpl mSpec;

    final CameraCharacteristics mCharacteristics;

    private CameraDevice mCamera;

    private CameraCaptureSession mPreviewSession;

    Camera2ManagerImpl(Context context, CameraConnectRequest request) throws CameraException {
        super(context, request);
        mSpec = new Camera2SpecImpl(context);
        mCharacteristics = mSpec.getCameraSpec(request.getCameraType());
    }

    @Override
    public boolean connect() throws CameraException {
        AndroidThreadUtil.assertBackgroundThread();

        Holder<CameraException> errorHolder = new Holder<>();
        Holder<CameraDevice> cameraDeviceHolder = new Holder<>();
        try {
            mSpec.getCameraManager().openCamera(mSpec.getCameraId(), new CameraDevice.StateCallback() {
                @Override
                public void onOpened(CameraDevice camera) {
                    cameraDeviceHolder.set(camera);
                    CameraLog.hardware("onOpened[%s]", camera.getId());
                }

                @Override
                public void onDisconnected(CameraDevice camera) {
                    mCamera = null;
                }

                @Override
                public void onError(CameraDevice camera, int error) {
                    errorHolder.set(new CameraSecurityException("Error :: " + error));
                }
            }, UIHandler.getInstance());

            // データ待ちを行う
            while (errorHolder.get() == null && cameraDeviceHolder.get() == null) {
                Util.sleep(1);
            }

            if (errorHolder.get() != null) {
                throw errorHolder.get();
            }

            mCamera = cameraDeviceHolder.get();
        } catch (CameraAccessException e) {
            throw new CameraAccessFailedException(e);
        } catch (SecurityException e) {
            throw new CameraSecurityException(e);
        }
        return false;
    }

    @Override
    public boolean isConnected() {
        return mCamera != null;
    }

    @Override
    public void disconnect() {
        AndroidThreadUtil.assertBackgroundThread();

        if (!isConnected()) {
            throw new IllegalStateException("not conencted");
        }

        try {
            stopPreview();
        } catch (Exception e) {

        }

        try {
            mCamera.close();
            mCamera = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private CaptureRequest.Builder newCaptureRequest(CameraEnvironmentRequest env, int template) throws CameraAccessException {
        CaptureRequest.Builder request = mCamera.createCaptureRequest(template);
        if (env != null) {
            if (env.getFlashMode() != null) {
                request.set(CaptureRequest.FLASH_MODE, Camera2SpecImpl.toFlashModeInt(env.getFlashMode()));
            }

            if (env.getFocusMode() != null) {
                FocusMode mode = env.getFocusMode();
                if (mode == FocusMode.SETTING_INFINITY) {
                    // https://developer.android.com/reference/android/hardware/camera2/CameraCharacteristics.html
                    // LEGACY devices will support OFF mode only if they support focusing to infinity (by also setting android.lens.focusDistance to 0.0f).
                    request.set(CaptureRequest.LENS_FOCUS_DISTANCE, 0.0f);
                }

                request.set(CaptureRequest.CONTROL_AE_MODE, Camera2SpecImpl.toAeModeInt(mode));
            }

            if (env.getScene() != null) {
                request.set(CaptureRequest.CONTROL_SCENE_MODE, Camera2SpecImpl.toSceneInt(env.getScene()));
            }

            if (env.getWhiteBalance() != null) {
                request.set(CaptureRequest.CONTROL_AWB_MODE, Camera2SpecImpl.toAwbInt(env.getWhiteBalance()));
            }
        }

        return request;
    }

    @Override
    public void startPreview(@NonNull Surface surface, @NonNull CameraPreviewRequest previewRequest, @Nullable CameraEnvironmentRequest env) throws CameraException {
        AndroidThreadUtil.assertBackgroundThread();

        try {
            Holder<CameraException> errorHolder = new Holder<>();
            Holder<CameraCaptureSession> sessionHolder = new Holder<>();

            mCamera.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    try {
                        CaptureRequest.Builder builder = newCaptureRequest(env, CameraDevice.TEMPLATE_PREVIEW);
                        builder.addTarget(surface);
                        session.stopRepeating();
                        session.setRepeatingRequest(builder.build(), null, null);
                    } catch (CameraAccessException e) {
                        errorHolder.set(new CameraException("CaptureRequest build failed"));
                        throw new IllegalStateException(e);
                    }

                    sessionHolder.set(session);
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                    errorHolder.set(new CameraException("Session create failed"));
                }
            }, UIHandler.getInstance());

            while (errorHolder.get() == null && sessionHolder.get() == null) {
                Util.sleep(1);
            }

            if (errorHolder.get() != null) {
                throw errorHolder.get();
            }

            mPreviewSession = sessionHolder.get();
        } catch (CameraAccessException e) {
            throw new CameraAccessFailedException(e);
        }
    }

    @Override
    public void stopPreview() {
        if (mPreviewSession == null) {
            throw new IllegalStateException();
        }

        try {
            mPreviewSession.stopRepeating();
            mPreviewSession.close();
            mPreviewSession = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

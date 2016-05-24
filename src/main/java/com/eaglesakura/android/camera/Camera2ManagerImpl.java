package com.eaglesakura.android.camera;

import com.eaglesakura.android.camera.CameraManager;
import com.eaglesakura.android.camera.CameraRequest;
import com.eaglesakura.android.camera.error.CameraAccessFailedException;
import com.eaglesakura.android.camera.error.CameraException;
import com.eaglesakura.android.camera.error.CameraSecurityException;
import com.eaglesakura.android.camera.log.CameraLog;
import com.eaglesakura.android.thread.ui.UIHandler;
import com.eaglesakura.android.util.AndroidThreadUtil;
import com.eaglesakura.android.util.AndroidUtil;
import com.eaglesakura.lambda.CancelCallback;
import com.eaglesakura.thread.Holder;
import com.eaglesakura.util.Util;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.Surface;

import java.util.Arrays;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class Camera2ManagerImpl extends CameraManager {
    final Camera2SpecImpl mSpec;

    final CameraCharacteristics mCharacteristics;

    private CameraDevice mCamera;

    private CameraCaptureSession mPreviewSession;

    Camera2ManagerImpl(Context context, CameraRequest request) throws CameraException {
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
            mCamera.close();
            mCamera = null;
        } catch (Exception e) {

        }
    }

    @Override
    public void startPreview(Surface surface, CancelCallback cancelCallback) throws CameraException {
        AndroidThreadUtil.assertBackgroundThread();

        try {
            Holder<CameraException> errorHolder = new Holder<>();
            Holder<CameraCaptureSession> sessionHolder = new Holder<>();

            mCamera.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    try {
                        CaptureRequest.Builder builder = mCamera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                        builder.addTarget(surface);
                        session.setRepeatingRequest(builder.build(), null, null);
                    } catch (CameraAccessException e) {
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

package com.eaglesakura.android.camera;

import com.eaglesakura.android.camera.error.CameraAccessFailedException;
import com.eaglesakura.android.camera.error.CameraException;
import com.eaglesakura.android.camera.error.CameraSecurityException;
import com.eaglesakura.android.camera.error.PictureFailedException;
import com.eaglesakura.android.camera.log.CameraLog;
import com.eaglesakura.android.camera.spec.CameraType;
import com.eaglesakura.android.camera.spec.FocusMode;
import com.eaglesakura.android.thread.async.AsyncHandler;
import com.eaglesakura.android.thread.ui.UIHandler;
import com.eaglesakura.android.util.AndroidThreadUtil;
import com.eaglesakura.android.util.ContextUtil;
import com.eaglesakura.thread.Holder;
import com.eaglesakura.util.Util;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.location.Location;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class Camera2ManagerImpl extends CameraManager {
    final Camera2SpecImpl mSpec;

    final CameraCharacteristics mCharacteristics;

    private CameraDevice mCamera;

    private CameraCaptureSession mPreviewSession;

    private Surface mPreviewSurface;


    private static AsyncHandler sControlHandler = AsyncHandler.createInstance("camera-ctrl");

    private static AsyncHandler sProcessingHandler = sControlHandler;


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

            synchronized (this) {
                mCamera = cameraDeviceHolder.get();
            }
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

    private int getJpegOrientation() {
        int sensorOrientation = mCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        int deviceRotateDegree = ContextUtil.getDeviceRotateDegree(mContext);

        if (mRequest.getCameraType() == CameraType.Back) {
            deviceRotateDegree = (360 - sensorOrientation + deviceRotateDegree) % 360;
        } else {
            deviceRotateDegree = (sensorOrientation + deviceRotateDegree + 360) % 360;
        }
        return deviceRotateDegree;
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

                request.set(CaptureRequest.CONTROL_AF_MODE, Camera2SpecImpl.toAeModeInt(mode));
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
    public void request(CameraEnvironmentRequest env) throws CameraException {
        AndroidThreadUtil.assertBackgroundThread();

        try {
            CaptureRequest.Builder builder = newCaptureRequest(env, CameraDevice.TEMPLATE_PREVIEW);
            builder.addTarget(mPreviewSurface);

            mPreviewSession.stopRepeating();
            mPreviewSession.setRepeatingRequest(builder.build(), null, null);
        } catch (CameraAccessException e) {
            throw new CameraAccessFailedException(e);
        }
    }

    @NonNull
    private CameraCaptureSession newSession(List<Surface> surfaces) throws CameraException {
        Holder<CameraException> errorHolder = new Holder<>();
        Holder<CameraCaptureSession> sessionHolder = new Holder<>();

        try {
            mCamera.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    sessionHolder.set(session);
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                    errorHolder.set(new CameraException("Session create failed"));
                }
            }, sControlHandler);
        } catch (CameraAccessException e) {
            throw new CameraAccessFailedException(e);
        }

        while (errorHolder.get() == null && sessionHolder.get() == null) {
            Util.sleep(1);
        }

        if (errorHolder.get() != null) {
            throw errorHolder.get();
        }

        return sessionHolder.get();
    }

    @Override
    public void startPreview(@NonNull Surface surface, @NonNull CameraPreviewRequest previewRequest, @Nullable CameraEnvironmentRequest env) throws CameraException {
        AndroidThreadUtil.assertBackgroundThread();
        try {

            // セッションを生成する
            CameraCaptureSession previewSession = newSession(Arrays.asList(surface));

            // プレビューを開始する
            CaptureRequest.Builder builder = newCaptureRequest(env, CameraDevice.TEMPLATE_PREVIEW);
            builder.addTarget(surface);
            previewSession.stopRepeating();
            previewSession.setRepeatingRequest(builder.build(), null, null);

            synchronized (this) {
                mPreviewSurface = surface;
                mPreviewSession = previewSession;
            }
        } catch (CameraAccessException e) {
            throw new CameraAccessFailedException(e);
        }
    }

    @Override
    public void stopPreview() {
        synchronized (this) {
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

    private void startPreCapture(CameraCaptureSession session, Surface imageSurface, @Nullable CameraEnvironmentRequest env) throws CameraException, CameraAccessException {
        CaptureRequest.Builder builder = newCaptureRequest(env, CameraDevice.TEMPLATE_PREVIEW);
        builder.addTarget(imageSurface);
        builder.set(CaptureRequest.JPEG_ORIENTATION, getJpegOrientation());
        builder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_START);
        builder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START);

        Holder<CameraException> errorHolder = new Holder<>();
        Holder<Boolean> completedHolder = new Holder<>();
        session.capture(builder.build(), new CameraCaptureSession.CaptureCallback() {
            @Override
            public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                CameraLog.hardware("onCaptureCompleted :: pre-capture");
                CameraLog.hardware("  - AE State :: " + aeState);

                completedHolder.set(Boolean.TRUE);
            }

            @Override
            public void onCaptureFailed(CameraCaptureSession session, CaptureRequest request, CaptureFailure failure) {
                errorHolder.set(new PictureFailedException("PreCapture Failed"));
            }
        }, sProcessingHandler);

        while (errorHolder.get() == null && completedHolder.get() == null) {
            Util.sleep(1);
        }

        if (errorHolder.get() != null) {
            throw errorHolder.get();
        }
    }

    @Override
    public PictureData takePicture(@NonNull CameraPictureShotRequest request, @Nullable CameraEnvironmentRequest env) throws CameraException {

        ImageReader imageReader = ImageReader.newInstance(
                request.getCaptureSize().getWidth(), request.getCaptureSize().getHeight(),
                Camera2SpecImpl.toImageFormatInt(request.getCaptureFormat()),
                1
        );

        CameraCaptureSession pictureSession = newSession(Arrays.asList(imageReader.getSurface()));
        try {

            startPreCapture(pictureSession, imageReader.getSurface(), env);

            Holder<CameraException> errorHolder = new Holder<>();
            Holder<PictureData> resultHolder = new Holder<>();

            // 撮影コールバック
            CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureFailed(CameraCaptureSession session, CaptureRequest request, CaptureFailure failure) {
                    errorHolder.set(new PictureFailedException("Fail :: " + failure.getReason()));
                }
            };

            // 画像圧縮完了コールバック
            imageReader.setOnImageAvailableListener(it -> {
                Image image = imageReader.acquireLatestImage();
                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                byte[] onMemoryFile = new byte[buffer.capacity()];
                buffer.get(onMemoryFile);

                resultHolder.set(new PictureData(image.getWidth(), image.getHeight(), onMemoryFile));
            }, sProcessingHandler);

            CaptureRequest.Builder builder = newCaptureRequest(env, CameraDevice.TEMPLATE_STILL_CAPTURE);
            builder.set(CaptureRequest.JPEG_ORIENTATION, getJpegOrientation());

            // Lat/Lng
            if (request.hasLocation()) {
                Location loc = new Location("camera");
                loc.setLatitude(request.getLatitude());
                loc.setLongitude(request.getLongitude());
                builder.set(CaptureRequest.JPEG_GPS_LOCATION, loc);
            }

            builder.addTarget(imageReader.getSurface());
            pictureSession.capture(builder.build(), captureCallback, sControlHandler);

            while (errorHolder.get() == null && resultHolder.get() == null) {
                Util.sleep(1);
            }

            if (errorHolder.get() != null) {
                throw errorHolder.get();
            }

            return resultHolder.get();
        } catch (CameraAccessException e) {
            throw new CameraAccessFailedException(e);
        } finally {
            imageReader.close();
            pictureSession.close();
        }
    }
}

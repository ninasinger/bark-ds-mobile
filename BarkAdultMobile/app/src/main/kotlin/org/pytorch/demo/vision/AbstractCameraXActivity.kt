package org.pytorch.demo.vision

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.SystemClock
import android.util.Size
import android.view.TextureView
import android.widget.Toast
import org.pytorch.demo.BaseModuleActivity
import org.pytorch.demo.StatusBarUtils
import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import androidx.camera.core.CameraX
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageAnalysisConfig
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.PreviewConfig
import androidx.core.app.ActivityCompat

abstract class AbstractCameraXActivity<R> : BaseModuleActivity() {
    private var mLastAnalysisResultTime: Long = 0
    protected abstract fun getContentViewLayoutId(): Int
    protected abstract fun getCameraPreviewTextureView(): TextureView?
    @Override
    protected fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StatusBarUtils.setStatusBarOverlay(getWindow(), true)
        setContentView(getContentViewLayoutId())
        startBackgroundThread()
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            !== PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                PERMISSIONS,
                REQUEST_CODE_CAMERA_PERMISSION
            )
        } else {
            setupCameraX()
        }
    }

    @Override
    fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String?>?, grantResults: IntArray?
    ) {
        if (requestCode == REQUEST_CODE_CAMERA_PERMISSION) {
            if (grantResults.get(0) == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(
                    this,
                    "You can't use image classification example without granting CAMERA permission",
                    Toast.LENGTH_LONG
                )
                    .show()
                finish()
            } else {
                setupCameraX()
            }
        }
    }

    private fun setupCameraX() {
        val textureView: TextureView? = getCameraPreviewTextureView()
        val previewConfig: PreviewConfig = Builder().build()
        val preview = Preview(previewConfig)
        preview.setOnPreviewOutputUpdateListener { output -> textureView.setSurfaceTexture(output.getSurfaceTexture()) }
        val imageAnalysisConfig: ImageAnalysisConfig = Builder()
            .setTargetResolution(Size(224, 224))
            .setCallbackHandler(mBackgroundHandler)
            .setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
            .build()
        val imageAnalysis = ImageAnalysis(imageAnalysisConfig)
        imageAnalysis.setAnalyzer { image, rotationDegrees ->
            if (SystemClock.elapsedRealtime() - mLastAnalysisResultTime < 500) {
                return@setAnalyzer
            }
            val result = analyzeImage(image, rotationDegrees)
            if (result != null) {
                mLastAnalysisResultTime = SystemClock.elapsedRealtime()
                runOnUiThread { applyToUiAnalyzeImageResult(result) }
            }
        }
        CameraX.bindToLifecycle(this, preview, imageAnalysis)
    }

    @WorkerThread
    @Nullable
    protected abstract fun analyzeImage(image: ImageProxy?, rotationDegrees: Int): R?
    @UiThread
    protected abstract fun applyToUiAnalyzeImageResult(result: R?)

    companion object {
        private const val REQUEST_CODE_CAMERA_PERMISSION = 200
        private val PERMISSIONS: Array<String?>? = arrayOf<String?>(Manifest.permission.CAMERA)
    }
}
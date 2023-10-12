package org.pytorch.demo.vision

import android.os.Bundle
import android.os.SystemClock
import android.text.TextUtils
import android.util.Log
import android.view.TextureView
import android.view.View
import android.view.ViewStub
import android.widget.TextView
import org.pytorch.IValue
import org.pytorch.Module
import org.pytorch.Tensor
import org.pytorch.demo.Constants
import org.pytorch.demo.R
import org.pytorch.demo.Utils
import org.pytorch.demo.vision.view.ResultRowView
import org.pytorch.torchvision.TensorImageUtils
import java.io.File
import java.nio.FloatBuffer
import java.util.LinkedList
import java.util.Locale
import java.util.Queue
import androidx.annotation.WorkerThread
import androidx.camera.core.ImageProxy

class ImageClassificationActivity :
    AbstractCameraXActivity<ImageClassificationActivity.AnalysisResult?>() {
    internal class AnalysisResult(
        private val topNClassNames: Array<String?>?, private val topNScores: FloatArray?,
        private val moduleForwardDuration: Long, private val analysisDuration: Long
    )

    private var mAnalyzeImageErrorState = false
    private val mResultRowViews: Array<ResultRowView?>? = arrayOfNulls<ResultRowView?>(TOP_K)
    private var mFpsText: TextView? = null
    private var mMsText: TextView? = null
    private var mMsAvgText: TextView? = null
    private var mModule: Module? = null
    private var mModuleAssetName: String? = null
    private var mInputTensorBuffer: FloatBuffer? = null
    private var mInputTensor: Tensor? = null
    private var mMovingAvgSum: Long = 0
    private val mMovingAvgQueue: Queue<Long?>? = LinkedList()
    @Override
    protected fun getContentViewLayoutId(): Int {
        return R.layout.activity_image_classification
    }

    @Override
    protected fun getCameraPreviewTextureView(): TextureView? {
        return (findViewById(R.id.image_classification_texture_view_stub) as ViewStub?)
            .inflate()
            .findViewById(R.id.image_classification_texture_view)
    }

    @Override
    protected fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val headerResultRowView: ResultRowView =
            findViewById(R.id.image_classification_result_header_row)
        headerResultRowView.nameTextView.setText(R.string.image_classification_results_header_row_name)
        headerResultRowView.scoreTextView.setText(R.string.image_classification_results_header_row_score)
        mResultRowViews.get(0) = findViewById(R.id.image_classification_top1_result_row)
        mResultRowViews.get(1) = findViewById(R.id.image_classification_top2_result_row)
        mResultRowViews.get(2) = findViewById(R.id.image_classification_top3_result_row)
        mFpsText = findViewById(R.id.image_classification_fps_text)
        mMsText = findViewById(R.id.image_classification_ms_text)
        mMsAvgText = findViewById(R.id.image_classification_ms_avg_text)
    }

    @Override
    protected fun applyToUiAnalyzeImageResult(result: AnalysisResult?) {
        mMovingAvgSum += result.moduleForwardDuration
        mMovingAvgQueue.add(result.moduleForwardDuration)
        if (mMovingAvgQueue.size() > MOVING_AVG_PERIOD) {
            mMovingAvgSum -= mMovingAvgQueue.remove()
        }
        for (i in 0 until TOP_K) {
            val rowView: ResultRowView? = mResultRowViews.get(i)
            rowView.nameTextView.setText(result.topNClassNames.get(i))
            rowView.scoreTextView.setText(
                String.format(
                    Locale.US, SCORES_FORMAT,
                    result.topNScores.get(i)
                )
            )
            rowView.setProgressState(false)
        }
        mMsText.setText(String.format(Locale.US, FORMAT_MS, result.moduleForwardDuration))
        if (mMsText.getVisibility() !== View.VISIBLE) {
            mMsText.setVisibility(View.VISIBLE)
        }
        mFpsText.setText(String.format(Locale.US, FORMAT_FPS, 1000f / result.analysisDuration))
        if (mFpsText.getVisibility() !== View.VISIBLE) {
            mFpsText.setVisibility(View.VISIBLE)
        }
        if (mMovingAvgQueue.size() === MOVING_AVG_PERIOD) {
            val avgMs = mMovingAvgSum.toFloat() / MOVING_AVG_PERIOD
            mMsAvgText.setText(String.format(Locale.US, FORMAT_AVG_MS, avgMs))
            if (mMsAvgText.getVisibility() !== View.VISIBLE) {
                mMsAvgText.setVisibility(View.VISIBLE)
            }
        }
    }

    protected fun getModuleAssetName(): String? {
        if (!TextUtils.isEmpty(mModuleAssetName)) {
            return mModuleAssetName
        }
        val moduleAssetNameFromIntent: String = getIntent().getStringExtra(
            INTENT_MODULE_ASSET_NAME
        )
        mModuleAssetName =
            if (!TextUtils.isEmpty(moduleAssetNameFromIntent)) moduleAssetNameFromIntent else "resnet18.pt"
        return mModuleAssetName
    }

    @Override
    protected fun getInfoViewAdditionalText(): String? {
        return getModuleAssetName()
    }

    @Override
    @WorkerThread
    @Nullable
    protected fun analyzeImage(image: ImageProxy?, rotationDegrees: Int): AnalysisResult? {
        return if (mAnalyzeImageErrorState) {
            null
        } else try {
            if (mModule == null) {
                val moduleFileAbsoluteFilePath: String = File(
                    Utils.assetFilePath(this, getModuleAssetName())
                ).getAbsolutePath()
                mModule = Module.load(moduleFileAbsoluteFilePath)
                mInputTensorBuffer =
                    Tensor.allocateFloatBuffer(3 * INPUT_TENSOR_WIDTH * INPUT_TENSOR_HEIGHT)
                mInputTensor = Tensor.fromBlob(
                    mInputTensorBuffer,
                    longArrayOf(1, 3, INPUT_TENSOR_HEIGHT.toLong(), INPUT_TENSOR_WIDTH.toLong())
                )
            }
            val startTime: Long = SystemClock.elapsedRealtime()
            TensorImageUtils.imageYUV420CenterCropToFloatBuffer(
                image.getImage(), rotationDegrees,
                INPUT_TENSOR_WIDTH, INPUT_TENSOR_HEIGHT,
                TensorImageUtils.TORCHVISION_NORM_MEAN_RGB,
                TensorImageUtils.TORCHVISION_NORM_STD_RGB,
                mInputTensorBuffer, 0
            )
            val moduleForwardStartTime: Long = SystemClock.elapsedRealtime()
            val outputTensor: Tensor = mModule.forward(IValue.from(mInputTensor)).toTensor()
            val moduleForwardDuration: Long = SystemClock.elapsedRealtime() - moduleForwardStartTime
            val scores: FloatArray = outputTensor.getDataAsFloatArray()
            val ixs: IntArray = Utils.topK(scores, TOP_K)
            val topKClassNames = arrayOfNulls<String?>(TOP_K)
            val topKScores = FloatArray(TOP_K)
            for (i in 0 until TOP_K) {
                val ix = ixs[i]
                topKClassNames[i] = Constants.IMAGENET_CLASSES.get(ix)
                topKScores[i] = scores[ix]
            }
            val analysisDuration: Long = SystemClock.elapsedRealtime() - startTime
            AnalysisResult(topKClassNames, topKScores, moduleForwardDuration, analysisDuration)
        } catch (e: Exception) {
            Log.e(Constants.TAG, "Error during image analysis", e)
            mAnalyzeImageErrorState = true
            runOnUiThread {
                if (!isFinishing()) {
                    showErrorDialog { v -> this@ImageClassificationActivity.finish() }
                }
            }
            null
        }
    }

    @Override
    protected fun getInfoViewCode(): Int {
        return getIntent().getIntExtra(INTENT_INFO_VIEW_TYPE, -1)
    }

    @Override
    protected fun onDestroy() {
        super.onDestroy()
        if (mModule != null) {
            mModule.destroy()
        }
    }

    companion object {
        val INTENT_MODULE_ASSET_NAME: String? = "INTENT_MODULE_ASSET_NAME"
        val INTENT_INFO_VIEW_TYPE: String? = "INTENT_INFO_VIEW_TYPE"
        private const val INPUT_TENSOR_WIDTH = 224
        private const val INPUT_TENSOR_HEIGHT = 224
        private const val TOP_K = 3
        private const val MOVING_AVG_PERIOD = 10
        private val FORMAT_MS: String? = "%dms"
        private val FORMAT_AVG_MS: String? = "avg:%.0fms"
        private val FORMAT_FPS: String? = "%.1fFPS"
        val SCORES_FORMAT: String? = "%.2f"
    }
}
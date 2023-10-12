package org.pytorch.demo.nlp

import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import org.pytorch.IValue
import org.pytorch.Module
import org.pytorch.Tensor
import org.pytorch.demo.BaseModuleActivity
import org.pytorch.demo.InfoViewFactory
import org.pytorch.demo.R
import org.pytorch.demo.Utils
import org.pytorch.demo.vision.view.ResultRowView
import java.io.File
import java.nio.charset.Charset
import java.util.Locale
import androidx.annotation.WorkerThread

class TextClassificationActivity : BaseModuleActivity() {
    private var mEditText: EditText? = null
    private var mResultContent: View? = null
    private val mResultRowViews: Array<ResultRowView?>? = arrayOfNulls<ResultRowView?>(3)
    private var mModule: Module? = null
    private var mModuleAssetName: String? = null
    private var mLastBgHandledText: String? = null
    private var mModuleClasses: Array<String?>?

    private class AnalysisResult(
        private val topKClassNames: Array<String?>?,
        private val topKScores: FloatArray?
    )

    private val mOnEditTextStopRunnable: Runnable? = Runnable {
        val text: String = mEditText.getText().toString()
        mBackgroundHandler.post {
            if (TextUtils.equals(text, mLastBgHandledText)) {
                return@post
            }
            if (TextUtils.isEmpty(text)) {
                runOnUiThread { applyUIEmptyTextState() }
                mLastBgHandledText = null
                return@post
            }
            val result = analyzeText(text)
            if (result != null) {
                runOnUiThread { applyUIAnalysisResult(result) }
                mLastBgHandledText = text
            }
        }
    }

    @Override
    protected fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_text_classification)
        mEditText = findViewById(R.id.text_classification_edit_text)
        findViewById(R.id.text_classification_clear_button).setOnClickListener { v ->
            mEditText.setText(
                ""
            )
        }
        val headerRow: ResultRowView = findViewById(R.id.text_classification_result_header_row)
        headerRow.nameTextView.setText(R.string.text_classification_topic)
        headerRow.scoreTextView.setText(R.string.text_classification_score)
        headerRow.setVisibility(View.VISIBLE)
        mResultRowViews.get(0) = findViewById(R.id.text_classification_top1_result_row)
        mResultRowViews.get(1) = findViewById(R.id.text_classification_top2_result_row)
        mResultRowViews.get(2) = findViewById(R.id.text_classification_top3_result_row)
        mResultContent = findViewById(R.id.text_classification_result_content)
        mEditText.addTextChangedListener(InternalTextWatcher())
    }

    protected fun getModuleAssetName(): String? {
        if (!TextUtils.isEmpty(mModuleAssetName)) {
            return mModuleAssetName
        }
        val moduleAssetNameFromIntent: String = getIntent().getStringExtra(
            INTENT_MODULE_ASSET_NAME
        )
        mModuleAssetName =
            if (!TextUtils.isEmpty(moduleAssetNameFromIntent)) moduleAssetNameFromIntent else "model-reddit16-f140225004_2.pt1"
        return mModuleAssetName
    }

    @WorkerThread
    @Nullable
    private fun analyzeText(text: String?): AnalysisResult? {
        if (mModule == null) {
            val moduleFileAbsoluteFilePath: String = File(
                Utils.assetFilePath(this, getModuleAssetName())
            ).getAbsolutePath()
            mModule = Module.load(moduleFileAbsoluteFilePath)
            val getClassesOutput: IValue = mModule.runMethod("get_classes")
            val classesListIValue: Array<IValue?> = getClassesOutput.toList()
            val moduleClasses = arrayOfNulls<String?>(classesListIValue.size)
            var i = 0
            for (iv in classesListIValue) {
                moduleClasses[i++] = iv.toStr()
            }
            mModuleClasses = moduleClasses
        }
        val bytes: ByteArray = text.getBytes(Charset.forName("UTF-8"))
        val shape = longArrayOf(1, bytes.size.toLong())
        val inputTensor: Tensor = Tensor.fromBlobUnsigned(bytes, shape)
        val outputTensor: Tensor = mModule.forward(IValue.from(inputTensor)).toTensor()
        val scores: FloatArray = outputTensor.getDataAsFloatArray()
        val ixs: IntArray = Utils.topK(scores, TOP_K)
        val topKClassNames = arrayOfNulls<String?>(TOP_K)
        val topKScores = FloatArray(TOP_K)
        for (i in 0 until TOP_K) {
            val ix = ixs[i]
            topKClassNames[i] = mModuleClasses.get(ix)
            topKScores[i] = scores[ix]
        }
        return AnalysisResult(topKClassNames, topKScores)
    }

    private fun applyUIAnalysisResult(result: AnalysisResult?) {
        for (i in 0 until TOP_K) {
            setUiResultRowView(
                mResultRowViews.get(i),
                result.topKClassNames.get(i),
                String.format(Locale.US, SCORES_FORMAT, result.topKScores.get(i))
            )
        }
        mResultContent.setVisibility(View.VISIBLE)
    }

    private fun applyUIEmptyTextState() {
        mResultContent.setVisibility(View.GONE)
    }

    private fun setUiResultRowView(resultRowView: ResultRowView?, name: String?, score: String?) {
        resultRowView.nameTextView.setText(name)
        resultRowView.scoreTextView.setText(score)
        resultRowView.setProgressState(false)
    }

    @Override
    protected fun getInfoViewCode(): Int {
        return InfoViewFactory.INFO_VIEW_TYPE_TEXT_CLASSIFICATION
    }

    @Override
    protected fun onDestroy() {
        super.onDestroy()
        if (mModule != null) {
            mModule.destroy()
        }
    }

    private inner class InternalTextWatcher : TextWatcher {
        @Override
        fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        @Override
        fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }

        @Override
        fun afterTextChanged(s: Editable?) {
            mUIHandler.removeCallbacks(mOnEditTextStopRunnable)
            mUIHandler.postDelayed(mOnEditTextStopRunnable, EDIT_TEXT_STOP_DELAY)
        }
    }

    companion object {
        val INTENT_MODULE_ASSET_NAME: String? = "INTENT_MODULE_ASSET_NAME"
        private const val EDIT_TEXT_STOP_DELAY = 600L
        private const val TOP_K = 3
        private val SCORES_FORMAT: String? = "%.2f"
    }
}
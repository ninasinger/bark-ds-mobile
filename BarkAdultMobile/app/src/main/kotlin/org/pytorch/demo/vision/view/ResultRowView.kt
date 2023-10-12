package org.pytorch.demo.vision.view

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import org.pytorch.demo.R
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.Px
import androidx.annotation.StyleRes

class ResultRowView(
    @NonNull context: Context?, @Nullable attrs: AttributeSet?, defStyleAttr: Int,
    defStyleRes: Int
) : RelativeLayout(context, attrs, defStyleAttr, defStyleRes) {
    val nameTextView: TextView?
    val scoreTextView: TextView?

    @Px
    private val mProgressBarHeightPx = 0

    @Px
    private val mProgressBarPaddingPx = 0

    @Nullable
    private val mProgressBarDrawable: Drawable? = null

    @Nullable
    private val mProgressBarProgressStateDrawable: Drawable? = null
    private var mIsInProgress = true

    constructor(@NonNull context: Context?) : this(context, null)
    constructor(@NonNull context: Context?, @Nullable attrs: AttributeSet?) : this(
        context,
        attrs,
        0
    )

    constructor(
        @NonNull context: Context?,
        @Nullable attrs: AttributeSet?,
        defStyleAttr: Int
    ) : this(context, attrs, defStyleAttr, 0)

    init {
        inflate(context, R.layout.image_classification_result_row, this)
        nameTextView = findViewById(R.id.result_row_name_text)
        scoreTextView = findViewById(R.id.result_row_score_text)
        val a: TypedArray = context.getTheme().obtainStyledAttributes(
            attrs,
            R.styleable.ResultRowView,
            defStyleAttr, defStyleRes
        )
        try {
            @StyleRes val textAppearanceResId: Int = a.getResourceId(
                R.styleable.ResultRowView_textAppearance,
                R.style.TextAppearanceImageClassificationResultTop2Plus
            )
            nameTextView.setTextAppearance(context, textAppearanceResId)
            scoreTextView.setTextAppearance(context, textAppearanceResId)
            @DimenRes val progressBarHeightDimenResId: Int =
                a.getResourceId(R.styleable.ResultRowView_progressBarHeightRes, 0)
            mProgressBarHeightPx =
                if (progressBarHeightDimenResId != 0) getResources().getDimensionPixelSize(
                    progressBarHeightDimenResId
                ) else 0
            @DimenRes val progressBarPaddingDimenResId: Int =
                a.getResourceId(R.styleable.ResultRowView_progressBarPaddingRes, 0)
            mProgressBarPaddingPx =
                if (progressBarPaddingDimenResId != 0) getResources().getDimensionPixelSize(
                    progressBarPaddingDimenResId
                ) else 0
            setPadding(
                getPaddingLeft(), getPaddingTop(), getPaddingRight(),
                getBottom() + mProgressBarPaddingPx + mProgressBarHeightPx
            )
            @DrawableRes val progressBarDrawableResId: Int =
                a.getResourceId(R.styleable.ResultRowView_progressBarDrawableRes, 0)
            mProgressBarDrawable = if (progressBarDrawableResId != 0) getResources().getDrawable(
                progressBarDrawableResId,
                null
            ) else null
            @DrawableRes val progressBarDrawableProgressStateResId: Int =
                a.getResourceId(R.styleable.ResultRowView_progressBarDrawableProgressStateRes, 0)
            mProgressBarProgressStateDrawable =
                if (progressBarDrawableResId != 0) getResources().getDrawable(
                    progressBarDrawableProgressStateResId,
                    null
                ) else null
        } finally {
            a.recycle()
        }
    }

    @Override
    protected fun dispatchDraw(canvas: Canvas?) {
        super.dispatchDraw(canvas)
        val drawable: Drawable =
            if (mIsInProgress) mProgressBarProgressStateDrawable else mProgressBarDrawable
        if (drawable != null) {
            val h: Int = canvas.getHeight()
            val w: Int = canvas.getWidth()
            drawable.setBounds(0, h - mProgressBarHeightPx, w, h)
            drawable.draw(canvas)
        }
    }

    fun setProgressState(isInProgress: Boolean) {
        val changed = isInProgress != mIsInProgress
        mIsInProgress = isInProgress
        if (isInProgress) {
            nameTextView.setVisibility(View.INVISIBLE)
            scoreTextView.setVisibility(View.INVISIBLE)
        } else {
            nameTextView.setVisibility(View.VISIBLE)
            scoreTextView.setVisibility(View.VISIBLE)
        }
        if (changed) {
            invalidate()
        }
    }
}
package org.pytorch.demo

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.constraintlayout.widget.ConstraintLayout

class ListCardView(@NonNull context: Context?, @Nullable attrs: AttributeSet?, defStyleAttr: Int) :
    ConstraintLayout(context, attrs, defStyleAttr) {
    private val mTitleTextView: TextView?
    private val mDescriptionTextView: TextView?
    private val mImageView: ImageView?

    constructor(@NonNull context: Context?) : this(context, null)
    constructor(@NonNull context: Context?, @Nullable attrs: AttributeSet?) : this(
        context,
        attrs,
        0
    )

    init {
        inflate(context, R.layout.list_card, this)
        mTitleTextView = findViewById(R.id.list_card_title)
        mDescriptionTextView = findViewById(R.id.list_card_description)
        mImageView = findViewById(R.id.list_card_image)
        val a: TypedArray = context.getTheme().obtainStyledAttributes(
            attrs,
            R.styleable.ListCardView,
            defStyleAttr, 0
        )
        try {
            @StringRes val titleResId: Int = a.getResourceId(R.styleable.ListCardView_titleRes, 0)
            if (titleResId != 0) {
                mTitleTextView.setText(titleResId)
                mTitleTextView.setVisibility(View.VISIBLE)
            } else {
                mTitleTextView.setVisibility(View.GONE)
            }
            @StringRes val descResId: Int =
                a.getResourceId(R.styleable.ListCardView_descriptionRes, 0)
            if (descResId != 0) {
                mDescriptionTextView.setText(descResId)
                mDescriptionTextView.setVisibility(View.VISIBLE)
            } else {
                mDescriptionTextView.setVisibility(View.GONE)
            }
            @DrawableRes val imageResId: Int = a.getResourceId(R.styleable.ListCardView_imageRes, 0)
            if (imageResId != 0) {
                mImageView.setImageResource(imageResId)
                mImageView.setVisibility(View.VISIBLE)
            } else {
                mImageView.setVisibility(View.GONE)
            }
        } finally {
            a.recycle()
        }
    }
}
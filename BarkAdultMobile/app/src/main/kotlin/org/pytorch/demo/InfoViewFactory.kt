package org.pytorch.demo

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView

object InfoViewFactory {
    const val INFO_VIEW_TYPE_IMAGE_CLASSIFICATION_RESNET = 1
    const val INFO_VIEW_TYPE_IMAGE_CLASSIFICATION_QMOBILENET = 2
    const val INFO_VIEW_TYPE_TEXT_CLASSIFICATION = 3
    fun newInfoView(
        context: Context?,
        infoViewType: Int,
        @Nullable additionalText: String?
    ): View? {
        val inflater: LayoutInflater = LayoutInflater.from(context)
        if (INFO_VIEW_TYPE_IMAGE_CLASSIFICATION_RESNET == infoViewType) {
            val view: View = inflater.inflate(R.layout.info, null, false)
            val infoTextView: TextView = view.findViewById(R.id.info_title)
            val descriptionTextView: TextView = view.findViewById(R.id.info_description)
            infoTextView.setText(R.string.vision_card_resnet_title)
            val sb = StringBuilder(context.getString(R.string.vision_card_resnet_description))
            if (additionalText != null) {
                sb.append('\n').append(additionalText)
            }
            descriptionTextView.setText(sb.toString())
            return view
        } else if (INFO_VIEW_TYPE_IMAGE_CLASSIFICATION_QMOBILENET == infoViewType) {
            val view: View = inflater.inflate(R.layout.info, null, false)
            val infoTextView: TextView = view.findViewById(R.id.info_title)
            val descriptionTextView: TextView = view.findViewById(R.id.info_description)
            infoTextView.setText(R.string.vision_card_qmobilenet_title)
            val sb = StringBuilder(context.getString(R.string.vision_card_qmobilenet_description))
            if (additionalText != null) {
                sb.append('\n').append(additionalText)
            }
            descriptionTextView.setText(sb.toString())
            return view
        } else if (INFO_VIEW_TYPE_TEXT_CLASSIFICATION == infoViewType) {
            val view: View = inflater.inflate(R.layout.info, null, false)
            val infoTextView: TextView = view.findViewById(R.id.info_title)
            val descriptionTextView: TextView = view.findViewById(R.id.info_description)
            infoTextView.setText(R.string.nlp_card_lstm_title)
            descriptionTextView.setText(R.string.nlp_card_lstm_description)
            return view
        }
        throw IllegalArgumentException("Unknown info view type")
    }

    fun newErrorDialogView(context: Context?): View? {
        val inflater: LayoutInflater = LayoutInflater.from(context)
        return inflater.inflate(R.layout.error_dialog, null, false)
    }
}
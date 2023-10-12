package org.pytorch.demo.nlp

import android.content.Intent
import android.os.Bundle
import org.pytorch.demo.AbstractListActivity
import org.pytorch.demo.R
import org.pytorch.demo.vision.ImageClassificationActivity
import org.pytorch.demo.vision.VisionListActivity

class NLPListActivity : AbstractListActivity() {
    @Override
    protected fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        findViewById(R.id.nlp_card_lstm_click_area).setOnClickListener { v ->
            val intent = Intent(this@NLPListActivity, TextClassificationActivity::class.java)
            startActivity(intent)
        }
    }

    @Override
    protected fun getListContentLayoutRes(): Int {
        return R.layout.nlp_list_content
    }
}
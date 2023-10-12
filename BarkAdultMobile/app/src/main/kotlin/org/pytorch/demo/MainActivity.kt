package org.pytorch.demo

import android.content.Intent
import android.os.Bundle
import org.pytorch.demo.nlp.NLPListActivity
import org.pytorch.demo.vision.VisionListActivity
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    @Override
    protected fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById(R.id.main_vision_click_view).setOnClickListener { v ->
            startActivity(
                Intent(
                    this@MainActivity,
                    VisionListActivity::class.java
                )
            )
        }
        findViewById(R.id.main_nlp_click_view).setOnClickListener { v ->
            startActivity(
                Intent(
                    this@MainActivity,
                    NLPListActivity::class.java
                )
            )
        }
    }
}
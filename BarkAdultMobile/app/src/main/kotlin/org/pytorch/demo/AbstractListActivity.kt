package org.pytorch.demo

import android.os.Bundle
import android.view.ViewStub
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity

abstract class AbstractListActivity : AppCompatActivity() {
    @Override
    protected fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_stub)
        findViewById(R.id.list_back).setOnClickListener { v -> finish() }
        val listContentStub: ViewStub = findViewById(R.id.list_content_stub)
        listContentStub.setLayoutResource(getListContentLayoutRes())
        listContentStub.inflate()
    }

    @LayoutRes
    protected abstract fun getListContentLayoutRes(): Int
}
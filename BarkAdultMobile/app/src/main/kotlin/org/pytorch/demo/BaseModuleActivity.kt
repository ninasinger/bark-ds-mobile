package org.pytorch.demo

import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.annotation.UiThread
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class BaseModuleActivity : AppCompatActivity() {
    protected var mBackgroundThread: HandlerThread? = null
    protected var mBackgroundHandler: Handler? = null
    protected var mUIHandler: Handler? = null
    @Override
    protected fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mUIHandler = Handler(getMainLooper())
    }

    @Override
    protected fun onPostCreate(@Nullable savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        if (toolbar != null) {
            setSupportActionBar(toolbar)
        }
        startBackgroundThread()
    }

    protected fun startBackgroundThread() {
        mBackgroundThread = HandlerThread("ModuleActivity")
        mBackgroundThread.start()
        mBackgroundHandler = Handler(mBackgroundThread.getLooper())
    }

    @Override
    protected fun onDestroy() {
        stopBackgroundThread()
        super.onDestroy()
    }

    protected fun stopBackgroundThread() {
        mBackgroundThread.quitSafely()
        try {
            mBackgroundThread.join()
            mBackgroundThread = null
            mBackgroundHandler = null
        } catch (e: InterruptedException) {
            Log.e(Constants.TAG, "Error on stopping background thread", e)
        }
    }

    @Override
    fun onCreateOptionsMenu(menu: Menu?): Boolean {
        getMenuInflater().inflate(R.menu.menu_model, menu)
        menu.findItem(R.id.action_info).setVisible(getInfoViewCode() != UNSET)
        return true
    }

    @Override
    fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item.getItemId() === R.id.action_info) {
            onMenuItemInfoSelected()
        }
        return super.onOptionsItemSelected(item)
    }

    protected fun getInfoViewCode(): Int {
        return UNSET
    }

    protected fun getInfoViewAdditionalText(): String? {
        return null
    }

    private fun onMenuItemInfoSelected() {
        val builder: AlertDialog.Builder = Builder(this)
            .setCancelable(true)
            .setView(
                InfoViewFactory.newInfoView(
                    this,
                    getInfoViewCode(),
                    getInfoViewAdditionalText()
                )
            )
        builder.show()
    }

    @UiThread
    protected fun showErrorDialog(clickListener: View.OnClickListener?) {
        val view: View = InfoViewFactory.newErrorDialogView(this)
        val builder: AlertDialog.Builder = Builder(this, R.style.CustomDialog)
            .setCancelable(false)
            .setView(view)
        val alertDialog: AlertDialog = builder.show()
        view.setOnClickListener { v ->
            clickListener.onClick(v)
            alertDialog.dismiss()
        }
    }

    companion object {
        private const val UNSET = 0
    }
}
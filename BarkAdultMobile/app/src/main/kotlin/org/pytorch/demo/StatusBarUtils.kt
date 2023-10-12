package org.pytorch.demo

import android.view.View
import android.view.Window
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

object StatusBarUtils {
    fun setStatusBarOverlay(window: Window?, showStatusBarAsOverlay: Boolean) {
        val decorView: View = window.getDecorView()
        ViewCompat.setOnApplyWindowInsetsListener(
            decorView
        ) { v, insets ->
            val defaultInsets: WindowInsetsCompat = ViewCompat.onApplyWindowInsets(v, insets)
            defaultInsets.replaceSystemWindowInsets(
                defaultInsets.getSystemWindowInsetLeft(),
                if (showStatusBarAsOverlay) 0 else defaultInsets.getSystemWindowInsetTop(),
                defaultInsets.getSystemWindowInsetRight(),
                defaultInsets.getSystemWindowInsetBottom()
            )
        }
        ViewCompat.requestApplyInsets(decorView)
    }
}
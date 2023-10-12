package org.pytorch.demo

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.tabs.TabLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager

class WelcomeActivity : AppCompatActivity() {
    private var mViewPager: ViewPager? = null
    private var mViewPagerAdapter: PagerAdapter? = null
    private var mTabLayout: TabLayout? = null

    private class PageData(
        private val titleTextResId: Int,
        private val imageResId: Int,
        private val descriptionTextResId: Int
    )

    @Override
    protected fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)
        findViewById(R.id.skip_button).setOnClickListener { v ->
            startActivity(
                Intent(
                    this@WelcomeActivity,
                    MainActivity::class.java
                )
            )
        }
        mViewPager = findViewById(R.id.welcome_view_pager)
        mViewPagerAdapter = WelcomeViewPagerAdapter()
        mViewPager.setAdapter(mViewPagerAdapter)
        mTabLayout = findViewById(R.id.welcome_tab_layout)
        mTabLayout.setupWithViewPager(mViewPager)
    }

    private inner class WelcomeViewPagerAdapter : PagerAdapter() {
        @Override
        fun getCount(): Int {
            return PAGES.size
        }

        @Override
        fun isViewFromObject(@NonNull view: View?, @NonNull `object`: Object?): Boolean {
            return `object` === view
        }

        @NonNull
        fun instantiateItem(@NonNull container: ViewGroup?, position: Int): Object? {
            val inflater: LayoutInflater = LayoutInflater.from(this@WelcomeActivity)
            val pageView: View = inflater.inflate(R.layout.welcome_pager_page, container, false)
            val titleTextView: TextView = pageView.findViewById(R.id.welcome_pager_page_title)
            val descriptionTextView: TextView =
                pageView.findViewById(R.id.welcome_pager_page_description)
            val imageView: ImageView = pageView.findViewById(R.id.welcome_pager_page_image)
            val pageData = PAGES.get(position)
            titleTextView.setText(pageData.titleTextResId)
            descriptionTextView.setText(pageData.descriptionTextResId)
            imageView.setImageResource(pageData.imageResId)
            container.addView(pageView)
            return pageView
        }

        @Override
        fun destroyItem(@NonNull container: ViewGroup?, position: Int, @NonNull `object`: Object?) {
            container.removeView(`object` as View?)
        }
    }

    companion object {
        private val PAGES: Array<PageData?>? = arrayOf<PageData?>(
            PageData(
                R.string.welcome_page_title,
                R.drawable.ic_logo_pytorch,
                R.string.welcome_page_description
            ),
            PageData(
                R.string.welcome_page_image_classification_title,
                R.drawable.ic_image_classification_l,
                R.string.welcome_page_image_classification_description
            ),
            PageData(
                R.string.welcome_page_nlp_title,
                R.drawable.ic_text_classification_l,
                R.string.welcome_page_nlp_description
            )
        )
    }
}
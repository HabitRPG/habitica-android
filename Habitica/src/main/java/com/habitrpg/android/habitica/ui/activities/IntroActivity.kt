package com.habitrpg.android.habitica.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.ContentRepository
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.ui.fragments.setup.IntroFragment
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.viewpagerindicator.IconPageIndicator
import com.viewpagerindicator.IconPagerAdapter
import io.reactivex.functions.Consumer
import javax.inject.Inject

class IntroActivity : BaseActivity(), View.OnClickListener, ViewPager.OnPageChangeListener {

    @Inject
    lateinit var contentRepository: ContentRepository

    private val pager: ViewPager by bindView(R.id.viewPager)
    private val indicator: IconPageIndicator by bindView(R.id.view_pager_indicator)
    private val skipButton: Button by bindView(R.id.skipButton)
    private val finishButton: Button by bindView(R.id.finishButton)

    override fun getLayoutResId(): Int {
        return R.layout.activity_intro
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupIntro()
        indicator.setViewPager(pager)

        this.skipButton.setOnClickListener(this)
        this.finishButton.setOnClickListener(this)

        compositeSubscription.add(contentRepository.retrieveContent(this).subscribe(Consumer { }, RxErrorHandler.handleEmptyError()))
    }

    override fun injectActivity(component: UserComponent?) {
        component?.inject(this)
    }

    private fun setupIntro() {
        val fragmentManager = supportFragmentManager

        pager.adapter = PagerAdapter(fragmentManager)

        pager.addOnPageChangeListener(this)
    }

    override fun onClick(v: View) {
        finishIntro()
    }

    private fun finishIntro() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)

        this.startActivity(intent)
        overridePendingTransition(0, R.anim.activity_fade_out)
        finish()
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

    override fun onPageSelected(position: Int) {
        if (position == 2) {
            this.finishButton.visibility = View.VISIBLE
        } else {
            this.finishButton.visibility = View.GONE
        }
    }

    override fun onPageScrollStateChanged(state: Int) {

    }

    private inner class PagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm), IconPagerAdapter {

        override fun getItem(position: Int): Fragment {
            val fragment = IntroFragment()

            when (position) {
                0 -> {
                    fragment.setImage(ResourcesCompat.getDrawable(resources, R.drawable.intro_1, null))
                    fragment.setSubtitle(getString(R.string.intro_1_subtitle))
                    fragment.setTitleImage(ResourcesCompat.getDrawable(resources, R.drawable.intro_1_title, null))
                    fragment.setDescription(getString(R.string.intro_1_description, getString(R.string.habitica_user_count)))
                    fragment.setBackgroundColor(ContextCompat.getColor(this@IntroActivity, R.color.brand_300))
                }
                1 -> {
                    fragment.setImage(ResourcesCompat.getDrawable(resources, R.drawable.intro_2, null))
                    fragment.setSubtitle(getString(R.string.intro_2_subtitle))
                    fragment.setTitle(getString(R.string.intro_2_title))
                    fragment.setDescription(getString(R.string.intro_2_description))
                    fragment.setBackgroundColor(ContextCompat.getColor(this@IntroActivity, R.color.blue_10))
                }
                2 -> {
                    fragment.setImage(ResourcesCompat.getDrawable(resources, R.drawable.intro_3, null))
                    fragment.setSubtitle(getString(R.string.intro_3_subtitle))
                    fragment.setTitle(getString(R.string.intro_3_title))
                    fragment.setDescription(getString(R.string.intro_3_description))
                    fragment.setBackgroundColor(ContextCompat.getColor(this@IntroActivity, R.color.red_100))
                }
            }

            return fragment
        }

        override fun getIconResId(index: Int): Int {
            return R.drawable.indicator_diamond
        }

        override fun getCount(): Int {
            return 3
        }
    }
}

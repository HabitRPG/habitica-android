package com.habitrpg.android.habitica.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.ContentRepository
import com.habitrpg.android.habitica.databinding.ActivityIntroBinding
import com.habitrpg.android.habitica.ui.fragments.setup.IntroFragment
import com.habitrpg.common.habitica.helpers.ExceptionHandler
import com.viewpagerindicator.IconPagerAdapter
import kotlinx.coroutines.launch
import javax.inject.Inject

class IntroActivity : BaseActivity(), View.OnClickListener, ViewPager.OnPageChangeListener {

    private lateinit var binding: ActivityIntroBinding
    @Inject
    lateinit var contentRepository: ContentRepository

    override fun getLayoutResId(): Int {
        return R.layout.activity_intro
    }

    override fun getContentView(layoutResId: Int?): View {
        binding = ActivityIntroBinding.inflate(layoutInflater)
        return binding.root
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupIntro()
        binding.viewPagerIndicator.setViewPager(binding.viewPager)

        binding.skipButton.setOnClickListener(this)
        binding.finishButton.setOnClickListener(this)

        lifecycleScope.launch(ExceptionHandler.coroutine()) {
            contentRepository.retrieveContent()
        }

        window.statusBarColor = ContextCompat.getColor(this, R.color.black_20_alpha)
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
    }

    override fun injectActivity(component: UserComponent?) {
        component?.inject(this)
    }

    private fun setupIntro() {
        binding.viewPager.adapter = PagerAdapter(supportFragmentManager)

        binding.viewPager.addOnPageChangeListener(this)
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

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) { /* no-on */ }

    override fun onPageSelected(position: Int) {
        if (position == 2) {
            binding.finishButton.visibility = View.VISIBLE
        } else {
            binding.finishButton.visibility = View.GONE
        }
    }

    override fun onPageScrollStateChanged(state: Int) { /* no-on */ }

    private inner class PagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT), IconPagerAdapter {

        override fun getItem(position: Int): Fragment {
            val fragment = IntroFragment()
            configureFragment(fragment, position)
            return fragment
        }

        override fun getIconResId(index: Int): Int {
            return R.drawable.indicator_diamond
        }

        override fun getCount(): Int {
            return 3
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val item = super.instantiateItem(container, position)
            if (item is IntroFragment) {
                configureFragment(item, position)
            }
            return item
        }
    }

    private fun configureFragment(fragment: IntroFragment, position: Int) {
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
    }
}

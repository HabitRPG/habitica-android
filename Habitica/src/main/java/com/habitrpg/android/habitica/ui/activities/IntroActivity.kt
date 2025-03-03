package com.habitrpg.android.habitica.ui.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.ContentRepository
import com.habitrpg.android.habitica.databinding.ActivityIntroBinding
import com.habitrpg.android.habitica.extensions.setNavigationBarDarkIcons
import com.habitrpg.android.habitica.ui.fragments.setup.IntroFragment
import com.habitrpg.common.habitica.helpers.ExceptionHandler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class IntroActivity : BaseActivity() {
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

        binding.skipButton.setOnClickListener { finishIntro() }
        binding.finishButton.setOnClickListener { finishIntro() }

        lifecycleScope.launch(ExceptionHandler.coroutine()) {
            contentRepository.retrieveContent()
        }
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
            val controller = WindowCompat.getInsetsController(window, window.decorView)
            controller.isAppearanceLightNavigationBars = false
            controller.isAppearanceLightStatusBars = false
            window.setNavigationBarDarkIcons(false)
        }
    }

    private fun setupIntro() {
        setViewPagerAdapter()
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position == 2) {
                    binding.finishButton.visibility = View.VISIBLE
                } else {
                    binding.finishButton.visibility = View.GONE
                }
            }
        })
    }

    private fun finishIntro() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)

        this.startActivity(intent)
        overridePendingTransition(0, R.anim.activity_fade_out)
        finish()
    }

    private fun setViewPagerAdapter() {
        val fragmentManager = supportFragmentManager
        val viewPagerAdapter = object : FragmentStateAdapter(fragmentManager, lifecycle) {
            override fun createFragment(position: Int): Fragment {
                val fragment = IntroFragment()
                configureFragment(fragment, position)
                return fragment
            }

            override fun getItemCount(): Int {
                return 3
            }
        }
        binding.viewPager.adapter = viewPagerAdapter
        TabLayoutMediator(binding.viewPagerIndicator, binding.viewPager) { tab, position -> }.attach()
    }

    private fun configureFragment(
        fragment: IntroFragment,
        position: Int
    ) {
        when (position) {
            0 -> {
                fragment.setImage(ResourcesCompat.getDrawable(resources, R.drawable.intro_1, null))
                fragment.setSubtitle(getString(R.string.intro_1_subtitle))
                fragment.setTitleImage(
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.intro_1_title,
                        null
                    )
                )
                fragment.setDescription(
                    getString(
                        R.string.intro_1_description,
                        getString(R.string.habitica_user_count)
                    )
                )
                fragment.setBackgroundColor(
                    ContextCompat.getColor(
                        this@IntroActivity,
                        R.color.brand_300
                    )
                )
            }

            1 -> {
                fragment.setImage(ResourcesCompat.getDrawable(resources, R.drawable.intro_2, null))
                fragment.setSubtitle(getString(R.string.intro_2_subtitle))
                fragment.setTitle(getString(R.string.intro_2_title))
                fragment.setDescription(getString(R.string.intro_2_description))
                fragment.setBackgroundColor(
                    ContextCompat.getColor(
                        this@IntroActivity,
                        R.color.blue_10
                    )
                )
            }

            2 -> {
                fragment.setImage(ResourcesCompat.getDrawable(resources, R.drawable.intro_3, null))
                fragment.setSubtitle(getString(R.string.intro_3_subtitle))
                fragment.setTitle(getString(R.string.intro_3_title))
                fragment.setDescription(getString(R.string.intro_3_description))
                fragment.setBackgroundColor(
                    ContextCompat.getColor(
                        this@IntroActivity,
                        R.color.red_100
                    )
                )
            }
        }
    }
}

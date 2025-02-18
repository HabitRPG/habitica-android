package com.habitrpg.android.habitica.ui.fragments

import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsetsController
import androidx.core.os.bundleOf
import androidx.core.view.WindowCompat
import androidx.viewbinding.ViewBinding
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayout
import com.google.firebase.analytics.FirebaseAnalytics
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.extensions.setNavigationBarDarkIcons
import com.habitrpg.android.habitica.helpers.SoundManager
import com.habitrpg.android.habitica.ui.activities.MainActivity
import com.habitrpg.android.habitica.ui.helpers.ToolbarColorHelper
import com.habitrpg.common.habitica.extensions.getThemeColor
import com.habitrpg.common.habitica.extensions.isUsingNightModeResources
import javax.inject.Inject

abstract class BaseMainFragment<VB : ViewBinding> : BaseFragment<VB>() {
    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var soundManager: SoundManager

    protected var showsBackButton: Boolean = false

    open val mainActivity get() = activity as? MainActivity
    val tabLayout get() = mainActivity?.binding?.content?.detailTabs
    val collapsingToolbar get() = mainActivity?.binding?.content?.toolbar
    val toolbarAccessoryContainer get() = mainActivity?.binding?.content?.toolbarAccessoryContainer
    val bottomNavigation get() = mainActivity?.binding?.content?.bottomNavigation
    var usesTabLayout: Boolean = false
        set(value) {
            field = value
            updateTabLayoutVisibility()
        }
    var hidesToolbar: Boolean = false
    var usesBottomNavigation = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (this.usesBottomNavigation) {
            bottomNavigation?.visibility = View.VISIBLE
        } else {
            bottomNavigation?.visibility = View.GONE
        }

        setHasOptionsMenu(true)

        tabLayout?.removeAllTabs()
        updateTabLayoutVisibility()
        updateToolbarInteractivity()

        if (hidesToolbar) {
            hideToolbar()
            disableToolbarScrolling()
        } else {
            showToolbar()
            enableToolbarScrolling()
        }
        context?.let {
            FirebaseAnalytics.getInstance(it).logEvent(
                "fragment_view",
                bundleOf(Pair("fragment", this::class.java.canonicalName))
            )
        }

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onResume() {
        super.onResume()
        mainActivity?.showBackButton = showsBackButton
        mainActivity?.supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val window = activity?.window
        if (window != null) {
            val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
            if (this.usesBottomNavigation) {
                windowInsetsController.isAppearanceLightNavigationBars = false
                view?.systemUiVisibility
                window.setNavigationBarDarkIcons(false)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    window.isNavigationBarContrastEnforced = false
                }
            } else {
                windowInsetsController.isAppearanceLightNavigationBars = requireActivity().isUsingNightModeResources()
                window.setNavigationBarDarkIcons(!requireActivity().isUsingNightModeResources())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    window.isNavigationBarContrastEnforced = true
                }
            }
        }
    }

    @Deprecated("Use onCreateOptionsMenu(Menu, MenuInflater) instead")
    override fun onCreateOptionsMenu(
        menu: Menu,
        inflater: MenuInflater
    ) {
        super.onCreateOptionsMenu(menu, inflater)
        mainActivity?.toolbar?.let { ToolbarColorHelper.colorizeToolbar(it, mainActivity) }
        updateToolbarInteractivity()
    }

    var isTitleInteractive = false

    open fun updateToolbarInteractivity() {
        mainActivity?.updateToolbarInteractivity(isTitleInteractive)
    }

    private fun updateTabLayoutVisibility() {
        if (this.usesTabLayout) {
            tabLayout?.visibility = View.VISIBLE
            tabLayout?.tabMode = TabLayout.MODE_FIXED
        } else {
            tabLayout?.visibility = View.GONE
        }
    }

    override fun onDestroy() {
        userRepository.close()
        super.onDestroy()
    }

    private fun hideToolbar() {
        mainActivity?.binding?.content?.headerView?.visibility = View.GONE
    }

    private fun showToolbar() {
        mainActivity?.binding?.content?.headerView?.visibility = View.VISIBLE
    }

    private fun disableToolbarScrolling() {
        val params = collapsingToolbar?.layoutParams as? AppBarLayout.LayoutParams
        params?.scrollFlags = 0
    }

    private fun enableToolbarScrolling() {
        val params = collapsingToolbar?.layoutParams as? AppBarLayout.LayoutParams
        params?.scrollFlags =
            AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED
    }

    protected fun tintMenuIcon(item: MenuItem?) {
        context?.getThemeColor(R.attr.headerTextColor)?.let {
            item?.icon?.setTint(it)
            item?.icon?.setTintMode(PorterDuff.Mode.MULTIPLY)
        }
    }
}

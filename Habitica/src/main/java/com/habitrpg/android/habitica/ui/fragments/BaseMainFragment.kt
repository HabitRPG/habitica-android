package com.habitrpg.android.habitica.ui.fragments

import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.setPadding
import androidx.viewbinding.ViewBinding
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayout
import com.google.firebase.analytics.FirebaseAnalytics
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.extensions.setScaledPadding
import com.habitrpg.android.habitica.helpers.SoundManager
import com.habitrpg.android.habitica.ui.activities.MainActivity
import com.habitrpg.android.habitica.ui.helpers.ToolbarColorHelper
import com.habitrpg.common.habitica.extensions.getThemeColor
import javax.inject.Inject

abstract class BaseMainFragment<VB : ViewBinding> : BaseFragment<VB>() {
    @Inject
    lateinit var userRepository: UserRepository
    @Inject
    lateinit var soundManager: SoundManager

    protected var showsBackButton: Boolean = false

    open val mainActivity get() = getActivity() as? MainActivity
    val tabLayout get() = mainActivity?.binding?.content?.detailTabs
    val collapsingToolbar get() = mainActivity?.binding?.content?.toolbar
    val toolbarAccessoryContainer get() = mainActivity?.binding?.content?.toolbarAccessoryContainer
    val bottomNavigation get() = mainActivity?.binding?.content?.bottomNavigation
    var usesTabLayout: Boolean = false
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
            FirebaseAnalytics.getInstance(it).logEvent("fragment_view", bundleOf(Pair("fragment", this::class.java.canonicalName)))
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
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        mainActivity?.toolbar?.let { ToolbarColorHelper.colorizeToolbar(it, mainActivity) }
        updateToolbarInteractivity()
    }

    var isTitleInteractive = false

    open fun updateToolbarInteractivity() {
        mainActivity?.binding?.content?.toolbarTitle?.background?.alpha = if (isTitleInteractive) 255 else 0
        if (isTitleInteractive) {
            mainActivity?.binding?.content?.toolbarTitle?.setScaledPadding(context, 16, 4, 16, 4)
        } else {
            mainActivity?.binding?.content?.toolbarTitle?.setPadding(0)
        }
    }

    private fun updateTabLayoutVisibility() {
        if (this.usesTabLayout) {
            tabLayout?.removeAllTabs()
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
        params?.scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED
    }

    protected fun tintMenuIcon(item: MenuItem?) {
        context?.getThemeColor(R.attr.headerTextColor)?.let {
            item?.icon?.setTint(it)
            item?.icon?.setTintMode(PorterDuff.Mode.MULTIPLY)
        }
    }
}

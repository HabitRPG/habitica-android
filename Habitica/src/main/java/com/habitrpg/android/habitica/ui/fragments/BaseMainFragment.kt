package com.habitrpg.android.habitica.ui.fragments

import android.content.Context
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.viewbinding.ViewBinding
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayout
import com.google.firebase.analytics.FirebaseAnalytics
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.extensions.getThemeColor
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.helpers.SoundManager
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.activities.MainActivity
import com.habitrpg.android.habitica.ui.helpers.ToolbarColorHelper
import javax.inject.Inject

abstract class BaseMainFragment<VB: ViewBinding> : BaseFragment<VB>() {

    @Inject
    lateinit var apiClient: ApiClient
    @Inject
    lateinit var userRepository: UserRepository
    @Inject
    lateinit var soundManager: SoundManager

    protected var showsBackButton: Boolean = false

    open val activity get() = getActivity() as? MainActivity
    val tabLayout get() = activity?.binding?.detailTabs
    val collapsingToolbar get() = activity?.binding?.toolbar
    val toolbarAccessoryContainer get() = activity?.binding?.toolbarAccessoryContainer
    val bottomNavigation get() = activity?.binding?.bottomNavigation
    var usesTabLayout: Boolean = false
    var hidesToolbar: Boolean = false
    var usesBottomNavigation = false
    open var user: User? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (getActivity()?.javaClass == MainActivity::class.java) {
            user = activity?.user
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        compositeSubscription.add(userRepository.getUser().subscribe({ user = it }, RxErrorHandler.handleEmptyError()))

        if (this.usesBottomNavigation) {
            bottomNavigation?.visibility = View.VISIBLE
        } else {
            bottomNavigation?.visibility = View.GONE
        }

        setHasOptionsMenu(true)

        updateTabLayoutVisibility()

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
        activity?.drawerToggle?.isDrawerIndicatorEnabled = !showsBackButton
        activity?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        activity?.toolbar?.let { ToolbarColorHelper.colorizeToolbar(it, activity, null) }
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
        activity?.binding?.avatarWithBars?.root?.visibility = View.GONE
    }

    private fun showToolbar() {
        activity?.binding?.avatarWithBars?.root?.visibility = View.VISIBLE
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

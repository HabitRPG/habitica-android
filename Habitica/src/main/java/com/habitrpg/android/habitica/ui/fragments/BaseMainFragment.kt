package com.habitrpg.android.habitica.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayout
import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.extensions.dpToPx
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.helpers.SoundManager
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.activities.MainActivity
import io.reactivex.functions.Consumer
import javax.inject.Inject

abstract class BaseMainFragment : BaseFragment() {

    @Inject
    lateinit var apiClient: ApiClient
    @Inject
    lateinit var userRepository: UserRepository
    @Inject
    lateinit var soundManager: SoundManager

    open val activity get() = getActivity() as? MainActivity
    val tabLayout get() = activity?.detailTabs
    val collapsingToolbar get() = activity?.toolbar
    val toolbarAccessoryContainer get() = activity?.toolbarAccessoryContainer
    val bottomNavigation get() = activity?.bottomNavigation
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
        super.onCreateView(inflater, container, savedInstanceState)
        compositeSubscription.add(userRepository.getUser().subscribe(Consumer { user = it }, RxErrorHandler.handleEmptyError()))

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

        return null
    }

    override fun onResume() {
        super.onResume()
        if (this.usesBottomNavigation) {
            activity?.snackbarContainer?.setPadding(0, 0, 0, (bottomNavigation?.barHeight ?: 0) + 12.dpToPx(context))
        } else {
            activity?.snackbarContainer?.setPadding(0, 0, 0, 0)
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
        activity?.avatarWithBars?.visibility = View.GONE
    }

    private fun showToolbar() {
        activity?.avatarWithBars?.visibility = View.VISIBLE
    }

    private fun disableToolbarScrolling() {
        val params = collapsingToolbar?.layoutParams as? AppBarLayout.LayoutParams
        params?.scrollFlags = 0
    }

    private fun enableToolbarScrolling() {
        val params = collapsingToolbar?.layoutParams as? AppBarLayout.LayoutParams
        params?.scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED
    }
}

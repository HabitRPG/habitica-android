package com.habitrpg.android.habitica.ui.fragments

import android.content.Context
import android.os.Bundle
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.tabs.TabLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout

import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.helpers.SoundManager
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.activities.MainActivity
import com.habitrpg.android.habitica.ui.views.bottombar.BottomBar
import io.reactivex.functions.Consumer

import javax.inject.Inject

abstract class BaseMainFragment : BaseFragment() {

    @Inject
    lateinit var apiClient: ApiClient
    @Inject
    lateinit var userRepository: UserRepository
    @Inject
    lateinit var soundManager: SoundManager

    open var activity: MainActivity? = null
    var tabLayout: TabLayout? = null
    var collapsingToolbar: CollapsingToolbarLayout? = null
    var toolbarAccessoryContainer: FrameLayout? = null
    var bottomNavigation: BottomBar? = null
    var floatingMenuWrapper: ViewGroup? = null
    var usesTabLayout: Boolean = false
    var usesBottomNavigation = false
    var fragmentSidebarIdentifier: String? = null
    open var user: User? = null

    open fun updateUserData(user: User?) {
        this.user = user
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        if (getActivity()?.javaClass == MainActivity::class.java) {
            this.activity = getActivity() as? MainActivity
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        if (savedInstanceState != null && savedInstanceState.containsKey("userId")) {
            val userId = savedInstanceState.getString("userId")
            if (userId != null) {
                compositeSubscription.add(userRepository.getUser(userId).subscribe(Consumer { habitRPGUser -> user = habitRPGUser }, RxErrorHandler.handleEmptyError()))
            }
        }

        if (this.usesBottomNavigation) {
            bottomNavigation?.removeOnTabSelectListener()
            bottomNavigation?.removeOnTabReselectListener()
            bottomNavigation?.visibility = View.VISIBLE
        } else {
            bottomNavigation?.visibility = View.GONE
        }

        floatingMenuWrapper?.removeAllViews()

        setHasOptionsMenu(true)

        activity?.setActiveFragment(this)

        updateTabLayoutVisibility()

        return null
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

    override fun onSaveInstanceState(outState: Bundle) {
        if (user?.isValid == true) {
            outState.putString("userId", user?.id)
        }

        super.onSaveInstanceState(outState)
    }

    open fun customTitle(): String {
        return ""
    }

    fun hideToolbar() {
        activity?.avatarWithBars?.visibility = View.GONE
    }

    fun showToolbar() {
        activity?.avatarWithBars?.visibility = View.VISIBLE
    }

    fun disableToolbarScrolling() {
        val params = collapsingToolbar?.layoutParams as? AppBarLayout.LayoutParams
        params?.scrollFlags = 0
    }

    fun enableToolbarScrolling() {
        val params = collapsingToolbar?.layoutParams as? AppBarLayout.LayoutParams
        params?.scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED
    }
}

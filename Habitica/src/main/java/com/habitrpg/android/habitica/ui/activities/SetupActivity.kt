package com.habitrpg.android.habitica.ui.activities

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.preference.PreferenceManager
import androidx.viewpager.widget.ViewPager
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.api.HostConfig
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.data.TaskRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.helpers.AmplitudeManager
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.fragments.setup.AvatarSetupFragment
import com.habitrpg.android.habitica.ui.fragments.setup.TaskSetupFragment
import com.habitrpg.android.habitica.ui.fragments.setup.WelcomeFragment
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.android.habitica.ui.views.FadingViewPager
import com.viewpagerindicator.IconPageIndicator
import com.viewpagerindicator.IconPagerAdapter
import io.reactivex.BackpressureStrategy
import io.reactivex.functions.Consumer
import java.util.*
import javax.inject.Inject

class SetupActivity : BaseActivity(), ViewPager.OnPageChangeListener {

    @Inject
    lateinit var apiClient: ApiClient
    @Inject
    lateinit var hostConfig: HostConfig
    @Inject
    lateinit var userRepository: UserRepository
    @Inject
    lateinit var inventoryRepository: InventoryRepository
    @Inject
    lateinit var taskRepository: TaskRepository

    private val pager: FadingViewPager by bindView(R.id.viewPager)
    private val nextButton: Button by bindView(R.id.nextButton)
    private val previousButton: Button by bindView(R.id.previousButton)
    private val indicator: IconPageIndicator by bindView(R.id.view_pager_indicator)

    internal var welcomeFragment: WelcomeFragment? = null
    internal var avatarSetupFragment: AvatarSetupFragment? = null
    internal var taskSetupFragment: TaskSetupFragment? = null
    internal var user: User? = null
    private var completedSetup = false

    private val isLastPage: Boolean
        get() = this.pager.adapter == null || this.pager.currentItem == (this.pager.adapter?.count ?: 0) - 1

    override fun getLayoutResId(): Int {
        return R.layout.activity_setup
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        compositeSubscription.add(userRepository.getUser(hostConfig.userID)
                .subscribe(Consumer { this.onUserReceived(it) }, RxErrorHandler.handleEmptyError()))

        val additionalData = HashMap<String, Any>()
        additionalData["status"] = "displayed"
        AmplitudeManager.sendEvent("setup", AmplitudeManager.EVENT_CATEGORY_BEHAVIOUR, AmplitudeManager.EVENT_HITTYPE_EVENT, additionalData)

        val currentDeviceLanguage = Locale.getDefault().language
        for (language in resources.getStringArray(R.array.LanguageValues)) {
            if (language == currentDeviceLanguage) {
                compositeSubscription.add(apiClient.registrationLanguage(currentDeviceLanguage)
                        .subscribe(Consumer { }, RxErrorHandler.handleEmptyError()))
            }
        }

        val window = window
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val decor = getWindow().decorView
            decor.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            window.statusBarColor = ContextCompat.getColor(this, R.color.light_gray_bg)
        } else {
            window.statusBarColor = ContextCompat.getColor(this, R.color.days_gray)
        }

        pager.disableFading = true

        previousButton.setOnClickListener { previousClicked() }
        nextButton.setOnClickListener { nextClicked() }
    }

    override fun injectActivity(component: UserComponent?) {
        component?.inject(this)
    }

    override fun onDestroy() {
        userRepository.close()
        super.onDestroy()
    }

    private fun setupViewpager() {
        val fragmentManager = supportFragmentManager

        pager.adapter = ViewPageAdapter(fragmentManager)

        pager.addOnPageChangeListener(this)
        indicator.setViewPager(pager)
    }

    private fun nextClicked() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        sharedPreferences.edit {
            putString("FirstDayOfTheWeek", Integer.toString(Calendar.getInstance().firstDayOfWeek))
        }
        if (isLastPage) {
            if (this.taskSetupFragment == null) {
                return
            }
            val newTasks = this.taskSetupFragment?.createSampleTasks()
            this.completedSetup = true
            newTasks?.let {
                this.taskRepository.createTasks(it).subscribe(Consumer { onUserReceived(user) }, RxErrorHandler.handleEmptyError())
            }
        } else if (pager.currentItem == 0) {

            confirmNames(welcomeFragment?.displayName ?: "", welcomeFragment?.username ?: "")

            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        }
        this.pager.currentItem = this.pager.currentItem + 1
    }

    private fun previousClicked() {
        this.pager.currentItem = this.pager.currentItem - 1
    }

    private fun setPreviousButtonEnabled(enabled: Boolean) {
        val leftDrawable: Drawable?
        if (enabled) {
            previousButton.setText(R.string.action_back)
            leftDrawable = AppCompatResources.getDrawable(this, R.drawable.back_arrow_enabled)
        } else {
            previousButton.text = null
            leftDrawable = AppCompatResources.getDrawable(this, R.drawable.back_arrow_disabled)
        }
        previousButton.setCompoundDrawablesWithIntrinsicBounds(leftDrawable, null, null, null)
    }

    private fun setNextButtonEnabled(enabled: Boolean) {
        nextButton.isEnabled = enabled
        val rightDrawable = AppCompatResources.getDrawable(this, R.drawable.forward_arrow_enabled)
        if (enabled) {
            nextButton.setTextColor(ContextCompat.getColor(this, R.color.white))
            rightDrawable?.alpha = 255
        } else {
            nextButton.setTextColor(ContextCompat.getColor(this, R.color.white_50_alpha))
            rightDrawable?.alpha = 127
        }
        nextButton.setCompoundDrawablesWithIntrinsicBounds(null, null, rightDrawable, null)
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

    }

    override fun onPageSelected(position: Int) {
        when {
            position == 0 -> {
                this.setPreviousButtonEnabled(false)
                this.nextButton.text = this.getString(R.string.next_button)
            }
            isLastPage -> {
                this.setPreviousButtonEnabled(true)
                this.nextButton.text = this.getString(R.string.intro_finish_button)
            }
            else -> {
                this.setPreviousButtonEnabled(true)
                this.nextButton.text = this.getString(R.string.next_button)
            }
        }
    }

    override fun onPageScrollStateChanged(state: Int) {

    }

    private fun onUserReceived(user: User?) {
        if (completedSetup) {
            if (!compositeSubscription.isDisposed) {
                compositeSubscription.dispose()
            }
            this.startMainActivity()
            return
        }
        this.user = user
        if (this.pager.adapter == null) {
            this.setupViewpager()
        } else {
            this.avatarSetupFragment?.setUser(user)
            this.taskSetupFragment?.setUser(user)
        }

        val additionalData = HashMap<String, Any>()
        additionalData["status"] = "completed"
        AmplitudeManager.sendEvent("setup", AmplitudeManager.EVENT_CATEGORY_BEHAVIOUR, AmplitudeManager.EVENT_HITTYPE_EVENT, additionalData)
    }

    private fun startMainActivity() {
        val intent = Intent(this@SetupActivity, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    private fun confirmNames(displayName: String, username: String) {
        compositeSubscription.add(userRepository.updateUser(null, "profile.name", displayName)
                .flatMap { userRepository.updateLoginName(username).toFlowable() }
                .subscribe(Consumer {  }, RxErrorHandler.handleEmptyError()))
    }

    private inner class ViewPageAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm), IconPagerAdapter {

        override fun getItem(position: Int): Fragment {
            return when (position) {
                1 -> {
                    val fragment = AvatarSetupFragment()
                    fragment.activity = this@SetupActivity
                    fragment.setUser(user)
                    fragment.width = pager.width
                    avatarSetupFragment = fragment
                    fragment
                }
                2 -> {
                    val fragment = TaskSetupFragment()
                    fragment.setUser(user)
                    taskSetupFragment = fragment
                    fragment
                }
                else -> {
                    val fragment = WelcomeFragment()
                    welcomeFragment = fragment
                    welcomeFragment?.nameValidEvents?.toFlowable(BackpressureStrategy.DROP)?.subscribe {
                        setNextButtonEnabled(it)
                    }
                    fragment
                }
            }
        }

        override fun getCount(): Int {
            return 3
        }

        override fun getIconResId(index: Int): Int {
            return R.drawable.indicator_diamond
        }
    }
}

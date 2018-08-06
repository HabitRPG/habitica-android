package com.habitrpg.android.habitica.ui.activities

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.support.v7.content.res.AppCompatResources
import android.support.v7.preference.PreferenceManager
import android.view.View
import android.widget.Button
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.api.HostConfig
import com.habitrpg.android.habitica.components.AppComponent
import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.TaskRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.events.commands.EquipCommand
import com.habitrpg.android.habitica.events.commands.UpdateUserCommand
import com.habitrpg.android.habitica.extensions.notNull
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
import io.reactivex.functions.Consumer
import org.greenrobot.eventbus.Subscribe
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
    lateinit var taskRepository: TaskRepository

    private val pager: FadingViewPager by bindView(R.id.viewPager)
    private val nextButton: Button by bindView(R.id.nextButton)
    private val previousButton: Button by bindView(R.id.previousButton)
    private val indicator: IconPageIndicator by bindView(R.id.view_pager_indicator)

    internal var avatarSetupFragment: AvatarSetupFragment? = null
    internal var taskSetupFragment: TaskSetupFragment? = null
    internal var user: User? = null
    private var completedSetup = false

    private val isLastPage: Boolean
        get() = this.pager.adapter == null || this.pager.currentItem == this.pager.adapter?.count ?: 0 - 1

    override fun getLayoutResId(): Int {
        return R.layout.activity_setup
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        compositeSubscription.add(userRepository.getUser(hostConfig.user)
                .subscribe(Consumer { this.onUserReceived(it) }, RxErrorHandler.handleEmptyError()))

        val additionalData = HashMap<String, Any>()
        additionalData["status"] = "displayed"
        AmplitudeManager.sendEvent("setup", AmplitudeManager.EVENT_CATEGORY_BEHAVIOUR, AmplitudeManager.EVENT_HITTYPE_EVENT, additionalData)

        val currentDeviceLanguage = Locale.getDefault().language
        for (language in resources.getStringArray(R.array.LanguageValues)) {
            if (language == currentDeviceLanguage) {
                apiClient.registrationLanguage(currentDeviceLanguage)
                        .subscribe(Consumer { }, RxErrorHandler.handleEmptyError())
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = window
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val decor = getWindow().decorView
                decor.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                window.statusBarColor = ContextCompat.getColor(this, R.color.light_gray_bg)
            } else {
                window.statusBarColor = ContextCompat.getColor(this, R.color.days_gray)
            }
        }

        pager.disableFading = true

        previousButton.setOnClickListener { previousClicked() }
        nextButton.setOnClickListener { nextClicked() }
    }

    override fun injectActivity(component: AppComponent?) {
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

    @Subscribe
    fun onEvent(event: UpdateUserCommand) {
        this.userRepository.updateUser(user, event.updateData)
                .subscribe(Consumer<User> { this.onUserReceived(it) }, RxErrorHandler.handleEmptyError())
    }

    @Subscribe
    fun onEvent(event: EquipCommand) {
        this.apiClient.equipItem(event.type, event.key)
                .subscribe(Consumer { }, RxErrorHandler.handleEmptyError())
    }

    private fun nextClicked() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = sharedPreferences.edit()
        editor.putString("FirstDayOfTheWeek", Integer.toString(Calendar.getInstance().firstDayOfWeek))
        editor.apply()
        if (isLastPage) {
            if (this.taskSetupFragment == null) {
                return
            }
            val newTasks = this.taskSetupFragment?.createSampleTasks()
            this.completedSetup = true
            newTasks.notNull {
                this.taskRepository.createTasks(it).subscribe(Consumer { onUserReceived(user) }, RxErrorHandler.handleEmptyError())
            }
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
                else -> { WelcomeFragment() }
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

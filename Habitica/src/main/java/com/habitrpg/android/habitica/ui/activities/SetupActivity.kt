package com.habitrpg.android.habitica.ui.activities

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.data.TaskRepository
import com.habitrpg.android.habitica.databinding.ActivitySetupBinding
import com.habitrpg.android.habitica.extensions.consumeWindowInsetsAbove30
import com.habitrpg.android.habitica.helpers.Analytics
import com.habitrpg.android.habitica.helpers.EventCategory
import com.habitrpg.android.habitica.helpers.HitType
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.fragments.setup.AvatarSetupFragment
import com.habitrpg.android.habitica.ui.fragments.setup.TaskSetupFragment
import com.habitrpg.android.habitica.ui.fragments.setup.WelcomeFragment
import com.habitrpg.common.habitica.extensions.dpToPx
import com.habitrpg.common.habitica.helpers.ExceptionHandler
import com.habitrpg.common.habitica.helpers.launchCatching
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@AndroidEntryPoint
class SetupActivity : BaseActivity() {
    private lateinit var binding: ActivitySetupBinding

    @Inject
    lateinit var apiClient: ApiClient

    @Inject
    lateinit var inventoryRepository: InventoryRepository

    @Inject
    lateinit var taskRepository: TaskRepository

    internal var welcomeFragment: WelcomeFragment? = null
    internal var avatarSetupFragment: AvatarSetupFragment? = null
    internal var taskSetupFragment: TaskSetupFragment? = null
    internal var user: User? = null
    private var completedSetup = false
    private var createdTasks = false

    private val isLastPage: Boolean
        get() =
            binding.viewPager.adapter == null || binding.viewPager.currentItem == (
                binding.viewPager.adapter?.itemCount
                    ?: 0
                ) - 1

    override fun getLayoutResId(): Int {
        return R.layout.activity_setup
    }

    override fun getContentView(layoutResId: Int?): View {
        binding = ActivitySetupBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch(ExceptionHandler.coroutine()) {
            userRepository.getUser()
                .debounce(500.milliseconds)
                .collect { onUserReceived(it) }
        }
        lifecycleScope.launch(ExceptionHandler.coroutine()) {
            userRepository.retrieveUser(true, true)
        }
        val additionalData = HashMap<String, Any>()
        additionalData["status"] = "displayed"
        Analytics.sendEvent("setup", EventCategory.BEHAVIOUR, HitType.EVENT, additionalData)

        val currentDeviceLanguage = Locale.getDefault().language
        for (language in resources.getStringArray(R.array.LanguageValues)) {
            if (language == currentDeviceLanguage) {
                lifecycleScope.launchCatching {
                    apiClient.registrationLanguage(currentDeviceLanguage)
                }
            }
        }

        binding.viewPager.isUserInputEnabled = false

        binding.previousButton.setOnClickListener { previousClicked() }
        binding.nextButton.setOnClickListener { nextClicked() }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, windowInsets ->
            val insets = windowInsets.getInsets(
                WindowInsetsCompat.Type.systemBars()
                    + WindowInsetsCompat.Type.displayCutout()
            )
            binding.viewPager.updatePadding(
                left = insets.left,
                right = insets.right,
                top = insets.top
            )
            binding.bottomBar.updatePadding(
                bottom = insets.bottom
            )
            binding.bottomBar.layoutParams.height = 56.dpToPx(this) + insets.bottom
            consumeWindowInsetsAbove30(windowInsets)
        }
    }

    override fun onDestroy() {
        userRepository.close()
        super.onDestroy()
    }

    private fun setupViewpager() {
        setViewPagerAdapter()
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when {
                    position == 0 -> {
                        setPreviousButtonEnabled(false)
                        binding.nextButton.text = getString(R.string.next_button)
                    }
                    isLastPage -> {
                        setPreviousButtonEnabled(true)
                        binding.nextButton.text = getString(R.string.finish)
                    }
                    else -> {
                        setPreviousButtonEnabled(true)
                        binding.nextButton.text = getString(R.string.next_button)
                    }
                }
            }
        })
    }

    private fun nextClicked() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        sharedPreferences.edit {
            putString("FirstDayOfTheWeek", Calendar.getInstance().firstDayOfWeek.toString())
        }
        if (isLastPage) {
            if (this.taskSetupFragment == null) {
                return
            }
            if (createdTasks) {
                onUserReceived(user)
                return
            }
            val newTasks = this.taskSetupFragment?.createSampleTasks()
            this.completedSetup = true
            createdTasks = true
            newTasks?.let {
                lifecycleScope.launchCatching {
                    taskRepository.createTasks(it)
                }
            }
        } else if (binding.viewPager.currentItem == 0) {
            confirmNames(welcomeFragment?.displayName ?: "", welcomeFragment?.username ?: "")

            val imm = getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        }
        binding.viewPager.currentItem = binding.viewPager.currentItem + 1
    }

    private fun previousClicked() {
        binding.viewPager.currentItem = binding.viewPager.currentItem - 1
    }

    private fun setPreviousButtonEnabled(enabled: Boolean) {
        val leftDrawable: Drawable?
        if (enabled) {
            binding.previousButton.setText(R.string.action_back)
            leftDrawable = AppCompatResources.getDrawable(this, R.drawable.back_arrow_enabled)
        } else {
            binding.previousButton.text = null
            leftDrawable = AppCompatResources.getDrawable(this, R.drawable.back_arrow_disabled)
        }
        binding.previousButton.setCompoundDrawablesWithIntrinsicBounds(
            leftDrawable,
            null,
            null,
            null
        )
    }

    private fun setNextButtonEnabled(enabled: Boolean) {
        binding.nextButton.isEnabled = enabled
        val rightDrawable = AppCompatResources.getDrawable(this, R.drawable.forward_arrow_enabled)
        if (enabled) {
            binding.nextButton.setTextColor(ContextCompat.getColor(this, R.color.white))
            rightDrawable?.alpha = 255
        } else {
            binding.nextButton.setTextColor(ContextCompat.getColor(this, R.color.white_50_alpha))
            rightDrawable?.alpha = 127
        }
        binding.nextButton.setCompoundDrawablesWithIntrinsicBounds(null, null, rightDrawable, null)
    }

    private var hasCompleted = false

    private fun onUserReceived(user: User?) {
        if (completedSetup && !hasCompleted) {
            val additionalData = HashMap<String, Any>()
            additionalData["status"] = "completed"
            Analytics.sendEvent("setup", EventCategory.BEHAVIOUR, HitType.EVENT, additionalData)
            hasCompleted = true
            lifecycleScope.launchCatching {
                userRepository.updateUser("flags.welcomed", true)
                userRepository.retrieveUser(true, true)
                startMainActivity()
            }
            return
        }
        this.user = user
        if (binding.viewPager.adapter == null) {
            setupViewpager()
        }
        avatarSetupFragment?.setUser(user)
        taskSetupFragment?.setUser(user)
    }

    private fun startMainActivity() {
        val intent = Intent(this@SetupActivity, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    private fun confirmNames(
        displayName: String,
        username: String
    ) {
        lifecycleScope.launch(ExceptionHandler.coroutine()) {
            userRepository.updateUser("profile.name", displayName)
            userRepository.updateLoginName(username)
        }
    }

    private fun setViewPagerAdapter() {
        val fragmentManager = supportFragmentManager
        val viewPagerAdapter = object : FragmentStateAdapter(fragmentManager, lifecycle) {
            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    1 -> {
                        val fragment = AvatarSetupFragment()
                        fragment.activity = this@SetupActivity
                        fragment.setUser(user)
                        fragment.width = binding.viewPager.width
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
                        welcomeFragment?.onNameValid = { setNextButtonEnabled(it == true) }
                        fragment
                    }
                }
            }

            override fun getItemCount(): Int {
                return 3
            }
        }
        binding.viewPager.adapter = viewPagerAdapter
        TabLayoutMediator(binding.viewPagerIndicator, binding.viewPager) { tab, position -> }.attach()
    }
}

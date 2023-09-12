package com.habitrpg.android.habitica.ui.activities

import android.content.SharedPreferences
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import com.habitrpg.android.habitica.HabiticaApplication
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.databinding.ActivityDeathBinding
import com.habitrpg.android.habitica.extensions.DateUtils
import com.habitrpg.android.habitica.extensions.getShortRemainingString
import com.habitrpg.android.habitica.extensions.observeOnce
import com.habitrpg.android.habitica.helpers.AdHandler
import com.habitrpg.android.habitica.helpers.AdType
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.ui.fragments.purchases.EventOutcomeSubscriptionBottomSheetFragment
import com.habitrpg.android.habitica.ui.viewmodels.MainUserViewModel
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar
import com.habitrpg.android.habitica.ui.views.SnackbarActivity
import com.habitrpg.android.habitica.ui.views.ads.AdButton
import com.habitrpg.common.habitica.extensions.fromHtml
import com.habitrpg.common.habitica.helpers.Animations
import com.habitrpg.common.habitica.helpers.ExceptionHandler
import com.habitrpg.common.habitica.helpers.launchCatching
import com.plattysoft.leonids.ParticleSystem
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

@AndroidEntryPoint
class DeathActivity : BaseActivity(), SnackbarActivity {
    private lateinit var binding: ActivityDeathBinding

    @Inject
    internal lateinit var inventoryRepository: InventoryRepository

    @Inject
    internal lateinit var appConfigManager: AppConfigManager

    @Inject
    lateinit var userViewModel: MainUserViewModel

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    override fun getLayoutResId(): Int = R.layout.activity_death

    override fun getContentView(layoutResId: Int?): View {
        binding = ActivityDeathBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userViewModel.user.observeOnce(this) { user ->
            binding.lossDescription.text = getString(R.string.faint_loss_description, (user?.stats?.lvl ?: 2).toInt() - 1, user?.stats?.gp?.toInt()).fromHtml()
        }

        if (appConfigManager.enableFaintAds()) {
            val handler = AdHandler(this, AdType.FAINT) {
                if (!it) {
                    return@AdHandler
                }
                lifecycleScope.launch(ExceptionHandler.coroutine()) {
                    userRepository.updateUser("stats.hp", 1)
                    finish()
                }
            }
            handler.prepare {
                if (it && binding.adButton.state == AdButton.State.LOADING) {
                    binding.adButton.state = AdButton.State.READY
                } else if (!it) {
                    binding.adButton.visibility = View.INVISIBLE
                }
            }
            binding.adButton.updateForAdType(AdType.FAINT, lifecycleScope)
            binding.adButton.setOnClickListener {
                binding.adButton.state = AdButton.State.LOADING
                handler.show()
            }
        } else {
            binding.adButton.visibility = View.GONE
        }

        if (appConfigManager.enableFaintSubs()) {
            userViewModel.user.observe(this) {
                if (it?.isSubscribed == true && binding.reviveSubscriberWrapper.visibility != View.INVISIBLE) {
                    val lastRevive = Date(sharedPreferences.getLong("last_sub_revive", 0L))
                    if (DateUtils.isSameDay(Date(), lastRevive)) {
                        binding.reviveSubscriberWrapper.visibility = View.GONE
                        binding.subscriberBenefitUsedView.visibility = View.VISIBLE
                        lifecycleScope.launchCatching {
                            val date: Calendar = Calendar.getInstance()
                            date.set(Calendar.HOUR_OF_DAY, 0)
                            date.set(Calendar.MINUTE, 0)
                            date.set(Calendar.SECOND, 0)
                            date.add(Calendar.DAY_OF_MONTH, 1)
                            val midnight = date.time
                            while (true) {
                                binding.subscriberBenefitUsedView.text = getString(R.string.subscriber_benefit_used_faint, midnight.getShortRemainingString())
                                delay(1000L)
                            }
                        }
                    } else {
                        binding.reviveSubscriberWrapper.visibility = View.VISIBLE
                        binding.subscriberBenefitUsedView.visibility = View.GONE
                    }
                    binding.unsubbedWrapper.visibility = View.GONE
                } else if (it?.isSubscribed == false) {
                    binding.reviveSubscriberWrapper.visibility = View.GONE
                    binding.unsubbedWrapper.visibility = View.VISIBLE
                    binding.subscribeModalButton.setOnClickListener {
                        val subscriptionBottomSheet = EventOutcomeSubscriptionBottomSheetFragment().apply {
                            eventType = EventOutcomeSubscriptionBottomSheetFragment.EVENT_DEATH_SCREEN
                        }
                        subscriptionBottomSheet.show(supportFragmentManager, EventOutcomeSubscriptionBottomSheetFragment.TAG)
                    }
                }
            }
        } else {
            binding.reviveSubscriberWrapper.visibility = View.GONE
            binding.unsubbedWrapper.visibility = View.GONE
            binding.subscriberBenefitUsedView.visibility = View.GONE
        }

        binding.reviveSubscriberButton.setOnClickListener {
            sharedPreferences.edit {
                putLong("last_sub_revive", Date().time)
            }
            lifecycleScope.launchCatching {
                delay(300)
                binding.reviveSubscriberWrapper.startAnimation(Animations.fadeOutAnimation())
            }
            lifecycleScope.launch(ExceptionHandler.coroutine()) {
                userRepository.updateUser("stats.hp", 1)
                HabiticaSnackbar.showSnackbar(
                    this@DeathActivity.snackbarContainer(), getString(R.string.subscriber_benefit_success_faint), HabiticaSnackbar.SnackbarDisplayType.SUCCESS, isSubscriberBenefit = true)
                delay(2000)
                finish()
            }
        }

        binding.restartButton.setOnClickListener {
            binding.restartButton.isEnabled = false
            lifecycleScope.launch(ExceptionHandler.coroutine()) {
                userRepository.revive()
                finish()
                delay(1000)
                (HabiticaBaseApplication.getInstance(this@DeathActivity)?.currentActivity as? SnackbarActivity)?.let {activity ->
                    HabiticaSnackbar.showSnackbar(
                        activity.snackbarContainer(), getString(R.string.subscriber_benefit_success_faint), HabiticaSnackbar.SnackbarDisplayType.SUCCESS, isSubscriberBenefit = true)
                }
            }
        }
        startAnimating()
    }

    private fun startAnimating() {
        binding.ghostView.startAnimation(Animations.bobbingAnimation())
        binding.heartView.post {
            makeCoins(305)
            makeCoins(160)
        }
    }

    private fun makeCoins(startAngle: Int) {
        val positionArray = intArrayOf(0, 0)
        binding.heartView.getLocationOnScreen(positionArray)
        ParticleSystem(
            binding.confettiContainer,
            14,
            BitmapDrawable(resources, HabiticaIconsHelper.imageOfGold()),
            5000
        )
            .setInitialRotationRange(0, 200)
            .setScaleRange(0.5f, 0.8f)
            .setSpeedRange(0.01f, 0.03f)
            .setFadeOut(4000, AccelerateInterpolator())
            .setSpeedModuleAndAngleRange(0.01f, 0.03f, startAngle, startAngle + 80)
            .emit(binding.root.width / 2, positionArray[1] + (binding.heartView.height / 2), 3, 6000)
    }

    override fun onBackPressed() {
        moveTaskToBack(true)
    }

    override fun snackbarContainer(): ViewGroup {
        return binding.snackbarContainer
    }
}

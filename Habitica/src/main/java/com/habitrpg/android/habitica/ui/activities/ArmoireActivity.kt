package com.habitrpg.android.habitica.ui.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.widget.FrameLayout
import android.widget.RelativeLayout
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.databinding.ActivityArmoireBinding
import com.habitrpg.android.habitica.helpers.AdHandler
import com.habitrpg.android.habitica.helpers.AdType
import com.habitrpg.android.habitica.helpers.Analytics
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.helpers.EventCategory
import com.habitrpg.android.habitica.helpers.HitType
import com.habitrpg.android.habitica.helpers.ReviewManager
import com.habitrpg.android.habitica.ui.fragments.purchases.EventOutcomeSubscriptionBottomSheetFragment
import com.habitrpg.android.habitica.ui.fragments.purchases.EventOutcomeSubscriptionBottomSheetFragment.Companion.EVENT_ARMOIRE_OPENED
import com.habitrpg.android.habitica.ui.viewmodels.MainUserViewModel
import com.habitrpg.android.habitica.ui.views.ads.AdButton
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaBottomSheetDialog
import com.habitrpg.common.habitica.extensionsCommon.dpToPx
import com.habitrpg.common.habitica.extensionsCommon.loadImage
import com.habitrpg.common.habitica.extensionsCommon.observeOnce
import com.habitrpg.common.habitica.helpers.Animations
import com.habitrpg.common.habitica.helpers.ExceptionHandler
import com.habitrpg.common.habitica.helpers.launchCatching
import com.habitrpg.common.habitica.viewsCommon.CircularProgressComposable
import com.plattysoft.leonids.ParticleSystem
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class ArmoireActivity : BaseActivity() {
    private var equipmentKey: String? = null
    private var gold: Double? = null
    private var hasAnimatedChanges: Boolean = false
    private lateinit var binding: ActivityArmoireBinding

    @Inject
    internal lateinit var inventoryRepository: InventoryRepository

    @Inject
    internal lateinit var appConfigManager: AppConfigManager

    @Inject
    lateinit var userViewModel: MainUserViewModel

    @Inject
    lateinit var reviewManager: ReviewManager

    override fun getLayoutResId(): Int = R.layout.activity_armoire

    private var hasUsedExtraArmoire = false
    private var lastType: String? = null
    private var lastKey: String? = null
    private var lastText: String? = null
    private var lastValue: String? = null

    override fun getContentView(layoutResId: Int?): View {
        binding = ActivityArmoireBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean("hasUsedExtraArmoire", hasUsedExtraArmoire)
        outState.putString("lastType", lastType)
        outState.putString("lastKey", lastKey)
        outState.putString("lastText", lastText)
        outState.putString("lastValue", lastValue)
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        hasUsedExtraArmoire = savedInstanceState.getBoolean("hasUsedExtraArmoire")
        lastType = savedInstanceState.getString("lastType")
        lastKey = savedInstanceState.getString("lastKey")
        lastText = savedInstanceState.getString("lastText")
        lastValue = savedInstanceState.getString("lastValue")
        lastType?.let {
            configure(lastType ?: "", lastKey ?: "", lastText ?: "", lastValue)
        }
        if (hasUsedExtraArmoire) {
            if (binding.adButton.visibility == View.VISIBLE) {
                binding.adButton.visibility = View.INVISIBLE
            } else binding.openArmoireSubscriberWrapper.visibility = View.INVISIBLE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.goldView.currency = "gold"
        binding.goldView.animationDuration = 1000
        binding.goldView.animationDelay = 500
        binding.goldView.minForAbbrevation = 1000000
        binding.goldView.decimals = 0

        userViewModel.user.observe(this) { user ->

            if (gold == null) gold = user?.stats?.gp

            lifecycleScope.launchCatching {
                val remaining = inventoryRepository.getArmoireRemainingCount().firstOrNull() ?: 0
                binding.equipmentCountView.text = getString(R.string.equipment_remaining, remaining)
                binding.noEquipmentView.visibility = if (remaining > 0) View.GONE else View.VISIBLE
            }
        }

        binding.progressView.setContent {
            CircularProgressComposable(indicatorSize = 60.dp)
        }

        if (appConfigManager.enableArmoireAds()) {
            val handler =
                AdHandler(this, AdType.ARMOIRE) {

                    if (!it) return@AdHandler

                    giveUserArmoire()
                }
            handler.prepare {
                if (it && binding.adButton.state == AdButton.State.LOADING) {
                    binding.adButton.state = AdButton.State.READY
                } else if (!it) {
                    binding.adButton.visibility = View.INVISIBLE
                }
            }
            binding.adButton.updateForAdType(AdType.ARMOIRE, lifecycleScope)
            binding.adButton.setOnClickListener {
                binding.adButton.state = AdButton.State.LOADING
                handler.show()
            }
        } else binding.adButton.visibility = View.GONE

        if (appConfigManager.enableArmoireSubs()) {
            userViewModel.user.observe(this) {
                if (it?.isSubscribed == true && binding.openArmoireSubscriberWrapper.visibility != View.INVISIBLE) {
                    binding.openArmoireSubscriberWrapper.visibility = View.VISIBLE
                    binding.unsubbedWrapper.visibility = View.GONE
                    binding.dropRateButton.visibility = View.VISIBLE
                } else if (it?.isSubscribed == false) {
                    binding.openArmoireSubscriberWrapper.visibility = View.GONE
                    binding.unsubbedWrapper.visibility = View.VISIBLE
                    binding.dropRateButton.visibility = View.GONE
                }
            }
        } else {
            binding.openArmoireSubscriberWrapper.visibility = View.GONE
            binding.unsubbedWrapper.visibility = View.GONE
            binding.dropRateButton.visibility = View.VISIBLE
        }

        binding.openArmoireSubscriberButton.setOnClickListener {
            Analytics.sendEvent("Free armoire perk", EventCategory.BEHAVIOUR, HitType.EVENT)
            giveUserArmoire()
            lifecycleScope.launchCatching {
                delay(400)
                binding.openArmoireSubscriberWrapper.startAnimation(Animations.fadeOutAnimation())
            }
        }

        binding.subscribeModalButton.setOnClickListener {
            Analytics.sendEvent("View armoire sub CTA", EventCategory.BEHAVIOUR, HitType.EVENT)
            val subscriptionBottomSheet =
                EventOutcomeSubscriptionBottomSheetFragment().apply {
                    eventType = EVENT_ARMOIRE_OPENED
                }
            subscriptionBottomSheet.show(
                supportFragmentManager,
                EventOutcomeSubscriptionBottomSheetFragment.TAG,
            )
        }

        binding.closeButton.setOnClickListener {
            finish()
        }
        binding.equipButton.setOnClickListener {
            equipmentKey?.let { it1 ->
                MainScope().launchCatching { inventoryRepository.equip("equipped", it1) }
            }
            finish()
        }
        binding.dropRateButton.setOnClickListener {
            showDropRateDialog()
        }
        binding.dropRateButtonUnsubbed.setOnClickListener {
            showDropRateDialog()
        }
        intent.extras?.let {
            val args = ArmoireActivityArgs.fromBundle(it)
            equipmentKey = args.key
            configure(args.type, args.key, args.text, args.value)

            if (args.type == "gear") {
                userViewModel.user.observeOnce(this) { user ->
                    user?.loginIncentives?.let { totalCheckins ->
                        reviewManager.requestReview(this@ArmoireActivity, totalCheckins)
                    }
                }
            }
        }
    }

    private fun giveUserArmoire(): Boolean {
        if (hasUsedExtraArmoire)  return false

        hasUsedExtraArmoire = true
        binding.iconWrapper.post {
            binding.iconView.bitmap = null
            Animations.circularHide(binding.iconWrapper)
        }
        binding.progressView.animate().apply {
            alpha(1f)
            startDelay = 0
            start()
        }
        binding.titleView.animate().apply {
            alpha(0f)
            duration = 300
            startDelay = 0
            start()
        }
        binding.subtitleView.animate().apply {
            alpha(0f)
            duration = 300
            startDelay = 0
            start()
        }

        binding.goldView.animate().apply {
            alpha(0f)
            start()
        }

        val user = userViewModel.user.value ?: return true
        val currentGold = user.stats?.gp ?: return true
        if (binding.adButton.visibility == View.VISIBLE) {
            binding.adButton.state = AdButton.State.UNAVAILABLE
            binding.adButton.visibility = View.INVISIBLE
        } else if (binding.openArmoireSubscriberWrapper.visibility == View.VISIBLE) {
            binding.openArmoireSubscriberWrapper.visibility = View.INVISIBLE
        }
        lifecycleScope.launch(ExceptionHandler.coroutine()) {
            userRepository.updateUser("stats.gp", currentGold + 100)
            val buyResponse =
                inventoryRepository.buyItem(user, "armoire", 0.0, 1) ?: return@launch
            configure(
                buyResponse.armoire["type"] ?: "",
                buyResponse.armoire["dropKey"] ?: "",
                buyResponse.armoire["dropText"] ?: "",
                buyResponse.armoire["value"] ?: "",
            )
            hasAnimatedChanges = false
            gold = null
            startAnimation(false)
        }
        return false
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launchCatching {
            delay(500L)
            startAnimation(true)
        }
    }

    private fun startAnimation(decreaseGold: Boolean) {
        val gold = gold?.toInt()
        if (hasAnimatedChanges) return
        if (gold != null && decreaseGold) {
            /**
             * We are adding 100 as the gold is already "deducted" before the animation starts,
             * and animating to show the current user's gold amount.
             */
            binding.goldView.value = (gold + 100).toDouble()
            binding.goldView.value = (gold).toDouble()
        }

        binding.progressView.animate().apply {
            alpha(0f)
            startDelay = 0
            start()
        }

        val container = binding.confettiAnchor
        container.postDelayed(
            {
                createParticles(container, R.drawable.confetti_blue)
                createParticles(container, R.drawable.confetti_red)
                createParticles(container, R.drawable.confetti_yellow)
                createParticles(container, R.drawable.confetti_purple)
            },
            500,
        )

        binding.iconView.startAnimation(Animations.bobbingAnimation())
        binding.titleView.alpha = 0f
        binding.subtitleView.alpha = 0f

        binding.iconWrapper.post {
            Animations.circularReveal(binding.iconWrapper, 300)
        }

        binding.leftSparkView.startAnimating()
        binding.rightSparkView.startAnimating()

        binding.titleView.animate().apply {
            alpha(1f)
            duration = 300
            startDelay = 600
            start()
        }
        binding.subtitleView.animate().apply {
            alpha(1f)
            duration = 300
            startDelay = 900
            start()
        }

        hasAnimatedChanges = true
    }

    private fun createParticles(
        container: FrameLayout,
        resource: Int,
    ) {
        ParticleSystem(
            container,
            30,
            ContextCompat.getDrawable(this, resource),
            6000,
        )
            .setRotationSpeed(144f)
            .setScaleRange(1.0f, 1.6f)
            .setSpeedByComponentsRange(-0.15f, 0.15f, 0.15f, 0.45f)
            .setFadeOut(200, AccelerateInterpolator())
            .emitWithGravity(binding.confettiAnchor, Gravity.TOP, 15, 2000)
    }

    private fun configure(
        type: String,
        key: String,
        text: String,
        value: String? = "",
    ) {
        lastType = type
        lastKey = key
        lastText = text
        lastValue = value

        binding.titleView.text =
            text.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        binding.equipButton.visibility = if (type == "gear") View.VISIBLE else View.GONE
        when (type) {
            "gear" -> {
                binding.subtitleView.text = getString(R.string.armoireEquipment_new)
                binding.iconView.loadImage("shop_$key")
            }

            "food" -> {
                binding.subtitleView.text = getString(R.string.armoireFood_new)
                binding.iconView.loadImage("Pet_Food_$key")
            }

            else -> {
                @SuppressLint("SetTextI18n")
                binding.titleView.text = "+$value ${binding.titleView.text}"
                binding.subtitleView.text = getString(R.string.armoireExp)
                binding.iconView.setImageResource(R.drawable.armoire_experience)
                val layoutParams = RelativeLayout.LayoutParams(108.dpToPx(this), 122.dpToPx(this))
                layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT)
                binding.iconView.layoutParams = layoutParams
            }
        }
    }

    private fun showDropRateDialog() {
        val dialog = HabiticaBottomSheetDialog(this)
        dialog.setContentView(R.layout.armoire_drop_rate_dialog)
        dialog.show()
    }
}

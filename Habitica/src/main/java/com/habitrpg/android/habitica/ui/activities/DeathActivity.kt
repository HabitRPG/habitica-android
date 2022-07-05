package com.habitrpg.android.habitica.ui.activities

import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AccelerateInterpolator
import androidx.lifecycle.lifecycleScope
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.databinding.ActivityDeathBinding
import com.habitrpg.android.habitica.extensions.fromHtml
import com.habitrpg.android.habitica.extensions.observeOnce
import com.habitrpg.android.habitica.helpers.AdHandler
import com.habitrpg.android.habitica.helpers.AdType
import com.habitrpg.common.habitica.helpers.Animations
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.ui.viewmodels.MainUserViewModel
import com.habitrpg.android.habitica.ui.views.ads.AdButton
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import com.plattysoft.leonids.ParticleSystem
import javax.inject.Inject

class DeathActivity: BaseActivity() {
    private lateinit var binding: ActivityDeathBinding

    @Inject
    internal lateinit var inventoryRepository: InventoryRepository
    @Inject
    internal lateinit var appConfigManager: AppConfigManager
    @Inject
    lateinit var userViewModel: MainUserViewModel

    override fun getLayoutResId(): Int = R.layout.activity_armoire

    override fun injectActivity(component: UserComponent?) {
        component?.inject(this)
    }

    override fun getContentView(): View {
        binding = ActivityDeathBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userViewModel.user.observeOnce(this) { user ->
            binding.lossDescription.text = getString(R.string.faint_loss_description, (user?.stats?.lvl ?: 2).toInt() - 1, user?.stats?.gp?.toInt()).fromHtml()
        }

        if (appConfigManager.enableArmoireAds()) {
            val handler = AdHandler(this, AdType.FAINT) {
                if (!it) {
                    return@AdHandler
                }
                Log.d("AdHandler", "Reviving user")
                compositeSubscription.add(
                        userRepository.updateUser("stats.hp", 1).subscribe({
                                                                           finish()
                        }, RxErrorHandler.handleEmptyError())
                )
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
        } else {
            binding.adButton.visibility = View.GONE
        }

        binding.restartButton.setOnClickListener {
            binding.restartButton.isEnabled = false
            userRepository.revive().subscribe({
                finish()
            }, RxErrorHandler.handleEmptyError())
        }
        startAnimating()
    }

    private fun startAnimating() {
        binding.ghostView.startAnimation(Animations.bobbingAnimation())
        makeCoins(305)
        makeCoins(160)
    }

    private fun makeCoins(startAngle: Int) {
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
            .setSpeedModuleAndAngleRange(0.01f, 0.03f, startAngle, startAngle+80)
            .emit(550, 680, 3, 6000)

    }

    override fun onBackPressed() {
        moveTaskToBack(true)
    }
}
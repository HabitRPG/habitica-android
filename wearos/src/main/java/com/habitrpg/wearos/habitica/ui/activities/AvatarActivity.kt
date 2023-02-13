package com.habitrpg.wearos.habitica.ui.activities

import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.ViewOutlineProvider
import android.widget.FrameLayout
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.habitrpg.android.habitica.databinding.ActivityAvatarBinding
import com.habitrpg.common.habitica.extensions.dpToPx
import com.habitrpg.wearos.habitica.ui.viewmodels.AvatarViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import java.lang.Integer.max
import kotlin.math.roundToInt

@AndroidEntryPoint
class AvatarActivity : BaseActivity<ActivityAvatarBinding, AvatarViewModel>() {
    override val viewModel: AvatarViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityAvatarBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            viewModel.user.filterNotNull().collect {
                binding.avatarView.setAvatar(it)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        scaleAvatar()
    }

    private fun scaleAvatar() {
        val params = binding.root.layoutParams as FrameLayout.LayoutParams
        val maxSize = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            max(
                windowManager.currentWindowMetrics.bounds.bottom,
                windowManager.currentWindowMetrics.bounds.right
            )
        } else {
            @Suppress("DEPRECATION")
            max(windowManager.defaultDisplay.width, windowManager.defaultDisplay.height)
        }
        var factor = (maxSize / 46f) / 3f
        var viewSize = 138 * factor.roundToInt()
        if (maxSize - viewSize > 20.dpToPx(this)) {
            viewSize += 46
            factor += 1
        }
        params.width = viewSize
        params.height = viewSize
        params.gravity = Gravity.CENTER
        binding.root.layoutParams = params

        val avatarParams = binding.avatarView.layoutParams as FrameLayout.LayoutParams
        avatarParams.width = 141 * factor.roundToInt()
        avatarParams.height = 147 * factor.roundToInt()
        binding.avatarView.layoutParams = avatarParams

        binding.root.outlineProvider = ViewOutlineProvider.BACKGROUND
        binding.root.clipToOutline = true
    }
}

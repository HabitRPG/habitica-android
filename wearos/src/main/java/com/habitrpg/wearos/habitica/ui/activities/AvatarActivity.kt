package com.habitrpg.wearos.habitica.ui.activities

import android.os.Bundle
import androidx.activity.viewModels
import com.habitrpg.wearos.habitica.databinding.ActivityAvatarBinding
import com.habitrpg.wearos.habitica.ui.viewmodels.AvatarViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AvatarActivity: BaseActivity<ActivityAvatarBinding, AvatarViewModel>() {
    override val viewModel: AvatarViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityAvatarBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)

        viewModel.user.observe(this) {
            binding.root.setAvatar(it)
        }
    }
}
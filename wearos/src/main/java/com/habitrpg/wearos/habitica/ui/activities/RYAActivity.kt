package com.habitrpg.wearos.habitica.ui.activities

import android.os.Bundle
import androidx.activity.viewModels
import com.habitrpg.android.habitica.databinding.ActivityRyaBinding
import com.habitrpg.wearos.habitica.ui.viewmodels.RYAViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RYAActivity: BaseActivity<ActivityRyaBinding, RYAViewModel>() {
    override val viewModel: RYAViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityRyaBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
    }
}
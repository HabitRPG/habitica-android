package com.habitrpg.wearos.habitica.ui.activities

import android.os.Bundle
import androidx.activity.viewModels
import com.habitrpg.android.habitica.databinding.ActivityTaskFormBinding
import com.habitrpg.wearos.habitica.ui.viewmodels.TaskFormViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TaskFormActivity: BaseActivity<ActivityTaskFormBinding, TaskFormViewModel>() {
    override val viewModel: TaskFormViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityTaskFormBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
    }
}
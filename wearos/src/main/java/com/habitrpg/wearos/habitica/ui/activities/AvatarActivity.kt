package com.habitrpg.wearos.habitica.ui.activities

import android.os.Bundle
import com.habitrpg.wearos.habitica.databinding.ActivityAvatarBinding

class AvatarActivity: BaseActivity<ActivityAvatarBinding>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityAvatarBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
    }
}
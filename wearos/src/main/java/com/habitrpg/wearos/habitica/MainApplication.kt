package com.habitrpg.wearos.habitica

import android.app.Application
import com.habitrpg.common.habitica.extensions.setupCoil
import com.habitrpg.common.habitica.helpers.MarkdownParser
import com.habitrpg.common.habitica.views.HabiticaIconsHelper
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        HabiticaIconsHelper.init(this)
        MarkdownParser.setup(this)
        setupCoil()
    }
}
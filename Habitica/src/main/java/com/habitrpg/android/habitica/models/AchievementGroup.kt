package com.habitrpg.android.habitica.models

import com.habitrpg.shared.habitica.data.models.Achievement
import java.util.*

class AchievementGroup {
    var label: String? = null
    var achievements: HashMap<String, Achievement>? = null
}

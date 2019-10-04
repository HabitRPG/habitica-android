package com.habitrpg.shared.habitica.models.members

import com.habitrpg.shared.habitica.nativeLibraries.NativeContext


expect class PlayerTier(title: String, id: Int) {
    var title: String
    var id: Int

    companion object {
        fun getTiers(): List<PlayerTier>

        fun getColorForTier(context: NativeContext, value: Int): Int
    }
}
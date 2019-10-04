package com.habitrpg.shared.habitica.models.members

import com.habitrpg.shared.habitica.nativeLibraries.NativeContext

actual class PlayerTier actual constructor(title: String, id: Int) {
    actual companion object {
        actual fun getTiers(): List<PlayerTier> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        actual fun getColorForTier(context: NativeContext, value: Int): Int {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

    }

}
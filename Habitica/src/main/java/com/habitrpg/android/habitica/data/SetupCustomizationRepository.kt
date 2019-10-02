package com.habitrpg.android.habitica.data


import com.habitrpg.android.habitica.models.SetupCustomization
import com.habitrpg.shared.habitica.models.user.User

interface SetupCustomizationRepository {

    fun getCustomizations(type: String, user: User): List<SetupCustomization>
    fun getCustomizations(type: String, subtype: String?, user: User): List<SetupCustomization>
}

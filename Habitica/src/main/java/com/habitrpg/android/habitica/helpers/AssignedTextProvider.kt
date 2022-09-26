package com.habitrpg.android.habitica.helpers

import android.content.res.Resources

interface AssignedTextProvider {
    fun textForTask(resources: Resources, assignedUsers: List<String>): String
}

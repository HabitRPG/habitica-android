/*
Copyright 2015 Michal Pawlowski

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.habitrpg.android.habitica.ui.helpers

import android.app.Activity
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Build
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.appcompat.widget.ActionMenuView
import androidx.appcompat.widget.Toolbar
import com.google.android.material.appbar.AppBarLayout
import com.habitrpg.android.habitica.R
import com.habitrpg.common.habitica.extensions.getThemeColor
import com.habitrpg.common.habitica.extensions.waitForLayout

/**
 * Helper class that iterates through Toolbar views, and sets dynamically icons and texts color
 * Created by chomi3 on 2015-01-19.
 */
object ToolbarColorHelper {
    /**
     * Use this method to colorize toolbar icons to the desired target color
     * @param toolbar toolbar view being colored
     * @param activity reference to activity needed to register observers
     */
    fun colorizeToolbar(
        toolbar: Toolbar,
        activity: Activity?,
        iconColor: Int? = null,
        backgroundColor: Int? = null,
        appbar: AppBarLayout? = null
    ) {
        if (activity == null) return
        toolbar.setBackgroundColor(
            backgroundColor ?: activity.getThemeColor(R.attr.headerBackgroundColor)
        )
        appbar?.setBackgroundColor(backgroundColor ?: activity.getThemeColor(R.attr.headerBackgroundColor))
        val toolbarIconsColor = iconColor ?: activity.getThemeColor(R.attr.headerTextColor)
        val colorFilter = PorterDuffColorFilter(toolbarIconsColor, PorterDuff.Mode.MULTIPLY)
        for (i in 0 until toolbar.childCount) {
            when (val v = toolbar.getChildAt(i)) {
                is ImageButton -> {
                    v.drawable.colorFilter = colorFilter
                }

                is ActionMenuView -> {
                    for (j in 0 until v.childCount) {
                        colorizeChild(v.getChildAt(j), toolbarIconsColor, colorFilter)
                    }
                }

                is TextView -> {
                    v.setTextColor(toolbarIconsColor)
                    v.setBackgroundColor(backgroundColor ?: activity.getThemeColor(R.attr.headerBackgroundColor))
                }
            }
        }

        toolbar.setTitleTextColor(toolbarIconsColor)
        toolbar.setSubtitleTextColor(toolbarIconsColor)

        setOverflowButtonColor(activity, toolbarIconsColor)
    }

    private fun colorizeChild(
        innerView: View,
        toolbarIconsColor: Int,
        colorFilter: PorterDuffColorFilter
    ) {
        if (innerView is ActionMenuItemView) {
            innerView.setTextColor(toolbarIconsColor)
            for (k in innerView.compoundDrawables.indices) {
                innerView.post {
                    if (innerView.compoundDrawables[k] != null) {
                        innerView.compoundDrawables[k].colorFilter = colorFilter
                    }
                }
            }
        }
    }

    /**
     * It's important to set overflowDescription atribute in styles, so we can grab the reference
     * to the overflow icon. Check: res/values/styles.xml
     * @param activity
     * @param color
     */
    private fun setOverflowButtonColor(
        activity: Activity,
        color: Int
    ) {
        val overflowDescription = activity.getString(R.string.abc_action_menu_overflow_description)
        activity.window.decorView.waitForLayout {
            val outViews = ArrayList<View>()
            findViewsWithText(
                outViews,
                overflowDescription,
                View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION
            )
            if (outViews.isEmpty()) {
                return@waitForLayout
            }
            val overflowViewParent = outViews[0].parent as? ActionMenuView
            overflowViewParent?.overflowIcon?.setTint(color)
            overflowViewParent?.overflowIcon?.setTintMode(PorterDuff.Mode.SRC_ATOP)
        }
    }
}

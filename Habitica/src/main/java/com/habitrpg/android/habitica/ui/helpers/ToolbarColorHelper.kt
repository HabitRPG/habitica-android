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
import androidx.preference.PreferenceManager
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.getThemeColor
import com.habitrpg.android.habitica.extensions.waitForLayout
import java.util.*

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
    fun colorizeToolbar(toolbar: Toolbar, activity: Activity?, overrideModernHeader: Boolean? = null) {
        if (activity == null) return
        val modernHeaderStyle = overrideModernHeader ?: PreferenceManager.getDefaultSharedPreferences(activity).getBoolean("modern_header_style", true)
        val toolbarIconsColor = if (modernHeaderStyle) {
            toolbar.setBackgroundColor(activity.getThemeColor(R.attr.headerBackgroundColor))
            activity.getThemeColor(R.attr.headerTextColor)
        } else {
            toolbar.setBackgroundColor(activity.getThemeColor(R.attr.colorPrimary))
            activity.getThemeColor(R.attr.toolbarContentColor)
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            activity.window.statusBarColor = activity.getThemeColor(R.attr.colorPrimaryDark)
        }
        val colorFilter = PorterDuffColorFilter(toolbarIconsColor, PorterDuff.Mode.MULTIPLY)
        for (i in 0 until toolbar.childCount) {
            val v = toolbar.getChildAt(i)

            // Step 1 : Changing the color of back button (or open drawer button).
            if (v is ImageButton) {
                // Action Bar back button
                v.drawable.colorFilter = colorFilter
            } else if (v is ActionMenuView) {
                for (j in 0 until v.childCount) {

                    // Step 2: Changing the color of any ActionMenuViews - icons that are not back button, nor text, nor overflow menu icon.
                    // Colorize the ActionViews -> all icons that are NOT: back button | overflow menu
                    val innerView = v.getChildAt(j)
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
            } else if (v is TextView) {
                v.setTextColor(toolbarIconsColor)
            }
        }

        // Step 3: Changing the color of title and subtitle.
        toolbar.setTitleTextColor(toolbarIconsColor)
        toolbar.setSubtitleTextColor(toolbarIconsColor)

        // Step 4: Changing the color of the Overflow Menu icon.
        setOverflowButtonColor(activity, toolbarIconsColor)
    }

    /**
     * It's important to set overflowDescription atribute in styles, so we can grab the reference
     * to the overflow icon. Check: res/values/styles.xml
     * @param activity
     * @param color
     */
    private fun setOverflowButtonColor(activity: Activity, color: Int) {
        val overflowDescription = activity.getString(R.string.abc_action_menu_overflow_description)
        activity.window.decorView.waitForLayout {
            val outViews = ArrayList<View>()
            findViewsWithText(
                outViews, overflowDescription,
                View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION
            )
            if (outViews.isEmpty()) {
                return@waitForLayout
            }
            val overflowViewParent = outViews[0].parent as ActionMenuView
            overflowViewParent.overflowIcon?.setTint(color)
            overflowViewParent.overflowIcon?.setTintMode(PorterDuff.Mode.SRC_ATOP)
        }
    }
}

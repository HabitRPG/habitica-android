package com.habitrpg.android.habitica.ui.helpers

import android.content.Context
import android.content.res.Resources
import android.graphics.Point
import android.os.Build
import android.support.annotation.IntRange
import android.util.DisplayMetrics
import android.view.Display
import android.view.KeyCharacterMap
import android.view.KeyEvent
import android.view.ViewConfiguration
import android.view.WindowManager

import com.habitrpg.android.habitica.R

object NavbarUtils {

    private const val RESOURCE_NOT_FOUND = 0

    @IntRange(from = 0)
    fun getNavbarHeight(context: Context): Int {
        val res = context.resources
        val navBarIdentifier = res.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (navBarIdentifier != RESOURCE_NOT_FOUND)
            res.getDimensionPixelSize(navBarIdentifier)
        else
            0
    }

    internal fun shouldDrawBehindNavbar(context: Context): Boolean {
        return isPortrait(context) && hasSoftKeys(context)
    }

    private fun isPortrait(context: Context): Boolean {
        val res = context.resources
        return res.getBoolean(R.bool.is_portrait_mode)
    }

    /**
     * http://stackoverflow.com/a/14871974
     */
    fun hasSoftKeys(context: Context): Boolean {
        var hasSoftwareKeys = true

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            val d = (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay

            val realDisplayMetrics = DisplayMetrics()
            d.getRealMetrics(realDisplayMetrics)

            val realHeight = realDisplayMetrics.heightPixels
            val realWidth = realDisplayMetrics.widthPixels

            val displayMetrics = DisplayMetrics()
            d.getMetrics(displayMetrics)

            val displayHeight = displayMetrics.heightPixels
            val displayWidth = displayMetrics.widthPixels

            hasSoftwareKeys = realWidth - displayWidth > 0 || realHeight - displayHeight > 0
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            val hasMenuKey = ViewConfiguration.get(context).hasPermanentMenuKey()
            val hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK)
            hasSoftwareKeys = !hasMenuKey && !hasBackKey
        }

        return hasSoftwareKeys
    }

    fun isBehindNavbar(parentLocation: IntArray, context: Context): Boolean {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay
        val size = Point()
        display.getSize(size)
        return parentLocation[1] > size.y - getNavbarHeight(context)
    }
}

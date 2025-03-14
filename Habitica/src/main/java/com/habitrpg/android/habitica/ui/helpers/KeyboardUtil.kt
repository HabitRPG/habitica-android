package com.habitrpg.android.habitica.ui.helpers

/*
 * Copyright 2015 Mike Penz All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.app.Activity
import android.util.Log
import android.view.inputmethod.InputMethodManager
import androidx.core.graphics.Insets

interface OnImeVisibilityChangedListener {
    fun onImeVisibilityChanged(visible: Boolean, height: Int, safeInsets: Insets)
}

class KeyboardUtil {
    companion object {
        private val imeListeners = mutableListOf<OnImeVisibilityChangedListener>()

        fun dismissKeyboard(act: Activity?) {
            if (act != null && act.currentFocus != null) {
                val inputMethodManager =
                    act.getSystemService(Activity.INPUT_METHOD_SERVICE) as? InputMethodManager
                inputMethodManager?.hideSoftInputFromWindow(act.currentFocus?.windowToken, 0)
            }
        }

        private var lastVisible = false
        private var lastHeight = 0
        fun updatePaddingForIme(isVisible: Boolean, height: Int, safeInsets: Insets) {
            if (lastVisible == isVisible && lastHeight == height) {
                return
            }
            lastVisible = isVisible
            lastHeight = height
            Log.e("KeyboardUtil", "updatePaddingForIme: isVisible = $isVisible, height = $height")
            for (listener in imeListeners) {
                Log.e("KeyboardUtil", "updatePaddingForIme: listener = $listener")
                listener.onImeVisibilityChanged(isVisible, height, safeInsets)
            }
        }

        fun addImeVisibilityListener(listener: OnImeVisibilityChangedListener) {
            Log.e("KeyboardUtil", "addImeVisibilityListener: listener = $listener")
            imeListeners.add(listener)
        }

        fun removeImeVisibilityListener(listener: OnImeVisibilityChangedListener) {
            Log.e("KeyboardUtil", "removeImeVisibilityListener: listener = $listener")
            imeListeners.remove(listener)
        }
    }
}

fun Activity.dismissKeyboard() {
    KeyboardUtil.dismissKeyboard(this)
}

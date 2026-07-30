/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.habitrpg.wearos.habitica.ui.views

import android.view.View
import android.widget.TextView
import androidx.wear.widget.CurvedTextView

/**
 * A wrapper around a [TextView] like object, that may not actually extend [TextView] (like a [CurvedTextView]).
 */
interface TextViewWrapper {
    val view: View
    var text: CharSequence?
    var textColor: Int
}

/**
 * A [TextViewWrapper] wrapping a [CurvedTextView].
 */
class CurvedTextViewWrapper(
    override val view: CurvedTextView,
) : TextViewWrapper {
    override var text: CharSequence?
        get() = view.text
        set(value) {
            view.text = value?.toString().orEmpty()
        }

    override var textColor: Int
        get() = view.textColor
        set(value) {
            view.textColor = value
        }
}

/**
 * A [TextViewWrapper] wrapping a [TextView].
 */
class NormalTextViewWrapper(
    override val view: TextView,
) : TextViewWrapper {
    override var text: CharSequence?
        get() = view.text
        set(value) {
            view.text = value
        }

    override var textColor: Int
        get() = view.currentTextColor
        set(value) {
            view.setTextColor(value)
        }
}

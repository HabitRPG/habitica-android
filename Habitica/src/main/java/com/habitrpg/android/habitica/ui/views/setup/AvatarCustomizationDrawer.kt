package com.habitrpg.android.habitica.ui.views.setup


import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.inflate

class AvatarCustomizationDrawer(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    init {
        inflate(R.layout.avatar_setup_drawer, true)
    }
}

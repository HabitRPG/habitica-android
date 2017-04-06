package com.habitrpg.android.habitica.ui.views.setup;


import com.habitrpg.android.habitica.R;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class AvatarCustomizationDrawer extends LinearLayout {
    public AvatarCustomizationDrawer(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        inflate(context, R.layout.avatar_setup_drawer, this);
    }


}

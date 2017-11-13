package com.habitrpg.android.habitica.ui.views.social;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.ui.helpers.NavbarUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChatBarView extends FrameLayout {

    @BindView(R.id.chatBarContainer)
    LinearLayout chatBarContainer;

    private boolean navBarAccountedHeightCalculated = false;

    public ChatBarView(@NonNull Context context) {
        super(context);
        setupView(context);
    }

    public ChatBarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setupView(context);
    }

    private void setupView(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.tavern_chat_new_entry_item, this);

        ButterKnife.bind(this, this);

        this.setBackgroundResource(R.color.white);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (changed) {
            resizeForDrawingUnderNavbar();
        }
        super.onLayout(changed, left, top, right, bottom);
    }

    //https://github.com/roughike/BottomBar/blob/master/bottom-bar/src/main/java/com/roughike/bottombar/BottomBar.java#L834
    private void resizeForDrawingUnderNavbar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int currentHeight = getHeight();

            if (!navBarAccountedHeightCalculated) {
                navBarAccountedHeightCalculated = true;

                int navbarHeight = NavbarUtils.getNavbarHeight(getContext());
                ViewGroup.LayoutParams params = getLayoutParams();
                params.height = currentHeight + navbarHeight;
                setLayoutParams(params);
            }
        }
    }
}

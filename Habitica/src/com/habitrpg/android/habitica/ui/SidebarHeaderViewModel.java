package com.habitrpg.android.habitica.ui;

import android.view.View;
import android.widget.TextView;

import com.habitrpg.android.habitica.R;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Negue on 15.06.2015.
 */
public class SidebarHeaderViewModel {
    @InjectView(R.id.userNameText)
    TextView userName;

    @InjectView(R.id.goldText)
    TextView goldText;

    @InjectView(R.id.silverText)
    TextView silverText;

    public SidebarHeaderViewModel(View v)
    {
        ButterKnife.inject(this, v);
    }

    public void SetData(HabitRPGUser user)
    {
        userName.setText(user.getProfile().getName() + " - Lv" + user.getStats().getLvl());

        Double goldPoints = user.getStats().getGp();

        goldText.setText(goldPoints.intValue());
        silverText.setText((int) ((goldPoints - goldPoints.intValue()) * 100));
    }
}

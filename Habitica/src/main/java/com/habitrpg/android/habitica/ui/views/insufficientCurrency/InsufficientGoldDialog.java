package com.habitrpg.android.habitica.ui.views.insufficientCurrency;

import android.content.Context;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper;

public class InsufficientGoldDialog extends InsufficientCurrencyDialog {
    public InsufficientGoldDialog(Context context) {
        super(context);

        imageView.setImageBitmap(HabiticaIconsHelper.imageOfGoldReward());
        textView.setText(context.getString(R.string.insufficientGold));
        setTitle(R.string.insufficientSubscriberGemsTitle);
    }
}

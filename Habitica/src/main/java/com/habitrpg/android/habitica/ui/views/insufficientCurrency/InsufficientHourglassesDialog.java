package com.habitrpg.android.habitica.ui.views.insufficientCurrency;

import android.content.Context;
import android.support.v7.app.AlertDialog;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.events.commands.OpenGemPurchaseFragmentCommand;
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper;

import org.greenrobot.eventbus.EventBus;

public class InsufficientHourglassesDialog extends InsufficientCurrencyDialog {
    public InsufficientHourglassesDialog(Context context) {
        super(context);

        imageView.setImageBitmap(HabiticaIconsHelper.imageOfHourglassShop());
        textView.setText(R.string.insufficientHourglasses);


        setButton(AlertDialog.BUTTON_POSITIVE, context.getString(R.string.get_hourglasses), (dialogInterface, i) -> {
            EventBus.getDefault().post(new OpenGemPurchaseFragmentCommand());
        });
    }
}

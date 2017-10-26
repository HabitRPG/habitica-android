package com.habitrpg.android.habitica.ui.views.insufficientCurrency;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.habitrpg.android.habitica.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by phillip on 27.09.17.
 */

abstract public class InsufficientCurrencyDialog extends AlertDialog {

    @BindView(R.id.imageView)
    ImageView imageView;
    @BindView(R.id.textView)
    TextView textView;

    public InsufficientCurrencyDialog(Context context) {
        super(context);

        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_insufficient_currency, null);
        ButterKnife.bind(this, view);
        setView(view);

        this.setButton(AlertDialog.BUTTON_NEUTRAL, context.getString(R.string.close), (dialogInterface, i) -> {
            this.dismiss();
        });
    }

}
